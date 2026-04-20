import { Injectable, BadRequestException, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, In } from 'typeorm';
import { Appointment } from '../appointments/entities/appointment.entity';
import { Upload } from '../uploads/entities/upload.entity';
import { DoctorPatientsService } from './doctor-patients.service';
import { Consultation } from './entities/consultation.entity';
import { PatientVital } from './entities/patient-vital.entity';
import { CreateConsultationDto } from './dto';

@Injectable()
export class ConsultationsService {
  constructor(
    @InjectRepository(Consultation)
    private consultationsRepo: Repository<Consultation>,
    @InjectRepository(PatientVital)
    private vitalsRepo: Repository<PatientVital>,
    @InjectRepository(Appointment)
    private appointmentsRepo: Repository<Appointment>,
    @InjectRepository(Upload)
    private uploadsRepo: Repository<Upload>,
    private doctorPatientsService: DoctorPatientsService,
  ) {}

  async create(doctorId: string, dto: CreateConsultationDto) {
    const { patientId } =
      await this.doctorPatientsService.assertDoctorKnowsFamilyMember(
        doctorId,
        dto.familyMemberId,
      );

    if (dto.patientId !== patientId) {
      throw new BadRequestException('patientId does not match family member');
    }

    if (dto.appointmentId) {
      const apt = await this.appointmentsRepo.findOne({
        where: { id: dto.appointmentId },
      });
      if (!apt) throw new NotFoundException('Appointment not found');
      if (
        apt.doctorId !== doctorId ||
        apt.familyMemberId !== dto.familyMemberId ||
        apt.patientId !== dto.patientId
      ) {
        throw new BadRequestException('Appointment does not match patient context');
      }
    }

    await this.doctorPatientsService.ensureMapping(
      doctorId,
      dto.familyMemberId,
      dto.patientId,
    );

    const consultation = this.consultationsRepo.create({
      doctorId,
      patientId: dto.patientId,
      familyMemberId: dto.familyMemberId,
      appointmentId: dto.appointmentId ?? null,
      symptoms: dto.symptoms,
      diagnosis: dto.diagnosis,
      illness: dto.illness,
      medications: dto.medications,
      notes: dto.notes ?? null,
    });
    const saved = await this.consultationsRepo.save(consultation);

    const hasVitals = !!(dto.bp || dto.sugar || dto.height || dto.weight);
    if (hasVitals) {
      const vital = this.vitalsRepo.create({
        doctorId,
        patientId: dto.patientId,
        familyMemberId: dto.familyMemberId,
        bp: dto.bp ?? null,
        sugar: dto.sugar ?? null,
        height: dto.height ?? null,
        weight: dto.weight ?? null,
      });
      await this.vitalsRepo.save(vital);
    }

    if (dto.uploadIds?.length) {
      const uploads = await this.uploadsRepo.find({
        where: { id: In(dto.uploadIds), familyMemberId: dto.familyMemberId },
      });
      if (uploads.length !== dto.uploadIds.length) {
        throw new BadRequestException('One or more uploads not found for this patient');
      }
      for (const u of uploads) {
        u.consultationId = saved.id;
        await this.uploadsRepo.save(u);
      }
    }

    return this.consultationsRepo.findOne({
      where: { id: saved.id },
      relations: ['appointment'],
    });
  }
}
