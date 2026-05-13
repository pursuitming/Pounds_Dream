import request from './request'

export function getNotifications() {
  return request.get('/notification')
}

export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

export function markAsRead(id) {
  return request.put(`/notification/${id}/read`)
}

export function markAllAsRead() {
  return request.put('/notification/read-all')
}
