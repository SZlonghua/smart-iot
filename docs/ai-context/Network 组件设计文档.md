# Network 组件设计文档

## 一、设计动机

IoT 平台需要管理各种底层网络组件——TCP Server/Client、MQTT Broker/Client、HTTP Server/Client、WebSocket、UDP、CoAP 等。`Network` 组件是对"网络通道"的统一抽象，屏蔽底层协议差异，为上层 `DeviceGateway` 提供可复用的网络能力。

## 二、架构概览

```
┌───────────────────────────────────────────────┐
│               NetworkManager                   │  ← 运行时管理（get/reload/shutdown/destroy）
└──────────────────┬────────────────────────────┘
                   │ 管理
┌──────────────────▼────────────────────────────┐
│                 Network                        │  ← 网络组件运行时
│  (id, type, isAlive, isAutoReload, shutdown)  │
│     ├── ServerNetwork (marker)                 │
│     └── ClientNetwork (marker)                 │
└──────────────────┬────────────────────────────┘
                   │ 创建
┌──────────────────▼────────────────────────────┐
│             NetworkProvider                    │  ← SPI 工厂
│  (type, createNetwork, reload, createConfig)  │
└──────────────────┬────────────────────────────┘
                   │ 配置
     ┌─────────────┴─────────────┐
     │                           │
┌────▼──────────┐        ┌───────▼──────────┐
│ NetworkConfig │        │ NetworkProperties │
│ (运行时接口)    │        │ (可序列化 POJO)    │
└───────────────┘        └──────────────────┘

辅助:
- NetworkType        ← 类型枚举 (TCP_CLIENT, MQTT_SERVER, ...)
- NetworkTypes       ← 类型注册中心
- NetworkTransport   ← 传输层枚举 (TCP / UDP)
- NetworkProviders   ← Provider 注册中心
```

## 三、包结构

```
net.lab1024.sa.base.common.network
  ├── Network.java                (接口：网络组件运行时)
  ├── ServerNetwork.java          (marker：服务端网络)
  ├── ClientNetwork.java          (marker：客户端网络)
  ├── NetworkType.java            (接口：类型标识)
  ├── DefaultNetworkType.java     (枚举：11种内置类型)
  ├── NetworkTypes.java           (类：类型注册中心)
  ├── NetworkTransport.java       (枚举：TCP/UDP)
  ├── NetworkConfig.java          (接口：运行时配置)
  ├── ServerNetworkConfig.java    (marker：服务端配置)
  ├── ClientNetworkConfig.java    (marker：客户端配置)
  ├── NetworkProvider.java        (接口：SPI 工厂)
  ├── NetworkProviders.java       (接口：Provider 注册中心)
  ├── NetworkManager.java         (接口：运行时管理)
  └── NetworkProperties.java      (POJO：可序列化配置)
```

## 四、核心接口

### 4.1 Network — 网络组件运行时

```java
public interface Network {
    String getId();                          // 组件唯一ID
    NetworkType getType();                   // 网络类型
    void shutdown();                         // 关闭
    boolean isAlive();                       // 是否存活
    boolean isAutoReload();                  // 失效时是否自动重载
}
```

**标记接口：**

```java
public interface ServerNetwork extends Network { }   // 服务端网络 (TCP Server, MQTT Broker...)
public interface ClientNetwork extends Network { }   // 客户端网络 (TCP Client, MQTT Client...)
```

### 4.2 NetworkType — 类型标识

```java
public interface NetworkType {
    String getId();                    // 类型唯一标识
    default String getName() { return getId(); }

    static NetworkType of(String id) { return () -> id; }
    static List<NetworkType> getAll() { return NetworkTypes.get(); }
    static Optional<NetworkType> lookup(String id) { return NetworkTypes.lookup(id); }
}
```

**`DefaultNetworkType` 枚举 — 11种内置类型：**

