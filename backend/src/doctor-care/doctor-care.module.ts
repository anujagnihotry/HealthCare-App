import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Appointment } from '../appointments/entities/appointment.entity';
import { FamilyMember } from '../family-members/entities/family-member.entity';
import { Patient } from '../patients/entities/patient.entity';
import { User } from '../users/entities/user.entity';
import { MedicalRecord } from '../uploads/entities/medical-record.entity';
import { Upload } from '../uploads/entities/upload.entity';
import {
  DoctorPatientMapping,
  Consultation,
  PatientVital,
} from './entities';
import { DoctorPatientsService } from './doctor-patients.service';
import { ConsultationsService } from './consultations.service';
import { DoctorPatientsController } from './doctor-patients.controller';
import { ConsultationsController } from './consultations.controller';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      DoctorPatientMapping,
      Consultation,
      PatientVital,
      Appointment,
      FamilyMember,
      Patient,
      User,
      MedicalRecord,
      Upload,
    ]),
  ],
  controllers: [DoctorPatientsController, ConsultationsController],
  providers: [DoctorPatientsService, ConsultationsService],
  exports: [DoctorPatientsService, ConsultationsService],
})
export class DoctorCareModule {}
