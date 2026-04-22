import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Cron } from '@nestjs/schedule';
import * as admin from 'firebase-admin';
import { Appointment } from '../appointments/entities/appointment.entity';
import { User } from '../users/entities/user.entity';
import { AppointmentStatus } from '../common/enums';

@Injectable()
export class NotificationsService implements OnModuleInit {
  private logger = new Logger('NotificationsService');
  private firebaseInitialized = false;

  constructor(
    @InjectRepository(Appointment)
    private appointmentsRepo: Repository<Appointment>,
    @InjectRepository(User)
    private usersRepo: Repository<User>,
  ) {}

  onModuleInit() {
    const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT;
    if (!serviceAccountJson) {
      this.logger.warn(
        'FIREBASE_SERVICE_ACCOUNT env not set — push notifications disabled',
      );
      return;
    }
    try {
      if (!admin.apps.length) {
        const serviceAccount = JSON.parse(serviceAccountJson);
        admin.initializeApp({
          credential: admin.credential.cert(serviceAccount),
        });
      }
      this.firebaseInitialized = true;
      this.logger.log('Firebase Admin initialized');
    } catch (e) {
      this.logger.error('Firebase Admin init failed', e);
    }
  }

  async sendPushNotification(
    fcmToken: string,
    title: string,
    body: string,
    data?: Record<string, string>,
  ) {
    if (!fcmToken) return;

    if (!this.firebaseInitialized) {
      this.logger.log(`[FCM-MOCK] ${title}: ${body}`);
      return;
    }

    try {
      await admin.messaging().send({
        token: fcmToken,
        notification: { title, body },
        data: data ?? {},
        android: {
          priority: 'high',
          notification: {
            channelId: 'appointments',
            sound: 'default',
            priority: 'high',
          },
        },
      });
      this.logger.log(`[FCM] Sent "${title}" to ${fcmToken.substring(0, 10)}...`);
    } catch (e) {
      this.logger.error(`[FCM] Failed to send: ${e.message}`);
    }
  }

  async sendToUser(
    userId: string,
    title: string,
    body: string,
    data?: Record<string, string>,
  ) {
    const user = await this.usersRepo.findOne({ where: { id: userId } });
    if (user?.fcmToken) {
      await this.sendPushNotification(user.fcmToken, title, body, data);
    }
  }

  /**
   * Cron: runs every 5 minutes, sends 2-hour and 1-hour reminders.
   */
  @Cron('*/5 * * * *')
  async sendAppointmentReminders() {
    const now = new Date();
    const today = now.toISOString().split('T')[0];
    const nowMinutes = now.getHours() * 60 + now.getMinutes();

    const twoHourWindowStart = nowMinutes + 120;
    const twoHourWindowEnd = nowMinutes + 125;
    const oneHourWindowStart = nowMinutes + 60;
    const oneHourWindowEnd = nowMinutes + 65;

    const appointments = await this.appointmentsRepo.find({
      where: { date: today, status: AppointmentStatus.BOOKED },
      relations: ['patient', 'patient.user', 'doctor', 'location', 'familyMember', 'token'],
    });

    for (const apt of appointments) {
      const user = apt.patient?.user;
      if (!user?.fcmToken) continue;

      const slotStr = String(apt.timeSlot);
      const parts = slotStr.split(':');
      const h = parseInt(parts[0] ?? '0', 10);
      const m = parseInt(parts[1] ?? '0', 10);
      const slotMinutes = h * 60 + m;

      const locationName = apt.location?.name ?? 'the clinic';
      const doctorName = apt.doctor?.name ?? 'your doctor';
      const lat = apt.location?.latitude;
      const lng = apt.location?.longitude;
      const formattedTime = this.formatTime(apt.timeSlot);

      // ── 2-hour reminder ──
      if (slotMinutes >= twoHourWindowStart && slotMinutes < twoHourWindowEnd) {
        await this.sendPushNotification(
          user.fcmToken,
          '⏰ Appointment in 2 Hours',
          `Your appointment with Dr. ${doctorName} at ${locationName} is at ${formattedTime}. Please be ready!`,
          {
            type: 'appointment_reminder_2h',
            appointmentId: apt.id,
            doctorName,
            locationName,
            timeSlot: formattedTime,
          },
        );
      }

      // ── 1-hour reminder with navigation + token info ──
      if (slotMinutes >= oneHourWindowStart && slotMinutes < oneHourWindowEnd) {
        const currentToken = null; // updated in real-time via sendTokenUpdate
        const patientToken = apt.token?.tokenNumber ?? null;

        let body = `Your appointment with Dr. ${doctorName} at ${locationName} is at ${formattedTime} — 1 hour away.`;
        if (currentToken != null && patientToken) {
          body += ` Currently serving token ${currentToken}, your token is #${patientToken}.`;
        }
        body += ' Tap to get directions.';

        await this.sendPushNotification(
          user.fcmToken,
          '🏥 1 Hour to Appointment — Get Directions',
          body,
          {
            type: 'appointment_reminder_1h',
            appointmentId: apt.id,
            doctorName,
            locationName,
            timeSlot: formattedTime,
            latitude: lat != null ? String(lat) : '',
            longitude: lng != null ? String(lng) : '',
            currentToken: currentToken != null ? String(currentToken) : '',
            patientToken: patientToken != null ? String(patientToken) : '',
          },
        );
      }
    }
  }

  /**
   * Send token update notification when patient's turn is approaching (≤3 tokens ahead).
   */
  async sendTokenUpdate(
    patientUserId: string,
    currentToken: number,
    patientToken: number,
    doctorName: string,
    latitude?: number,
    longitude?: number,
  ) {
    const tokensAhead = patientToken - currentToken;
    if (tokensAhead <= 3 && tokensAhead > 0) {
      await this.sendToUser(
        patientUserId,
        '🔔 Your Turn is Approaching!',
        `Dr. ${doctorName} is now on token ${currentToken}. You are token #${patientToken} — ${tokensAhead} patient(s) ahead. Tap to navigate.`,
        {
          type: 'token_update',
          currentToken: String(currentToken),
          patientToken: String(patientToken),
          doctorName,
          latitude: latitude != null ? String(latitude) : '',
          longitude: longitude != null ? String(longitude) : '',
        },
      );
    }
  }

  private formatTime(timeSlot: unknown): string {
    const parts = String(timeSlot).split(':');
    const h = parseInt(parts[0] ?? '0', 10);
    const m = parts[1] ?? '00';
    const ampm = h >= 12 ? 'PM' : 'AM';
    const hour12 = h % 12 || 12;
    return `${hour12}:${m} ${ampm}`;
  }
}
