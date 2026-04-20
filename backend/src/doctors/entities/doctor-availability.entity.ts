import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { Doctor } from './doctor.entity';
import { DoctorLocation } from './doctor-location.entity';

@Entity('doctor_availability')
export class DoctorAvailability {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'doctor_id' })
  doctorId: string;

  @ManyToOne(() => Doctor, (doctor) => doctor.availabilities)
  @JoinColumn({ name: 'doctor_id' })
  doctor: Doctor;

  @Column({ name: 'location_id' })
  locationId: string;

  @ManyToOne(() => DoctorLocation, (loc) => loc.availabilities)
  @JoinColumn({ name: 'location_id' })
  location: DoctorLocation;

  @Column({ name: 'day_of_week', type: 'int' })
  dayOfWeek: number; // 0=Sunday, 1=Monday, ..., 6=Saturday

  @Column({ name: 'session_name' })
  sessionName: string; // e.g., "Morning", "Evening"

  @Column({ name: 'start_time', type: 'time' })
  startTime: string; // HH:mm

  @Column({ name: 'end_time', type: 'time' })
  endTime: string; // HH:mm

  @Column({ name: 'slot_duration_minutes', type: 'int', default: 10 })
  slotDurationMinutes: number;

  @Column({ name: 'break_start', type: 'time', nullable: true })
  breakStart: string;

  @Column({ name: 'break_end', type: 'time', nullable: true })
  breakEnd: string;

  @Column({ name: 'is_active', default: true })
  isActive: boolean;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;
}
