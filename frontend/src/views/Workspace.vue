<template>
  <div class="page">
    <!-- ===== 顶栏 ===== -->
    <header class="topbar">
      <div class="brand">
        <span class="logo">💰</span>
        <span class="title">校园经费管理系统</span>
      </div>
      <div class="user-area">
        <el-tag :type="roleTagType" effect="dark" size="small">{{ roleText }}</el-tag>
        <span class="username">{{ user?.displayName }}</span>
        <el-button link type="primary" @click="handleLogout">登出</el-button>
      </div>
    </header>

    <main class="content">
      <!-- ===== 经费看板 ===== -->
      <section class="cards">
        <el-card shadow="hover" class="card">
          <div class="card-label">总预算</div>
          <div class="card-value primary">{{ money(project.totalBudget) }}</div>
          <div class="card-unit">元</div>
        </el-card>
        <el-card shadow="hover" class="card">
          <div class="card-label">可用</div>
          <div class="card-value success">{{ money(project.available) }}</div>
          <div class="card-unit">元</div>
        </el-card>
        <el-card shadow="hover" class="card">
          <div class="card-label">冻结</div>
          <div class="card-value warning">{{ money(project.frozen) }}</div>
          <div class="card-unit">元</div>
        </el-card>
        <el-card shadow="hover" class="card">
          <div class="card-label">已使用</div>
          <div class="card-value info">{{ money(project.used) }}</div>
          <div class="card-unit">元</div>
        </el-card>
      </section>

      <!-- 守恒校验：答辩时可直接指这一行 -->
      <el-alert
        v-if="project.id"
        :type="balanced ? 'success' : 'error'"
        :closable="false"
        show-icon
        class="balance-tip"
      >
        可用 {{ money(project.available) }} + 冻结 {{ money(project.frozen) }} + 已用
        {{ money(project.used) }} =
        <b>{{ money(sumOfThree) }}</b>
        ，总预算 {{ money(project.totalBudget) }}
        <b>{{ balanced ? '✔ 守恒成立' : '✘ 金额异常' }}</b>
      </el-alert>

      <!-- ===== 申请列表 ===== -->
      <el-card class="table-card">
        <template #header>
          <div class="table-header">
            <span>申请列表</span>
            <div>
              <el-button type="primary" size="small" @click="openCreate">新建申请</el-button>
              <el-button size="small" @click="loadAll">刷新</el-button>
            </div>
          </div>
        </template>

        <el-table :data="applications" v-loading="loading" stripe style="width: 100%">
          <el-table-column prop="id" label="单号" width="70" />
          <el-table-column prop="purpose" label="用途" min-width="140" show-overflow-tooltip />
          <el-table-column label="申请额" width="110" align="right">
            <template #default="{ row }">{{ money(row.requestedAmount) }}</template>
          </el-table-column>
          <el-table-column label="批准额" width="110" align="right">
            <template #default="{ row }">{{ moneyOrDash(row.approvedAmount) }}</template>
          </el-table-column>
          <el-table-column label="结算额" width="110" align="right">
            <template #default="{ row }">{{ moneyOrDash(row.settledAmount) }}</template>
          </el-table-column>

          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" min-width="230">
            <template #default="{ row }">
              <el-button
                v-if="canSubmit(row)"
                size="small"
                type="primary"
                @click="doSubmit(row)"
              >提交</el-button>

              <el-button
                v-if="canApprove(row)"
                size="small"
                type="success"
                @click="doApprove(row)"
              >批准</el-button>

              <el-button
                v-if="canApprove(row)"
                size="small"
                type="danger"
                plain
                @click="doReject(row)"
              >拒绝</el-button>

              <el-button
                v-if="canSettle(row)"
                size="small"
                type="warning"
                @click="doSettle(row)"
              >结算</el-button>

              <el-button
                v-if="canCancel(row)"
                size="small"
                @click="doCancel(row)"
              >取消</el-button>

              <span v-if="!hasAnyAction(row)" class="no-action">—</span>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!loading && applications.length === 0" class="empty">
          暂无申请单
        </div>
      </el-card>

      <!-- ===== 新建申请对话框 ===== -->
      <el-dialog v-model="createVisible" title="新建申请" width="460px">
        <el-form :model="createForm" label-width="80px">
          <el-form-item label="用途">
            <el-input
              v-model="createForm.purpose"
              placeholder="例如：采购传感器"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="申请金额">
            <el-input v-model="createForm.requestedAmount" placeholder="例如：120.00">
              <template #append>元</template>
            </el-input>
          </el-form-item>
        </el-form>
        <p class="dialog-tip">
          新建的是<strong>草稿</strong>，此时不占用经费；点"提交"后才从可用转入冻结。
        </p>
        <template #footer>
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
        </template>
      </el-dialog>

      <!-- 提示：前端按钮只是"显示与否"，真正的权限在后端 -->
      <p class="footnote">
        * 页面上的按钮是否显示，只影响操作体验；真正的权限校验在后端 Service 中。
        即使手动调用接口，越权操作也会被拒绝并返回 403。
      </p>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { logout } from '../api/auth'