| 常量 | 中文名 | 角色 |
|------|-------|------|
| `TCP_CLIENT` | TCP客户端 | Client |
| `TCP_SERVER` | TCP服务 | Server |
| `MQTT_CLIENT` | MQTT客户端 | Client |
| `MQTT_SERVER` | MQTT服务 | Server |
| `HTTP_CLIENT` | HTTP客户端 | Client |
| `HTTP_SERVER` | HTTP服务 | Server |
| `WEB_SOCKET_CLIENT` | WebSocket客户端 | Client |
| `WEB_SOCKET_SERVER` | WebSocket服务 | Server |
| `UDP` | UDP | — |
| `COAP_CLIENT` | CoAP客户端 | Client |
| `COAP_SERVER` | CoAP服务 | Server |

静态初始化块自动注册到 `NetworkTypes`。

### 4.3 NetworkTransport — 传输层

```java
public enum NetworkTransport { TCP, UDP }
```

### 4.4 NetworkConfig — 运行时配置

```java
public interface NetworkConfig {
    String getId();                         // 配置ID
    NetworkTransport getTransport();         // TCP or UDP
    String getSchema();                     // 传输模式：http, mqtt, ws...
}
```

**标记接口：**
```java
public interface ServerNetworkConfig extends NetworkConfig { }
public interface ClientNetworkConfig extends NetworkConfig { }
```

### 4.5 NetworkProvider — SPI 工厂

```java
public interface NetworkProvider {
    @Nonnull NetworkType getType();
    @Nonnull Mono<Network> createNetwork(@Nonnull NetworkConfig config);
    Mono<Network> reload(@Nonnull Network network, @Nonnull NetworkConfig config);
    @Nonnull Mono<NetworkConfig> createConfig(@Nonnull NetworkProperties properties);
}
```

- `createConfig(properties)`：将可序列化的 `NetworkProperties` 转换为运行时 `NetworkConfig`
- `createNetwork(config)`：根据配置创建网络组件实例
- `reload(network, config)`：重载已有网络组件（默认可能关闭旧实例再创建新实例）

### 4.6 NetworkManager — 运行时管理

```java
public interface NetworkManager {
    <T extends Network> Mono<T> getNetwork(NetworkType type, String id);
    Flux<Network> getNetworks();
    Mono<Void> reload(NetworkType type, String id);
    Mono<Void> shutdown(NetworkType type, String id);
    Mono<Void> destroy(NetworkType type, String id);
}
```

- `shutdown` vs `destroy`：shutdown 关闭网络但不移除记录，destroy 彻底销毁并移除

### 4.7 NetworkProviders — Provider 注册中心

```java
public interface NetworkProviders {
    Mono<NetworkProvider> getProvider(String provider);
    Flux<NetworkProvider> getProviders();
    Mono<Void> addNetworkProvider(NetworkProvider provider);
}
```

### 4.8 NetworkProperties — 可序列化配置 POJO

```java
@Getter @Setter
public class NetworkProperties implements Serializable {
    private String id;                      // 配置ID
    private String type;                    // NetworkType.getId()
    private String name;                    // 配置名称
    private boolean enabled;                // 是否启用
    private Map<String, Object> configurations;  // 配置内容（不同组件不同结构）

    public Map<String, Object> values() { return configurations; }
}
```

`NetworkProperties` 是持久化层到运行时层的桥梁：

```
DB/配置 → NetworkProperties → NetworkProvider.createConfig() → NetworkConfig → NetworkProvider.createNetwork() → Network
```

### 4.9 NetworkConfigManager — 配置管理

```java
public interface NetworkConfigManager {

    Mono<NetworkProperties> getConfig(@Nullable NetworkType networkType,
                                      @Nonnull String id);

    Flux<NetworkProperties> getAllConfigs();
}
```

**实现类：`NetworkConfigManagerImpl`** — 位于 `sa-admin` 模块的 `networkcomponent.service` 包，从 `network_component` 表读取配置，将 `NetworkComponentEntity` 转换为 `NetworkProperties`：

