import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<any>(null)

  const login = async (account: string, password: string) => {
    try {
      const response = await apiLogin({ account, password })
      if (response.code === 200 || response.code === '200000') {
        const tokenValue = response.data.tokenValue || response.data.token || ''
        token.value = tokenValue
        localStorage.setItem('token', tokenValue)
        return response
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
  }

  const isAuthenticated = () => {
    return !!token.value
  }

  return {
    token,
    userInfo,
    login,
    logout,
    isAuthenticated
  }
})
