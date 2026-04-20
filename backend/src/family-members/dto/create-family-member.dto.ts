import {
  IsArray,
  IsEnum,
  IsInt,
  IsNotEmpty,
  IsOptional,
  IsString,
  Max,
  Min,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { Gender, BloodGroup } from '../../common/enums';

export class CreateFamilyMemberDto {
  @ApiProperty({ example: 'Jane Doe' })
  @IsString()
  @IsNotEmpty()
  name: string;

  @ApiProperty({ example: 65 })
  @IsInt()
  @Min(0)
  @Max(150)
  age: number;

  @ApiProperty({ enum: Gender })
  @IsEnum(Gender)
  gender: Gender;

  @ApiProperty({ enum: BloodGroup, required: false })
  @IsEnum(BloodGroup)
  @IsOptional()
  bloodGroup?: BloodGroup;

  @ApiProperty({ example: ['Penicillin', 'Dust'], required: false })
  @IsArray()
  @IsString({ each: true })
  @IsOptional()
  allergies?: string[];
}
