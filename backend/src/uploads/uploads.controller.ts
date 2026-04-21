import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Query,
  UseGuards,
  UseInterceptors,
  UploadedFile,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiConsumes } from '@nestjs/swagger';
import { diskStorage } from 'multer';
import { extname } from 'path';
import { v4 as uuid } from 'uuid';
import { UploadsService } from './uploads.service';
import { CreateUploadDto, CreateMedicalRecordDto } from './dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles, CurrentUser } from '../common/decorators';
import { UserRole } from '../common/enums';
import { User } from '../users/entities/user.entity';

const multerStorage = diskStorage({
  destination: './uploads',
  filename: (_req, file, cb) => {
    const uniqueName = `${uuid()}${extname(file.originalname)}`;
    cb(null, uniqueName);
  },
});

@ApiTags('Uploads')
@Controller('uploads')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class UploadsController {
  constructor(private readonly uploadsService: UploadsService) {}

  // ── File Upload (Patient or Doctor) ──────────────────

  @Post('file')
  @ApiConsumes('multipart/form-data')
  @ApiOperation({ summary: 'Upload a report or prescription file' })
  @UseInterceptors(
    FileInterceptor('file', {
      storage: multerStorage,
      limits: { fileSize: 10 * 1024 * 1024 }, // 10MB
    }),
  )
  async uploadFile(
    @CurrentUser() user: User,
    @UploadedFile() file: Express.Multer.File,
    @Body() dto: CreateUploadDto,
  ) {
    return this.uploadsService.createUpload(user.id, file, dto);
  }

  @Get('family-member/:familyMemberId')
  @ApiOperation({ summary: 'Get uploads for a family member' })
  async getByFamilyMember(
    @Param('familyMemberId') familyMemberId: string,
  ) {
    return this.uploadsService.getUploadsByFamilyMember(familyMemberId);
  }

  @Get('appointment/:appointmentId')
  @ApiOperation({ summary: 'Get uploads for an appointment' })
  async getByAppointment(
    @Param('appointmentId') appointmentId: string,
  ) {
    return this.uploadsService.getUploadsByAppointment(appointmentId);
  }

  // ── Medical Records (Doctor) ─────────────────────────

  @Post('medical-records')
  @UseGuards(RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiOperation({ summary: 'Create a medical record (doctor)' })
  async createMedicalRecord(
    @CurrentUser() user: User,
    @Body() dto: CreateMedicalRecordDto,
  ) {
    return this.uploadsService.createMedicalRecord(user.doctor.id, dto);
  }

  @Get('medical-records/:familyMemberId')
  @ApiOperation({ summary: 'Get medical records for a family member' })
  async getMedicalRecords(
    @Param('familyMemberId') familyMemberId: string,
  ) {
    return this.uploadsService.getMedicalRecords(familyMemberId);
  }

  @Get('patient-records/:familyMemberId')
  @UseGuards(RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiOperation({ summary: 'Get all records + uploads for a patient (doctor view)' })
  async getPatientRecords(
    @CurrentUser() user: User,
    @Param('familyMemberId') familyMemberId: string,
  ) {
    return this.uploadsService.getPatientRecordsForDoctor(
      user.doctor.id,
      familyMemberId,
    );
  }
}
