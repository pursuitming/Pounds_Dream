<template>
  <div class="weight-page">
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="card">
          <h3 class="card-title">记录体重</h3>
          <el-form :model="form" label-width="80px">
            <el-form-item label="日期">
              <el-date-picker v-model="form.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="体重(kg)">
              <el-input-number v-model="form.weight" :min="20" :max="300" :precision="1" :step="0.1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="体脂率(%)">
              <div class="bodyfat-display">
                <div v-if="calculatedBodyFat" class="bodyfat-value">
                  <el-tag type="success" effect="dark" round size="large">
                    {{ calculatedBodyFat }}%
                  </el-tag>
                  <span class="bodyfat-label">自动计算</span>
                </div>
                <div v-else class="bodyfat-placeholder">
                  请输入体重后自动计算
                </div>
              </div>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.note" type="textarea" :rows="2" placeholder="可选" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card">
          <h3 class="card-title">记录围度</h3>
          <el-form :model="measurementForm" label-width="80px">
            <el-form-item label="日期">
              <el-date-picker v-model="measurementForm.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="腰围(cm)">
              <el-input-number v-model="measurementForm.waist" :min="30" :max="200" :precision="1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="臀围(cm)">
              <el-input-number v-model="measurementForm.hip" :min="30" :max="200" :precision="1" style="width: 100%" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="submittingMeasurement" @click="handleMeasurementSubmit">保存围度</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <el-col :span="16">
        <div class="card">
          <div class="chart-header">
            <h3 class="card-title">体重趋势</h3>
            <el-radio-group v-model="trendDays" @change="fetchTrend">
              <el-radio-button :value="7">7天</el-radio-button>
              <el-radio-button :value="30">30天</el-radio-button>
              <el-radio-button :value="90">90天</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="chartRef" style="height: 400px"></div>
          <div class="trend-stats" v-if="trend">
            <div class="stat">
              <span class="label">起始体重</span>
              <span class="value">{{ trend.startWeight || '--' }} kg</span>
            </div>
            <div class="stat">
              <span class="label">当前体重</span>
              <span class="value">{{ trend.currentWeight || '--' }} kg</span>
            </div>
            <div class="stat">
              <span class="label">总变化</span>
              <span class="value" :class="trend.totalChange < 0 ? 'down' : 'up'">
                {{ trend.totalChange ? (trend.totalChange > 0 ? '+' : '') + trend.totalChange : '--' }} kg
              </span>
            </div>
            <div class="stat">
              <span class="label">日均变化</span>
              <span class="value" :class="trend.averageChange < 0 ? 'down' : 'up'">
                {{ trend.averageChange ? (trend.averageChange > 0 ? '+' : '') + trend.averageChange : '--' }} kg
              </span>
            </div>
          </div>
        </div>

        <div class="card">
          <h3 class="card-title">历史记录</h3>
          <el-table :data="history" stripe style="width: 100%">
            <el-table-column prop="recordDate" label="日期" width="120" />
            <el-table-column prop="weight" label="体重(kg)" width="100" />
            <el-table-column prop="bodyFat" label="体脂率(%)" width="100">
              <template #default="{ row }">
                {{ row.bodyFat || '--' }}
              </template>
            </el-table-column>
            <el-table-column prop="bmi" label="BMI" width="80">
              <template #default="{ row }">
                {{ row.bmi || '--' }}
              </template>
            </el-table-column>
            <el-table-column prop="note" label="备注" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { addOrUpdateWeight, getWeightTrend, getWeightHistory, deleteWeight, addBodyMeasurement } from '../../api/weight'
import { useUserStore } from '../../store/modules/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const form = reactive({
  recordDate: new Date().toISOString().split('T')[0],
  weight: null,
  bodyFat: null,
  note: ''
})

const measurementForm = reactive({
  recordDate: new Date().toISOString().split('T')[0],
  waist: null,
  hip: null
})

// 计算体脂率
const calculatedBodyFat = ref(null)

function updateCalculatedBodyFat() {
  if (!form.weight) {
    calculatedBodyFat.value = null
    form.bodyFat = null
    return
  }
  const userInfo = userStore.userInfo
  if (!userInfo?.height || !userInfo?.gender) {
    calculatedBodyFat.value = null
    form.bodyFat = null
    return
  }

  const weight = form.weight
  const heightCm = userInfo.height
  const heightM = heightCm / 100
  const bmi = weight / (heightM * heightM)
  const gender = userInfo.gender === 1 ? 1 : 0 // 1=男，0=女

  // 计算年龄
  let age = 25
  if (userInfo.birthday) {
    const today = new Date()
    const birthday = new Date(userInfo.birthday)
    age = today.getFullYear() - birthday.getFullYear()
    if (today.getMonth() < birthday.getMonth() ||
        (today.getMonth() === birthday.getMonth() && today.getDate() < birthday.getDate())) {
      age--
    }
    age = Math.max(1, age)
  }

  // Deurenberg 公式
  let bodyFat = 1.2 * bmi + 0.23 * age - 10.8 * gender - 5.4
  bodyFat = Math.round(bodyFat * 10) / 10

  // 限制范围
  if (gender === 1) {
    bodyFat = Math.max(3, Math.min(25, bodyFat))
  } else {
    bodyFat = Math.max(10, Math.min(35, bodyFat))
  }

  calculatedBodyFat.value = bodyFat
  form.bodyFat = bodyFat
}

