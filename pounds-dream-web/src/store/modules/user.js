import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProfile, getHealthProfile } from '../../api/user'
import { getUser, setUser, clearAuth } from '../../utils/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(getUser())
  const healthProfile = ref(null)

  async function fetchUserInfo() {
    try {
      const res = await getProfile()
      userInfo.value = res.data
      setUser(res.data)
      return res.data
    } catch (error) {
      throw error
    }
  }

  async function fetchHealthProfile() {
    try {
      const res = await getHealthProfile()
      healthProfile.value = res.data
      return res.data
    } catch (error) {
      throw error
    }
  }

  function logout() {
    userInfo.value = null
    healthProfile.value = null
    clearAuth()
  }

  return {
    userInfo,
    healthProfile,
    fetchUserInfo,
    fetchHealthProfile,
    logout
  }
})
