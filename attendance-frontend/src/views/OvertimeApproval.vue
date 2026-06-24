<template>
  <div class="overtime-approval-page">
    <el-card shadow="never">
      <template #header>
        <span>加班审批</span>
      </template>

      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadList" style="width: 150px">
          <el-option label="待审批" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
      </div>

      <el-table :data="overtimes" stripe v-loading="loading" style="width: 100%; margin-top: 16px">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="加班日期" width="120">
          <template #default="{ row }">{{ formatDate(row.overtimeDate) }}</template>
        </el-table-column>
        <el-table-column label="时间段" width="280">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="durationHours" label="时长(h)" width="90" align="center">
          <template #default="{ row }">{{ row.durationHours }}h</template>
        </el-table-column>
        <el-table-column prop="reason" label="事由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveRemark" label="审批意见" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" link size="small" @click="openApproveDialog(row, 'APPROVED')">通过</el-button>
              <el-button type="danger" link size="small" @click="openApproveDialog(row, 'REJECTED')">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top: 16px; justify-content: center"
        background
        layout="total, prev, pager, next"
:total="total"
v-model:current-page="pageNum"
v-model:page-size="pageSize"
@current-change="loadList"
      />
    </el-card>

    <!-- Approval Dialog -->
    <el-dialog v-model="dialogVisible" :title="approveAction === 'APPROVED' ? '审批加班申请' : '拒绝加班申请'" width="420px">
      <div style="margin-bottom: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px;">
        <div style="color: #606266; font-size: 14px;">
          <span>用户ID: {{ approveForm?.userId || '-' }}</span> &nbsp;&nbsp;
          <span>时长: {{ approveForm?.durationHours || 0 }}h</span>
        </div>
        <div style="margin-top: 8px; color: #909399; font-size: 13px;">
          事由: {{ approveForm?.reason || '-' }}
        </div>
      </div>
      <el-form :model="remarkForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="remarkForm.remark" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="approving" @click="doApprove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOvertimeList, approveOvertime } from '@/api/overtime'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()

const overtimes = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')
const dialogVisible = ref(false)
const approving = ref(false)
const approveAction = ref('APPROVED')
const approveForm = ref(null)
const remarkForm = ref({ remark: '' })
const currentOvertimeId = ref(null)

const statusLabel = (status) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status
}

const statusTag = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD') : '-'
const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'

const loadList = async () => {
  loading.value = true
  try {
    const res = await getOvertimeList({
      status: statusFilter.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    overtimes.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row) => {
  router.push({ name: 'OvertimeDetail', params: { id: row.id } })
}

const openApproveDialog = (row, action) => {
  approveForm.value = row
  approveAction.value = action
  remarkForm.value = { remark: '' }
  currentOvertimeId.value = row.id
  dialogVisible.value = true
}

const doApprove = async () => {
  approving.value = true
  try {
    await approveOvertime(currentOvertimeId.value, {
      status: approveAction.value,
      approveRemark: remarkForm.value.remark,
    })
    ElMessage.success('审批成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // handled
  } finally {
    approving.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.overtime-approval-page {
  padding-top: 10px;
}
.filter-bar {
  display: flex;
  gap: 12px;
}
</style>
