<template>
  <div class="training-page">
    <el-row :gutter="16">
      <el-col :span="16">
        <!-- 智能推荐训练 -->
        <div class="card recommend-card">
          <div class="card-header">
            <h3 class="card-title">
              <span class="title-icon">🎯</span>
              智能推荐训练
            </h3>
            <div class="intensity-selector">
              <el-radio-group v-model="selectedIntensity" @change="fetchRecommend" size="small">
                <el-radio-button value="LOW">轻松</el-radio-button>
                <el-radio-button value="MEDIUM">适中</el-radio-button>
                <el-radio-button value="HIGH">燃脂</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <div v-if="recommendData" class="recommend-content">
            <!-- 热量概览 -->
            <div class="calorie-overview">
              <div class="calorie-item">
                <span class="calorie-label">今日目标消耗</span>
                <span class="calorie-value highlight">{{ recommendData.calorieToBurn }} kcal</span>
              </div>
              <div class="calorie-item">
                <span class="calorie-label">预计时长</span>
                <span class="calorie-value">{{ recommendData.totalDuration }} 分钟</span>
              </div>
              <div class="calorie-item">
                <span class="calorie-label">TDEE</span>
                <span class="calorie-value">{{ recommendData.tdee }} kcal</span>
              </div>
            </div>

            <!-- 推荐运动列表 -->
            <div class="recommend-exercises">
              <div v-for="exercise in recommendData.exercises" :key="exercise.exerciseTypeId"
                   class="exercise-item"
                   :class="'priority-' + exercise.priority">
                <div class="exercise-left">
                  <span class="exercise-icon">{{ getCategoryIcon(exercise.category) }}</span>
                  <div class="exercise-info">
                    <span class="exercise-name">{{ exercise.name }}</span>
                    <span class="exercise-category">{{ exercise.category }}</span>
                  </div>
                </div>
                <div class="exercise-right">
                  <div class="exercise-stats">
                    <span class="stat-duration">{{ exercise.duration }}分钟</span>
                    <span class="stat-calorie">消耗 {{ exercise.calorieBurn }} kcal</span>
                  </div>
                  <el-tag :type="getPriorityType(exercise.priority)" size="small" effect="plain">
                    {{ getPriorityLabel(exercise.priority) }}
                  </el-tag>
                </div>
              </div>
            </div>

            <!-- 一键添加按钮 -->
            <div class="recommend-actions">
              <el-button type="primary" @click="addRecommendToPlan" :loading="addingPlan">
                <el-icon><Plus /></el-icon>
                一键添加到训练计划
              </el-button>
              <el-button @click="fetchRecommend">
                <el-icon><Refresh /></el-icon>
                换一组
              </el-button>
            </div>
          </div>

          <div v-else-if="recommendLoading" class="loading-state">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <span>正在为你生成推荐...</span>
          </div>

          <div v-else class="empty-state">
            <span>请先完善个人信息以获取推荐</span>
          </div>
        </div>

        <!-- 训练计划列表 -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">我的训练计划</h3>
            <el-button type="primary" @click="openCreateDialog">新建计划</el-button>
          </div>

          <div v-if="plans.length === 0" class="empty">暂无训练计划，点击"新建计划"开始</div>
          <div v-for="plan in plans" :key="plan.id" class="plan-item" :class="{ active: plan.status === 1 }">
            <div class="plan-info">
              <span class="plan-name">{{ plan.name }}</span>
              <el-tag v-if="plan.status === 1" type="success" size="small">进行中</el-tag>
              <el-tag v-else size="small">已归档</el-tag>
            </div>
            <div v-if="plan.description" class="plan-desc">{{ plan.description }}</div>
            <div class="plan-actions">
              <el-button type="primary" link size="small" @click="viewPlanDetail(plan.id)">查看</el-button>
              <el-button type="primary" link size="small" @click="editPlan(plan.id)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDeletePlan(plan.id)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 计划详情 -->
        <div v-if="selectedPlan" class="card">
          <div class="card-header">
            <h3 class="card-title">{{ selectedPlan.name }}</h3>
            <el-button type="primary" link @click="selectedPlan = null">收起</el-button>
          </div>
          <div v-if="selectedPlan.description" class="plan-detail-desc">{{ selectedPlan.description }}</div>
          <div v-for="day in 7" :key="day" class="day-section">
            <div class="day-header">{{ dayNames[day] }}</div>
            <div v-if="getDayItems(day).length === 0" class="empty-day">休息日</div>
            <div v-for="item in getDayItems(day)" :key="item.id" class="plan-exercise">
              <span class="ex-name">{{ item.exerciseTypeName || '未知运动' }}</span>
              <span v-if="item.sets" class="ex-detail">{{ item.sets }}组</span>
              <span v-if="item.reps" class="ex-detail">{{ item.reps }}次</span>
              <span v-if="item.duration" class="ex-detail">{{ item.duration }}分钟</span>
              <span v-if="item.note" class="ex-note">{{ item.note }}</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="8">
        <!-- 今日训练 -->
        <div class="card">
          <h3 class="card-title">今日训练</h3>
          <div v-if="!todayPlan" class="empty">今日无训练安排</div>
          <template v-else>
            <div class="today-plan-name">{{ todayPlan.planName }} - {{ todayPlan.dayName }}</div>
            <div v-if="todayPlan.items && todayPlan.items.length === 0" class="empty">今日休息</div>
            <div v-for="item in todayPlan.items" :key="item.id" class="today-exercise">
              <span class="ex-name">{{ item.exerciseTypeName || '未知运动' }}</span>
              <span v-if="item.sets" class="ex-detail">{{ item.sets }}组</span>
              <span v-if="item.reps" class="ex-detail">{{ item.reps }}次</span>
              <span v-if="item.duration" class="ex-detail">{{ item.duration }}分钟</span>
            </div>
          </template>
        </div>

        <!-- 运动小贴士 -->
        <div class="card">
          <h3 class="card-title">运动小贴士</h3>
          <div class="tips-list">
            <div class="tip-item" v-for="(tip, index) in tips" :key="index">
              <span class="tip-icon">{{ tip.icon }}</span>
              <span class="tip-text">{{ tip.text }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 创建/编辑计划弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑计划' : '新建计划'" width="700px">
      <el-form :model="planForm" label-width="100px">
        <el-form-item label="计划名称">
          <el-input v-model="planForm.name" placeholder="如：增肌计划、减脂计划" />
        </el-form-item>
        <el-form-item label="计划描述">
          <el-input v-model="planForm.description" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
        <el-divider>训练安排</el-divider>
        <div v-for="day in 7" :key="day" class="form-day-section">
          <div class="form-day-header">{{ dayNames[day] }}</div>
          <div v-for="(item, idx) in getFormItemDay(day)" :key="idx" class="form-exercise-row">
            <el-select v-model="item.exerciseTypeId" placeholder="选择运动" style="width: 160px" size="small">
              <el-option-group v-for="group in exerciseTypeGroups" :key="group.label" :label="group.label">
                <el-option v-for="et in group.options" :key="et.id" :label="et.name" :value="et.id" />
              </el-option-group>
            </el-select>
            <el-input-number v-model="item.sets" :min="1" placeholder="组数" size="small" style="width: 100px" controls-position="right" />
            <el-input-number v-model="item.reps" :min="1" placeholder="次数" size="small" style="width: 100px" controls-position="right" />
            <el-input-number v-model="item.duration" :min="1" placeholder="时长" size="small" style="width: 100px" controls-position="right" />
            <el-input v-model="item.note" placeholder="备注" size="small" style="width: 120px" />
            <el-button type="danger" link size="small" @click="removeFormItem(day, idx)">删除</el-button>
          </div>
          <el-button type="primary" link size="small" @click="addFormItem(day)">+ 添加运动</el-button>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ isEdit ? '更新' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import {
  getTrainingPlans, getTrainingPlanDetail, createTrainingPlan,
  updateTrainingPlan, deleteTrainingPlan, getTodayPlan,
  getRecommendedTraining
} from '../../api/trainingPlan'
import { getExerciseTypes } from '../../api/exercise'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Loading } from '@element-plus/icons-vue'

