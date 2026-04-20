import {
  IsEmail,
  IsEnum,
  IsInt,
  IsOptional,
  IsString,
  Max,
  MaxLength,
  Min,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { MedicalSystem } from '../../common/enums';

export class UpdateDoctorProfileDto {
  @ApiProperty({ required: false })
  @IsString()
  @IsOptional()
  name?: string;

  @ApiProperty({ required: false })
  @IsString()
  @IsOptional()
  specialization?: string;

  @ApiProperty({ required: false })
  @IsString()
  @IsOptional()
  bio?: string;

  @ApiProperty({ required: false })
  @IsString()
  @IsOptional()
  contactPhone?: string;

  @ApiProperty({ required: false })
  @IsEmail()
  @IsOptional()
  contactEmail?: string;

  @ApiProperty({ required: false, description: 'Total years of clinical experience' })
  @IsInt()
  @Min(0)
  @Max(80)
  @IsOptional()
  yearsOfExperience?: number;

  @ApiProperty({ required: false, example: 'MBBS, MD (Cardiology)' })
  @IsString()
  @MaxLength(200)
  @IsOptional()
  degree?: string;

  @ApiProperty({ required: false, enum: MedicalSystem })
  @IsEnum(MedicalSystem)
  @IsOptional()
  medicalSystem?: MedicalSystem;
}
