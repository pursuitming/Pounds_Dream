import request from './request'

export function getPetInfo() {
  return request.get('/pet')
}

export function renamePet(name) {
  return request.put('/pet/name', null, { params: { name } })
}

export function interactPet(action) {
  return request.post('/pet/interact', null, { params: { action } })
}

export function changePetType(petType) {
  return request.put('/pet/type', null, { params: { petType } })
}

export function getPetLogs() {
  return request.get('/pet/logs')
}
