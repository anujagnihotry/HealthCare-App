import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { FamilyMembersService } from './family-members.service';
import { FamilyMembersController } from './family-members.controller';
import { FamilyMember } from './entities/family-member.entity';

@Module({
  imports: [TypeOrmModule.forFeature([FamilyMember])],
  controllers: [FamilyMembersController],
  providers: [FamilyMembersService],
  exports: [FamilyMembersService],
})
export class FamilyMembersModule {}
