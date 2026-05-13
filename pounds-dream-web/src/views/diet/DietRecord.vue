<template>
  <div class="diet-page">
    <el-row :gutter="16">
      <el-col :span="16">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">饮食记录</h3>
            <div class="header-actions">
              <el-button size="small" @click="templateDialogVisible = true">从模版录入</el-button>
              <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" @change="fetchDiet" />
            </div>
          </div>

          <div v-for="(records, key) in dietData.mealsByType" :key="key" class="meal-section">
            <div class="meal-header">
              <span class="meal-name">{{ mealNames[key] }}</span>
              <span class="meal-cal">{{ getMealCal(key) }} kcal</span>
              <el-button type="info" link size="small" @click="handleSkipMeal(key)" :disabled="records.length > 0">跳过</el-button>
              <el-button type="primary" link size="small" @click="saveAsTemplate(key)" :disabled="!records.length">存为模版</el-button>
              <el-button type="primary" link @click="openAddDialog(key)">+ 添加</el-button>
            </div>
            <div v-if="records.length === 0" class="empty-meal">暂无记录</div>
            <div v-for="item in records" :key="item.id" class="food-item">
              <div class="food-info">
                <span class="food-name">{{ item.foodName }}</span>
                <span class="food-amount">{{ item.amount }}g</span>
                <template v-if="item.riskTags && item.riskTags.length">
                  <el-tag v-for="tag in item.riskTags" :key="tag" size="small" :type="item.riskLevel === 'HIGH' ? 'danger' : 'warning'" class="risk-tag">
                    {{ tag }}
                  </el-tag>
                </template>
              </div>
              <div class="food-nutrition">
                <span class="cal">{{ item.calorie }} kcal</span>
                <span class="macro">P: {{ item.protein }}g</span>
                <span class="macro">F: {{ item.fat }}g</span>
                <span class="macro">C: {{ item.carbohydrate }}g</span>
              </div>
              <el-button type="primary" link size="small" @click="openAlternatives(item.foodId)">换一个</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(item.id)">删除</el-button>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="card">
          <h3 class="card-title">今日热量</h3>
          <div ref="calorieChartRef" style="height: 250px"></div>
          <div class="calorie-summary">
            <div class="cal-item">
              <span class="label">已摄入</span>
              <span class="value">{{ dietData.totalCalorie || 0 }} kcal</span>
            </div>
            <div class="cal-item">
              <span class="label">目标</span>
              <span class="value">{{ dietData.targetCalorie || 1800 }} kcal</span>
            </div>
            <div class="cal-item">
              <span class="label">剩余</span>
              <span class="value" :class="(dietData.remainingCalorie || 0) >= 0 ? 'ok' : 'over'">
                {{ dietData.remainingCalorie || 0 }} kcal
              </span>
            </div>
          </div>
        </div>

        <div class="card">
          <h3 class="card-title">营养素</h3>
          <div class="macro-bars">
            <div class="macro-item">
              <span class="label">蛋白质</span>
              <el-progress :percentage="getMacroPercent('protein')" color="#ff6b9d" />
              <span class="value">{{ dietData.totalProtein || 0 }}g</span>
            </div>
            <div class="macro-item">
              <span class="label">脂肪</span>
              <el-progress :percentage="getMacroPercent('fat')" color="#ffb347" />
              <span class="value">{{ dietData.totalFat || 0 }}g</span>
            </div>
            <div class="macro-item">
              <span class="label">碳水</span>
              <el-progress :percentage="getMacroPercent('carb')" color="#9b7fe6" />
              <span class="value">{{ dietData.totalCarbohydrate || 0 }}g</span>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3 class="card-title">AI 饮食推荐</h3>
            <el-button type="primary" link size="small" @click="fetchRecommendations" :loading="recommendLoading">刷新</el-button>
          </div>
          <div v-if="recommendations.length === 0" class="empty-meal">点击刷新获取推荐</div>
          <div v-for="rec in recommendations" :key="rec.id" class="recommend-item">
            <div class="rec-info">
              <span class="rec-name">{{ rec.name }}</span>
              <span class="rec-cal">{{ rec.calorie }}kcal/100g</span>
            </div>
            <div class="rec-reason">{{ rec.reason }}</div>
            <el-button type="primary" link size="small" @click="quickAddFood(rec.id)">+ 添加</el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 添加食物弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加食物" width="500px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="搜索食物">
          <el-select
            v-model="addForm.foodId"
            filterable
            remote
            :remote-method="searchFood"
            placeholder="输入食物名称搜索"
            style="width: 100%"
            :loading="searchLoading"
          >
            <el-option
              v-for="food in foodList"
              :key="food.id"
              :label="`${food.name} (${food.calorie}kcal/100g)`"
              :value="food.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="食用量(g)">
          <el-input-number v-model="addForm.amount" :min="1" :max="5000" :step="10" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="handleAddFood">确定</el-button>
      </template>
    </el-dialog>

    <!-- 食物替代弹窗 -->
    <el-dialog v-model="altDialogVisible" title="替代建议" width="550px">
      <div v-if="alternatives.length === 0" class="empty-meal">暂无替代建议</div>
      <div v-for="alt in alternatives" :key="alt.id" class="alt-item">
        <div class="alt-info">
          <span class="alt-name">{{ alt.name }}</span>
          <span class="alt-cal">{{ alt.calorie }}kcal/100g</span>
        </div>
        <div class="alt-reason">{{ alt.reason }}</div>
        <el-button type="primary" size="small" @click="replaceWithAlternative(alt.id)">替换</el-button>
      </div>
    </el-dialog>

    <!-- 饮食模版弹窗 -->
    <el-dialog v-model="templateDialogVisible" title="饮食模版" width="550px">
      <div v-if="templates.length === 0" class="empty-meal">暂无模版，可在饮食记录中保存模版</div>
      <div v-for="tpl in templates" :key="tpl.id" class="template-item">
        <div class="tpl-info">
          <span class="tpl-name">{{ tpl.name }}</span>
          <span class="tpl-meal">{{ mealNames[tpl.mealTypeKey] || '' }}</span>
          <span class="tpl-cal">{{ tpl.totalCalorie }} kcal</span>
        </div>
        <div class="tpl-foods">
          <span v-for="(f, i) in tpl.items" :key="i" class="tpl-food-tag">{{ f.foodName }} {{ f.amount }}g</span>
        </div>
        <div class="tpl-actions">
          <el-button type="primary" size="small" @click="handleApplyTemplate(tpl.id)">一键录入</el-button>
          <el-button type="danger" link size="small" @click="handleDeleteTemplate(tpl.id)">删除</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getDailyDiet, addDietRecord, deleteDietRecord,
  searchFood as searchFoodApi, getAlternatives as getAlternativesApi,
  getRecommendations as getRecommendationsApi,
  getDietTemplates, saveDietTemplate, deleteDietTemplate, applyDietTemplate,
  skipMeal
} from '../../api/diet'
import { ElMessage, ElMessageBox } from 'element-plus'