import { getProject } from '../api/project'
import {
  listApplications,
  createApplication,
  submitApplication,
  approveApplication,
  rejectApplication,
  cancelApplication,
  settleApplication
} from '../api/application'

const router = useRouter()

const user = ref(JSON.parse(localStorage.getItem('loginUser') || 'null'))
const project = ref({})
const applications = ref([])
const loading = ref(false)

/** 新建申请对话框 */
const createVisible = ref(false)
const creating = ref(false)
const createForm = ref({ purpose: '', requestedAmount: '' })

function openCreate() {
  createForm.value = { purpose: '', requestedAmount: '' }
  createVisible.value = true
}

async function handleCreate() {
  const purpose = createForm.value.purpose?.trim()
  const amount = createForm.value.requestedAmount

  if (!purpose) {
    ElMessage.warning('请填写用途')
    return
  }
  if (!/^\d+(\.\d{1,2})?$/.test(amount) || Number(amount) <= 0) {
    ElMessage.warning('请填写大于 0 的金额，最多两位小数')
    return
  }

  creating.value = true
  try {
    const newId = await createApplication({
      projectId: PROJECT_ID,
      purpose,
      requestedAmount: Number(amount)
    })
    ElMessage.success(`已创建申请单 #${newId}（草稿）`)
    createVisible.value = false
    await loadAll()
  } catch (e) {
    // 错误提示由拦截器统一处理
  } finally {
    creating.value = false
  }
}

/** 本演示固定操作 1 号项目 */
const PROJECT_ID = 1

// ===== 金额与状态显示 =====

/** 金额格式化：null 显示破折号，0 显示 0.00 —— 两者业务含义不同 */
const money = (v) => (v == null ? '—' : Number(v).toFixed(2))
const moneyOrDash = (v) => (v == null ? '—' : Number(v).toFixed(2))

const STATUS_TEXT = {
  DRAFT: '草稿',
  PENDING_L1: '待审批',
  APPROVED: '已批准',
  REJECTED: '已拒绝',
  CANCELED: '已取消',
  SETTLED: '已结算'
}
const STATUS_TYPE = {
  DRAFT: 'info',
  PENDING_L1: 'warning',
  APPROVED: 'primary',
  REJECTED: 'danger',
  CANCELED: 'info',
  SETTLED: 'success'
}
const statusText = (s) => STATUS_TEXT[s] || s
const statusType = (s) => STATUS_TYPE[s] || 'info'

const ROLE_TEXT = {
  MEMBER: '成员',
  TEACHER: '老师',
  FINANCE: '财务',
  ADMIN: '管理员'
}
const roleText = computed(() => ROLE_TEXT[user.value?.role] || user.value?.role)
const roleTagType = computed(() => {
  const map = { MEMBER: 'primary', TEACHER: 'success', FINANCE: 'warning', ADMIN: 'danger' }
  return map[user.value?.role] || 'info'
})

// ===== 守恒校验 =====
const sumOfThree = computed(
  () =>
    Number(project.value.available || 0) +
    Number(project.value.frozen || 0) +
    Number(project.value.used || 0)
)
const balanced = computed(
  () =>
    project.value.totalBudget != null &&
    Math.abs(sumOfThree.value - Number(project.value.totalBudget)) < 0.005
)

// ===== 按钮可见性（前端体验层，后端才是真权限）=====

const isSelf = (row) => row.applicantId === user.value?.id
const role = computed(() => user.value?.role)

const canSubmit = (row) => row.status === 'DRAFT' && isSelf(row)
const canCancel = (row) =>
  isSelf(row) && (row.status === 'PENDING_L1' || row.status === 'APPROVED')
