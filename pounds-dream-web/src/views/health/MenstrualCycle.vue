<template>
  <div class="menstrual-cycle">
    <div class="page-nav">
      <el-button text @click="router.push('/health-habits')">
        <el-icon><ArrowLeft /></el-icon>
        返回健康习惯
      </el-button>
    </div>

    <!-- 顶部概览 -->
    <div class="overview-card">
      <div class="overview-left">
        <div class="phase-icon" :style="{ background: phaseColor }">
          <el-icon size="28"><Female /></el-icon>
        </div>
        <div class="phase-info">
          <h3>{{ currentPhase }}</h3>
          <p v-if="stats?.currentCycle?.daysUntilNext !== null && stats?.currentCycle?.daysUntilNext > 0">
            距离下次经期还有 <strong>{{ stats.currentCycle.daysUntilNext }}</strong> 天
          </p>
          <p v-else-if="stats?.currentCycle?.daysUntilNext === 0">今天是预测的经期开始日</p>
          <p v-else>点击下方按钮记录经期</p>
        </div>
      </div>
      <div class="overview-right">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          记录经期
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row" v-if="stats">
      <div class="stat-card">
        <span class="stat-num">{{ stats.avgCycleLength || '--' }}</span>
        <span class="stat-label">平均周期(天)</span>
      </div>
      <div class="stat-card">
        <span class="stat-num">{{ stats.avgPeriodLength || '--' }}</span>
        <span class="stat-label">平均经期(天)</span>
      </div>
      <div class="stat-card">
        <span class="stat-num">{{ stats.minCycleLength || '--' }}</span>
        <span class="stat-label">最短周期(天)</span>
      </div>
      <div class="stat-card">
        <span class="stat-num">{{ stats.maxCycleLength || '--' }}</span>
        <span class="stat-label">最长周期(天)</span>
      </div>
    </div>

    <!-- 日历视图 -->
    <div class="card calendar-card">
      <div class="calendar-header">
        <el-button text @click="prevMonth">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h3>{{ currentYear }}年{{ currentMonth + 1 }}月</h3>
        <el-button text @click="nextMonth">
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="calendar-weekdays">
        <span v-for="day in weekdays" :key="day">{{ day }}</span>
      </div>
      <div class="calendar-grid">
        <div
          v-for="(cell, index) in calendarCells"
          :key="index"
          class="calendar-cell"
          :class="{
            'other-month': !cell.isCurrentMonth,
            'is-today': cell.isToday,
            'period-day': cell.isPeriod,
            'predicted-day': cell.isPredicted,
            'ovulation-day': cell.isOvulation,
            'safe-day': cell.isSafe
          }"
          @click="cell.isCurrentMonth && selectDate(cell)"
        >
          <span class="cell-date">{{ cell.day }}</span>
          <span v-if="cell.isPeriod" class="cell-dot period"></span>
          <span v-else-if="cell.isOvulation" class="cell-dot ovulation"></span>
          <span v-else-if="cell.isPredicted" class="cell-dot predicted"></span>
          <span v-else-if="cell.isSafe" class="cell-dot safe"></span>
        </div>
      </div>
      <div class="calendar-legend">
        <span class="legend-item"><span class="legend-dot period"></span> 经期</span>
        <span class="legend-item"><span class="legend-dot predicted"></span> 预测经期</span>
        <span class="legend-item"><span class="legend-dot ovulation"></span> 排卵期</span>
        <span class="legend-item"><span class="legend-dot safe"></span> 安全期</span>
      </div>
    </div>

    <!-- 历史记录 -->
    <div class="card history-card">
      <h3 class="card-title">历史记录</h3>
      <div class="history-list" v-if="records.length > 0">
        <div class="history-item" v-for="record in records" :key="record.id">
          <div class="history-date">
            <span class="date-month">{{ formatMonth(record.startDate) }}</span>
            <span class="date-day">{{ formatDay(record.startDate) }}</span>
          </div>
          <div class="history-info">
            <span class="history-range">
              {{ formatDate(record.startDate) }} - {{ record.endDate ? formatDate(record.endDate) : '进行中' }}
            </span>
            <span class="history-duration" v-if="record.periodLength">
              持续 {{ record.periodLength }} 天
            </span>
            <span class="history-mood" v-if="record.mood">{{ record.mood }}</span>
          </div>
          <div class="history-actions">
            <el-button text size="small" @click="editRecord(record)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button text size="small" type="danger" @click="confirmDelete(record)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无记录" />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑经期记录' : '记录经期'"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="开始日期" required>
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择经期开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择经期结束日期(可稍后补填)"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="endDateDisabled"
          />
        </el-form-item>
        <el-form-item label="情绪">
          <el-select v-model="form.mood" placeholder="选择情绪状态" clearable style="width: 100%">
            <el-option label="😊 开心" value="开心" />
            <el-option label="😌 平静" value="平静" />
            <el-option label="😢 低落" value="低落" />
            <el-option label="😤 烦躁" value="烦躁" />
            <el-option label="😴 疲惫" value="疲惫" />
          </el-select>
        </el-form-item>
        <el-form-item label="症状">
          <el-checkbox-group v-model="selectedSymptoms">
            <el-checkbox label="腹痛" />
            <el-checkbox label="腰酸" />
            <el-checkbox label="头痛" />
            <el-checkbox label="乳房胀痛" />
            <el-checkbox label="疲劳" />
            <el-checkbox label="食欲变化" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.note"
            type="textarea"
            :rows="2"
            placeholder="记录其他信息(可选)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '更新' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Female, Plus, ArrowLeft, ArrowRight, Edit, Delete } from '@element-plus/icons-vue'