const selectedDate = ref(new Date().toISOString().split('T')[0])
const dietData = ref({
  totalCalorie: 0,
  targetCalorie: 1800,
  remainingCalorie: 1800,
  totalProtein: 0,
  totalFat: 0,
  totalCarbohydrate: 0,
  mealsByType: { breakfast: [], lunch: [], dinner: [], snack: [] }
})

const mealNames = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }
const mealTypeMap = { breakfast: 1, lunch: 2, dinner: 3, snack: 4 }
const addDialogVisible = ref(false)
const addLoading = ref(false)
const searchLoading = ref(false)
const foodList = ref([])
const calorieChartRef = ref(null)
let calorieChart = null

// 营养避雷针 & 替代
const altDialogVisible = ref(false)
const alternatives = ref([])

// AI 推荐
const recommendations = ref([])
const recommendLoading = ref(false)

// 饮食模版
const templateDialogVisible = ref(false)
const templates = ref([])

const addForm = reactive({
  mealType: 1,
  foodId: null,
  amount: 100
})

function getMealCal(key) {
  const records = dietData.value.mealsByType[key] || []
  return records.reduce((sum, r) => sum + r.calorie, 0)
}

function getMacroPercent(type) {
  const target = dietData.value.targetCalorie || 1800
  const ratio = { protein: 0.3, fat: 0.3, carb: 0.4 }
  const calPerGram = { protein: 4, fat: 9, carb: 4 }
  const targetGram = (target * ratio[type]) / calPerGram[type]
  const current = { protein: dietData.value.totalProtein, fat: dietData.value.totalFat, carb: dietData.value.totalCarbohydrate }
  return Math.min(100, Math.round(((current[type] || 0) / targetGram) * 100))
}

