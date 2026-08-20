# MqttDeviceMessageCodec 解码方案设计

## Context

`MqttDeviceMessageCodec` 目前是空实现（decode 返回 `Mono.empty()`）。需要根据 `MQTT协议文档.md` 的 Topic 规范和 `平台统一设备消息定义文档.md` 的消息类型定义，实现 EncodedMessage（MQTT 报文）→ 平台统一 Message 的解码。

## 1. 解码流程

```
MqttEncodedMessage (topic + payload)
  → 1. Topic 解析（分段匹配）
  → 2. 判断消息类型（直连/子设备/回复/事件/生命周期）
  → 3. JSON payload 解析
  → 4. 构建对应 Message 实例
  → 5. 补全字段（deviceKey 优先取 topic 段，messageId 取 payload，timestamp 取 payload）
```

---

## 2. 消息类层级（新建，sa-base `common/message/` 包）

### 2.1 接口层

```java
// MessageReply — 回复消息标记
public interface MessageReply extends Message {
    boolean isSuccess();
    @Nullable String getCode();
    @Nullable String getMessage();
}

// RepayableMessage — 可回复消息（下行），newReply() 生成回复骨架
public interface RepayableMessage<R extends MessageReply> extends Message {
    R newReply();
}

// DeviceMessageReply
public interface DeviceMessageReply extends DeviceMessage, MessageReply {}

// RepayableDeviceMessage
public interface RepayableDeviceMessage<R extends DeviceMessageReply>
        extends DeviceMessage, RepayableMessage<R> {}
```

### 2.2 AbstractDeviceMessage（基类）

```java
@Getter @Setter
public abstract class AbstractDeviceMessage implements DeviceMessage {
    private String deviceKey;
    private String messageId;
    private long timestamp = System.currentTimeMillis();
    private Map<String, Object> headers;

    @Override
    public DeviceMessage addHeader(String header, Object value) {
        if (headers == null) headers = new HashMap<>();
        headers.put(header, value);
        return this;
    }
    // addHeaderIfAbsent / computeHeader / removeHeader / getHeaderOrElse 同上模式
}
```

### 2.3 具体消息类清单

| 类名 | 继承/实现 | 特有字段 |
|------|-----------|----------|
| `ReadPropertyMessage` | AbstractDeviceMessage, RepayableDeviceMessage<ReadPropertyMessageReply> | `List<String> properties` |
| `ReadPropertyMessageReply` | AbstractDeviceMessage, DeviceMessageReply | `boolean success`, `Map<String,Object> properties` |
| `WritePropertyMessage` | AbstractDeviceMessage, RepayableDeviceMessage<WritePropertyMessageReply> | `Map<String,Object> properties` |
| `WritePropertyMessageReply` | AbstractDeviceMessage, DeviceMessageReply | `boolean success`, `Map<String,Object> properties` |
| `ReportPropertyMessage` | AbstractDeviceMessage | `Map<String,Object> properties` |
| `FunctionInvokeMessage` | AbstractDeviceMessage, RepayableDeviceMessage<FunctionInvokeMessageReply> | `String functionId`, `List<FunctionParameter> inputs` |
| `FunctionParameter` | 简单 POJO | `String name`, `Object value` |
| `FunctionInvokeMessageReply` | AbstractDeviceMessage, DeviceMessageReply | `boolean success`, `Object output` |
| `EventMessage` | AbstractDeviceMessage | `String event`, `Object data`（HashMap） |
| `DeviceOnlineMessage` | AbstractDeviceMessage | 无 |
| `DeviceOfflineMessage` | AbstractDeviceMessage | 无 |
| `DeviceRegisterMessage` | AbstractDeviceMessage | 无（productId/deviceName 存 header） |
| `DeviceUnRegisterMessage` | AbstractDeviceMessage | 无 |
| `DisconnectDeviceMessage` | AbstractDeviceMessage, RepayableDeviceMessage<DisconnectDeviceMessageReply> | 无 |
| `DisconnectDeviceMessageReply` | AbstractDeviceMessage, DeviceMessageReply | `boolean success` |
| `ChildDeviceMessage<T extends Message>` | AbstractDeviceMessage, ResolvableTypeProvider | `String childDeviceKey`, `T childDeviceMessage` |
| `ChildDeviceMessageReply<T extends Message>` | AbstractDeviceMessage, DeviceMessageReply, ResolvableTypeProvider | `String childDeviceKey`, `T childDeviceMessage` |