const dayNames = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }
const plans = ref([])
const selectedPlan = ref(null)
const todayPlan = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const exerciseTypes = ref([])

// 推荐训练相关
const selectedIntensity = ref('MEDIUM')
const recommendData = ref(null)
const recommendLoading = ref(false)
const addingPlan = ref(false)

// 运动小贴士
const tips = [
  { icon: '💧', text: '运动前后记得补充水分' },
  { icon: '🔥', text: '先热身5分钟再开始运动' },
  { icon: '😴', text: '运动后充分拉伸放松' },
  { icon: '⏰', text: '建议每次运动30-60分钟' }
]

const planForm = reactive({
  name: '',
  description: '',
  items: []
})

const exerciseTypeGroups = computed(() => {
  const groups = {}
  exerciseTypes.value.forEach(et => {
    if (!groups[et.category]) groups[et.category] = []
    groups[et.category].push(et)
  })
  return Object.entries(groups).map(([label, options]) => ({ label, options }))
})

function getDayItems(day) {
  if (!selectedPlan.value || !selectedPlan.value.items) return []
  return selectedPlan.value.items.filter(i => i.dayOfWeek === day)
}

function getFormItemDay(day) {
  return planForm.items.filter(i => i.dayOfWeek === day)
}

function addFormItem(day) {
  planForm.items.push({ dayOfWeek: day, exerciseTypeId: null, sets: null, reps: null, duration: null, note: '' })
}

