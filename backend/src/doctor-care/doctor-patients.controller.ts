import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles, CurrentUser } from '../common/decorators';
import { UserRole } from '../common/enums';
import { User } from '../users/entities/user.entity';
import { DoctorPatientsService } from './doctor-patients.service';
import { AssignCustomCodeDto } from './dto';

@ApiTags('Doctor Patients')
@Controller('doctor/patients')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.DOCTOR)
@ApiBearerAuth()
export class DoctorPatientsController {
  constructor(private readonly doctorPatientsService: DoctorPatientsService) {}

  @Get('search')
  @ApiOperation({
    summary: 'Search patients seen by this doctor (name, mobile, or custom code)',
  })
  async search(
    @CurrentUser() user: User,
    @Query('q') q: string,
    @Query('mode') mode?: 'name' | 'mobile' | 'code',
  ) {
    const doctorId = user.doctor.id;
    return this.doctorPatientsService.searchPatients(doctorId, q || '', mode);
  }

  @Get(':familyMemberId/history')
  @ApiOperation({ summary: 'Patient history scoped to this doctor' })
  async history(
    @CurrentUser() user: User,
    @Param('familyMemberId') familyMemberId: string,
  ) {
    return this.doctorPatientsService.getPatientHistory(
      user.doctor.id,
      familyMemberId,
    );
  }

  @Post(':familyMemberId/custom-code')
  @ApiOperation({ summary: 'Assign or update custom patient code' })
  async assignCode(
    @CurrentUser() user: User,
    @Param('familyMemberId') familyMemberId: string,
    @Body() dto: AssignCustomCodeDto,
  ) {
    return this.doctorPatientsService.assignCustomCode(
      user.doctor.id,
      familyMemberId,
      dto,
    );
  }
}
