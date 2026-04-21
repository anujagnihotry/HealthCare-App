import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Upload } from './entities/upload.entity';
import { MedicalRecord } from './entities/medical-record.entity';
import { CreateUploadDto, CreateMedicalRecordDto } from './dto';
import { ConfigService } from '@nestjs/config';
import * as path from 'path';
import * as fs from 'fs';

@Injectable()
export class UploadsService {
  private uploadDir: string;

  constructor(
    @InjectRepository(Upload)
    private uploadsRepo: Repository<Upload>,
    @InjectRepository(MedicalRecord)
    private medicalRecordsRepo: Repository<MedicalRecord>,
    private configService: ConfigService,
  ) {
    this.uploadDir = this.configService.get('UPLOAD_DIR', './uploads');
    if (!fs.existsSync(this.uploadDir)) {
      fs.mkdirSync(this.uploadDir, { recursive: true });
    }
  }

  // ── File Uploads ─────────────────────────────────────

  async createUpload(
    userId: string,
    file: Express.Multer.File,
    dto: CreateUploadDto,
  ) {
    const fileUrl = `/uploads/${file.filename}`;
    const upload = this.uploadsRepo.create({
      familyMemberId: dto.familyMemberId,
      appointmentId: dto.appointmentId,
      consultationId: dto.consultationId ?? null,
      uploadedByUserId: userId,
      type: dto.type,
      fileUrl,
      fileName: file.originalname,
      tags: dto.tags || [],
      date: dto.date,
    });
    return this.uploadsRepo.save(upload);
  }

  async getUploadsByFamilyMember(familyMemberId: string) {
    return this.uploadsRepo.find({
      where: { familyMemberId },
      order: { date: 'DESC' },
    });
  }

  async getUploadsByAppointment(appointmentId: string) {
    return this.uploadsRepo.find({
      where: { appointmentId },
      order: { createdAt: 'DESC' },
    });
  }

  // ── Medical Records ──────────────────────────────────

  async createMedicalRecord(doctorId: string, dto: CreateMedicalRecordDto) {
    const record = this.medicalRecordsRepo.create({
      doctorId,
      ...dto,
    });
    return this.medicalRecordsRepo.save(record);
  }

  async getMedicalRecords(familyMemberId: string) {
    return this.medicalRecordsRepo.find({
      where: { familyMemberId },
      relations: ['doctor', 'appointment'],
      order: { createdAt: 'DESC' },
    });
  }

  async getPatientRecordsForDoctor(doctorId: string, familyMemberId: string) {
    const records = await this.medicalRecordsRepo.find({
      where: { familyMemberId, doctorId },
      relations: ['doctor', 'appointment'],
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

    return { records, uploads };
  }
}
