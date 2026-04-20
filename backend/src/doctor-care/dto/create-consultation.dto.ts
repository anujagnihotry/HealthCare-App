import {
  IsArray,
  IsDateString,
  IsOptional,
  IsString,
  IsUUID,
  MaxLength,
  MinLength,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateConsultationDto {
  @ApiProperty()
  @IsUUID()
  patientId: string;

  @ApiProperty()
  @IsUUID()
  familyMemberId: string;

  @ApiProperty({ required: false })
  @IsUUID()
  @IsOptional()
  appointmentId?: string;

  @ApiProperty()
  @IsString()
  @MinLength(1)
  @MaxLength(8000)
  symptoms: string;

  @ApiProperty()
  @IsString()
  @MinLength(1)
  @MaxLength(8000)
  diagnosis: string;

  @ApiProperty()
  @IsString()
  @MinLength(1)
  @MaxLength(8000)
  illness: string;

  @ApiProperty()
  @IsString()
  @MinLength(1)
  @MaxLength(8000)
  medications: string;

  @ApiProperty({ required: false })
  @IsString()
  @IsOptional()
  @MaxLength(8000)
  notes?: string;

  @ApiProperty({ required: false, description: 'Optional vitals captured with visit' })
  @IsString()
  @IsOptional()
  @MaxLength(32)
  bp?: string;

  @ApiProperty({ required: false })
  @IsString()
  @IsOptional()
  @MaxLength(32)
  sugar?: string;

  @ApiProperty({ required: false })
  @IsString()
  @IsOptional()
  @MaxLength(32)
  height?: string;

  @ApiProperty({ required: false })
  @IsString()
  @IsOptional()
  @MaxLength(32)
  weight?: string;

  @ApiProperty({
    required: false,
    description: 'Existing upload IDs to attach to this consultation',
  })
  @IsArray()
  @IsUUID('4', { each: true })
  @IsOptional()
  uploadIds?: string[];
}

export class AssignCustomCodeDto {
  @ApiProperty({ example: 'RK-1024' })
  @IsString()
  @MinLength(2)
  @MaxLength(64)
  customCode: string;
}
