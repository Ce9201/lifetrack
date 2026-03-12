# Lifetrack 睡眠追踪系统

一个前后端分离的睡眠记录与可视化应用，帮助用户追踪每日睡眠时长，并通过图表直观展示近7天趋势。

## 技术栈

- **后端**：Spring Boot 3.x, Spring Security, JWT, Spring Data JPA, MySQL, Redis
- **前端**：HTML + ECharts（静态页面，集成在 Spring Boot 中）
- **工具**：Maven, Knife4j (Swagger), Lombok

## 功能亮点

- ✅ **用户认证**：注册/登录，基于 JWT 的无状态认证，Redis 存储 token 黑名单与 refresh token，支持主动注销。
- ✅ **睡眠记录管理**：增删改查，自动计算睡眠时长，时间重叠校验。
- ✅ **数据可视化**：个人主页展示最近7天睡眠时长柱状图，鼠标悬停可查看每日详细记录。
- ✅ **缓存优化**：使用 Redis 缓存近期睡眠记录（key `sleep:recent:{userId}`，过期1小时），通过互斥锁防止缓存击穿，高并发下仅单一线程查询数据库。
- ✅ **全局异常处理**：统一响应格式，业务异常携带自定义错误码，前端友好提示。
- ✅ **API 文档**：集成 Knife4j，自动生成交互式接口文档，支持在线调试。

## 快速开始

### 前置要求

- JDK 17 或更高版本
- MySQL 8.0+
- Redis 7.0+
- Maven 3.6+

### 配置

#### 使用示例配置文件（开发环境）
复制`application-example.properties`为`application.properties`，并根据本地环境修改其中的数据库连接、Redis 连接和 JWT 密钥。

### 运行

1. 创建数据库 `lifetrack`（字符集建议 `utf8mb4`）。
2. 确保 MySQL 和 Redis 服务已启动。
3. 在项目根目录执行：

```bash
mvn spring-boot:run
```

4. 访问前端页面：http://localhost:8080  
5. 访问 API 文档：http://localhost:8080/doc.html

## 项目结构

```
src/main/java/com/sun/lifetrack/
├── config         # 安全配置、JWT过滤器、Jackson配置等
├── controller     # 接口层（UserController, SleepRecordController）
├── dto            # 请求/响应对象（含校验注解）
├── entity         # 数据库实体
├── exception      # 自定义异常、错误码枚举、全局异常处理器
├── repository     # JPA数据访问层
├── service        # 业务逻辑层（接口与实现）
│   └── impl       # 具体实现，包含缓存与互斥锁逻辑
└── util           # 工具类（JwtUtil）
```

## API 文档

启动应用后，访问 [http://localhost:8080/doc.html](http://localhost:8080/doc.html) 即可查看所有接口的详细说明，并在线进行调试（需先通过登录接口获取 token，然后在文档右上角设置全局 `Authorization` 头）。

## 部署

### 打包为可执行 JAR

使用 Maven 打包项目：

```bash
mvn clean package
```

生成的 JAR 文件位于 `target/` 目录下，使用以下命令运行：

```bash
java -jar target/lifetrack-*.jar
```

## 贡献

欢迎提交 Issue 或 Pull Request。如果你有任何建议或改进，请通过 GitHub 仓库联系。

## 许可证

[MIT](LICENSE)

## 作者

[sun]  
GitHub: [https://github.com/Ce9201](https://github.com/Ce9201)  
项目地址: [https://github.com/Ce9201/LifeTrack.git](https://github.com/Ce9201/LifeTrack.git)
```

