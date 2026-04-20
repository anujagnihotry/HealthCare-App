import {
  Injectable,
  NotFoundException,
  ForbiddenException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { FamilyMember } from './entities/family-member.entity';
import { CreateFamilyMemberDto } from './dto';

@Injectable()
export class FamilyMembersService {
  constructor(
    @InjectRepository(FamilyMember)
    private familyMembersRepo: Repository<FamilyMember>,
  ) {}

  async create(patientId: string, dto: CreateFamilyMemberDto) {
    const member = this.familyMembersRepo.create({
      patientId,
      ...dto,
    });
    return this.familyMembersRepo.save(member);
  }

  async findAll(patientId: string) {
    return this.familyMembersRepo.find({
      where: { patientId },
      order: { isSelf: 'DESC', name: 'ASC' },
    });
  }

  async findOne(patientId: string, memberId: string) {
    const member = await this.familyMembersRepo.findOne({
      where: { id: memberId, patientId },
    });
    if (!member) throw new NotFoundException('Family member not found');
    return member;
  }

  async update(
    patientId: string,
    memberId: string,
    dto: Partial<CreateFamilyMemberDto>,
  ) {
    const member = await this.findOne(patientId, memberId);
    Object.assign(member, dto);
    return this.familyMembersRepo.save(member);
  }

  async remove(patientId: string, memberId: string) {
    const member = await this.findOne(patientId, memberId);
    if (member.isSelf) {
      throw new ForbiddenException('Cannot delete the self record');
    }
    await this.familyMembersRepo.remove(member);
    return { deleted: true };
  }
}
