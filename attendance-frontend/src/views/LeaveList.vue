<template>
  <div class="leave-list-page">
    <el-card shadow="never">
      <template #header>
        <span>请假列表</span>
      </template>

      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadList" style="width: 150px">
          <el-option label="待审批" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
      </div>

      <el-table :data="leaves" stripe v-loading="loading" style="width: 100%; margin-top: 16px">
        <el-table-column prop="leaveType" label="类型" width="100">
          <template #default="{ row }">{{ row.leaveType }}</template>
        </el-table-column>
        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="180">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="事由" min-width="200" show-overflow-tooltip />
        <el-table-column label="附件" width="80" align="center">
          <template #default="{ row }">
            <a v-if="row.attachmentUrl" :href="getAttachmentUrl(row.attachmentUrl)" target="_blank" rel="noopener" title="点击查看/下载附件">
              <el-icon :size="18" style="color: #409EFF"><Document /></el-icon>
            </a>
            <span v-else style="color: #C0C4CC">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveRemark" label="审批意见" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" size="small" @click="handleApprove(row)">
              审批
            </el-button>
            <el-button size="small" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" title="审批请假申请" width="450px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveForm.status">
            <el-radio value="APPROVED">通过</el-radio>
            <el-radio value="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="请输入审批意见" />
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
import { getLeaveList, approveLeave } from '@/api/leave'
import { getToken, isAdmin } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

// Backend API base URL for direct file access
const BACKEND_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'
import dayjs from 'dayjs'

const leaves = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')
const dialogVisible = ref(false)
const approving = ref(false)
const currentLeaveId = ref(null)

const approveForm = ref({
  status: 'APPROVED',
  remark: '',
})

const statusLabel = (status) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status
}

const statusTag = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')

const getAttachmentUrl = (url) => {
  if (!url) return ''
  // If already a full URL, return as-is
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  // Otherwise prepend backend base URL
  return BACKEND_BASE_URL + url
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getLeaveList({
      status: statusFilter.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    leaves.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

const handleApprove = (row) => {
  if (!isAdmin()) {
    ElMessage.warning('只有管理员可以审批')
    return
  }
  currentLeaveId.value = row.id
  approveForm.value = { status: 'APPROVED', remark: '' }
  dialogVisible.value = true
}

const doApprove = async () => {
  approving.value = true
  try {
    await approveLeave(currentLeaveId.value, {
      approveRemark: approveForm.value.remark,
      status: approveForm.value.status,
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

const handleDelete = (row) => {
  ElMessage.info('删除功能开发中')
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
}
</style>