const canApprove = (row) =>
  role.value === 'TEACHER' && row.status === 'PENDING_L1' && !isSelf(row)
const canSettle = (row) => role.value === 'FINANCE' && row.status === 'APPROVED'

const hasAnyAction = (row) =>
  canSubmit(row) || canApprove(row) || canSettle(row) || canCancel(row)

// ===== 数据加载 =====

async function loadProject() {
  project.value = (await getProject(PROJECT_ID)) || {}
}

async function loadApplications() {
  applications.value = (await listApplications(PROJECT_ID)) || []
}

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([loadProject(), loadApplications()])
  } finally {
    loading.value = false
  }
}

// ===== 五个操作 =====

async function doSubmit(row) {
  try {
    await ElMessageBox.confirm(
      `确认提交申请单 #${row.id}（${row.purpose}，${money(row.requestedAmount)} 元）？\n` +
        `提交后 ${money(row.requestedAmount)} 元将从"可用"转入"冻结"。`,
      '提交申请',
      { type: 'warning' }
    )
  } catch {
    return
  }
  await submitApplication(row.id)
  ElMessage.success('提交成功')
  await loadAll()
}

async function doApprove(row) {
  let input
  try {
    input = await ElMessageBox.prompt(
      `申请金额 ${money(row.requestedAmount)} 元。请输入批准金额（可减额，但不能超过申请额）：`,
      '批准申请',
      {
        inputValue: String(row.requestedAmount),
        inputPattern: /^\d+(\.\d{1,2})?$/,
        inputErrorMessage: '请输入数字，最多两位小数'
      }
    )
  } catch {
    return
  }
  await approveApplication(row.id, input.value)
  ElMessage.success('批准成功')
  await loadAll()
}

async function doReject(row) {
  let input
  try {
    input = await ElMessageBox.prompt('请填写拒绝原因（必填）：', '拒绝申请', {
      inputValidator: (v) => (v && v.trim() ? true : '原因不能为空')
    })
  } catch {
    return
  }
  await rejectApplication(row.id, input.value)
  ElMessage.success('已拒绝')
  await loadAll()
}

async function doCancel(row) {
  let input
  try {
    input = await ElMessageBox.prompt('请填写取消原因（必填）：', '取消申请', {
      inputValidator: (v) => (v && v.trim() ? true : '原因不能为空')
    })
  } catch {
    return
  }
  await cancelApplication(row.id, input.value)
  ElMessage.success('已取消')
  await loadAll()
}

async function doSettle(row) {
  let input
  try {
    input = await ElMessageBox.prompt(
      `批准金额 ${money(row.approvedAmount)} 元。请输入实际花费（可以是 0）：`,
      '登记结算',
      {
        inputValue: String(row.approvedAmount),
        inputPattern: /^\d+(\.\d{1,2})?$/,
        inputErrorMessage: '请输入数字，最多两位小数'
      }
    )
  } catch {
    return
  }
  await settleApplication(row.id, input.value)
  ElMessage.success('结算成功')
  await loadAll()
}

// ===== 登出 =====

async function handleLogout() {
  try {
    await logout()
  } catch {
    // 后端失败也要清本地
  }
  localStorage.removeItem('loginUser')
  ElMessage.success('已登出')
  router.push('/login')
}

onMounted(loadAll)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 顶栏 */
.topbar {
  height: 56px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
}
.logo {
  font-size: 20px;
}
.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  font-size: 14px;
  color: #606266;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 24px 40px;
}

/* 经费卡片 */
.cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 900px) {
  .cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
.card {
  text-align: center;
}
.card-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.card-value {
  font-size: 26px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.card-value.primary {
  color: #409eff;
}
.card-value.success {
  color: #67c23a;
}
.card-value.warning {
  color: #e6a23c;
}
.card-value.info {
  color: #909399;
}
.card-unit {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

.balance-tip {
  margin-top: 16px;
}

.table-card {
  margin-top: 16px;
}
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.no-action {
  color: #c0c4cc;
}
.empty {
  padding: 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
.dialog-tip {
  margin: 0;
  padding: 8px 12px;
  background: #f4f4f5;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.7;
  color: #606266;
}
.footnote {
  margin-top: 16px;
  font-size: 12px;
  color: #909399;
  line-height: 1.7;
}
</style>
