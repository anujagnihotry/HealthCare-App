import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Doctor } from '../../doctors/entities/doctor.entity';
import { Patient } from '../../patients/entities/patient.entity';
import { Appointment } from '../../appointments/entities/appointment.entity';
import { Lead } from '../entities/lead.entity';
import { AnalyticsQueryDto, AnalyticsPeriod } from '../dto/analytics-query.dto';

@Injectable()
export class AdminAnalyticsService {
  constructor(
    @InjectRepository(Doctor)
    private doctorsRepo: Repository<Doctor>,
    @InjectRepository(Patient)
    private patientsRepo: Repository<Patient>,
    @InjectRepository(Appointment)
    private appointmentsRepo: Repository<Appointment>,
    @InjectRepository(Lead)
    private leadsRepo: Repository<Lead>,
  ) {}

  async getOverview() {
    const [totalDoctors, totalPatients, totalAppointments, totalLeads] =
      await Promise.all([
        this.doctorsRepo.count(),
        this.patientsRepo.count(),
        this.appointmentsRepo.count(),
        this.leadsRepo.count(),
      ]);

    return { totalDoctors, totalPatients, totalAppointments, totalLeads };
  }

  async getDoctorsOnboarded(query: AnalyticsQueryDto) {
    const period = query.period || AnalyticsPeriod.MONTH;
    const qb = this.doctorsRepo.createQueryBuilder('doctor');
    this.applyDateFilter(qb, 'doctor.createdAt', query.from, query.to);
    const rows = await qb
      .select(`DATE_TRUNC('${period}', doctor.created_at)`, 'period')
      .addSelect('COUNT(*)', 'count')
      .groupBy('period')
      .orderBy('period', 'ASC')
      .getRawMany();
    return rows.map((r) => ({ period: r.period, count: parseInt(r.count, 10) }));
  }

  async getPatientsOnboarded(query: AnalyticsQueryDto) {
    const period = query.period || AnalyticsPeriod.MONTH;
    const qb = this.patientsRepo.createQueryBuilder('patient');
    this.applyDateFilter(qb, 'patient.createdAt', query.from, query.to);
    const rows = await qb
      .select(`DATE_TRUNC('${period}', patient.created_at)`, 'period')
      .addSelect('COUNT(*)', 'count')
      .groupBy('period')
      .orderBy('period', 'ASC')
      .getRawMany();
    return rows.map((r) => ({ period: r.period, count: parseInt(r.count, 10) }));
  }

  async getAppointments(query: AnalyticsQueryDto) {
    const period = query.period || AnalyticsPeriod.MONTH;
    const qb = this.appointmentsRepo.createQueryBuilder('appointment');
    this.applyDateFilter(qb, 'appointment.createdAt', query.from, query.to);
    if (query.doctorId) {
      qb.andWhere('appointment.doctorId = :doctorId', { doctorId: query.doctorId });
    }
    const rows = await qb
      .select(`DATE_TRUNC('${period}', appointment.created_at)`, 'period')
      .addSelect('COUNT(*)', 'count')
      .groupBy('period')
      .orderBy('period', 'ASC')
      .getRawMany();
    return rows.map((r) => ({ period: r.period, count: parseInt(r.count, 10) }));
  }

  async getAppointmentsByDoctor(query: AnalyticsQueryDto) {
    const period = query.period || AnalyticsPeriod.MONTH;
    const qb = this.appointmentsRepo
      .createQueryBuilder('appointment')
      .leftJoin('appointment.doctor', 'doctor')
      .select('doctor.id', 'doctorId')
      .addSelect('doctor.name', 'doctorName')
      .addSelect('doctor.specialization', 'specialization')
      .addSelect(`DATE_TRUNC('${period}', appointment.created_at)`, 'period')
      .addSelect('COUNT(*)', 'count');

    this.applyDateFilter(qb, 'appointment.createdAt', query.from, query.to);
    if (query.doctorId) {
      qb.andWhere('appointment.doctorId = :doctorId', { doctorId: query.doctorId });
    }

    const rows = await qb
      .groupBy('doctor.id')
      .addGroupBy('doctor.name')
      .addGroupBy('doctor.specialization')
      .addGroupBy('period')
      .orderBy('count', 'DESC')
      .getRawMany();

    return rows.map((r) => ({
      doctorId: r.doctorId,
      doctorName: r.doctorName,
      specialization: r.specialization,
      period: r.period,
      count: parseInt(r.count, 10),
    }));
  }

  private applyDateFilter(qb: any, field: string, from?: string, to?: string) {
    if (from) qb.andWhere(`${field} >= :from`, { from });
    if (to) qb.andWhere(`${field} <= :to`, { to });
  }
}
