<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header><span>头像</span></template>
          <div class="avatar-section">
            <el-avatar :size="100" :src="userStore.user?.avatarUrl || undefined">
              {{ userStore.user?.realName?.charAt(0) || 'U' }}
            </el-avatar>
            <div style="margin-top: 16px">
              <el-upload
                action="/api/upload/file"
                :headers="{ Authorization: 'Bearer ' + token }"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeUpload"
                accept="image/*"
              >
                <el-button type="primary" size="small">
                  <el-icon><Upload /></el-icon> 更换头像
                </el-button>
              </el-upload>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header><span>基本信息</span></template>
          <el-form ref="formRef" :model="form" label-width="100px" size="large">
            <el-form-item label="用户名">
              <el-input :value="userStore.user?.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="form.realName" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="form.role === 'ROLE_ADMIN' ? 'danger' : 'primary'">
                {{ form.role === 'ROLE_ADMIN' ? '管理员' : '普通员工' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="所属部门">
              <span>{{ userStore.user?.departmentName || '-' }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header><span>修改密码</span></template>
      <el-form :model="passwordForm" :rules="passwordRules" ref="pwdRef" label-width="100px" size="large" style="max-width: 500px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { updateProfile } from '@/api/auth'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const userStore = useUserStore()
const token = getToken()
const formRef = ref(null)
const pwdRef = ref(null)
const saving = ref(false)
const changingPwd = ref(false)

const form = reactive({
  realName: '',
  email: '',
  phone: '',
  role: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少6个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

const loadProfile = () => {
  if (userStore.user) {
    form.realName = userStore.user.realName || ''
    form.email = userStore.user.email || ''
    form.phone = userStore.user.phone || ''
    form.role = userStore.user.role || ''
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateProfile({
      id: userStore.user.id,
      realName: form.realName,
      email: form.email,
      phone: form.phone,
    })
    ElMessage.success('保存成功')
    loadProfile()
  } catch (e) {
    // handled
  } finally {
    saving.value = false
  }
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    const user = userStore.user
    user.avatarUrl = response.data
    userStore.user = user
    ElMessage.success('头像上传成功')
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const handleChangePassword = async () => {
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) return

  changingPwd.value = true
  try {
    await updateProfile({
      id: userStore.user.id,
      password: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    await userStore.logout()
    window.location.href = '/login'
  } catch (e) {
    // handled
  } finally {
    changingPwd.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  padding-top: 10px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>
