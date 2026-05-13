<template>
  <div class="rank-card" :class="rankClass">
    <div class="rank-header">
      <div class="rank-badge" :class="'badge-' + rankData.rankLevel">
        <div class="badge-icon">{{ rankIcon }}</div>
      </div>
      <div class="rank-info">
        <div class="rank-title">{{ rankData.rankName }}</div>
        <div class="rank-en">{{ rankData.rankEnName }}</div>
      </div>
      <div class="rank-points">
        <span class="points-value">{{ rankData.points }}</span>
        <span class="points-label">积分</span>
      </div>
    </div>

    <div class="rank-stats">
      <div class="stat-item">
        <span class="stat-icon">🔥</span>
        <span class="stat-value">{{ rankData.currentStreak }}</span>
        <span class="stat-label">连续打卡</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon">🏆</span>
        <span class="stat-value">{{ rankData.maxStreak }}</span>
        <span class="stat-label">最高纪录</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon">📅</span>
        <span class="stat-value">+{{ rankData.dailyPoints || 10 }}</span>
        <span class="stat-label">每日打卡</span>
      </div>
    </div>

    <div v-if="rankData.nextRankName" class="rank-progress">
      <div class="progress-info">
        <span>距离「{{ rankData.nextRankName }}」</span>
        <span>还需 {{ daysToNext }} 天</span>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      </div>
    </div>
    <div v-else class="rank-max">
      🎉 已达成最高段位！
    </div>

    <div class="rank-actions">
      <el-button text type="primary" @click="showRanks = true">查看所有段位</el-button>
    </div>

    <!-- 段位总览弹窗 -->
    <el-dialog v-model="showRanks" title="段位总览" width="500px">
      <div class="rank-list">
        <div
          v-for="rank in (rankData.allRanks || [])"
          :key="rank.level"
          class="rank-item"
          :class="{ current: rank.level === rankData.rankLevel, reached: rank.level <= rankData.rankLevel }"
        >
          <div class="rank-item-badge" :class="'badge-' + rank.level">
            <span class="badge-icon">{{ rank.icon }}</span>
          </div>
          <div class="rank-item-info">
            <div class="rank-item-name">
              {{ rank.name }}
              <span v-if="rank.level === rankData.rankLevel" class="current-tag">当前</span>
            </div>
            <div class="rank-item-en">{{ rank.enName }}</div>
          </div>
          <div class="rank-item-req">
            <div v-if="rank.requiredDays > 0" class="req-days">连续 {{ rank.requiredDays }} 天</div>
            <div v-else class="req-days">初始段位</div>
            <div v-if="rank.milestoneReward > 0" class="req-reward">+{{ rank.milestoneReward }} 积分</div>
          </div>
          <div class="rank-item-status">
            <span v-if="rank.level <= rankData.rankLevel" class="status-reached">✓</span>
            <span v-else class="status-locked">🔒</span>
          </div>
        </div>
      </div>
      <div class="rank-rules">
        <p>📌 积分规则：</p>
        <ul>
          <li>每天打卡（记录饮食/运动/体重）获得 <strong>+10 积分</strong></li>
          <li>连续打卡达到里程碑天数获得额外积分奖励</li>
          <li>段位根据历史最高连续天数提升，不会降级</li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getRankInfo, checkIn } from '../api/rank'

const route = useRoute()
const showRanks = ref(false)

const defaultRank = {
  points: 0, rankLevel: 1, rankName: '觉醒学徒', rankEnName: 'Awakening Apprentice',
  currentStreak: 0, maxStreak: 0, nextRankName: '燃脂工匠', nextRankDays: 5,
  dailyPoints: 10, allRanks: []
}
const rankData = ref({ ...defaultRank })

async function loadRank() {
  try {
    // 先触发打卡检查（会更新积分），再获取最新数据
    await checkIn()
    const res = await getRankInfo()
    rankData.value = res.data
  } catch (e) {
    console.error('加载段位数据失败:', e)
  }
}

onMounted(() => loadRank())
onActivated(() => loadRank())

watch(() => route.path, (path) => {
  if (path === '/dashboard') loadRank()
})

const rankIcons = { 1: '🌱', 2: '⚒️', 3: '🏃', 4: '🔥', 5: '💎', 6: '🚀', 7: '👑' }
const rankIcon = computed(() => rankIcons[rankData.value.rankLevel] || '🌱')

const rankClass = computed(() => {
  const level = rankData.value.rankLevel
  if (level >= 5) return 'rank-high'
  if (level >= 3) return 'rank-mid'
  return 'rank-low'
})

const daysToNext = computed(() => {
  if (!rankData.value.nextRankDays) return 0
  return Math.max(0, rankData.value.nextRankDays - rankData.value.currentStreak)
})

const progressPercent = computed(() => {
  if (!rankData.value.nextRankDays) return 100
  const current = rankData.value.currentStreak
  const target = rankData.value.nextRankDays
  return Math.min(100, Math.round((current / target) * 100))
})
</script>

