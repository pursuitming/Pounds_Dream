# Pounds-Dream 智能减脂平台

<p align="center">
  <img src="pounds-dream-web/public/vite.svg" alt="Logo" width="100" height="100">
</p>

<h3 align="center">AI 驱动的智能健康管理平台</h3>

<p align="center">
  一个集成了 AI 健康教练、数字孪生宠物、智能营养分析的全方位减脂管理平台
</p>

<p align="center">
  <a href="#功能特性">功能特性</a> •
  <a href="#技术栈">技术栈</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#项目结构">项目结构</a> •
  <a href="#部署">部署</a>
</p>

---

## 功能特性

### AI 智能功能

- **AI 健康教练** - 基于大语言模型的智能对话助手，支持 SSE 流式输出
  - 智能营养分析：描述食物即可获得详细的营养成分分析
  - 运动咨询：查询各种运动的热量消耗和建议
  - 餐食推荐：根据目标热量智能推荐减脂餐
  - 健康问答：解答各类健康和减脂问题
  - 思考过程展示：可视化 AI 的推理过程

- **数字孪生健身宠** - 虚拟宠物陪伴减脂旅程
  - 多种宠物形象可选
  - 通过健康行为（饮水、睡眠达标）获取经验值
  - 宠物成长系统，记录成长日志
  - 互动玩法，增加减脂趣味性

### 核心功能

- **用户系统** - 注册、登录、个人信息管理（JWT 认证）
- **仪表盘** - 数据可视化，一目了然的健康数据概览
- **体重记录** - 记录体重变化，生成趋势图表，计算 BMI
- **饮食记录** - 记录每日饮食，自动计算热量和营养素
- **运动记录** - 记录运动数据，基于 MET 值科学计算消耗热量
- **训练计划** - 创建个性化训练计划，按周几安排运动

### 健康管理

- **饮水记录** - 每日饮水量追踪，支持多种饮品类型
- **睡眠记录** - 记录睡眠时长和质量，生成睡眠报告
- **月经周期** - 女性生理周期记录和预测
- **段位系统** - 游戏化积分激励，连续打卡升级段位
- **社区互动** - 发帖分享减脂经验，互相鼓励

---

## 技术栈

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 渐进式 JavaScript 框架 |
| Vite | 下一代前端构建工具 |
| Element Plus | Vue 3 组件库 |
| ECharts | 数据可视化图表库 |
| Three.js | 3D 渲染（数字宠物） |
| Pinia | Vue 状态管理 |
| Vue Router | 路由管理 |
| Axios | HTTP 请求库 |

### 后端

| 技术 | 说明 |
|------|------|
| Spring Boot 3.2.5 | Java 应用框架 |
| MyBatis-Plus | ORM 框架 |
| Spring Security | 安全框架 |
| JWT | 身份认证 |
| Knife4j | API 文档 |
| Hutool | Java 工具库 |

### 数据库 & 部署

| 技术 | 说明 |
|------|------|
| MySQL 8.0 | 关系型数据库 |
| Docker | 容器化部署 |
| Nginx | 反向代理 & 静态资源服务 |

---

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 本地开发

**1. 克隆项目**

```bash
git clone https://github.com/your-username/Pounds-Dream.git
cd Pounds-Dream
```

**2. 初始化数据库**

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本
source pounds-dream-server/src/main/resources/db/schema.sql

# 导入初始数据
source pounds-dream-server/src/main/resources/db/data.sql
```

**3. 启动后端**

```bash
cd pounds-dream-server

# 修改数据库配置
# src/main/resources/application.yml

# 启动
mvn spring-boot:run
```

**4. 启动前端**

```bash
cd pounds-dream-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

**5. 访问应用**

- 前端：http://localhost:3000
- 后端 API：http://localhost:8080
- API 文档：http://localhost:8080/doc.html

---

## 项目结构

```
Pounds-Dream/
├── docker-compose.yml                  # Docker 编排文件
├── pom.xml                            # Maven 父工程
│
├── pounds-dream-server/               # 后端服务
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/poundsdream/
│       │   ├── controller/           # 控制器
│       │   │   ├── AICoachController.java    # AI 教练
│       │   │   ├── PetController.java        # 数字宠物
│       │   │   ├── RankController.java       # 段位系统
│       │   │   ├── DietController.java       # 饮食记录
│       │   │   ├── ExerciseController.java   # 运动记录
│       │   │   ├── WeightController.java     # 体重记录
│       │   │   ├── CommunityController.java  # 社区
│       │   │   └── ...
│       │   ├── service/              # 业务逻辑
│       │   ├── mapper/               # 数据访问
│       │   ├── entity/               # 实体类
│       │   ├── dto/                  # 数据传输对象
│       │   ├── vo/                   # 视图对象
│       │   ├── config/               # 配置类
│       │   └── security/             # 安全认证
│       └── resources/
│           ├── application.yml       # 应用配置
│           └── db/                   # 数据库脚本
│               ├── schema.sql
│               └── data.sql
│
└── pounds-dream-web/                  # 前端应用
    ├── Dockerfile
    ├── nginx.conf
    ├── package.json
    └── src/
        ├── api/                      # API 接口
        ├── views/                    # 页面组件
        │   ├── ai/
        │   │   └── AICoach.vue       # AI 教练页面
        │   ├── pet/
        │   │   └── PetPage.vue       # 数字宠物页面
        │   ├── dashboard/
        │   ├── diet/
        │   ├── exercise/
        │   ├── weight/
        │   ├── community/
        │   └── health/
        ├── components/               # 公共组件
        ├── store/                    # 状态管理
        ├── router/                   # 路由配置
        ├── utils/                    # 工具函数
        └── styles/                   # 样式文件
```

---

## 部署

### Docker 部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/your-username/Pounds-Dream.git
cd Pounds-Dream

# 2. 创建环境变量文件
cat > .env << 'EOF'
DB_PASSWORD=your-strong-password
JWT_SECRET=your-jwt-secret-key-at-least-32-chars
EOF

# 3. 构建并启动
docker compose up -d --build

# 4. 访问应用
# http://your-server-ip
```

详细部署文档请参考：[部署文档.md](./部署文档.md)

---

## 功能截图

### AI 健康教练
智能对话界面，支持流式输出和思考过程展示

### 数字孪生宠物
可爱的虚拟宠物陪伴你的减脂旅程

### 数据仪表盘
直观的数据可视化，掌握健康状况

### 社区互动
分享经验，互相鼓励

---

## API 文档

启动后端服务后访问：
- Swagger UI：http://localhost:8080/swagger-ui.html
- Knife4j：http://localhost:8080/doc.html

### 主要 API 模块

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/api/auth` | 注册、登录、刷新 Token |
| AI 教练 | `/api/ai` | AI 对话、对话管理 |
| 饮食 | `/api/diet` | 饮食记录、食物查询 |
| 运动 | `/api/exercise` | 运动记录、运动类型 |
| 体重 | `/api/weight` | 体重记录、趋势统计 |
| 宠物 | `/api/pet` | 宠物信息、互动 |
| 段位 | `/api/rank` | 段位信息、打卡 |
| 社区 | `/api/community` | 帖子、评论、点赞 |
| 健康 | `/api/water`, `/api/sleep` | 饮水、睡眠记录 |

---

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

---

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 致谢

感谢所有为这个项目做出贡献的开发者！

---

<p align="center">
  Made with ❤️ by Pounds-Dream Team
</p>