---

## 3. Topic 路由规则

Topic 分段：`[productKey] [deviceKey] [child|child-reply] [childDeviceId] [type] [action] [reply]`

| Topic 模式（上行） | 消息类 | 说明 |
|--------------------|--------|------|
| `/pk/dk/properties/read/reply` | ReadPropertyMessageReply | 直连读属性回复 |
| `/pk/dk/properties/write/reply` | WritePropertyMessageReply | 直连写属性回复 |
| `/pk/dk/function/invoke/reply` | FunctionInvokeMessageReply | 直连功能回复 |
| `/pk/dk/properties/report` | ReportPropertyMessage | 直连属性上报 |
| `/pk/dk/event/{eventId}` | EventMessage | 直连事件上报，event 取 topic 段 |
| `/pk/dk/register` | DeviceRegisterMessage | 直连注册（header: productId, deviceName） |
| `/pk/dk/unregister` | DeviceUnRegisterMessage | 直连注销 |
| `/pk/dk/online` | DeviceOnlineMessage | 直连上线 |
| `/pk/dk/offline` | DeviceOfflineMessage | 直连离线 |
| `/pk/dk/child/ck/properties/report` | ChildDeviceMessage(ReportPropertyMessage) | 子设备属性上报 |
| `/pk/dk/child/ck/online` | ChildDeviceMessage(DeviceOnlineMessage) | 子设备上线 |
| `/pk/dk/child/ck/offline` | ChildDeviceMessage(DeviceOfflineMessage) | 子设备下线 |
| `/pk/dk/child/ck/register` | ChildDeviceMessage(DeviceRegisterMessage) | 子设备注册 |
| `/pk/dk/child/ck/unregister` | ChildDeviceMessage(DeviceUnRegisterMessage) | 子设备注销 |
| `/pk/dk/child/ck/event/{eventId}` | ChildDeviceMessage(EventMessage) | 子设备事件 |
| `/pk/dk/child-reply/ck/properties/read/reply` | ChildDeviceMessageReply(ReadPropertyMessageReply) | 子设备读属性回复 |
| `/pk/dk/child-reply/ck/properties/write/reply` | ChildDeviceMessageReply(WritePropertyMessageReply) | 子设备写属性回复 |
| `/pk/dk/child-reply/ck/function/invoke/reply` | ChildDeviceMessageReply(FunctionInvokeMessageReply) | 子设备功能回复 |
| `/pk/dk/child-reply/ck/disconnect/reply` | ChildDeviceMessageReply(DisconnectDeviceMessageReply) | 子设备断开回复 |

---

## 4. decode 实现

### 4.1 TopicMessageCodec 枚举 — pattern + 消息类 + Jackson 反序列化

