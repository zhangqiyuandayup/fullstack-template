<template>
  <div class="workspace">
    <el-card>
      <h2>登录成功 🎉</h2>
      <p>当前登录用户：</p>
      <pre>{{ user }}</pre>
      <el-button type="danger" @click="handleLogout">登出</el-button>
    </el-card>

    <el-alert
      class="next"
      title="工作台（经费看板 + 申请列表）明天继续"
      type="info"
      :closable="false"
      show-icon
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout } from '../api/auth'

const router = useRouter()
const user = ref(null)

onMounted(() => {
  const raw = localStorage.getItem('loginUser')
  user.value = raw ? JSON.parse(raw) : null
})

async function handleLogout() {
  try {
    await logout()
  } catch (e) {
    // 即使后端登出失败，本地也要清掉，避免卡在页面里
  }
  localStorage.removeItem('loginUser')
  ElMessage.success('已登出')
  router.push('/login')
}
</script>

<style scoped>
.workspace {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 16px;
}
h2 {
  margin-top: 0;
  color: #303133;
}
pre {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  color: #303133;
  overflow-x: auto;
}
.next {
  margin-top: 16px;
}
</style>