<style scoped lang="scss">
.rank-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  &.rank-high {
    background: linear-gradient(135deg, #fff 0%, #fef9e7 100%);
    .progress-fill { background: linear-gradient(90deg, #f39c12, #e74c3c); }
  }
  &.rank-mid { .progress-fill { background: linear-gradient(90deg, #3498db, #2ecc71); } }
  &.rank-low { .progress-fill { background: linear-gradient(90deg, #95a5a6, #7f8c8d); } }
}

.rank-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }

.rank-badge {
  width: 56px; height: 56px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  .badge-icon { font-size: 28px; }
  &.badge-1 { background: linear-gradient(135deg, #8d6e63, #a1887f); box-shadow: 0 2px 8px rgba(141,110,99,0.4); }
  &.badge-2 { background: linear-gradient(135deg, #78909c, #90a4ae); box-shadow: 0 2px 8px rgba(120,144,156,0.4); }
  &.badge-3 { background: linear-gradient(135deg, #bf8040, #d4a06a); box-shadow: 0 2px 8px rgba(191,128,64,0.4); }
  &.badge-4 { background: linear-gradient(135deg, #c0c0c0, #e0e0e0); box-shadow: 0 2px 8px rgba(192,192,192,0.5); animation: shimmer 3s infinite; }
  &.badge-5 { background: linear-gradient(135deg, #ffd700, #ffed4a); box-shadow: 0 2px 12px rgba(255,215,0,0.5); animation: glow 2s infinite; }
  &.badge-6 { background: linear-gradient(135deg, #e0f7fa, #b2ebf2, #e0f7fa); box-shadow: 0 2px 16px rgba(0,188,212,0.4); animation: crystal 2.5s infinite; }
  &.badge-7 { background: linear-gradient(135deg, #455a64, #ffd700, #455a64); box-shadow: 0 2px 20px rgba(255,215,0,0.6); animation: titanium 2s infinite; }
}

@keyframes shimmer { 0%,100%{box-shadow:0 2px 8px rgba(192,192,192,0.5)} 50%{box-shadow:0 2px 16px rgba(192,192,192,0.8)} }
@keyframes glow { 0%,100%{box-shadow:0 2px 12px rgba(255,215,0,0.5)} 50%{box-shadow:0 4px 24px rgba(255,215,0,0.8)} }
@keyframes crystal { 0%,100%{box-shadow:0 2px 16px rgba(0,188,212,0.4)} 50%{box-shadow:0 4px 24px rgba(0,188,212,0.7),0 0 12px rgba(255,0,128,0.2)} }
@keyframes titanium { 0%,100%{box-shadow:0 2px 20px rgba(255,215,0,0.6)} 50%{box-shadow:0 4px 30px rgba(255,215,0,0.9),0 0 20px rgba(69,90,100,0.5)} }

.rank-info {
  flex: 1;
  .rank-title { font-size: 18px; font-weight: 700; color: #3d2b4a; }
  .rank-en { font-size: 12px; color: #9b8aad; margin-top: 2px; }
}

.rank-points {
  text-align: right;
  .points-value { display: block; font-size: 24px; font-weight: 700; color: #ff6b9d; }
  .points-label { font-size: 12px; color: #9b8aad; }
}

.rank-stats {
  display: flex; gap: 24px; margin-bottom: 16px;
  .stat-item { display: flex; align-items: center; gap: 6px; }
  .stat-icon { font-size: 18px; }
  .stat-value { font-size: 20px; font-weight: 700; color: #3d2b4a; }
  .stat-label { font-size: 12px; color: #9b8aad; }
}

.rank-progress {
  .progress-info { display: flex; justify-content: space-between; font-size: 12px; color: #6b5a7a; margin-bottom: 6px; }
  .progress-bar { height: 8px; background: #f3e4ee; border-radius: 4px; overflow: hidden; }
  .progress-fill { height: 100%; border-radius: 4px; transition: width 0.6s ease; }
}

.rank-max { text-align: center; font-size: 14px; color: #ff6b9d; font-weight: 600; padding: 8px 0; }

.rank-actions { text-align: center; margin-top: 8px; border-top: 1px solid #f3e4ee; padding-top: 8px; }

.rank-list { margin-bottom: 16px; }

.rank-item {
  display: flex; align-items: center; gap: 12px; padding: 12px;
  border-radius: 8px; margin-bottom: 8px; background: #fafafa;
  border: 2px solid transparent; transition: all 0.2s;
  &.current { border-color: #ff6b9d; background: #fef6f9; }
  &.reached { background: #f0faf0; }
}

.rank-item-badge {
  width: 44px; height: 44px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  .badge-icon { font-size: 22px; }
  &.badge-1 { background: linear-gradient(135deg, #8d6e63, #a1887f); }
  &.badge-2 { background: linear-gradient(135deg, #78909c, #90a4ae); }
  &.badge-3 { background: linear-gradient(135deg, #bf8040, #d4a06a); }
  &.badge-4 { background: linear-gradient(135deg, #c0c0c0, #e0e0e0); }
  &.badge-5 { background: linear-gradient(135deg, #ffd700, #ffed4a); }
  &.badge-6 { background: linear-gradient(135deg, #e0f7fa, #b2ebf2, #e0f7fa); }
  &.badge-7 { background: linear-gradient(135deg, #455a64, #ffd700, #455a64); }
}

.rank-item-info {
  flex: 1;
  .rank-item-name { font-size: 15px; font-weight: 600; color: #3d2b4a; }
  .current-tag { font-size: 11px; background: #ff6b9d; color: #fff; padding: 1px 6px; border-radius: 8px; margin-left: 6px; }
  .rank-item-en { font-size: 11px; color: #9b8aad; }
}

.rank-item-req {
  text-align: right;
  .req-days { font-size: 13px; color: #6b5a7a; font-weight: 500; }
  .req-reward { font-size: 12px; color: #ff6b9d; }
}

.rank-item-status {
  width: 24px; text-align: center;
  .status-reached { color: #2ecc71; font-size: 16px; }
  .status-locked { font-size: 14px; }
}

.rank-rules {
  background: #fef6f9; border-radius: 8px; padding: 12px 16px;
  p { font-size: 14px; font-weight: 600; color: #3d2b4a; margin: 0 0 8px; }
  ul { margin: 0; padding-left: 20px; }
  li { font-size: 13px; color: #6b5a7a; line-height: 1.8; }
  strong { color: #ff6b9d; }
}
</style>