function removeFormItem(day, idx) {
  const dayItems = planForm.items.filter(i => i.dayOfWeek === day)
  const globalIdx = planForm.items.indexOf(dayItems[idx])
  if (globalIdx !== -1) planForm.items.splice(globalIdx, 1)
}

function openCreateDialog() {
  isEdit.value = false
  editingId.value = null
  planForm.name = ''
  planForm.description = ''
  planForm.items = []
  dialogVisible.value = true
}

async function editPlan(id) {
  isEdit.value = true
  editingId.value = id
  try {
    const res = await getTrainingPlanDetail(id)
    const plan = res.data
    planForm.name = plan.name
    planForm.description = plan.description || ''
    planForm.items = (plan.items || []).map(i => ({
      dayOfWeek: i.dayOfWeek,
      exerciseTypeId: i.exerciseTypeId,
      sets: i.sets,
      reps: i.reps,
      duration: i.duration,
      note: i.note || ''
    }))
    dialogVisible.value = true
  } catch (e) {}
}

async function handleSubmit() {
  if (!planForm.name) {
    ElMessage.warning('请输入计划名称')
    return
  }
  submitLoading.value = true
  try {
    const data = {
      name: planForm.name,
      description: planForm.description,
      items: planForm.items.filter(i => i.exerciseTypeId)
    }
    if (isEdit.value) {
      await updateTrainingPlan(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createTrainingPlan(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchPlans()
  } catch (e) {} finally {
    submitLoading.value = false
  }
}

async function viewPlanDetail(id) {
  try {
    const res = await getTrainingPlanDetail(id)
    selectedPlan.value = res.data
  } catch (e) {}
}

async function handleDeletePlan(id) {
  await ElMessageBox.confirm('确定删除该训练计划？', '提示', { type: 'warning' })
  try {
    await deleteTrainingPlan(id)
    ElMessage.success('删除成功')
    if (selectedPlan.value?.id === id) selectedPlan.value = null
    await fetchPlans()
  } catch (e) {}
}

async function fetchPlans() {
  try {
    const res = await getTrainingPlans()
    plans.value = res.data
  } catch (e) {}
}

async function fetchTodayPlan() {
  try {
    const res = await getTodayPlan()
    todayPlan.value = res.data
  } catch (e) {}
}

async function fetchExerciseTypes() {
  try {
    const res = await getExerciseTypes()
    exerciseTypes.value = res.data
  } catch (e) {}
}

// 获取推荐训练
async function fetchRecommend() {
  recommendLoading.value = true
  recommendData.value = null
  try {
    const res = await getRecommendedTraining(selectedIntensity.value)
    recommendData.value = res.data
  } catch (e) {
    console.error('获取推荐训练失败', e)
  } finally {
    recommendLoading.value = false
  }
}

// 一键添加推荐到训练计划
async function addRecommendToPlan() {
  if (!recommendData.value || !recommendData.value.exercises.length) {
    ElMessage.warning('暂无推荐运动')
    return
  }

  addingPlan.value = true
  try {
    const today = new Date().getDay() || 7 // 1=周一, 7=周日
    const items = recommendData.value.exercises.map(ex => ({
      dayOfWeek: today,
      exerciseTypeId: ex.exerciseTypeId,
      sets: null,
      reps: null,
      duration: ex.duration,
      note: `${ex.category} - 预计消耗${ex.calorieBurn}kcal`
    }))

    const data = {
      name: `智能推荐训练 (${new Date().toLocaleDateString()})`,
      description: `目标消耗 ${recommendData.value.calorieToBurn} kcal，预计时长 ${recommendData.value.totalDuration} 分钟`,
      items: items
    }

    await createTrainingPlan(data)
    ElMessage.success('已添加到训练计划')
    await fetchPlans()
  } catch (e) {
    ElMessage.error('添加失败')
  } finally {
    addingPlan.value = false
  }
}

// 获取分类图标
function getCategoryIcon(category) {
  if (!category) return '🏃'
  if (category.includes('有氧') || category.includes('跑步') || category.includes('游泳')) return '🏃'
  if (category.includes('力量') || category.includes('器械')) return '💪'
  if (category.includes('拉伸') || category.includes('瑜伽')) return '🧘'
  if (category.includes('跳绳')) return '⚡'
  return '🏃'
}

// 获取优先级类型
function getPriorityType(priority) {
  if (priority === 1) return 'danger'
  if (priority === 2) return 'warning'
  return 'info'
}

// 获取优先级标签
function getPriorityLabel(priority) {
  if (priority === 1) return '必选'
  if (priority === 2) return '推荐'
  return '可选'
}

onMounted(async () => {
  await Promise.all([fetchPlans(), fetchTodayPlan(), fetchExerciseTypes()])
  await fetchRecommend()
})
</script>

<style scoped lang="scss">
.training-page {
  .card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      .card-title { margin: 0; }
    }

    .card-title {
      font-size: 16px;
      margin: 0 0 16px;
      color: #3d2b4a;
      display: flex;
      align-items: center;
      gap: 8px;

      .title-icon {
        font-size: 20px;
      }
    }
  }
}

// 推荐训练卡片
.recommend-card {
  background: linear-gradient(135deg, #fff 0%, #f8f0ff 100%);
  border: 2px solid rgba(155, 127, 230, 0.2);

  .intensity-selector {
    :deep(.el-radio-button__inner) {
      border-color: rgba(155, 127, 230, 0.3);
    }
    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      background-color: #9b7fe6;
      border-color: #9b7fe6;
    }
  }
}

.recommend-content {
  .calorie-overview {
    display: flex;
    justify-content: space-around;
    padding: 16px;
    background: linear-gradient(135deg, #f8f0ff, #fff0f5);
    border-radius: 12px;
    margin-bottom: 20px;

    .calorie-item {
      text-align: center;

      .calorie-label {
        display: block;
        font-size: 12px;
        color: #9b8aad;
        margin-bottom: 6px;
      }

      .calorie-value {
        font-size: 20px;
        font-weight: 600;
        color: #3d2b4a;

        &.highlight {
          color: #ff6b9d;
          font-size: 24px;
        }
      }
    }
  }

  .recommend-exercises {
    margin-bottom: 20px;

    .exercise-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 14px 16px;
      background: #fff;
      border-radius: 10px;
      margin-bottom: 10px;
      border: 1px solid rgba(155, 127, 230, 0.15);
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(155, 127, 230, 0.15);
        transform: translateY(-2px);
      }

      &.priority-1 {
        border-left: 4px solid #ff6b9d;
      }
      &.priority-2 {
        border-left: 4px solid #ffb347;
      }
      &.priority-3 {
        border-left: 4px solid #9b7fe6;
      }

      .exercise-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .exercise-icon {
          font-size: 28px;
        }

        .exercise-info {
          .exercise-name {
            display: block;
            font-size: 15px;
            font-weight: 600;
            color: #3d2b4a;
          }

          .exercise-category {
            font-size: 12px;
            color: #9b8aad;
          }
        }
      }

      .exercise-right {
        display: flex;
        align-items: center;
        gap: 16px;

        .exercise-stats {
          text-align: right;

          .stat-duration {
            display: block;
            font-size: 14px;
            color: #6b5a7a;
          }

          .stat-calorie {
            font-size: 12px;
            color: #ff6b9d;
          }
        }
      }
    }
  }

  .recommend-actions {
    display: flex;
    justify-content: center;
    gap: 16px;

    .el-button--primary {
      background: linear-gradient(135deg, #9b7fe6, #da70d6);
      border: none;
      padding: 12px 28px;
      font-size: 15px;

      &:hover {
        background: linear-gradient(135deg, #8a6fe6, #c960d6);
      }
    }
  }
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 40px;
  color: #9b8aad;

  .loading-icon {
    font-size: 24px;
    margin-right: 8px;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 训练计划列表
.empty { text-align: center; color: #9b8aad; padding: 20px; font-size: 13px; }

.plan-item {
  padding: 12px 0;
  border-bottom: 1px solid #edd6e6;
  &:last-child { border-bottom: none; }

  .plan-info {
    display: flex;
    align-items: center;
    gap: 8px;
    .plan-name { font-size: 15px; font-weight: 600; color: #3d2b4a; }
  }

  .plan-desc { font-size: 13px; color: #9b8aad; margin: 4px 0; }
  .plan-actions { display: flex; gap: 8px; margin-top: 8px; }
}

.plan-detail-desc { font-size: 13px; color: #6b5a7a; margin-bottom: 16px; }

.day-section {
  margin-bottom: 12px;
  .day-header {
    font-size: 14px;
    font-weight: 600;
    color: #3d2b4a;
    padding: 8px 0;
    border-bottom: 1px solid #f3e4ee;
  }
  .empty-day { font-size: 12px; color: #9b8aad; padding: 8px 0; }
}

.plan-exercise, .today-exercise {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  .ex-name { font-size: 14px; color: #3d2b4a; }
  .ex-detail { font-size: 12px; color: #9b7fe6; background: #f3e4ee; padding: 2px 6px; border-radius: 4px; }
  .ex-note { font-size: 12px; color: #9b8aad; }
}

.today-plan-name { font-size: 14px; font-weight: 600; color: #3d2b4a; margin-bottom: 12px; }

// 运动小贴士
.tips-list {
  .tip-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid #f8f0ff;

    &:last-child { border-bottom: none; }

    .tip-icon {
      font-size: 20px;
    }

    .tip-text {
      font-size: 13px;
      color: #6b5a7a;
    }
  }
}

// 表单相关
.form-day-section {
  margin-bottom: 16px;
  .form-day-header {
    font-size: 14px;
    font-weight: 600;
    color: #3d2b4a;
    margin-bottom: 8px;
  }
  .form-exercise-row {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
    align-items: center;
    flex-wrap: wrap;
  }
}
</style>
