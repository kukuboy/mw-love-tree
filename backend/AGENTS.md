# AGENTS.md - 开发约束与规范

本文档为 AI 助手在修改此项目时必须遵守的约束和操作规范。

## 项目结构

```
workspace/
├── backend/                    # Spring Boot 后端
│   ├── src/
│   │   ├── main/java/com/lovetree/
│   │   │   ├── common/       # 公共类
│   │   │   ├── config/       # 配置类
│   │   │   ├── controller/   # 控制器
│   │   │   ├── dto/         # DTO
│   │   │   ├── entity/      # 实体
│   │   │   ├── mapper/      # MyBatis
│   │   │   └── service/     # 业务逻辑
│   │   └── test/            # 测试
│   ├── AGENTS.md            # 本文件
│   └── pom.xml
└── frontend/                  # Vue 3 前端
    ├── src/
    │   ├── api/             # API 调用
    │   ├── components/      # 组件
    │   ├── pages/          # 页面
    │   ├── stores/         # Pinia 状态
    │   └── styles/         # 样式
    └── package.json

## 修改前必读

### 1. 修改前必须阅读的文件

在开始任何修改之前，必须先完整阅读以下文件：

| 文件路径 | 必要性 | 原因 |
|---------|--------|------|
| `AGENTS.md` | **必须** | 本文档，了解所有约束 |
| `pom.xml` | **必须** | 了解依赖和构建配置 |
| `src/main/resources/application.yml` | **必须** | 了解配置项和默认值 |
| 相关业务代码文件 | **必须** | 理解现有实现逻辑 |

### 2. 前端修改约束

#### 编译验证
- **每次前端代码修改后，必须运行编译检查**：
  ```bash
  cd frontend
  npx vue-tsc --noEmit
  ```

- **如果编译失败，必须立即修复**：
  - 检查 TypeScript 类型错误
  - 确保导入路径正确
  - 验证依赖是否正确安装
  - 修复所有编译错误后再继续其他任务

#### 构建验证
- 修改完成后，运行生产构建确保无问题：
  ```bash
  cd frontend
  npm run build
  ```

### 3. 后端修改约束

#### 测试用例验证
- **后端代码修改后，必须运行测试用例验证**：
  ```bash
  cd backend
  mvn test -Dtest=*ControllerTest
  ```

- **测试覆盖范围**：
  - 所有 Controller 层的 API 接口
  - 关键业务逻辑的 Service 层
  - 数据校验和边界条件

#### API 测试用例清单

| Controller | 测试类 | 测试方法数 | 覆盖接口 |
|-----------|--------|-----------|---------|
| AuthController | AuthControllerTest | 7 | /api/auth/register, /api/auth/login |
| CoupleController | CoupleControllerTest | 8 | /api/couple/* |
| EventController | EventControllerTest | 10 | /api/events/* |
| MessageController | MessageControllerTest | 9 | /api/messages/* |
| PhotoController | PhotoControllerTest | 6 | /api/photos/* |
| SpecialDayController | SpecialDayControllerTest | 5 | /api/special-days/* |
| TreeController | TreeControllerTest | 8 | /api/tree/* |

#### 运行完整测试
```bash
cd backend
mvn test
```

#### 运行特定测试
```bash
# 运行单个测试类
mvn test -Dtest=AuthControllerTest

# 运行单个测试方法
mvn test -Dtest=AuthControllerTest#register_Success
```

### 4. 代码风格约束

#### 前端
- 使用 TypeScript 的类型安全特性
- 避免使用 `any` 类型
- Vue 组件使用 `<script setup>` 语法
- 样式使用 scoped 避免污染

#### 后端
- 使用 `@Valid` 注解进行输入校验
- 异常使用 `BusinessException` 而非直接抛出
- API 响应统一使用 `ApiResponse` 包装
- Controller 参数使用 `@RequestBody`、`@RequestParam` 明确标注

### 5. Git 提交规范

- 提交前确保所有测试通过
- 提交信息使用中文描述修改内容
- 相关联的修改放在同一提交中
- 不相关的修改分开提交

### 6. 安全约束

- **禁止提交敏感信息**：API 密钥、数据库密码等
- **使用环境变量**：敏感配置通过环境变量注入
- **JWT 安全**：token 必须在客户端正确存储和传输

## 修改工作流程

### 前端修改流程
1. 阅读相关代码和文档
2. 进行代码修改
3. 运行 `vue-tsc --noEmit` 验证类型
4. 如有错误，修复后重新验证
5. 运行 `npm run build` 确保构建成功
6. 提交代码

### 后端修改流程
1. 阅读相关代码和文档
2. 进行代码修改
3. 运行 `mvn test` 验证测试通过
4. 如有测试失败，修复代码或更新测试用例
5. 确保所有测试通过
6. 提交代码

## 常见问题处理

### 前端编译错误
- 检查是否有缺失的导入
- 验证类型定义是否正确
- 确保依赖已正确安装 `npm install`

### 后端测试失败
- 检查数据库 schema 是否匹配
- 验证测试数据是否正确
- 查看具体错误信息进行针对性修复

## 文件结构参考

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/lovetree/
│   │   │   ├── common/          # 公共类（异常、响应、工具）
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/     # 控制器
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── mapper/          # MyBatis 映射
│   │   │   └── service/         # 业务逻辑
│   │   └── resources/
│   │       └── application.yml  # 应用配置
│   └── test/
│       ├── java/com/lovetree/
│       │   └── controller/      # 控制器测试
│       └── resources/
│           ├── application-test.yml  # 测试配置
│           └── schema.sql       # 测试数据库 schema
└── pom.xml

frontend/
├── src/
│   ├── api/                     # API 调用
│   ├── components/              # Vue 组件
│   ├── pages/                   # 页面组件
│   ├── stores/                  # Pinia 状态
│   ├── styles/                  # 全局样式
│   ├── types/                   # TypeScript 类型
│   └── utils/                   # 工具函数
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```
