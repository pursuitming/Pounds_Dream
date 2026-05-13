<template>
  <div class="pet-card" :class="'mood-' + petData.mood.toLowerCase()">
    <div class="pet-main">
      <!-- 宠物图标 -->
      <div class="pet-avatar" :class="'anim-' + petData.mood.toLowerCase()">
        <span class="pet-type-icon">{{ petTypeIcon }}</span>
        <span class="pet-evolution-badge">{{ petData.evolutionIcon }}</span>
        <span v-if="petData.mood === 'HAPPY'" class="mood-sparkle">✨</span>
        <span v-if="petData.mood === 'SLEEPY'" class="mood-zzz">💤</span>
        <span v-if="petData.mood === 'MISSING'" class="mood-tear">💧</span>
      </div>

      <!-- 宠物信息 -->
      <div class="pet-info">
        <div class="pet-name-row">
          <span class="pet-name">{{ petData.petName }}</span>
          <span class="pet-level">Lv.{{ petData.petLevel || 1 }}</span>
          <span class="pet-evolution">{{ petData.evolutionName }}</span>
          <el-icon class="rename-btn" @click="showRename = true"><Edit /></el-icon>
        </div>
        <div class="pet-mood-text">{{ moodText }}</div>
        <div class="happiness-bar">
          <div class="happiness-label">
            <span>幸福值</span>
            <span class="happiness-value">{{ petData.happiness }}</span>
          </div>
          <el-progress
            :percentage="petData.happiness"
            :color="happinessColor"
            :show-text="false"
            :stroke-width="8"
          />
        </div>
      </div>
    </div>

    <!-- 宠物台词 -->
    <div class="pet-message">
      <span class="msg-quote">"</span>
      {{ petData.message }}
      <span class="msg-quote">"</span>
    </div>

    <!-- 快捷操作 -->
    <div class="pet-actions">
      <el-button size="small" @click="$router.push('/diet')">
        🍰 记录饮食
      </el-button>
      <el-button size="small" @click="$router.push('/exercise')">
        💃 去运动
      </el-button>
      <el-button size="small" @click="$router.push('/weight')">
        ✨ 记体重
      </el-button>
    </div>

    <!-- 改名弹窗 -->
    <el-dialog v-model="showRename" title="给宠物改个名字吧" width="360px">
      <el-input v-model="newName" placeholder="输入新昵称" maxlength="50" show-word-limit />
      <template #footer>
        <el-button @click="showRename = false">取消</el-button>
        <el-button type="primary" @click="handleRename">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Edit } from '@element-plus/icons-vue'
import { getPetInfo, renamePet } from '../api/pet'
import { ElMessage } from 'element-plus'

const route = useRoute()
const showRename = ref(false)
const newName = ref('')

const defaultPet = {
  petName: '小梦',
  petType: 'cat',
  mood: 'NORMAL',
  happiness: 0,
  petLevel: 1,
  totalHappiness: 0,
  evolutionLevel: 1,
  evolutionName: '梦想之蛋',
  evolutionIcon: '🥚',
  daysSinceLastRecord: 99,
  currentStreak: 0,
  message: '今天也要加油哦！'
}
const petData = ref({ ...defaultPet })

async function loadPet() {
  try {
    const res = await getPetInfo()
    petData.value = res.data
  } catch (e) {
    console.error('加载宠物数据失败:', e)
  }
}

onMounted(() => {
  loadPet()
  // 监听宠物数据更新事件
  window.addEventListener('pet-data-updated', loadPet)
})
onActivated(() => loadPet())
onUnmounted(() => {
  // 移除事件监听器
  window.removeEventListener('pet-data-updated', loadPet)
})
watch(() => route.path, (path) => {
  if (path === '/dashboard') loadPet()
})

const moodText = computed(() => {
  const map = {
    HAPPY: '超级开心',
    ENERGETIC: '精力充沛',
    NORMAL: '平静待机',
    SLEEPY: '有点困了',
    MISSING: '想念你了'
  }
  return map[petData.value.mood] || '平静待机'
})

const petTypeIcon = computed(() => {
  const map = { cat: '🐱', dog: '🐶', rabbit: '🐰' }
  return map[petData.value.petType] || '🐱'
})

const happinessColor = computed(() => {
  const h = petData.value.happiness
  if (h >= 80) return '#ff69b4'
  if (h >= 50) return '#da70d6'
  return '#c8a2c8'
})

async function handleRename() {
  const name = newName.value.trim()
  if (!name) {
    ElMessage.warning('请输入昵称')
    return
  }
  try {
    await renamePet(name)
    petData.value.petName = name
    showRename.value = false
    newName.value = ''
    ElMessage.success('改名成功')
  } catch (e) {
    ElMessage.error('改名失败')
  }
}
</script>