| NetworkComponentEntity 字段 | → | NetworkProperties 字段 |
|---------------------------|---|------------------------|
| `id` | → | `id` (String.valueOf) |
| `name` | → | `name` |
| `type` | → | `type` |
| `status` | → | `enabled` (status == 1) |
| `configuration` (JSON) | → | `configurations` (Map) |
| `description` | → | (暂不映射) |

```
NetworkComponentEntity (数据库)
    → NetworkConfigManager.getConfig/load() 
    → NetworkProperties 
    → NetworkProvider.createConfig(properties) 
    → NetworkConfig 
    → NetworkProvider.createNetwork(config) 
    → Network
```

## 五、网络协议实现

### 5.1 Network 接口与实现

每种网络类型定义为**接口**，底层框架实现可替换。当前使用 **Vert.x**，后续可切换到 Netty、Reactor Netty 等，只需新增实现类并在 Provider 中切换即可。

```
Network (接口)
  ├── ServerNetwork (marker接口)
  │     ├── TcpServerNetwork (接口)
  │     │     └── VertxTcpServerNetwork (Vert.x实现)
  │     ├── MqttServerNetwork (接口)
  │     │     └── VertxMqttServerNetwork
  │     ├── HttpServerNetwork (接口)
  │     │     └── VertxHttpServerNetwork
  │     ├── WebSocketServerNetwork (接口)
  │     │     └── VertxWebSocketServerNetwork
  │     └── CoapServerNetwork (接口)
  │           └── VertxCoapServerNetwork
  │
  └── ClientNetwork (marker接口)
        ├── TcpClientNetwork (接口)
        │     └── VertxTcpClientNetwork
        ├── MqttClientNetwork (接口)
        │     └── VertxMqttClientNetwork
        ├── HttpClientNetwork (接口)
        │     └── VertxHttpClientNetwork
        ├── WebSocketClientNetwork (接口)
        │     └── VertxWebSocketClientNetwork
        └── CoapClientNetwork (接口)
              └── VertxCoapClientNetwork

Network (接口)
  └── UdpNetwork (接口)
        └── VertxUdpNetwork
```

| 接口 | 对应 NetworkType | 标记 | Vert.x 实现 |
|------|-----------------|------|-----------|
| `TcpServerNetwork` | TCP_SERVER | `ServerNetwork` | `VertxTcpServerNetwork` |
| `TcpClientNetwork` | TCP_CLIENT | `ClientNetwork` | `VertxTcpClientNetwork` |
| `MqttServerNetwork` | MQTT_SERVER | `ServerNetwork` | `VertxMqttServerNetwork` |
| `MqttClientNetwork` | MQTT_CLIENT | `ClientNetwork` | `VertxMqttClientNetwork` |
| `HttpServerNetwork` | HTTP_SERVER | `ServerNetwork` | `VertxHttpServerNetwork` |
| `HttpClientNetwork` | HTTP_CLIENT | `ClientNetwork` | `VertxHttpClientNetwork` |
| `WebSocketServerNetwork` | WEB_SOCKET_SERVER | `ServerNetwork` | `VertxWebSocketServerNetwork` |
| `WebSocketClientNetwork` | WEB_SOCKET_CLIENT | `ClientNetwork` | `VertxWebSocketClientNetwork` |
| `UdpNetwork` | UDP | `Network` | `VertxUdpNetwork` |
| `CoapServerNetwork` | COAP_SERVER | `ServerNetwork` | `VertxCoapServerNetwork` |
| `CoapClientNetwork` | COAP_CLIENT | `ClientNetwork` | `VertxCoapClientNetwork` |

### 5.2 NetworkProvider 实现类

每个接口对应一个 Provider，当前使用 Vert.x 实现：

