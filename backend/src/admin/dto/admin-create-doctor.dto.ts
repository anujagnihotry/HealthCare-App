import {
  IsEmail,
  IsString,
  MinLength,
  IsOptional,
  IsInt,
  Min,
  IsEnum,
  IsPhoneNumber,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { MedicalSystem } from '../../common/enums';

export class AdminCreateDoctorDto {
  @ApiPropertyOptional({ example: 'doctor@example.com' })
  @IsEmail()
  @IsOptional()
  email?: string;

  @ApiPropertyOptional({ example: '+919876543210' })
  @IsString()
  @IsOptional()
  phone?: string;

  @ApiProperty({ example: 'Doctor@123' })
  @IsString()
  @MinLength(6)
  password: string;

  @ApiProperty({ example: 'Dr. Rajesh Kumar' })
  @IsString()
  name: string;

  @ApiProperty({ example: 'Cardiology' })
  @IsString()
  specialization: string;

  @ApiPropertyOptional()
  @IsString()
  @IsOptional()
  bio?: string;

  @ApiPropertyOptional({ example: '+919876543210' })
  @IsString()
  @IsOptional()
  contactPhone?: string;

  @ApiPropertyOptional({ example: 'dr.rajesh@clinic.com' })
  @IsEmail()
  @IsOptional()
  contactEmail?: string;

  @ApiPropertyOptional({ example: 10 })
  @IsInt()
  @Min(0)
  @IsOptional()
  yearsOfExperience?: number;

  @ApiPropertyOptional({ example: 'MBBS, MD' })
  @IsString()
  @IsOptional()
  degree?: string;

  @ApiPropertyOptional({ enum: MedicalSystem })
  @IsEnum(MedicalSystem)
  @IsOptional()
  medicalSystem?: MedicalSystem;
}
