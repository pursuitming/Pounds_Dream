import request from './request'

export function getTrainingPlans() {
  return request.get('/training-plan')
}

export function getTrainingPlanDetail(id) {
  return request.get(`/training-plan/${id}`)
}

export function createTrainingPlan(data) {
  return request.post('/training-plan', data)
}

export function updateTrainingPlan(id, data) {
  return request.put(`/training-plan/${id}`, data)
}

export function deleteTrainingPlan(id) {
  return request.delete(`/training-plan/${id}`)
}

export function getTodayPlan() {
  return request.get('/training-plan/today')
}

export function getRecommendedTraining(intensity = 'MEDIUM') {
  return request.get('/training-plan/recommend', { params: { intensity } })
}
