# DeviceSession 设计文档

## 一、会话分层模型

```
                            ┌─────────────────────────┐
                            │   DeviceSessionManager   │  ← 全局会话管理（注册、查找、移除、事件监听）
                            │  (LocalDeviceSessionManager)│
                            └──────────┬──────────────┘
                                       │ 管理
                            ┌──────────▼──────────────┐
                            │     DeviceSession        │  ← 会话接口（抽象）
                            │  (id, deviceId, operator, │
                            │   transport, isAlive,    │
                            │   close, lastPingTime)   │
                            └──────────┬──────────────┘
                                       │ 实现
               ┌───────────────────────┼───────────────────────┐
               │                       │                       │
    ┌──────────▼──────────┐ ┌─────────▼─────────┐ ┌───────────▼───────────┐
    │  连接型会话           │ │  客户端型会话       │ │  无连接型会话           │
    │ (server-side conn)  │ │ (client-side conn) │ │ (stateless/presence)  │
    └─────────────────────┘ └───────────────────┘ └───────────────────────┘
```

## 二、Transport 与会话映射

| Transport | 连接模型 | 服务端角色 | 会话类 | 说明 |
|-----------|---------|-----------|--------|------|
| **MQTT** | 长连接 (TCP) | Broker 侧：接受设备连接 | `MqttConnectionSession` | EMQX/Smart-MQTT broker 创建的连接 |
| **MQTT** | 长连接 (TCP) | Client 侧：连接外部 Broker | `MqttClientSession` | 平台作为 MQTT client 连接 EMQX |
| **TCP** | 长连接 | Server | `TcpDeviceSession` | 自建 TCP server 接受设备连接 |
| **WebSocket** | 长连接 | Server | `WebSocketDeviceSession` | WebSocket 设备连接 |
| **HTTP** | 无连接 | Server | `HttpDeviceSession` | 轻量会话，仅追踪最后活跃时间 |
| **CoAP** | 无连接 (UDP) | Server | `CoapDeviceSession` | 轻量会话，追踪 observe 关系 |

### CoAP 是否需要会话？

**不需要传统意义上的"连接会话"，但需要"设备会话"：**

- CoAP 基于 UDP，**没有持久连接**。每次请求都是独立的 `CON/NON` 消息交换
- 但 IoT 场景下，平台需要：
    1. **设备认证状态缓存** — 避免每次请求都认证（可缓存 token）
    2. **Observe 关系追踪** — 服务器需要记住哪些设备订阅了哪些资源，以便推送通知
    3. **设备在线状态** — 通过最近一次请求时间判断设备是否"活跃"
    4. **消息下行** — 设备下次发起请求时捎带下发（piggyback），而不是实时推送

**CoapDeviceSession 设计：**

```java
/**
 * CoAP 设备会话 — 无连接型，仅追踪设备存在性和 Observe 关系。
 * 不持有 Socket/Channel，无法实时下发消息。
 * 下行消息通过"设备下次请求时捎带"机制投递。
 */
public class CoapDeviceSession implements DeviceSession {

    private final String id;
    private final String deviceId;
    private DeviceOperator operator;
    private long lastPingTime;       // 最近一次请求时间
    private long connectTime;        // 首次认证时间
    private boolean alive = true;

    // CoAP 特有：Observe 订阅列表
    private Set<String> observeResources = ConcurrentHashMap.newKeySet();

    @Override
    public Transport getTransport() {
        return Transports.CoAP;
    }

    @Override
    public boolean isAlive() {
        // 超过 120 秒无请求视为离线（CoAP 典型超时）
        return alive && (System.currentTimeMillis() - lastPingTime) < 120_000;
    }

    @Override
    public void close() {
        alive = false;
        observeResources.clear();
    }

    // ... 其他方法
}
```

## 三、会话生命周期

```
设备首次连接/认证
       │
       ▼
┌─────────────┐   register    ┌─────────────────┐
│  创建 Session │ ────────────► │ DeviceSessionEvent│
│  (未注册)      │              │  Type.register    │
└──────┬──────┘               └─────────────────┘
       │ manager.compute()
       ▼
┌─────────────┐
│   已注册      │  ← isAlive() = true
│   Session    │
└──────┬──────┘
       │ 心跳 / ping
       ▼
┌─────────────┐
│  更新心跳时间  │  ← lastPingTime = now
└──────┬──────┘
       │ 断开 / 超时
       ▼
┌─────────────┐   unregister  ┌─────────────────┐
│  移除 Session │ ────────────► │ DeviceSessionEvent│
│  close()     │              │  Type.unregister  │
└─────────────┘               └─────────────────┘
```

