import api from './axios'

export const getOverview = () => api.get('/analytics/overview')
export const getDoctorsOnboarded = (params?: object) => api.get('/analytics/doctors-onboarded', { params })
export const getPatientsOnboarded = (params?: object) => api.get('/analytics/patients-onboarded', { params })
export const getAppointmentsAnalytics = (params?: object) => api.get('/analytics/appointments', { params })
export const getAppointmentsByDoctor = (params?: object) => api.get('/analytics/appointments-by-doctor', { params })
