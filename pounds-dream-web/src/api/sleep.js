import request from './request'
import { getToken } from '../utils/auth'

export function getTodaySleep() {
  return request.get('/sleep/today')
}

export function addSleepRecord(data) {
  return request.post('/sleep', data)
}

export function deleteSleepRecord(id) {
  return request.delete(`/sleep/${id}`)
}

export function updateSleepGoal(goal) {
  return request.put('/sleep/goal', null, { params: { goal } })
}

export function addSleepExp() {
  return request.post('/pet/sleep-exp')
}

export function getSleepTrend(days = 7) {
  return request.get('/sleep/trend', { params: { days } })
}

export function getSleepRegularity() {
  return request.get('/sleep/regularity')
}

export function getSleepRecommendation() {
  return request.get('/sleep/recommendation')
}

export function getSleepAiAdvice() {
  return request.get('/sleep/ai-advice')
}

export function getSleepAiAdviceStream(onThinking, onDone, onError, onContent) {
  const token = getToken() || ''
  const xhr = new XMLHttpRequest()
  xhr.open('GET', '/api/sleep/ai-advice-stream')
  xhr.setRequestHeader('Authorization', `Bearer ${token}`)
  xhr.setRequestHeader('Accept', 'text/event-stream')

  let buffer = ''
  let currentEvent = ''

  xhr.onprogress = () => {
    const text = xhr.responseText
    const newPart = text.substring(buffer.length)
    buffer = text

    const lines = newPart.split('\n')
    for (const line of lines) {
      if (line.startsWith('event: ')) {
        currentEvent = line.substring(7).trim()
      } else if (line.startsWith('data: ')) {
        const data = line.substring(6)
        if (currentEvent === 'thinking') {
          onThinking?.(data)
        } else if (currentEvent === 'content') {
          onContent?.(data)
        } else if (currentEvent === 'done') {
          onDone?.()
          return
        } else if (currentEvent === 'error') {
          onError?.(data)
          return
        }
      }
    }
  }

  xhr.onload = () => {
    onDone?.()
  }

  xhr.onerror = () => {
    onError?.('网络请求失败')
  }

  xhr.send()
}