function openAddDialog(mealTypeKey) {
  addForm.mealType = mealTypeMap[mealTypeKey]
  addForm.foodId = null
  addForm.amount = 100
  foodList.value = []
  addDialogVisible.value = true
}

async function searchFood(keyword) {
  if (!keyword) return
  searchLoading.value = true
  try {
    const res = await searchFoodApi(keyword)
    foodList.value = res.data
  } catch (e) {} finally {
    searchLoading.value = false
  }
}

async function handleAddFood() {
  if (!addForm.foodId) {
    ElMessage.warning('请选择食物')
    return
  }
  addLoading.value = true
  try {
    await addDietRecord({
      foodId: addForm.foodId,
      mealType: addForm.mealType,
      amount: addForm.amount,
      recordDate: selectedDate.value
    })
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    await fetchDiet()
  } catch (e) {} finally {
    addLoading.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除这条记录？', '提示', { type: 'warning' })
  try {
    await deleteDietRecord(id)
    ElMessage.success('删除成功')
    await fetchDiet()
  } catch (e) {}
}

async function fetchDiet() {
  try {
    const res = await getDailyDiet(selectedDate.value)
    dietData.value = res.data
    await nextTick()
    initCalorieChart()
  } catch (e) {}
}

// 食物替代
async function openAlternatives(foodId) {
  try {
    const res = await getAlternativesApi(foodId, 5)
    alternatives.value = res.data
    altDialogVisible.value = true
  } catch (e) {}
}

async function replaceWithAlternative(newFoodId) {
  addForm.foodId = newFoodId
  addForm.amount = 100
  altDialogVisible.value = false
  addDialogVisible.value = true
}

// AI 推荐
async function fetchRecommendations() {
  recommendLoading.value = true
  try {
    const res = await getRecommendationsApi()
    recommendations.value = res.data
  } catch (e) {} finally {
    recommendLoading.value = false
  }
}

async function quickAddFood(foodId) {
  try {
    await addDietRecord({
      foodId,
      mealType: 4,
      amount: 100,
      recordDate: selectedDate.value
    })
    ElMessage.success('添加成功')
    await fetchDiet()
  } catch (e) {}
}

// 跳过某餐
async function handleSkipMeal(mealTypeKey) {
  await ElMessageBox.confirm(`确定跳过${mealNames[mealTypeKey]}？`, '提示', { type: 'info' })
  try {
    await skipMeal(mealTypeMap[mealTypeKey], selectedDate.value)
    ElMessage.success('已跳过')
  } catch (e) {}
}

// 饮食模版
async function fetchTemplates() {
  try {
    const res = await getDietTemplates()
    templates.value = res.data
  } catch (e) {}
}

async function saveAsTemplate(mealTypeKey) {
  const { value: name } = await ElMessageBox.prompt('请输入模版名称', '保存模版', {
    inputPlaceholder: '如：我的早餐组合',
    confirmButtonText: '保存',
    cancelButtonText: '取消'
  }).catch(() => ({ value: null }))
  if (!name) return
  try {
    await saveDietTemplate({ name, mealType: mealTypeMap[mealTypeKey] })
    ElMessage.success('模版保存成功')
    await fetchTemplates()
  } catch (e) {}
}

async function handleApplyTemplate(templateId) {
  try {
    await applyDietTemplate(templateId, selectedDate.value)
    ElMessage.success('模版应用成功')
    templateDialogVisible.value = false
    await fetchDiet()
  } catch (e) {}
}

async function handleDeleteTemplate(id) {
  await ElMessageBox.confirm('确定删除该模版？', '提示', { type: 'warning' })
  try {
    await deleteDietTemplate(id)
    ElMessage.success('模版删除成功')
    await fetchTemplates()
  } catch (e) {}
}

function initCalorieChart() {
  if (!calorieChartRef.value) return
  if (!calorieChart) {
    calorieChart = echarts.init(calorieChartRef.value)
  }
  const intake = dietData.value.totalCalorie || 0
  const target = dietData.value.targetCalorie || 1800
  const remaining = Math.max(0, target - intake)
  calorieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} kcal ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['50%', '70%'],
      label: { show: true, position: 'center', formatter: `${intake}\nkcal`, fontSize: 16, fontWeight: 'bold' },
      data: [
        { value: intake, name: '已摄入', itemStyle: { color: '#ff6b9d' } },
        { value: remaining, name: '剩余', itemStyle: { color: '#f3e4ee' } }
      ]
    }]
  })
}

