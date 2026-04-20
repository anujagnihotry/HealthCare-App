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
import { Doctor } from '../../doctors/entities/doctor.entity';
import { DoctorLocation } from '../../doctors/entities/doctor-location.entity';
import { Appointment } from '../../appointments/entities/appointment.entity';
import { TokenStatus } from '../../common/enums';

@Entity('tokens')
@Index(['doctorId', 'locationId', 'date'])
@Index(['doctorId', 'locationId', 'date', 'tokenNumber'], { unique: true })
export class Token {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'appointment_id', nullable: true })
  appointmentId: string;

  @OneToOne(() => Appointment, (apt) => apt.token, { nullable: true })
  @JoinColumn({ name: 'appointment_id' })
  appointment: Appointment;

  @Column({ name: 'doctor_id' })
  doctorId: string;

  @ManyToOne(() => Doctor, (doctor) => doctor.tokens)
  @JoinColumn({ name: 'doctor_id' })
  doctor: Doctor;

  @Column({ name: 'location_id' })
  locationId: string;

  @ManyToOne(() => DoctorLocation, (loc) => loc.tokens)
  @JoinColumn({ name: 'location_id' })
  location: DoctorLocation;

  @Column({ type: 'date' })
  date: string;

  @Column({ name: 'token_number', type: 'int' })
  tokenNumber: number;

  @Column({
    type: 'enum',
    enum: TokenStatus,
    default: TokenStatus.WAITING,
  })
  status: TokenStatus;

  // Position in the queue — allows reordering via swap
  @Column({ type: 'int' })
  position: number;

  // Whether patient has physically arrived
  @Column({ name: 'has_arrived', default: false })
  hasArrived: boolean;

  // True for walk-in patients assigned offline tokens
  @Column({ name: 'is_offline', default: false })
  isOffline: boolean;

  @Column({ name: 'patient_name', nullable: true })
  patientName: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;
}
