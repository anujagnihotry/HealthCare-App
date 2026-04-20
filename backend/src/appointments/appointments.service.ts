import {
  Injectable,
  BadRequestException,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, DataSource } from 'typeorm';
import { Appointment } from './entities/appointment.entity';
import { Token } from '../tokens/entities/token.entity';
import { DoctorsService } from '../doctors/doctors.service';
import { FamilyMembersService } from '../family-members/family-members.service';
import { BookAppointmentDto } from './dto';
import { AppointmentStatus, TokenStatus } from '../common/enums';

const BOOKING_WINDOW_DAYS = 6;
const MAX_EXTENDED_WINDOW_DAYS = 30;

/** DB `time` / TypeORM may return "HH:MM:SS"; slot grid uses "HH:MM". */
function toSlotTimeKey(time: string | Date): string {
  if (time instanceof Date) {
    return `${String(time.getHours()).padStart(2, '0')}:${String(
      time.getMinutes(),
    ).padStart(2, '0')}`;
  }
  const parts = String(time).split(':');
  const h = (parts[0] ?? '0').padStart(2, '0');
  const m = (parts[1] ?? '0').padStart(2, '0');
  return `${h}:${m}`;
}

@Injectable()
export class AppointmentsService {
  constructor(
    @InjectRepository(Appointment)
    private appointmentsRepo: Repository<Appointment>,
    @InjectRepository(Token)
    private tokensRepo: Repository<Token>,
    private doctorsService: DoctorsService,
    private familyMembersService: FamilyMembersService,
    private dataSource: DataSource,
  ) {}

  /**
   * Get the booking window for a doctor+location.
   * Default: next 6 days. If doctor is unavailable on some days,
   * extend the window to find enough available dates.
   */
  async getBookingWindow(doctorId: string, locationId: string) {
    const today = new Date();
    const availableDates: {
      date: string;
      slots: { time: string; sessionName: string; isBooked: boolean }[];
    }[] = [];

    let daysChecked = 0;
    let daysFound = 0;
    const targetDays = BOOKING_WINDOW_DAYS;

    while (
      daysFound < targetDays &&
      daysChecked < MAX_EXTENDED_WINDOW_DAYS
    ) {
      const checkDate = new Date(today);
      checkDate.setDate(today.getDate() + daysChecked + 1);
      const dateStr = checkDate.toISOString().split('T')[0];

      const slots = await this.doctorsService.getAvailableSlots(
        doctorId,
        locationId,
        dateStr,
      );

      if (slots.length > 0) {
        // Check which slots are already booked
        const bookedAppointments = await this.appointmentsRepo.find({
          where: {
            doctorId,
            locationId,
            date: dateStr,
            status: AppointmentStatus.BOOKED,
          },
          select: ['timeSlot'],
        });
        const bookedTimes = new Set(
          bookedAppointments.map((a) => toSlotTimeKey(a.timeSlot)),
        );

        const slotsWithStatus = slots.map((s) => ({
          ...s,
          isBooked: bookedTimes.has(toSlotTimeKey(s.time)),
        }));

        // Only include date if it has at least one available slot
        if (slotsWithStatus.some((s) => !s.isBooked)) {
          availableDates.push({ date: dateStr, slots: slotsWithStatus });
          daysFound++;
        }
      }

      daysChecked++;
    }

    const startDate =
      availableDates.length > 0 ? availableDates[0].date : null;
    const endDate =
      availableDates.length > 0
        ? availableDates[availableDates.length - 1].date
        : null;

    return {
      startDate,
      endDate,
      availableDates,
    };
  }

