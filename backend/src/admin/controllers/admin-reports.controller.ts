import { Controller, Get, Query, UseGuards } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiQuery } from '@nestjs/swagger';
import { JwtAuthGuard } from '../../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../../auth/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { UserRole, UploadType } from '../../common/enums';
import { AdminReportsService } from '../services/admin-reports.service';

@ApiTags('Admin - Reports')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.SUPER_ADMIN)
@Controller('admin/reports')
export class AdminReportsController {
  constructor(private readonly reportsService: AdminReportsService) {}

  @Get('uploads')
  @ApiOperation({ summary: 'List all uploads (reports/prescriptions)' })
  @ApiQuery({ name: 'doctorId', required: false })
  @ApiQuery({ name: 'type', required: false, enum: UploadType })
  @ApiQuery({ name: 'dateFrom', required: false })
  @ApiQuery({ name: 'dateTo', required: false })
  @ApiQuery({ name: 'page', required: false })
  @ApiQuery({ name: 'limit', required: false })
  getUploads(
    @Query('doctorId') doctorId?: string,
    @Query('type') type?: UploadType,
    @Query('dateFrom') dateFrom?: string,
    @Query('dateTo') dateTo?: string,
    @Query('page') page = 1,
    @Query('limit') limit = 20,
  ) {
    return this.reportsService.getUploads({ doctorId, type, dateFrom, dateTo, page: +page, limit: +limit });
  }

  @Get('consultations')
  @ApiOperation({ summary: 'List all consultations with diagnosis/medications' })
  @ApiQuery({ name: 'doctorId', required: false })
  @ApiQuery({ name: 'patientId', required: false })
  @ApiQuery({ name: 'dateFrom', required: false })
  @ApiQuery({ name: 'dateTo', required: false })
  @ApiQuery({ name: 'page', required: false })
  @ApiQuery({ name: 'limit', required: false })
  getConsultations(
    @Query('doctorId') doctorId?: string,
    @Query('patientId') patientId?: string,
    @Query('dateFrom') dateFrom?: string,
    @Query('dateTo') dateTo?: string,
    @Query('page') page = 1,
    @Query('limit') limit = 20,
  ) {
    return this.reportsService.getConsultations({ doctorId, patientId, dateFrom, dateTo, page: +page, limit: +limit });
  }

  @Get('medical-records')
  @ApiOperation({ summary: 'List all medical records' })
  @ApiQuery({ name: 'doctorId', required: false })
  @ApiQuery({ name: 'familyMemberId', required: false })
  @ApiQuery({ name: 'page', required: false })
  @ApiQuery({ name: 'limit', required: false })
  getMedicalRecords(
    @Query('doctorId') doctorId?: string,
    @Query('familyMemberId') familyMemberId?: string,
    @Query('page') page = 1,
    @Query('limit') limit = 20,
  ) {
    return this.reportsService.getMedicalRecords({ doctorId, familyMemberId, page: +page, limit: +limit });
  }
}
