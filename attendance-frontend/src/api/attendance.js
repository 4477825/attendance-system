import request from '@/api/request'

export function checkIn() {
  return request.post('/api/attendance/check-in')
}

export function checkOut() {
  return request.post('/api/attendance/check-out')
}

export function getRecords(params) {
  return request.get('/api/attendance/records', { params })
}

export function getTodayRecord() {
  return request.get('/api/attendance/today')
}

export function getMyMonthSummary(month) {
  return request.get(`/api/attendance/my-monthly/${month}`)
}
