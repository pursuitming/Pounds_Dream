<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #ff6b9d">
            <el-icon size="28"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <p class="label">今日体重</p>
            <h2>{{ dashboard.todayWeight ? dashboard.todayWeight + ' kg' : '--' }}</h2>
            <p class="sub">
              {{ dashboard.weightChange ? (dashboard.weightChange > 0 ? '+' : '') + dashboard.weightChange + ' kg' : '&nbsp;' }}
            </p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #9b7fe6">
            <el-icon size="28"><Food /></el-icon>
          </div>
          <div class="stat-info">
            <p class="label">今日摄入</p>
            <h2>{{ dashboard.todayCalorieIntake || 0 }} kcal</h2>
            <p class="sub">目标 {{ dashboard.targetCalorie || 1800 }} kcal</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #ffb347">
            <el-icon size="28"><Bicycle /></el-icon>
          </div>
          <div class="stat-info">
            <p class="label">运动消耗</p>
            <h2>{{ dashboard.todayCalorieBurned || 0 }} kcal</h2>
            <p class="sub">&nbsp;</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #ff6b8a">
            <el-icon size="28"><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <p class="label">连续打卡</p>
            <h2>{{ dashboard.streakDays || 0 }} 天</h2>
            <p class="sub">&nbsp;</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 段位卡片 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <RankCard />
      </el-col>
    </el-row>

    <!-- 数字孪生宠物 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <PetCard />
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <div class="card body-card">
          <div class="body-card-content">
            <div class="body-avatar-section">
              <BodyAvatar3D
                :bmi="dashboard.bmi"
                :weight="dashboard.todayWeight"
                :height="dashboard.height"
                :gender="dashboard.gender"
                :chest="dashboard.chest"
                :waist="dashboard.waist"
                :hip="dashboard.hip"
              />
            </div>
            <div class="body-stats-section">
              <h3 class="card-title">我的体型</h3>
              <div class="body-stats">
                <div class="stat-row">
                  <span class="stat-label">身高</span>
                  <span class="stat-value">{{ dashboard.height || '--' }} cm</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">体重</span>
                  <span class="stat-value">{{ dashboard.todayWeight || '--' }} kg</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">BMI</span>
                  <span class="stat-value" :style="{ color: bmiColor }">{{ dashboard.bmi || '--' }}</span>
                </div>
                <div class="stat-row" v-if="dashboard.chest">
                  <span class="stat-label">胸围</span>
                  <span class="stat-value">{{ dashboard.chest }} cm</span>
                </div>
                <div class="stat-row" v-if="dashboard.waist">
                  <span class="stat-label">腰围</span>
                  <span class="stat-value">{{ dashboard.waist }} cm</span>
                </div>
                <div class="stat-row" v-if="dashboard.hip">
                  <span class="stat-label">臀围</span>
                  <span class="stat-value">{{ dashboard.hip }} cm</span>
                </div>
              </div>
              <div class="body-tip">
                ✨ 点击人物互动
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px" class="equal-height-row">
      <!-- 今日热量 - 正方形 -->
      <el-col :span="6">
        <div class="card calorie-card">
          <h3 class="card-title">今日热量</h3>
          <div ref="calorieChartRef" class="calorie-chart"></div>
          <div class="calorie-info">
            <span class="calorie-intake">{{ dashboard.todayCalorieIntake || 0 }}</span>
            <span class="calorie-label">已摄入 kcal</span>
          </div>
        </div>
      </el-col>

      <!-- 今日各餐热量 -->
      <el-col :span="10">
        <div class="card equal-height-card">
          <h3 class="card-title">今日各餐热量</h3>
          <div class="meal-bars">
            <div class="meal-item" v-for="(cal, key) in dashboard.todayMeals" :key="key">
              <span class="meal-name">{{ mealNames[key] }}</span>
              <el-progress
                :percentage="Math.min(100, Math.round((cal / (dashboard.targetCalorie / 4)) * 100))"
                :color="getMealColor(key)"
              />
              <span class="meal-cal">{{ cal }} kcal</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- BMI 指数 -->
      <el-col :span="8">
        <div class="card equal-height-card">
          <h3 class="card-title">BMI 指数</h3>
          <div class="bmi-display">
            <div class="bmi-value" :style="{ color: bmiColor }">
              {{ dashboard.bmi || '--' }}
            </div>
            <div class="bmi-category" :style="{ color: bmiColor }">
              {{ bmiCategory }}
            </div>
            <el-slider
              v-model="bmiSlider"
              :min="15"
              :max="35"
              :step="0.1"
              disabled
              :show-tooltip="false"
            />
            <div class="bmi-labels">
              <span>偏瘦</span>
              <span>正常</span>
              <span>偏胖</span>
              <span>肥胖</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '../../api/dashboard'
import { TrendCharts, Food, Bicycle, Trophy } from '@element-plus/icons-vue'
import { getBMICategory, getBMIColor } from '../../utils/format'
import RankCard from '../../components/RankCard.vue'
import PetCard from '../../components/PetCard.vue'
import BodyAvatar3D from '../../components/BodyAvatar3D.vue'

