import api from './axios'

export const getPatients = (params?: object) => api.get('/patients', { params })
export const getPatient = (id: string) => api.get(`/patients/${id}`)
export const createPatient = (data: object) => api.post('/patients', data)
export const updatePatient = (id: string, data: object) => api.patch(`/patients/${id}`, data)
export const deletePatient = (id: string) => api.delete(`/patients/${id}`)