import {
  getMenstrualRecords,
  getMenstrualStats,
  addMenstrualRecord,
  updateMenstrualRecord,
  deleteMenstrualRecord
} from '../../api/menstrual'

const router = useRouter()

const records = ref([])
const stats = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const selectedSymptoms = ref([])

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth())

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const form = ref({
  id: null,
  startDate: '',
  endDate: '',
  mood: '',
  note: ''
})

const currentPhase = computed(() => {
  return stats.value?.currentCycle?.currentPhase || '未记录'
})

const phaseColor = computed(() => {
  const phase = currentPhase.value
  if (phase === '月经期') return 'linear-gradient(135deg, #ff6b9d, #ff8fb1)'
  if (phase === '卵泡期') return 'linear-gradient(135deg, #a8e6cf, #88d8a8)'
  if (phase === '排卵期') return 'linear-gradient(135deg, #ffd93d, #ff9a3c)'
  if (phase === '黄体期') return 'linear-gradient(135deg, #c9b1ff, #a78bfa)'
  return 'linear-gradient(135deg, #ff6b9d, #ff8fb1)'
})

// 生成日历数据
const calendarCells = computed(() => {
  const cells = []
  const firstDay = new Date(currentYear.value, currentMonth.value, 1)
  const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0)
  const startDayOfWeek = firstDay.getDay()

  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  // 上个月的日期
  const prevMonthLastDay = new Date(currentYear.value, currentMonth.value, 0)
  for (let i = startDayOfWeek - 1; i >= 0; i--) {
    cells.push({
      day: prevMonthLastDay.getDate() - i,
      isCurrentMonth: false,
      isToday: false,
      isPeriod: false,
      isPredicted: false
    })
  }

  // 本月的日期
  for (let d = 1; d <= lastDay.getDate(); d++) {
    const dateStr = `${currentYear.value}-${String(currentMonth.value + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const isToday = dateStr === todayStr
    const isPeriod = isDateInPeriod(dateStr)
    const isPredicted = isDatePredicted(dateStr)
    const isOvulation = !isPeriod && !isPredicted && isDateInOvulation(dateStr)
    const isSafe = !isPeriod && !isPredicted && !isOvulation && isDateInSafe(dateStr)

    cells.push({
      day: d,
      dateStr,
      isCurrentMonth: true,
      isToday,
      isPeriod,
      isPredicted,
      isOvulation,
      isSafe
    })
  }

  // 下个月的日期
  const remaining = 42 - cells.length
  for (let d = 1; d <= remaining; d++) {
    cells.push({
      day: d,
      isCurrentMonth: false,
      isToday: false,
      isPeriod: false,
      isPredicted: false
    })
  }

  return cells
})

function isDateInPeriod(dateStr) {
  const date = new Date(dateStr)
  return records.value.some(record => {
    const start = new Date(record.startDate)
    const end = record.endDate ? new Date(record.endDate) : start
    return date >= start && date <= end
  })
}

function isDatePredicted(dateStr) {
  const date = new Date(dateStr)
  if (!stats.value?.currentCycle?.nextPredictedDate) return false
  const predicted = new Date(stats.value.currentCycle.nextPredictedDate)
  const avgPeriod = stats.value?.avgPeriodLength || 5
  const predictedEnd = new Date(predicted)
  predictedEnd.setDate(predictedEnd.getDate() + avgPeriod)
  return date >= predicted && date <= predictedEnd
}

// 排卵期：从最近一次经期开始日算起，第12-16天（共5天）
function isDateInOvulation(dateStr) {
  if (records.value.length === 0) return false
  const date = new Date(dateStr)
  const latest = records.value[0]
  const periodStart = new Date(latest.startDate)
  const cycleLen = latest.cycleLength || stats.value?.avgCycleLength || 28
  const periodLen = latest.periodLength || stats.value?.avgPeriodLength || 5

  // 检查当前周期和上一个周期的排卵期
  for (let cycle = 0; cycle < 2; cycle++) {
    const cycleStart = new Date(periodStart)
    cycleStart.setDate(cycleStart.getDate() + cycle * cycleLen)
    const ovulationStart = new Date(cycleStart)
    ovulationStart.setDate(ovulationStart.getDate() + cycleLen - 14 - 2) // 排卵日前2天
    const ovulationEnd = new Date(cycleStart)
    ovulationEnd.setDate(ovulationEnd.getDate() + cycleLen - 14 + 2) // 排卵日后2天
    if (date >= ovulationStart && date <= ovulationEnd) return true
  }
  return false
}

// 安全期：经期结束后到排卵期前，以及排卵期后到下次经期前
function isDateInSafe(dateStr) {
  if (records.value.length === 0) return false
  const date = new Date(dateStr)
  const latest = records.value[0]
  const periodStart = new Date(latest.startDate)
  const cycleLen = latest.cycleLength || stats.value?.avgCycleLength || 28
  const periodLen = latest.periodLength || stats.value?.avgPeriodLength || 5

  for (let cycle = 0; cycle < 2; cycle++) {
    const cycleStart = new Date(periodStart)
    cycleStart.setDate(cycleStart.getDate() + cycle * cycleLen)

    // 经期结束后7天内（前安全期）
    const safeEnd = new Date(cycleStart)
    safeEnd.setDate(safeEnd.getDate() + periodLen + 7)

    // 排卵期后到下次经期前（后安全期）
    const ovulationEnd = new Date(cycleStart)
    ovulationEnd.setDate(ovulationEnd.getDate() + cycleLen - 14 + 2)
    const nextPeriod = new Date(cycleStart)
    nextPeriod.setDate(nextPeriod.getDate() + cycleLen)

    if ((date >= new Date(cycleStart.getTime() + periodLen * 86400000) && date <= safeEnd) ||
        (date > ovulationEnd && date < nextPeriod)) {
      return true
    }
  }
  return false
}

function prevMonth() {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

function nextMonth() {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

function selectDate(cell) {
  if (cell.isPeriod) {
    // 点击经期日子，找到对应记录并编辑
    const record = records.value.find(r => {
      const start = new Date(r.startDate)
      const end = r.endDate ? new Date(r.endDate) : start
      const date = new Date(cell.dateStr)
      return date >= start && date <= end
    })
    if (record) {
      editRecord(record)
    }
  } else {
    // 点击其他日子，清空表单新建记录
    form.value = { id: null, startDate: cell.dateStr, endDate: '', mood: '', note: '' }
    selectedSymptoms.value = []
    isEdit.value = false
    dialogVisible.value = true
  }
}

function showAddDialog() {
  form.value = { id: null, startDate: '', endDate: '', mood: '', note: '' }
  selectedSymptoms.value = []
  isEdit.value = false
  dialogVisible.value = true
}

function editRecord(record) {
  form.value = {
    id: record.id,
    startDate: record.startDate,
    endDate: record.endDate,
    mood: record.mood || '',
    note: record.note || ''
  }
  try {
    selectedSymptoms.value = record.symptoms ? JSON.parse(record.symptoms) : []
  } catch {
    selectedSymptoms.value = []
  }
  isEdit.value = true
  dialogVisible.value = true
}

function endDateDisabled(date) {
  if (!form.value.startDate) return false
  return date < new Date(form.value.startDate)
}

async function submitForm() {
  if (!form.value.startDate) {
    ElMessage.warning('请选择开始日期')
    return
  }
  submitting.value = true
  try {
    const data = {
      ...form.value,
      symptoms: JSON.stringify(selectedSymptoms.value)
    }
    if (isEdit.value) {
      await updateMenstrualRecord(data)
      ElMessage.success('更新成功')
    } else {
      await addMenstrualRecord(data)
      ElMessage.success('记录成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function confirmDelete(record) {
  try {
    await ElMessageBox.confirm('确定删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMenstrualRecord(record.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch {
    // cancelled
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function formatMonth(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月`
}

function formatDay(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.getDate()
}

async function loadData() {
  try {
    const [recordsRes, statsRes] = await Promise.all([
      getMenstrualRecords(),
      getMenstrualStats()
    ])
    records.value = recordsRes.data || []
    stats.value = statsRes.data
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.menstrual-cycle {
  max-width: 1000px;
  margin: 0 auto;
}

.page-nav {
  margin-bottom: 16px;

  .el-button {
    color: #9b8aad;
    font-size: 14px;

    &:hover {
      color: #ff6b9d;
    }
  }
}

.overview-card {
  background: linear-gradient(135deg, #fff 0%, #fff0f5 100%);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 2px solid rgba(255, 107, 157, 0.15);
  margin-bottom: 20px;

  .overview-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .phase-icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }

    .phase-info {
      h3 {
        font-size: 20px;
        color: #3d2b4a;
        margin: 0 0 4px;
      }

      p {
        font-size: 14px;
        color: #9b8aad;
        margin: 0;

        strong {
          color: #ff6b9d;
          font-size: 18px;
        }
      }
    }
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;

  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    .stat-num {
      display: block;
      font-size: 28px;
      font-weight: 700;
      color: #ff6b9d;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 12px;
      color: #9b8aad;
    }
  }
}

.card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;

  .card-title {
    font-size: 16px;
    color: #3d2b4a;
    margin: 0 0 16px;
  }
}

.calendar-card {
  .calendar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;

    h3 {
      font-size: 18px;
      color: #3d2b4a;
      margin: 0;
    }
  }

  .calendar-weekdays {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    text-align: center;
    margin-bottom: 8px;

    span {
      font-size: 13px;
      color: #9b8aad;
      padding: 8px 0;
    }
  }

  .calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 4px;
  }

  .calendar-cell {
    aspect-ratio: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;

    &:hover {
      background: #fff0f5;
    }

    &.other-month {
      opacity: 0.3;
      cursor: default;
    }

    &.is-today {
      .cell-date {
        background: #ff6b9d;
        color: #fff;
        border-radius: 50%;
        width: 28px;
        height: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    &.period-day {
      background: rgba(255, 107, 157, 0.15);
    }

    &.predicted-day {
      background: rgba(255, 107, 157, 0.08);
      border: 1px dashed rgba(255, 107, 157, 0.3);
    }

    &.ovulation-day {
      background: rgba(255, 183, 77, 0.15);
      border: 1px solid rgba(255, 183, 77, 0.3);
    }

    &.safe-day {
      background: rgba(168, 230, 207, 0.15);
    }

    .cell-date {
      font-size: 14px;
      color: #3d2b4a;
    }

    .cell-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      margin-top: 2px;

      &.period {
        background: #ff6b9d;
      }

      &.predicted {
        background: #ffb8d0;
      }

      &.ovulation {
        background: #ffb347;
      }

      &.safe {
        background: #a8e6cf;
      }
    }
  }

  .calendar-legend {
    display: flex;
    gap: 20px;
    margin-top: 16px;
    justify-content: center;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #9b8aad;

      .legend-dot {
        width: 10px;
        height: 10px;
        border-radius: 3px;

        &.period {
          background: rgba(255, 107, 157, 0.3);
        }

        &.predicted {
          background: rgba(255, 107, 157, 0.15);
          border: 1px dashed rgba(255, 107, 157, 0.4);
        }

        &.ovulation {
          background: rgba(255, 179, 71, 0.3);
          border: 1px solid rgba(255, 179, 71, 0.5);
        }

        &.safe {
          background: rgba(168, 230, 207, 0.3);
        }
      }
    }
  }
}

.history-list {
  .history-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 14px 0;
    border-bottom: 1px solid #f5e6f0;

    &:last-child {
      border-bottom: none;
    }

    .history-date {
      width: 50px;
      text-align: center;

      .date-month {
        display: block;
        font-size: 12px;
        color: #9b8aad;
      }

      .date-day {
        display: block;
        font-size: 24px;
        font-weight: 700;
        color: #ff6b9d;
      }
    }

    .history-info {
      flex: 1;

      .history-range {
        display: block;
        font-size: 14px;
        color: #3d2b4a;
        margin-bottom: 4px;
      }

      .history-duration {
        font-size: 12px;
        color: #9b8aad;
        margin-right: 12px;
      }

      .history-mood {
        font-size: 12px;
        color: #b8a0c8;
      }
    }

    .history-actions {
      display: flex;
      gap: 4px;
    }
  }
}

:deep(.el-checkbox) {
  margin-right: 12px;
  margin-bottom: 6px;
}
</style>