```java
public enum TopicMessageCodec {

    // ===== 直连设备 =====
    reportProperty("/:productKey/:deviceKey/properties/report", ReportPropertyMessage.class, true),
    readProperty("/:productKey/:deviceKey/properties/read", ReadPropertyMessage.class, false),
    writeProperty("/:productKey/:deviceKey/properties/write", WritePropertyMessage.class, false),
    functionInvoke("/:productKey/:deviceKey/function/invoke", FunctionInvokeMessage.class, false),
    disconnect("/:productKey/:deviceKey/disconnect", DisconnectDeviceMessage.class, false),
    readPropertyReply("/:productKey/:deviceKey/properties/read/reply", ReadPropertyMessageReply.class, true),
    writePropertyReply("/:productKey/:deviceKey/properties/write/reply", WritePropertyMessageReply.class, true),
    functionInvokeReply("/:productKey/:deviceKey/function/invoke/reply", FunctionInvokeMessageReply.class, true),
    event("/:productKey/:deviceKey/event/:eventId", EventMessage.class, true) {
        @Override
        DeviceMessage doDecode(ObjectMapper mapper, String[] topic, byte[] payload) {
            EventMessage message = (EventMessage) super.doDecode(mapper, topic, payload);
            message.setEvent(topic[topic.length - 1]);   // eventId 取 topic 最后一段
            return message;
        }
    },
    register("/:productKey/:deviceKey/register", DeviceRegisterMessage.class, true),
    unregister("/:productKey/:deviceKey/unregister", DeviceUnRegisterMessage.class, true),
    online("/:productKey/:deviceKey/online", DeviceOnlineMessage.class, true),
    offline("/:productKey/:deviceKey/offline", DeviceOfflineMessage.class, true),
    disconnectReply("/:productKey/:deviceKey/disconnect/reply", DisconnectDeviceMessageReply.class, true),

    // ===== 子设备消息（网关代理上行）=====
    child("/:productKey/:deviceKey/child/:childProductKey/:childDeviceKey/*", ChildDeviceMessage.class, true) {
        @Override
        DeviceMessage doDecode(ObjectMapper mapper, String[] topic, byte[] payload) {
            // 去掉子设备标识段，重组直连 topic 递归解码内层
            String[] childTopic = Arrays.copyOfRange(topic, 2, topic.length);
            childTopic[0] = "";   // topic 以 / 开头第一位是空
            DeviceMessage childMsg = TopicMessageCodec.decode(mapper, childTopic, payload);
            if (childMsg == null) return null;
            // 创建包装，填充父类字段
            ChildDeviceMessage msg = new ChildDeviceMessage();
            msg.setDeviceKey(topic[1]);                     // 网关 deviceKey
            msg.setChildDeviceId(topic[3]);                // 子设备 ID
            msg.setChildDeviceMessage(childMsg);
            msg.setTimestamp(childMsg.getTimestamp());
            msg.setMessageId(childMsg.getMessageId());
            return msg;
        }
    },

    // ===== 子设备消息回复 =====
    childReply("/:productKey/:deviceKey/child-reply/:childProductKey/:childDeviceKey/*", ChildDeviceMessageReply.class, true) {
        @Override
        DeviceMessage doDecode(ObjectMapper mapper, String[] topic, byte[] payload) {
            String[] childTopic = Arrays.copyOfRange(topic, 2, topic.length);
            childTopic[0] = "";
            DeviceMessage childMsg = TopicMessageCodec.decode(mapper, childTopic, payload);
            if (childMsg == null) return null;
            ChildDeviceMessageReply msg = new ChildDeviceMessageReply();
            msg.setDeviceKey(topic[1]);
            msg.setChildDeviceId(topic[3]);
            msg.setChildDeviceMessage(childMsg);
            msg.setTimestamp(childMsg.getTimestamp());
            msg.setMessageId(childMsg.getMessageId());
            return msg;
        }
    };

    private final String[] pattern;                      // 分段后的 topic 模板
    private final Class<? extends DeviceMessage> type;
    private final boolean upstream;                      // 是否上行（设备 → 平台）

    TopicMessageCodec(String topic, Class<? extends DeviceMessage> type, boolean upstream) {
        this.pattern = topic.split("/");
        this.type = type;
        this.upstream = upstream;
    }

    public boolean isUpstream() { return upstream; }

    public String getTopicPattern() { return String.join("/", pattern); }

    /** 上行 topic 列表 — HTTP 服务启动时遍历注册路由（POST） */
    public static List<String> upstreamTopics() {
        return Arrays.stream(values())
                .filter(t -> t.upstream)
                .map(TopicMessageCodec::getTopicPattern)
                .collect(Collectors.toList());
    }

    /** 入口：topic + payload → 消息 */
    public static DeviceMessage decode(ObjectMapper mapper, String topic, byte[] payload) {
        return decode(mapper, topic.split("/"), payload);
    }

    public static DeviceMessage decode(ObjectMapper mapper, String[] topics, byte[] payload) {
        return fromTopic(topics)
                .map(codec -> codec.doDecode(mapper, topics, payload))
                .orElse(null);
    }

    /** 编码入口：消息 → topic + payload */
    public static TopicPayload encode(ObjectMapper mapper, DeviceMessage message) {
        return fromMessage(message)
                .orElseThrow(() -> new UnsupportedOperationException("不支持的消息: " + message.getClass()))
                .doEncode(mapper, message);
    }

    /** 枚举匹配：pattern 与 topic 逐段比对，* 匹配任意段 */
    static Optional<TopicMessageCodec> fromTopic(String[] topic) {
        for (TopicMessageCodec value : values()) {
            if (TopicUtils.match(value.pattern, topic)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    static Optional<TopicMessageCodec> fromMessage(DeviceMessage message) {
        for (TopicMessageCodec value : values()) {
            if (value.type == message.getClass()) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /** 默认解码：Jackson 反序列化 payload 到消息类，deviceKey 从 topic[1] 覆盖 */
    @SneakyThrows
    DeviceMessage doDecode(ObjectMapper mapper, String[] topic, byte[] payload) {
        DeviceMessage message = mapper.readValue(payload, type);
        message.setDeviceKey(topic[1]);
        return message;
    }

    @SneakyThrows
    TopicPayload doEncode(ObjectMapper mapper, DeviceMessage message) {
        String[] topics = Arrays.copyOf(pattern, pattern.length);
        refactorTopic(topics, message);
        return TopicPayload.of(String.join("/", topics), mapper.writeValueAsBytes(message));
    }

    void refactorTopic(String[] topics, DeviceMessage message) {
        topics[1] = message.getDeviceKey();
    }
}
```

