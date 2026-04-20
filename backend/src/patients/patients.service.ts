import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, MoreThanOrEqual, LessThan } from 'typeorm';
import { Patient } from './entities/patient.entity';
import { Appointment } from '../appointments/entities/appointment.entity';

@Injectable()
export class PatientsService {
  constructor(
    @InjectRepository(Patient)
    private patientsRepo: Repository<Patient>,
    @InjectRepository(Appointment)
    private appointmentsRepo: Repository<Appointment>,
  ) {}

  async getProfile(patientId: string) {
    const patient = await this.patientsRepo.findOne({
      where: { id: patientId },
      relations: ['familyMembers'],
    });
    if (!patient) throw new NotFoundException('Patient not found');
    return patient;
  }

  async updateProfile(
    patientId: string,
    dto: { name?: string; phone?: string; email?: string },
  ) {
    await this.patientsRepo.update(patientId, dto);
    return this.getProfile(patientId);
  }

  async getDashboard(patientId: string) {
    const today = new Date().toISOString().split('T')[0];

    const [upcoming, past] = await Promise.all([
      this.appointmentsRepo.find({
        where: { patientId, date: MoreThanOrEqual(today) },
        relations: ['doctor', 'location', 'familyMember', 'token'],
        order: { date: 'ASC', timeSlot: 'ASC' },
        take: 20,
      }),
      this.appointmentsRepo.find({
        where: { patientId, date: LessThan(today) },
        relations: ['doctor', 'location', 'familyMember'],
        order: { date: 'DESC', timeSlot: 'DESC' },
        take: 20,
      }),
    ]);

    return { upcoming, past };
  }
}
