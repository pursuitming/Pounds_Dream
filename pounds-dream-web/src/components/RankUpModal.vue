<template>
  <el-dialog
    v-model="visible"
    :show-close="false"
    :close-on-click-modal="false"
    width="400px"
    class="rank-up-dialog"
    @closed="$emit('close')"
  >
    <div class="rank-up-content" :class="'rank-' + newRank.rankLevel">
      <!-- 粒子效果 -->
      <div class="particles">
        <span v-for="i in 20" :key="i" class="particle" :style="particleStyle(i)"></span>
      </div>

      <!-- 升级文字 -->
      <div class="upgrade-text">段位提升!</div>

      <!-- 新段位徽章 -->
      <div class="new-badge" :class="'badge-' + newRank.rankLevel">
        <div class="badge-icon">{{ rankIcon }}</div>
      </div>

      <!-- 段位信息 -->
      <div class="rank-name">{{ newRank.rankName }}</div>
      <div class="rank-en-name">{{ newRank.rankEnName }}</div>

      <!-- 积分奖励 -->
      <div v-if="newRank.rankUpPoints" class="points-reward">
        +{{ newRank.rankUpPoints }} 积分
      </div>

      <!-- 确认按钮 -->
      <el-button type="primary" class="confirm-btn" @click="visible = false">
        太棒了!
      </el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  newRank: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const rankIcons = { 1: '🌱', 2: '⚒️', 3: '🏃', 4: '🔥', 5: '💎', 6: '🚀', 7: '👑' }
const rankIcon = computed(() => rankIcons[props.newRank.rankLevel] || '🌱')

function particleStyle(i) {
  const angle = (i / 20) * Math.PI * 2
  const delay = (i * 0.15) % 2
  const size = 4 + (i % 3) * 2
  const distance = 80 + (i % 4) * 15
  const x = Math.cos(angle) * distance
  const y = Math.sin(angle) * distance
  return {
    '--x': `${x}px`,
    '--y': `${y}px`,
    '--delay': `${delay}s`,
    '--size': `${size}px`
  }
}
</script>

<style scoped lang="scss">
.rank-up-content {
  text-align: center;
  padding: 20px 0;
  position: relative;
  overflow: hidden;

  // 代谢引擎及以上有脉冲背景
  &.rank-4, &.rank-5, &.rank-6, &.rank-7 {
    animation: pulse-bg 2s infinite;
  }
}

@keyframes pulse-bg {
  0%, 100% { background: transparent; }
  50% { background: rgba(255, 215, 0, 0.03); }
}

.particles {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;

  .particle {
    position: absolute;
    width: var(--size);
    height: var(--size);
    border-radius: 50%;
    background: #ff6b9d;
    animation: burst 2s var(--delay) infinite;
    opacity: 0;
  }
}

@keyframes burst {
  0% { transform: translate(0, 0) scale(1); opacity: 1; }
  100% { transform: translate(var(--x), var(--y)) scale(0); opacity: 0; }
}

.upgrade-text {
  font-size: 14px;
  color: #9b8aad;
  margin-bottom: 16px;
  letter-spacing: 4px;
}

.new-badge {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;

  .badge-icon {
    font-size: 40px;
  }

  &.badge-1 { background: linear-gradient(135deg, #8d6e63, #a1887f); box-shadow: 0 4px 16px rgba(141,110,99,0.4); }
  &.badge-2 { background: linear-gradient(135deg, #78909c, #90a4ae); box-shadow: 0 4px 16px rgba(120,144,156,0.4); }
  &.badge-3 { background: linear-gradient(135deg, #bf8040, #d4a06a); box-shadow: 0 4px 16px rgba(191,128,64,0.4); }
  &.badge-4 { background: linear-gradient(135deg, #c0c0c0, #e0e0e0); box-shadow: 0 4px 20px rgba(192,192,192,0.6); animation: shimmer 1.5s infinite; }
  &.badge-5 { background: linear-gradient(135deg, #ffd700, #ffed4a); box-shadow: 0 4px 24px rgba(255,215,0,0.6); animation: glow 1.5s infinite; }
  &.badge-6 { background: linear-gradient(135deg, #e0f7fa, #b2ebf2, #e0f7fa); box-shadow: 0 4px 28px rgba(0,188,212,0.5); animation: crystal 2s infinite; }
  &.badge-7 { background: linear-gradient(135deg, #455a64, #ffd700, #455a64); box-shadow: 0 4px 32px rgba(255,215,0,0.7); animation: titanium 1.5s infinite; }
}

@keyframes shimmer {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); box-shadow: 0 6px 24px rgba(192,192,192,0.8); }
}

@keyframes glow {
  0%, 100% { transform: scale(1); box-shadow: 0 4px 24px rgba(255,215,0,0.6); }
  50% { transform: scale(1.08); box-shadow: 0 6px 32px rgba(255,215,0,0.9); }
}

@keyframes crystal {
  0%, 100% { box-shadow: 0 4px 28px rgba(0,188,212,0.5); }
  50% { box-shadow: 0 6px 36px rgba(0,188,212,0.8), 0 0 16px rgba(255,0,128,0.3); }
}

@keyframes titanium {
  0%, 100% { box-shadow: 0 4px 32px rgba(255,215,0,0.7); }
  50% { box-shadow: 0 6px 40px rgba(255,215,0,1), 0 0 24px rgba(69,90,100,0.6); }
}

.rank-name {
  font-size: 24px;
  font-weight: 700;
  color: #3d2b4a;
  margin-bottom: 4px;
}

.rank-en-name {
  font-size: 13px;
  color: #9b8aad;
  margin-bottom: 12px;
}

.points-reward {
  font-size: 18px;
  font-weight: 700;
  color: #ff6b9d;
  margin-bottom: 20px;
}

.confirm-btn {
  width: 120px;
  height: 36px;
  border-radius: 18px;
  background: #ff6b9d;
  border-color: #ff6b9d;
  &:hover { background: #e85585; border-color: #e85585; }
}
</style>
