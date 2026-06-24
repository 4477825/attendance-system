import request from '@/api/request'

export function getUserList() {
  return request.get('/api/users')
}

export function createUser(data) {
  return request.post('/api/users', data)
}

export function updateUser(id, data) {
  return request.put(`/api/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/api/users/${id}`)
}

export function toggleUserStatus(id) {
  return request.put(`/api/users/${id}/status`)
}
