import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, isAdmin } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', public: true },
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' },
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/Attendance.vue'),
        meta: { title: '考勤管理' },
      },
      {
        path: 'leave/apply',
        name: 'LeaveApply',
        component: () => import('@/views/LeaveApply.vue'),
        meta: { title: '请假申请' },
      },
      {
        path: 'leave/list',
        name: 'LeaveList',
        component: () => import('@/views/LeaveList.vue'),
        meta: { title: '请假列表' },
      },
      {
        path: 'overtime',
        name: 'Overtime',
        component: () => import('@/views/Overtime.vue'),
        meta: { title: '加班登记' },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: '统计报表', adminOnly: true },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/UserProfile.vue'),
        meta: { title: '个人中心' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 员工考勤管理系统` : '员工考勤管理系统'

  if (to.meta.public) {
    next()
  } else if (!isLoggedIn()) {
    next('/login')
  } else if (to.meta.adminOnly && !isAdmin()) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
