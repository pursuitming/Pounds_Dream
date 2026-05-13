import request from './request'

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function createPost(data) {
  return request.post('/community/posts', data)
}

export function getPosts(params) {
  return request.get('/community/posts', { params })
}

export function getPostDetail(id) {
  return request.get(`/community/posts/${id}`)
}

export function addComment(data) {
  return request.post('/community/comments', data)
}

export function getComments(postId, params) {
  return request.get(`/community/posts/${postId}/comments`, { params })
}

export function toggleLike(targetId, targetType) {
  return request.post('/community/like', null, { params: { targetId, targetType } })
}
