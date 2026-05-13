<template>
  <div class="water-intake">
    <div class="page-nav">
      <el-button text @click="router.push('/health-habits')">
        <el-icon><ArrowLeft /></el-icon>
        返回健康习惯
      </el-button>
    </div>

    <!-- 顶部：进度环 + 操作区 -->
    <div class="top-section">
      <div class="card progress-card">
        <div class="progress-ring">
          <svg viewBox="0 0 200 200">
            <defs>
              <linearGradient id="waterGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#4facfe" />
                <stop offset="100%" style="stop-color:#00f2fe" />
              </linearGradient>
            </defs>
            <circle class="ring-bg" cx="100" cy="100" r="85" />
            <circle class="ring-fill" cx="100" cy="100" r="85"
              :style="{ strokeDashoffset: dashOffset }" />
          </svg>
          <div class="ring-center">
            <span class="ring-amount">{{ data.todayAmount || 0 }}</span>
            <span class="ring-unit">ml</span>
            <span class="ring-target">目标 {{ data.dailyGoal || 2000 }} ml</span>
          </div>
        </div>
        <div class="remaining" v-if="data.remaining > 0">
          还需喝 <strong>{{ data.remaining }}</strong> ml
        </div>
        <div class="remaining done" v-else>
          今日目标已达成！
          <div class="pet-reward" v-if="!petRewarded">
            <el-button type="primary" size="small" @click="rewardPet" :loading="rewarding">
              给宠物加经验 💧
            </el-button>
          </div>
          <div class="pet-reward done" v-else>
            已获得宠物经验
          </div>
        </div>
      </div>

      <div class="card action-card">
        <h3 class="card-title">快速添加</h3>
        <div class="quick-add">
          <el-button
            v-for="preset in presets"
            :key="preset.amount"
            class="preset-btn"
            @click="addWater(preset.amount)"
          >
            <span class="preset-icon">{{ preset.icon }}</span>
            <span class="preset-amount">+{{ preset.amount }}ml</span>
          </el-button>
        </div>

        <div class="custom-row">
          <el-input-number
            v-model="customAmount"
            :min="1"
            :max="5000"
            :step="50"
            size="large"
          />
          <el-button type="primary" size="large" @click="addWater(customAmount)">
            <el-icon><Plus /></el-icon>
            添加
          </el-button>
        </div>

        <div class="drink-type-row">
          <span class="type-label">饮水方式：</span>
          <el-radio-group v-model="drinkType" size="small">
            <el-radio-button value="water">白开水</el-radio-button>
            <el-radio-button value="tea">茶</el-radio-button>
            <el-radio-button value="coffee">咖啡</el-radio-button>
            <el-radio-button value="other">其他</el-radio-button>
          </el-radio-group>
        </div>

        <div class="goal-row">
          <span class="goal-label">每日目标：<strong>{{ data.dailyGoal || 2000 }} ml</strong></span>
          <el-button text size="small" @click="showGoalDialog = true">
            <el-icon><Setting /></el-icon>
            修改
          </el-button>
          <span class="goal-tip">推荐：体重(kg) × 35ml</span>
        </div>
      </div>
    </div>

    <!-- 中部：记录 + 7天统计 -->
    <div class="middle-section">
      <div class="card records-card">
        <h3 class="card-title">今日记录</h3>
        <div class="records-list" v-if="data.records && data.records.length > 0">
          <div class="record-item" v-for="record in data.records" :key="record.id">
            <span class="record-icon">{{ getTypeIcon(record.drinkType) }}</span>
            <span class="record-amount">+{{ record.amount }} ml</span>
            <span class="record-type">{{ getTypeName(record.drinkType) }}</span>
            <span class="record-time">{{ formatTime(record.recordTime) }}</span>
            <el-button text size="small" type="danger" @click="deleteRecord(record.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <el-empty v-else description="今天还没有饮水记录" :image-size="80" />
      </div>

      <div class="card week-card">
        <h3 class="card-title">近7天</h3>
        <div class="week-chart">
          <div class="chart-bar" v-for="day in data.recentDays" :key="day.date">
            <div class="bar-wrapper">
              <div class="bar-fill" :style="{ height: getBarHeight(day.totalAmount) }">
                <span class="bar-value" v-if="day.totalAmount > 0">
                  {{ day.totalAmount >= 1000 ? (day.totalAmount / 1000).toFixed(1) + 'L' : day.totalAmount }}
                </span>
              </div>
            </div>
            <span class="bar-date">{{ formatBarDate(day.date) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部：饼图 + 趋势图 -->
    <div class="bottom-section">
      <div class="card type-stats-card">
        <div class="card-header">
          <h3 class="card-title">饮水方式统计</h3>
          <el-radio-group v-model="typeStatsDays" size="small" @change="loadTypeStats">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="typeChartRef" class="type-chart"></div>
        <div class="type-legend" v-if="typeStats.length > 0">
          <span v-for="item in typeStats" :key="item.drinkType" class="legend-item">
            <span class="legend-dot" :style="{ background: typeColors[item.drinkType] }"></span>
            {{ item.typeName }} {{ item.percentage }}%
          </span>
        </div>
      </div>

      <div class="card trend-card">
        <div class="card-header">
          <h3 class="card-title">饮水趋势</h3>
          <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="trendChartRef" class="trend-chart"></div>
      </div>
    </div>

    <!-- 修改目标弹窗 -->
    <el-dialog v-model="showGoalDialog" title="修改每日饮水目标" width="360px">
      <el-form label-width="80px">
        <el-form-item label="目标(ml)">
          <el-input-number v-model="editGoal" :min="500" :max="5000" :step="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGoalDialog = false">取消</el-button>
        <el-button type="primary" @click="saveGoal">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Setting, Delete } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getTodayWater, addWaterRecord, deleteWaterRecord,
  updateWaterGoal, getWaterTypeStats, getWaterTrend, addWaterExp
} from '../../api/water'

