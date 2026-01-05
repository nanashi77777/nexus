import request from '@/utils/request'

export interface LoginRequest {
  account: string
  password: string
}

export interface LoginResponse {
  code: number | string
  message: string
  data: {
    tokenValue?: string
    token?: string
    [key: string]: any
  }
}

export const login = (data: LoginRequest): Promise<LoginResponse> => {
  return request.post('/api/v1/user/login', data)
}

export const logout = () => {
  return request.post('/api/v1/user/logout')
}

export const getUserInfo = () => {
  return request.get('/api/v1/user/info')
}
