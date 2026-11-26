<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>校园经费管理系统</h2>
      <p class="subtitle">成员申请 · 老师审批 · 财务结算</p>

      <el-form :model="form" label-width="64px" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="tips">
        演示账号：xiaolin（成员） / wang（老师） / licaiwu（财务） / admin（管理员）
        <br />
        密码均为 123456
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: 'xiaolin', password: '123456' })

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const user = await login(form.value.username, form.value.password)
    localStorage.setItem('loginUser', JSON.stringify(user))
    ElMessage.success(`欢迎，${user.displayName}`)
    router.push('/workspace')
  } catch (e) {
    // 具体错误提示已由 request.js 的拦截器统一弹出
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8eefc 0%, #f5f7fa 100%);
}
.login-card {
  width: 420px;
  padding: 8px 12px;
}
h2 {
  margin: 8px 0 4px;
  text-align: center;
  color: #303133;
  font-size: 20px;
}
.subtitle {
  margin: 0 0 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
.tips {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px dashed #dcdfe6;
  font-size: 12px;
  line-height: 1.8;
  color: #909399;
  text-align: center;
}
</style>
