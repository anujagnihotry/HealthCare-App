import {
  Controller,
  Get,
  Patch,
  Body,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { PatientsService } from './patients.service';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles, CurrentUser } from '../common/decorators';
import { UserRole } from '../common/enums';
import { User } from '../users/entities/user.entity';

@ApiTags('Patients')
@Controller('patients')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.PATIENT)
@ApiBearerAuth()
export class PatientsController {
  constructor(private readonly patientsService: PatientsService) {}

  @Get('me/profile')
  @ApiOperation({ summary: 'Get own patient profile' })
  async getMyProfile(@CurrentUser() user: User) {
    return this.patientsService.getProfile(user.patient.id);
  }

  @Patch('me/profile')
  @ApiOperation({ summary: 'Update own patient profile' })
  async updateMyProfile(
    @CurrentUser() user: User,
    @Body() dto: { name?: string; phone?: string; email?: string },
  ) {
    return this.patientsService.updateProfile(user.patient.id, dto);
  }

  @Get('me/dashboard')
  @ApiOperation({ summary: 'Get patient dashboard (upcoming + past appointments)' })
  async getDashboard(@CurrentUser() user: User) {
    return this.patientsService.getDashboard(user.patient.id);
  }
}