### 4.2 TopicUtils — `:param` 通配符逐段匹配工具

```java
public final class TopicUtils {

    /** pattern 与 topic 逐段比对：
     *  - ":xxx" 段匹配任意值（命名参数，Vert.x 原生语法）
     *  - "*" 段（仅尾部）匹配剩余全部路径
     */
    public static boolean match(String[] pattern, String[] topic) {
        if (pattern.length > topic.length) return false;
        for (int i = 0; i < pattern.length; i++) {
            String p = pattern[i];
            if (p.startsWith(":")) continue;
            if ("*".equals(p)) return true;    // 尾部通配：剩余全部匹配
            if (!p.equals(topic[i])) return false;
        }
        return pattern.length == topic.length;  // 段数必须一致（无 * 时）
    }
}
```

**匹配示例**：

| pattern | topic | 结果 |
|---------|-------|------|
| `/:productKey/:deviceKey/properties/report` | `/p1/d1/properties/report` | ✓（段数一致） |
| `/:productKey/:deviceKey/event/:eventId` | `/p1/d1/event/tempAlarm` | ✓ |
| `/:productKey/:deviceKey/child/:childProductKey/:childDeviceKey/*` | `/p1/d1/child/c1/properties/report` | ✓（尾部 `*` 匹配 `properties/report` 两段） |
| `/:productKey/:deviceKey/child/:childProductKey/:childDeviceKey/*` | `/p1/d1/child/c1/online` | ✓（尾部 `*` 匹配 `online` 一段） |

### 4.3 MqttDeviceMessageCodec — 薄封装

```java
public class MqttDeviceMessageCodec implements DeviceMessageCodec {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Publisher<? extends Message> decode(MessageDecodeContext context) {
        return Mono.fromCallable(() -> {
            MqttEncodedMessage encoded = (MqttEncodedMessage) context.getMessage();
            return TopicMessageCodec.decode(mapper, encoded.getTopic(), encoded.payloadAsBytes());
        }).filter(Objects::nonNull);
    }

    @Override
    public Publisher<? extends EncodedMessage> encode(MessageEncodeContext context) {
        return Mono.fromCallable(() -> {
            DeviceMessage message = (DeviceMessage) context.getMessage();
            TopicPayload payload = TopicMessageCodec.encode(mapper, message);
            return new DefaultMqttEncodedMessage(...payload...);
        });
    }
}
```

**设计要点**：
1. **枚举即全部映射** — pattern（`"/*/properties/report"`）+ 消息类 + `upstream` 标志，`*` 通配符逐段匹配，无 if/switch
2. **Jackson 自动反序列化** — payload JSON 字段名与消息类属性一致，默认 `doDecode` 一行反序列化 + deviceId 覆盖；无需手写 fill 逻辑
3. **子设备递归解码** — `child`/`childReply` 条目覆写 `doDecode`：去掉 `child/{childDeviceId}` 段重组直连 topic → 递归 `decode()` → 包装填父字段，与用户要求完全一致
4. **双向** — 同一枚举同时承担 decode（topic→消息）和 encode（消息→topic+payload），`refactorTopic` 把 deviceId 回填到 topic 模板
5. **枚举即路由注册表** — 每个条目带 `upstream` 标志；HTTP 服务启动时遍历 `upstreamTopics()` 直接注册 POST 路由，URL path 即 topic；MQTT 网关订阅时同样遍历上行 topic。新增消息类型只需加一条枚举，路由自动生成

### 4.4 HTTP 服务路由注册（示例）

