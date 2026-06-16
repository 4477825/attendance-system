<template>
  <div class="statistics-page">
    <el-card shadow="never" style="margin-bottom: 20px">
      <div class="toolbar">
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          value-format="YYYY-MM"
          @change="loadStats"
        />
        <el-button type="primary" @click="loadStats" style="margin-left: 12px">
          <el-icon><Search /></el-icon> 查询
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">总人数</p>
          <p class="stat-value">{{ stats.totalEmployees || 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">正常出勤</p>
          <p class="stat-value" style="color: #67C23A">{{ stats.normalCount || 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">迟到次数</p>
          <p class="stat-value" style="color: #E6A23C">{{ stats.lateCount || 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">缺勤次数</p>
          <p class="stat-value" style="color: #F56C6C">{{ stats.absentCount || 0 }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">早退次数</p>
          <p class="stat-value" style="color: #909399">{{ stats.earlyCount || 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">待审批请假</p>
          <p class="stat-value" style="color: #E6A23C">{{ stats.pendingLeaves || 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="12">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">已批准加班</p>
          <p class="stat-value" style="color: #409EFF">{{ stats.approvedOvertimes || 0 }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span>月度考勤明细</span>
      </template>
      <el-table :data="details" stripe v-loading="detailsLoading" style="width: 100%">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="checkInTime" label="签到" width="100" />
        <el-table-column prop="checkOutTime" label="签退" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="工时(h)" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllStats, getUserMonthlyDetail } from '@/api/statistics'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const userStore = useUserStore()
const selectedMonth = ref(dayjs().format('YYYY-MM'))
const stats = ref({})
const details = ref([])
const detailsLoading = ref(false)

const statusLabel = (status) => {
  const map = { NORMAL: '正常', LATE: '迟到', ABSENT: '缺勤', EARLY_ABSENTEE: '早退' }
  return map[status] || status
}

const statusTag = (status) => {
  const map = { NORMAL: 'success', LATE: 'warning', ABSENT: 'danger', EARLY_ABSENTEE: 'info' }
  return map[status] || 'info'
}

const loadStats = async () => {
  try {
    const res = await getAllStats({ month: selectedMonth.value })
    stats.value = res.data
    loadDetails()
  } catch (e) {
    ElMessage.error('获取统计数据失败')
  }
}

const loadDetails = async () => {
  detailsLoading.value = true
  try {
    const res = await getUserMonthlyDetail({
      month: selectedMonth.value,
      userId: userStore.user?.id,
    })
    details.value = res.data || []
  } catch (e) {
    // handled
  } finally {
    detailsLoading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}

.stat-card {
  text-align: center;
  padding: 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
</style>
