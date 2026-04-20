import api from './axios'

export const getAppointments = (params?: object) => api.get('/appointments', { params })
export const getAppointment = (id: string) => api.get(`/appointments/${id}`)
export const updateAppointmentStatus = (id: string, status: string) =>
  api.patch(`/appointments/${id}/status`, { status })
