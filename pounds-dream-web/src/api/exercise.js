import request from './request'

export function addExerciseRecord(data) {
  return request.post('/exercise', data)
}

export function getTodayExercise() {
  return request.get('/exercise/today')
}

export function getDailyExercise(date) {
  return request.get('/exercise/daily', { params: { date } })
}

export function deleteExerciseRecord(id) {
  return request.delete(`/exercise/${id}`)
}

export function getExerciseTypes() {
  return request.get('/exercise/types')
}
