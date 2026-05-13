import request from './request'

export function getRankInfo() {
  return request.get('/rank')
}

export function checkIn() {
  return request.post('/rank/check-in')
}
