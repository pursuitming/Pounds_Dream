<template>
  <div class="exercise-page">
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="card">
          <h3 class="card-title">添加运动</h3>
          <el-form :model="form" label-width="80px">
            <el-form-item label="日期">
              <el-date-picker v-model="form.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="运动类型">
              <el-select v-model="form.exerciseTypeId" placeholder="选择运动" style="width: 100%">
                <el-option-group v-for="group in groupedTypes" :key="group.category" :label="group.category">
                  <el-option v-for="type in group.types" :key="type.id" :label="`${type.name} (MET: ${type.metValue})`" :value="type.id" />
                </el-option-group>
              </el-select>
            </el-form-item>
            <el-form-item label="时长(分钟)">
              <el-input-number v-model="form.duration" :min="1" :max="600" :step="5" style="width: 100%" />
            </el-form-item>
            <el-form-item label="心率(可选)">
              <el-input-number v-model="form.heartRate" :min="40" :max="220" style="width: 100%" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.note" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
            </el-form-item>
          </el-form>

          <div class="calorie-preview" v-if="selectedType && form.duration">
            <p>预计消耗: <strong>{{ estimatedCalorie }}</strong> kcal</p>
            <p class="formula">计算公式: MET({{ selectedType.metValue }}) × 体重 × 时长({{ form.duration }}分钟)</p>
          </div>
        </div>
      </el-col>

      <el-col :span="16">
        <div class="card">
          <h3 class="card-title">今日运动</h3>
          <div class="total-burned">
            总消耗: <strong>{{ totalBurned }}</strong> kcal
          </div>

          <div v-if="records.length === 0" class="empty">今日暂无运动记录</div>

          <div v-for="item in records" :key="item.id" class="exercise-item">
            <div class="exercise-icon" :style="{ background: getCategoryColor(item.exerciseCategory) }">
              {{ getCategoryIcon(item.exerciseCategory) }}
            </div>
            <div class="exercise-info">
              <h4>{{ item.exerciseName }}</h4>
              <p>{{ item.duration }} 分钟 · {{ item.exerciseCategory }}</p>
            </div>
            <div class="exercise-cal">
              <span class="cal">-{{ item.calorieBurned }} kcal</span>
              <span v-if="item.heartRate" class="hr">❤️ {{ item.heartRate }} bpm</span>
            </div>
            <el-button type="danger" link @click="handleDelete(item.id)">删除</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { addExerciseRecord, getTodayExercise, deleteExerciseRecord, getExerciseTypes } from '../../api/exercise'
import { ElMessage, ElMessageBox } from 'element-plus'

const form = reactive({
  recordDate: new Date().toISOString().split('T')[0],
  exerciseTypeId: null,
  duration: 30,
  heartRate: null,
  note: ''
})

const submitting = ref(false)
const records = ref([])
const exerciseTypes = ref([])

const groupedTypes = computed(() => {
  const groups = {}
  exerciseTypes.value.forEach(t => {
    if (!groups[t.category]) {
      groups[t.category] = { category: t.category, types: [] }
    }
    groups[t.category].types.push(t)
  })
  return Object.values(groups)
})

const selectedType = computed(() => {
  return exerciseTypes.value.find(t => t.id === form.exerciseTypeId)
})

const estimatedCalorie = computed(() => {
  if (!selectedType.value || !form.duration) return 0
  return Math.round(selectedType.value.metValue * 65 * (form.duration / 60))
})

const totalBurned = computed(() => {
  return records.value.reduce((sum, r) => sum + r.calorieBurned, 0)
})

function getCategoryColor(category) {
  const colors = { '有氧': '#ff6b9d', '无氧': '#ff6b8a', '柔韧': '#9b7fe6', '球类': '#ffb347' }
  return colors[category] || '#b8a9c9'
}

function getCategoryIcon(category) {
  const icons = { '有氧': '🏃', '无氧': '💪', '柔韧': '🧘', '球类': '⚽' }
  return icons[category] || '🏋️'
}

async function handleSubmit() {
  if (!form.exerciseTypeId) {
    ElMessage.warning('请选择运动类型')
    return
  }
  submitting.value = true
  try {
    await addExerciseRecord(form)
    ElMessage.success('保存成功')
    await fetchRecords()
  } catch (e) {
    // handled
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除这条记录？', '提示', { type: 'warning' })
  try {
    await deleteExerciseRecord(id)
    ElMessage.success('删除成功')
    await fetchRecords()
  } catch (e) {
    // handled
  }
}

async function fetchRecords() {
  try {
    const res = await getTodayExercise()
    records.value = res.data
  } catch (e) {
    // handled
  }
}

async function fetchTypes() {
  try {
    const res = await getExerciseTypes()
    exerciseTypes.value = res.data
  } catch (e) {
    // handled
  }
}

onMounted(async () => {
  await fetchTypes()
  await fetchRecords()
})
</script>

<style scoped lang="scss">
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

.calorie-preview {
  margin-top: 16px;
  padding: 12px;
  background: #fdf2f6;
  border-radius: 8px;

  p {
    margin: 4px 0;
    font-size: 14px;
    color: #6b5a7a;

    strong {
      color: #ff6b9d;
      font-size: 20px;
    }
  }

  .formula {
    font-size: 12px;
    color: #9b8aad;
  }
}

.total-burned {
  font-size: 16px;
  color: #6b5a7a;
  margin-bottom: 16px;

  strong {
    color: #ff6b8a;
    font-size: 24px;
  }
}

.empty {
  text-align: center;
  color: #9b8aad;
  padding: 40px;
}

.exercise-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #edd6e6;
  gap: 12px;

  &:last-child {
    border-bottom: none;
  }

  .exercise-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }

  .exercise-info {
    flex: 1;

    h4 {
      margin: 0;
      font-size: 15px;
      color: #3d2b4a;
    }

    p {
      margin: 4px 0 0;
      font-size: 13px;
      color: #9b8aad;
    }
  }

  .exercise-cal {
    text-align: right;

    .cal {
      display: block;
      font-size: 16px;
      font-weight: 600;
      color: #ff6b8a;
    }

    .hr {
      font-size: 12px;
      color: #9b8aad;
    }
  }
}
</style>
