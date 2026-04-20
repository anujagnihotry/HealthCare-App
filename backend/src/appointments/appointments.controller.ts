import {
  Controller,
  Get,
  Post,
  Patch,
  Body,
  Param,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { AppointmentsService } from './appointments.service';
import { BookAppointmentDto } from './dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles, CurrentUser } from '../common/decorators';
import { UserRole } from '../common/enums';
import { User } from '../users/entities/user.entity';

@ApiTags('Appointments')
@Controller('appointments')
export class AppointmentsController {
  constructor(private readonly appointmentsService: AppointmentsService) {}

  // ── Patient Endpoints ────────────────────────────────

  @Get('booking-window')
  @ApiOperation({
    summary: 'Get booking window with available dates and slots',
  })
  async getBookingWindow(
    @Query('doctorId') doctorId: string,
    @Query('locationId') locationId: string,
  ) {
    return this.appointmentsService.getBookingWindow(doctorId, locationId);
  }

  @Post('book')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.PATIENT)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Book an appointment' })
  async book(@CurrentUser() user: User, @Body() dto: BookAppointmentDto) {
    return this.appointmentsService.bookAppointment(user.patient.id, dto);
  }

  @Patch(':appointmentId/cancel')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.PATIENT)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Cancel an appointment' })
  async cancel(
    @CurrentUser() user: User,
    @Param('appointmentId') appointmentId: string,
  ) {
    return this.appointmentsService.cancelAppointment(
      user.patient.id,
      appointmentId,
    );
  }

  // ── Doctor Endpoints ─────────────────────────────────

  @Get('doctor/by-date')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get appointments for a date (doctor view)' })
  async getByDate(
    @CurrentUser() user: User,
    @Query('date') date: string,
    @Query('locationId') locationId?: string,
  ) {
    return this.appointmentsService.getAppointmentsByDoctor(
      user.doctor.id,
      date,
      locationId,
    );
  }

  @Post('doctor/reschedule-all')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Reschedule all appointments from one date to another' })
  async rescheduleAll(
    @CurrentUser() user: User,
    @Body() body: { fromDate: string; toDate: string; locationId?: string },
  ) {
    return this.appointmentsService.rescheduleAllAppointments(
      user.doctor.id,
      body.fromDate,
      body.toDate,
      body.locationId,
    );
  }
}
