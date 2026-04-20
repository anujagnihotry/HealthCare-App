import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Param,
  Body,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiQuery } from '@nestjs/swagger';
import { JwtAuthGuard } from '../../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../../auth/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { UserRole } from '../../common/enums';
import { AdminPatientsService } from '../services/admin-patients.service';
import { AdminCreatePatientDto } from '../dto/admin-create-patient.dto';
import { AdminUpdatePatientDto } from '../dto/admin-update-patient.dto';

@ApiTags('Admin - Patients')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.SUPER_ADMIN)
@Controller('admin/patients')
export class AdminPatientsController {
  constructor(private readonly patientsService: AdminPatientsService) {}

  @Get()
  @ApiOperation({ summary: 'List all patients with search' })
  @ApiQuery({ name: 'search', required: false })
  @ApiQuery({ name: 'page', required: false })
  @ApiQuery({ name: 'limit', required: false })
  findAll(
    @Query('search') search?: string,
    @Query('page') page = 1,
    @Query('limit') limit = 20,
  ) {
    return this.patientsService.findAll(search, +page, +limit);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get patient by ID with family members' })
  findOne(@Param('id') id: string) {
    return this.patientsService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Create a new patient' })
  create(@Body() dto: AdminCreatePatientDto) {
    return this.patientsService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update patient profile' })
  update(@Param('id') id: string, @Body() dto: AdminUpdatePatientDto) {
    return this.patientsService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Deactivate (soft-delete) a patient' })
  remove(@Param('id') id: string) {
    return this.patientsService.remove(id);
  }
}
