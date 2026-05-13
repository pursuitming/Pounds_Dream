<template>
  <div class="publish-page">
    <div class="card">
      <h3 class="card-title">发布帖子</h3>

      <el-form :model="form" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 200px">
            <el-option label="经验分享" value="经验分享" />
            <el-option label="打卡" value="打卡" />
            <el-option label="求助" value="求助" />
            <el-option label="食谱" value="食谱" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="分享你的经验..." />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-upload="beforeUpload"
            :limit="9"
            accept="image/jpeg,image/png,image/gif,image/webp"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleSubmit">发布</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createPost, uploadImage } from '../../api/community'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const fileList = ref([])

const form = reactive({
  title: '',
  content: '',
  category: '经验分享',
  images: []
})

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isImage) {
    ElMessage.error('仅支持 jpg/png/gif/webp 格式')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

async function handleFileChange(file) {
  if (!beforeUpload(file.raw)) return
  try {
    const res = await uploadImage(file.raw)
    form.images.push(res.data)
    file.url = res.data
  } catch (e) {
    ElMessage.error('图片上传失败')
  }
}

function handleFileRemove(file) {
  const idx = form.images.indexOf(file.url)
  if (idx !== -1) {
    form.images.splice(idx, 1)
  }
}

async function handleSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  loading.value = true
  try {
    await createPost(form)
    ElMessage.success('发布成功')
    router.push('/community')
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  max-width: 800px;

  .card-title {
    font-size: 20px;
    margin: 0 0 20px;
    color: #3d2b4a;
  }
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>
