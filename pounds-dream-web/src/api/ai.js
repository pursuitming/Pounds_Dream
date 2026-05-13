import { getToken } from '../utils/auth'
import request from './request'

export function chatWithAI(message, conversationId) {
  return fetch('/api/ai/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    },
    body: JSON.stringify({ message, conversationId })
  })
}

export function getConversations() {
  return request.get('/ai/conversations')
}

export function getMessages(conversationId) {
  return request.get(`/ai/conversations/${conversationId}/messages`)
}

export function deleteConversation(conversationId) {
  return request.delete(`/ai/conversations/${conversationId}`)
}