### 连接型 vs 无连接型生命周期差异

| 事件 | 连接型 (MQTT/TCP/WS) | 无连接型 (HTTP/CoAP) |
|------|---------------------|---------------------|
| 创建 | 连接建立时 | 首次认证请求时 |
| 心跳 | keep-alive / PINGREQ | 每次请求都更新 lastPingTime |
| 超时 | keep-alive 超时 | 120s 无请求 |
| 关闭 | 连接断开 | 主动注销或超时 |
| 下行消息 | 直接写入 Channel | 下次请求时捎带（CoAP piggyback）或等待设备轮询（HTTP push） |

## 四、核心接口

### DeviceSessionManager

```java
public interface DeviceSessionManager {

    /** 计算型获取或创建会话（原子操作） */
    Mono<DeviceSession> compute(@Nonnull String deviceId,
                               @Nonnull Function<Mono<DeviceSession>, Mono<DeviceSession>> computer);

    /** 获取会话，不存在返回 empty */
    Mono<DeviceSession> getSession(String deviceId);

    /** 获取会话，可指定失效时是否自动注销 */
    Mono<DeviceSession> getSession(String deviceId, boolean unregisterWhenNotAlive);

    /** 获取所有会话 */
    Flux<DeviceSession> getSessions();

    /** 移除会话，触发 DeviceSessionEvent.unregister */
    Mono<Long> remove(String deviceId);

    /** 条件移除 */
    Mono<Long> remove(String deviceId, Predicate<DeviceSession> predicate);

    /** 会话是否存活 */
    Mono<Boolean> isAlive(String deviceId);

    /** 会话总数 */
    Mono<Long> totalSessions();

    /** 监听会话事件，返回 Disposable 可取消 */
    Disposable listenEvent(Function<DeviceSessionEvent, Mono<Void>> handler);
}
```

### DeviceSession

```java
public interface DeviceSession {

    String getId();                    // 会话ID
    String getDeviceId();              // 设备ID

    @Nullable
    DeviceOperator getOperator();      // 设备操作对象（TCP首次连接时可能为null）

    long lastPingTime();               // 最近心跳/活跃时间
    long connectTime();                // 会话创建时间
    Transport getTransport();          // 传输协议
    void close();                      // 关闭会话
    boolean isAlive();                 // 是否存活

    default Mono<Boolean> isAliveAsync() {
        return Mono.fromSupplier(this::isAlive);
    }
}
```

### DeviceSessionEvent

```java
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DeviceSessionEvent {
    private long timestamp;
    private Type type;
    private DeviceSession session;

    public static DeviceSessionEvent of(Type type, DeviceSession session) {
        return of(System.currentTimeMillis(), type, session);
    }

    public enum Type {
        register,    // 会话注册
        unregister   // 会话注销
    }
}
```

## 五、实现类结构

```
net.lab1024.sa.base.device.session          (接口 + 通用实现)
  ├── DeviceSession.java
  ├── DeviceSessionManager.java
  ├── DeviceSessionEvent.java
  │
  └── support/
        ├── LocalDeviceSessionManager.java
        ├── AbstractDeviceSession.java
        └── ChildrenDeviceSession.java

net.lab1024.sa.base.module.support.protocol (协议支撑层 — 按协议分包)
  ├── mqtt/
  │     ├── session/
  │     │     ├── MqttConnectionSession.java
  │     │     └── MqttClientSession.java
  │     └── (后续：MQTT 网络组件、编解码等)
  ├── tcp/
  │     ├── session/
  │     │     └── TcpDeviceSession.java
  │     └── (后续：TCP Server、编解码等)
  ├── websocket/
  │     ├── session/
  │     │     └── WebSocketDeviceSession.java
  │     └── (后续：WebSocket 组件等)
  ├── http/
  │     ├── session/
  │     │     └── HttpDeviceSession.java
  │     └── (后续：HTTP 组件等)
  └── coap/
        ├── session/
        │     └── CoapDeviceSession.java
        └── (后续：CoAP 网络组件等)
```

