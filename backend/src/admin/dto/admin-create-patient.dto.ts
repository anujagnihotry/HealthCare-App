import { IsEmail, IsString, MinLength, IsOptional } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class AdminCreatePatientDto {
  @ApiPropertyOptional({ example: 'patient@example.com' })
  @IsEmail()
  @IsOptional()
  email?: string;

  @ApiProperty({ example: '+919876543210' })
  @IsString()
  phone: string;

  @ApiProperty({ example: 'Patient@123' })
  @IsString()
  @MinLength(6)
  password: string;

  @ApiProperty({ example: 'Rahul Sharma' })
  @IsString()
  name: string;
}
