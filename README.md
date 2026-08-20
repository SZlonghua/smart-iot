# Smart IoT — 轻量级物联网管理平台

基于 [SmartAdmin v3.0](https://smartadmin.vip) 构建的轻量级物联网设备管理平台，聚焦设备管理、多协议接入、设备告警三大核心能力，内置自研 MQTT Broker，支持协议 SPI 扩展。

## 功能架构

```
轻量级 IoT 平台
├── 物联管理
│   ├── 产品分类    — 产品分类管理
│   ├── 产品管理    — 产品 CRUD + 物模型（属性/功能/事件）编辑
│   └── 设备管理    — 设备 CRUD + 父子设备 + deviceKey 自动生成
├── 网络管理
│   ├── 网络组件    — MQTT/HTTP/CoAP/TCP Server 等网络通道管理
│   ├── 协议管理    — 协议 JAR 包 / 本地 class 加载（SPI 扩展）
│   └── 设备网关    — 组件 + 协议绑定，支持 MQTT 直连 / TCP 透传 / 网关子设备
└── 告警管理
    └── 告警日志    — 紧急/重要/次要/提示 四级告警 + 处理状态流转
```

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 + Ant Design Vue 4 + Vite 5 |
| 后端 | Java 8 + Spring Boot 2.7 + MyBatis-Plus 3.5 |
| 响应式 | Project Reactor 3.4（全链路 Mono/Flux） |
| 网络框架 | Vert.x 4.5（MQTT Broker / TCP / HTTP / WebSocket / CoAP） |
| 认证 | Sa-Token（管理端）+ HMAC-SHA256 设备签名认证 |
| 数据库 | MySQL 8.x |
| 缓存 | Redis（设备/产品注册表、会话缓存） |
| 时序存储 | TDengine（规划中） |

## 设备接入链路

平台运行时以事件驱动方式联动：网络组件提供通道，网关执行接入逻辑，协议负责编解码与认证，会话管理设备在线状态，消息最终进入事件总线供业务消费。

```
设备连接
  │
  ▼
Network 组件（MQTT Broker / TCP Server / HTTP ...）    ← 网络通道，SPI 可替换实现
  │ 连接到达
  ▼
DeviceGateway（接入网关）                              ← 生命周期管理（启动/暂停/关闭）
  │ ① 网关状态判定 ② 设备认证 ③ 会话注册 ④ accept
  ▼
ProtocolSupport.authenticate()                        ← 协议认证（HMAC-SHA256 签名比对）
  │ 认证成功
  ▼
DeviceSessionManager.compute(session)                 ← 会话注册（DeviceSessionEvent 发布）
  │ 设备消息到达
  ▼
ProtocolSupport.getMessageCodec() → codec.decode()     ← 协议编解码（Topic → Message）
  │ deviceId 回填（直连取连接会话 / 子设备查子设备会话）
  ▼
DecodedClientMessageHandler.handle()                   ← 消息路由
  │
  ▼
IEventBus（动态事件总线，泛型精准匹配）                   ← 业务侧订阅消费
```

关键设计：

- **网络与接入分离** — `Network` 只负责通道建立与生命周期（bind/connect/shutdown），`DeviceGateway` 负责设备接入逻辑（编解码、认证、会话），通过 `NetworkManager.getNetwork(type, componentId)` 关联
- **协议 SPI 扩展** — `ProtocolSupport` 以 jar 包 / 本地 class 形式加载，编解码和认证全部由外部实现，平台通过 `ProtocolSupportManager` 注册中心管理
- **全链路响应式** — 从连接到达、认证、会话注册到消息路由全部走 `Mono`/`Flux` 链式调用
- **会话分层** — 连接型会话（MQTT/TCP/WS 持有连接）、无连接型会话（HTTP/CoAP 追踪活跃时间）、子设备会话（alive 跟随父网关会话，不持有连接）
- **事件驱动** — 网络组件/网关/协议 CRUD 事件驱动运行时动态启停与重载，设备上线/下线事件持久化在线状态

## MQTT 通信协议

### 连接认证

| 字段 | 格式 | 说明 |
|------|------|------|
| clientId | `{productKey}:{deviceKey}:{timestamp}:{mode}:{signType}` | `:` 分隔，5 段 |
| username | `{productKey}:{deviceKey}` | `:` 分隔（productKey 本身可能含 `-`） |
| password | HMAC-SHA256 签名（hex） | `HMAC-SHA256(productKey:deviceKey:timestamp, secret)` |

- **一机一密（mode=1）**：secret = 设备密钥 deviceSecret
- **一型一密（mode=2）**：secret = 产品密钥 productSecret
- **防重放**：`|now - timestamp| ≤ 5 分钟`，超出拒绝连接

### 消息主题

主题格式：`/{productKey}/{deviceKey}/...`，子设备消息经由网关代理：`/{gatewayPk}/{gatewayDk}/child/{childPk}/{childDk}/...`

| 方向 | Topic | 消息类型 |
|------|-------|----------|
| 上行 | `/{pk}/{dk}/properties/report` | 属性上报 |
| 上行 | `/{pk}/{dk}/properties/read/reply` | 读属性回复 |
| 上行 | `/{pk}/{dk}/properties/write/reply` | 写属性回复 |
| 上行 | `/{pk}/{dk}/function/invoke/reply` | 功能调用回复 |
| 上行 | `/{pk}/{dk}/event/{eventId}` | 事件上报（eventId 取 topic 末段） |
| 上行 | `/{pk}/{dk}/register` / `unregister` | 注册 / 注销 |
| 上行 | `/{pk}/{dk}/online` / `offline` | 上线 / 离线 |
| 下行 | `/{pk}/{dk}/properties/read` / `write` | 属性读写 |
| 下行 | `/{pk}/{dk}/function/invoke` | 功能调用 |
| 上行 | `/{pk}/{dk}/child/{cpk}/{cdk}/...` | 子设备消息（属性/事件/上下线/注册） |
| 上行 | `/{pk}/{dk}/child-reply/{cpk}/{cdk}/...` | 子设备回复 |

### 消息定义

统一消息体系位于 `common/message/`：`Message` → `DeviceMessage` → `AbstractDeviceMessage`（deviceKey/messageId/timestamp/headers）。回复类消息实现 `MessageReply`，可回复消息实现 `RepayableMessage`。常用 headers：`async`（异步调用）、`timeout`（调用超时）、`frag_*`（消息分片）、`keepOnline`（设备离线保留）、`ignoreStorage`（跳过存储）等。

## 核心设计

| 组件 | 说明 |
|------|------|
| DeviceGateway | 网关运行时抽象（id/state/onMessage/startup/pause/shutdown），生命周期状态机 starting→started→paused→shutdown |
| ProtocolSupport | 协议 SPI（getMessageCodec / authenticate / addAuthenticator），jar / local 双加载器，CRUD 事件动态注册注销 |
| Network | 网络组件抽象（Server/Client 标记接口），11 种内置类型，Vert.x 实现可替换 |
| DeviceRegistry | 设备注册中心（Redis Hash 存储设备/产品信息，Set 维护产品-设备绑定，String 维护 key 映射） |
| DeviceSession | 会话体系（连接型/无连接型/子设备会话），LocalDeviceSessionManager 提供 compute/remove/事件监听 |
| IEventBus | 动态事件总线（EventHandler 泛型精准匹配，publish/publishAsync），与 Spring @EventListener 共存 |
| CRUD 事件 | MyBatis 拦截器自动发布 Save/Update/Delete 前后事件（beforeData/afterData，物理/软删除区分） |
| 物模型 TSL | ThingsMetadata 聚合根 + 7 种 DataType 编解码体系（DataTypeCodecRegistry 扩展） |
| 缓存管理 | ICacheManager（String/Hash/Set 三种结构）+ IConfigStorage（配置 Hash 专用存储） |

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- Node.js 20+
- MySQL 8.x
- Redis

### 数据库

```bash
# 导入完整表结构（smart_admin_v3.sql 已包含所有表，含 IoT 业务表）
mysql -u root -p --default-character-set=utf8mb4 smart_admin_v3 < 数据库SQL脚本/mysql/smart_admin_v3.sql
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

协议扩展项目独立构建：

```bash
cd sa-protocol    # 独立协议实现（编解码 + 认证）
mvn install -DskipTests
```

### 前端

```bash
cd smart-admin-web-javascript
npm install
npm run localhost
# 启动后访问: http://localhost:8081
```

### MQTT 设备联调

平台内置 MQTT Broker，设备接入步骤：

1. 产品管理创建产品（记录 productKey/productSecret，编辑物模型）
2. 设备管理创建设备（记录 deviceKey/deviceSecret）
3. MQTT 客户端（如 MQTTX）连接 `localhost:1883`，按上文认证格式填写 clientId/username/password
4. 连接成功后向 `/{productKey}/{deviceKey}/properties/report` 发布属性上报 JSON，平台侧设备管理可见在线状态与日志

## 数据库表

| 表名 | 说明 |
|------|------|
| `product_category` | 产品分类 |
| `product` | 产品（含 productKey/Secret、物模型 model_json、设备类型） |
| `device` | 设备（含 deviceKey/Secret、父子设备、上线状态） |
| `network_component` | 网络组件（MQTT/HTTP/CoAP/TCP Server，configuration JSON） |
| `protocol` | 协议（jar/local 加载方式、jarPath） |
| `gateway` | 设备网关（组件 + 协议绑定） |
| `alarm_log` | 告警日志（四级告警、状态流转） |
| `device_log` | 设备运行日志（上下线/属性上报/事件/命令） |

## 设备类型

| 类型 | 说明 |
|------|------|
| 直连设备 (direct) | 直接通过 MQTT 等协议连接平台 |
| 网关子设备 (gateway_child) | 挂载在网关下的子设备（经网关代理通信） |
| 网关设备 (gateway) | 代理子设备连接的管理设备 |

## 项目结构

```
smart-iot/
├── smart-admin-web-javascript/     # 前端 (Vue 3)
│   └── src/
│       ├── api/business/           # IoT 业务 API 模块
│       ├── views/business/         # IoT 业务页面
│       └── constants/business/     # IoT 枚举常量
├── smart-admin-api-java8-springboot2/  # 后端 (Spring Boot)
│   ├── sa-base/                    # 基础设施（IoT 运行时核心）
│   │   └── src/main/java/net/lab1024/sa/base/
│   │       ├── common/gateway/     # DeviceGateway 网关抽象
│   │       ├── common/network/     # Network 网络组件抽象
│   │       ├── common/protocol/    # ProtocolSupport 协议 SPI
│   │       ├── common/message/     # 统一消息体系 + topic 编解码
│   │       ├── common/event/       # CRUD 事件 + EventBus
│   │       ├── device/             # 设备注册中心/操作器/认证
│   │       ├── device/session/     # 会话体系
│   │       ├── metadata/           # 物模型 TSL 元数据
│   │       ├── module/support/protocol/  # 协议实现（mqtt/http/tcp...）
│   │       └── module/support/cache/     # 缓存管理
│   └── sa-admin/                   # 业务模块（物联/网络/告警管理）
│       └── src/main/
│           ├── java/.../module/business/  # 业务模块
│           └── resources/mapper/business/ # MyBatis XML
├── sa-protocol/                    # 协议扩展实现（编解码 + 认证，独立构建）
└── docs/ai-context/                # 需求与设计文档
```

## 文档

详细设计文档见 [docs/ai-context/](./docs/ai-context/)：

| 文档 | 内容 |
|------|------|
| [PRD 文档](./docs/ai-context/轻量级IoT平台PRD文档_产品不绑定网关.md) | 产品需求（物联/网络/告警管理） |
| [详细设计文档](./docs/ai-context/IoT平台详细设计文档.md) | 数据库设计、物模型 JSON 结构 |
| [MQTT 协议文档](./docs/ai-context/MQTT协议文档.md) | 上下行 Topic 规范、消息 JSON 格式 |
| [设备消息定义](./docs/ai-context/平台统一设备消息定义文档.md) | 消息接口体系、headers 约定 |
| [MQTT 认证方案](./docs/ai-context/MQTT设备认证方案设计.md) | 一机一密/一型一密签名认证 |
| [Codec 解码方案](./docs/ai-context/MqttDeviceMessageCodec解码方案设计.md) | Topic 枚举解码、子设备递归解码 |
| [网关设计](./docs/ai-context/DeviceGateway 设计文档.md) | 网关抽象、生命周期、事件驱动 |
| [协议 SPI 设计](./docs/ai-context/ProtocolSupport 设计文档.md) | 协议扩展、jar 加载 |
| [网络组件设计](./docs/ai-context/Network 组件设计文档.md) | 网络抽象、11 种类型、Vert.x 实现 |
| [会话设计](./docs/ai-context/DeviceSession 设计文档.md) | 三类会话、生命周期、子设备会话 |
| [设备注册中心](./docs/ai-context/device-registry设计文档.md) | Redis 存储结构、注册/查询流程 |
| [EventBus 设计](./docs/ai-context/EventBus设计文档.md) | 动态事件总线、泛型匹配 |
| [CRUD 事件系统](./docs/ai-context/CRUD 事件系统 — 设计文档.md) | MyBatis 拦截器事件发布 |
| [物模型 TSL](./docs/ai-context/物模型TSL后端json解析设计文档.md) | 元数据体系、数据类型编解码 |
| [缓存管理器](./docs/ai-context/自定义缓存管理器设计文档.md) | 缓存抽象、Config 存储 |
| [高保真原型](./docs/ai-context/iot-platform-prototype.html) | 页面原型 |

## License

基于 [SmartAdmin](https://smartadmin.vip) 构建，遵循其开源协议。
