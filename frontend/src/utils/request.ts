import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = config.headers || {}
      config.headers['auth_token'] = token
    }
    return config
  },
  (error: any) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // 如果返回的状态码不是200或者不是200000，则认为是错误
    if (res.code !== 200 && res.code !== '200000') {
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error: any) => {
    return Promise.reject(error)
  }
)

export default request