<style scoped lang="scss">
.pet-card {
  background: linear-gradient(135deg, #fff 0%, #fff8fa 100%);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(255, 182, 193, 0.1);
  border: 1px solid rgba(255, 182, 193, 0.15);

  &.mood-happy { background: linear-gradient(135deg, #fff 0%, #fff0f5 100%); }
  &.mood-energetic { background: linear-gradient(135deg, #fff 0%, #fdf5ff 100%); }
  &.mood-sleepy { background: linear-gradient(135deg, #fff 0%, #f8f0ff 100%); }
  &.mood-missing { background: linear-gradient(135deg, #fff 0%, #fff0f3 100%); }
}

.pet-main {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 12px;
}

.pet-avatar {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff5f8 0%, #ffe8f0 50%, #ffc8d6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;
  box-shadow:
    0 4px 12px rgba(255, 182, 193, 0.2),
    inset 0 -2px 4px rgba(255, 240, 245, 0.5);

  .pet-icon {
    font-size: 36px;
    line-height: 1;
  }

  .pet-type-icon {
    font-size: 34px;
    line-height: 1;
  }

  .pet-evolution-badge {
    position: absolute;
    bottom: -4px;
    right: -4px;
    font-size: 18px;
    background: linear-gradient(135deg, #fff, #fff8fa);
    border-radius: 50%;
    width: 26px;
    height: 26px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 6px rgba(255, 182, 193, 0.2);
  }

  .mood-sparkle {
    position: absolute;
    top: -6px;
    right: -6px;
    font-size: 18px;
    animation: sparkle-float 1.5s ease-in-out infinite;
  }

  .mood-zzz {
    position: absolute;
    top: -6px;
    right: -6px;
    font-size: 18px;
    animation: zzz-float 2s ease-in-out infinite;
  }

  .mood-tear {
    position: absolute;
    bottom: 2px;
    right: 4px;
    font-size: 14px;
    animation: tear-drop 2s ease-in-out infinite;
  }
}

// 心情动画
.anim-happy { animation: bounce 0.6s ease-in-out infinite; }
.anim-energetic { animation: glow-pulse 1.5s ease-in-out infinite; }
.anim-sleepy { animation: sleepy-sway 3s ease-in-out infinite; }
.anim-missing { animation: sad-shake 2s ease-in-out infinite; }

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes glow-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 215, 0, 0.4); }
  50% { box-shadow: 0 0 20px 8px rgba(255, 215, 0, 0.2); }
}

@keyframes sleepy-sway {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-5deg); }
  75% { transform: rotate(5deg); }
}

@keyframes sad-shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-3px); }
  40% { transform: translateX(3px); }
  60% { transform: translateX(-2px); }
  80% { transform: translateX(2px); }
}

@keyframes sparkle-float {
  0%, 100% { transform: translateY(0) scale(1); opacity: 1; }
  50% { transform: translateY(-6px) scale(1.2); opacity: 0.7; }
}

@keyframes zzz-float {
  0%, 100% { transform: translateY(0) rotate(0deg); opacity: 0.6; }
  50% { transform: translateY(-8px) rotate(10deg); opacity: 1; }
}

@keyframes tear-drop {
  0% { transform: translateY(0); opacity: 1; }
  100% { transform: translateY(8px); opacity: 0; }
}

.pet-info {
  flex: 1;
  min-width: 0;
}

.pet-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;

  .pet-name {
    font-size: 18px;
    font-weight: 700;
    color: #3d2b4a;
  }

  .pet-level {
    font-size: 12px;
    color: #ff6b9d;
    background: linear-gradient(135deg, #fff5f8, #ffe8f0);
    padding: 2px 8px;
    border-radius: 10px;
    border: 1px solid rgba(255, 107, 157, 0.2);
    font-weight: 600;
  }

  .pet-evolution {
    font-size: 12px;
    color: #da70d6;
    background: linear-gradient(135deg, #fdf5ff, #f8f0ff);
    padding: 3px 10px;
    border-radius: 12px;
    border: 1px solid rgba(218, 112, 214, 0.2);
  }

  .rename-btn {
    color: #c8a2c8;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.2s ease;
    &:hover {
      color: #da70d6;
      transform: scale(1.1);
    }
  }
}

.pet-mood-text {
  font-size: 13px;
  color: #9b8aad;
  margin-bottom: 8px;
}

.happiness-bar {
  .happiness-label {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #9b8aad;
    margin-bottom: 4px;

    .happiness-value {
      font-weight: 600;
      color: #3d2b4a;
    }
  }
}

.pet-message {
  background: linear-gradient(135deg, #fff5f8 0%, #f8f0ff 100%);
  border-radius: 12px;
  padding: 12px 18px;
  margin-bottom: 14px;
  font-size: 13px;
  color: #5a4a6a;
  line-height: 1.6;
  border: 1px solid rgba(255, 182, 193, 0.2);

  .msg-quote {
    color: #da70d6;
    font-size: 18px;
    font-weight: bold;
  }
}

.pet-actions {
  display: flex;
  gap: 10px;
  border-top: 1px solid rgba(255, 182, 193, 0.15);
  padding-top: 14px;

  .el-button {
    flex: 1;
    border-color: rgba(255, 182, 193, 0.3);
    color: #5a4a6a;
    border-radius: 12px;
    transition: all 0.3s ease;
    &:hover {
      border-color: #da70d6;
      color: #da70d6;
      background: linear-gradient(135deg, #fdf5ff, #f8f0ff);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(218, 112, 214, 0.1);
    }
  }
}
</style>
