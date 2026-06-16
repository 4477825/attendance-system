import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setUser, getUser, removeToken, removeUser } from '@/utils/auth'
import { login as loginApi, logout as logoutApi, getProfile } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const user = ref(getUser() || null)
  const roles = ref([])
  const permissions = ref([])

  const isAuthenticated = computed(() => !!token.value)

  async function login(loginForm) {
    const result = await loginApi(loginForm)
    token.value = result.data.token
    user.value = result.data.user
    setUser(result.data.user)
    // Parse roles
    if (result.data.user.role === 'ROLE_ADMIN') {
      roles.value = ['ADMIN']
    } else {
      roles.value = ['EMPLOYEE']
    }
    return result
  }

  async function getInfo() {
    const result = await getProfile()
    user.value = result.data
    setUser(result.data)
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      roles.value = []
      user.value = null
      removeToken()
      removeUser()
    }
  }

  return {
    token,
    user,
    roles,
    permissions,
    isAuthenticated,
    login,
    getInfo,
    logout,
  }
})
