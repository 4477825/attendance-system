[Uploading README.md…]()
# 员工考勤管理系统

基于 Spring Boot 2.7 + Vue 3 + MySQL + Redis 的员工考勤管理系统，支持打卡签到、请假审批、加班登记、考勤统计与数据导出等功能。

## 技术栈

| 后端 | 前端 |
|------|------|
| Java 11 + Spring Boot 2.7.18 | Vue 3 + Vue Router 4 |
| Spring Security + JWT | Pinia 状态管理 |
| MyBatis-Plus 3.5.5 | Element Plus 2.6 |
| MySQL 8.0 + Redis | Axios + ECharts |
| EasyExcel 3.3 | Vite 5 |

## 功能模块

- **用户管理** — 注册登录、个人信息管理、头像上传
- **打卡签到** — 每日签到/签退，自动判断迟到/早退/缺勤
- **请假管理** — 申请提交、审批流转（年假/事假/病假/调休）
- **加班管理** — 加班登记、审批、时长自动计算
- **考勤统计** — 按月统计、部门筛选、可视化图表
- **数据导出** — 考勤/请假/加班数据导出为 Excel
- **系统管理** — 部门管理、用户管理、权限控制（管理员）

## 快速启动

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+
- Node.js 16+
- npm 8+

### 1. 数据库初始化

执行 SQL 脚本创建数据库和表结构：

```bash
mysql -u root -p < sql/init.sql
```

如遇加班表缺少字段，执行修复脚本：

```bash
mysql -u root -p < sql/fix_overtime_table.sql
```

默认种子数据：
- 管理员：`admin` / `admin123`
- 普通员工：`employee` / `employee123`

### 2. 启动 Redis

```bash
redis-server
```

### 3. 启动后端

```bash
cd attendance-backend
mvn clean install -DskipTests
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8081`，启动后可通过 Swagger 文档查看接口：

```
http://localhost:8081/swagger-ui.html
```

### 4. 启动前端

```bash
cd attendance-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:3000`，已配置代理转发 `/api` 和 `/uploads` 到后端 8081 端口。

### 5. 登录系统

打开浏览器访问 `http://localhost:3000`，使用管理员账号登录体验完整功能。

## 项目结构

```
attendance-backend/                          # 后端项目
├── pom.xml
├── src/main/java/com/attendance/
│   ├── AttendanceApplication.java           # 应用入口
│   ├── common/                              # 公共模块（Result、ErrorCode、异常处理）
│   ├── config/                              # 配置类（Security、Redis、Swagger等）
│   ├── security/                            # JWT认证与鉴权
│   ├── controller/                          # RESTful API 控制器
│   ├── dto/                                 # 数据传输对象
│   ├── entity/                              # 实体类
│   ├── mapper/                              # MyBatis-Plus 数据访问层
│   └── service/                             # 业务逻辑层
└── src/main/resources/
    └── application.yml                      # 应用配置

attendance-frontend/                         # 前端项目
├── package.json
├── vite.config.js
└── src/
    ├── api/                                 # API 接口层（Axios）
    ├── router/                              # 路由配置与导航守卫
    ├── store/                               # Pinia 状态管理
    ├── utils/                               # 工具函数（Token管理等）
    ├── components/                          # 公共组件
    └── views/                               # 页面视图
```

## API 接口

系统提供 30+ 个 RESTful API，统一返回格式 `{code, message, data}`，认证方式为 Bearer JWT。

主要接口分类：

| 分类 | 接口数 | 说明 |
|------|--------|------|
| 认证 | 2 | 注册、登录 |
| 用户 | 8 | 个人信息、用户管理（管理员） |
| 部门 | 4 | 部门列表、CRUD（管理员） |
| 考勤 | 5 | 签到/签退、记录查询、月度汇总 |
| 请假 | 5 | 申请、列表、详情、审批、删除 |
| 加班 | 4 | 申请、列表、详情、审批 |
| 统计 | 3 | 部门统计、全局统计、个人明细 |
| 导出 | 3 | 考勤、请假、加班导出为 Excel |
| 文件 | 2 | 上传、下载 |

## 部署

### 单机部署

```bash
# 后端打包
cd attendance-backend
mvn clean package -DskipTests
java -jar target/attendance-system.jar

# 前端构建
cd attendance-frontend
npm run build     # 生成 dist/ 目录
```

使用 Nginx 反向代理，将 `/api` 和 `/uploads` 代理到后端，前端静态文件由 Nginx 托管。
