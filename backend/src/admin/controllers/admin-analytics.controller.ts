import { Controller, Get, Query, UseGuards } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation } from '@nestjs/swagger';
import { JwtAuthGuard } from '../../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../../auth/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { UserRole } from '../../common/enums';
import { AdminAnalyticsService } from '../services/admin-analytics.service';
import { AnalyticsQueryDto } from '../dto/analytics-query.dto';

@ApiTags('Admin - Analytics')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.SUPER_ADMIN)
@Controller('admin/analytics')
export class AdminAnalyticsController {
  constructor(private readonly analyticsService: AdminAnalyticsService) {}

  @Get('overview')
  @ApiOperation({ summary: 'Get overall counts (doctors, patients, appointments, leads)' })
  getOverview() {
    return this.analyticsService.getOverview();
  }

  @Get('doctors-onboarded')
  @ApiOperation({ summary: 'Doctors onboarded over time' })
  getDoctorsOnboarded(@Query() query: AnalyticsQueryDto) {
    return this.analyticsService.getDoctorsOnboarded(query);
  }

  @Get('patients-onboarded')
  @ApiOperation({ summary: 'Patients onboarded over time' })
  getPatientsOnboarded(@Query() query: AnalyticsQueryDto) {
    return this.analyticsService.getPatientsOnboarded(query);
  }

  @Get('appointments')
  @ApiOperation({ summary: 'Appointments over time (optionally by doctor)' })
  getAppointments(@Query() query: AnalyticsQueryDto) {
    return this.analyticsService.getAppointments(query);
  }

  @Get('appointments-by-doctor')
  @ApiOperation({ summary: 'Appointments per doctor for a date range' })
  getAppointmentsByDoctor(@Query() query: AnalyticsQueryDto) {
    return this.analyticsService.getAppointmentsByDoctor(query);
  }
}
