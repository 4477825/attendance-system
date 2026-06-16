<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409EFF">
            <el-icon :size="28"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">今日状态</p>
            <p class="stat-value">
              <el-tag v-if="todayRecord" :type="todayRecord.status === 'NORMAL' ? 'success' : 'warning'" size="small">
                {{ statusLabel(todayRecord.status) }}
              </el-tag>
              <el-tag v-else type="info" size="small">尚未打卡</el-tag>
            </p>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67C23A">
            <el-icon :size="28"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">本月正常</p>
            <p class="stat-value">{{ summary.normalDays || 0 }} 天</p>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #E6A23C">
            <el-icon :size="28"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">本月迟到</p>
            <p class="stat-value">{{ summary.lateDays || 0 }} 次</p>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #F56C6C">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">待审批</p>
            <p class="stat-value">{{ pendingCount }} 条</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="handleCheckIn" :loading="checkLoading">
              <el-icon><Clock /></el-icon>
              签到打卡
            </el-button>
            <el-button type="success" size="large" @click="handleCheckOut" :loading="checkOutLoading">
              <el-icon><SwitchButton /></el-icon>
              签退打卡
            </el-button>
            <el-button type="warning" size="large" @click="$router.push('/leave/apply')">
              <el-icon><Document /></el-icon>
              请假申请
            </el-button>
            <el-button type="info" size="large" @click="$router.push('/overtime')">
              <el-icon><Timer /></el-icon>
              加班登记
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span>本月工时</span>
          </template>
          <div class="hours-display">
            <p class="hours-number">{{ summary.totalHours || 0 }} <span class="hours-unit">小时</span></p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>最近考勤记录</span>
          </template>
          <el-table :data="recentRecords" stripe style="width: 100%">
            <el-table-column prop="date" label="日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.date) }}
              </template>
            </el-table-column>
            <el-table-column prop="checkInTime" label="签到时间" width="160">
              <template #default="{ row }">
                {{ row.checkInTime ? formatTime(row.checkInTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="checkOutTime" label="签退时间" width="160">
              <template #default="{ row }">
                {{ row.checkOutTime ? formatTime(row.checkOutTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">
                  {{ statusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="workHours" label="工时" width="100">
              <template #default="{ row }">
                {{ row.workHours ? row.workHours + 'h' : '-' }}
              </template>
            </el-table-column>
          </el-table>
          <div v-loading="recordsLoading" style="min-height: 200px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { checkIn, checkOut, getRecords, getTodayRecord, getMyMonthSummary } from '@/api/attendance'
import { getLeaveList } from '@/api/leave'
import { ElMessage } from 'element-plus'
import { Clock, CircleCheck, Warning, Document, Timer, SwitchButton } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const userStore = useUserStore()
const todayRecord = ref(null)
const summary = ref({})
const recentRecords = ref([])
const pendingCount = ref(0)
const checkLoading = ref(false)
const checkOutLoading = ref(false)
const recordsLoading = ref(true)

const statusLabel = (status) => {
  const map = { NORMAL: '正常', LATE: '迟到', ABSENT: '缺勤', EARLY_ABSENTEE: '早退' }
  return map[status] || status
}

const statusTagType = (status) => {
  const map = { NORMAL: 'success', LATE: 'warning', ABSENT: 'danger', EARLY_ABSENTEE: 'info' }
  return map[status] || 'info'
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')
const formatTime = (time) => dayjs(time).format('HH:mm:ss')

const loadDashboard = async () => {
  recordsLoading.value = true
  try {
    const [todayRes, summaryRes, recordsRes] = await Promise.all([
      getTodayRecord(),
      getMyMonthSummary(dayjs().format('YYYY-MM')),
      getRecords({ pageNum: 1, pageSize: 5 }),
    ])
    todayRecord.value = todayRes.data
    summary.value = summaryRes.data
    recentRecords.value = recordsRes.data.records || []
  } catch (e) {
    // Silently handle
  } finally {
    recordsLoading.value = false
  }
}

const loadPendingCount = async () => {
  try {
    const res = await getLeaveList({ status: 'PENDING', pageNum: 1, pageSize: 1 })
    pendingCount.value = res.data.total || 0
  } catch (e) {
    // Silently handle
  }
}

const handleCheckIn = async () => {
  checkLoading.value = true
  try {
    await checkIn()
    ElMessage.success('签到成功')
    await loadDashboard()
  } catch (e) {
    // Handled by interceptor
  } finally {
    checkLoading.value = false
  }
}

const handleCheckOut = async () => {
  checkOutLoading.value = true
  try {
    await checkOut()
    ElMessage.success('签退成功')
    await loadDashboard()
  } catch (e) {
    // Handled by interceptor
  } finally {
    checkOutLoading.value = false
  }
}

onMounted(() => {
  loadDashboard()
  loadPendingCount()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-actions .el-button {
  flex: 1;
  min-width: 140px;
}

.hours-display {
  text-align: center;
  padding: 20px 0;
}

.hours-number {
  font-size: 48px;
  font-weight: 700;
  color: #409EFF;
}

.hours-unit {
  font-size: 16px;
  font-weight: 400;
  color: #909399;
  margin-left: 4px;
}
</style>
