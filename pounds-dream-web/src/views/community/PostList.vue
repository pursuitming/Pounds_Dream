<template>
  <div class="community-page">
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">社区</h3>
        <el-button type="primary" @click="$router.push('/community/publish')">发布帖子</el-button>
      </div>

      <el-radio-group v-model="category" @change="fetchPosts" style="margin-bottom: 16px">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="经验分享">经验分享</el-radio-button>
        <el-radio-button value="打卡">打卡</el-radio-button>
        <el-radio-button value="求助">求助</el-radio-button>
        <el-radio-button value="食谱">食谱</el-radio-button>
      </el-radio-group>

      <div v-if="posts.length === 0" class="empty">暂无帖子</div>

      <div v-for="post in posts" :key="post.id" class="post-card" @click="$router.push(`/community/post/${post.id}`)">
        <div class="post-header">
          <el-avatar :size="36" :src="post.avatar">{{ post.nickname?.charAt(0) }}</el-avatar>
          <div class="post-meta">
            <span class="nickname">{{ post.nickname }}</span>
            <span class="time">{{ post.createdAt }}</span>
          </div>
          <el-tag size="small" type="info">{{ post.category }}</el-tag>
        </div>
        <h4 class="post-title">{{ post.title }}</h4>
        <p class="post-content">{{ post.content.substring(0, 150) }}{{ post.content.length > 150 ? '...' : '' }}</p>
        <div v-if="post.images && post.images.length" class="post-images">
          <img v-for="(img, idx) in post.images.slice(0, 3)" :key="idx" :src="img" class="post-img" />
          <span v-if="post.images.length > 3" class="more-count">+{{ post.images.length - 3 }}</span>
        </div>
        <div class="post-footer">
          <span>👁️ {{ post.viewCount }}</span>
          <span>👍 {{ post.likeCount }}</span>
          <span>💬 {{ post.commentCount }}</span>
        </div>
      </div>

      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchPosts"
        style="margin-top: 16px; justify-content: center"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPosts } from '../../api/community'

const category = ref('')
const posts = ref([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)

async function fetchPosts() {
  try {
    const res = await getPosts({
      category: category.value || undefined,
      pageNum: pageNum.value,
      pageSize
    })
    posts.value = res.data.list
    total.value = res.data.total
  } catch (e) {
    // handled
  }
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped lang="scss">
.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .card-title {
      margin: 0;
      font-size: 20px;
    }
  }
}

.empty {
  text-align: center;
  color: #9b8aad;
  padding: 60px;
}

.post-card {
  padding: 16px;
  border-bottom: 1px solid #edd6e6;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #fdf2f6;
  }

  &:last-child {
    border-bottom: none;
  }

  .post-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;

    .post-meta {
      flex: 1;

      .nickname {
        display: block;
        font-size: 14px;
        font-weight: 600;
        color: #3d2b4a;
      }

      .time {
        font-size: 12px;
        color: #9b8aad;
      }
    }
  }

  .post-title {
    font-size: 16px;
    margin: 0 0 8px;
    color: #3d2b4a;
  }

  .post-content {
    font-size: 14px;
    color: #6b5a7a;
    margin: 0 0 10px;
    line-height: 1.5;
  }

  .post-images {
    display: flex;
    gap: 8px;
    margin-bottom: 10px;

    .post-img {
      width: 80px;
      height: 80px;
      object-fit: cover;
      border-radius: 6px;
    }

    .more-count {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 80px;
      height: 80px;
      background: #f3e4ee;
      border-radius: 6px;
      color: #6b5a7a;
      font-size: 14px;
    }
  }

  .post-footer {
    display: flex;
    gap: 20px;
    font-size: 13px;
    color: #9b8aad;
  }
}
</style>