const router = useRouter()

const data = ref({
  todayAmount: 0,
  dailyGoal: 2000,
  percentage: 0,
  remaining: 2000,
  records: [],
  recentDays: []
})

const customAmount = ref(250)
const drinkType = ref('water')
const showGoalDialog = ref(false)
const editGoal = ref(2000)
const petRewarded = ref(false)
const rewarding = ref(false)

const typeStatsDays = ref(7)
const trendDays = ref(7)
const typeStats = ref([])
const trendData = ref([])

const typeChartRef = ref(null)
const trendChartRef = ref(null)
let typeChart = null
let trendChart = null

const presets = [
  { amount: 250, icon: '🥤' },
  { amount: 500, icon: '🍶' },
  { amount: 750, icon: '💧' },
  { amount: 1000, icon: '🫗' }
]

const typeColors = {
  water: '#4facfe',
  tea: '#67c23a',
  coffee: '#e6a23c',
  other: '#909399'
}

const circumference = 2 * Math.PI * 85

const dashOffset = computed(() => {
  const pct = Math.min(100, data.value.percentage || 0)
  return circumference - (circumference * pct / 100)
})

function getBarHeight(amount) {
  const maxAmount = Math.max(data.value.dailyGoal || 2000, ...((data.value.recentDays || []).map(d => d.totalAmount)))
  if (maxAmount === 0 || amount === 0) return '4px'
  const pct = Math.max(4, Math.round((amount / maxAmount) * 100))
  return pct + '%'
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatBarDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return weekdays[d.getDay()]
}

function getTypeIcon(type) {
  const icons = { water: '💧', tea: '🍵', coffee: '☕', other: '🥤' }
  return icons[type] || '💧'
}

function getTypeName(type) {
  const names = { water: '白开水', tea: '茶', coffee: '咖啡', other: '其他' }
  return names[type] || '白开水'
}

async function loadData() {
  try {
    const res = await getTodayWater()
    data.value = res.data
    editGoal.value = res.data.dailyGoal || 2000
  } catch (e) {
    // ignore
  }
}

async function loadTypeStats() {
  try {
    const res = await getWaterTypeStats(typeStatsDays.value)
    typeStats.value = res.data || []
    await nextTick()
    renderTypeChart()
  } catch (e) {
    // ignore
  }
}

