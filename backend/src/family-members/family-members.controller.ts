import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Body,
  Param,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { FamilyMembersService } from './family-members.service';
import { CreateFamilyMemberDto } from './dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles, CurrentUser } from '../common/decorators';
import { UserRole } from '../common/enums';
import { User } from '../users/entities/user.entity';

@ApiTags('Family Members')
@Controller('patients/me/family-members')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.PATIENT)
@ApiBearerAuth()
export class FamilyMembersController {
  constructor(private readonly familyMembersService: FamilyMembersService) {}

  @Post()
  @ApiOperation({ summary: 'Add a family member' })
  async create(
    @CurrentUser() user: User,
    @Body() dto: CreateFamilyMemberDto,
  ) {
    return this.familyMembersService.create(user.patient.id, dto);
  }

  @Get()
  @ApiOperation({ summary: 'List all family members' })
  async findAll(@CurrentUser() user: User) {
    return this.familyMembersService.findAll(user.patient.id);
  }

  @Get(':memberId')
  @ApiOperation({ summary: 'Get a family member' })
  async findOne(
    @CurrentUser() user: User,
    @Param('memberId') memberId: string,
  ) {
    return this.familyMembersService.findOne(user.patient.id, memberId);
  }

  @Patch(':memberId')
  @ApiOperation({ summary: 'Update a family member' })
  async update(
    @CurrentUser() user: User,
    @Param('memberId') memberId: string,
    @Body() dto: Partial<CreateFamilyMemberDto>,
  ) {
    return this.familyMembersService.update(user.patient.id, memberId, dto);
  }

  @Delete(':memberId')
  @ApiOperation({ summary: 'Remove a family member' })
  async remove(
    @CurrentUser() user: User,
    @Param('memberId') memberId: string,
  ) {
    return this.familyMembersService.remove(user.patient.id, memberId);
  }
}
