import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Lead } from '../entities/lead.entity';
import { LeadStatus, LeadSource } from '../../common/enums';
import { AdminCreateLeadDto } from '../dto/admin-create-lead.dto';
import { AdminUpdateLeadDto } from '../dto/admin-update-lead.dto';

@Injectable()
export class AdminLeadsService {
  constructor(
    @InjectRepository(Lead)
    private leadsRepo: Repository<Lead>,
  ) {}

  async findAll(filters: {
    status?: LeadStatus;
    source?: LeadSource;
    search?: string;
    page?: number;
    limit?: number;
  }) {
    const { status, source, search, page = 1, limit = 20 } = filters;
    const qb = this.leadsRepo.createQueryBuilder('lead');

    if (status) qb.andWhere('lead.status = :status', { status });
    if (source) qb.andWhere('lead.source = :source', { source });
    if (search) {
      qb.andWhere('(lead.name ILIKE :s OR lead.phone ILIKE :s OR lead.email ILIKE :s)', {
        s: `%${search}%`,
      });
    }

    const [data, total] = await qb
      .orderBy('lead.createdAt', 'DESC')
      .skip((page - 1) * limit)
      .take(limit)
      .getManyAndCount();

    return { data, total, page, limit };
  }

  async findOne(id: string) {
    const lead = await this.leadsRepo.findOne({ where: { id } });
    if (!lead) throw new NotFoundException('Lead not found');
    return lead;
  }

  async create(dto: AdminCreateLeadDto) {
    const lead = this.leadsRepo.create(dto);
    return this.leadsRepo.save(lead);
  }

  async update(id: string, dto: AdminUpdateLeadDto) {
    await this.findOne(id);
    await this.leadsRepo.update(id, dto as any);
    return this.findOne(id);
  }

  async remove(id: string) {
    await this.findOne(id);
    await this.leadsRepo.delete(id);
    return { message: 'Lead deleted successfully' };
  }
}
