export enum UserRole {
  PATIENT = 'patient',
  DOCTOR = 'doctor',
  SUPER_ADMIN = 'super_admin',
}

export enum LeadSource {
  WEBSITE = 'website',
  REFERRAL = 'referral',
  SOCIAL_MEDIA = 'social_media',
  WALK_IN = 'walk_in',
  PHONE_CALL = 'phone_call',
  OTHER = 'other',
}

export enum LeadStatus {
  NEW = 'new',
  CONTACTED = 'contacted',
  QUALIFIED = 'qualified',
  CONVERTED = 'converted',
  LOST = 'lost',
}

export enum Gender {
  MALE = 'male',
  FEMALE = 'female',
  OTHER = 'other',
}

export enum BloodGroup {
  A_POSITIVE = 'A+',
  A_NEGATIVE = 'A-',
  B_POSITIVE = 'B+',
  B_NEGATIVE = 'B-',
  AB_POSITIVE = 'AB+',
  AB_NEGATIVE = 'AB-',
  O_POSITIVE = 'O+',
  O_NEGATIVE = 'O-',
}

export enum AppointmentStatus {
  BOOKED = 'booked',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled',
  NO_SHOW = 'no_show',
}

export enum TokenStatus {
  WAITING = 'waiting',
  SERVING = 'serving',
  COMPLETED = 'completed',
  SKIPPED = 'skipped',
}

export enum UploadType {
  REPORT = 'report',
  PRESCRIPTION = 'prescription',
}

export enum DayOfWeek {
  MONDAY = 1,
  TUESDAY = 2,
  WEDNESDAY = 3,
  THURSDAY = 4,
  FRIDAY = 5,
  SATURDAY = 6,
  SUNDAY = 0,
}

/** System of medicine shown on doctor profile. */
export enum MedicalSystem {
  HOMEOPATHY = 'homeopathy',
  ALLOPATHY = 'allopathy',
  AYUSH = 'ayush',
}
