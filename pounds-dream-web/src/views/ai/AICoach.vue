<template>
  <div class="ai-coach-page">
    <!-- 左侧对话列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <div class="sidebar-title">
          <span class="sidebar-title-icon">🏋️</span>
          <span>AI 健身教练</span>
        </div>
        <el-button type="primary" class="new-chat-btn" @click="newConversation">
          + 新建对话
        </el-button>
      </div>
      <div class="conversation-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conversation-item"
          :class="{ active: currentConversationId === conv.id }"
          @click="selectConversation(conv.id)"
        >
          <div class="conv-title">{{ conv.title }}</div>
          <div class="conv-time">{{ formatTime(conv.updatedAt) }}</div>
          <el-icon class="delete-btn" @click.stop="handleDelete(conv.id)"><Delete /></el-icon>
        </div>
        <div v-if="conversations.length === 0" class="empty-tip">
          暂无对话记录
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-container">
      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome">
          <div class="welcome-icon">🥗</div>
          <h2>AI 健康教练</h2>
          <p>告诉我你吃了什么，我来帮你分析营养成分</p>
          <div class="quick-actions">
            <div class="quick-btn" @click="sendQuick('我今天早餐吃了一个鸡蛋和一杯250ml全脂牛奶')">🥚 早餐分析</div>
            <div class="quick-btn" @click="sendQuick('我中午吃了一碗牛肉面，大概400g')">🍜 午餐分析</div>
            <div class="quick-btn" @click="sendQuick('帮我推荐一顿500大卡的减脂晚餐')">🍽️ 晚餐推荐</div>
            <div class="quick-btn" @click="sendQuick('跑步30分钟能消耗多少热量？')">🏃 运动咨询</div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(msg, idx) in messages" :key="idx" class="message" :class="msg.role">
          <div class="message-avatar">
            <span v-if="msg.role === 'user'">👤</span>
            <span v-else>🥗</span>
          </div>
          <div class="message-body">
            <div class="message-content">
              <!-- 思考过程（可折叠） -->
              <div v-if="msg.thinking" class="thinking-block" :class="{ expanded: msg.thinkingExpanded }">
                <div class="thinking-header" @click="msg.thinkingExpanded = !msg.thinkingExpanded">
                  <span class="thinking-icon">💭</span>
                  <span>思考过程</span>
                  <span class="thinking-toggle">{{ msg.thinkingExpanded ? '收起' : '展开' }}</span>
                </div>
                <div v-show="msg.thinkingExpanded" class="thinking-content">{{ msg.thinking }}</div>
              </div>
              <!-- 正式回答 -->
              <div class="text-content" v-html="renderMarkdown(msg.content)"></div>
              <!-- 打字光标 -->
              <span v-if="msg.loading && !msg.thinking" class="typing-cursor">|</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="告诉我你吃了什么，或问我任何健康问题..."
            @keydown.enter.exact.prevent="sendMessage"
            :disabled="isLoading"
            resize="none"
          />
          <el-button
            type="primary"
            :icon="Promotion"
            :loading="isLoading"
            @click="sendMessage"
            :disabled="!inputMessage.trim()"
            class="send-btn"
          />
        </div>
        <div class="input-tip">Enter 发送，Shift+Enter 换行</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { Promotion, Delete } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { chatWithAI, getConversations, getMessages, deleteConversation } from '../../api/ai'
import { marked } from 'marked'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const inputMessage = ref('')
const isLoading = ref(false)
const messages = reactive([])
const messageListRef = ref(null)
const conversations = ref([])
const currentConversationId = ref(null)

// Markdown 渲染
marked.setOptions({
  breaks: true,
  gfm: true
})

