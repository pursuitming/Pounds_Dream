import request from './request'

export function getTodayWater() {
  return request.get('/water/today')
}

export function addWaterRecord(data) {
  return request.post('/water', data)
}

export function deleteWaterRecord(id) {
  return request.delete(`/water/${id}`)
}

export function updateWaterGoal(goal) {
  return request.put('/water/goal', null, { params: { goal } })
}

export function getWaterTypeStats(days = 7) {
  return request.get('/water/type-stats', { params: { days } })
}

export function getWaterTrend(days = 7) {
  return request.get('/water/trend', { params: { days } })
}

export function addWaterExp() {
  return request.post('/pet/water-exp')
}