  /**
   * Book an appointment — transactional to prevent double-booking
   * and ensure correct token assignment.
   */
  async bookAppointment(patientId: string, dto: BookAppointmentDto) {
    return this.dataSource.transaction(async (manager) => {
      // Verify family member belongs to patient
      await this.familyMembersService.findOne(patientId, dto.familyMemberId);

      // Verify slot is valid
      const slots = await this.doctorsService.getAvailableSlots(
        dto.doctorId,
        dto.locationId,
        dto.date,
      );
      const slotExists = slots.some((s) => s.time === dto.timeSlot);
      if (!slotExists) {
        throw new BadRequestException(
          'This time slot is not available for the selected date',
        );
      }

      // Check for double-booking on same slot
      const existing = await manager.findOne(Appointment, {
        where: {
          doctorId: dto.doctorId,
          locationId: dto.locationId,
          date: dto.date,
          timeSlot: dto.timeSlot,
          status: AppointmentStatus.BOOKED,
        },
      });
      if (existing) {
        throw new ConflictException('This slot is already booked');
      }

      // Check if same family member already has appointment with same doctor on same date
      const duplicateBooking = await manager.findOne(Appointment, {
        where: {
          familyMemberId: dto.familyMemberId,
          doctorId: dto.doctorId,
          date: dto.date,
          status: AppointmentStatus.BOOKED,
        },
      });
      if (duplicateBooking) {
        throw new ConflictException(
          'This family member already has an appointment with this doctor on this date',
        );
      }

      // Create appointment
      const appointment = manager.create(Appointment, {
        patientId,
        ...dto,
        status: AppointmentStatus.BOOKED,
      });
      const savedAppointment = await manager.save(Appointment, appointment);

      // Assign token — get max token number for this doctor+location+date
      const maxTokenResult = await manager
        .createQueryBuilder(Token, 'token')
        .select('MAX(token.tokenNumber)', 'maxToken')
        .where('token.doctorId = :doctorId', { doctorId: dto.doctorId })
        .andWhere('token.locationId = :locationId', {
          locationId: dto.locationId,
        })
        .andWhere('token.date = :date', { date: dto.date })
        .getRawOne();

      const nextTokenNumber = (maxTokenResult?.maxToken || 0) + 1;

      // Get max position
      const maxPositionResult = await manager
        .createQueryBuilder(Token, 'token')
        .select('MAX(token.position)', 'maxPosition')
        .where('token.doctorId = :doctorId', { doctorId: dto.doctorId })
        .andWhere('token.locationId = :locationId', {
          locationId: dto.locationId,
        })
        .andWhere('token.date = :date', { date: dto.date })
        .getRawOne();

      const nextPosition = (maxPositionResult?.maxPosition || 0) + 1;

      const token = manager.create(Token, {
        appointmentId: savedAppointment.id,
        doctorId: dto.doctorId,
        locationId: dto.locationId,
        date: dto.date,
        tokenNumber: nextTokenNumber,
        position: nextPosition,
        status: TokenStatus.WAITING,
      });
      const savedToken = await manager.save(Token, token);

      // Load location for confirmation message
      const appointmentWithDetails = await manager.findOne(Appointment, {
        where: { id: savedAppointment.id },
        relations: ['doctor', 'location', 'familyMember', 'token'],
      });

      return {
        appointment: appointmentWithDetails,
        tokenNumber: savedToken.tokenNumber,
        message: `This appointment is booked at ${appointmentWithDetails!.location.name}`,
      };
    });
  }

  async cancelAppointment(patientId: string, appointmentId: string) {
    const appointment = await this.appointmentsRepo.findOne({
      where: { id: appointmentId, patientId },
      relations: ['token'],
    });
    if (!appointment) throw new NotFoundException('Appointment not found');
    if (appointment.status !== AppointmentStatus.BOOKED) {
      throw new BadRequestException('Only booked appointments can be cancelled');
    }

    appointment.status = AppointmentStatus.CANCELLED;
    await this.appointmentsRepo.save(appointment);

    // Mark token as skipped
    if (appointment.token) {
      appointment.token.status = TokenStatus.SKIPPED;
      await this.tokensRepo.save(appointment.token);
    }

    return { message: 'Appointment cancelled' };
  }

  async getAppointmentsByDoctor(
    doctorId: string,
    date: string,
    locationId?: string,
  ) {
    const where: any = { doctorId, date };
    if (locationId) where.locationId = locationId;

    return this.appointmentsRepo.find({
      where,
      relations: ['patient', 'familyMember', 'token', 'location'],
      order: { timeSlot: 'ASC' },
    });
  }

  async rescheduleAllAppointments(
    doctorId: string,
    fromDate: string,
    toDate: string,
    locationId?: string,
  ) {
    const where: any = {
      doctorId,
      date: fromDate,
      status: AppointmentStatus.BOOKED,
    };
    if (locationId) where.locationId = locationId;

    const appointments = await this.appointmentsRepo.find({
      where,
      relations: ['token'],
    });

    if (appointments.length === 0) {
      return { message: 'No appointments to reschedule' };
    }

    // Update all appointments to new date
    for (const apt of appointments) {
      apt.date = toDate;
      await this.appointmentsRepo.save(apt);

      if (apt.token) {
        apt.token.date = toDate;
        await this.tokensRepo.save(apt.token);
      }
    }

    return {
      message: `${appointments.length} appointments rescheduled to ${toDate}`,
      count: appointments.length,
    };
  }
}
