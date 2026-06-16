<template>
  <div class="attendance-page">
    <el-card shadow="never" style="margin-bottom: 20px">
      <div class="toolbar">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="loadRecords"
        />
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadRecords" style="width: 150px; margin-left: 12px">
          <el-option label="正常" value="NORMAL" />
          <el-option label="迟到" value="LATE" />
          <el-option label="早退" value="EARLY_ABSENTEE" />
          <el-option label="缺勤" value="ABSENT" />
        </el-select>
        <el-button type="primary" @click="loadRecords" style="margin-left: 12px">
          <el-icon><Search /></el-icon> 查询
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <span>考勤记录</span>
      </template>
      <el-table :data="records" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="date" label="日期" width="120">
          <template #default="{ row }">{{ formatDate(row.date) }}</template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" width="180">
          <template #default="{ row }">{{ row.checkInTime ? formatTime(row.checkInTime) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="checkOutTime" label="签退时间" width="180">
          <template #default="{ row }">{{ row.checkOutTime ? formatTime(row.checkOutTime) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="工时" width="80">
          <template #default="{ row }">{{ row.workHours ? row.workHours + 'h' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top: 16px; justify-content: center"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        @current-change="loadRecords"
        @size-change="loadRecords"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRecords } from '@/api/attendance'
import dayjs from 'dayjs'

const records = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dateRange = ref(null)
const statusFilter = ref('')

const statusLabel = (status) => {
  const map = { NORMAL: '正常', LATE: '迟到', ABSENT: '缺勤', EARLY_ABSENTEE: '早退' }
  return map[status] || status
}

const tagType = (status) => {
  const map = { NORMAL: 'success', LATE: 'warning', ABSENT: 'danger', EARLY_ABSENTEE: 'info' }
  return map[status] || 'info'
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')
const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

const loadRecords = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const res = await getRecords(params)
    records.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}
</style>
