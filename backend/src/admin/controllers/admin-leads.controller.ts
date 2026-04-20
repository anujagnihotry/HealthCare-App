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
import { UserRole, LeadStatus, LeadSource } from '../../common/enums';
import { AdminLeadsService } from '../services/admin-leads.service';
import { AdminCreateLeadDto } from '../dto/admin-create-lead.dto';
import { AdminUpdateLeadDto } from '../dto/admin-update-lead.dto';

@ApiTags('Admin - Leads')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.SUPER_ADMIN)
@Controller('admin/leads')
export class AdminLeadsController {
  constructor(private readonly leadsService: AdminLeadsService) {}

  @Get()
  @ApiOperation({ summary: 'List all leads' })
  @ApiQuery({ name: 'status', required: false, enum: LeadStatus })
  @ApiQuery({ name: 'source', required: false, enum: LeadSource })
  @ApiQuery({ name: 'search', required: false })
  @ApiQuery({ name: 'page', required: false })
  @ApiQuery({ name: 'limit', required: false })
  findAll(
    @Query('status') status?: LeadStatus,
    @Query('source') source?: LeadSource,
    @Query('search') search?: string,
    @Query('page') page = 1,
    @Query('limit') limit = 20,
  ) {
    return this.leadsService.findAll({ status, source, search, page: +page, limit: +limit });
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get lead by ID' })
  findOne(@Param('id') id: string) {
    return this.leadsService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Create a lead' })
  create(@Body() dto: AdminCreateLeadDto) {
    return this.leadsService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update lead status or notes' })
  update(@Param('id') id: string, @Body() dto: AdminUpdateLeadDto) {
    return this.leadsService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Delete a lead' })
  remove(@Param('id') id: string) {
    return this.leadsService.remove(id);
  }
}
