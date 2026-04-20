import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  OneToOne,
  JoinColumn,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';
import { Patient } from '../../patients/entities/patient.entity';
import { FamilyMember } from '../../family-members/entities/family-member.entity';
import { Doctor } from '../../doctors/entities/doctor.entity';
import { DoctorLocation } from '../../doctors/entities/doctor-location.entity';
import { AppointmentStatus } from '../../common/enums';
import { Token } from '../../tokens/entities/token.entity';

@Entity('appointments')
@Index(['doctorId', 'locationId', 'date'])
@Index(['patientId', 'date'])
export class Appointment {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'patient_id' })
  patientId: string;

  @ManyToOne(() => Patient, (patient) => patient.appointments)
  @JoinColumn({ name: 'patient_id' })
  patient: Patient;

  @Column({ name: 'family_member_id' })
  familyMemberId: string;

  @ManyToOne(() => FamilyMember, (fm) => fm.appointments)
  @JoinColumn({ name: 'family_member_id' })
  familyMember: FamilyMember;

  @Column({ name: 'doctor_id' })
  doctorId: string;

  @ManyToOne(() => Doctor, (doctor) => doctor.appointments)
  @JoinColumn({ name: 'doctor_id' })
  doctor: Doctor;

  @Column({ name: 'location_id' })
  locationId: string;

  @ManyToOne(() => DoctorLocation, (loc) => loc.appointments)
  @JoinColumn({ name: 'location_id' })
  location: DoctorLocation;

  @Column({ type: 'date' })
  date: string;

  @Column({ name: 'time_slot', type: 'time' })
  timeSlot: string;

  @Column({
    type: 'enum',
    enum: AppointmentStatus,
    default: AppointmentStatus.BOOKED,
  })
  status: AppointmentStatus;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @OneToOne(() => Token, (token) => token.appointment)
  token: Token;
}
