import {
  IsArray,
  IsDateString,
  IsEnum,
  IsOptional,
  IsString,
  IsUUID,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { UploadType } from '../../common/enums';

export class CreateUploadDto {
  @ApiProperty({ description: 'Family member ID' })
  @IsUUID()
  familyMemberId: string;

  @ApiProperty({ description: 'Appointment ID (optional)' })
  @IsUUID()
  @IsOptional()
  appointmentId?: string;

  @ApiProperty({ description: 'Consultation ID (optional)', required: false })
  @IsUUID()
  @IsOptional()
  consultationId?: string;

  @ApiProperty({ enum: UploadType })
  @IsEnum(UploadType)
  type: UploadType;

  @ApiProperty({ example: '2024-12-20' })
  @IsDateString()
  date: string;

  @ApiProperty({ example: ['blood-test', 'annual-checkup'], required: false })
  @IsArray()
  @IsString({ each: true })
  @IsOptional()
  tags?: string[];
}

export class CreateMedicalRecordDto {
  @ApiProperty({ description: 'Family member ID' })
  @IsUUID()
  familyMemberId: string;

  @ApiProperty({ description: 'Appointment ID (optional)' })
  @IsUUID()
  @IsOptional()
  appointmentId?: string;

  @ApiProperty({ example: 'Hypertension', required: false })
  @IsString()
  @IsOptional()
  diagnosis?: string;

  @ApiProperty({ example: 'Patient shows signs of...', required: false })
  @IsString()
  @IsOptional()
  notes?: string;
}
