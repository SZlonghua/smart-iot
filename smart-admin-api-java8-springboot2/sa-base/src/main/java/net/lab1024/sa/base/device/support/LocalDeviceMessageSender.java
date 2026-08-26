package net.lab1024.sa.base.device.support;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.message.*;
import net.lab1024.sa.base.common.message.codec.DefaultMessageEncodeContext;
import net.lab1024.sa.base.common.message.codec.MessageEncodeContext;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.protocol.ProtocolSupportManager;
import net.lab1024.sa.base.device.DeviceMessageReplyHandler;
import net.lab1024.sa.base.device.DeviceMessageSender;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.device.session.support.ChildDeviceSession;
import net.lab1024.sa.base.module.support.cache.core.Value;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地发送实现 — 单机核心逻辑（也是集群中"设备所在节点"执行的部分），
 * 同时实现 {@link DeviceMessageSender} + {@link DeviceMessageReplyHandler}（发送与回复处理内聚一体）。
 * <p>
 * 发送链路：查会话 → 在线判定 → 消息填充（messageId/keys/默认超时）→ 子设备包装 → 注册 pending（发送前）
 * → 编码发送 → 等待回复（超时以消息 timeout header 为准）；pending 经外层 doFinally 清理，防内存遗留。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
@Slf4j
public class LocalDeviceMessageSender implements DeviceMessageSender, DeviceMessageReplyHandler {

    /** messageId → 回复等待（发送时注册，回复到达时完成） */
    private final Map<String, Sinks.One<DeviceMessageReply>> pending = new ConcurrentHashMap<>();

    private final DeviceSessionManager sessionManager;
    private final DeviceRegistry registry;
    private final ProtocolSupportManager protocolSupportManager;

    public LocalDeviceMessageSender(DeviceSessionManager sessionManager,
                                    DeviceRegistry registry,
                                    ProtocolSupportManager protocolSupportManager) {
        this.sessionManager = sessionManager;
        this.registry = registry;
        this.protocolSupportManager = protocolSupportManager;
    }

    @Override
    public Mono<DeviceMessageReply> sendAndWait(DeviceMessage message, long timeout) {
        // 显式超时写入 header（addHeaderIfAbsent — 调用方已设置的 header 优先）— 等待以 header 为准；
        // prepareMessage 对仍缺省的消息回填默认 10s（不允许没有超时）
        if (timeout > 0) {
            message.addHeaderIfAbsent(Headers.TIMEOUT.getValue(), timeout);
        }
        // deviceId 由消息携带（所有具体消息均为 AbstractDeviceMessage）
        return sessionManager.getSession(((AbstractDeviceMessage) message).getDeviceId(), true)   // ① 查会话（失效自动注销）
                .filter(DeviceSession::isAlive)                          // ② 在线判定
                .switchIfEmpty(Mono.error(new BusinessException("设备离线，无法下发命令")))
                .flatMap(session -> prepareMessage(session, message)    // ③ 填充 + 默认超时回填 + 子设备包装
                        .flatMap(msg -> {
                            // ④ 发送前先注册 pending（设备可能秒回：回复在 doSend 完成前到达也能经 onReply 命中 sink，
                            //    Sinks.One 无订阅时缓存值 — 不丢回复）；生效超时以 header 为准（prepareMessage 已保证恒有值）
                            Mono<DeviceMessageReply> replyMono = isAsync(msg)
                                    ? Mono.just((DeviceMessageReply) ((RepayableDeviceMessage<?>) msg).newReply())  // async：下发即成功，返回空回复骨架
                                    : waitReply(msg.getMessageId(), resolveTimeout(msg));                            // 等待设备回复
                            // ⑤ 编码 + 发送 → 等待回复；⑥ 无论成功 / 失败 / 超时 / 取消 → doFinally 移除 pending（防内存遗留）
                            return doSend(session, msg)
                                    .then(replyMono)
                                    .doFinally(ignore -> pending.remove(msg.getMessageId()));
                        }));
    }

    /** 生效超时 — 以消息 timeout header 为准（prepareMessage 已回填默认 10s，恒有值）；非法值兜底默认 10s */
    private long resolveTimeout(DeviceMessage message) {
        Object header = message.getHeaderOrElse(Headers.TIMEOUT.getValue(), () -> DEFAULT_TIMEOUT_MS);
        return header instanceof Number && ((Number) header).longValue() > 0
                ? ((Number) header).longValue() : DEFAULT_TIMEOUT_MS;
    }

    @Override
    public Mono<Void> send(DeviceMessage message) {
        return sessionManager.getSession(((AbstractDeviceMessage) message).getDeviceId(), true)
                .filter(DeviceSession::isAlive)
                .switchIfEmpty(Mono.error(new BusinessException("设备离线，无法下发命令")))
                .flatMap(session -> prepareMessage(session, message)
                        .flatMap(msg -> doSend(session, msg)));   // 异步：不等待回复，下发即成功，无需超时
    }

    /** async header 判定 — 带 async=true 的消息下发即成功，不等待回复（功能调用的物模型 isAsync 自动补充，见 3.2） */
    private boolean isAsync(DeviceMessage message) {
        return Boolean.TRUE.equals(message.getHeaderOrElse(Headers.ASYNC.getValue(), () -> Boolean.FALSE));
    }

