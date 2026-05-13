<template>
  <div class="login-container">
    <!-- 装饰性图形 -->
    <div class="decoration dumbbell-1"></div>
    <div class="decoration dumbbell-2"></div>
    <div class="decoration heart-1"></div>
    <div class="decoration heart-2"></div>
    <div class="decoration runner"></div>

    <div class="login-card">
      <h2>Pounds-Dream</h2>
      <p class="subtitle">减肥减脂平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="links">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { setToken, setRefreshToken, setUser } from '../utils/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    setToken(res.data.token)
    setRefreshToken(res.data.refreshToken)
    setUser(res.data.user)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5e6f0 0%, #e8e0f5 50%, #f0e6ef 100%);
  position: relative;
  overflow: hidden;

  .login-card {
    width: 400px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(180, 160, 200, 0.2);
    backdrop-filter: blur(10px);
    position: relative;
    z-index: 1;

    h2 {
      text-align: center;
      font-size: 28px;
      color: #6b5b7b;
      margin: 0;
    }

    .subtitle {
      text-align: center;
      color: #9b8aad;
      margin: 8px 0 30px;
    }

    .links {
      text-align: center;
      margin-top: 16px;

      a {
        color: #b8a0c8;
        text-decoration: none;
        font-size: 14px;

        &:hover {
          color: #9b7fe6;
        }
      }
    }
  }
}

// 装饰性图形元素
.decoration {
  position: absolute;
  opacity: 0.12;
  z-index: 0;
}

// 哑铃图形
.dumbbell-1 {
  width: 120px;
  height: 40px;
  top: 15%;
  left: 10%;
  background: #c8a8c8;
  border-radius: 20px;
  box-shadow:
    -30px 0 0 0 #c8a8c8,
    30px 0 0 0 #c8a8c8;
  animation: float 6s ease-in-out infinite;
}

.dumbbell-2 {
  width: 100px;
  height: 35px;
  bottom: 20%;
  right: 12%;
  background: #b8a0d0;
  border-radius: 18px;
  box-shadow:
    -25px 0 0 0 #b8a0d0,
    25px 0 0 0 #b8a0d0;
  transform: rotate(-15deg);
  animation: float 8s ease-in-out infinite reverse;
}

// 心形图形（使用 CSS 创建）
.heart-1 {
  width: 30px;
  height: 30px;
  top: 25%;
  right: 20%;
  background: #d4a8d4;
  transform: rotate(45deg);
  animation: pulse 4s ease-in-out infinite;

  &::before,
  &::after {
    content: '';
    position: absolute;
    width: 30px;
    height: 30px;
    background: #d4a8d4;
    border-radius: 50%;
  }

  &::before {
    top: -15px;
    left: 0;
  }

  &::after {
    top: 0;
    left: -15px;
  }
}

.heart-2 {
  width: 25px;
  height: 25px;
  bottom: 30%;
  left: 18%;
  background: #c8b0e0;
  transform: rotate(45deg);
  animation: pulse 5s ease-in-out infinite reverse;

  &::before,
  &::after {
    content: '';
    position: absolute;
    width: 25px;
    height: 25px;
    background: #c8b0e0;
    border-radius: 50%;
  }

  &::before {
    top: -12px;
    left: 0;
  }

  &::after {
    top: 0;
    left: -12px;
  }
}

// 跑步人形（简化版）
.runner {
  width: 40px;
  height: 60px;
  top: 60%;
  left: 8%;
  animation: run 3s ease-in-out infinite;

  &::before {
    content: '';
    position: absolute;
    width: 20px;
    height: 20px;
    background: #b8a0c8;
    border-radius: 50%;
    top: 0;
    left: 10px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 12px;
    height: 35px;
    background: #b8a0c8;
    border-radius: 6px;
    top: 20px;
    left: 14px;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: rotate(45deg) scale(1);
    opacity: 0.12;
  }
  50% {
    transform: rotate(45deg) scale(1.1);
    opacity: 0.18;
  }
}

@keyframes run {
  0%, 100% {
    transform: translateX(0) rotate(0deg);
  }
  25% {
    transform: translateX(10px) rotate(5deg);
  }
  75% {
    transform: translateX(-10px) rotate(-5deg);
  }
}
</style>
