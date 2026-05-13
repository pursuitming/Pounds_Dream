<template>
  <el-popover placement="bottom-end" :width="360" trigger="click" @show="fetchNotifications">
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
        <el-icon :size="20" class="bell-icon"><Bell /></el-icon>
      </el-badge>
    </template>

    <div class="notification-panel">
      <div class="notification-header">
        <span>通知</span>
        <el-button v-if="notifications.length > 0" type="primary" link size="small" @click="handleMarkAllRead">全部已读</el-button>
      </div>
      <div v-if="notifications.length === 0" class="empty-notif">暂无通知</div>
      <div v-for="item in notifications" :key="item.id" class="notif-item" :class="{ unread: !item.isRead }" @click="handleRead(item)">
        <div class="notif-title">{{ item.title }}</div>
        <div class="notif-content">{{ formatContent(item.content) }}</div>
        <div class="notif-time">{{ formatTime(item.createdAt) }}</div>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '../api/notification'

const notifications = ref([])
const unreadCount = ref(0)

async function fetchNotifications() {
  try {
    const [listRes, countRes] = await Promise.all([
      getNotifications(),
      getUnreadCount()
    ])
    notifications.value = listRes.data
    unreadCount.value = countRes.data
  } catch (e) {}
}

async function handleRead(item) {
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {}
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
  } catch (e) {}
}

function formatContent(content) {
  if (!content) return ''
  const parts = content.split('|')
  return parts.length > 1 ? parts[parts.length - 1] : content
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString()
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped lang="scss">
.notification-badge {
  cursor: pointer;
  .bell-icon {
    color: #6b5a7a;
    &:hover { color: #ff6b9d; }
  }
}

.notification-panel {
  max-height: 400px;
  overflow-y: auto;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #edd6e6;
  margin-bottom: 8px;
  font-weight: 600;
  color: #3d2b4a;
}

.empty-notif {
  text-align: center;
  color: #9b8aad;
  padding: 20px 0;
  font-size: 13px;
}

.notif-item {
  padding: 8px 0;
  border-bottom: 1px solid #f3e4ee;
  cursor: pointer;
  &:last-child { border-bottom: none; }
  &:hover { background: #fef6f9; }

  &.unread {
    .notif-title { font-weight: 600; }
    &::before {
      content: '';
      display: inline-block;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #ff6b9d;
      margin-right: 6px;
      vertical-align: middle;
    }
  }

  .notif-title { font-size: 14px; color: #3d2b4a; }
  .notif-content { font-size: 12px; color: #6b5a7a; margin: 4px 0; }
  .notif-time { font-size: 11px; color: #9b8aad; }
}
</style>