onMounted(async () => {
  await fetchDiet()
  await fetchTemplates()
  window.addEventListener('resize', () => calorieChart?.resize())
})
</script>

<style scoped lang="scss">
.card {
  background: #fff;
  border-radius: 8px;
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
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meal-section {
  margin-bottom: 20px;
  border-bottom: 1px solid #edd6e6;
  padding-bottom: 16px;
  &:last-child { border-bottom: none; margin-bottom: 0; }

  .meal-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
    .meal-name { font-size: 16px; font-weight: 600; color: #3d2b4a; }
    .meal-cal { font-size: 14px; color: #9b8aad; flex: 1; }
  }

  .empty-meal { text-align: center; color: #9b8aad; padding: 12px; font-size: 13px; }

  .food-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    gap: 12px;

    .food-info {
      flex: 1;
      .food-name { font-size: 14px; color: #3d2b4a; }
      .food-amount { font-size: 12px; color: #9b8aad; margin-left: 8px; }
      .risk-tag { margin-left: 4px; font-size: 11px; }
    }

    .food-nutrition {
      display: flex;
      gap: 8px;
      font-size: 12px;
      color: #9b8aad;
      .cal { color: #ff6b9d; font-weight: 600; }
    }
  }
}

.calorie-summary {
  margin-top: 16px;
  .cal-item {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid #f3e4ee;
    &:last-child { border-bottom: none; }
    .label { color: #9b8aad; }
    .value { font-weight: 600; color: #3d2b4a; &.ok { color: #9b7fe6; } &.over { color: #ff6b8a; } }
  }
}

.macro-bars {
  .macro-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    .label { width: 50px; font-size: 14px; color: #6b5a7a; }
    .el-progress { flex: 1; }
    .value { width: 50px; text-align: right; font-size: 14px; color: #3d2b4a; }
  }
}

.recommend-item {
  padding: 8px 0;
  border-bottom: 1px solid #f3e4ee;
  &:last-child { border-bottom: none; }
  .rec-info {
    display: flex;
    justify-content: space-between;
    .rec-name { font-size: 14px; color: #3d2b4a; }
    .rec-cal { font-size: 12px; color: #9b8aad; }
  }
  .rec-reason { font-size: 12px; color: #9b7fe6; margin: 4px 0; }
}

.alt-item, .template-item {
  padding: 12px 0;
  border-bottom: 1px solid #f3e4ee;
  &:last-child { border-bottom: none; }
}

.alt-item {
  .alt-info {
    display: flex;
    justify-content: space-between;
    .alt-name { font-size: 14px; color: #3d2b4a; font-weight: 600; }
    .alt-cal { font-size: 12px; color: #9b8aad; }
  }
  .alt-reason { font-size: 12px; color: #9b7fe6; margin: 4px 0 8px; }
}

.template-item {
  .tpl-info {
    display: flex;
    align-items: center;
    gap: 8px;
    .tpl-name { font-size: 14px; color: #3d2b4a; font-weight: 600; }
    .tpl-meal { font-size: 12px; color: #9b7fe6; }
    .tpl-cal { font-size: 12px; color: #9b8aad; margin-left: auto; }
  }
  .tpl-foods {
    margin: 8px 0;
    .tpl-food-tag {
      display: inline-block;
      background: #fef6f9;
      border: 1px solid #edd6e6;
      border-radius: 4px;
      padding: 2px 8px;
      font-size: 12px;
      color: #6b5a7a;
      margin: 2px 4px 2px 0;
    }
  }
  .tpl-actions { display: flex; gap: 8px; }
}
</style>
