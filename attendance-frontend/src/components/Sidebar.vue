<template>
  <el-aside width="220px" class="sidebar">
    <div class="logo">
      <el-icon :size="24"><OfficeBuilding /></el-icon>
      <span class="logo-text">考勤管理</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      router
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
    >
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>

      <el-sub-menu index="attendance">
        <template #title>
          <el-icon><Calendar /></el-icon>
          <span>考勤管理</span>
        </template>
        <el-menu-item index="/attendance">打卡记录</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="leave">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>请假管理</span>
        </template>
        <el-menu-item index="/leave/apply">请假申请</el-menu-item>
        <el-menu-item index="/leave/list">请假列表</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/overtime">
        <el-icon><Timer /></el-icon>
        <span>加班登记</span>
      </el-menu-item>

      <el-menu-item v-if="isAdmin" index="/overtime/approval">
        <el-icon><Checked /></el-icon>
        <span>加班审批</span>
      </el-menu-item>

      <el-menu-item v-if="isAdmin" index="/statistics">
        <el-icon><TrendCharts /></el-icon>
        <span>统计报表</span>
      </el-menu-item>

      <el-sub-menu v-if="isAdmin" index="admin">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/departments">部门管理</el-menu-item>
        <el-menu-item index="/users">用户管理</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/profile">
        <el-icon><User /></el-icon>
        <span>个人中心</span>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { isAdmin } from '@/utils/auth'
import { OfficeBuilding, DataBoard, Calendar, Document, Timer, TrendCharts, Setting, User, Checked } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  background: #304156;
  overflow-x: hidden;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  background: #263445;
  gap: 8px;
  color: #fff;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

.el-menu {
  border-right: none;
}

.el-menu-item.is-active {
  background-color: #409EFF !important;
}
</style>