    /** ⑦ 设备回复到达 → 按 messageId 匹配并完成等待（DeviceMessageReplyHandler 接口实现） */
    @Override
    public void onReply(AbstractDeviceMessageReply reply) {
        // 子设备回复解包 — 等待方（sendAndWait 调用方）是主动发起方，已知道下发目标设备，只需内层回复内容；
        // 解包后按内层回复匹配/下发（外层 messageId = 内层回复 messageId，见十章 ChildDeviceMessageReply 改造）；
        // 父类信息（网关/子设备标识）保留在事件总线发布路径 — handler ② 按外层发布，监听器自行解包
        AbstractDeviceMessageReply target = reply;
        if (reply instanceof ChildDeviceMessageReply) {
            target = (AbstractDeviceMessageReply) ((ChildDeviceMessageReply<?>) reply).getChildDeviceMessage();
        }
        Sinks.One<DeviceMessageReply> sink = pending.remove(target.getMessageId());
        if (sink != null) {
            sink.tryEmitValue(target);
        } else {
            // 无等待方 — 回复晚于超时/发送方已清理（doFinally 移除 pending），此时丢弃属正常兜底，非链路异常
            log.warn("reply message 无等待方被丢弃: {}", reply);
        }
    }

    /**
     * 注册等待 — 在【发送前】注册（回复可能在消息发出后立刻到达，注册必须早于 doSend；
     * Sinks.One 无订阅时缓存值，回复到达即命中 sink，不丢失）。
     * 清理由 sendAndWait 外层 doFinally 兜底（见上）：doSend 失败 / 超时 / 调用方取消 / 正常完成均移除条目，防内存遗留。
     */
    private Mono<DeviceMessageReply> waitReply(String messageId, long timeout) {
        Sinks.One<DeviceMessageReply> sink = Sinks.one();
        pending.put(messageId, sink);
        return sink.asMono()
                .timeout(Duration.ofMillis(timeout), Mono.error(
                        new BusinessException("命令执行超时，设备未在 " + timeout + "ms 内回复")));
    }

    /** 消息填充与子设备包装 */
    private Mono<DeviceMessage> prepareMessage(DeviceSession session, DeviceMessage message) {
        AbstractDeviceMessage msg = (AbstractDeviceMessage) message;
        // messageId / timestamp 填充（messageId 全局唯一，回复按它绑定）
        if (msg.getMessageId() == null) {
            msg.setMessageId(UUID.randomUUID().toString());
        }
        msg.setDeviceId(session.getDeviceId());
        msg.setProductKey(session.getProductKey());
        msg.setDeviceKey(session.getDeviceKey());
        // 默认超时回填 — 每个消息发送前必须有 timeout header（缺省 10s，不允许未设置），等待/发送以 header 为准
        msg.addHeaderIfAbsent(Headers.TIMEOUT.getValue(), DEFAULT_TIMEOUT_MS);

        // 子设备 → 包装为 ChildDeviceMessage（外层 = 网关三元信息，内层 = 子设备消息），
        // TopicMessageCodec.child 枚举自动拼接 /{gatewayPk}/{gatewayDk}/child/{childPk}/{childDk}/...
        if (session instanceof ChildDeviceSession) {
            ChildDeviceSession child = (ChildDeviceSession) session;
            DeviceSession parent = child.getParent();
            // 构造只传父（网关）设备 id / deviceKey / productKey；子设备三元信息由内层消息携带（上方已填充 session 即子设备三元），
            // 外层与内层是同一条消息，外层 messageId 取内层 messageId（构造内设置，回复按它绑定）
            ChildDeviceMessage<DeviceMessage> wrapper =
                    new ChildDeviceMessage<>(parent.getDeviceId(), parent.getDeviceKey(), parent.getProductKey(), msg);
            return Mono.just(wrapper);
        }
        return Mono.just(msg);
    }

    /** 编码与发送 */
    private Mono<Void> doSend(DeviceSession session, DeviceMessage message) {
        // 子设备消息发往父（网关）会话的连接（子设备与父设备同协议，取父设备的 protocolId）
        DeviceSession target = (session instanceof ChildDeviceSession)
                ? ((ChildDeviceSession) session).getParent() : session;

        return registry.getDevice(target.getDeviceId())                       // ① 设备 Redis Hash 取 protocolId（会话注册时写入）
                .flatMap(operator -> operator.getSelfConfig(DeviceField.PROTOCOL_ID.getValue()))
                .map(Value::asString)
                .filter(StringUtils::isNotEmpty)
                .switchIfEmpty(Mono.error(new BusinessException("设备未绑定协议，无法下发")))
                .flatMap(protocolSupportManager::getProtocol)                  // ② 协议管理器取协议（ProtocolSupportManager）
                .flatMap(ps -> ps.getMessageCodec(target.getTransport()))
                .flatMap(codec -> Mono.from(codec.encode(DefaultMessageEncodeContext.of(target, message))))
                .flatMap(target::send)                                        // 会话发送（见 3.6）
                .then();
    }
}
