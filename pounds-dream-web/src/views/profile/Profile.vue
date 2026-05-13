<template>
  <div class="profile-page">
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="card">
          <h3 class="card-title">个人信息</h3>
          <el-form :model="form" label-width="100px">
            <el-form-item label="用户名">
              <el-input :value="userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="生日">
              <el-date-picker v-model="form.birthday" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="身高(cm)">
              <el-input-number v-model="form.height" :min="100" :max="250" :precision="1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="card">
          <h3 class="card-title">目标设置</h3>
          <el-form :model="form" label-width="100px">
            <el-form-item label="目标体重(kg)">
              <el-input-number v-model="form.targetWeight" :min="30" :max="200" :precision="1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="每日热量(kcal)">
              <el-input-number v-model="form.targetCalorie" :min="1000" :max="5000" :step="50" style="width: 100%" />
            </el-form-item>
            <el-form-item label="活动量">
              <el-select v-model="form.activityLevel" style="width: 100%">
                <el-option label="久坐不动" :value="1" />
                <el-option label="轻度活动" :value="2" />
                <el-option label="中度活动" :value="3" />
                <el-option label="重度活动" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card">
          <h3 class="card-title">健康画像</h3>
          <div v-if="healthProfile" class="health-info">
            <div class="health-item">
              <span class="label">BMR (基础代谢)</span>
              <span class="value">{{ healthProfile.bmr }} kcal</span>
            </div>
            <div class="health-item">
              <span class="label">TDEE (每日消耗)</span>
              <span class="value">{{ healthProfile.tdee }} kcal</span>
            </div>
            <div class="health-item">
              <span class="label">推荐摄入</span>
              <span class="value">{{ healthProfile.targetCalorie }} kcal</span>
            </div>
            <div class="health-item">
              <span class="label">BMI</span>
              <span class="value" :style="{ color: bmiColor }">{{ healthProfile.bmi }} ({{ healthProfile.bmiCategory }})</span>
            </div>
            <el-divider />
            <h4>营养素建议</h4>
            <div class="health-item">
              <span class="label">碳水化合物</span>
              <span class="value">{{ healthProfile.carbGram }}g</span>
            </div>
            <div class="health-item">
              <span class="label">蛋白质</span>
              <span class="value">{{ healthProfile.proteinGram }}g</span>
            </div>
            <div class="health-item">
              <span class="label">脂肪</span>
              <span class="value">{{ healthProfile.fatGram }}g</span>
            </div>
          </div>
          <el-empty v-else description="请先完善个人信息" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/modules/user'
import { updateProfile, getHealthProfile } from '../../api/user'
import { ElMessage } from 'element-plus'
import { getBMIColor } from '../../utils/format'

const userStore = useUserStore()
const saving = ref(false)
const healthProfile = ref(null)

const userInfo = computed(() => userStore.userInfo)

const form = reactive({
  nickname: '',
  gender: 0,
  birthday: '',
  height: null,
  targetWeight: null,
  targetCalorie: null,
  activityLevel: 2,
  phone: '',
  email: ''
})

const bmiColor = computed(() => {
  if (!healthProfile.value) return '#9b8aad'
  return getBMIColor(healthProfile.value.bmi)
})

function initForm() {
  if (userInfo.value) {
    form.nickname = userInfo.value.nickname || ''
    form.gender = userInfo.value.gender || 0
    form.birthday = userInfo.value.birthday || ''
    form.height = userInfo.value.height
    form.targetWeight = userInfo.value.targetWeight
    form.targetCalorie = userInfo.value.targetCalorie
    form.activityLevel = userInfo.value.activityLevel || 2
    form.phone = userInfo.value.phone || ''
    form.email = userInfo.value.email || ''
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateProfile(form)
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
    await fetchHealthProfile()
  } catch (e) {
    // handled
  } finally {
    saving.value = false
  }
}

async function fetchHealthProfile() {
  try {
    const res = await getHealthProfile()
    healthProfile.value = res.data
  } catch (e) {
    // handled
  }
}

onMounted(async () => {
  await userStore.fetchUserInfo()
  initForm()
  await fetchHealthProfile()
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
    font-size: 18px;
    margin: 0 0 20px;
    color: #3d2b4a;
  }
}

.health-info {
  .health-item {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid #edd6e6;

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: #6b5a7a;
    }

    .value {
      font-weight: 600;
      color: #3d2b4a;
    }
  }

  h4 {
    margin: 0 0 12px;
    color: #3d2b4a;
  }
}
</style>
