import api from './axios'

export const getDoctors = (params?: object) => api.get('/doctors', { params })
export const getDoctor = (id: string) => api.get(`/doctors/${id}`)
export const createDoctor = (data: object) => api.post('/doctors', data)
export const updateDoctor = (id: string, data: object) => api.patch(`/doctors/${id}`, data)
export const deleteDoctor = (id: string) => api.delete(`/doctors/${id}`)
