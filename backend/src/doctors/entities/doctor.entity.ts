import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToOne,
  OneToMany,
  JoinColumn,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { User } from '../../users/entities/user.entity';
import { DoctorLocation } from './doctor-location.entity';
import { DoctorAvailability } from './doctor-availability.entity';
import { DoctorUnavailability } from './doctor-unavailability.entity';
import { Appointment } from '../../appointments/entities/appointment.entity';
import { Token } from '../../tokens/entities/token.entity';
import { MedicalRecord } from '../../uploads/entities/medical-record.entity';

@Entity('doctors')
export class Doctor {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'user_id' })
  userId: string;

  @OneToOne(() => User, (user) => user.doctor)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column()
  name: string;

  @Column()
  specialization: string;

  @Column({ nullable: true })
  bio: string;

  @Column({ name: 'profile_image_url', nullable: true })
  profileImageUrl: string;

  @Column({ name: 'contact_phone', nullable: true })
  contactPhone: string;

  @Column({ name: 'contact_email', nullable: true })
  contactEmail: string;

  @Column({ name: 'years_of_experience', type: 'int', nullable: true })
  yearsOfExperience: number | null;

  @Column({ type: 'varchar', length: 200, nullable: true })
  degree: string | null;

  /** homeopathy | allopathy | ayush */
  @Column({ name: 'medical_system', type: 'varchar', length: 32, nullable: true })
  medicalSystem: string | null;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @OneToMany(() => DoctorLocation, (loc) => loc.doctor)
  locations: DoctorLocation[];

  @OneToMany(() => DoctorAvailability, (av) => av.doctor)
  availabilities: DoctorAvailability[];

  @OneToMany(() => DoctorUnavailability, (unav) => unav.doctor)
  unavailabilities: DoctorUnavailability[];

  @OneToMany(() => Appointment, (apt) => apt.doctor)
  appointments: Appointment[];

  @OneToMany(() => Token, (token) => token.doctor)
  tokens: Token[];

  @OneToMany(() => MedicalRecord, (record) => record.doctor)
  medicalRecords: MedicalRecord[];
}
