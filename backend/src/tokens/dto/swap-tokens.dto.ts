import { IsUUID } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class SwapTokensDto {
  @ApiProperty({ description: 'Token ID to move' })
  @IsUUID()
  tokenId: string;

  @ApiProperty({ description: 'Token ID to place after' })
  @IsUUID()
  afterTokenId: string;
}
