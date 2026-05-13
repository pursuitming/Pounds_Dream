<template>
  <div class="post-detail-page">
    <div class="card">
      <el-button @click="$router.back()" style="margin-bottom: 16px">返回</el-button>

      <div v-if="post" class="post-detail">
        <div class="post-header">
          <el-avatar :size="48" :src="post.avatar">{{ post.nickname?.charAt(0) }}</el-avatar>
          <div class="post-meta">
            <span class="nickname">{{ post.nickname }}</span>
            <span class="time">{{ post.createdAt }}</span>
          </div>
          <el-tag>{{ post.category }}</el-tag>
        </div>

        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-content">{{ post.content }}</div>

        <div v-if="post.images && post.images.length" class="post-images">
          <el-image
            v-for="(img, idx) in post.images"
            :key="idx"
            :src="img"
            :preview-src-list="post.images"
            :initial-index="idx"
            fit="cover"
            class="post-img"
          />
        </div>

        <div class="post-actions">
          <el-button :type="post.liked ? 'primary' : 'default'" @click="handleLike">
            👍 {{ post.likeCount }}
          </el-button>
          <span class="views">👁️ {{ post.viewCount }} 浏览</span>
        </div>

        <div class="comment-section">
          <h3>评论 ({{ post.commentCount }})</h3>

          <div class="comment-input">
            <el-input v-model="commentContent" type="textarea" :rows="2" placeholder="写下你的评论..." />
            <el-button type="primary" :loading="commentLoading" @click="handleComment" style="margin-top: 8px">
              发表评论
            </el-button>
          </div>

          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="36" :src="comment.avatar">{{ comment.nickname?.charAt(0) }}</el-avatar>
            <div class="comment-body">
              <div class="comment-header">
                <span class="nickname">{{ comment.nickname }}</span>
                <span v-if="comment.replyToNickname" class="reply-to">回复 {{ comment.replyToNickname }}</span>
                <span class="time">{{ comment.createdAt }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
              <div class="comment-actions">
                <el-button link size="small" @click="replyTo(comment)">回复</el-button>
                <el-button link size="small" @click="handleLikeComment(comment)">
                  👍 {{ comment.likeCount }}
                </el-button>
              </div>
            </div>
          </div>

          <el-pagination
            v-if="commentTotal > 20"
            v-model:current-page="commentPage"
            :page-size="20"
            :total="commentTotal"
            layout="prev, pager, next"
            @current-change="fetchComments"
            style="margin-top: 16px"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPostDetail, addComment, getComments, toggleLike } from '../../api/community'
import { ElMessage } from 'element-plus'

const route = useRoute()
const post = ref(null)
const comments = ref([])
const commentContent = ref('')
const commentLoading = ref(false)
const commentPage = ref(1)
const commentTotal = ref(0)
const replyToComment = ref(null)

async function fetchPost() {
  try {
    const res = await getPostDetail(route.params.id)
    post.value = res.data
  } catch (e) {
    // handled
  }
}

async function fetchComments() {
  try {
    const res = await getComments(route.params.id, {
      pageNum: commentPage.value,
      pageSize: 20
    })
    comments.value = res.data.list
    commentTotal.value = res.data.total
  } catch (e) {
    // handled
  }
}

async function handleLike() {
  try {
    await toggleLike(post.value.id, 1)
    post.value.liked = !post.value.liked
    post.value.likeCount += post.value.liked ? 1 : -1
  } catch (e) {
    // handled
  }
}

async function handleLikeComment(comment) {
  try {
    await toggleLike(comment.id, 2)
    comment.liked = !comment.liked
    comment.likeCount += comment.liked ? 1 : -1
  } catch (e) {
    // handled
  }
}

function replyTo(comment) {
  replyToComment.value = comment
  commentContent.value = ''
}

async function handleComment() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commentLoading.value = true
  try {
    const data = {
      postId: Number(route.params.id),
      content: commentContent.value
    }
    if (replyToComment.value) {
      data.parentId = replyToComment.value.id
      data.replyToUserId = replyToComment.value.userId
    }
    await addComment(data)
    ElMessage.success('评论成功')
    commentContent.value = ''
    replyToComment.value = null
    await fetchComments()
    await fetchPost()
  } catch (e) {
    // handled
  } finally {
    commentLoading.value = false
  }
}

onMounted(async () => {
  await fetchPost()
  await fetchComments()
})
</script>

<style scoped lang="scss">
.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.post-detail {
  .post-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;

    .post-meta {
      flex: 1;

      .nickname {
        display: block;
        font-size: 16px;
        font-weight: 600;
        color: #3d2b4a;
      }

      .time {
        font-size: 13px;
        color: #9b8aad;
      }
    }
  }

  .post-title {
    font-size: 24px;
    margin: 0 0 16px;
    color: #3d2b4a;
  }

  .post-content {
    font-size: 15px;
    line-height: 1.8;
    color: #6b5a7a;
    margin-bottom: 20px;
    white-space: pre-wrap;
  }

  .post-images {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 20px;

    .post-img {
      width: 150px;
      height: 150px;
      border-radius: 8px;
      cursor: pointer;
    }
  }

  .post-actions {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 0;
    border-top: 1px solid #edd6e6;
    border-bottom: 1px solid #edd6e6;
    margin-bottom: 20px;

    .views {
      color: #9b8aad;
      font-size: 14px;
    }
  }
}

.comment-section {
  h3 {
    font-size: 18px;
    margin: 0 0 16px;
    color: #3d2b4a;
  }

  .comment-input {
    margin-bottom: 20px;
  }

  .comment-item {
    display: flex;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #edd6e6;

    &:last-child {
      border-bottom: none;
    }

    .comment-body {
      flex: 1;

      .comment-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;

        .nickname {
          font-weight: 600;
          color: #3d2b4a;
        }

        .reply-to {
          color: #9b8aad;
          font-size: 13px;
        }

        .time {
          color: #9b8aad;
          font-size: 12px;
          margin-left: auto;
        }
      }

      .comment-content {
        margin: 0 0 8px;
        font-size: 14px;
        color: #6b5a7a;
        line-height: 1.5;
      }

      .comment-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
}
</style>
