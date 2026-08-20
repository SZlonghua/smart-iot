package net.lab1024.sa.base.common.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import net.lab1024.sa.base.common.message.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Topic → 消息类型映射枚举
 * <p>
 * pattern 使用 `:param` 命名参数语法（Vert.x 原生支持），
 * 同一枚举同时承担 decode（topic→消息）和 encode（消息→topic+payload）。
 * HTTP 服务启动时遍历 {@link #upstreamTopics()} 直接注册 POST 路由。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
public enum TopicMessageCodec {

    // ===== 直连设备（上行）=====
    reportProperty("/:productKey/:deviceKey/properties/report", ReportPropertyMessage.class, true),
    readPropertyReply("/:productKey/:deviceKey/properties/read/reply", ReadPropertyMessageReply.class, true),
    writePropertyReply("/:productKey/:deviceKey/properties/write/reply", WritePropertyMessageReply.class, true),
    functionInvokeReply("/:productKey/:deviceKey/function/invoke/reply", FunctionInvokeMessageReply.class, true),
    register("/:productKey/:deviceKey/register", DeviceRegisterMessage.class, true),
    unregister("/:productKey/:deviceKey/unregister", DeviceUnRegisterMessage.class, true),
    online("/:productKey/:deviceKey/online", DeviceOnlineMessage.class, true),
    offline("/:productKey/:deviceKey/offline", DeviceOfflineMessage.class, true),
    disconnectReply("/:productKey/:deviceKey/disconnect/reply", DisconnectDeviceMessageReply.class, true),

    // ===== 直连设备（下行）=====
    readProperty("/:productKey/:deviceKey/properties/read", ReadPropertyMessage.class, false),
    writeProperty("/:productKey/:deviceKey/properties/write", WritePropertyMessage.class, false),
    functionInvoke("/:productKey/:deviceKey/function/invoke", FunctionInvokeMessage.class, false),
    disconnect("/:productKey/:deviceKey/disconnect", DisconnectDeviceMessage.class, false),

    // ===== 子设备消息（网关代理上行）=====
    child("/:productKey/:deviceKey/child/:childProductKey/:childDeviceKey/*", ChildDeviceMessage.class, true) {
        @Override
        DeviceMessage doDecode(String[] topic, byte[] payload) {
            // 去掉 child 标识段，重组直连 topic（子设备 productKey/deviceKey）递归解码内层
            String[] childTopic = new String[topic.length - 3];
            childTopic[0] = "";
            childTopic[1] = topic[4];
            childTopic[2] = topic[5];
            System.arraycopy(topic, 6, childTopic, 3, topic.length - 6);
            DeviceMessage childMsg = TopicMessageCodec.decode(childTopic, payload);
            if (childMsg == null) {
                return null;
            }
            ChildDeviceMessage<Message> msg = new ChildDeviceMessage<>(topic[2], topic[4], topic[5], childMsg);
            msg.setProductKey(topic[1]);
            msg.setTimestamp(childMsg.getTimestamp());
            msg.setMessageId(childMsg.getMessageId());
            return msg;
        }

        @Override
        String[] buildTopicSegments(DeviceMessage message) {
            ChildDeviceMessage<?> child = (ChildDeviceMessage<?>) message;
            // 内层消息 topic：/:childProductKey/:childDeviceKey/{后缀...}
            String[] inner = TopicMessageCodec.buildInnerTopicSegments((DeviceMessage) child.getChildDeviceMessage());
            // 外层：/:productKey/:gatewayDeviceKey/child/:childProductKey/:childDeviceKey + 内层后缀段
            String[] merged = new String[6 + (inner.length - 3)];
            merged[0] = "";
            merged[1] = child.getProductKey();         // 网关 productKey
            merged[2] = child.getDeviceKey();          // 网关 deviceKey
            merged[3] = "child";
            merged[4] = child.getChildProductKey();
            merged[5] = child.getChildDeviceKey();
            System.arraycopy(inner, 3, merged, 6, inner.length - 3);
            return merged;
        }
    },

    // ===== 子设备消息回复（上行）=====
    childReply("/:productKey/:deviceKey/child-reply/:childProductKey/:childDeviceKey/*", ChildDeviceMessageReply.class, true) {
        @Override
        DeviceMessage doDecode(String[] topic, byte[] payload) {
            // 去掉 child-reply 标识段，重组直连 topic（子设备 productKey/deviceKey）递归解码内层
            String[] childTopic = new String[topic.length - 3];
            childTopic[0] = "";
            childTopic[1] = topic[4];
            childTopic[2] = topic[5];
            System.arraycopy(topic, 6, childTopic, 3, topic.length - 6);
            DeviceMessage childMsg = TopicMessageCodec.decode(childTopic, payload);
            if (childMsg == null) {
                return null;
            }
            ChildDeviceMessageReply<Message> msg = new ChildDeviceMessageReply<>(topic[2], topic[4], topic[5], childMsg);
            msg.setProductKey(topic[1]);
            msg.setTimestamp(childMsg.getTimestamp());
            msg.setMessageId(childMsg.getMessageId());
            return msg;
        }

        @Override
        String[] buildTopicSegments(DeviceMessage message) {
            ChildDeviceMessageReply<?> child = (ChildDeviceMessageReply<?>) message;
            String[] inner = TopicMessageCodec.buildInnerTopicSegments((DeviceMessage) child.getChildDeviceMessage());
            String[] merged = new String[6 + (inner.length - 3)];
            merged[0] = "";
            merged[1] = child.getProductKey();
            merged[2] = child.getDeviceKey();
            merged[3] = "child-reply";
            merged[4] = child.getChildProductKey();
            merged[5] = child.getChildDeviceKey();
            System.arraycopy(inner, 3, merged, 6, inner.length - 3);
            return merged;
        }
    },

    // ===== 事件上报（上行，eventId 在 topic 最后一段）=====
    event("/:productKey/:deviceKey/event/:eventId", EventMessage.class, true) {
        @Override
        DeviceMessage doDecode(String[] topic, byte[] payload) {
            EventMessage message = (EventMessage) super.doDecode(topic, payload);
            message.setEvent(topic[topic.length - 1]);
            return message;
        }
    };

    /** Jackson 线程安全，全局共享 */
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String[] pattern;
    @Getter
    private final Class<? extends DeviceMessage> type;
    @Getter
    private final boolean upstream;

    TopicMessageCodec(String topic, Class<? extends DeviceMessage> type, boolean upstream) {
        this.pattern = topic.split("/");
        this.type = type;
        this.upstream = upstream;
    }

    public String getTopicPattern() {
        return String.join("/", pattern);
    }

    /** 上行 topic 列表 — HTTP 服务启动时遍历注册 POST 路由 */
    public static List<String> upstreamTopics() {
        return Arrays.stream(values())
                .filter(t -> t.upstream)
                .map(TopicMessageCodec::getTopicPattern)
                .collect(Collectors.toList());
    }

    /** 解码入口：topic + payload → 消息 */
    public static DeviceMessage decode(String topic, byte[] payload) {
        return decode(topic.split("/"), payload);
    }

    public static DeviceMessage decode(String[] topics, byte[] payload) {
        return fromTopic(topics)
                .map(codec -> codec.doDecode(topics, payload))
                .orElse(null);
    }

    /** 编码入口：消息 → topic + payload */
    public static TopicPayload encode(DeviceMessage message) {
        return fromMessage(message)
                .orElseThrow(() -> new UnsupportedOperationException("不支持的消息类型: " + message.getClass()))
                .doEncode(message);
    }

    /** 通过消息类型获取实际 topic（deviceKey 已回填，子类消息拼接内层 topic） */
    public static String getTopic(DeviceMessage message) {
        return String.join("/", buildInnerTopicSegments(message));
    }

    /** 构建消息的实际 topic 分段（子设备消息递归取内层 topic 拼接） */
    static String[] buildInnerTopicSegments(DeviceMessage message) {
        TopicMessageCodec codec = fromMessage(message)
                .orElseThrow(() -> new UnsupportedOperationException("不支持的消息类型: " + message.getClass()));
        return codec.buildTopicSegments(message);
    }

    /** 默认：复制 pattern 并回填 productKey/deviceKey（子设备消息覆写） */
    String[] buildTopicSegments(DeviceMessage message) {
        String[] topics = Arrays.copyOf(pattern, pattern.length);
        topics[1] = message.getProductKey();
        topics[2] = message.getDeviceKey();
        return topics;
    }

    /** 枚举匹配：pattern 与 topic 逐段比对 */
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

    /** 默认解码：Jackson 反序列化 payload 到消息类，productKey/deviceKey 从 topic 覆盖 */
    @SneakyThrows
    DeviceMessage doDecode(String[] topic, byte[] payload) {
        DeviceMessage message = mapper.readValue(payload, type);
        if (message instanceof AbstractDeviceMessage) {
            ((AbstractDeviceMessage) message).setProductKey(topic[1]);
            ((AbstractDeviceMessage) message).setDeviceKey(topic[2]);
        }
        return message;
    }

    @SneakyThrows
    TopicPayload doEncode(DeviceMessage message) {
        return TopicPayload.of(String.join("/", buildTopicSegments(message)), mapper.writeValueAsBytes(message));
    }
}
