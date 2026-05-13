import request from './request'

export function getMenstrualRecords() {
  return request.get('/menstrual')
}

export function getMenstrualStats() {
  return request.get('/menstrual/stats')
}

export function addMenstrualRecord(data) {
  return request.post('/menstrual', data)
}

export function updateMenstrualRecord(data) {
  return request.put('/menstrual', data)
}

export function deleteMenstrualRecord(id) {
  return request.delete(`/menstrual/${id}`)
}
