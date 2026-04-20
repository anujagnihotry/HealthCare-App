import { IsString, IsOptional, IsEmail, IsEnum } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { LeadSource } from '../../common/enums';

export class AdminCreateLeadDto {
  @ApiProperty({ example: 'Priya Mehta' })
  @IsString()
  name: string;

  @ApiPropertyOptional({ example: '+919876543210' })
  @IsString()
  @IsOptional()
  phone?: string;

  @ApiPropertyOptional({ example: 'priya@example.com' })
  @IsEmail()
  @IsOptional()
  email?: string;

  @ApiProperty({ enum: LeadSource, default: LeadSource.OTHER })
  @IsEnum(LeadSource)
  source: LeadSource;

  @ApiPropertyOptional()
  @IsString()
  @IsOptional()
  notes?: string;

  @ApiPropertyOptional()
  @IsString()
  @IsOptional()
  assignedDoctorId?: string;
}
