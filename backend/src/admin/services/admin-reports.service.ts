import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Upload } from '../../uploads/entities/upload.entity';
import { Consultation } from '../../doctor-care/entities/consultation.entity';
import { MedicalRecord } from '../../uploads/entities/medical-record.entity';
import { UploadType } from '../../common/enums';

@Injectable()
export class AdminReportsService {
  constructor(
    @InjectRepository(Upload)
    private uploadsRepo: Repository<Upload>,
    @InjectRepository(Consultation)
    private consultationsRepo: Repository<Consultation>,
    @InjectRepository(MedicalRecord)
    private medicalRecordsRepo: Repository<MedicalRecord>,
  ) {}

  async getUploads(filters: {
    doctorId?: string;
    type?: UploadType;
    dateFrom?: string;
    dateTo?: string;
    page?: number;
    limit?: number;
  }) {
    const { doctorId, type, dateFrom, dateTo, page = 1, limit = 20 } = filters;
    const qb = this.uploadsRepo
      .createQueryBuilder('upload')
      .leftJoinAndSelect('upload.familyMember', 'familyMember')
      .leftJoinAndSelect('upload.appointment', 'appointment');

    if (type) qb.andWhere('upload.type = :type', { type });
    if (dateFrom) qb.andWhere('upload.date >= :dateFrom', { dateFrom });
    if (dateTo) qb.andWhere('upload.date <= :dateTo', { dateTo });
    if (doctorId) {
      qb.andWhere('appointment.doctorId = :doctorId', { doctorId });
    }

    const [data, total] = await qb
      .orderBy('upload.createdAt', 'DESC')
      .skip((page - 1) * limit)
      .take(limit)
      .getManyAndCount();

    return { data, total, page, limit };
  }

  async getConsultations(filters: {
    doctorId?: string;
    patientId?: string;
    dateFrom?: string;
    dateTo?: string;
    page?: number;
    limit?: number;
  }) {
    const { doctorId, patientId, dateFrom, dateTo, page = 1, limit = 20 } = filters;
    const qb = this.consultationsRepo
      .createQueryBuilder('consultation')
      .leftJoinAndSelect('consultation.doctor', 'doctor')
      .leftJoinAndSelect('consultation.patient', 'patient')
      .leftJoinAndSelect('consultation.familyMember', 'familyMember');

    if (doctorId) qb.andWhere('consultation.doctorId = :doctorId', { doctorId });
    if (patientId) qb.andWhere('consultation.patientId = :patientId', { patientId });
    if (dateFrom) qb.andWhere('consultation.createdAt >= :dateFrom', { dateFrom });
    if (dateTo) qb.andWhere('consultation.createdAt <= :dateTo', { dateTo });

    const [data, total] = await qb
      .orderBy('consultation.createdAt', 'DESC')
      .skip((page - 1) * limit)
      .take(limit)
      .getManyAndCount();

    return { data, total, page, limit };
  }

  async getMedicalRecords(filters: {
    doctorId?: string;
    familyMemberId?: string;
    page?: number;
    limit?: number;
  }) {
    const { doctorId, familyMemberId, page = 1, limit = 20 } = filters;
    const qb = this.medicalRecordsRepo
      .createQueryBuilder('record')
      .leftJoinAndSelect('record.doctor', 'doctor')
      .leftJoinAndSelect('record.familyMember', 'familyMember');

    if (doctorId) qb.andWhere('record.doctorId = :doctorId', { doctorId });
    if (familyMemberId) qb.andWhere('record.familyMemberId = :familyMemberId', { familyMemberId });

    const [data, total] = await qb
      .orderBy('record.createdAt', 'DESC')
      .skip((page - 1) * limit)
      .take(limit)
      .getManyAndCount();

    return { data, total, page, limit };
  }
}
