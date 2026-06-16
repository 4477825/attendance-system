import request from '@/api/request'

export function getAllStats(params) {
  return request.get('/api/statistics/all', { params })
}

export function getUserMonthlyDetail(params) {
  return request.get('/api/statistics/user/monthly/' + params.month, { params: { userId: params.userId } })
}

export function getDepartments() {
  return request.get('/api/departments')
}