| Provider | 创建的接口 | 实际实例 |
|----------|-----------|---------|
| `TcpServerNetworkProvider` | `TcpServerNetwork` | `VertxTcpServerNetwork` |
| `TcpClientNetworkProvider` | `TcpClientNetwork` | `VertxTcpClientNetwork` |
| `MqttServerNetworkProvider` | `MqttServerNetwork` | `VertxMqttServerNetwork` |
| `MqttClientNetworkProvider` | `MqttClientNetwork` | `VertxMqttClientNetwork` |
| `HttpServerNetworkProvider` | `HttpServerNetwork` | `VertxHttpServerNetwork` |
| `HttpClientNetworkProvider` | `HttpClientNetwork` | `VertxHttpClientNetwork` |
| `WebSocketServerNetworkProvider` | `WebSocketServerNetwork` | `VertxWebSocketServerNetwork` |
| `WebSocketClientNetworkProvider` | `WebSocketClientNetwork` | `VertxWebSocketClientNetwork` |
| `UdpNetworkProvider` | `UdpNetwork` | `VertxUdpNetwork` |
| `CoapServerNetworkProvider` | `CoapServerNetwork` | `VertxCoapServerNetwork` |
| `CoapClientNetworkProvider` | `CoapClientNetwork` | `VertxCoapClientNetwork` |

Provider 内部决定使用哪个底层实现。切换实现框架只需修改 Provider 的 `createNetwork()` 方法。

### 5.3 包结构

```
net.lab1024.sa.base.module.support.protocol
  ├── tcp/
  │     ├── network/
  │     │     ├── TcpServerNetwork.java              (接口)
  │     │     ├── TcpClientNetwork.java              (接口)
  │     │     ├── VertxTcpServerNetwork.java         (Vert.x实现)
  │     │     ├── VertxTcpClientNetwork.java
  │     │     ├── TcpServerNetworkProvider.java
  │     │     └── TcpClientNetworkProvider.java
  │     ├── gateway/
  │     └── session/
  ├── mqtt/  {network/, gateway/, session/}     (同上模式：接口+Vert.x实现+Provider)
  ├── http/  {network/, gateway/, session/}     (同上)
  ├── websocket/ {network/, gateway/, session/} (同上)
  ├── udp/   {network/, gateway/, session/}     (同上)
  └── coap/  {network/, gateway/, session/}     (同上)
```

### 5.4 DeviceGateway 如何引用 Network

```
DeviceGateway (接入层)
    │
    ├── TcpDeviceGateway
    │       └── NetworkManager.getNetwork(TCP_SERVER, "xxx") → TcpServerNetwork
    │
    ├── MqttClientDeviceGateway
    │       └── NetworkManager.getNetwork(MQTT_CLIENT, "xxx") → MqttClientNetwork
    │
    └── MqttServerDeviceGateway
            └── NetworkManager.getNetwork(MQTT_SERVER, "xxx") → MqttServerNetwork
```

`DeviceGatewayProperties.componentId` 存储的是 `Network` 的 ID。`DeviceGateway` 启动时通过 `NetworkManager.getNetwork(type, componentId)` 获取底层网络通道，然后在其上构建设备接入逻辑。

## 六、Client/Server 区分

网络组件天然有客户端和服务端两种角色，通过标记接口区分：

| 角色 | Network 标记 | Config 标记 | 典型实例 |
|------|-------------|------------|---------|
| 服务端 | `ServerNetwork` | `ServerNetworkConfig` | TCP Server 监听端口，接受设备连接 |
| 客户端 | `ClientNetwork` | `ClientNetworkConfig` | MQTT Client 连接外部 Broker |

## 七、与 DeviceGateway 的关系

`Network` 位于 `DeviceGateway` 之下：

```
DeviceGateway（接入层：消息流、认证、会话）
    └── 依赖
Network（网络层：TCP Server、MQTT Client、HTTP Endpoint）
```

- `Network` 负责**网络通道的建立和生命周期管理**（bind/connect、shutdown、health check）
- `DeviceGateway` 负责**设备接入逻辑**（消息编解码、认证、会话管理、物模型交互）
- `DeviceGatewayProvider.createDeviceGateway()` 内部通过 `NetworkManager.getNetwork()` 获取底层网络组件

## 八、NetworkConfigEvent 事件系统

### 8.1 NetworkConfigEvent

