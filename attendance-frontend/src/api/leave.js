import request from '@/api/request'

export function applyLeave(data) {
  return request.post('/api/leave/apply', data)
}

export function getLeaveList(params) {
  return request.get('/api/leave/list', { params })
}

export function getLeaveById(id) {
  return request.get(`/api/leave/${id}`)
}

export function approveLeave(id, data) {
  return request.put(`/api/leave/approve/${id}`, null, { params: data })
}