```java
// HTTP 网关启动时 — 枚举即路由表，:param 为 Vert.x 原生语法，直接注册零转换
for (String topic : TopicMessageCodec.upstreamTopics()) {
    router.post(topic).handler(ctx -> {
        DeviceMessage msg = TopicMessageCodec.decode(mapper, ctx.request().path(), ctx.getBody().getBytes());
        if (msg == null) {
            ctx.response().setStatusCode(404).end("unsupported topic");
            return;
        }
        messageHandler.handle(msg);
        ctx.response().end();
    });
}
```

**统一原则**：`:param` 命名参数 pattern 是所有传输的**唯一**匹配语法：
- **HTTP**：Vert.x `router.post(topic)` 原生支持 `:param`，直接注册
- **MQTT**：同一 pattern 用于订阅和 `TopicUtils.match()` 匹配（`:` 开头段视为通配符）
- **CoAP**：URI path 即 topic，同一 `TopicMessageCodec.decode()` 入口

新增消息类型只需加一条枚举，HTTP 路由、MQTT 订阅、编解码全部自动生效。

## 5. 子设备消息的事件总线泛型匹配（关键设计）

**复用项目已有模式**：`CrudEvent<T>` 实现 `ResolvableTypeProvider`（`common/event/CrudEvent.java`），`SpringEventBus` 通过 `ResolvableTypeUtil.resolve(event)` 提取运行时泛型类型，`SubscriptionEntry.matches()` 按泛型精准匹配。`ChildDeviceMessage` 同样实现：

```java
public class ChildDeviceMessage<T extends Message> extends AbstractDeviceMessage
        implements ResolvableTypeProvider {

    private String childDeviceId;      // 子设备 ID
    private T childDeviceMessage;      // 内层消息

    // getDeviceKey() 返回网关 deviceKey（topic 第 2 段），保留父设备信息

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                ChildDeviceMessage.class,
                ResolvableType.forClass(childDeviceMessage.getClass()));
    }
}
```

**业务侧订阅方式**（与 `EventHandler<SaveAfterEvent<DeviceEntity>>` 同款）：

| 订阅 | 收到 |
|------|------|
| `new EventHandler<ChildDeviceMessage<EventMessage>>()` | 仅子设备事件上报（泛型精准匹配） |
| `new EventHandler<ChildDeviceMessage>()` | 所有子设备消息（无泛型 → 全匹配） |
| `new EventHandler<EventMessage>()` | 仅直连设备事件（子设备事件是 ChildDeviceMessage，rawType 不匹配，天然隔离） |
| `new EventHandler<DeviceMessage>()` | 所有设备消息 |

**优势**：
- 网关信息保留在 `ChildDeviceMessage.getDeviceKey()`（网关 ID）+ `getChildDeviceId()`（子设备 ID）
- 可精准监听子设备的某一类消息，也可监听全部子设备消息
- 直连设备监听 `EventMessage` 天然不受子设备事件干扰
- 无需在 Handler 层解包，`DefaultDecodedClientMessageHandler` 原样发布即可

---

## 6. 涉及文件

| 文件 | 操作 |
|------|------|
| `common/message/MessageReply.java` | **新建** — 回复消息标记接口 |
| `common/message/RepayableMessage.java` | **新建** — 可回复消息接口 |
| `common/message/DeviceMessageReply.java` | **新建** |
| `common/message/RepayableDeviceMessage.java` | **新建** |
| `common/message/AbstractDeviceMessage.java` | **新建** — 基类（headers/deviceId/messageId/timestamp） |
| `common/message/` 下 15+ 个具体消息类 | **新建** — 见 2.3 清单 |
| `common/topic/TopicMessageCodec.java` | **新建** — 枚举：pattern + 消息类 + doDecode/doEncode |
| `common/topic/TopicUtils.java` | **新建** — `:param` 通配符逐段匹配工具 |
| `common/topic/TopicPayload.java` | **新建** — topic + payload 载体 |
| `sa-protocol/.../MqttDeviceMessageCodec.java` | **重写** — 薄封装：decode/encode 均调枚举入口 |

## 6. 验证方案

1. 单元测试：构造各 Topic 模式的 MqttEncodedMessage，验证解码出的消息类型、字段值
2. 子设备消息：验证 ChildDeviceMessage 解包后 childDeviceMessage 类型正确
3. 集成：MQTT 客户端 publish 到各 topic，观察事件总线收到的 Message 类型
