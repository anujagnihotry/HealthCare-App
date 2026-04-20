import {
  Injectable,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, DataSource } from 'typeorm';
import { Token } from './entities/token.entity';
import { TokenStatus } from '../common/enums';
import { AssignOfflineTokenDto, SwapTokensDto } from './dto';

@Injectable()
export class TokensService {
  constructor(
    @InjectRepository(Token)
    private tokensRepo: Repository<Token>,
    private dataSource: DataSource,
  ) {}

  /**
   * Get all tokens for a doctor+location+date, ordered by position.
   */
  async getTokenQueue(doctorId: string, locationId: string, date: string) {
    return this.tokensRepo.find({
      where: { doctorId, locationId, date },
      relations: ['appointment', 'appointment.familyMember', 'appointment.patient'],
      order: { position: 'ASC' },
    });
  }

  /**
   * Get the currently serving token.
   */
  async getCurrentToken(doctorId: string, locationId: string, date: string) {
    const serving = await this.tokensRepo.findOne({
      where: {
        doctorId,
        locationId,
        date,
        status: TokenStatus.SERVING,
      },
    });

    const totalWaiting = await this.tokensRepo.count({
      where: {
        doctorId,
        locationId,
        date,
        status: TokenStatus.WAITING,
      },
    });

    return {
      currentToken: serving?.tokenNumber || null,
      totalWaiting,
    };
  }

  /**
   * Advance to next token — mark current as completed, next waiting as serving.
   */
  async advanceToken(doctorId: string, locationId: string, date: string) {
    return this.dataSource.transaction(async (manager) => {
      // Complete current serving token
      const current = await manager.findOne(Token, {
        where: {
          doctorId,
          locationId,
          date,
          status: TokenStatus.SERVING,
        },
      });
      if (current) {
        current.status = TokenStatus.COMPLETED;
        await manager.save(Token, current);
      }

      // Find next waiting token by position
      const next = await manager.findOne(Token, {
        where: {
          doctorId,
          locationId,
          date,
          status: TokenStatus.WAITING,
        },
        order: { position: 'ASC' },
      });

      if (next) {
        next.status = TokenStatus.SERVING;
        await manager.save(Token, next);
        return {
          currentToken: next.tokenNumber,
          tokenId: next.id,
        };
      }

      return { currentToken: null, message: 'No more tokens in queue' };
    });
  }

  /**
   * Start serving the first token (when no token is currently serving).
   */
  async startServing(doctorId: string, locationId: string, date: string) {
    const serving = await this.tokensRepo.findOne({
      where: { doctorId, locationId, date, status: TokenStatus.SERVING },
    });
    if (serving) {
      throw new BadRequestException(
        `Token ${serving.tokenNumber} is currently being served. Use advance to move to next.`,
      );
    }

    const next = await this.tokensRepo.findOne({
      where: { doctorId, locationId, date, status: TokenStatus.WAITING },
      order: { position: 'ASC' },
    });
    if (!next) {
      throw new BadRequestException('No waiting tokens');
    }

    next.status = TokenStatus.SERVING;
    await this.tokensRepo.save(next);
    return { currentToken: next.tokenNumber, tokenId: next.id };
  }

  /**
   * Swap token positions — move tokenId to after afterTokenId.
   */
  async swapTokens(doctorId: string, dto: SwapTokensDto) {
    return this.dataSource.transaction(async (manager) => {
      const tokenToMove = await manager.findOne(Token, {
        where: { id: dto.tokenId, doctorId },
      });
      const targetToken = await manager.findOne(Token, {
        where: { id: dto.afterTokenId, doctorId },
      });

      if (!tokenToMove || !targetToken) {
        throw new NotFoundException('Token not found');
      }
      if (tokenToMove.date !== targetToken.date) {
        throw new BadRequestException('Tokens must be on the same date');
      }

      // Get all tokens for this queue, ordered by position
      const allTokens = await manager.find(Token, {
        where: {
          doctorId,
          locationId: tokenToMove.locationId,
          date: tokenToMove.date,
        },
        order: { position: 'ASC' },
      });

      // Remove the token to move from the list
      const filtered = allTokens.filter((t) => t.id !== tokenToMove.id);

      // Find the index of the target token
      const targetIndex = filtered.findIndex((t) => t.id === targetToken.id);

      // Insert after target
      filtered.splice(targetIndex + 1, 0, tokenToMove);

      // Reassign positions
      for (let i = 0; i < filtered.length; i++) {
        filtered[i].position = i + 1;
        await manager.save(Token, filtered[i]);
      }

      return { message: 'Tokens swapped successfully' };
    });
  }

  /**
   * Assign an offline (walk-in) token.
   */
  async assignOfflineToken(doctorId: string, dto: AssignOfflineTokenDto) {
    return this.dataSource.transaction(async (manager) => {
      // Get next token number
      const maxResult = await manager
        .createQueryBuilder(Token, 'token')
        .select('MAX(token.tokenNumber)', 'maxToken')
        .where('token.doctorId = :doctorId', { doctorId })
        .andWhere('token.locationId = :locationId', {
          locationId: dto.locationId,
        })
        .andWhere('token.date = :date', { date: dto.date })
        .getRawOne();

      const nextTokenNumber = (maxResult?.maxToken || 0) + 1;

      // Get max position
      const maxPosResult = await manager
        .createQueryBuilder(Token, 'token')
        .select('MAX(token.position)', 'maxPos')
        .where('token.doctorId = :doctorId', { doctorId })
        .andWhere('token.locationId = :locationId', {
          locationId: dto.locationId,
        })
        .andWhere('token.date = :date', { date: dto.date })
        .getRawOne();

      const nextPosition = (maxPosResult?.maxPos || 0) + 1;

      const token = manager.create(Token, {
        doctorId,
        locationId: dto.locationId,
        date: dto.date,
        tokenNumber: nextTokenNumber,
        position: nextPosition,
        status: TokenStatus.WAITING,
        isOffline: true,
        patientName: dto.patientName || `Walk-in #${nextTokenNumber}`,
        hasArrived: true,
      });

      return manager.save(Token, token);
    });
  }

  /**
   * Mark patient as arrived/not arrived.
   */
  async markArrival(tokenId: string, hasArrived: boolean) {
    const token = await this.tokensRepo.findOne({ where: { id: tokenId } });
    if (!token) throw new NotFoundException('Token not found');
    token.hasArrived = hasArrived;
    return this.tokensRepo.save(token);
  }

  /**
   * Skip a token (patient didn't show up).
   */
  async skipToken(tokenId: string) {
    const token = await this.tokensRepo.findOne({ where: { id: tokenId } });
    if (!token) throw new NotFoundException('Token not found');
    token.status = TokenStatus.SKIPPED;
    return this.tokensRepo.save(token);
  }
}
