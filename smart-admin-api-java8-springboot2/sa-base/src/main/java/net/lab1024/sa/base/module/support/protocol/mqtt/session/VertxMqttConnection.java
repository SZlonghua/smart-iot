package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.ReferenceCountUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.mqtt.MqttEndpoint;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.common.message.raw.EncodedMessage;
import net.lab1024.sa.base.device.AuthenticationRequest;
import net.lab1024.sa.base.module.support.protocol.mqtt.message.DefaultMqttEncodedMessage;
import net.lab1024.sa.base.module.support.protocol.mqtt.message.MqttEncodedMessage;
import net.lab1024.sa.base.module.support.protocol.mqtt.message.WillMqttEncodedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Vertx MQTT Connection 实现 — 封装 MqttEndpoint。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/07
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
public class VertxMqttConnection implements MqttConnection {
    @Setter
    private String deviceId;
    @Setter
    private String productKey;
    @Setter
    private String deviceKey;
    private final MqttEndpoint endpoint;
    private long keepAliveTimeoutMs;
    private long lastPingTime = System.currentTimeMillis();
    private int messageIdCounter;

    private final Sinks.Many<EncodedMessage> messageSink = Sinks.many().multicast().onBackpressureBuffer();

    /** 连接已关闭标志 — 防止 disconnectConsumer 清理链内重复关闭 endpoint */
    private final java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean(false);


    private final Consumer<MqttConnection> defaultListener = mqttConnection -> {
        VertxMqttConnection.log.debug("mqtt client [{}] disconnected", getClientId());
        messageSink.tryEmitComplete();
    };
    private Consumer<MqttConnection> disconnectConsumer = defaultListener;


    private final MqttAuth emptyAuth = new MqttAuth() {
        @Override
        public String getClientId() {
            return VertxMqttConnection.this.getClientId();
        }

        @Override
        public String getUsername() {
            return "";
        }

        @Override
        public String getPassword() {
            return "";
        }
    };


    public VertxMqttConnection(MqttEndpoint endpoint) {
        this.endpoint = endpoint;
        this.keepAliveTimeoutMs = (endpoint.keepAliveTimeSeconds() + 10) * 1000L;
    }

    @Override
    public String getClientId() {
        return endpoint.clientIdentifier();
    }

    @Override
    public String getDeviceId() {
        return deviceId==null?getClientId():deviceId;
    }

    @Override
    public String getProductKey() {
        return productKey;
    }

    @Override
    public String getDeviceKey() {
        return deviceKey;
    }

    @Override
    public boolean isAlive() {
        return endpoint.isConnected() && (keepAliveTimeoutMs < 0 || ((System.currentTimeMillis() - lastPingTime) < keepAliveTimeoutMs));
    }

    void ping() {
        lastPingTime = System.currentTimeMillis();
    }

    // ===== 连接生命周期 =====

    /**
     * accept — 回复 CONNACK 接受连接
     *
     */
    @Override
    public MqttConnection accept() {
        endpoint.accept();
        log.info("连接已建立");
        return this;
    }

    @Override
    public Optional<MqttEncodedMessage> getWillMessage() {
        return Optional.of(new WillMqttEncodedMessage(endpoint.clientIdentifier(), endpoint.will()));
    }

    /** reject — 回复 CONNACK 拒绝连接 */
    @Override
    public void reject(MqttConnectReturnCode returnCode) {
        // 已关闭则跳过，避免对已关闭连接重复 reject / 触发清理链
        if (closed.get() || !endpoint.isConnected()) {
            return;
        }
        endpoint.reject(returnCode);
        complete();
    }

    /** init — 链式注册全部 MQTT 包处理器 */
    public VertxMqttConnection init() {
        endpoint
                .pingHandler(v -> {
                    log.info("ping");
                    this.ping();
                    if (!endpoint.isAutoKeepAlive()) {
                        endpoint.pong();
                    }
                })
                .publishHandler(msg -> {
                    // 延迟 ACK — 不在此处回复，解码成功后由网关调用 acknowledge()：
                    // 解码失败不 ack → 网关断开连接 → 客户端超时重发
                    DefaultMqttEncodedMessage encodedMessage = new DefaultMqttEncodedMessage(getDeviceId(), endpoint.clientIdentifier(), msg);
                    encodedMessage.setAck(() -> acknowledgePublish(encodedMessage));
                    messageSink.tryEmitNext(encodedMessage);
                })
                //QoS 1 PUBACK
                .publishAcknowledgeHandler(id -> {
                    ping();
                    log.debug("PUBACK mqtt[{}] message[{}]", getClientId(), id);
                })
                //QoS 2  PUBREC
                .publishReceivedHandler(id ->{
                    ping();
                    log.debug("PUBREC mqtt[{}] message[{}]", getClientId(), id);
                    endpoint.publishRelease(id);
                })
                //QoS 2  PUBREL
                .publishReleaseHandler(id -> {
                    ping();
                    log.debug("PUBREL mqtt[{}] message[{}]", getClientId(), id);
                    endpoint.publishComplete(id);
                })
                //QoS 2  PUBCOMP
                .publishCompletionHandler(id ->{
                    ping();
                    log.debug("PUBCOMP mqtt[{}] message[{}]", getClientId(), id);
                })
                .subscribeHandler(sub -> {

                    /*List<MqttQoS> grantedQos = sub.topicSubscriptions().stream()
                            .map(s -> MqttQoS.valueOf(s.qualityOfService().value()))
                            .collect(Collectors.toList());
                    endpoint.subscribeAcknowledge(sub.messageId(), grantedQos);*/
                })
                .unsubscribeHandler(unsub ->{
//                    endpoint.unsubscribeAcknowledge(unsub.messageId());
                })
                .disconnectHandler(ignore -> complete())
                .closeHandler(v -> complete())
                .exceptionHandler(error ->{
                    if (error instanceof DecoderException) {
                        if (error.getMessage().contains("too large message")) {
                            log.error("MQTT消息过大,请在网络组件中设置[最大消息长度].", error);
                            return;
                        }
                    }
                    log.error(error.getMessage(), error);
                });
        return this;
    }

