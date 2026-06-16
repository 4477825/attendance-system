import request from '@/api/request'

export function applyOvertime(data) {
  return request.post('/api/overtime/apply', data)
}

export function getOvertimeList(params) {
  return request.get('/api/overtime/list', { params })
}

export function approveOvertime(id, data) {
  return request.put(`/api/overtime/approve/${id}`, null, { params: data })
}
