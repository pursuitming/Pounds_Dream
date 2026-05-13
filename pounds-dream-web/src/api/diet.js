import request from './request'

export function addDietRecord(data) {
  return request.post('/diet', data)
}

export function getTodayDiet() {
  return request.get('/diet/today')
}

export function getDailyDiet(date) {
  return request.get('/diet/daily', { params: { date } })
}

export function deleteDietRecord(id) {
  return request.delete(`/diet/${id}`)
}

export function searchFood(keyword) {
  return request.get('/diet/food/search', { params: { keyword } })
}

export function addCustomFood(data) {
  return request.post('/diet/food', data)
}

export function getAlternatives(foodId, limit = 5) {
  return request.get('/diet/food/alternatives', { params: { foodId, limit } })
}

export function getRecommendations() {
  return request.get('/diet/recommend')
}

export function getDietTemplates() {
  return request.get('/diet/templates')
}

export function saveDietTemplate(data) {
  return request.post('/diet/templates', data)
}

export function deleteDietTemplate(id) {
  return request.delete(`/diet/templates/${id}`)
}

export function applyDietTemplate(templateId, date) {
  return request.post(`/diet/templates/${templateId}/apply`, { date })
}

export function skipMeal(mealType, date) {
  return request.post('/diet/skip-meal', null, { params: { mealType, date } })
}
