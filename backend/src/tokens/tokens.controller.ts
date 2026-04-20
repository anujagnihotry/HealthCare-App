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
import { TokensService } from './tokens.service';
import { AssignOfflineTokenDto, SwapTokensDto } from './dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles, CurrentUser } from '../common/decorators';
import { UserRole } from '../common/enums';
import { User } from '../users/entities/user.entity';

@ApiTags('Tokens')
@Controller('tokens')
export class TokensController {
  constructor(private readonly tokensService: TokensService) {}

  // ── Public (for patient live tracking) ───────────────

  @Get('current')
  @ApiOperation({ summary: 'Get currently serving token (public)' })
  async getCurrentToken(
    @Query('doctorId') doctorId: string,
    @Query('locationId') locationId: string,
    @Query('date') date: string,
  ) {
    return this.tokensService.getCurrentToken(doctorId, locationId, date);
  }

  // ── Doctor Endpoints ─────────────────────────────────

  @Get('queue')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get full token queue for a date' })
  async getQueue(
    @CurrentUser() user: User,
    @Query('locationId') locationId: string,
    @Query('date') date: string,
  ) {
    return this.tokensService.getTokenQueue(
      user.doctor.id,
      locationId,
      date,
    );
  }

  @Post('start-serving')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Start serving first token' })
  async startServing(
    @CurrentUser() user: User,
    @Body() body: { locationId: string; date: string },
  ) {
    return this.tokensService.startServing(
      user.doctor.id,
      body.locationId,
      body.date,
    );
  }

  @Post('advance')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Advance to next token' })
  async advance(
    @CurrentUser() user: User,
    @Body() body: { locationId: string; date: string },
  ) {
    return this.tokensService.advanceToken(
      user.doctor.id,
      body.locationId,
      body.date,
    );
  }

  @Post('swap')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Swap token positions' })
  async swap(@CurrentUser() user: User, @Body() dto: SwapTokensDto) {
    return this.tokensService.swapTokens(user.doctor.id, dto);
  }

  @Post('offline')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Assign offline walk-in token' })
  async assignOffline(
    @CurrentUser() user: User,
    @Body() dto: AssignOfflineTokenDto,
  ) {
    return this.tokensService.assignOfflineToken(user.doctor.id, dto);
  }

  @Patch(':tokenId/arrival')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Mark patient arrived/not arrived' })
  async markArrival(
    @Param('tokenId') tokenId: string,
    @Body() body: { hasArrived: boolean },
  ) {
    return this.tokensService.markArrival(tokenId, body.hasArrived);
  }

  @Patch(':tokenId/skip')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DOCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Skip a token (no-show)' })
  async skip(@Param('tokenId') tokenId: string) {
    return this.tokensService.skipToken(tokenId);
  }
}
