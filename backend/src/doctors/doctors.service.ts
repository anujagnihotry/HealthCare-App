import {
  Injectable,
  NotFoundException,
  ForbiddenException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import {
  Doctor,
  DoctorLocation,
  DoctorAvailability,
  DoctorUnavailability,
} from './entities';
import {
  CreateLocationDto,
  CreateAvailabilityDto,
  CreateUnavailabilityDto,
  UpdateDoctorProfileDto,
} from './dto';

@Injectable()
export class DoctorsService {
  constructor(
    @InjectRepository(Doctor)
    private doctorsRepo: Repository<Doctor>,
    @InjectRepository(DoctorLocation)
    private locationsRepo: Repository<DoctorLocation>,
    @InjectRepository(DoctorAvailability)
    private availabilityRepo: Repository<DoctorAvailability>,
    @InjectRepository(DoctorUnavailability)
    private unavailabilityRepo: Repository<DoctorUnavailability>,
  ) {}

  // ── Profile ──────────────────────────────────────────

  async getProfile(doctorId: string) {
    const doctor = await this.doctorsRepo.findOne({
      where: { id: doctorId },
      relations: ['locations', 'availabilities', 'availabilities.location'],
    });
    if (!doctor) throw new NotFoundException('Doctor not found');
    return doctor;
  }

  async updateProfile(doctorId: string, dto: UpdateDoctorProfileDto) {
    await this.doctorsRepo.update(doctorId, dto);
    return this.getProfile(doctorId);
  }

  async findAll() {
    return this.doctorsRepo.find({
      relations: ['locations'],
    });
  }

  async findById(doctorId: string) {
    const doctor = await this.doctorsRepo.findOne({
      where: { id: doctorId },
      relations: ['locations', 'availabilities', 'availabilities.location'],
    });
    if (!doctor) throw new NotFoundException('Doctor not found');
    return doctor;
  }

  // ── Locations ────────────────────────────────────────

  async createLocation(doctorId: string, dto: CreateLocationDto) {
    const location = this.locationsRepo.create({
      doctorId,
      ...dto,
    });
    return this.locationsRepo.save(location);
  }

  async getLocations(doctorId: string) {
    return this.locationsRepo.find({
      where: { doctorId, isActive: true },
    });
  }

  async updateLocation(
    doctorId: string,
    locationId: string,
    dto: Partial<CreateLocationDto>,
  ) {
    const location = await this.locationsRepo.findOne({
      where: { id: locationId, doctorId },
    });
    if (!location) throw new NotFoundException('Location not found');
    Object.assign(location, dto);
    return this.locationsRepo.save(location);
  }

  async deleteLocation(doctorId: string, locationId: string) {
    const location = await this.locationsRepo.findOne({
      where: { id: locationId, doctorId },
    });
    if (!location) throw new NotFoundException('Location not found');
    location.isActive = false;
    return this.locationsRepo.save(location);
  }

  // ── Availability ─────────────────────────────────────

  async createAvailability(doctorId: string, dto: CreateAvailabilityDto) {
    // Verify location belongs to doctor
    const location = await this.locationsRepo.findOne({
      where: { id: dto.locationId, doctorId },
    });
    if (!location) throw new ForbiddenException('Location does not belong to you');

    const availability = this.availabilityRepo.create({
      doctorId,
      ...dto,
    });
    return this.availabilityRepo.save(availability);
  }

  async getAvailabilities(doctorId: string, locationId?: string) {
    const where: any = { doctorId, isActive: true };
    if (locationId) where.locationId = locationId;
    return this.availabilityRepo.find({
      where,
      relations: ['location'],
      order: { dayOfWeek: 'ASC', startTime: 'ASC' },
    });
  }

  async updateAvailability(
    doctorId: string,
    availabilityId: string,
    dto: Partial<CreateAvailabilityDto>,
  ) {
    const av = await this.availabilityRepo.findOne({
      where: { id: availabilityId, doctorId },
    });
    if (!av) throw new NotFoundException('Availability not found');
    Object.assign(av, dto);
    return this.availabilityRepo.save(av);
  }

  async deleteAvailability(doctorId: string, availabilityId: string) {
    const av = await this.availabilityRepo.findOne({
      where: { id: availabilityId, doctorId },
    });
    if (!av) throw new NotFoundException('Availability not found');
    av.isActive = false;
    return this.availabilityRepo.save(av);
  }

  // ── Unavailability ───────────────────────────────────

  async createUnavailability(doctorId: string, dto: CreateUnavailabilityDto) {
    const unavailability = this.unavailabilityRepo.create({
      doctorId,
      ...dto,
    });
    return this.unavailabilityRepo.save(unavailability);
  }

  async getUnavailabilities(doctorId: string) {
    return this.unavailabilityRepo.find({
      where: { doctorId },
      order: { date: 'ASC' },
    });
  }

  async deleteUnavailability(doctorId: string, unavailabilityId: string) {
    const result = await this.unavailabilityRepo.delete({
      id: unavailabilityId,
      doctorId,
    });
    if (result.affected === 0)
      throw new NotFoundException('Unavailability record not found');
    return { deleted: true };
  }

  // ── Slot Calculation (used by appointment module) ────

  async getAvailableSlots(doctorId: string, locationId: string, date: string) {
    const dayOfWeek = new Date(date).getDay();

    // Get availability sessions for this day
    const availabilities = await this.availabilityRepo.find({
      where: {
        doctorId,
        locationId,
        dayOfWeek,
        isActive: true,
      },
    });

    if (availabilities.length === 0) return [];

    // Check for unavailability
    const unavailabilities = await this.unavailabilityRepo.find({
      where: [
        { doctorId, date, locationId: undefined as any },
        { doctorId, date, locationId },
      ],
    });

    // Full day off
    const fullDayOff = unavailabilities.some(
      (u) => !u.sessionName && !u.locationId,
    );
    if (fullDayOff) return [];

    const slots: { time: string; sessionName: string }[] = [];

    for (const av of availabilities) {
      // Check if this session is unavailable
      const sessionOff = unavailabilities.some(
        (u) => u.sessionName === av.sessionName,
      );
      if (sessionOff) continue;

      // Generate time slots
      const start = this.timeToMinutes(av.startTime);
      const end = this.timeToMinutes(av.endTime);
      const breakStart = av.breakStart
        ? this.timeToMinutes(av.breakStart)
        : null;
      const breakEnd = av.breakEnd ? this.timeToMinutes(av.breakEnd) : null;

      for (let t = start; t < end; t += av.slotDurationMinutes) {
        // Skip break time
        if (breakStart !== null && breakEnd !== null) {
          if (t >= breakStart && t < breakEnd) continue;
        }
        slots.push({
          time: this.minutesToTime(t),
          sessionName: av.sessionName,
        });
      }
    }

    return slots;
  }

  private timeToMinutes(time: string): number {
    const [h, m] = time.split(':').map(Number);
    return h * 60 + m;
  }

  private minutesToTime(minutes: number): string {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
  }
}
