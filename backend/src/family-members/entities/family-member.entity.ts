import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  OneToMany,
  JoinColumn,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { Patient } from '../../patients/entities/patient.entity';
import { Gender, BloodGroup } from '../../common/enums';
import { Appointment } from '../../appointments/entities/appointment.entity';
import { MedicalRecord } from '../../uploads/entities/medical-record.entity';
import { Upload } from '../../uploads/entities/upload.entity';

@Entity('family_members')
export class FamilyMember {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'patient_id' })
  patientId: string;

  @ManyToOne(() => Patient, (patient) => patient.familyMembers)
  @JoinColumn({ name: 'patient_id' })
  patient: Patient;

  @Column()
  name: string;

  @Column({ type: 'int' })
  age: number;

  @Column({ type: 'enum', enum: Gender })
  gender: Gender;

  @Column({ name: 'blood_group', type: 'enum', enum: BloodGroup, nullable: true })
  bloodGroup: BloodGroup;

  @Column({ type: 'text', array: true, default: '{}' })
  allergies: string[];

  @Column({ name: 'is_self', default: false })
  isSelf: boolean;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @OneToMany(() => Appointment, (apt) => apt.familyMember)
  appointments: Appointment[];

  @OneToMany(() => MedicalRecord, (record) => record.familyMember)
  medicalRecords: MedicalRecord[];

  @OneToMany(() => Upload, (upload) => upload.familyMember)
  uploads: Upload[];
}