const dashboard = ref({
  todayWeight: null,
  weightChange: null,
  targetWeight: null,
  bmi: null,
  todayCalorieIntake: 0,
  todayCalorieBurned: 0,
  targetCalorie: 1800,
  streakDays: 0,
  weekWeightTrend: [],
  todayMeals: { breakfast: 0, lunch: 0, dinner: 0, snack: 0 },
  height: null,
  chest: null,
  waist: null,
  hip: null
})

const calorieChartRef = ref(null)
let calorieChart = null

const mealNames = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }

const bmiSlider = computed(() => {
  return dashboard.value.bmi ? Number(dashboard.value.bmi) : 22
})

const bmiCategory = computed(() => getBMICategory(dashboard.value.bmi))
const bmiColor = computed(() => getBMIColor(dashboard.value.bmi))

function getMealColor(key) {
  const colors = { breakfast: '#ff6b9d', lunch: '#9b7fe6', dinner: '#ffb347', snack: '#b8a9c9' }
  return colors[key] || '#b8a9c9'
}

function initCalorieChart() {
  if (!calorieChartRef.value) return
  calorieChart = echarts.init(calorieChartRef.value)

  const intake = dashboard.value.todayCalorieIntake || 0
  const target = dashboard.value.targetCalorie || 1800
  const remaining = Math.max(0, target - intake)
  const percentage = target > 0 ? Math.round((intake / target) * 100) : 0

  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} kcal ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['55%', '75%'],
      avoidLabelOverlap: false,
      label: {
        show: true,
        position: 'center',
        formatter: `${percentage}%`,
        fontSize: 24,
        fontWeight: 'bold',
        color: '#ff6b9d'
      },
      data: [
        { value: intake, name: '已摄入', itemStyle: { color: '#ff6b9d' } },
        { value: remaining, name: '剩余', itemStyle: { color: '#f3e4ee' } }
      ]
    }]
  }

  calorieChart.setOption(option)
}

onMounted(async () => {
  try {
    const res = await getDashboard()
    dashboard.value = res.data
    await nextTick()
    initCalorieChart()
  } catch (e) {
    // ignore
  }

  window.addEventListener('resize', () => {
    calorieChart?.resize()
  })
})
</script>

<style scoped lang="scss">
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .stat-info {
    .label {
      font-size: 13px;
      color: #9b8aad;
      margin: 0;
    }

    h2 {
      font-size: 24px;
      margin: 4px 0;
      color: #3d2b4a;
    }

    .sub {
      font-size: 12px;
      color: #9b8aad;
      margin: 0;
    }
  }
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .card-title {
    font-size: 16px;
    margin: 0 0 16px;
    color: #3d2b4a;
  }
}

.body-card {
  background: linear-gradient(135deg, #fff 0%, #f8f0ff 50%, #fff0f5 100%);
  border: 2px solid rgba(255, 182, 193, 0.2);
  overflow: hidden;
}

.body-card-content {
  display: flex;
  gap: 30px;
  min-height: 350px;
}

.body-avatar-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.body-stats-section {
  width: 250px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(255, 182, 193, 0.2);

  .card-title {
    text-align: center;
    font-size: 20px;
    margin-bottom: 24px;
    background: linear-gradient(135deg, #ff6b9d, #9b7fe6);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.body-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: linear-gradient(135deg, #fff, #fff8fa);
  border-radius: 10px;
  border: 1px solid rgba(255, 182, 193, 0.15);

  .stat-label {
    font-size: 14px;
    color: #9b8aad;
  }

  .stat-value {
    font-size: 16px;
    font-weight: 600;
    color: #3d2b4a;
  }
}

.body-tip {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #b8a0c8;
  padding: 10px;
  background: linear-gradient(135deg, rgba(255, 107, 157, 0.1), rgba(155, 127, 230, 0.1));
  border-radius: 10px;
}

.equal-height-row {
  display: flex;
  align-items: stretch;

  .el-col {
    display: flex;
  }
}

.equal-height-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

// 今日热量卡片 - 正方形
.calorie-card {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(135deg, #fff 0%, #fff0f5 100%);
  border: 2px solid rgba(255, 107, 157, 0.15);

  .card-title {
    margin-bottom: 8px;
  }

  .calorie-chart {
    width: 100%;
    flex: 1;
    min-height: 120px;
  }

  .calorie-info {
    text-align: center;
    margin-top: 8px;

    .calorie-intake {
      display: block;
      font-size: 28px;
      font-weight: 700;
      color: #ff6b9d;
    }

    .calorie-label {
      font-size: 12px;
      color: #9b8aad;
    }
  }
}

.meal-bars {
  .meal-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .meal-name {
      width: 50px;
      font-size: 14px;
      color: #6b5a7a;
    }

    .el-progress {
      flex: 1;
    }

    .meal-cal {
      width: 80px;
      text-align: right;
      font-size: 14px;
      color: #9b8aad;
    }
  }
}

.bmi-display {
  text-align: center;
  padding: 20px 0;

  .bmi-value {
    font-size: 48px;
    font-weight: bold;
  }

  .bmi-category {
    font-size: 18px;
    margin: 8px 0 24px;
  }

  .bmi-labels {
    display: flex;
    justify-content: space-between;
    margin-top: 8px;
    font-size: 12px;
    color: #9b8aad;
  }
}
</style>
