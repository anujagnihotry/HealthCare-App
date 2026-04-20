import { IsDateString, IsOptional, IsString, IsUUID } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateUnavailabilityDto {
  @ApiProperty({ example: '2024-12-25' })
  @IsDateString()
  date: string;

  @ApiProperty({ required: false, description: 'Null = full day off' })
  @IsUUID()
  @IsOptional()
  locationId?: string;

  @ApiProperty({ example: 'Morning', required: false })
  @IsString()
  @IsOptional()
  sessionName?: string;

  @ApiProperty({ example: 'Holiday', required: false })
  @IsString()
  @IsOptional()
  reason?: string;
}
