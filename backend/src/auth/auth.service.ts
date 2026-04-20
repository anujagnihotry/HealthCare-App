import {
  Injectable,
  ConflictException,
  UnauthorizedException,
  BadRequestException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { User } from '../users/entities/user.entity';
import { Doctor } from '../doctors/entities/doctor.entity';
import { Patient } from '../patients/entities/patient.entity';
import { FamilyMember } from '../family-members/entities/family-member.entity';
import { RegisterDto, LoginDto } from './dto';
import { UserRole, Gender } from '../common/enums';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    @InjectRepository(Doctor)
    private doctorsRepository: Repository<Doctor>,
    @InjectRepository(Patient)
    private patientsRepository: Repository<Patient>,
    @InjectRepository(FamilyMember)
    private familyMembersRepository: Repository<FamilyMember>,
    private jwtService: JwtService,
    private configService: ConfigService,
  ) {}

  async register(dto: RegisterDto) {
    if (!dto.email && !dto.phone) {
      throw new BadRequestException('Email or phone is required');
    }

    // Check if user exists
    const existingUser = await this.usersRepository.findOne({
      where: [
        ...(dto.email ? [{ email: dto.email }] : []),
        ...(dto.phone ? [{ phone: dto.phone }] : []),
      ],
    });
    if (existingUser) {
      throw new ConflictException('User with this email or phone already exists');
    }

    // Validate doctor-specific fields
    if (dto.role === UserRole.DOCTOR && !dto.specialization) {
      throw new BadRequestException('Specialization is required for doctors');
    }

    const passwordHash = await bcrypt.hash(dto.password, 12);

    const user = this.usersRepository.create({
      email: dto.email,
      phone: dto.phone,
      passwordHash,
      role: dto.role,
    });
    await this.usersRepository.save(user);

    if (dto.role === UserRole.DOCTOR) {
      const doctor = this.doctorsRepository.create({
        userId: user.id,
        name: dto.name,
        specialization: dto.specialization,
      });
      await this.doctorsRepository.save(doctor);
    } else {
      const patient = this.patientsRepository.create({
        userId: user.id,
        name: dto.name,
        phone: dto.phone,
        email: dto.email,
      });
      const savedPatient = await this.patientsRepository.save(patient);

      // Create "Self" family member automatically
      const selfMember = this.familyMembersRepository.create({
        patientId: savedPatient.id,
        name: dto.name,
        age: 0,
        gender: Gender.OTHER,
        isSelf: true,
      });
      await this.familyMembersRepository.save(selfMember);
    }

    const tokens = await this.generateTokens(user);
    return {
      user: this.sanitizeUser(user),
      ...tokens,
    };
  }

  async login(dto: LoginDto) {
    if (!dto.email && !dto.phone) {
      throw new BadRequestException('Email or phone is required');
    }

    const user = await this.usersRepository.findOne({
      where: [
        ...(dto.email ? [{ email: dto.email }] : []),
        ...(dto.phone ? [{ phone: dto.phone }] : []),
      ],
      relations: ['doctor', 'patient'],
    });

    if (!user) {
      throw new UnauthorizedException('Invalid credentials');
    }

    const isPasswordValid = await bcrypt.compare(dto.password, user.passwordHash);
    if (!isPasswordValid) {
      throw new UnauthorizedException('Invalid credentials');
    }

    if (!user.isActive) {
      throw new UnauthorizedException('Account is deactivated');
    }

    const tokens = await this.generateTokens(user);
    return {
      user: this.sanitizeUser(user),
      ...tokens,
    };
  }

  async refreshToken(refreshToken: string) {
    try {
      const payload = this.jwtService.verify(refreshToken, {
        secret: this.configService.get('JWT_REFRESH_SECRET'),
      });
      const user = await this.usersRepository.findOne({
        where: { id: payload.sub },
        relations: ['doctor', 'patient'],
      });
      if (!user || !user.isActive) {
        throw new UnauthorizedException();
      }
      const tokens = await this.generateTokens(user);
      return {
        user: this.sanitizeUser(user),
        ...tokens,
      };
    } catch {
      throw new UnauthorizedException('Invalid refresh token');
    }
  }

  async updateFcmToken(userId: string, fcmToken: string) {
    await this.usersRepository.update(userId, { fcmToken });
  }

  private async generateTokens(user: User) {
    const payload = { sub: user.id, email: user.email, role: user.role };

    const [accessToken, refreshToken] = await Promise.all([
      this.jwtService.signAsync(payload),
      this.jwtService.signAsync(payload, {
        secret: this.configService.get('JWT_REFRESH_SECRET'),
        expiresIn: this.configService.get('JWT_REFRESH_EXPIRATION', '30d'),
      }),
    ]);

    return { accessToken, refreshToken };
  }

  private sanitizeUser(user: User) {
    return {
      id: user.id,
      email: user.email,
      phone: user.phone,
      role: user.role,
      doctorId: user.doctor?.id,
      patientId: user.patient?.id,
    };
  }
}
