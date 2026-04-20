import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { DoctorsService } from './doctors.service';
import { DoctorsController } from './doctors.controller';
import {
  Doctor,
  DoctorLocation,
  DoctorAvailability,
  DoctorUnavailability,
} from './entities';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      Doctor,
      DoctorLocation,
      DoctorAvailability,
      DoctorUnavailability,
    ]),
  ],
  controllers: [DoctorsController],
  providers: [DoctorsService],
  exports: [DoctorsService],
})
export class DoctorsModule {}