### 抽象基类设计

```java
/**
 * 设备会话抽象基类 — 提供公共字段和默认实现。
 * 连接型会话持有 Channel/Connection，无连接型仅追踪时间戳。
 */
public abstract class AbstractDeviceSession implements DeviceSession {

    protected final String id;
    protected final String deviceId;
    protected final Transport transport;
    protected DeviceOperator operator;
    protected final long connectTime;
    protected volatile long lastPingTime;
    protected volatile boolean closed;

    @Override
    public boolean isAlive() {
        return !closed;
    }

    @Override
    public void close() {
        closed = true;
    }

    // ... getter/setter
}
```

### 各实现类的差异化行为

| 实现类 | 持有的连接对象 | `close()` | `isAlive()` |
|--------|--------------|-----------|-------------|
| `AbstractDeviceSession` | 无 | `closed = true` | `!closed` |
| `MqttConnectionSession` | MqttConnection | 断开 MQTT 连接 | `!closed && channel.isActive()` |
| `MqttClientSession` | MqttClient | 断开 MQTT client | `!closed && client.isConnected()` |
| `TcpDeviceSession` | Channel (Netty) | `channel.close()` | `!closed && channel.isActive()` |
| `WebSocketDeviceSession` | WebSocketSession | `session.close()` | `!closed && session.isOpen()` |
| `HttpDeviceSession` | 无 | `closed = true` | `!closed && 120s 内有请求` |
| `ChildrenDeviceSession` | 父设备会话引用 | 委托父设备 | 委托父设备 |
| `CoapDeviceSession` | 无 | `closed = true` | `!closed && 120s 内有请求` |

## 六、LocalDeviceSessionManager 设计要点

1. **存储结构**：`ConcurrentHashMap<String, DeviceSession>` — deviceId → session
2. **`compute()` 方法**：原子 CAS 操作，并发安全。回调 `computer` 可获取当前会话（可能为 null）并返回新会话
3. **`listenEvent()` 方法**：内部维护 `List<Function<DeviceSessionEvent, Mono<Void>>>` 监听器列表，会话注册/注销时遍历通知
4. **定期清理**：后台线程定时扫描超时会话并移除

### compute() 并发模式

```java
// TCP 设备首次连接：session 不存在 → 创建
manager.compute(deviceId, current -> {
    if (current == null) {
        return Mono.just(new TcpDeviceSession(deviceId, channel));
    }
    // 已存在则更新 channel
    return current.doOnNext(s -> s.updateChannel(channel));
});

// 获取或创建
manager.compute(deviceId, current -> {
    return current != null ? current : createNewSession(deviceId);
});
```

## 七、子设备会话（ChildrenDeviceSession）

网关子设备不直接连接平台，通过网关代理通信：

```java
/**
 * 子设备会话 — 不持有实际网络连接。
 *
 * 关键设计：
 *  - operator：子设备自己的 DeviceOperator，用于访问自身配置、物模型、认证等
 *  - parent：父设备（网关）的会话，子设备通过网关代理通信
 *  - alive 状态取决于父设备会话是否存活
 */
public class ChildrenDeviceSession implements DeviceSession {

    private final String id;
    private final String deviceId;
    private final DeviceSession parent;           // 网关会话（连通性依赖）
    private final DeviceOperator operator;        // 子设备自己的操作对象

    @Override
    public boolean isAlive() {
        return parent != null && parent.isAlive();
    }

    @Override
    public DeviceOperator getOperator() {
        return operator;  // 返回子设备自己的 operator，不是网关的
    }

    // 下行消息：交给网关的 send() 方法，网关负责转发给子设备
}
```

## 八、验证清单

1. MQTT 设备连接 → 创建 `MqttConnectionSession` → `DeviceSessionEvent.register` 事件触发
2. HTTP 设备请求 → 首次创建 `HttpDeviceSession` → 后续请求更新 `lastPingTime`
3. 会话超时 → `isAlive()` 返回 false → `getSession(id, true)` 自动移除
4. 子设备注册 → `ChildrenDeviceSession` → alive 跟随网关
5. CoAP Observe → `observeResources` 记录订阅 → `close()` 清理
