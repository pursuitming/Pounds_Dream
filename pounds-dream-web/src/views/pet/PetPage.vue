<template>
  <div class="pet-page">
    <el-row :gutter="16">
      <!-- 左侧主区域 -->
      <el-col :span="16">
        <!-- 宠物场景区 -->
        <div class="card pet-stage" :class="'mood-bg-' + petData.mood.toLowerCase()">
          <div class="pet-scene" @click="handleSceneClick">
            <!-- 背景装饰 -->
            <div class="scene-decor">
              <div class="cloud cloud-1"></div>
              <div class="cloud cloud-2"></div>
              <div class="star star-1">✦</div>
              <div class="star star-2">✦</div>
              <div class="star star-3">✧</div>
              <div class="star star-4">✧</div>
              <div class="star star-5">✦</div>
              <div class="petal petal-1">🌸</div>
              <div class="petal petal-2">💮</div>
              <div class="petal petal-3">✿</div>
              <div class="petal petal-4">🌸</div>
              <div class="petal petal-5">💮</div>
              <div class="flower flower-1">🌷</div>
              <div class="flower flower-2">🌹</div>
              <div class="flower flower-3">🌺</div>
              <div class="heart heart-1">💕</div>
              <div class="heart heart-2">💖</div>
              <div class="sparkle sparkle-1">✨</div>
              <div class="sparkle sparkle-2">✨</div>
              <div class="sparkle sparkle-3">✨</div>
              <div class="grass"></div>
            </div>

            <!-- 宠物容器 -->
            <div class="pet-container" :class="['pet-' + petData.mood.toLowerCase(), { 'pet-interact': interacting }]">
              <!-- 小猫 -->
              <div v-if="petData.petType === 'cat'" class="animal cat">
                <div class="ear ear-left"><div class="ear-inner"></div></div>
                <div class="ear ear-right"><div class="ear-inner"></div></div>
                <div class="head">
                  <div class="eyes">
                    <div class="eye" :class="'eye-' + petData.mood.toLowerCase()">
                      <div class="pupil"></div>
                    </div>
                    <div class="eye" :class="'eye-' + petData.mood.toLowerCase()">
                      <div class="pupil"></div>
                    </div>
                  </div>
                  <div class="nose"></div>
                  <div class="mouth" :class="'mouth-' + petData.mood.toLowerCase()"></div>
                  <div class="whisker whisker-lt"></div>
                  <div class="whisker whisker-lb"></div>
                  <div class="whisker whisker-rt"></div>
                  <div class="whisker whisker-rb"></div>
                </div>
                <div class="body">
                  <div class="belly"></div>
                  <div class="tail"></div>
                  <div class="paw paw-left"></div>
                  <div class="paw paw-right"></div>
                </div>
              </div>

              <!-- 小狗 -->
              <div v-if="petData.petType === 'dog'" class="animal dog">
                <div class="ear ear-left"></div>
                <div class="ear ear-right"></div>
                <div class="head">
                  <div class="eyes">
                    <div class="eye" :class="'eye-' + petData.mood.toLowerCase()">
                      <div class="pupil"></div>
                    </div>
                    <div class="eye" :class="'eye-' + petData.mood.toLowerCase()">
                      <div class="pupil"></div>
                    </div>
                  </div>
                  <div class="nose"></div>
                  <div class="mouth" :class="'mouth-' + petData.mood.toLowerCase()"></div>
                  <div class="tongue" v-if="petData.mood === 'HAPPY' || petData.mood === 'ENERGETIC'"></div>
                </div>
                <div class="body">
                  <div class="belly"></div>
                  <div class="tail"></div>
                  <div class="paw paw-left"></div>
                  <div class="paw paw-right"></div>
                </div>
              </div>

              <!-- 小兔 -->
              <div v-if="petData.petType === 'rabbit'" class="animal rabbit">
                <div class="ear ear-left"><div class="ear-inner"></div></div>
                <div class="ear ear-right"><div class="ear-inner"></div></div>
                <div class="head">
                  <div class="eyes">
                    <div class="eye" :class="'eye-' + petData.mood.toLowerCase()">
                      <div class="pupil"></div>
                    </div>
                    <div class="eye" :class="'eye-' + petData.mood.toLowerCase()">
                      <div class="pupil"></div>
                    </div>
                  </div>
                  <div class="nose"></div>
                  <div class="mouth" :class="'mouth-' + petData.mood.toLowerCase()"></div>
                  <div class="cheek cheek-left"></div>
                  <div class="cheek cheek-right"></div>
                </div>
                <div class="body">
                  <div class="belly"></div>
                  <div class="carrot">🥕</div> <!-- 抱着小胡萝卜 -->
                  <div class="tail"></div>
                  <div class="paw paw-left"></div>
                  <div class="paw paw-right"></div>
                </div>
              </div>

              <!-- 进化标记 -->
              <div class="evolution-badge">{{ petData.evolutionIcon }}</div>
            </div>

            <!-- 互动特效 -->
            <div v-if="showEffect" class="interact-effect" :class="'effect-' + lastAction">
              <span v-for="(emoji, i) in effectEmojis" :key="i" class="effect-emoji" :style="effectStyle(i)">{{ emoji }}</span>
            </div>

            <!-- 点击特效容器 -->
            <div class="click-effects-container" ref="clickEffectsContainer"></div>

            <!-- 宠物名牌 -->
            <div class="pet-name-plate">
              <span class="plate-name">{{ petData.petName }}</span>
              <el-icon class="rename-icon" @click="showRename = true"><Edit /></el-icon>
            </div>
          </div>

          <!-- 台词气泡 -->
          <div class="speech-bubble" :class="{ 'bubble-pop': interacting }">
            <div class="bubble-content">{{ displayMessage }}</div>
          </div>
        </div>

        <!-- 互动按钮区 -->
        <div class="card">
          <h3 class="card-title">互动一下</h3>
          <div class="interact-buttons">
            <button
              v-for="btn in interactButtons"
              :key="btn.action"
              class="interact-btn"
              :class="'btn-' + btn.action"
              :disabled="cooldowns[btn.action] > 0 || interacting"
              @click="handleButtonClick(btn.action, $event)"
            >
              <span class="btn-emoji">{{ btn.emoji }}</span>
              <span class="btn-label">{{ btn.label }}</span>
              <span v-if="cooldowns[btn.action] > 0" class="btn-cooldown">{{ cooldowns[btn.action] }}s</span>
            </button>
          </div>
        </div>

        <!-- 宠物形象选择 -->
        <div class="card">
          <h3 class="card-title">选择宠物形象</h3>
          <div class="pet-type-selector">
            <div
              v-for="type in petTypes"
              :key="type.value"
              class="type-card"
              :class="{ active: petData.petType === type.value }"
              @click="switchPetType(type.value)"
            >
              <div class="type-icon">{{ type.icon }}</div>
              <div class="type-name">{{ type.label }}</div>
              <div class="type-desc">{{ type.desc }}</div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :span="8">
        <!-- 宠物状态卡 -->
        <div class="card">
          <h3 class="card-title">宠物状态</h3>
          <div class="stat-item">
            <span class="stat-label">进化形态</span>
            <span class="stat-value evolution">
              {{ petData.evolutionIcon }} {{ petData.evolutionName }}
              <span class="pet-level-badge">Lv.{{ petData.petLevel }}</span>
            </span>
          </div>
          <div class="stat-item">
            <span class="stat-label">当前心情</span>
            <span class="stat-value" :class="'mood-' + petData.mood.toLowerCase()">{{ moodText }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">连续打卡</span>
            <span class="stat-value highlight">{{ petData.currentStreak }} 天</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">上次记录</span>
            <span class="stat-value">{{ petData.daysSinceLastRecord === 99 ? '暂无记录' : petData.daysSinceLastRecord + ' 天前' }}</span>
          </div>

          <div class="happiness-section">
            <div class="happiness-header">
              <span>今日幸福值</span>
              <span class="happiness-num">{{ petData.happiness }}</span>
            </div>
            <el-progress
              :percentage="petData.happiness"
              :color="happinessColor"
              :show-text="false"
              :stroke-width="10"
            />
          </div>

          <!-- 升级进度 -->
          <div class="level-section">
            <div class="level-header">
              <span>宠物等级</span>
              <span class="level-num">Lv.{{ petData.petLevel }}</span>
            </div>
            <div class="level-progress">
              <div class="level-bar">
                <div class="level-fill" :style="{ width: levelProgress + '%' }"></div>
              </div>
              <div class="level-text">
                <span>{{ petData.totalHappiness }} / {{ levelUpRequired }}</span>
                <span class="level-hint">距离升级还需 {{ levelUpRequired - petData.totalHappiness }} 幸福值</span>
              </div>
            </div>
          </div>

          <!-- 快捷入口 -->
          <div class="quick-links">
            <el-button size="small" @click="$router.push('/diet')">🍰 记录饮食</el-button>
            <el-button size="small" @click="$router.push('/exercise')">💃 去运动</el-button>
            <el-button size="small" @click="$router.push('/weight')">✨ 记体重</el-button>
          </div>
        </div>

        <!-- 成长日志 -->
        <div class="card">
          <h3 class="card-title">成长日志</h3>
          <div class="log-timeline" v-if="petLogs.length > 0">
            <div v-for="(log, index) in petLogs" :key="index" class="log-item">
              <div class="log-dot" :class="'dot-' + log.action"></div>
              <div class="log-content">
                <div class="log-action">{{ log.actionName }}</div>
                <div class="log-meta">
                  <span class="log-mood">{{ moodEmoji(log.petMood) }} {{ moodLabel(log.petMood) }}</span>
                  <span class="log-happiness">幸福+{{ log.happiness }}</span>
                </div>
                <div class="log-time">{{ formatTime(log.createdAt) }}</div>
              </div>
            </div>
          </div>
          <div v-else class="empty-log">
            <div class="empty-icon">📝</div>
            <div class="empty-text">还没有互动记录哦~</div>
          </div>
        </div>
      </el-col>
    </el-row>

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
import { ref, computed, onMounted, onActivated, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Edit } from '@element-plus/icons-vue'
import { getPetInfo, renamePet, interactPet, changePetType, getPetLogs } from '../../api/pet'
import { ElMessage } from 'element-plus'

const route = useRoute()
const showRename = ref(false)
const newName = ref('')
const interacting = ref(false)
const showEffect = ref(false)
const lastAction = ref('')
const displayMessage = ref('')
const clickEffectsContainer = ref(null)
let messageTimer = null
let cooldownTimer = null

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
const cooldowns = ref({ pet: 0, feed: 0, play: 0, bath: 0, sleep: 0, dance: 0 })
const petLogs = ref([])

const interactButtons = [
  { action: 'pet', emoji: '🤚', label: '摸摸头' },
  { action: 'feed', emoji: '🍰', label: '喂食' },
  { action: 'play', emoji: '🎾', label: '玩耍' },
  { action: 'bath', emoji: '🛁', label: '洗澡' },
  { action: 'sleep', emoji: '💤', label: '睡觉' },
  { action: 'dance', emoji: '💃', label: '跳舞' }
]

const petTypes = [
  { value: 'cat', icon: '🐱', label: '小猫咪', desc: '奶白萌猫·二次元小公主' },
  { value: 'dog', icon: '🐶', label: '小狗狗', desc: '柴犬幼崽·元气小天使' },
  { value: 'rabbit', icon: '🐰', label: '小兔子', desc: '梦幻粉白·胡萝卜小甜心' }
]

const effectEmojiMap = {
  pet: ['💕', '💖', '💗'],
  feed: ['🍰', '✨', '💫'],
  play: ['🌟', '💫', '✨'],
  bath: ['🫧', '💧', '✨'],
  sleep: ['🌙', '💤', '✨'],
  dance: ['🎵', '🎶', '💕']
}
const effectEmojis = computed(() => effectEmojiMap[lastAction.value] || ['✨'])

function effectStyle(index) {
  const angles = [-30, 0, 30]
  const angle = angles[index % 3]
  return {
    '--angle': angle + 'deg',
    '--delay': (index * 0.15) + 's'
  }
}

async function loadPet() {
  try {
    const res = await getPetInfo()
    petData.value = res.data
    if (res.data.cooldowns) {
      cooldowns.value = { ...cooldowns.value, ...res.data.cooldowns }
    }
    displayMessage.value = res.data.message
  } catch (e) {
    console.error('加载宠物数据失败:', e)
  }
}

async function loadLogs() {
  try {
    const res = await getPetLogs()
    petLogs.value = res.data || []
  } catch (e) {
    console.error('加载日志失败:', e)
  }
}

onMounted(() => {
  loadPet()
  loadLogs()
  startCooldownTimer()
})
onActivated(() => {
  loadPet()
  loadLogs()
})
watch(() => route.path, (path) => {
  if (path === '/pet') {
    loadPet()
    loadLogs()
  }
})
onUnmounted(() => {
  if (messageTimer) clearTimeout(messageTimer)
  if (cooldownTimer) clearInterval(cooldownTimer)
})

function startCooldownTimer() {
  cooldownTimer = setInterval(() => {
    for (const key in cooldowns.value) {
      if (cooldowns.value[key] > 0) {
        cooldowns.value[key] = Math.max(0, cooldowns.value[key] - 1)
      }
    }
  }, 1000)
}

const moodText = computed(() => {
  const map = {
    HAPPY: '超级开心 🥰',
    ENERGETIC: '精力充沛 ⚡',
    NORMAL: '平静待机 😊',
    SLEEPY: '有点困了 😴',
    MISSING: '想念你了 🥺'
  }
  return map[petData.value.mood] || '平静待机'
})

const happinessColor = computed(() => {
  const h = petData.value.happiness
  if (h >= 80) return '#ff69b4'
  if (h >= 50) return '#da70d6'
  return '#c8a2c8'
})

// 升级所需幸福值配置
const levelUpConfig = {
  1: 100,
  2: 300,
  3: 600,
  4: 1000,
  5: 1500,
  6: 2100,
  7: 2800
}

// 升级所需幸福值
const levelUpRequired = computed(() => {
  const level = petData.value.petLevel || 1
  return levelUpConfig[level] || 9999
})

// 升级进度百分比
const levelProgress = computed(() => {
  const level = petData.value.petLevel || 1
  const total = petData.value.totalHappiness || 0
  const required = levelUpConfig[level] || 9999

  // 计算当前级别的起始幸福值
  let prevRequired = 0
  if (level > 1) {
    prevRequired = levelUpConfig[level - 1] || 0
  }

  // 计算进度
  const currentProgress = total - prevRequired
  const levelRange = required - prevRequired
  return Math.min(100, Math.max(0, (currentProgress / levelRange) * 100))
})

function moodEmoji(mood) {
  const map = { HAPPY: '🥰', ENERGETIC: '⚡', NORMAL: '😊', SLEEPY: '😴', MISSING: '🥺' }
  return map[mood] || '😊'
}

function moodLabel(mood) {
  const map = { HAPPY: '开心', ENERGETIC: '活力', NORMAL: '平静', SLEEPY: '困倦', MISSING: '想念' }
  return map[mood] || '平静'
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  const hour = d.getHours().toString().padStart(2, '0')
  const min = d.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hour}:${min}`
}

async function doInteract(action) {
  if (interacting.value || cooldowns.value[action] > 0) return

  interacting.value = true
  lastAction.value = action
  showEffect.value = true

  try {
    const res = await interactPet(action)
    petData.value = res.data
    if (res.data.cooldowns) {
      cooldowns.value = { ...cooldowns.value, ...res.data.cooldowns }
    }
    displayMessage.value = res.data.message

    // 重新加载宠物信息和日志，确保幸福值等数据同步
    await Promise.all([loadPet(), loadLogs()])

    // 通知其他组件刷新宠物数据
    window.dispatchEvent(new CustomEvent('pet-data-updated'))
  } catch (e) {
    ElMessage.error('互动失败')
  }

  // 特效持续时间
  setTimeout(() => {
    showEffect.value = false
    interacting.value = false
  }, 1200)
}

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

async function switchPetType(type) {
  if (type === petData.value.petType) return
  try {
    await changePetType(type)
    petData.value.petType = type
    ElMessage.success('切换成功')
  } catch (e) {
    ElMessage.error('切换失败')
  }
}

// 点击特效相关函数
function handleButtonClick(action, event) {
  const rect = event.currentTarget.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top

  // 创建按钮点击特效
  createClickEffect(x, y, action)

  // 执行原有的互动逻辑
  doInteract(action)
}

function createClickEffect(x, y, type) {
  if (!clickEffectsContainer.value) return

  const container = clickEffectsContainer.value
  const effectCount = type === 'pet' ? 12 : 8

  for (let i = 0; i < effectCount; i++) {
    const particle = document.createElement('div')
    particle.className = `click-particle click-particle-${type}`

    // 随机位置和动画
    const angle = (Math.PI * 2 * i) / effectCount
    const velocity = 50 + Math.random() * 50
    const tx = Math.cos(angle) * velocity
    const ty = Math.sin(angle) * velocity
    const rotation = Math.random() * 360
    const scale = 0.5 + Math.random() * 0.5
    const delay = Math.random() * 0.2

    particle.style.cssText = `
      left: ${x}px;
      top: ${y}px;
      --tx: ${tx}px;
      --ty: ${ty}px;
      --rotation: ${rotation}deg;
      --scale: ${scale};
      --delay: ${delay}s;
    `

    // 根据类型设置不同的内容
    if (type === 'pet') {
      const emojis = ['💕', '💖', '💗', '✨', '⭐', '🌟']
      particle.textContent = emojis[Math.floor(Math.random() * emojis.length)]
    } else if (type === 'pet') {
      const emojis = ['🤚', '✨', '💫', '🌟']
      particle.textContent = emojis[Math.floor(Math.random() * emojis.length)]
    } else if (type === 'feed') {
      const emojis = ['🍰', '🍩', '🍪', '✨', '💖']
      particle.textContent = emojis[Math.floor(Math.random() * emojis.length)]
    } else {
      const emojis = ['✨', '💫', '⭐']
      particle.textContent = emojis[Math.floor(Math.random() * emojis.length)]
    }

    container.appendChild(particle)

    // 动画结束后移除粒子
    setTimeout(() => {
      particle.remove()
    }, 1000)
  }

  // 创建波纹效果
  createRipple(x, y, type)
}

function createRipple(x, y, type) {
  if (!clickEffectsContainer.value) return

  const container = clickEffectsContainer.value
  const ripple = document.createElement('div')
  ripple.className = `click-ripple click-ripple-${type}`
  ripple.style.cssText = `left: ${x}px; top: ${y}px;`

  container.appendChild(ripple)

  setTimeout(() => {
    ripple.remove()
  }, 800)
}

// 宠物场景点击特效
function handleSceneClick(event) {
  const x = event.clientX
  const y = event.clientY
  const emojis = ['✨', '⭐', '🌟', '💫', '💖', '💕', '💗', '💝']
  const count = 5 + Math.floor(Math.random() * 4)

  for (let i = 0; i < count; i++) {
    const el = document.createElement('span')
    el.textContent = emojis[Math.floor(Math.random() * emojis.length)]

    const angle = (Math.PI * 2 * i) / count + (Math.random() - 0.5) * 0.5
    const dist = 40 + Math.random() * 60
    const tx = Math.cos(angle) * dist
    const ty = Math.sin(angle) * dist - 30
    const delay = Math.random() * 100

    el.style.cssText = `
      position: fixed;
      left: ${x}px;
      top: ${y}px;
      font-size: 22px;
      pointer-events: none;
      z-index: 9999;
      opacity: 1;
      transform: translate(-50%, -50%) scale(0.3);
      transition: all 0.8s ease-out ${delay}ms;
      filter: drop-shadow(0 1px 3px rgba(0,0,0,0.15));
    `
    document.body.appendChild(el)

    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        el.style.opacity = '0'
        el.style.transform = `translate(calc(-50% + ${tx}px), calc(-50% + ${ty}px)) scale(0) rotate(${(Math.random() - 0.5) * 360}deg)`
      })
    })

    setTimeout(() => el.remove(), 1000 + delay)
  }
}
</script>

<style scoped lang="scss">
// ========== 基础布局 ==========
.pet-page {
  .card {
    background: #fff;
    border-radius: 16px; // 更圆润
    padding: 22px;
    margin-bottom: 18px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  }
  .card-title {
    font-size: 17px;
    margin: 0 0 18px;
    color: #3d2b4a;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 10px;

    &::before {
      content: '';
      display: inline-block;
      width: 5px;
      height: 18px;
      background: linear-gradient(180deg, #ff6b9d, #9b7fe6);
      border-radius: 3px;
    }
  }
}

// ========== 宠物场景区 (梦幻二次元) ==========
.pet-stage {
  min-height: 400px; // 稍微增加高度
  position: relative;
  overflow: hidden;

  &.mood-bg-happy { background: linear-gradient(180deg, #fff0f5 0%, #f8f0ff 50%, #fff 100%); }
  &.mood-bg-energetic { background: linear-gradient(180deg, #fff5f7 0%, #fef0ff 50%, #fff 100%); }
  &.mood-bg-normal { background: linear-gradient(180deg, #f8f0ff 0%, #fff0f8 50%, #fff 100%); }
  &.mood-bg-sleepy { background: linear-gradient(180deg, #f0e8ff 0%, #fff0f5 50%, #fff 100%); }
  &.mood-bg-missing { background: linear-gradient(180deg, #fff0f3 0%, #f8f0ff 50%, #fff 100%); }

  // 添加梦幻光晕效果
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(255, 182, 193, 0.1) 0%, rgba(155, 127, 230, 0.05) 50%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
    z-index: 1;
  }
}

.pet-scene {
  position: relative;
  height: 300px; // 增加高度以容纳更多装饰
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden; // 防止特效溢出
}

// ========== 背景装饰 ==========
.scene-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .cloud {
    position: absolute;
    width: 80px; // 更大的云朵
    height: 28px;
    background: rgba(255, 240, 245, 0.9);
    border-radius: 28px;
    box-shadow: 0 2px 8px rgba(255, 182, 193, 0.2);
    z-index: 0;

    &::before, &::after {
      content: '';
      position: absolute;
      background: rgba(255, 240, 245, 0.9);
      border-radius: 50%;
    }
    &::before { width: 40px; height: 40px; top: -20px; left: 15px; }
    &::after { width: 28px; height: 28px; top: -14px; right: 15px; }
  }
  .cloud-1 { top: 25px; left: 8%; animation: cloud-float 8s ease-in-out infinite; }
  .cloud-2 { top: 50px; right: 10%; animation: cloud-float 10s ease-in-out infinite reverse; }

  .star {
    position: absolute;
    animation: star-twinkle 2s ease-in-out infinite;
    z-index: 0;
  }
  .star-1 { top: 30px; right: 18%; font-size: 16px; color: #ffb6c1; }
  .star-2 { top: 55px; left: 22%; font-size: 12px; color: #dda0dd; animation-delay: 0.5s; }
  .star-3 { top: 20px; left: 38%; font-size: 10px; color: #ff69b4; animation-delay: 1s; }
  .star-4 { top: 70px; right: 30%; font-size: 14px; color: #9b7fe6; animation-delay: 1.5s; }
  .star-5 { top: 45px; left: 55%; font-size: 11px; color: #ffb6c1; animation-delay: 0.8s; }

  .grass {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 50px; // 稍微增加高度
    background: linear-gradient(0deg, rgba(255, 240, 245, 0.9) 0%, rgba(248, 240, 255, 0.5) 50%, transparent 100%);
    border-radius: 50% 50% 0 0;
    z-index: 0;

    &::before {
      content: '';
      position: absolute;
      bottom: 0;
      left: 5%;
      right: 5%;
      height: 25px;
      background: linear-gradient(0deg, rgba(255, 182, 193, 0.4) 0%, transparent 100%);
      border-radius: 50% 50% 0 0;
    }

    // 添加草地纹理
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 15px;
      background: linear-gradient(90deg,
        rgba(255, 240, 245, 0.6) 0%,
        rgba(255, 245, 250, 0.8) 20%,
        rgba(255, 240, 245, 0.6) 40%,
        rgba(255, 245, 250, 0.8) 60%,
        rgba(255, 240, 245, 0.6) 80%,
        rgba(255, 245, 250, 0.8) 100%
      );
      border-radius: 50% 50% 0 0;
    }
  }

  // 漂浮的花瓣
  .petal {
    position: absolute;
    font-size: 14px;
    opacity: 0.6;
    animation: petal-fall 6s ease-in-out infinite;
  }
  .petal-1 { top: -10px; left: 20%; animation-delay: 0s; }
  .petal-2 { top: -10px; left: 60%; animation-delay: 2s; font-size: 12px; }
  .petal-3 { top: -10px; left: 80%; animation-delay: 4s; font-size: 10px; }
  .petal-4 { top: -10px; left: 35%; animation-delay: 1s; font-size: 11px; }
  .petal-5 { top: -10px; left: 75%; animation-delay: 3s; font-size: 13px; }

  // 花朵装饰 - 分散在四周
  .flower {
    position: absolute;
    font-size: 18px;
    opacity: 0.7;
    animation: flower-sway 4s ease-in-out infinite;
    z-index: 0;
  }
  .flower-1 { bottom: 40px; left: 8%; animation-delay: 0s; }
  .flower-2 { bottom: 35px; right: 8%; animation-delay: 1s; font-size: 16px; }
  .flower-3 { bottom: 45px; left: 30%; animation-delay: 2s; font-size: 20px; }

  // 爱心装饰 - 飘在上方
  .heart {
    position: absolute;
    font-size: 16px;
    opacity: 0.5;
    animation: heart-float 3s ease-in-out infinite;
    z-index: 0;
  }
  .heart-1 { top: 40px; left: 8%; animation-delay: 0s; }
  .heart-2 { top: 30px; right: 8%; animation-delay: 1.5s; font-size: 14px; }

  // 闪烁星光 - 点缀在各处
  .sparkle {
    position: absolute;
    font-size: 12px;
    opacity: 0.8;
    animation: sparkle-twinkle 1.5s ease-in-out infinite;
    z-index: 0;
  }
  .sparkle-1 { top: 60px; left: 20%; animation-delay: 0s; }
  .sparkle-2 { top: 40px; right: 20%; animation-delay: 0.5s; font-size: 10px; }
  .sparkle-3 { top: 80px; left: 50%; animation-delay: 1s; font-size: 14px; }
}

// ========== 宠物容器 ==========
.pet-container {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0; // 上下留白，更适合花卉装饰
  cursor: pointer; // 添加鼠标指针样式
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.02); // 悬停时轻微放大
  }
}

// ========== 心情动画 (二次元风格) ==========
.pet-happy { animation: bounce 0.6s ease-in-out infinite; }
.pet-energetic { animation: wiggle 0.8s ease-in-out infinite; }
.pet-normal { animation: breathe 3s ease-in-out infinite; }
.pet-sleepy { animation: sway 4s ease-in-out infinite; }
.pet-missing { animation: tremble 1.5s ease-in-out infinite; }
.pet-interact { animation: jump 0.4s ease-out; }

// 额外的二次元效果
.pet-happy::after {
  content: '✨';
  position: absolute;
  top: -20px;
  right: -10px;
  font-size: 16px;
  animation: sparkle-appear 0.5s ease-out;
}

// ========== 动物基础样式 (二次元Q版) ==========
.animal {
  position: relative;
  width: 130px; // 稍微增加宽度
  height: 150px; // 稍微增加高度
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1)); // 添加阴影效果
}

// ========== 小猫 (二次元萌系) ==========
.animal.cat {
  transform: rotate(-5deg); // 歪着脑袋的效果

  // 蓝色水汪汪大眼睛 - 圆圆的
  .eye {
    width: 28px !important;
    height: 28px !important;
    box-shadow:
      inset 0 2px 4px rgba(0, 0, 0, 0.05),
      0 0 0 2px rgba(135, 206, 235, 0.4),
      0 0 10px rgba(135, 206, 235, 0.25); // 发光效果

    .pupil {
      width: 20px !important;
      height: 20px !important;
      background: radial-gradient(circle at 35% 35%, #5a9fc4, #3a7ca8);
    }
  }

  .ear {
    position: absolute;
    top: -14px; // 调整位置
    width: 0; height: 0;
    border-left: 20px solid transparent; // 更大的耳朵
    border-right: 20px solid transparent;
    border-bottom: 35px solid #fff5ee;
    z-index: 1;
    filter: drop-shadow(0 2px 4px rgba(255, 240, 230, 0.4));
    &-left { left: 8px; transform: rotate(-12deg); }
    &-right { right: 8px; transform: rotate(12deg); }
    .ear-inner {
      position: absolute;
      top: 14px; left: -12px;
      width: 0; height: 0;
      border-left: 12px solid transparent;
      border-right: 12px solid transparent;
      border-bottom: 22px solid #ffe8d6;
    }
  }
  .head {
    position: absolute;
    top: 14px; left: 50%;
    transform: translateX(-50%);
    width: 95px; height: 85px; // 更大的脑袋
    background: linear-gradient(145deg, #fff8f0 0%, #fff5ee 50%, #ffede0 100%);
    border-radius: 50% 50% 45% 45%;
    z-index: 2;
    box-shadow:
      inset 0 -4px 8px rgba(255, 240, 230, 0.3),
      0 4px 12px rgba(255, 240, 230, 0.2);

    // 蝴蝶结装饰
    &::before {
      content: '';
      position: absolute;
      top: -6px;
      right: 10px;
      width: 18px;
      height: 14px;
      background: #ff69b4;
      border-radius: 50% 50% 20% 20%;
      transform: rotate(20deg);
      box-shadow: 0 2px 4px rgba(255, 105, 180, 0.3);
      z-index: 3;
    }
    &::after {
      content: '';
      position: absolute;
      top: -6px;
      right: 4px;
      width: 12px;
      height: 12px;
      background: #ff69b4;
      border-radius: 50%;
      z-index: 3;
    }
  }
  .body {
    position: absolute;
    bottom: 0; left: 50%;
    transform: translateX(-50%);
    width: 72px; height: 58px; // Q版小身体
    background: linear-gradient(180deg, #fff5ee 0%, #ffede0 100%);
    border-radius: 50% 50% 40% 40%;
    z-index: 1;
    box-shadow: 0 4px 8px rgba(255, 240, 230, 0.2);
  }
  .tail {
    position: absolute;
    bottom: 14px; right: -26px;
    width: 48px; height: 24px; // 更大的尾巴
    background: linear-gradient(90deg, #fff5ee, #fff8f0);
    border-radius: 0 24px 24px 0;
    transform-origin: left center;
    animation: tail-wag 1.2s ease-in-out infinite;
    box-shadow: 0 2px 4px rgba(255, 240, 230, 0.2);
  }
  .whisker {
    position: absolute;
    width: 30px; height: 2px; // 更长的胡须
    background: linear-gradient(90deg, rgba(200, 180, 160, 0.6), rgba(200, 180, 160, 0.2));
    top: 48px;
    border-radius: 1px;
    &-lt { left: -22px; transform: rotate(-8deg); }
    &-lb { left: -20px; top: 54px; transform: rotate(8deg); }
    &-rt { right: -22px; transform: rotate(8deg); }
    &-rb { right: -20px; top: 54px; transform: rotate(-8deg); }
  }
}

// ========== 小狗 (二次元柴犬) ==========
.animal.dog {
  .ear {
    position: absolute;
    top: 6px;
    width: 34px; height: 48px; // 更大的耳朵
    background: linear-gradient(180deg, #f0c8a0 0%, #e8b888 100%);
    border-radius: 0 0 50% 50%;
    z-index: 0;
    box-shadow: 0 2px 4px rgba(232, 184, 136, 0.3);
    &-left { left: -2px; transform: rotate(-18deg); }
    &-right { right: -2px; transform: rotate(18deg); }

    // 毛茸茸效果
    &::before {
      content: '';
      position: absolute;
      top: -3px; left: -3px; right: -3px; bottom: -3px;
      background: inherit;
      border-radius: inherit;
      filter: blur(2px);
      opacity: 0.4;
    }
  }
  .head {
    position: absolute;
    top: 14px; left: 50%;
    transform: translateX(-50%);
    width: 92px; height: 76px; // 更大的脑袋
    background: linear-gradient(145deg, #f5d8b8 0%, #f0c8a0 50%, #e8b888 100%);
    border-radius: 50% 50% 40% 40%;
    z-index: 2;
    box-shadow:
      inset 0 -4px 8px rgba(240, 200, 160, 0.3),
      0 4px 12px rgba(240, 200, 160, 0.2);

    // 项圈装饰
    &::after {
      content: '';
      position: absolute;
      bottom: -8px;
      left: 50%;
      transform: translateX(-50%);
      width: 74px;
      height: 10px;
      background: linear-gradient(90deg, #ff69b4, #ff8ec4, #ff69b4);
      border-radius: 5px;
      box-shadow: 0 2px 4px rgba(255, 105, 180, 0.3);
    }
  }
  .nose {
    position: absolute;
    top: 30px; left: 50%;
    transform: translateX(-50%);
    width: 22px; height: 18px; // 更大的鼻子
    background: radial-gradient(circle at 40% 30%, #4a4a4a, #333);
    border-radius: 50% 50% 40% 40%;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  .mouth {
    position: absolute;
    top: 48px; left: 50%;
    transform: translateX(-50%);
    width: 26px; height: 14px;
    border-bottom: 2.5px solid #8a7a6a;
    border-radius: 0 0 50% 50%;
  }
  .tongue {
    position: absolute;
    top: 50px; left: 50%;
    transform: translateX(-50%);
    width: 16px; height: 18px; // 更大的舌头
    background: linear-gradient(180deg, #ff9bb0 0%, #ff7a95 100%);
    border-radius: 0 0 50% 50%;
    animation: tongue-pant 0.6s ease-in-out infinite;
    box-shadow: 0 2px 4px rgba(255, 122, 149, 0.2);
  }
  .body {
    position: absolute;
    bottom: 0; left: 50%;
    transform: translateX(-50%);
    width: 74px; height: 60px; // 圆滚滚的小身体
    background: linear-gradient(180deg, #f0c8a0 0%, #e8b888 100%);
    border-radius: 50% 50% 40% 40%;
    z-index: 1;
    box-shadow: 0 4px 8px rgba(232, 184, 136, 0.2);

    // 毛茸茸效果
    &::before {
      content: '';
      position: absolute;
      top: -3px; left: -3px; right: -3px; bottom: -3px;
      background: inherit;
      border-radius: inherit;
      filter: blur(2px);
      opacity: 0.3;
    }
  }
  .tail {
    position: absolute;
    bottom: 24px; right: -20px;
    width: 30px; height: 36px; // 更大的尾巴
    background: linear-gradient(180deg, #f0c8a0 0%, #e8b888 100%);
    border-radius: 50% 50% 0 0;
    transform-origin: bottom center;
    animation: tail-wag-fast 0.5s ease-in-out infinite;
    box-shadow: 0 2px 4px rgba(232, 184, 136, 0.2);
  }

  // 琥珀色大眼睛 - 圆圆的
  .eye {
    width: 26px !important;
    height: 26px !important;
    box-shadow:
      inset 0 2px 4px rgba(0, 0, 0, 0.05),
      0 0 0 2px rgba(218, 165, 32, 0.4),
      0 0 10px rgba(218, 165, 32, 0.25); // 琥珀色发光效果

    .pupil {
      width: 18px !important;
      height: 18px !important;
      background: radial-gradient(circle at 35% 35%, #d4a520, #b8860b); // 琥珀色瞳孔
    }
  }
}

// ========== 小兔 (二次元梦幻粉白) ==========
.animal.rabbit {
  .ear {
    position: absolute;
    top: -36px; // 更高的耳朵
    width: 24px; height: 65px; // 更长的耳朵
    background: linear-gradient(180deg, #fff8fa 0%, #fff5f8 100%);
    border-radius: 50% 50% 40% 40%;
    z-index: 1;
    box-shadow: 0 2px 6px rgba(255, 248, 250, 0.4);

    // 微微垂下的效果
    &-left { left: 16px; transform: rotate(-20deg); }
    &-right { right: 16px; transform: rotate(20deg); }

    .ear-inner {
      position: absolute;
      top: 14px; left: 50%;
      transform: translateX(-50%);
      width: 12px; height: 42px;
      background: linear-gradient(180deg, #ffc8d6 0%, #ffb6c1 100%);
      border-radius: 50%;
    }
  }
  .head {
    position: absolute;
    top: 22px; left: 50%;
    transform: translateX(-50%);
    width: 86px; height: 76px; // 更圆的脑袋
    background: linear-gradient(145deg, #fff8fa 0%, #fff5f8 50%, #fff0f5 100%);
    border-radius: 50%;
    z-index: 2;
    box-shadow:
      inset 0 -4px 8px rgba(255, 248, 250, 0.4),
      0 4px 12px rgba(255, 248, 250, 0.3);

    // 花朵头饰 - 更精致
    &::before {
      content: '';
      position: absolute;
      top: -10px;
      right: 6px;
      width: 22px;
      height: 22px;
      background: radial-gradient(circle, #ff69b4 30%, #ff8faa 70%);
      border-radius: 50%;
      box-shadow:
        -12px -6px 0 -2px #ffb6c1,
        12px -6px 0 -2px #ffb6c1,
        -6px -12px 0 -2px #ffc8d6,
        6px -12px 0 -2px #ffc8d6;
      z-index: 3;
    }
  }
  .cheek {
    position: absolute;
    top: 44px;
    width: 22px; height: 16px; // 更大的腮红
    background: rgba(255, 182, 193, 0.5);
    border-radius: 50%;
    box-shadow: 0 0 10px rgba(255, 182, 193, 0.4);
    &-left { left: -2px; }
    &-right { right: -2px; }
  }
  .body {
    position: absolute;
    bottom: 0; left: 50%;
    transform: translateX(-50%);
    width: 68px; height: 56px; // Q版小身体
    background: linear-gradient(180deg, #fff5f8 0%, #fff0f5 100%);
    border-radius: 50% 50% 40% 40%;
    z-index: 1;
    box-shadow: 0 4px 8px rgba(255, 248, 250, 0.3);
  }
  .tail {
    position: absolute;
    bottom: 14px; right: -14px;
    width: 26px; height: 26px; // 更圆的尾巴
    background: radial-gradient(circle at 40% 40%, #fff, #fff8fa);
    border-radius: 50%;
    box-shadow: 0 2px 6px rgba(255, 248, 250, 0.4);
  }

  // 抱着小胡萝卜
  .carrot {
    position: absolute;
    bottom: 18px; left: 50%;
    transform: translateX(-50%);
    font-size: 26px; // 更大的胡萝卜
    z-index: 2;
    animation: carrot-hold 2s ease-in-out infinite;
    filter: drop-shadow(0 2px 4px rgba(255, 165, 0, 0.3));
  }

  // 玻璃珠大眼睛 - 圆圆的闪亮效果
  .eye {
    width: 28px !important;
    height: 28px !important;
    box-shadow:
      inset 0 2px 4px rgba(0, 0, 0, 0.05),
      0 0 0 2px rgba(255, 182, 193, 0.5),
      0 0 14px rgba(255, 182, 193, 0.35); // 闪亮发光效果

    .pupil {
      width: 20px !important;
      height: 20px !important; // 圆圆的大瞳孔
      background: radial-gradient(circle at 30% 30%, #ff8faa, #ff6b8a); // 粉色瞳孔

      // 更亮的高光点
      &::before {
        width: 8px; height: 8px;
        background: rgba(255, 255, 255, 0.95);
        box-shadow: 0 0 6px rgba(255, 255, 255, 0.6);
      }
      &::after {
        width: 5px; height: 5px;
        background: rgba(255, 255, 255, 0.8);
      }
    }
  }
}

// ========== 通用动物部位 ==========
.eyes {
  position: absolute;
  top: 18px; left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 18px;
}

.eye {
  width: 26px; height: 26px; // 圆圆的大眼睛
  background: #fff;
  border-radius: 50%;
  position: relative;
  overflow: hidden;
  box-shadow:
    inset 0 2px 4px rgba(0, 0, 0, 0.05),
    0 0 0 2px rgba(135, 206, 235, 0.3),
    0 0 6px rgba(135, 206, 235, 0.15); // 蓝色边框+发光

  .pupil {
    position: absolute;
    width: 18px; height: 18px; // 圆圆的大瞳孔
    background: radial-gradient(circle at 35% 35%, #4a8fb5, #2d6a8a); // 蓝色瞳孔
    border-radius: 50%;
    top: 50%; left: 50%;
    transform: translate(-50%, -50%);
    transition: all 0.3s;

    // 高光点 - 水汪汪效果
    &::before {
      content: '';
      position: absolute;
      top: 2px; left: 3px;
      width: 7px; height: 7px;
      background: rgba(255, 255, 255, 0.95);
      border-radius: 50%;
      box-shadow: 0 0 4px rgba(255, 255, 255, 0.6);
    }
    // 第二个高光点
    &::after {
      content: '';
      position: absolute;
      bottom: 3px; right: 4px;
      width: 5px; height: 5px;
      background: rgba(255, 255, 255, 0.8);
      border-radius: 50%;
    }
  }
}

// 心情表情 (二次元风格 - 圆圆的可爱眼睛)
.eye-happy {
  width: 26px !important;
  height: 26px !important;
  border-radius: 50% !important;
  box-shadow: 0 0 0 2px rgba(255, 182, 193, 0.5), 0 0 8px rgba(255, 182, 193, 0.3) !important;
  .pupil {
    width: 14px !important;
    height: 14px !important;
    background: radial-gradient(circle at 35% 35%, #ff8faa, #ff6b8a) !important;
  }
}
.eye-sleepy {
  width: 22px !important;
  height: 22px !important;
  border-radius: 50% !important;
  box-shadow: 0 0 0 2px rgba(200, 180, 220, 0.5), 0 0 6px rgba(200, 180, 220, 0.2) !important;
  .pupil {
    width: 12px !important;
    height: 12px !important;
    opacity: 0.8;
  }
}
.eye-missing {
  width: 24px !important;
  height: 24px !important;
  border-radius: 50% !important;
  box-shadow: 0 0 0 2px rgba(180, 160, 200, 0.5), 0 0 8px rgba(180, 160, 200, 0.3) !important;
  .pupil {
    top: 60%;
    width: 16px !important;
    height: 16px !important;
  }
}
.eye-energetic {
  width: 28px !important;
  height: 28px !important;
  border-radius: 50% !important;
  box-shadow: 0 0 0 2px rgba(255, 182, 193, 0.6), 0 0 12px rgba(255, 182, 193, 0.4) !important;
  .pupil {
    width: 20px !important;
    height: 20px !important;
    box-shadow: 0 0 10px rgba(255, 182, 193, 0.5);
  }
}

.nose {
  position: absolute;
  top: 40px; left: 50%;
  transform: translateX(-50%);
  width: 14px; height: 10px; // 更大的鼻子
  background: radial-gradient(circle at 40% 30%, #ff9eb5, #ff8faa);
  border-radius: 50% 50% 40% 40%;
  box-shadow: 0 1px 3px rgba(255, 158, 181, 0.3);
}

.mouth {
  position: absolute;
  top: 50px; left: 50%;
  transform: translateX(-50%);
  width: 20px; height: 10px; // 更大的嘴巴
}
.mouth-happy {
  border-bottom: 2.5px solid #b8a0b8;
  border-radius: 0 0 50% 50%;
}
.mouth-energetic {
  width: 16px; height: 14px;
  background: radial-gradient(circle, #b8a0b8 40%, #9a8a9a);
  border-radius: 50%;
}
.mouth-sleepy {
  width: 14px; height: 5px;
  border-bottom: 2.5px solid #c8b0c8;
  border-radius: 0 0 50% 50%;
}
.mouth-missing {
  width: 16px; height: 8px;
  border-top: 2.5px solid #b8a0b8;
  border-radius: 50% 50% 0 0;
  transform: translateX(-50%) scaleY(-1);
}

.belly {
  position: absolute;
  bottom: 10px; left: 50%;
  transform: translateX(-50%);
  width: 48px; height: 38px; // 更大的肚子
  background: radial-gradient(circle at 50% 60%, rgba(255, 255, 255, 0.9), rgba(255, 240, 245, 0.7));
  border-radius: 50%;
  box-shadow: inset 0 -2px 4px rgba(255, 240, 245, 0.3);
}

.paw {
  position: absolute;
  bottom: -4px;
  width: 20px; height: 14px;
  background: inherit;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  &-left { left: 10px; }
  &-right { right: 10px; }

  // 粉色肉垫效果
  &::after {
    content: '';
    position: absolute;
    bottom: 2px; left: 50%;
    transform: translateX(-50%);
    width: 10px; height: 7px; // 更大的肉垫
    background: radial-gradient(circle, #ffb6c1, #ffa0b4); // 粉色肉垫
    border-radius: 50%;
    box-shadow: 0 1px 3px rgba(255, 182, 193, 0.3);
  }
}

.evolution-badge {
  position: absolute;
  bottom: -12px; left: 50%;
  transform: translateX(-50%);
  font-size: 28px; // 更大的进化徽章
  z-index: 3;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  animation: badge-float 2s ease-in-out infinite;
}

// ========== 台词气泡 (二次元风格) ==========
.speech-bubble {
  position: relative;
  background: linear-gradient(135deg, #fff5f8 0%, #f8f0ff 100%);
  border-radius: 24px; // 更圆润
  padding: 16px 28px;
  margin-top: 20px;
  font-size: 14px;
  color: #5a4a6a;
  text-align: center;
  border: 2px solid rgba(255, 182, 193, 0.3); // 更粗的边框
  box-shadow: 0 4px 16px rgba(255, 182, 193, 0.15);
  transition: transform 0.3s;

  &::before {
    content: '';
    position: absolute;
    top: -10px; left: 50%;
    transform: translateX(-50%);
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
    border-bottom: 10px solid #fff5f8;
  }

  // 添加小装饰点
  &::after {
    content: '💬';
    position: absolute;
    top: -15px;
    right: 20px;
    font-size: 16px;
    opacity: 0.6;
  }

  &.bubble-pop { animation: bubble-bounce 0.5s ease; }
}

// ========== 互动按钮 ==========
.interact-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.interact-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 14px;
  border: 2px solid rgba(255, 182, 193, 0.3);
  border-radius: 18px; // 更圆润
  background: linear-gradient(135deg, #fff 0%, #fff8fa 100%);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;

  &:hover:not(:disabled) {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(255, 105, 180, 0.2);
    border-color: rgba(255, 182, 193, 0.5);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .btn-emoji { font-size: 32px; } // 更大的 emoji
  .btn-label { font-size: 13px; color: #5a4a6a; font-weight: 500; }
  .btn-cooldown {
    position: absolute;
    top: 6px; right: 10px;
    font-size: 11px;
    color: #da70d6;
    background: linear-gradient(135deg, #fdf5ff, #f8f0ff);
    padding: 2px 8px;
    border-radius: 10px;
  }
}

.btn-pet:hover:not(:disabled) { border-color: #ff69b4; background: linear-gradient(135deg, #fff5f8, #ffe8f0); }
.btn-feed:hover:not(:disabled) { border-color: #da70d6; background: linear-gradient(135deg, #fdf5ff, #f8f0ff); }
.btn-play:hover:not(:disabled) { border-color: #ba68c8; background: linear-gradient(135deg, #f8f4ff, #f0e8ff); }
.btn-bath:hover:not(:disabled) { border-color: #ce93d8; background: linear-gradient(135deg, #f0f8ff, #e8f0ff); }
.btn-sleep:hover:not(:disabled) { border-color: #e1bee7; background: linear-gradient(135deg, #fdf5ff, #f8f0ff); }
.btn-dance:hover:not(:disabled) { border-color: #f48fb1; background: linear-gradient(135deg, #fff0f3, #ffe8f0); }

// ========== 互动特效 (二次元风格) ==========
.interact-effect {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  pointer-events: none;

  .effect-emoji {
    position: absolute;
    font-size: 28px; // 更大的特效 emoji
    animation: effect-float 1.2s ease-out forwards;
    animation-delay: var(--delay, 0s);
    transform: rotate(var(--angle, 0deg));
  }
}

// ========== 点击特效 ==========
.click-effects-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 20;
  overflow: hidden;
  border-radius: 16px; // 与卡片圆角一致
}

// 点击特效容器覆盖整个宠物场景
.pet-stage {
  .click-effects-container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none; // 确保不影响点击事件
  }
}

.click-particle {
  position: absolute;
  font-size: 28px; // 更大的粒子
  pointer-events: none;
  animation: click-particle-float 0.8s ease-out forwards;
  animation-delay: var(--delay, 0s);
  opacity: 0;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2)); // 添加阴影
  z-index: 25; // 确保在最上层
  will-change: transform, opacity; // 优化动画性能
}

.click-particle-pet {
  animation-name: click-particle-pet;
  color: #ff69b4; // 粉色
  text-shadow: 0 0 25px rgba(255, 105, 180, 1); // 添加发光效果
}

.click-particle-pet {
  animation-name: click-particle-pet;
  color: #ffb6c1; // 浅粉色
  text-shadow: 0 0 25px rgba(255, 182, 193, 1); // 添加发光效果
}

.click-particle-feed {
  animation-name: click-particle-feed;
  color: #da70d6; // 紫色
  text-shadow: 0 0 25px rgba(218, 112, 214, 1); // 添加发光效果
}

.click-ripple {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  pointer-events: none;
  transform: translate(-50%, -50%);
  border: 2px solid rgba(255, 255, 255, 0.5); // 添加白色边框
  z-index: 24; // 确保在粒子下面
  will-change: width, height, opacity; // 优化动画性能
}

.click-ripple-pet {
  background: radial-gradient(circle, rgba(255, 105, 180, 0.6) 0%, rgba(255, 105, 180, 0) 70%);
  animation: click-ripple-pet 0.8s ease-out forwards;
  box-shadow: 0 0 35px rgba(255, 105, 180, 0.8); // 添加发光效果
}

.click-ripple-pet {
  background: radial-gradient(circle, rgba(255, 182, 193, 0.6) 0%, rgba(255, 182, 193, 0) 70%);
  animation: click-ripple-pet 0.8s ease-out forwards;
  box-shadow: 0 0 35px rgba(255, 182, 193, 0.8); // 添加发光效果
}

.click-ripple-feed {
  background: radial-gradient(circle, rgba(218, 112, 214, 0.6) 0%, rgba(218, 112, 214, 0) 70%);
  animation: click-ripple-feed 0.8s ease-out forwards;
  box-shadow: 0 0 35px rgba(218, 112, 214, 0.8); // 添加发光效果
}

@keyframes effect-float {
  0% { opacity: 1; transform: translateY(0) scale(0.5); }
  50% { opacity: 1; transform: translateY(-50px) scale(1.3); }
  100% { opacity: 0; transform: translateY(-100px) scale(0.9); }
}

// ========== 点击特效动画 ==========
@keyframes click-particle-pet {
  0% {
    opacity: 1;
    transform: translate(0, 0) scale(var(--scale, 1)) rotate(0deg);
  }
  50% {
    opacity: 1;
    transform: translate(calc(var(--tx, 0) * 0.5), calc(var(--ty, 0) * 0.5)) scale(1.2) rotate(calc(var(--rotation, 0deg) * 0.5));
  }
  100% {
    opacity: 0;
    transform: translate(var(--tx, 0), var(--ty, 0)) scale(0) rotate(var(--rotation, 0deg));
  }
}

@keyframes click-particle-pet {
  0% {
    opacity: 1;
    transform: translate(0, 0) scale(var(--scale, 1)) rotate(0deg);
  }
  30% {
    opacity: 1;
    transform: translate(calc(var(--tx, 0) * 0.3), calc(var(--ty, 0) * 0.3)) scale(1.3) rotate(calc(var(--rotation, 0deg) * 0.3));
  }
  100% {
    opacity: 0;
    transform: translate(var(--tx, 0), var(--ty, 0)) scale(0) rotate(var(--rotation, 0deg));
  }
}

@keyframes click-particle-feed {
  0% {
    opacity: 1;
    transform: translate(0, 0) scale(var(--scale, 1)) rotate(0deg);
  }
  40% {
    opacity: 1;
    transform: translate(calc(var(--tx, 0) * 0.4), calc(var(--ty, 0) * 0.4)) scale(1.4) rotate(calc(var(--rotation, 0deg) * 0.4));
  }
  100% {
    opacity: 0;
    transform: translate(var(--tx, 0), var(--ty, 0)) scale(0) rotate(var(--rotation, 0deg));
  }
}

@keyframes click-ripple-pet {
  0% {
    width: 10px;
    height: 10px;
    opacity: 0.8;
  }
  50% {
    opacity: 0.4;
  }
  100% {
    width: 160px;
    height: 160px;
    opacity: 0;
  }
}

@keyframes click-ripple-pet {
  0% {
    width: 10px;
    height: 10px;
    opacity: 0.8;
  }
  50% {
    opacity: 0.4;
  }
  100% {
    width: 130px;
    height: 130px;
    opacity: 0;
  }
}

@keyframes click-ripple-feed {
  0% {
    width: 10px;
    height: 10px;
    opacity: 0.8;
  }
  50% {
    opacity: 0.4;
  }
  100% {
    width: 140px;
    height: 140px;
    opacity: 0;
  }
}

// ========== 宠物形象选择 ==========
.pet-type-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.type-card {
  text-align: center;
  padding: 22px 14px;
  border: 2px solid rgba(255, 182, 193, 0.2);
  border-radius: 18px; // 更圆润
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #fff 0%, #fff8fa 100%);

  &:hover {
    border-color: #ff69b4;
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(255, 105, 180, 0.15);
  }

  &.active {
    border-color: #ff69b4;
    background: linear-gradient(135deg, #fff5f8 0%, #f8f0ff 100%);
    box-shadow: 0 8px 24px rgba(255, 105, 180, 0.2);

    // 激活状态的发光效果
    &::before {
      content: '';
      position: absolute;
      top: -2px; left: -2px; right: -2px; bottom: -2px;
      background: linear-gradient(135deg, rgba(255, 105, 180, 0.1), rgba(155, 127, 230, 0.1));
      border-radius: inherit;
      z-index: -1;
    }
  }

  .type-icon { font-size: 44px; margin-bottom: 12px; } // 更大的图标
  .type-name { font-size: 15px; font-weight: 600; color: #3d2b4a; }
  .type-desc { font-size: 12px; color: #b8a0c8; margin-top: 8px; }
}

// ========== 右侧边栏 ==========
.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3e4ee;

  &:last-of-type { border-bottom: none; }

  .stat-label { font-size: 13px; color: #9b8aad; }
  .stat-value {
    font-size: 14px;
    font-weight: 600;
    color: #3d2b4a;

    &.evolution {
      color: #ff6b9d;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    &.highlight { color: #9b7fe6; font-size: 18px; } // 更大的高亮数字
    &.mood-happy { color: #ff69b4; }
    &.mood-energetic { color: #da70d6; }
    &.mood-sleepy { color: #c8a2c8; }
    &.mood-missing { color: #b8a0d0; }

    .pet-level-badge {
      font-size: 11px;
      color: #ff6b9d;
      background: linear-gradient(135deg, #fff5f8, #ffe8f0);
      padding: 2px 6px;
      border-radius: 8px;
      border: 1px solid rgba(255, 107, 157, 0.2);
      font-weight: 600;
    }
  }
}

.happiness-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3e4ee;

  .happiness-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 13px;
    color: #9b8aad;

    .happiness-num {
      font-weight: 700;
      font-size: 16px;
      color: #3d2b4a;
    }
  }
}

.level-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3e4ee;

  .level-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    font-size: 13px;
    color: #9b8aad;

    .level-num {
      font-weight: 700;
      font-size: 18px;
      color: #ff6b9d;
      text-shadow: 0 0 8px rgba(255, 107, 157, 0.3);
    }
  }

  .level-progress {
    .level-bar {
      width: 100%;
      height: 12px;
      background: linear-gradient(135deg, #f3e4ee, #e8d5f0);
      border-radius: 6px;
      overflow: hidden;
      box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);

      .level-fill {
        height: 100%;
        background: linear-gradient(90deg, #ff6b9d, #da70d6, #9b7fe6);
        border-radius: 6px;
        transition: width 0.5s ease;
        box-shadow: 0 0 8px rgba(255, 107, 157, 0.3);
      }
    }

    .level-text {
      display: flex;
      justify-content: space-between;
      margin-top: 6px;
      font-size: 12px;
      color: #b8a0c8;

      .level-hint {
        color: #9b8aad;
        font-size: 11px;
      }
    }
  }
}

.quick-links {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;

  .el-button {
    width: 100%;
    border-color: rgba(255, 182, 193, 0.3);
    color: #5a4a6a;
    border-radius: 14px; // 更圆润
    transition: all 0.3s ease;
    font-weight: 500;

    &:hover {
      border-color: #ff69b4;
      color: #ff69b4;
      background: linear-gradient(135deg, #fff5f8, #ffe8f0);
      transform: translateY(-3px);
      box-shadow: 0 6px 16px rgba(255, 105, 180, 0.15);
    }
  }
}

// ========== 成长日志 ==========
.log-timeline {
  max-height: 400px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f8f4fb;

  &:last-child { border-bottom: none; }
}

.log-dot {
  width: 12px; height: 12px; // 更大的圆点
  border-radius: 50%;
  margin-top: 4px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #ffc8d6, #ffb6c1);
  box-shadow: 0 0 6px rgba(255, 182, 193, 0.4);

  &.dot-pet { background: linear-gradient(135deg, #ff69b4, #ff8faa); }
  &.dot-feed { background: linear-gradient(135deg, #da70d6, #e1bee7); }
  &.dot-play { background: linear-gradient(135deg, #ba68c8, #ce93d8); }
  &.dot-bath { background: linear-gradient(135deg, #9c27b0, #ab47bc); }
  &.dot-sleep { background: linear-gradient(135deg, #e1bee7, #f3e5f5); }
  &.dot-dance { background: linear-gradient(135deg, #f48fb1, #f8bbd0); }
}

.log-content {
  flex: 1;
  min-width: 0;
}

.log-action {
  font-size: 14px;
  font-weight: 600;
  color: #3d2b4a;
}

.log-meta {
  display: flex;
  gap: 12px;
  margin-top: 4px;
  font-size: 12px;

  .log-mood { color: #b8a0c8; }
  .log-happiness { color: #da70d6; }
}

.log-time {
  font-size: 12px;
  color: #b8a9c9;
  margin-top: 2px;
}

.empty-log {
  text-align: center;
  padding: 35px 0;

  .empty-icon { font-size: 44px; margin-bottom: 14px; } // 更大的图标
  .empty-text { font-size: 14px; color: #b8a9c9; }
}

// ========== 改名弹窗 (二次元风格) ==========
.rename-icon {
  color: #b8a9c9;
  cursor: pointer;
  font-size: 16px; // 更大的图标
  margin-left: 6px;
  transition: all 0.3s ease;

  &:hover {
    color: #ff6b9d;
    transform: scale(1.1);
  }
}

.pet-name-plate {
  text-align: center;
  margin-top: 12px;
  position: relative;
  z-index: 3;

  .plate-name {
    font-size: 20px;
    font-weight: 700;
    color: #3d2b4a;
    text-shadow: 0 1px 2px rgba(255, 182, 193, 0.3);
    background: linear-gradient(135deg, #ff6b9d, #9b7fe6);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .rename-icon {
    margin-left: 6px;
    font-size: 16px;
    color: #b8a9c9;
    transition: all 0.3s ease;

    &:hover {
      color: #ff6b9d;
      transform: scale(1.1);
    }
  }
}

// ========== 关键帧动画 (二次元风格) ==========
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); } // 更高的弹跳
}

@keyframes wiggle {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); } // 更大的摇摆
  75% { transform: rotate(10deg); }
}

@keyframes breathe {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.04); } // 更明显的呼吸效果
}

@keyframes sway {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-6deg); }
  75% { transform: rotate(6deg); }
}

@keyframes tremble {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-4px); }
  40% { transform: translateX(4px); }
  60% { transform: translateX(-3px); }
  80% { transform: translateX(3px); }
}

@keyframes jump {
  0% { transform: translateY(0); }
  40% { transform: translateY(-25px); } // 更高的跳跃
  60% { transform: translateY(-25px); }
  100% { transform: translateY(0); }
}

@keyframes tail-wag {
  0%, 100% { transform: rotate(-12deg); }
  50% { transform: rotate(12deg); }
}

@keyframes tail-wag-fast {
  0%, 100% { transform: rotate(-25deg); } // 更快的尾巴摇摆
  50% { transform: rotate(25deg); }
}

@keyframes tongue-pant {
  0%, 100% { transform: translateX(-50%) scaleY(1); }
  50% { transform: translateX(-50%) scaleY(1.3); } // 更明显的舌头伸缩
}

@keyframes cloud-float {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(35px); } // 更大的漂浮距离
}

@keyframes star-twinkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.3; transform: scale(0.7); } // 更明显的闪烁效果
}

@keyframes bubble-bounce {
  0% { transform: scale(1); }
  30% { transform: scale(1.06); } // 更明显的弹跳效果
  60% { transform: scale(0.97); }
  100% { transform: scale(1); }
}

@keyframes petal-fall {
  0% { transform: translateY(0) rotate(0deg); opacity: 0.7; }
  50% { transform: translateY(130px) rotate(180deg); opacity: 0.9; }
  100% { transform: translateY(260px) rotate(360deg); opacity: 0; }
}

@keyframes carrot-hold {
  0%, 100% { transform: translateX(-50%) rotate(-6deg); }
  50% { transform: translateX(-50%) rotate(6deg); }
}

@keyframes flower-sway {
  0%, 100% { transform: rotate(-6deg) scale(1); }
  50% { transform: rotate(6deg) scale(1.06); }
}

@keyframes heart-float {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.5; }
  50% { transform: translateY(-12px) scale(1.15); opacity: 0.7; }
}

@keyframes sparkle-twinkle {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 0.2; transform: scale(0.6); } // 更明显的闪烁效果
}

@keyframes sparkle-appear {
  0% { opacity: 0; transform: scale(0) rotate(0deg); }
  50% { opacity: 1; transform: scale(1.2) rotate(180deg); }
  100% { opacity: 0; transform: scale(0) rotate(360deg); }
}

@keyframes badge-float {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50% { transform: translateX(-50%) translateY(-5px); }
}
</style>
