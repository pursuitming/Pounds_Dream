<template>
  <div class="sleep-record">
    <div class="page-nav">
      <el-button text @click="router.push('/health-habits')">
        <el-icon><ArrowLeft /></el-icon>
        返回健康习惯
      </el-button>
    </div>

    <!-- 顶部：睡眠评分 + 添加记录 -->
    <div class="top-section">
      <div class="card score-card">
        <div class="score-ring">
          <svg viewBox="0 0 200 200">
            <defs>
              <linearGradient id="sleepGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#a18cd1" />
                <stop offset="100%" style="stop-color:#fbc2eb" />
              </linearGradient>
            </defs>
            <circle class="ring-bg" cx="100" cy="100" r="85" />
            <circle class="ring-fill" cx="100" cy="100" r="85"
              :style="{ strokeDashoffset: dashOffset }" />
          </svg>
          <div class="ring-center">
            <span class="ring-score">{{ data.score || 0 }}</span>
            <span class="ring-label">睡眠评分</span>
          </div>
        </div>

        <!-- 连击天数 -->
        <div class="streak-badge" v-if="data.streak > 0">
          <span class="streak-icon">&#x1F525;</span>
          <span class="streak-text">连续 <strong>{{ data.streak }}</strong> 天</span>
        </div>

        <div class="score-info">
          <div class="info-item">
            <span class="info-label">今日睡眠</span>
            <span class="info-value">{{ formatDuration(data.lastDuration) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">目标</span>
            <span class="info-value">{{ formatDuration(data.dailyGoal || 480) }}</span>
          </div>
        </div>
        <div class="remaining" v-if="data.percentage < 100">
          完成度 <strong>{{ data.percentage || 0 }}%</strong>
        </div>
        <div class="remaining done" v-else>
          今日睡眠目标已达成！
          <div class="pet-reward" v-if="!petRewarded">
            <el-button type="primary" size="small" @click="rewardPet" :loading="rewarding">
              给宠物加经验
            </el-button>
          </div>
          <div class="pet-reward done" v-else>
            已获得宠物经验
          </div>
        </div>
      </div>

      <div class="card action-card">
        <h3 class="card-title">添加睡眠记录</h3>

        <!-- 智能推荐提示 -->
        <div class="recommend-tip" v-if="recommendation">
          <span class="tip-icon">&#x1F4A1;</span>
          <span class="tip-text">
            {{ recommendation.exerciseLevel }}：推荐睡眠 <strong>{{ formatDuration(recommendation.recommendedDuration) }}</strong>
          </span>
          <el-button text size="small" @click="applyRecommendation">应用</el-button>
        </div>

        <el-form :model="form" label-position="top" class="sleep-form">
          <div class="time-row">
            <el-form-item label="上床时间" class="time-item">
              <el-date-picker
                v-model="form.bedTime"
                type="datetime"
                placeholder="选择上床时间"
                format="MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="起床时间" class="time-item">
              <el-date-picker
                v-model="form.wakeTime"
                type="datetime"
                placeholder="选择起床时间"
                format="MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </div>

          <el-form-item label="睡眠质量">
            <el-rate v-model="form.quality" :max="5" :colors="['#a18cd1', '#c9a7e8', '#fbc2eb']" />
          </el-form-item>

          <el-form-item label="睡眠标签">
            <div class="tags-row">
              <el-tag
                v-for="tag in tagOptions"
                :key="tag"
                :type="form.tags?.includes(tag) ? '' : 'info'"
                :effect="form.tags?.includes(tag) ? 'dark' : 'plain'"
                class="tag-option"
                @click="toggleTag(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </el-form-item>

          <el-form-item label="午睡/小憩">
            <el-switch v-model="form.isNap" active-text="是" inactive-text="否" />
          </el-form-item>

          <el-form-item label="备注">
            <el-input v-model="form.note" type="textarea" :rows="2" placeholder="记录今天的睡眠感受..." />
          </el-form-item>

          <el-button type="primary" size="large" class="submit-btn" @click="submitRecord" :loading="submitting">
            保存睡眠记录
          </el-button>
        </el-form>

        <div class="goal-row">
          <span class="goal-label">每日目标：<strong>{{ formatDuration(data.dailyGoal || 480) }}</strong></span>
          <el-button text size="small" @click="showGoalDialog = true">
            <el-icon><Setting /></el-icon>
            修改
          </el-button>
          <span class="goal-tip">推荐：7-9 小时</span>
        </div>
      </div>
    </div>

    <!-- 中部：记录 + 7天统计 -->
    <div class="middle-section">
      <div class="card records-card">
        <h3 class="card-title">今日记录</h3>
        <div class="records-list" v-if="data.records && data.records.length > 0">
          <div class="record-item" v-for="record in data.records" :key="record.id">
            <div class="record-main">
              <span class="record-type-badge" :class="{ nap: record.isNap }">
                {{ record.isNap ? '午睡' : '夜间睡眠' }}
              </span>
              <span class="record-duration">{{ formatDuration(record.duration) }}</span>
              <div class="record-quality">
                <el-rate v-model="record.quality" disabled :max="5" size="small" />
              </div>
            </div>
            <div class="record-detail">
              <span class="record-time">
                {{ formatDateTime(record.bedTime) }} - {{ formatDateTime(record.wakeTime) }}
              </span>
              <span class="record-tags" v-if="record.tags">{{ record.tags }}</span>
            </div>
            <div class="record-note" v-if="record.note">{{ record.note }}</div>
            <el-button text size="small" type="danger" class="record-delete" @click="deleteRecord(record.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <el-empty v-else description="今天还没有睡眠记录" :image-size="80" />
      </div>

      <div class="card week-card">
        <h3 class="card-title">近7天</h3>
        <div class="week-chart">
          <div class="chart-bar" v-for="day in data.recentDays" :key="day.date">
            <div class="bar-wrapper">
              <div class="bar-fill" :style="{ height: getBarHeight(day.totalDuration) }">
                <span class="bar-value" v-if="day.totalDuration > 0">
                  {{ formatDurationShort(day.totalDuration) }}
                </span>
              </div>
            </div>
            <span class="bar-date">{{ formatBarDate(day.date) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部：规律性 + 推荐 + 趋势 -->
    <div class="bottom-section">
      <!-- 作息规律性 -->
      <div class="card regularity-card">
        <h3 class="card-title">作息规律性</h3>
        <div class="regularity-score">
          <div class="score-circle" :class="regularityLevel">
            <span class="score-num">{{ regularity.score || 0 }}</span>
            <span class="score-unit">分</span>
          </div>
          <span class="score-level">{{ regularityLevelText }}</span>
        </div>
        <div class="regularity-detail">
          <div class="detail-item">
            <span class="detail-label">平均上床</span>
            <span class="detail-value">{{ regularity.avgBedTime || '--:--' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">平均起床</span>
            <span class="detail-value">{{ regularity.avgWakeTime || '--:--' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">上床波动</span>
            <span class="detail-value">{{ regularity.bedTimeStdDev || 0 }}分钟</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">起床波动</span>
            <span class="detail-value">{{ regularity.wakeTimeStdDev || 0 }}分钟</span>
          </div>
        </div>
      </div>

      <!-- 智能推荐详情 -->
      <div class="card recommendation-card">
        <h3 class="card-title">运动联动推荐</h3>
        <div class="recommend-content" v-if="recommendation">
          <div class="recommend-icon">
            {{ recommendation.exerciseLevel === '无运动' ? '&#x1F6CC;' : '&#x1F3C3;' }}
          </div>
          <div class="recommend-info">
            <div class="recommend-level">{{ recommendation.exerciseLevel }}</div>
            <div class="recommend-duration">推荐睡眠 <strong>{{ formatDuration(recommendation.recommendedDuration) }}</strong></div>
            <div class="recommend-reason">{{ recommendation.reason }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无推荐" :image-size="60" />
      </div>

      <!-- 趋势图 -->
      <div class="card trend-card">
        <div class="card-header">
          <h3 class="card-title">睡眠趋势</h3>
          <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="trendChartRef" class="trend-chart"></div>
      </div>
    </div>

    <!-- AI 建议 -->
    <div class="ai-section">
      <div class="card ai-card">
        <div class="card-header">
          <h3 class="card-title">
            <span class="ai-icon">&#x1F9E0;</span> AI 睡眠建议
          </h3>
          <el-button text size="small" @click="loadAiAdvice" :loading="aiLoading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="ai-content" v-if="aiThinking || aiAdvice">
          <!-- 思考过程（可折叠） -->
          <div v-if="aiThinking" class="thinking-block" :class="{ expanded: aiThinkingExpanded }">
            <div class="thinking-header" @click="aiThinkingExpanded = !aiThinkingExpanded">
              <span class="thinking-icon">&#x1F4AD;</span>
              <span>思考过程</span>
              <span class="thinking-toggle">{{ aiThinkingExpanded ? '收起' : '展开' }}</span>
            </div>
            <div v-show="aiThinkingExpanded" class="thinking-content">{{ aiThinking }}</div>
          </div>
          <!-- 正式建议 -->
          <div v-if="aiAdvice" class="advice-text" v-html="formatAiAdvice(aiAdvice)"></div>
          <!-- 加载中光标 -->
          <span v-if="aiLoading && !aiThinking && !aiAdvice" class="typing-cursor">|</span>
        </div>
        <div class="ai-content loading" v-else-if="aiLoading">
          <el-skeleton :rows="3" animated />
        </div>
        <el-empty v-else description="点击刷新获取AI建议" :image-size="60" />
      </div>
    </div>

    <!-- 修改目标弹窗 -->
    <el-dialog v-model="showGoalDialog" title="修改每日睡眠目标" width="360px">
      <el-form label-width="80px">
        <el-form-item label="目标(分钟)">
          <el-input-number v-model="editGoal" :min="240" :max="720" :step="30" style="width: 100%" />
        </el-form-item>
        <div class="goal-presets">
          <el-button size="small" @click="editGoal = 420">7小时</el-button>
          <el-button size="small" @click="editGoal = 480">8小时</el-button>
          <el-button size="small" @click="editGoal = 540">9小时</el-button>
        </div>
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
import { ArrowLeft, Setting, Delete, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getTodaySleep, addSleepRecord, deleteSleepRecord,
  updateSleepGoal, addSleepExp, getSleepTrend,
  getSleepRegularity, getSleepRecommendation, getSleepAiAdviceStream
} from '../../api/sleep'

const router = useRouter()

const data = ref({
  lastDuration: 0,
  lastQuality: 0,
  lastTags: '',
  dailyGoal: 480,
  percentage: 0,
  score: 0,
  streak: 0,
  records: [],
  recentDays: []
})

const form = ref({
  bedTime: null,
  wakeTime: null,
  quality: 3,
  tags: '',
  isNap: false,
  note: ''
})

const tagOptions = ['深睡', '浅睡', '多梦', '失眠', '打鼾', '早醒', '入睡困难']
const submitting = ref(false)
const showGoalDialog = ref(false)
const editGoal = ref(480)
const petRewarded = ref(false)
const rewarding = ref(false)

const regularity = ref({})
const recommendation = ref(null)
const trendDays = ref(7)
const trendData = ref([])
const trendChartRef = ref(null)
let trendChart = null

const aiAdvice = ref('')
const aiThinking = ref('')
const aiThinkingExpanded = ref(false)
const aiLoading = ref(false)

const circumference = 2 * Math.PI * 85

const dashOffset = computed(() => {
  const pct = Math.min(100, data.value.score || 0)
  return circumference - (circumference * pct / 100)
})

const regularityLevel = computed(() => {
  const s = regularity.value.score || 0
  if (s >= 80) return 'excellent'
  if (s >= 60) return 'good'
  if (s >= 40) return 'fair'
  return 'poor'
})

const regularityLevelText = computed(() => {
  const s = regularity.value.score || 0
  if (s >= 80) return '非常规律'
  if (s >= 60) return '较为规律'
  if (s >= 40) return '一般'
  if (s > 0) return '需改善'
  return '暂无数据'
})

function formatDuration(minutes) {
  if (!minutes || minutes <= 0) return '0小时'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0) return `${m}分钟`
  if (m === 0) return `${h}小时`
  return `${h}小时${m}分`
}

function formatDurationShort(minutes) {
  if (!minutes || minutes <= 0) return ''
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0) return `${m}m`
  if (m === 0) return `${h}h`
  return `${h}h${m}`
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatBarDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return weekdays[d.getDay()]
}

function getBarHeight(duration) {
  const maxDuration = Math.max(data.value.dailyGoal || 480, ...((data.value.recentDays || []).map(d => d.totalDuration || 0)))
  if (maxDuration === 0 || !duration || duration === 0) return '4px'
  const pct = Math.max(4, Math.round((duration / maxDuration) * 100))
  return pct + '%'
}

function toggleTag(tag) {
  if (!form.value.tags) {
    form.value.tags = tag
    return
  }
  const tags = form.value.tags.split(',').filter(Boolean)
  const idx = tags.indexOf(tag)
  if (idx >= 0) {
    tags.splice(idx, 1)
  } else {
    tags.push(tag)
  }
  form.value.tags = tags.join(',')
}

function applyRecommendation() {
  if (recommendation.value) {
    editGoal.value = recommendation.value.recommendedDuration
    saveGoal()
  }
}

function formatAiAdvice(text) {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}

async function loadData() {
  try {
    const res = await getTodaySleep()
    data.value = res.data
    editGoal.value = res.data.dailyGoal || 480
  } catch (e) {
    // ignore
  }
}

async function loadRegularity() {
  try {
    const res = await getSleepRegularity()
    regularity.value = res.data
  } catch (e) {
    // ignore
  }
}

async function loadRecommendation() {
  try {
    const res = await getSleepRecommendation()
    recommendation.value = res.data
  } catch (e) {
    // ignore
  }
}

async function loadTrend() {
  try {
    const res = await getSleepTrend(trendDays.value)
    trendData.value = res.data || []
    await nextTick()
    renderTrendChart()
  } catch (e) {
    // ignore
  }
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
  const durations = trendData.value.map(d => d.totalDuration)
  const qualities = trendData.value.map(d => d.avgQuality)

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        let html = params[0].axisValue + '<br/>'
        params.forEach(p => {
          if (p.seriesName === '睡眠时长') {
            const h = Math.floor(p.value / 60)
            const m = p.value % 60
            html += `睡眠时长: ${h}h${m}m<br/>`
          } else {
            html += `睡眠质量: ${p.value.toFixed(1)}/5<br/>`
          }
        })
        return html
      }
    },
    legend: {
      data: ['睡眠时长', '睡眠质量'],
      bottom: 0,
      textStyle: { fontSize: 11, color: '#9b8aad' }
    },
    grid: { left: 50, right: 50, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { color: '#9b8aad', fontSize: 11 },
      axisLine: { lineStyle: { color: '#e8f4fd' } }
    },
    yAxis: [
      {
        type: 'value',
        name: '时长(分钟)',
        axisLabel: { color: '#9b8aad', fontSize: 11 },
        splitLine: { lineStyle: { color: '#f0f0f0' } }
      },
      {
        type: 'value',
        name: '质量',
        min: 0,
        max: 5,
        axisLabel: { color: '#9b8aad', fontSize: 11 },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '睡眠时长',
        type: 'line',
        data: durations,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#a18cd1', width: 2 },
        itemStyle: { color: '#a18cd1' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(161,140,209,0.3)' },
            { offset: 1, color: 'rgba(161,140,209,0.05)' }
          ])
        }
      },
      {
        name: '睡眠质量',
        type: 'line',
        yAxisIndex: 1,
        data: qualities,
        smooth: true,
        symbol: 'diamond',
        symbolSize: 6,
        lineStyle: { color: '#fbc2eb', width: 2 },
        itemStyle: { color: '#fbc2eb' }
      }
    ]
  })
}

async function loadAiAdvice() {
  aiLoading.value = true
  aiAdvice.value = ''
  aiThinking.value = ''
  aiThinkingExpanded.value = false
  try {
    getSleepAiAdviceStream(
      (text) => {
        aiThinking.value += text
        aiThinkingExpanded.value = true
      },
      () => {
        aiLoading.value = false
      },
      (err) => {
        aiAdvice.value = '暂时无法获取AI建议，请稍后再试。'
        aiLoading.value = false
      },
      (text) => {
        aiAdvice.value += text
        aiThinkingExpanded.value = false
      }
    )
  } catch (e) {
    aiAdvice.value = '暂时无法获取AI建议，请稍后再试。'
    aiLoading.value = false
  }
}

async function submitRecord() {
  if (!form.value.bedTime || !form.value.wakeTime) {
    ElMessage.warning('请选择上床时间和起床时间')
    return
  }
  submitting.value = true
  try {
    await addSleepRecord(form.value)
    ElMessage.success('睡眠记录已保存')
    form.value = { bedTime: null, wakeTime: null, quality: 3, tags: '', isNap: false, note: '' }
    await loadData()
    loadRegularity()
    loadRecommendation()
    loadTrend()
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function deleteRecord(id) {
  try {
    await ElMessageBox.confirm('确定删除这条记录？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSleepRecord(id)
    ElMessage.success('已删除')
    await loadData()
    loadRegularity()
    loadTrend()
  } catch {
    // cancelled
  }
}

async function saveGoal() {
  try {
    await updateSleepGoal(editGoal.value)
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
    const res = await addSleepExp()
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
  trendChart?.resize()
}

onMounted(async () => {
  await loadData()
  loadRegularity()
  loadRecommendation()
  loadTrend()
  loadAiAdvice()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})
</script>

<style scoped lang="scss">
.sleep-record {
  max-width: 1100px;
  margin: 0 auto;
}

.page-nav {
  margin-bottom: 16px;

  .el-button {
    color: #9b8aad;
    font-size: 14px;

    &:hover {
      color: #a18cd1;
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

.score-card {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-ring {
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
    stroke: #f0e6f6;
    stroke-width: 12;
  }

  .ring-fill {
    fill: none;
    stroke: url(#sleepGradient);
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

    .ring-score {
      font-size: 36px;
      font-weight: 700;
      color: #a18cd1;
    }

    .ring-label {
      font-size: 12px;
      color: #9b8aad;
      margin-top: 2px;
    }
  }
}

.streak-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: linear-gradient(135deg, #fff5e6, #ffe8cc);
  border-radius: 20px;
  margin-bottom: 10px;

  .streak-icon {
    font-size: 16px;
  }

  .streak-text {
    font-size: 13px;
    color: #e6a23c;

    strong {
      font-size: 16px;
      font-weight: 700;
    }
  }
}

.score-info {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;

  .info-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .info-label {
      font-size: 12px;
      color: #b8a0c8;
    }

    .info-value {
      font-size: 15px;
      font-weight: 600;
      color: #a18cd1;
    }
  }
}

.remaining {
  font-size: 14px;
  color: #9b8aad;
  text-align: center;

  strong {
    color: #a18cd1;
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

.recommend-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #f0f9ff, #e8f4fd);
  border-radius: 10px;
  margin-bottom: 16px;

  .tip-icon {
    font-size: 18px;
  }

  .tip-text {
    font-size: 13px;
    color: #6b5a7a;
    flex: 1;

    strong {
      color: #a18cd1;
    }
  }
}

.sleep-form {
  margin-bottom: 16px;
}

.time-row {
  display: flex;
  gap: 16px;

  .time-item {
    flex: 1;
  }
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .tag-option {
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      transform: translateY(-1px);
    }
  }
}

.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #a18cd1, #fbc2eb);
  border: none;

  &:hover {
    background: linear-gradient(135deg, #9176c4, #f0b0de);
  }
}

.goal-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f8f5ff;
  border-radius: 10px;

  .goal-label {
    font-size: 14px;
    color: #6b5a7a;

    strong {
      color: #a18cd1;
    }
  }

  .goal-tip {
    font-size: 12px;
    color: #b8a0c8;
    margin-left: auto;
  }
}

.goal-presets {
  margin-top: 12px;
  display: flex;
  gap: 8px;
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
    max-height: 400px;
    overflow-y: auto;

    .record-item {
      position: relative;
      padding: 14px 0;
      border-bottom: 1px solid #f5e6f0;

      &:last-child {
        border-bottom: none;
      }

      .record-main {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 6px;
      }

      .record-type-badge {
        font-size: 12px;
        padding: 2px 10px;
        border-radius: 12px;
        background: linear-gradient(135deg, #a18cd1, #fbc2eb);
        color: #fff;

        &.nap {
          background: linear-gradient(135deg, #89f7fe, #66a6ff);
        }
      }

      .record-duration {
        font-size: 16px;
        font-weight: 600;
        color: #a18cd1;
      }

      .record-detail {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 13px;
        color: #9b8aad;
      }

      .record-tags {
        font-size: 12px;
        color: #b8a0c8;
      }

      .record-note {
        font-size: 13px;
        color: #6b5a7a;
        margin-top: 6px;
        padding: 8px 12px;
        background: #f8f5ff;
        border-radius: 8px;
      }

      .record-delete {
        position: absolute;
        top: 14px;
        right: 0;
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
        background: linear-gradient(180deg, #a18cd1, #fbc2eb);
        border-radius: 6px 6px 0 0;
        position: relative;
        transition: height 0.4s ease;

        .bar-value {
          position: absolute;
          top: -20px;
          left: 50%;
          transform: translateX(-50%);
          font-size: 11px;
          color: #a18cd1;
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
  margin-bottom: 20px;
}

.regularity-card {
  width: 260px;
  flex-shrink: 0;
}

.regularity-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16px;

  .score-circle {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin-bottom: 8px;

    &.excellent {
      background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
      border: 3px solid #67c23a;
    }

    &.good {
      background: linear-gradient(135deg, #e8f4fd, #d6efff);
      border: 3px solid #409eff;
    }

    &.fair {
      background: linear-gradient(135deg, #fdf6ec, #faecd8);
      border: 3px solid #e6a23c;
    }

    &.poor {
      background: linear-gradient(135deg, #fef0f0, #fde2e2);
      border: 3px solid #f56c6c;
    }

    .score-num {
      font-size: 28px;
      font-weight: 700;
      color: #3d2b4a;
    }

    .score-unit {
      font-size: 12px;
      color: #9b8aad;
      margin-top: -4px;
    }
  }

  .score-level {
    font-size: 14px;
    color: #6b5a7a;
    font-weight: 600;
  }
}

.regularity-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;

  .detail-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 8px;
    background: #f8f5ff;
    border-radius: 8px;

    .detail-label {
      font-size: 11px;
      color: #b8a0c8;
    }

    .detail-value {
      font-size: 14px;
      font-weight: 600;
      color: #a18cd1;
      margin-top: 2px;
    }
  }
}

.recommendation-card {
  width: 280px;
  flex-shrink: 0;
}

.recommend-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;

  .recommend-icon {
    font-size: 36px;
    flex-shrink: 0;
  }

  .recommend-info {
    .recommend-level {
      font-size: 14px;
      font-weight: 600;
      color: #3d2b4a;
      margin-bottom: 4px;
    }

    .recommend-duration {
      font-size: 13px;
      color: #6b5a7a;
      margin-bottom: 8px;

      strong {
        color: #a18cd1;
        font-size: 16px;
      }
    }

    .recommend-reason {
      font-size: 12px;
      color: #9b8aad;
      line-height: 1.6;
    }
  }
}

.trend-card {
  flex: 1;

  .trend-chart {
    width: 100%;
    height: 260px;
  }
}

// AI 建议区域
.ai-section {
  margin-bottom: 20px;
}

.ai-card {
  .ai-icon {
    font-size: 18px;
    margin-right: 6px;
  }

  .ai-content {
    padding: 16px;
    background: linear-gradient(135deg, #f8f5ff, #f0e6f6);
    border-radius: 12px;
    min-height: 60px;

    .advice-text {
      font-size: 14px;
      color: #3d2b4a;
      line-height: 1.8;
    }
  }
}

.thinking-block {
  margin-bottom: 12px;
  border: 1px solid #edd6e6;
  border-radius: 8px;
  overflow: hidden;

  .thinking-header {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    background: #fef6f9;
    cursor: pointer;
    font-size: 12px;
    color: #9b8aad;
    user-select: none;

    .thinking-icon { font-size: 14px; }
    .thinking-toggle {
      margin-left: auto;
      color: #a18cd1;
      font-size: 11px;
    }

    &:hover {
      background: #f8eef3;
    }
  }

  .thinking-content {
    padding: 8px 12px;
    font-size: 12px;
    color: #9b8aad;
    line-height: 1.6;
    white-space: pre-wrap;
    max-height: 200px;
    overflow-y: auto;
  }
}

.typing-cursor {
  display: inline-block;
  animation: blink 0.8s infinite;
  color: #a18cd1;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

:deep(.el-form-item) {
  margin-bottom: 14px;
}

:deep(.el-form-item__label) {
  font-size: 13px;
  color: #6b5a7a;
}

:deep(.el-rate) {
  --el-rate-icon-size: 20px;
}

:deep(.el-radio-button__inner) {
  padding: 6px 12px;
}
</style>
