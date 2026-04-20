import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Appointment } from '../../appointments/entities/appointment.entity';
import { AppointmentStatus } from '../../common/enums';

@Injectable()
export class AdminAppointmentsService {
  constructor(
    @InjectRepository(Appointment)
    private appointmentsRepo: Repository<Appointment>,
  ) {}

  async findAll(filters: {
    doctorId?: string;
    patientId?: string;
    date?: string;
    dateFrom?: string;
    dateTo?: string;
    status?: AppointmentStatus;
    page?: number;
    limit?: number;
  }) {
    const { doctorId, patientId, date, dateFrom, dateTo, status, page = 1, limit = 20 } = filters;

    const qb = this.appointmentsRepo
      .createQueryBuilder('appointment')
      .leftJoinAndSelect('appointment.doctor', 'doctor')
      .leftJoinAndSelect('appointment.patient', 'patient')
      .leftJoinAndSelect('appointment.familyMember', 'familyMember')
      .leftJoinAndSelect('appointment.location', 'location');

    if (doctorId) qb.andWhere('appointment.doctorId = :doctorId', { doctorId });
    if (patientId) qb.andWhere('appointment.patientId = :patientId', { patientId });
    if (date) qb.andWhere('appointment.date = :date', { date });
    if (dateFrom) qb.andWhere('appointment.date >= :dateFrom', { dateFrom });
    if (dateTo) qb.andWhere('appointment.date <= :dateTo', { dateTo });
    if (status) qb.andWhere('appointment.status = :status', { status });

    const [data, total] = await qb
      .orderBy('appointment.date', 'DESC')
      .addOrderBy('appointment.timeSlot', 'ASC')
      .skip((page - 1) * limit)
      .take(limit)
      .getManyAndCount();

    return { data, total, page, limit };
  }

  async findOne(id: string) {
    const appointment = await this.appointmentsRepo.findOne({
      where: { id },
      relations: ['doctor', 'patient', 'familyMember', 'location', 'token'],
    });
    if (!appointment) throw new NotFoundException('Appointment not found');
    return appointment;
  }

  async updateStatus(id: string, status: AppointmentStatus) {
    const appointment = await this.appointmentsRepo.findOne({ where: { id } });
    if (!appointment) throw new NotFoundException('Appointment not found');
    await this.appointmentsRepo.update(id, { status });
    return this.findOne(id);
  }
}
