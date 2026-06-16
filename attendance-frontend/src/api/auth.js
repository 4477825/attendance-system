import request from '@/api/request'

export function login(data) {
  return request.post('/api/auth/login', data)
}

export function register(data) {
  return request.post('/api/auth/register', data)
}

export function logout() {
  return request.post('/api/users/logout')
}

export function getProfile() {
  return request.get('/api/users/profile')
}

export function updateProfile(data) {
  return request.put('/api/users/profile', data)
}
