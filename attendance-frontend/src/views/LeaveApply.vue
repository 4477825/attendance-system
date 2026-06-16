<template>
  <div class="leave-apply-page">
    <el-card shadow="never" style="max-width: 700px; margin: 0 auto">
      <template #header>
        <span>提交请假申请</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="年假" value="年假" />
            <el-option label="事假" value="事假" />
            <el-option label="病假" value="病假" />
            <el-option label="调休" value="调休" />
          </el-select>
        </el-form-item>

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

        <el-form-item label="请假事由" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入请假事由"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="附件上传">
          <el-upload
            action="/api/upload/file"
            :headers="{ Authorization: 'Bearer ' + token }"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :limit="1"
            accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
          >
            <el-button type="primary" v-if="!form.attachmentUrl">
              <el-icon><Upload /></el-icon> 选择文件
            </el-button>
            <el-tag v-else type="success">
              <el-icon><Document /></el-icon> 已上传附件
            </el-tag>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { applyLeave } from '@/api/leave'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { Upload, Document } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const token = getToken()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  leaveType: '',
  startTime: '',
  endTime: '',
  reason: '',
  attachmentUrl: '',
})

const rules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假事由', trigger: 'blur' }],
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.attachmentUrl = response.data
    ElMessage.success('附件上传成功')
  }
}

const beforeUpload = (file) => {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('文件大小不能超过10MB')
    return false
  }
  return true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // Convert datetime string to ISO format
  const submitData = {
    ...form,
    startTime: form.startTime ? new Date(form.startTime).toISOString() : null,
    endTime: form.endTime ? new Date(form.endTime).toISOString() : null,
  }

  submitting.value = true
  try {
    await applyLeave(submitData)
    ElMessage.success('请假申请已提交')
    router.push('/leave/list')
  } catch (e) {
    // handled
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.leave-apply-page {
  padding-top: 10px;
}
</style>
