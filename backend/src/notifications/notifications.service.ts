import { Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, LessThanOrEqual, MoreThanOrEqual } from 'typeorm';
import { Cron, CronExpression } from '@nestjs/schedule';
import { Appointment } from '../appointments/entities/appointment.entity';
import { User } from '../users/entities/user.entity';
import { AppointmentStatus } from '../common/enums';

@Injectable()
export class NotificationsService {
  private logger = new Logger('NotificationsService');

  constructor(
    @InjectRepository(Appointment)
    private appointmentsRepo: Repository<Appointment>,
    @InjectRepository(User)
    private usersRepo: Repository<User>,
  ) {}

  /**
   * Send push notification via FCM.
   * In production, integrate with firebase-admin SDK.
   * This is a placeholder that logs the notification.
   */
  async sendPushNotification(
    fcmToken: string,
    title: string,
    body: string,
    data?: Record<string, string>,
  ) {
    // TODO: Integrate firebase-admin in production
    // For now, log the notification
    this.logger.log(
      `[FCM] To: ${fcmToken?.substring(0, 10)}... | Title: ${title} | Body: ${body}`,
    );

    // Production code:
    // import * as admin from 'firebase-admin';
    // await admin.messaging().send({
    //   token: fcmToken,
    //   notification: { title, body },
    //   data,
    // });
  }

  /**
   * Send notification to a user by userId.
   */
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
   * Cron: Send appointment reminders 2 hours before.
   * Runs every 15 minutes to check for upcoming appointments.
   */
  @Cron('*/15 * * * *')
  async sendAppointmentReminders() {
    const now = new Date();
    const twoHoursFromNow = new Date(now.getTime() + 2 * 60 * 60 * 1000);
    const reminderWindowEnd = new Date(
      now.getTime() + 2 * 60 * 60 * 1000 + 15 * 60 * 1000,
    );

    const today = now.toISOString().split('T')[0];
    const twoHoursTime = twoHoursFromNow.toTimeString().substring(0, 5);
    const windowEndTime = reminderWindowEnd.toTimeString().substring(0, 5);

    const appointments = await this.appointmentsRepo.find({
      where: {
        date: today,
        status: AppointmentStatus.BOOKED,
      },
      relations: ['patient', 'patient.user', 'doctor', 'location', 'familyMember'],
    });

    for (const apt of appointments) {
      // Check if time slot falls within the 2-hour reminder window
      if (apt.timeSlot >= twoHoursTime && apt.timeSlot < windowEndTime) {
        const user = apt.patient?.user;
        if (user?.fcmToken) {
          await this.sendPushNotification(
            user.fcmToken,
            'Appointment Reminder',
            `Your appointment with Dr. ${apt.doctor.name} is in ~2 hours at ${apt.location.name}. Token will be assigned upon arrival.`,
            {
              appointmentId: apt.id,
              type: 'appointment_reminder',
            },
          );
        }
      }
    }
  }

  /**
   * Send token update notification to a patient.
   */
  async sendTokenUpdate(
    patientUserId: string,
    currentToken: number,
    patientToken: number,
    doctorName: string,
  ) {
    const tokensAhead = patientToken - currentToken;
    if (tokensAhead <= 3 && tokensAhead > 0) {
      await this.sendToUser(
        patientUserId,
        'Your Turn is Approaching!',
        `Dr. ${doctorName} is now serving token ${currentToken}. You are token ${patientToken} (${tokensAhead} ahead).`,
        { type: 'token_update' },
      );
    }
  }
}