async function loadTrend() {
  try {
    const res = await getWaterTrend(trendDays.value)
    trendData.value = res.data || []
    await nextTick()
    renderTrendChart()
  } catch (e) {
    // ignore
  }
}

function renderTypeChart() {
  if (!typeChartRef.value) return
  if (!typeChart) {
    typeChart = echarts.init(typeChartRef.value)
  }

  const chartData = typeStats.value.map(item => ({
    value: item.totalAmount,
    name: item.typeName,
    itemStyle: { color: typeColors[item.drinkType] || '#909399' }
  }))

  if (chartData.length === 0) {
    chartData.push({ value: 1, name: '暂无数据', itemStyle: { color: '#e8f4fd' } })
  }

  typeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}ml ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      avoidLabelOverlap: false,
      label: {
        show: true,
        position: 'center',
        formatter: typeStats.value.length > 0 ? '饮水\n方式' : '暂无\n数据',
        fontSize: 14,
        color: '#9b8aad'
      },
      data: chartData
    }]
  })
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const dates = trendData.value.map(d => {
    const date = new Date(d.date)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
  const amounts = trendData.value.map(d => d.totalAmount)

  trendChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>饮水量: {c} ml' },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { color: '#9b8aad', fontSize: 11 },
      axisLine: { lineStyle: { color: '#e8f4fd' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#9b8aad', fontSize: 11, formatter: '{value}ml' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      type: 'line',
      data: amounts,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#4facfe', width: 2 },
      itemStyle: { color: '#4facfe' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(79,172,254,0.3)' },
          { offset: 1, color: 'rgba(79,172,254,0.05)' }
        ])
      },
      markLine: {
        silent: true,
        data: [{
          yAxis: data.value.dailyGoal || 2000,
          lineStyle: { color: '#ff6b9d', type: 'dashed', width: 1 },
          label: { formatter: '目标', color: '#ff6b9d', fontSize: 11 }
        }]
      }
    }]
  })
}

async function addWater(amount) {
  if (!amount || amount <= 0) {
    ElMessage.warning('请输入有效的饮水量')
    return
  }
  try {
    await addWaterRecord({ amount, drinkType: drinkType.value })
    ElMessage.success(`+${amount}ml`)
    await loadData()
    loadTypeStats()
    loadTrend()
  } catch (e) {
    // handled by interceptor
  }
}

