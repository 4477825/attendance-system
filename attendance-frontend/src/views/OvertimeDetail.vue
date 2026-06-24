<template>
  <div class="overtime-detail-page">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px">
          <el-button @click="$router.back()">返回</el-button>
          <span>加班详情</span>
        </div>
      </template>
      <el-descriptions :column="2" border v-if="record">
        <el-descriptions-item label="加班日期">{{ formatDate(record.overtimeDate) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(record.status)">{{ statusLabel(record.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(record.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(record.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="时长">{{ record.durationHours }}小时</el-descriptions-item>
        <el-descriptions-item label="事由" :span="2">{{ record.reason }}</el-descriptions-item>
        <el-descriptions-item label="审批人ID">{{ record.approverId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ record.approveTime ? formatTime(record.approveTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ record.approveRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(record.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="加载中..." />
      <!-- Admin approve actions -->
      <el-card shadow="never" v-if="record && isAdminVal && record.status === 'PENDING'" style="margin-top: 16px">
        <template #header><span>审批操作</span></template>
        <el-form :model="approveForm" label-width="80px" style="max-width: 500px">
          <el-form-item label="审批结果">
            <el-radio-group v-model="approveForm.status">
              <el-radio value="APPROVED">通过</el-radio>
              <el-radio value="REJECTED">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审批意见">
            <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="请输入审批意见" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="approving" @click="doApprove">提交审批</el-button>
            <el-button @click="$router.back()">返回</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOvertimeById, approveOvertime } from '@/api/overtime'
import { ElMessage } from 'element-plus'
import { isAdmin } from '@/utils/auth'
import dayjs from 'dayjs'

const route = useRoute()
const record = ref(null)

const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD') : '-'
const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'

const statusLabel = (status) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status
}

const statusTag = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const loadDetail = async () => {
  try {
    const res = await getOvertimeById(route.params.id)
    record.value = res.data
  } catch (e) {
    // handled
  }
}

const isAdminVal = isAdmin()

const approveForm = ref({
  status: 'APPROVED',
  remark: '',
})
const approving = ref(false)

const doApprove = async () => {
  approving.value = true
  try {
    await approveOvertime(route.params.id, {
      status: approveForm.value.status,
      approveRemark: approveForm.value.remark,
    })
    ElMessage.success('审批成功')
    await loadDetail()
  } catch (e) {
    // handled
  } finally {
    approving.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.overtime-detail-page {
  padding-top: 10px;
}
</style>
