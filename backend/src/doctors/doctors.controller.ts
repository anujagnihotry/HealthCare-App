import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { DoctorsService } from './doctors.service';
import {
  CreateLocationDto,
  CreateAvailabilityDto,
  CreateUnavailabilityDto,
  UpdateDoctorProfileDto,
} from './dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles, CurrentUser } from '../common/decorators';
import { UserRole } from '../common/enums';
import { User } from '../users/entities/user.entity';

@ApiTags('Doctors')
@Controller('doctors')
export class DoctorsController {
  constructor(private readonly doctorsService: DoctorsService) {}

  // ── Public ───────────────────────────────────────────

  @Get()
  @ApiOperation({ summary: 'List all doctors (public)' })
  async findAll() {
    return this.doctorsService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get doctor details (public)' })
  async findById(@Param('id') id: string) {
    return this.doctorsService.findById(id);
  }

  @Get(':doctorId/locations/:locationId/slots')
  @ApiOperation({ summary: 'Get available slots for a date (public)' })
  async getAvailableSlots(
    @Param('doctorId') doctorId: string,
    @Param('locationId') locationId: string,
    @Query('date') date: string,
  ) {
    return this.doctorsService.getAvailableSlots(doctorId, locationId, date);
  }

  // ── Doctor Profile (auth required) ──────────────────

  @Get('me/profile')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get own doctor profile' })
  async getMyProfile(@CurrentUser() user: User) {
    return this.doctorsService.getProfile(user.doctor.id);
  }

  @Patch('me/profile')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Update own doctor profile' })
  async updateMyProfile(
    @CurrentUser() user: User,
    @Body() dto: UpdateDoctorProfileDto,
  ) {
    return this.doctorsService.updateProfile(user.doctor.id, dto);
  }

  // ── Locations ────────────────────────────────────────

  @Post('me/locations')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Add a clinic location' })
  async createLocation(
    @CurrentUser() user: User,
    @Body() dto: CreateLocationDto,
  ) {
    return this.doctorsService.createLocation(user.doctor.id, dto);
  }

  @Get('me/locations')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'List my clinic locations' })
  async getMyLocations(@CurrentUser() user: User) {
    return this.doctorsService.getLocations(user.doctor.id);
  }

  @Patch('me/locations/:locationId')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Update a clinic location' })
  async updateLocation(
    @CurrentUser() user: User,
    @Param('locationId') locationId: string,
    @Body() dto: Partial<CreateLocationDto>,
  ) {
    return this.doctorsService.updateLocation(user.doctor.id, locationId, dto);
  }

  @Delete('me/locations/:locationId')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Deactivate a clinic location' })
  async deleteLocation(
    @CurrentUser() user: User,
    @Param('locationId') locationId: string,
  ) {
    return this.doctorsService.deleteLocation(user.doctor.id, locationId);
  }

  // ── Availability ─────────────────────────────────────

  @Post('me/availability')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Add availability session' })
  async createAvailability(
    @CurrentUser() user: User,
    @Body() dto: CreateAvailabilityDto,
  ) {
    return this.doctorsService.createAvailability(user.doctor.id, dto);
  }

  @Get('me/availability')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'List my availability sessions' })
  async getMyAvailabilities(
    @CurrentUser() user: User,
    @Query('locationId') locationId?: string,
  ) {
    return this.doctorsService.getAvailabilities(user.doctor.id, locationId);
  }

  @Patch('me/availability/:availabilityId')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Update availability session' })
  async updateAvailability(
    @CurrentUser() user: User,
    @Param('availabilityId') availabilityId: string,
    @Body() dto: Partial<CreateAvailabilityDto>,
  ) {
    return this.doctorsService.updateAvailability(
      user.doctor.id,
      availabilityId,
      dto,
    );
  }

  @Delete('me/availability/:availabilityId')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Remove availability session' })
  async deleteAvailability(
    @CurrentUser() user: User,
    @Param('availabilityId') availabilityId: string,
  ) {
    return this.doctorsService.deleteAvailability(
      user.doctor.id,
      availabilityId,
    );
  }

  // ── Unavailability ───────────────────────────────────

  @Post('me/unavailability')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Mark unavailable date/session' })
  async createUnavailability(
    @CurrentUser() user: User,
    @Body() dto: CreateUnavailabilityDto,
  ) {
    return this.doctorsService.createUnavailability(user.doctor.id, dto);
  }

  @Get('me/unavailability')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'List my unavailability records' })
  async getMyUnavailabilities(@CurrentUser() user: User) {
    return this.doctorsService.getUnavailabilities(user.doctor.id);
  }

  @Delete('me/unavailability/:unavailabilityId')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Remove unavailability record' })
  async deleteUnavailability(
    @CurrentUser() user: User,
    @Param('unavailabilityId') unavailabilityId: string,
  ) {
    return this.doctorsService.deleteUnavailability(
      user.doctor.id,
      unavailabilityId,
    );
  }
}
