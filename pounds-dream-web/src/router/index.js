import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'weight',
        name: 'Weight',
        component: () => import('../views/weight/WeightRecord.vue'),
        meta: { title: '体重记录' }
      },
      {
        path: 'diet',
        name: 'Diet',
        component: () => import('../views/diet/DietRecord.vue'),
        meta: { title: '饮食记录' }
      },
      {
        path: 'exercise',
        name: 'Exercise',
        component: () => import('../views/exercise/ExerciseRecord.vue'),
        meta: { title: '运动记录' }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('../views/community/PostList.vue'),
        meta: { title: '社区' }
      },
      {
        path: 'community/post/:id',
        name: 'PostDetail',
        component: () => import('../views/community/PostDetail.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: 'community/publish',
        name: 'PostPublish',
        component: () => import('../views/community/PostPublish.vue'),
        meta: { title: '发布帖子' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'training-plan',
        name: 'TrainingPlan',
        component: () => import('../views/training/TrainingPlan.vue'),
        meta: { title: '训练计划' }
      },
      {
        path: 'ai-coach',
        name: 'AICoach',
        component: () => import('../views/ai/AICoach.vue'),
        meta: { title: 'AI 健康教练' }
      },
      {
        path: 'pet',
        name: 'Pet',
        component: () => import('../views/pet/PetPage.vue'),
        meta: { title: '我的宠物' }
      },
      {
        path: 'health-habits',
        name: 'HealthHabits',
        component: () => import('../views/health/HealthHabits.vue'),
        meta: { title: '健康习惯' }
      },
      {
        path: 'health-habits/menstrual',
        name: 'MenstrualCycle',
        component: () => import('../views/health/MenstrualCycle.vue'),
        meta: { title: '经期记录' }
      },
      {
        path: 'health-habits/water',
        name: 'WaterIntake',
        component: () => import('../views/health/WaterIntake.vue'),
        meta: { title: '饮水记录' }
      },
      {
        path: 'health-habits/sleep',
        name: 'SleepRecord',
        component: () => import('../views/health/SleepRecord.vue'),
        meta: { title: '睡眠记录' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
