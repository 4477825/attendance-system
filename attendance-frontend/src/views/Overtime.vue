<template>
  <div class="overtime-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header>
            <span>提交加班申请</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>

            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>

            <el-form-item label="加班事由" prop="reason">
              <el-input
                v-model="form.reason"
                type="textarea"
                :rows="4"
                placeholder="请输入加班事由"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header>
            <span>我的加班记录</span>
          </template>
          <el-table :data="overtimes" stripe v-loading="loading" style="width: 100%">
            <el-table-column prop="overtimeDate" label="日期" width="120">
              <template #default="{ row }">{{ formatDate(row.overtimeDate) }}</template>
            </el-table-column>
            <el-table-column label="时段" width="280">
              <template #default="{ row }">
                {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="durationHours" label="时长" width="80">
              <template #default="{ row }">{{ row.durationHours }}h</template>
            </el-table-column>
            <el-table-column prop="reason" label="事由" min-width="150" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { applyOvertime, getOvertimeList } from '@/api/overtime'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const formRef = ref(null)
const submitting = ref(false)
const overtimes = ref([])
const loading = ref(false)

const form = reactive({
  startTime: '',
  endTime: '',
  reason: '',
})

const rules = {
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入加班事由', trigger: 'blur' }],
}

const statusLabel = (status) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status
}

const statusTag = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')
const formatTime = (time) => dayjs(time).format('HH:mm')

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const submitData = {
    startTime: form.startTime ? new Date(form.startTime).toISOString() : null,
    endTime: form.endTime ? new Date(form.endTime).toISOString() : null,
    reason: form.reason,
  }

  submitting.value = true
  try {
    await applyOvertime(submitData)
    ElMessage.success('加班申请已提交')
    resetForm()
    loadOvertimes()
  } catch (e) {
    // handled
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.startTime = ''
  form.endTime = ''
  form.reason = ''
}

const loadOvertimes = async () => {
  loading.value = true
  try {
    const res = await getOvertimeList({ pageNum: 1, pageSize: 20 })
    overtimes.value = res.data.records || []
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOvertimes()
})
</script>

<style scoped>
.overtime-page {
  padding-top: 10px;
}
</style>