// 监听体重变化，实时计算体脂率
watch(() => form.weight, () => {
  updateCalculatedBodyFat()
  // 自动将计算结果赋值给表单
  if (calculatedBodyFat.value) {
    form.bodyFat = calculatedBodyFat.value
  }
})

const submitting = ref(false)
const submittingMeasurement = ref(false)
const trendDays = ref(30)
const trend = ref(null)
const history = ref([])
const chartRef = ref(null)
let chart = null

async function handleSubmit() {
  if (!form.weight) {
    ElMessage.warning('请输入体重')
    return
  }
  submitting.value = true
  try {
    const res = await addOrUpdateWeight(form)
    // 如果用户没有输入体脂率，显示自动计算的提示
    if (!form.bodyFat && res.data?.bodyFat) {
      ElMessage.success(`保存成功，体脂率已自动计算: ${res.data.bodyFat}%`)
    } else {
      ElMessage.success('保存成功')
    }
    // 清空表单中的体脂率，下次可以重新自动计算
    form.bodyFat = null
    await fetchTrend()
    await fetchHistory()
  } catch (e) {
    // handled
  } finally {
    submitting.value = false
  }
}

async function handleMeasurementSubmit() {
  submittingMeasurement.value = true
  try {
    await addBodyMeasurement(measurementForm)
    ElMessage.success('围度记录保存成功')
  } catch (e) {
    // handled
  } finally {
    submittingMeasurement.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除这条记录？', '提示', { type: 'warning' })
  try {
    await deleteWeight(id)
    ElMessage.success('删除成功')
    await fetchTrend()
    await fetchHistory()
  } catch (e) {
    // handled
  }
}

async function fetchTrend() {
  try {
    const res = await getWeightTrend(trendDays.value)
    trend.value = res.data
    initChart()
  } catch (e) {
    // handled
  }
}

async function fetchHistory() {
  try {
    const res = await getWeightHistory({})
    history.value = res.data
  } catch (e) {
    // handled
  }
}

function initChart() {
  if (!chartRef.value || !trend.value?.records?.length) return

  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const records = trend.value.records
  const dates = records.map(r => r.recordDate.substring(5))
  const weights = records.map(r => r.weight)
  const targetWeight = trend.value.records[0] ? null : null

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', name: 'kg', scale: true },
    series: [{
      type: 'line',
      data: weights,
      smooth: true,
      symbolSize: 8,
      itemStyle: { color: '#ff6b9d' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255,107,157,0.3)' },
          { offset: 1, color: 'rgba(255,107,157,0.05)' }
        ])
      }
    }]
  }

  chart.setOption(option)
}

onMounted(async () => {
  // 获取用户信息
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  await fetchTrend()
  await fetchHistory()

  // 初始化体脂率计算
  if (form.weight) {
    updateCalculatedBodyFat()
  }

  window.addEventListener('resize', () => chart?.resize())
})
</script>

<style scoped lang="scss">
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .card-title {
    margin: 0;
  }
}

.trend-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #edd6e6;

  .stat {
    text-align: center;

    .label {
      display: block;
      font-size: 13px;
      color: #9b8aad;
      margin-bottom: 4px;
    }

    .value {
      font-size: 18px;
      font-weight: 600;
      color: #3d2b4a;

      &.down { color: #9b7fe6; }
      &.up { color: #ff6b8a; }
    }
  }
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .card-title {
    font-size: 16px;
    margin: 0 0 16px;
    color: #3d2b4a;
  }
}

.bodyfat-display {
  width: 100%;
  padding: 10px 14px;
  background: linear-gradient(135deg, #f0f9eb, #e1f3d8);
  border-radius: 8px;
  border: 1px solid #c2e7b0;
  min-height: 40px;
  display: flex;
  align-items: center;
}

.bodyfat-value {
  display: flex;
  align-items: center;
  gap: 12px;

  .el-tag {
    font-size: 18px;
    padding: 10px 20px;
    font-weight: 600;
  }

  .bodyfat-label {
    font-size: 13px;
    color: #67c23a;
    font-weight: 500;
  }
}

.bodyfat-placeholder {
  color: #c0c4cc;
  font-size: 14px;
}
</style>
