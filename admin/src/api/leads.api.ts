import api from './axios'

export const getLeads = (params?: object) => api.get('/leads', { params })
export const getLead = (id: string) => api.get(`/leads/${id}`)
export const createLead = (data: object) => api.post('/leads', data)
export const updateLead = (id: string, data: object) => api.patch(`/leads/${id}`, data)
export const deleteLead = (id: string) => api.delete(`/leads/${id}`)
