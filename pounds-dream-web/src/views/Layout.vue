<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <h2>Pounds-Dream</h2>
        <p>减肥减脂平台</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="side-menu"
        background-color="#3d2b4a"
        text-color="#d4c5e0"
        active-text-color="#ff6b9d"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/weight">
          <el-icon><TrendCharts /></el-icon>
          <span>体重记录</span>
        </el-menu-item>
        <el-menu-item index="/diet">
          <el-icon><Food /></el-icon>
          <span>饮食记录</span>
        </el-menu-item>
        <el-menu-item index="/exercise">
          <el-icon><Bicycle /></el-icon>
          <span>运动记录</span>
        </el-menu-item>
        <el-menu-item index="/training-plan">
          <el-icon><Promotion /></el-icon>
          <span>训练计划</span>
        </el-menu-item>
        <el-menu-item index="/ai-coach">
          <el-icon><ChatLineSquare /></el-icon>
          <span>AI 健康教练</span>
        </el-menu-item>
        <el-menu-item index="/health-habits">
          <el-icon><FirstAidKit /></el-icon>
          <span>健康习惯</span>
        </el-menu-item>
        <el-menu-item index="/pet">
          <el-icon><MagicStick /></el-icon>
          <span>我的宠物</span>
        </el-menu-item>
        <el-menu-item index="/community">
          <el-icon><ChatDotRound /></el-icon>
          <span>社区</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h3>{{ currentTitle }}</h3>
        </div>
        <div class="header-right">
          <NotificationBell />
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo?.avatar">
                {{ userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/modules/user'
import NotificationBell from '../components/NotificationBell.vue'
import {
  DataBoard, TrendCharts, Food, Bicycle,
  ChatDotRound, User, Promotion, ChatLineSquare, MagicStick, FirstAidKit
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '仪表盘')
const userInfo = computed(() => userStore.userInfo)

onMounted(async () => {
  try {
    await userStore.fetchUserInfo()
  } catch (e) {
    // ignore
  }
})

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #3d2b4a;
  overflow: hidden;

  .logo {
    height: 80px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #fff;

    h2 {
      font-size: 20px;
      margin: 0;
    }

    p {
      font-size: 12px;
      color: #d4c5e0;
      margin: 4px 0 0;
    }
  }

  .side-menu {
    border-right: none;
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #edd6e6;
  padding: 0 20px;

  .header-left h3 {
    margin: 0;
    font-size: 18px;
    color: #3d2b4a;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      gap: 8px;

      .username {
        font-size: 14px;
        color: #6b5a7a;
      }
    }
  }
}

.main {
  background-color: #fef6f9;
  padding: 20px;
}
</style>
