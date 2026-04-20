import {
  IsInt,
  IsNotEmpty,
  IsOptional,
  IsString,
  IsUUID,
  Max,
  Min,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateAvailabilityDto {
  @ApiProperty({ description: 'Location ID' })
  @IsUUID()
  locationId: string;

  @ApiProperty({ example: 1, description: '0=Sun, 1=Mon, ..., 6=Sat' })
  @IsInt()
  @Min(0)
  @Max(6)
  dayOfWeek: number;

  @ApiProperty({ example: 'Morning' })
  @IsString()
  @IsNotEmpty()
  sessionName: string;

  @ApiProperty({ example: '07:00' })
  @IsString()
  @IsNotEmpty()
  startTime: string;

  @ApiProperty({ example: '09:00' })
  @IsString()
  @IsNotEmpty()
  endTime: string;

  @ApiProperty({ example: 10 })
  @IsInt()
  @Min(5)
  @Max(60)
  slotDurationMinutes: number;

  @ApiProperty({ example: '08:00', required: false })
  @IsString()
  @IsOptional()
  breakStart?: string;

  @ApiProperty({ example: '08:15', required: false })
  @IsString()
  @IsOptional()
  breakEnd?: string;
}
