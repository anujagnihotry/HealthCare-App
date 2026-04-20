import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';
import { Doctor } from '../../doctors/entities/doctor.entity';
import { Patient } from '../../patients/entities/patient.entity';
import { FamilyMember } from '../../family-members/entities/family-member.entity';

@Entity('doctor_patient_mappings')
@Index(['doctorId', 'familyMemberId'], { unique: true })
@Index(['doctorId', 'customCode'], { unique: true })
export class DoctorPatientMapping {
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

  /** Doctor-assigned short code for search (unique per doctor when set). */
  @Column({ name: 'custom_code', type: 'varchar', length: 64, nullable: true })
  customCode: string | null;

  @Column({ name: 'first_visit_date', type: 'date', nullable: true })
  firstVisitDate: string | null;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;
}
