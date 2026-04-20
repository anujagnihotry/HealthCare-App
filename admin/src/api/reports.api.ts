import api from './axios'

export const getUploads = (params?: object) => api.get('/reports/uploads', { params })
export const getConsultations = (params?: object) => api.get('/reports/consultations', { params })
export const getMedicalRecords = (params?: object) => api.get('/reports/medical-records', { params })
