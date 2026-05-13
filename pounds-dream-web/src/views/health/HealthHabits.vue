<template>
  <div class="health-habits">
    <div class="page-header">
      <h2>我的健康习惯</h2>
      <p class="subtitle">记录每一个健康小习惯，让生活更美好</p>
    </div>

    <div class="habit-grid">
      <!-- 经期记录卡片 -->
      <div class="habit-card" @click="goToMenstrual" v-if="isFemale">
        <div class="card-icon" style="background: linear-gradient(135deg, #ff6b9d, #ff8fb1)">
          <el-icon size="32"><Female /></el-icon>
        </div>
        <div class="card-info">
          <h3>经期记录</h3>
          <p class="card-desc">记录月经周期，预测下次经期</p>
          <div class="card-stats" v-if="menstrualStats">
            <span class="stat-label">当前阶段</span>
            <span class="stat-value">{{ menstrualStats.currentCycle?.currentPhase || '未记录' }}</span>
          </div>
          <div class="card-stats" v-else>
            <span class="stat-label">状态</span>
            <span class="stat-value">点击开始记录</span>
          </div>
        </div>
        <el-icon class="card-arrow"><ArrowRight /></el-icon>
      </div>

      <!-- 饮水记录卡片 -->
      <div class="habit-card" @click="goToWater">
        <div class="card-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
          <el-icon size="32"><Coffee /></el-icon>
        </div>
        <div class="card-info">
          <h3>饮水记录</h3>
          <p class="card-desc">每日饮水打卡，保持水分平衡</p>
          <div class="card-stats" v-if="waterData">
            <span class="stat-label">今日</span>
            <span class="stat-value" style="color: #4facfe">{{ waterData.todayAmount || 0 }} / {{ waterData.dailyGoal || 2000 }} ml</span>
          </div>
          <div class="card-stats" v-else>
            <span class="stat-label">状态</span>
            <span class="stat-value">点击开始记录</span>
          </div>
        </div>
        <el-icon class="card-arrow"><ArrowRight /></el-icon>
      </div>

      <!-- 睡眠记录卡片 -->
      <div class="habit-card" @click="goToSleep">
        <div class="card-icon" style="background: linear-gradient(135deg, #a18cd1, #fbc2eb)">
          <el-icon size="32"><Moon /></el-icon>
        </div>
        <div class="card-info">
          <h3>睡眠记录</h3>
          <p class="card-desc">追踪睡眠质量，改善作息习惯</p>
          <div class="card-stats" v-if="sleepData">
            <span class="stat-label">今日</span>
            <span class="stat-value" style="color: #a18cd1">{{ formatSleepDuration(sleepData.lastDuration) }}</span>
          </div>
          <div class="card-stats" v-else>
            <span class="stat-label">状态</span>
            <span class="stat-value">点击开始记录</span>
          </div>
        </div>
        <el-icon class="card-arrow"><ArrowRight /></el-icon>
      </div>

      <!-- 冥想记录卡片 -->
      <div class="habit-card disabled">
        <div class="card-icon" style="background: linear-gradient(135deg, #89f7fe, #66a6ff)">
          <el-icon size="32"><Sunny /></el-icon>
        </div>
        <div class="card-info">
          <h3>冥想记录</h3>
          <p class="card-desc">每日冥想放松，提升专注力</p>
          <div class="card-stats">
            <span class="stat-label">状态</span>
            <span class="stat-value coming-soon">即将上线</span>
          </div>
        </div>
        <el-icon class="card-arrow"><ArrowRight /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/modules/user'
import { getMenstrualStats } from '../../api/menstrual'
import { getTodayWater } from '../../api/water'
import { getTodaySleep } from '../../api/sleep'
import { Female, ArrowRight, Coffee, Moon, Sunny } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const menstrualStats = ref(null)
const waterData = ref(null)
const sleepData = ref(null)

const isFemale = computed(() => {
  return userStore.userInfo?.gender === 2
})

function goToMenstrual() {
  router.push('/health-habits/menstrual')
}

function goToWater() {
  router.push('/health-habits/water')
}

function goToSleep() {
  router.push('/health-habits/sleep')
}

function formatSleepDuration(minutes) {
  if (!minutes || minutes <= 0) return '未记录'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0) return `${m}分钟`
  if (m === 0) return `${h}小时`
  return `${h}小时${m}分`
}

onMounted(async () => {
  // 加载饮水数据
  try {
    const waterRes = await getTodayWater()
    waterData.value = waterRes.data
  } catch (e) {
    // ignore
  }

  // 加载睡眠数据
  try {
    const sleepRes = await getTodaySleep()
    sleepData.value = sleepRes.data
  } catch (e) {
    // ignore
  }

  if (isFemale.value) {
    try {
      const res = await getMenstrualStats()
      menstrualStats.value = res.data
    } catch (e) {
      // ignore
    }
  }
})
</script>

<style scoped lang="scss">
.health-habits {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;

  h2 {
    font-size: 24px;
    color: #3d2b4a;
    margin: 0 0 8px;
  }

  .subtitle {
    font-size: 14px;
    color: #9b8aad;
    margin: 0;
  }
}

.habit-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.habit-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    border-color: rgba(255, 107, 157, 0.3);
  }

  &.disabled {
    opacity: 0.6;
    cursor: not-allowed;

    &:hover {
      transform: none;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
      border-color: transparent;
    }
  }

  .card-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  .card-info {
    flex: 1;

    h3 {
      font-size: 18px;
      color: #3d2b4a;
      margin: 0 0 6px;
    }

    .card-desc {
      font-size: 13px;
      color: #9b8aad;
      margin: 0 0 12px;
    }

    .card-stats {
      display: flex;
      align-items: center;
      gap: 8px;

      .stat-label {
        font-size: 12px;
        color: #b8a0c8;
      }

      .stat-value {
        font-size: 14px;
        font-weight: 600;
        color: #ff6b9d;

        &.coming-soon {
          color: #b8a0c8;
          font-weight: normal;
        }
      }
    }
  }

  .card-arrow {
    color: #d4c5e0;
    font-size: 20px;
  }
}
</style>
