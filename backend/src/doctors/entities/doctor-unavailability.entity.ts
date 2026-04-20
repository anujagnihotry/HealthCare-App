import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
  CreateDateColumn,
} from 'typeorm';
import { Doctor } from './doctor.entity';
import { DoctorLocation } from './doctor-location.entity';

@Entity('doctor_unavailability')
export class DoctorUnavailability {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'doctor_id' })
  doctorId: string;

  @ManyToOne(() => Doctor, (doctor) => doctor.unavailabilities)
  @JoinColumn({ name: 'doctor_id' })
  doctor: Doctor;

  @Column({ name: 'location_id', nullable: true })
  locationId: string;

  @ManyToOne(() => DoctorLocation, (loc) => loc.unavailabilities, {
    nullable: true,
  })
  @JoinColumn({ name: 'location_id' })
  location: DoctorLocation;

  @Column({ type: 'date' })
  date: string;

  // Null = full day unavailable, otherwise specific session
  @Column({ name: 'session_name', nullable: true })
  sessionName: string;

  @Column({ nullable: true })
  reason: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;
}
