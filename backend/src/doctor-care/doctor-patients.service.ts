import {
  Injectable,
  ForbiddenException,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Not, Repository } from 'typeorm';
import { Appointment } from '../appointments/entities/appointment.entity';
import { AppointmentStatus } from '../common/enums';
import { FamilyMember } from '../family-members/entities/family-member.entity';
import { Patient } from '../patients/entities/patient.entity';
import { User } from '../users/entities/user.entity';
import { MedicalRecord } from '../uploads/entities/medical-record.entity';
import { Upload } from '../uploads/entities/upload.entity';
import { DoctorPatientMapping } from './entities/doctor-patient-mapping.entity';
import { Consultation } from './entities/consultation.entity';
import { PatientVital } from './entities/patient-vital.entity';
import { AssignCustomCodeDto } from './dto';

@Injectable()
export class DoctorPatientsService {
  constructor(
    @InjectRepository(Appointment)
    private appointmentsRepo: Repository<Appointment>,
    @InjectRepository(FamilyMember)
    private familyMemberRepo: Repository<FamilyMember>,
    @InjectRepository(Patient)
    private patientRepo: Repository<Patient>,
    @InjectRepository(DoctorPatientMapping)
    private mappingRepo: Repository<DoctorPatientMapping>,
    @InjectRepository(Consultation)
    private consultationsRepo: Repository<Consultation>,
    @InjectRepository(PatientVital)
    private vitalsRepo: Repository<PatientVital>,
    @InjectRepository(MedicalRecord)
    private medicalRecordsRepo: Repository<MedicalRecord>,
    @InjectRepository(Upload)
    private uploadsRepo: Repository<Upload>,
  ) {}

  /** At least one appointment (any status except cancelled) with this doctor. */
  async assertDoctorKnowsFamilyMember(
    doctorId: string,
    familyMemberId: string,
  ): Promise<{ patientId: string; firstAptDate: string | null }> {
    const apt = await this.appointmentsRepo.findOne({
      where: {
        doctorId,
        familyMemberId,
        status: Not(In([AppointmentStatus.CANCELLED])),
      },
      order: { date: 'ASC' },
    });
    if (!apt) {
      throw new ForbiddenException(
        'You can only access patients who have booked with you',
      );
    }
    return { patientId: apt.patientId, firstAptDate: apt.date };
  }

  async ensureMapping(
    doctorId: string,
    familyMemberId: string,
    patientId: string,
  ): Promise<DoctorPatientMapping> {
    let map = await this.mappingRepo.findOne({
      where: { doctorId, familyMemberId },
    });
    if (map) return map;

    const first = await this.appointmentsRepo.findOne({
      where: {
        doctorId,
        familyMemberId,
        status: Not(In([AppointmentStatus.CANCELLED])),
      },
      order: { date: 'ASC' },
    });
    if (!first) {
      throw new ForbiddenException('No prior appointment with this patient');
    }

    map = this.mappingRepo.create({
      doctorId,
      patientId,
      familyMemberId,
      firstVisitDate: first.date,
    });
    return this.mappingRepo.save(map);
  }

  async assignCustomCode(
    doctorId: string,
    familyMemberId: string,
    dto: AssignCustomCodeDto,
  ) {
    await this.assertDoctorKnowsFamilyMember(doctorId, familyMemberId);
    const code = dto.customCode.trim();
    const existing = await this.mappingRepo.findOne({
      where: { doctorId, customCode: code },
    });
    if (existing && existing.familyMemberId !== familyMemberId) {
      throw new ConflictException('This code is already used for another patient');
    }

    let map = await this.mappingRepo.findOne({
      where: { doctorId, familyMemberId },
    });
    const { patientId } = await this.assertDoctorKnowsFamilyMember(
      doctorId,
      familyMemberId,
    );
    if (!map) {
      map = await this.ensureMapping(doctorId, familyMemberId, patientId);
    }
    map.customCode = code;
    return this.mappingRepo.save(map);
  }

