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
import { AdminDoctorsService } from '../services/admin-doctors.service';
import { AdminCreateDoctorDto } from '../dto/admin-create-doctor.dto';
import { AdminUpdateDoctorDto } from '../dto/admin-update-doctor.dto';

@ApiTags('Admin - Doctors')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.SUPER_ADMIN)
@Controller('admin/doctors')
export class AdminDoctorsController {
  constructor(private readonly doctorsService: AdminDoctorsService) {}

  @Get()
  @ApiOperation({ summary: 'List all doctors with search + filter' })
  @ApiQuery({ name: 'search', required: false })
  @ApiQuery({ name: 'specialization', required: false })
  @ApiQuery({ name: 'page', required: false })
  @ApiQuery({ name: 'limit', required: false })
  findAll(
    @Query('search') search?: string,
    @Query('specialization') specialization?: string,
    @Query('page') page = 1,
    @Query('limit') limit = 20,
  ) {
    return this.doctorsService.findAll(search, specialization, +page, +limit);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get doctor by ID' })
  findOne(@Param('id') id: string) {
    return this.doctorsService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Create a new doctor' })
  create(@Body() dto: AdminCreateDoctorDto) {
    return this.doctorsService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update doctor profile' })
  update(@Param('id') id: string, @Body() dto: AdminUpdateDoctorDto) {
    return this.doctorsService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Deactivate (soft-delete) a doctor' })
  remove(@Param('id') id: string) {
    return this.doctorsService.remove(id);
  }
}
