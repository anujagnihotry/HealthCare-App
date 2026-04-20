import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AppointmentsService } from './appointments.service';
import { AppointmentsController } from './appointments.controller';
import { Appointment } from './entities/appointment.entity';
import { Token } from '../tokens/entities/token.entity';
import { DoctorsModule } from '../doctors/doctors.module';
import { FamilyMembersModule } from '../family-members/family-members.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([Appointment, Token]),
    DoctorsModule,
    FamilyMembersModule,
  ],
  controllers: [AppointmentsController],
  providers: [AppointmentsService],
  exports: [AppointmentsService],
})
export class AppointmentsModule {}
