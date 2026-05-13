import axios from 'axios'
import { getToken, clearAuth, getRefreshToken, setToken, setRefreshToken } from '../utils/auth'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        clearAuth()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  async error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        const refreshToken = getRefreshToken()
        if (refreshToken && error.config._retry !== true) {
          error.config._retry = true
          try {
            const res = await axios.post('/api/auth/refresh', null, {
              params: { refreshToken }
            })
            if (res.data.code === 200) {
              setToken(res.data.data.token)
              setRefreshToken(res.data.data.refreshToken)
              error.config.headers.Authorization = `Bearer ${res.data.data.token}`
              return request(error.config)
            }
          } catch (e) {
            // refresh failed
          }
        }
        clearAuth()
        router.push('/login')
      }
      ElMessage.error(data?.message || '请求失败')
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
