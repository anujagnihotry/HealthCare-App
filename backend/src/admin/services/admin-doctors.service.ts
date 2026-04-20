import {
  Injectable,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { DataSource, Repository, ILike } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { User } from '../../users/entities/user.entity';
import { Doctor } from '../../doctors/entities/doctor.entity';
import { UserRole } from '../../common/enums';
import { AdminCreateDoctorDto } from '../dto/admin-create-doctor.dto';
import { AdminUpdateDoctorDto } from '../dto/admin-update-doctor.dto';

@Injectable()
export class AdminDoctorsService {
  constructor(
    @InjectRepository(User)
    private usersRepo: Repository<User>,
    @InjectRepository(Doctor)
    private doctorsRepo: Repository<Doctor>,
    private dataSource: DataSource,
  ) {}

  async findAll(
    search?: string,
    specialization?: string,
    page = 1,
    limit = 20,
  ) {
    const qb = this.doctorsRepo
      .createQueryBuilder('doctor')
      .leftJoinAndSelect('doctor.user', 'user')
      .leftJoin('doctor.locations', 'location')
      .addSelect(['location.id', 'location.name', 'location.address'])
      .loadRelationCountAndMap('doctor.appointmentCount', 'doctor.appointments');

    if (search) {
      qb.andWhere(
        '(doctor.name ILIKE :s OR doctor.contactPhone ILIKE :s OR doctor.contactEmail ILIKE :s OR user.email ILIKE :s OR user.phone ILIKE :s)',
        { s: `%${search}%` },
      );
    }
    if (specialization) {
      qb.andWhere('doctor.specialization ILIKE :spec', {
        spec: `%${specialization}%`,
      });
    }

    const [data, total] = await qb
      .skip((page - 1) * limit)
      .take(limit)
      .getManyAndCount();

    return { data, total, page, limit };
  }

  async findOne(id: string) {
    const doctor = await this.doctorsRepo.findOne({
      where: { id },
      relations: ['user', 'locations'],
    });
    if (!doctor) throw new NotFoundException('Doctor not found');

    const appointmentCount = await this.dataSource
      .getRepository('appointments')
      .count({ where: { doctorId: id } });

    return { ...doctor, appointmentCount };
  }

  async create(dto: AdminCreateDoctorDto) {
    if (!dto.email && !dto.phone) {
      throw new ConflictException('Email or phone is required');
    }

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
        role: UserRole.DOCTOR,
      });
      const savedUser = await manager.save(User, user);

      const doctor = manager.create(Doctor, {
        userId: savedUser.id,
        name: dto.name,
        specialization: dto.specialization,
        bio: dto.bio,
        contactPhone: dto.contactPhone,
        contactEmail: dto.contactEmail,
        yearsOfExperience: dto.yearsOfExperience,
        degree: dto.degree,
        medicalSystem: dto.medicalSystem,
      });
      const savedDoctor = await manager.save(Doctor, doctor);
      return { ...savedDoctor, user: { id: savedUser.id, email: savedUser.email, phone: savedUser.phone } };
    });
  }

  async update(id: string, dto: AdminUpdateDoctorDto) {
    const doctor = await this.doctorsRepo.findOne({
      where: { id },
      relations: ['user'],
    });
    if (!doctor) throw new NotFoundException('Doctor not found');

    const { isActive, ...doctorFields } = dto;
    if (Object.keys(doctorFields).length) {
      await this.doctorsRepo.update(id, doctorFields as any);
    }
    if (isActive !== undefined) {
      await this.usersRepo.update(doctor.userId, { isActive });
    }

    return this.findOne(id);
  }

  async remove(id: string) {
    const doctor = await this.doctorsRepo.findOne({
      where: { id },
      relations: ['user'],
    });
    if (!doctor) throw new NotFoundException('Doctor not found');
    await this.usersRepo.update(doctor.userId, { isActive: false });
    return { message: 'Doctor deactivated successfully' };
  }
}
