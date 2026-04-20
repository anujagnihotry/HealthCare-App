import api from './axios'

export const loginApi = (email: string, password: string) =>
  api.post('/auth/login', { email, password })
