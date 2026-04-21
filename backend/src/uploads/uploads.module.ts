import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UploadsService } from './uploads.service';
import { UploadsController } from './uploads.controller';
import { Upload } from './entities/upload.entity';
import { MedicalRecord } from './entities/medical-record.entity';
import { Consultation } from '../doctor-care/entities/consultation.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Upload, MedicalRecord, Consultation])],
  controllers: [UploadsController],
  providers: [UploadsService],
  exports: [UploadsService],
})
export class UploadsModule {}
