import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
  CreateDateColumn,
  Index,
} from 'typeorm';
import { Doctor } from '../../doctors/entities/doctor.entity';
import { Patient } from '../../patients/entities/patient.entity';
import { FamilyMember } from '../../family-members/entities/family-member.entity';

@Entity('patient_vitals')
@Index(['doctorId', 'familyMemberId'])
@Index(['recordedAt'])
export class PatientVital {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'doctor_id' })
  doctorId: string;

  @ManyToOne(() => Doctor)
  @JoinColumn({ name: 'doctor_id' })
  doctor: Doctor;

  @Column({ name: 'patient_id' })
  patientId: string;

  @ManyToOne(() => Patient)
  @JoinColumn({ name: 'patient_id' })
  patient: Patient;

  @Column({ name: 'family_member_id' })
  familyMemberId: string;

  @ManyToOne(() => FamilyMember)
  @JoinColumn({ name: 'family_member_id' })
  familyMember: FamilyMember;

  @Column({ type: 'varchar', length: 32, nullable: true })
  bp: string | null;

  @Column({ type: 'varchar', length: 32, nullable: true })
  sugar: string | null;

  @Column({ type: 'varchar', length: 32, nullable: true })
  height: string | null;

  @Column({ type: 'varchar', length: 32, nullable: true })
  weight: string | null;

  @CreateDateColumn({ name: 'recorded_at' })
  recordedAt: Date;
}
