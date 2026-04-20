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
import { Doctor } from './doctor.entity';
import { DoctorAvailability } from './doctor-availability.entity';
import { DoctorUnavailability } from './doctor-unavailability.entity';
import { Appointment } from '../../appointments/entities/appointment.entity';
import { Token } from '../../tokens/entities/token.entity';

@Entity('doctor_locations')
export class DoctorLocation {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'doctor_id' })
  doctorId: string;

  @ManyToOne(() => Doctor, (doctor) => doctor.locations)
  @JoinColumn({ name: 'doctor_id' })
  doctor: Doctor;

  @Column()
  name: string;

  @Column()
  address: string;

  @Column({ type: 'decimal', precision: 10, scale: 7 })
  latitude: number;

  @Column({ type: 'decimal', precision: 10, scale: 7 })
  longitude: number;

  @Column({ name: 'is_active', default: true })
  isActive: boolean;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @OneToMany(() => DoctorAvailability, (av) => av.location)
  availabilities: DoctorAvailability[];

  @OneToMany(() => DoctorUnavailability, (unav) => unav.location)
  unavailabilities: DoctorUnavailability[];

  @OneToMany(() => Appointment, (apt) => apt.location)
  appointments: Appointment[];

  @OneToMany(() => Token, (token) => token.location)
  tokens: Token[];
}
