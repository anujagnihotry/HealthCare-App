import { IsDateString, IsNotEmpty, IsOptional, IsString, IsUUID } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class AssignOfflineTokenDto {
  @ApiProperty({ description: 'Location ID' })
  @IsUUID()
  locationId: string;

  @ApiProperty({ example: '2024-12-20' })
  @IsDateString()
  date: string;

  @ApiProperty({ example: 'Walk-in Patient', required: false })
  @IsString()
  @IsOptional()
  patientName?: string;
}