```java
@Getter @AllArgsConstructor(staticName = "of")
public class NetworkConfigEvent {
    private final Type type;
    private final NetworkProperties properties;

    public enum Type { reload, destroy }
}
```

| 事件 | 触发条件 | 处理 |
|------|---------|------|
| `reload` | 新增 / 修改（status=1） | `NetworkManager.reload(type, id)` |
| `destroy` | 删除 / 修改（status=0） | `NetworkManager.destroy(type, id)` |

### 8.2 NetworkComponentEventHandler（sa-admin 模块）

位于 `sa-admin` 的 `networkcomponent.handler` 包，监听 `NetworkComponentEntity` CRUD 事件：

```java
@Component
public class NetworkComponentEventHandler {

    @EventListener
    public void onSaved(SaveAfterEvent<NetworkComponentEntity> event) {
        NetworkComponentEntity entity = event.getAfterData();
        if (entity == null || !entity.isEnabled()) return;
        eventBus.publish(NetworkConfigEvent.of(reload, entity.toProperties()));
    }

    @EventListener
    public void onUpdated(UpdateAfterEvent<NetworkComponentEntity> event) {
        NetworkComponentEntity entity = event.getAfterData();
        if (entity == null) return;
        Type type = entity.isEnabled() ? reload : destroy;
        eventBus.publish(NetworkConfigEvent.of(type, entity.toProperties()));
    }

    /** 删除前 → 校验是否有网关引用此组件 */
    @EventListener
    public void onBeforeDelete(DeleteBeforeEvent<NetworkComponentEntity> event) {
        // 查询引用此组件的网关数量
        long refCount = gatewayDao.countByComponentId(event.getEntityId());
        if (refCount > 0) {
            throw new BusinessException("该网络组件被 " + refCount + " 个网关引用，无法删除");
        }
    }

    /** 删除后 → destroy */
    @EventListener
    public void onDeleted(DeleteAfterEvent<NetworkComponentEntity> event) {
        NetworkProperties props = new NetworkProperties();
        props.setId(String.valueOf(event.getEntityId()));
        eventBus.publish(NetworkConfigEvent.of(destroy, props));
    }
}
```

### 8.3 NetworkManager.init() 监听事件

```java
public void init() {
    eventBus.subscribe(new EventHandler<NetworkConfigEvent>() {
        @Override
        public void handle(NetworkConfigEvent event) {
            NetworkProperties props = event.getProperties();
            NetworkType type = NetworkType.of(props.getType());
            switch (event.getType()) {
                case reload:
                    reload(type, props.getId()).subscribe();
                    break;
                case destroy:
                    destroy(type, props.getId()).subscribe();
                    break;
            }
        }
    });
}
```

## 九、典型流程

```
1. 前端配置网络组件 → 保存 NetworkProperties 到 DB
2. NetworkEventHandler 监听到 NetworkEntity 保存事件 → 发布事件
3. NetworkManager 收到事件 → NetworkProvider.createConfig(properties) → NetworkConfig
4. NetworkProvider.createNetwork(config) → Network
5. NetworkManager 持有 Network 实例，提供 getNetwork / shutdown / reload
6. DeviceGateway 创建时通过 NetworkManager.getNetwork() 获取底层网络通道
```

## 十、验证清单

1. `DefaultNetworkType` 静态初始化注册 → `NetworkTypes.get()` 返回 11 种类型
2. `NetworkType.lookup("TCP服务")` → 返回 `DefaultNetworkType.TCP_SERVER`
3. 实现 `NetworkProvider` → 注册到 `NetworkProviders` → `getProvider(id)` 可获取
4. `createConfig(properties)` → `NetworkConfig`，`createNetwork(config)` → `Network`
5. `NetworkManager.getNetwork(type, id)` → 返回已创建的网络组件
6. `shutdown()` → `isAlive()=false`，`isAutoReload()` 控制是否自动重建
7. `ServerNetwork` / `ClientNetwork` marker 接口用于类型检查
