import { DataSource } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { User } from '../../users/entities/user.entity';
import { Doctor } from '../../doctors/entities/doctor.entity';
import { Patient } from '../../patients/entities/patient.entity';
import { FamilyMember } from '../../family-members/entities/family-member.entity';
import { DoctorLocation } from '../../doctors/entities/doctor-location.entity';
import { DoctorAvailability } from '../../doctors/entities/doctor-availability.entity';
import {
  UserRole,
  Gender,
  BloodGroup,
} from '../../common/enums';

async function seed() {
  const dataSource = new DataSource({
    type: 'postgres',
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || '5432'),
    username: process.env.DB_USERNAME || 'healthcare_user',
    password: process.env.DB_PASSWORD || 'healthcare_pass_2024',
    database: process.env.DB_NAME || 'healthcare_db',
    entities: [__dirname + '/../../**/*.entity{.ts,.js}'],
    synchronize: true,
  });

  await dataSource.initialize();
  console.log('Database connected for seeding...');

  // Clean existing data (in correct order to respect foreign keys)
  console.log('Clearing existing data...');
  await dataSource.query('DELETE FROM "uploads"');
  await dataSource.query('DELETE FROM "medical_records"');
  await dataSource.query('DELETE FROM "consultations"');
  await dataSource.query('DELETE FROM "patient_vitals"');
  await dataSource.query('DELETE FROM "doctor_patient_mappings"');
  await dataSource.query('DELETE FROM "tokens"');
  await dataSource.query('DELETE FROM "appointments"');
  await dataSource.query('DELETE FROM "doctor_availability"');
  await dataSource.query('DELETE FROM "doctor_unavailability"');
  await dataSource.query('DELETE FROM "doctor_locations"');
  await dataSource.query('DELETE FROM "family_members"');
  await dataSource.query('DELETE FROM "doctors"');
  await dataSource.query('DELETE FROM "patients"');
  await dataSource.query('DELETE FROM "users"');
  console.log('Existing data cleared.');

  const userRepo = dataSource.getRepository(User);
  const doctorRepo = dataSource.getRepository(Doctor);
  const patientRepo = dataSource.getRepository(Patient);
  const familyMemberRepo = dataSource.getRepository(FamilyMember);
  const locationRepo = dataSource.getRepository(DoctorLocation);
  const availabilityRepo = dataSource.getRepository(DoctorAvailability);

  const passwordHash = await bcrypt.hash('password123', 12);

  // ── Create Doctor Users ────────────────────────────

  const doctorUser1 = await userRepo.save(
    userRepo.create({
      email: 'dr.sharma@example.com',
      phone: '+919876543001',
      passwordHash,
      role: UserRole.DOCTOR,
    }),
  );

  const doctor1 = await doctorRepo.save(
    doctorRepo.create({
      userId: doctorUser1.id,
      name: 'Dr. Rajesh Sharma',
      specialization: 'Cardiologist',
      bio: 'Senior cardiologist with 15 years of experience.',
      contactPhone: '+919876543001',
      contactEmail: 'dr.sharma@example.com',
      yearsOfExperience: 15,
      degree: 'MBBS, MD (Cardiology)',
      medicalSystem: 'allopathy',
    }),
  );

  const doctorUser2 = await userRepo.save(
    userRepo.create({
      email: 'dr.patel@example.com',
      phone: '+919876543002',
      passwordHash,
      role: UserRole.DOCTOR,
    }),
  );

  const doctor2 = await doctorRepo.save(
    doctorRepo.create({
      userId: doctorUser2.id,
      name: 'Dr. Priya Patel',
      specialization: 'General Physician',
      bio: 'Experienced GP focused on family medicine.',
      contactPhone: '+919876543002',
      contactEmail: 'dr.patel@example.com',
      yearsOfExperience: 10,
      degree: 'MBBS',
      medicalSystem: 'allopathy',
    }),
  );

  // ── Create Doctor Locations ────────────────────────

  const loc1 = await locationRepo.save(
    locationRepo.create({
      doctorId: doctor1.id,
      name: 'City Heart Clinic',
      address: '123 MG Road, Mumbai 400001',
      latitude: 19.076,
      longitude: 72.8777,
    }),
  );

  const loc2 = await locationRepo.save(
    locationRepo.create({
      doctorId: doctor1.id,
      name: 'Suburban Health Center',
      address: '45 Link Road, Andheri West, Mumbai',
      latitude: 19.1364,
      longitude: 72.8296,
    }),
  );

  const loc3 = await locationRepo.save(
    locationRepo.create({
      doctorId: doctor2.id,
      name: 'Patel Family Clinic',
      address: '78 Station Road, Pune 411001',
      latitude: 18.5204,
      longitude: 73.8567,
    }),
  );

  // ── Create Doctor Availability ─────────────────────

  // Dr. Sharma - Mon-Sat Morning at City Heart Clinic
  for (let day = 1; day <= 6; day++) {
    await availabilityRepo.save(
      availabilityRepo.create({
        doctorId: doctor1.id,
        locationId: loc1.id,
        dayOfWeek: day,
        sessionName: 'Morning',
        startTime: '07:00',
        endTime: '09:00',
        slotDurationMinutes: 10,
        breakStart: '08:00',
        breakEnd: '08:10',
      }),
    );
  }

  // Dr. Sharma - Mon/Wed/Fri Evening at Suburban Health Center
  for (const day of [1, 3, 5]) {
    await availabilityRepo.save(
      availabilityRepo.create({
        doctorId: doctor1.id,
        locationId: loc2.id,
        dayOfWeek: day,
        sessionName: 'Evening',
        startTime: '17:00',
        endTime: '19:00',
        slotDurationMinutes: 15,
      }),
    );
  }

  // Dr. Patel - Mon-Fri at Patel Family Clinic
  for (let day = 1; day <= 5; day++) {
    await availabilityRepo.save(
      availabilityRepo.create({
        doctorId: doctor2.id,
        locationId: loc3.id,
        dayOfWeek: day,
        sessionName: 'Morning',
        startTime: '09:00',
        endTime: '12:00',
        slotDurationMinutes: 15,
        breakStart: '10:30',
        breakEnd: '10:45',
      }),
    );

    await availabilityRepo.save(
      availabilityRepo.create({
        doctorId: doctor2.id,
        locationId: loc3.id,
        dayOfWeek: day,
        sessionName: 'Evening',
        startTime: '16:00',
        endTime: '18:00',
        slotDurationMinutes: 15,
      }),
    );
  }

  // ── Create Patient Users ───────────────────────────

  const patientUser1 = await userRepo.save(
    userRepo.create({
      email: 'rahul@example.com',
      phone: '+919876543010',
      passwordHash,
      role: UserRole.PATIENT,
    }),
  );

  const patient1 = await patientRepo.save(
    patientRepo.create({
      userId: patientUser1.id,
      name: 'Rahul Kumar',
      phone: '+919876543010',
      email: 'rahul@example.com',
    }),
  );

  // Self family member
  await familyMemberRepo.save(
    familyMemberRepo.create({
      patientId: patient1.id,
      name: 'Rahul Kumar',
      age: 35,
      gender: Gender.MALE,
      bloodGroup: BloodGroup.B_POSITIVE,
      isSelf: true,
    }),
  );

  // Family members
  await familyMemberRepo.save(
    familyMemberRepo.create({
      patientId: patient1.id,
      name: 'Sunita Kumar',
      age: 60,
      gender: Gender.FEMALE,
      bloodGroup: BloodGroup.O_POSITIVE,
      allergies: ['Penicillin'],
    }),
  );

  await familyMemberRepo.save(
    familyMemberRepo.create({
      patientId: patient1.id,
      name: 'Arjun Kumar',
      age: 8,
      gender: Gender.MALE,
      bloodGroup: BloodGroup.B_POSITIVE,
    }),
  );

  const patientUser2 = await userRepo.save(
    userRepo.create({
      email: 'meera@example.com',
      phone: '+919876543020',
      passwordHash,
      role: UserRole.PATIENT,
    }),
  );

  const patient2 = await patientRepo.save(
    patientRepo.create({
      userId: patientUser2.id,
      name: 'Meera Singh',
      phone: '+919876543020',
      email: 'meera@example.com',
    }),
  );

  await familyMemberRepo.save(
    familyMemberRepo.create({
      patientId: patient2.id,
      name: 'Meera Singh',
      age: 28,
      gender: Gender.FEMALE,
      bloodGroup: BloodGroup.A_POSITIVE,
      isSelf: true,
    }),
  );

  console.log('Seed complete!');
  console.log('');
  console.log('Test accounts (password: password123):');
  console.log('  Doctor 1: dr.sharma@example.com');
  console.log('  Doctor 2: dr.patel@example.com');
  console.log('  Patient 1: rahul@example.com');
  console.log('  Patient 2: meera@example.com');

  await dataSource.destroy();
}

seed().catch((err) => {
  console.error('Seed failed:', err);
  process.exit(1);
});
