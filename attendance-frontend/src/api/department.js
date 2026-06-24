import request from '@/api/request'

export function getDepartmentList() {
  return request.get('/api/departments')
}

export function createDepartment(data) {
  return request.post('/api/departments', data)
}

export function updateDepartment(id, data) {
  return request.put(`/api/departments/${id}`, data)
}

export function deleteDepartment(id) {
  return request.delete(`/api/departments/${id}`)
}
