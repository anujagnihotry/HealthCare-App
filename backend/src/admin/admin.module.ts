import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AuthModule } from '../auth/auth.module';
import { User } from '../users/entities/user.entity';
import { Doctor } from '../doctors/entities/doctor.entity';
import { Patient } from '../patients/entities/patient.entity';
import { FamilyMember } from '../family-members/entities/family-member.entity';
import { Appointment } from '../appointments/entities/appointment.entity';
import { Consultation } from '../doctor-care/entities/consultation.entity';
import { Upload } from '../uploads/entities/upload.entity';
import { MedicalRecord } from '../uploads/entities/medical-record.entity';
import { Lead } from './entities/lead.entity';

import { AdminAuthController } from './controllers/admin-auth.controller';
import { AdminDoctorsController } from './controllers/admin-doctors.controller';
import { AdminPatientsController } from './controllers/admin-patients.controller';
import { AdminAppointmentsController } from './controllers/admin-appointments.controller';
import { AdminAnalyticsController } from './controllers/admin-analytics.controller';
import { AdminReportsController } from './controllers/admin-reports.controller';
import { AdminLeadsController } from './controllers/admin-leads.controller';

import { AdminAuthService } from './services/admin-auth.service';
import { AdminDoctorsService } from './services/admin-doctors.service';
import { AdminPatientsService } from './services/admin-patients.service';
import { AdminAppointmentsService } from './services/admin-appointments.service';
import { AdminAnalyticsService } from './services/admin-analytics.service';
import { AdminReportsService } from './services/admin-reports.service';
import { AdminLeadsService } from './services/admin-leads.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      User,
      Doctor,
      Patient,
      FamilyMember,
      Appointment,
      Consultation,
      Upload,
      MedicalRecord,
      Lead,
    ]),
    AuthModule,
  ],
  controllers: [
    AdminAuthController,
    AdminDoctorsController,
    AdminPatientsController,
    AdminAppointmentsController,
    AdminAnalyticsController,
    AdminReportsController,
    AdminLeadsController,
  ],
  providers: [
    AdminAuthService,
    AdminDoctorsService,
    AdminPatientsService,
    AdminAppointmentsService,
    AdminAnalyticsService,
    AdminReportsService,
    AdminLeadsService,
  ],
})
export class AdminModule {}