  async searchPatients(
    doctorId: string,
    q: string,
    mode?: 'name' | 'mobile' | 'code',
  ) {
    const term = `%${q.trim()}%`;
    if (!q.trim()) return [];

    const qb = this.familyMemberRepo
      .createQueryBuilder('fm')
      .innerJoin(
        Appointment,
        'apt',
        'apt.family_member_id = fm.id AND apt.doctor_id = :doctorId AND apt.status != :cancelled',
        { doctorId, cancelled: AppointmentStatus.CANCELLED },
      )
      .leftJoin(Patient, 'p', 'p.id = fm.patient_id')
      .leftJoin(User, 'u', 'u.id = p.user_id')
      .leftJoin(
        DoctorPatientMapping,
        'map',
        'map.family_member_id = fm.id AND map.doctor_id = :doctorId',
        { doctorId },
      )
      .select([
        'fm.id AS "familyMemberId"',
        'fm.name AS name',
        'p.id AS "patientId"',
        'COALESCE(p.phone, u.phone) AS phone',
        'map.custom_code AS "customCode"',
      ])
      .distinct(true);

    if (mode === 'name') {
      qb.andWhere('fm.name ILIKE :term', { term });
    } else if (mode === 'mobile') {
      qb.andWhere('(p.phone ILIKE :term OR u.phone ILIKE :term)', { term });
    } else if (mode === 'code') {
      qb.andWhere('map.custom_code ILIKE :term', { term });
    } else {
      qb.andWhere(
        '(fm.name ILIKE :term OR p.phone ILIKE :term OR u.phone ILIKE :term OR map.custom_code ILIKE :term)',
        { term },
      );
    }

    return qb.getRawMany();
  }

  async getPatientHistory(doctorId: string, familyMemberId: string) {
    await this.assertDoctorKnowsFamilyMember(doctorId, familyMemberId);

    const fm = await this.familyMemberRepo.findOne({
      where: { id: familyMemberId },
      relations: ['patient', 'patient.user'],
    });
    if (!fm) throw new NotFoundException('Family member not found');

    await this.ensureMapping(doctorId, familyMemberId, fm.patientId);

    const mapping = await this.mappingRepo.findOne({
      where: { doctorId, familyMemberId },
    });

    const consultations = await this.consultationsRepo.find({
      where: { doctorId, familyMemberId },
      order: { createdAt: 'DESC' },
      relations: ['appointment'],
    });

    const vitals = await this.vitalsRepo.find({
      where: { doctorId, familyMemberId },
      order: { recordedAt: 'DESC' },
      take: 50,
    });

    const legacyRecords = await this.medicalRecordsRepo.find({
      where: { doctorId, familyMemberId },
      relations: ['appointment'],
      order: { createdAt: 'DESC' },
    });

    const uploads = await this.uploadsRepo
      .createQueryBuilder('u')
      .leftJoin('u.appointment', 'apt')
      .where('u.familyMemberId = :familyMemberId', { familyMemberId })
      .andWhere(
        '(apt.doctorId = :doctorId OR u.consultationId IN (SELECT c.id FROM consultations c WHERE c.doctor_id = :doctorId))',
        { doctorId },
      )
      .orderBy('u.createdAt', 'DESC')
      .getMany();

    const patientPhone =
      fm.patient.phone || fm.patient.user?.phone || null;

    return {
      familyMember: {
        id: fm.id,
        name: fm.name,
        age: fm.age,
        gender: fm.gender,
        bloodGroup: fm.bloodGroup,
        allergies: fm.allergies,
        isSelf: fm.isSelf,
      },
      patient: {
        id: fm.patient.id,
        name: fm.patient.name,
        phone: patientPhone,
        email: fm.patient.email,
      },
      customCode: mapping?.customCode ?? null,
      vitals,
      consultations,
      legacyMedicalRecords: legacyRecords,
      uploads,
    };
  }
}