function renderMarkdown(text) {
  if (!text) return ''
  return marked(text)
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

async function loadConversations() {
  try {
    const res = await getConversations()
    conversations.value = res.data || []
  } catch (e) {
    console.error('加载对话列表失败:', e)
  }
}

async function selectConversation(id) {
  currentConversationId.value = id
  messages.length = 0
  try {
    const res = await getMessages(id)
    const data = res.data || []
    for (const msg of data) {
      messages.push({
        role: msg.role,
        content: msg.content,
        thinking: msg.thinking || '',
        thinkingExpanded: false,
        loading: false
      })
    }
    scrollToBottom()
  } catch (e) {
    console.error('加载消息失败:', e)
    ElMessage.error('加载消息失败')
  }
}

function newConversation() {
  currentConversationId.value = null
  messages.length = 0
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除这个对话吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteConversation(id)
    conversations.value = conversations.value.filter(c => c.id !== id)
    if (currentConversationId.value === id) {
      newConversation()
    }
    ElMessage.success('已删除')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function sendQuick(text) {
  inputMessage.value = text
  sendMessage()
}

async function sendMessage() {
  const text = inputMessage.value.trim()
  if (!text || isLoading.value) return

  // 添加用户消息
  messages.push({ role: 'user', content: text, thinking: '', thinkingExpanded: false, loading: false })
  inputMessage.value = ''
  scrollToBottom()

  // 添加 AI 消息占位
  const aiMessage = reactive({
    role: 'assistant',
    content: '',
    thinking: '',
    thinkingExpanded: false,
    loading: true
  })
  messages.push(aiMessage)
  isLoading.value = true
  scrollToBottom()

  try {
    const response = await chatWithAI(text, currentConversationId.value)

    if (!response.ok) {
      if (response.status === 403 || response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
        return
      }
      throw new Error(`请求失败: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      let currentEvent = ''

      for (const line of lines) {
        if (line.startsWith('event: ')) {
          currentEvent = line.substring(7).trim()
          continue
        }

        if (line.startsWith('data: ')) {
          const data = line.substring(6).trim()
          if (data === '[DONE]') continue

          try {
            if (currentEvent === 'conversation') {
              // 获取会话ID
              currentConversationId.value = parseInt(data)
            } else if (currentEvent === 'thinking') {
              aiMessage.thinking += data
              aiMessage.thinkingExpanded = true
            } else if (currentEvent === 'content') {
              aiMessage.content += data
              aiMessage.thinkingExpanded = false
            } else if (currentEvent === 'error') {
              ElMessage.error(data)
            }
          } catch (e) {
            // 解析失败跳过
          }

          scrollToBottom()
        }
      }
    }

    // 刷新对话列表
    loadConversations()
  } catch (e) {
    console.error('AI 对话异常:', e)
    aiMessage.content = '抱歉，AI 服务暂时不可用，请稍后再试。'
    ElMessage.error('AI 服务异常')
  } finally {
    aiMessage.loading = false
    isLoading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  loadConversations()
})
</script>

<style scoped lang="scss">
.ai-coach-page {
  height: calc(100vh - 120px);
  display: flex;
  gap: 0;
}

.sidebar {
  width: 240px;
  background: #2d2035;
  border-radius: 8px 0 0 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .sidebar-header {
    padding: 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .sidebar-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      font-size: 15px;
      font-weight: 600;
      color: #e8dff0;

      .sidebar-title-icon {
        font-size: 20px;
      }
    }

    .new-chat-btn {
      width: 100%;
      background: #ff6b9d;
      border-color: #ff6b9d;
      &:hover { background: #e85585; border-color: #e85585; }
    }
  }

  .conversation-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;

    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.2); border-radius: 2px; }
  }

  .conversation-item {
    padding: 12px;
    border-radius: 6px;
    cursor: pointer;
    margin-bottom: 4px;
    position: relative;
    transition: background 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.08);
      .delete-btn { display: block; }
    }

    &.active {
      background: rgba(255, 107, 157, 0.2);
    }

    .conv-title {
      color: #e8dff0;
      font-size: 13px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      padding-right: 20px;
    }

    .conv-time {
      color: #9b8aad;
      font-size: 11px;
      margin-top: 4px;
    }

    .delete-btn {
      display: none;
      position: absolute;
      right: 8px;
      top: 50%;
      transform: translateY(-50%);
      color: #9b8aad;
      font-size: 14px;
      &:hover { color: #ff6b9d; }
    }
  }

  .empty-tip {
    color: #9b8aad;
    font-size: 13px;
    text-align: center;
    padding: 20px;
  }
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 0 8px 8px 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;
}

.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b5a7a;

  .welcome-icon { font-size: 64px; margin-bottom: 16px; }
  h2 { color: #3d2b4a; margin: 0 0 8px; }
  p { margin: 0 0 24px; font-size: 14px; }

  .quick-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    justify-content: center;

    .quick-btn {
      padding: 10px 16px;
      background: #fef6f9;
      border: 1px solid #edd6e6;
      border-radius: 8px;
      cursor: pointer;
      font-size: 13px;
      transition: all 0.2s;
      &:hover { background: #f3e4ee; border-color: #ff6b9d; }
    }
  }
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  &.user {
    flex-direction: row-reverse;
    .message-body { align-items: flex-end; }
    .message-content {
      background: #ff6b9d;
      color: #fff;
      border-radius: 16px 16px 4px 16px;
    }
  }

  &.assistant {
    .message-content {
      background: #f8f4fb;
      color: #3d2b4a;
      border-radius: 16px 16px 16px 4px;
    }
  }

  .message-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #f3e4ee;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    flex-shrink: 0;
  }

  .message-body {
    display: flex;
    flex-direction: column;
    max-width: 75%;
  }

  .message-content {
    padding: 12px 16px;
    font-size: 14px;
    line-height: 1.6;
    word-break: break-word;

    :deep(p) { margin: 0 0 8px; &:last-child { margin: 0; } }
    :deep(ul), :deep(ol) { margin: 4px 0; padding-left: 20px; }
    :deep(table) { border-collapse: collapse; margin: 8px 0; width: 100%;
      th, td { border: 1px solid #edd6e6; padding: 6px 10px; text-align: left; font-size: 13px; }
      th { background: #f3e4ee; }
    }
    :deep(code) { background: rgba(0,0,0,0.06); padding: 2px 4px; border-radius: 3px; font-size: 13px; }
    :deep(pre) { background: #2d2035; color: #e8dff0; padding: 12px; border-radius: 6px; overflow-x: auto;
      code { background: none; color: inherit; }
    }
    :deep(strong) { color: #ff6b9d; }
  }
}

.thinking-block {
  margin-bottom: 8px;
  border: 1px solid #edd6e6;
  border-radius: 8px;
  overflow: hidden;

  .thinking-header {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    background: #fef6f9;
    cursor: pointer;
    font-size: 12px;
    color: #9b8aad;

    .thinking-icon { font-size: 14px; }
    .thinking-toggle {
      margin-left: auto;
      color: #ff6b9d;
      font-size: 11px;
    }
  }

  .thinking-content {
    padding: 8px 12px;
    font-size: 12px;
    color: #9b8aad;
    line-height: 1.6;
    white-space: pre-wrap;
    max-height: 200px;
    overflow-y: auto;
  }
}

.typing-cursor {
  display: inline-block;
  animation: blink 0.8s infinite;
  color: #ff6b9d;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.input-area {
  border-top: 1px solid #edd6e6;
  padding: 16px 20px;
  background: #fff;

  .input-wrapper {
    display: flex;
    gap: 8px;
    align-items: flex-end;

    :deep(.el-textarea__inner) {
      border-radius: 12px;
      padding: 10px 16px;
      resize: none;
      border-color: #edd6e6;
      &:focus { border-color: #ff6b9d; }
    }
  }

  .send-btn {
    height: 40px;
    width: 40px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .input-tip {
    font-size: 11px;
    color: #9b8aad;
    margin-top: 6px;
    text-align: center;
  }
}
</style>
