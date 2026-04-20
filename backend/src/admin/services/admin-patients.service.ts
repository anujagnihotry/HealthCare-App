import {
  Injectable,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { DataSource, Repository } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { User } from '../../users/entities/user.entity';
import { Patient } from '../../patients/entities/patient.entity';
import { FamilyMember } from '../../family-members/entities/family-member.entity';
import { UserRole, Gender } from '../../common/enums';
import { AdminCreatePatientDto } from '../dto/admin-create-patient.dto';
import { AdminUpdatePatientDto } from '../dto/admin-update-patient.dto';

@Injectable()
export class AdminPatientsService {
  constructor(
    @InjectRepository(User)
    private usersRepo: Repository<User>,
    @InjectRepository(Patient)
    private patientsRepo: Repository<Patient>,
    @InjectRepository(FamilyMember)
    private familyMembersRepo: Repository<FamilyMember>,
    private dataSource: DataSource,
  ) {}

  async findAll(search?: string, page = 1, limit = 20) {
    const qb = this.patientsRepo
      .createQueryBuilder('patient')
      .leftJoinAndSelect('patient.user', 'user');

    if (search) {
      qb.andWhere(
        '(patient.name ILIKE :s OR patient.phone ILIKE :s OR patient.email ILIKE :s OR user.phone ILIKE :s)',
        { s: `%${search}%` },
      );
    }

    const [data, total] = await qb
      .skip((page - 1) * limit)
      .take(limit)
      .getManyAndCount();

    return { data, total, page, limit };
  }

  async findOne(id: string) {
    const patient = await this.patientsRepo.findOne({
      where: { id },
      relations: ['user', 'familyMembers'],
    });
    if (!patient) throw new NotFoundException('Patient not found');
    return patient;
  }

  async create(dto: AdminCreatePatientDto) {
    const existing = await this.usersRepo.findOne({
      where: [
        ...(dto.email ? [{ email: dto.email }] : []),
        ...(dto.phone ? [{ phone: dto.phone }] : []),
      ],
    });
    if (existing) throw new ConflictException('User already exists');

    return this.dataSource.transaction(async (manager) => {
      const passwordHash = await bcrypt.hash(dto.password, 12);
      const user = manager.create(User, {
        email: dto.email,
        phone: dto.phone,
        passwordHash,
        role: UserRole.PATIENT,
      });
      const savedUser = await manager.save(User, user);

      const patient = manager.create(Patient, {
        userId: savedUser.id,
        name: dto.name,
        phone: dto.phone,
        email: dto.email,
      });
      const savedPatient = await manager.save(Patient, patient);

      const selfMember = manager.create(FamilyMember, {
        patientId: savedPatient.id,
        name: dto.name,
        age: 0,
        gender: Gender.OTHER,
        isSelf: true,
      });
      await manager.save(FamilyMember, selfMember);

      return savedPatient;
    });
  }

  async update(id: string, dto: AdminUpdatePatientDto) {
    const patient = await this.patientsRepo.findOne({
      where: { id },
      relations: ['user'],
    });
    if (!patient) throw new NotFoundException('Patient not found');

    const { isActive, ...patientFields } = dto;
    if (Object.keys(patientFields).length) {
      await this.patientsRepo.update(id, patientFields as any);
    }
    if (isActive !== undefined) {
      await this.usersRepo.update(patient.userId, { isActive });
    }

    return this.findOne(id);
  }

  async remove(id: string) {
    const patient = await this.patientsRepo.findOne({
      where: { id },
      relations: ['user'],
    });
    if (!patient) throw new NotFoundException('Patient not found');
    await this.usersRepo.update(patient.userId, { isActive: false });
    return { message: 'Patient deactivated successfully' };
  }
}