async function deleteRecord(id) {
  try {
    await ElMessageBox.confirm('确定删除这条记录？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteWaterRecord(id)
    ElMessage.success('已删除')
    await loadData()
  } catch {
    // cancelled
  }
}

async function saveGoal() {
  try {
    await updateWaterGoal(editGoal.value)
    showGoalDialog.value = false
    ElMessage.success('目标已更新')
    await loadData()
  } catch (e) {
    // handled by interceptor
  }
}

async function rewardPet() {
  rewarding.value = true
  try {
    const res = await addWaterExp()
    petRewarded.value = true
    ElMessage.success(res.data.message || '宠物获得经验！')
    window.dispatchEvent(new CustomEvent('pet-data-updated'))
  } catch (e) {
    // handled by interceptor
  } finally {
    rewarding.value = false
  }
}

function handleResize() {
  typeChart?.resize()
  trendChart?.resize()
}

onMounted(async () => {
  await loadData()
  loadTypeStats()
  loadTrend()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  typeChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped lang="scss">
.water-intake {
  max-width: 1000px;
  margin: 0 auto;
}

.page-nav {
  margin-bottom: 16px;

  .el-button {
    color: #9b8aad;
    font-size: 14px;

    &:hover {
      color: #4facfe;
    }
  }
}

.card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .card-title {
    font-size: 16px;
    color: #3d2b4a;
    margin: 0 0 16px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .card-title {
    margin: 0;
  }
}

// 顶部区域
.top-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.progress-card {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.progress-ring {
  position: relative;
  width: 180px;
  height: 180px;
  margin-bottom: 12px;

  svg {
    transform: rotate(-90deg);
    width: 100%;
    height: 100%;
  }

  .ring-bg {
    fill: none;
    stroke: #e8f4fd;
    stroke-width: 12;
  }

  .ring-fill {
    fill: none;
    stroke: url(#waterGradient);
    stroke-width: 12;
    stroke-linecap: round;
    stroke-dasharray: 534;
    transition: stroke-dashoffset 0.6s ease;
  }

  .ring-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;

    .ring-amount {
      font-size: 32px;
      font-weight: 700;
      color: #4facfe;
    }

    .ring-unit {
      font-size: 14px;
      color: #9b8aad;
      margin-top: -4px;
    }

    .ring-target {
      font-size: 11px;
      color: #b8a0c8;
      margin-top: 4px;
    }
  }
}

.remaining {
  font-size: 14px;
  color: #9b8aad;
  text-align: center;

  strong {
    color: #4facfe;
    font-size: 18px;
  }

  &.done {
    color: #67c23a;
    font-weight: 600;
    margin-top: 4px;
  }

  .pet-reward {
    margin-top: 10px;

    &.done {
      font-size: 12px;
      color: #b8a0c8;
      font-weight: normal;
    }
  }
}

.action-card {
  flex: 1;
}

.quick-add {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;

  .preset-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 16px 8px;
    border-radius: 12px;
    border: 1px solid #e8f4fd;
    background: linear-gradient(135deg, #f0f9ff, #e8f4fd);
    transition: all 0.2s;

    &:hover {
      border-color: #4facfe;
      background: linear-gradient(135deg, #e8f4fd, #d6efff);
      transform: translateY(-2px);
    }

    .preset-icon {
      font-size: 24px;
    }

    .preset-amount {
      font-size: 13px;
      color: #4facfe;
      font-weight: 600;
    }
  }
}

.custom-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;

  .el-input-number {
    flex: 1;
  }
}

.drink-type-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;

  .type-label {
    font-size: 13px;
    color: #9b8aad;
    white-space: nowrap;
  }
}

.goal-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f8f9ff;
  border-radius: 10px;

  .goal-label {
    font-size: 14px;
    color: #6b5a7a;

    strong {
      color: #4facfe;
    }
  }

  .goal-tip {
    font-size: 12px;
    color: #b8a0c8;
    margin-left: auto;
  }
}

// 中部区域
.middle-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.records-card {
  flex: 1;

  .records-list {
    max-height: 320px;
    overflow-y: auto;

    .record-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 0;
      border-bottom: 1px solid #f5e6f0;

      &:last-child {
        border-bottom: none;
      }

      .record-icon {
        font-size: 18px;
      }

      .record-amount {
        font-size: 15px;
        font-weight: 600;
        color: #4facfe;
      }

      .record-type {
        font-size: 13px;
        color: #9b8aad;
      }

      .record-time {
        font-size: 13px;
        color: #b8a0c8;
        margin-left: auto;
      }
    }
  }
}

.week-card {
  width: 300px;
  flex-shrink: 0;

  .week-chart {
    display: flex;
    gap: 12px;
    align-items: flex-end;
    height: 200px;

    .chart-bar {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      height: 100%;

      .bar-wrapper {
        flex: 1;
        width: 100%;
        display: flex;
        align-items: flex-end;
        justify-content: center;
      }

      .bar-fill {
        width: 70%;
        min-height: 4px;
        background: linear-gradient(180deg, #4facfe, #00f2fe);
        border-radius: 6px 6px 0 0;
        position: relative;
        transition: height 0.4s ease;

        .bar-value {
          position: absolute;
          top: -20px;
          left: 50%;
          transform: translateX(-50%);
          font-size: 11px;
          color: #4facfe;
          white-space: nowrap;
        }
      }

      .bar-date {
        margin-top: 8px;
        font-size: 12px;
        color: #9b8aad;
      }
    }
  }
}

// 底部区域
.bottom-section {
  display: flex;
  gap: 20px;
}

.type-stats-card {
  width: 360px;
  flex-shrink: 0;

  .type-chart {
    width: 100%;
    height: 220px;
  }

  .type-legend {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 8px;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #6b5a7a;

      .legend-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
      }
    }
  }
}

.trend-card {
  flex: 1;

  .trend-chart {
    width: 100%;
    height: 250px;
  }
}

:deep(.el-radio-button__inner) {
  padding: 6px 12px;
}
</style>