    // ===== ClientConnection =====

    /** 延迟确认发布消息 — QoS1 回 PUBACK / QoS2 回 PUBREC（此后客户端回 PUBREL → publishReleaseHandler 回 PUBCOMP） */
    private void acknowledgePublish(DefaultMqttEncodedMessage message) {
        if (message.getQos() == MqttQoS.AT_LEAST_ONCE.value()) {
            endpoint.publishAcknowledge(message.getMessageId());
            log.debug("延迟 PUBACK mqtt[{}] message[{}]", getClientId(), message.getMessageId());
        } else if (message.getQos() == MqttQoS.EXACTLY_ONCE.value()) {
            endpoint.publishReceived(message.getMessageId());
            log.debug("延迟 PUBREC mqtt[{}] message[{}]", getClientId(), message.getMessageId());
        }
    }

    @Override
    public Mono<Void> sendMessage(EncodedMessage message) {
        // TODO: 实现下行消息发送
        ping();
        MqttEncodedMessage mqttMessage = (MqttEncodedMessage) message;
        int messageId = mqttMessage.getMessageId() <= 0 ? nextMessageId() : mqttMessage.getMessageId();
        return Mono
                .create(sink -> {
                    ByteBuf buf = message.getPayload();
                    Buffer buffer = BufferImpl.buffer(buf);
                    endpoint.publish(
                            mqttMessage.getTopic(),
                            buffer,
                            MqttQoS.valueOf(mqttMessage.getQos()),
                            mqttMessage.isDup(),
                            mqttMessage.isRetain(),
                            messageId,
                            MqttProperties.NO_PROPERTIES,
                            result -> {
                                if (result.succeeded()) {
                                    sink.success();
                                } else {
                                    sink.error(result.cause());
                                }
                                ReferenceCountUtil.safeRelease(buf);
                            }
                    );
                });
    }

    private synchronized int nextMessageId() {
        this.messageIdCounter = ((this.messageIdCounter % 65535) != 0) ? this.messageIdCounter + 1 : 1;
        return this.messageIdCounter;
    }

    @Override
    public Flux<EncodedMessage> receiveMessage() {
        return messageSink.asFlux();
    }

    @Override
    public void disconnect() {
        close();
    }

    @Override
    public Optional<MqttAuth> getAuth() {
        return endpoint.auth() == null ? Optional.of(emptyAuth) : Optional.of(new VertxMqttAuth());
    }

    @Override
    public void close() {
        // 已关闭则跳过，避免重复关闭抛 IllegalStateException
        if (closed.get()) {
            return;
        }
        if (endpoint.isConnected()) {
            endpoint.close();
        }
        // 统一由 complete() 置位 closed 并执行清理链
        complete();
    }

    private void complete() {
        // 校验并置位：首次触发执行清理；清理链内重复调用（remove → connection.close）及 closeHandler 二次触发直接跳过
        if (closed.getAndSet(true)) {
            return;
        }
        log.info("Mqtt connection complete [{}]", getClientId());
        disconnectConsumer.accept(this);
    }

    public AuthenticationRequest getAuthenticationRequest(Transport transport) {
        return MqttAuthenticationRequest.builder()
                .clientId(getClientId())
                .username(endpoint.auth().getUsername())
                .password(endpoint.auth().getPassword())
                .transport(transport)
                .build();
    }

    @Override
    public void onClose(Consumer<MqttConnection> listener) {
        disconnectConsumer = disconnectConsumer.andThen(listener);
    }

    @Override
    public void keepAlive() {
        ping();
    }

    @Override
    public Duration getKeepAliveTimeout() {
        return Duration.ofMillis(keepAliveTimeoutMs);
    }

    @Override
    public void setKeepAliveTimeout(Duration duration) {
        this.keepAliveTimeoutMs = duration.toMillis();
    }

    @Override
    public long getLastPingTime() {
        return lastPingTime;
    }


    class VertxMqttAuth implements MqttAuth {

        @Override
        public String getClientId() {
            return endpoint.clientIdentifier();
        }

        @Override
        public String getUsername() {
            return endpoint.auth().getUsername();
        }

        @Override
        public String getPassword() {
            return endpoint.auth().getPassword();
        }
    }
}
