import request from '@/api/request'
import { getToken } from '@/utils/auth'

export function applyOvertime(data) {
  return request.post('/api/overtime/apply', data)
}

export function getOvertimeList(params) {
  return request.get('/api/overtime/list', { params })
}

export function approveOvertime(id, data) {
  return request.put(`/api/overtime/approve/${id}`, null, { params: data })
}

export function exportOvertime() {
  const token = getToken()
  return fetch('/api/export/overtime', {
    headers: { 'Authorization': `Bearer ${token}` },
  })
}
