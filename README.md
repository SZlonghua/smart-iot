# Smart IoT — 轻量级物联网管理平台

基于 [SmartAdmin v3.0](https://smartadmin.vip) 构建的轻量级物联网设备管理平台，支持多协议设备接入、设备监控告警、物模型管理。

## 系统架构

```
IoT 平台
├── 物联管理
│   ├── 产品分类    — 层级分类树管理
│   ├── 产品管理    — 产品 CRUD + 物模型（属性/事件/功能）编辑
│   ├── 设备管理    — 设备 CRUD + 父子设备 + deviceKey 自动生成
│   └── 设备日志    — 设备运行日志查询
├── 网络管理
│   ├── 网络组件    — MQTT Broker / HTTP Server / CoAP Server / TCP Server
│   ├── 协议管理    — 协议 JAR 包管理
│   └── 设备网关    — 组件 + 协议绑定，支持 MQTT 直连 / TCP 透传 / 网关子设备
└── 告警管理
    └── 告警日志    — 紧急/重要/次要/提示 四级告警 + 处理状态流转
```

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 + Ant Design Vue 4 + Vite 5 |
| 后端 | Java 8 + Spring Boot 2.7 + MyBatis-Plus 3.5 |
| 认证 | Sa-Token |
| 数据库 | MySQL 8.x |
| 缓存 | Redis |
| 设备通信 | MQTT（规划中） |
| 时序存储 | TDengine（规划中） |

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- Node.js 20+
- MySQL 8.x
- Redis

### 数据库

```bash
# 导入基础表结构（已包含 8 张 IoT 业务表）
mysql -u root -p --default-character-set=utf8mb4 smart_admin_v3 < docs/ai-context/create_tables.sql
```

### 后端

```bash
cd smart-admin-api-java8-springboot2
mvn clean install -Pdev -DskipTests
cd sa-admin
mvn spring-boot:run -Pdev
# 启动后访问: http://localhost:1024
# API 文档: http://localhost:1024/doc.html
```

### 前端

```bash
cd smart-admin-web-javascript
npm install
npm run localhost
# 启动后访问: http://localhost:8081
```

## 数据库表

| 表名 | 说明 |
|------|------|
| `product_category` | 产品分类（层级树） |
| `product` | 产品（含 productKey/Secret、物模型 JSON） |
| `device` | 设备（含 deviceKey/Secret、父子设备、上线状态） |
| `network_component` | 网络组件（MQTT/HTTP/CoAP/TCP Server） |
| `protocol` | 协议（JAR 包管理） |
| `gateway` | 设备网关（组件+协议绑定） |
| `alarm_log` | 告警日志（四级告警、状态流转） |
| `device_log` | 设备运行日志 |

## 设备类型

| 类型 | 说明 |
|------|------|
| 直连设备 (direct) | 直接通过 MQTT 等协议连接平台 |
| 网关子设备 (gateway_child) | 挂载在网关下的子设备 |
| 网关设备 (gateway) | 代理子设备连接的管理设备 |

## MQTT 通信主题（规划）

设备与平台通过 MQTT 通信，主题格式：`/{productId}/{deviceId}/...`

- `properties/report` — 属性上报
- `properties/read` / `properties/write` — 属性读写
- `function/invoke` — 功能调用
- `event/{eventId}` — 事件上报
- `online` / `offline` — 上下线通知
- `register` / `unregister` — 设备注册/注销

## 项目结构

```
smart-iot/
├── smart-admin-web-javascript/     # 前端 (Vue 3)
│   └── src/
│       ├── api/business/           # IoT 业务 API 模块
│       ├── views/business/         # IoT 业务页面
│       └── constants/business/     # IoT 枚举常量
├── smart-admin-api-java8-springboot2/  # 后端 (Spring Boot)
│   └── sa-admin/src/main/
│       ├── java/.../module/business/  # IoT 业务模块
│       └── resources/mapper/business/ # MyBatis XML
└── docs/ai-context/               # 需求与设计文档
    ├── 轻量级IoT平台PRD文档.md
    ├── IoT平台详细设计文档.md
    ├── MQTT协议文档.md
    ├── 平台统一设备消息定义文档.md
    ├── iot-platform-prototype.html
    └── create_tables.sql
```

## 文档

详细设计文档见 [docs/ai-context/](./docs/ai-context/)：

- [PRD 文档](./docs/ai-context/轻量级IoT平台PRD文档_产品不绑定网关.md)
- [详细设计文档](./docs/ai-context/IoT平台详细设计文档.md)
- [MQTT 协议文档](./docs/ai-context/MQTT协议文档.md)
- [设备消息定义文档](./docs/ai-context/平台统一设备消息定义文档.md)
- [高保真原型](./docs/ai-context/iot-platform-prototype.html)

## License

基于 [SmartAdmin](https://smartadmin.vip) 构建，遵循其开源协议。
