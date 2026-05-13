import request from './request'

export function addOrUpdateWeight(data) {
  return request.post('/weight', data)
}

export function getWeightTrend(days = 30) {
  return request.get('/weight/trend', { params: { days } })
}

export function getWeightHistory(params) {
  return request.get('/weight/history', { params })
}

export function deleteWeight(id) {
  return request.delete(`/weight/${id}`)
}

export function addBodyMeasurement(data) {
  return request.post('/weight/measurement', data)
}

export function getBodyMeasurements(params) {
  return request.get('/weight/measurements', { params })
}
