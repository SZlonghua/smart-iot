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
 *
 * &#064;Author  廖涛
 * &#064;Date  2026/08/07
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
public class VertxMqttConnection implements MqttConnection {
    @Setter
    private String deviceId;
    private final MqttEndpoint endpoint;
    private long keepAliveTimeoutMs;
    private long lastPingTime = System.currentTimeMillis();
    private int messageIdCounter;

    private final Sinks.Many<EncodedMessage> messageSink = Sinks.many().multicast().onBackpressureBuffer();


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
     * @return
     */
    @Override
    public MqttConnection accept() {
        endpoint.accept();
        return this;
    }

    @Override
    public Optional<MqttEncodedMessage> getWillMessage() {
        return Optional.of(new WillMqttEncodedMessage(endpoint.clientIdentifier(), endpoint.will()));
    }

    /** reject — 回复 CONNACK 拒绝连接 */
    @Override
    public void reject(MqttConnectReturnCode returnCode) {
        endpoint.reject(returnCode);
        endpoint.close();

    }

    /** init — 链式注册全部 MQTT 包处理器 */
    public VertxMqttConnection init() {
        endpoint
                .pingHandler(v -> {
                    this.ping();
                    if (!endpoint.isAutoKeepAlive()) {
                        endpoint.pong();
                    }
                })
                .publishHandler(msg -> {
                    if (msg.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                        endpoint.publishAcknowledge(msg.messageId());
                    }
                    if (msg.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                        endpoint.publishReceived(msg.messageId());
                    }
                    messageSink.tryEmitNext(new DefaultMqttEncodedMessage(endpoint.clientIdentifier(), msg));
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
                .disconnectHandler(ignore->{
                    messageSink.tryEmitComplete();
                    disconnectConsumer.accept(this);
                })
                .closeHandler(v -> {
                    messageSink.tryEmitComplete();
                    disconnectConsumer.accept(this);
                })
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

    @Override
    public Mono<Void> sendMessage(EncodedMessage message) {
        // TODO: 实现下行消息发送
        ping();
        MqttEncodedMessage mqttMessage = (MqttEncodedMessage) message;
        int messageId = mqttMessage.getMessageId() <= 0 ? nextMessageId() : mqttMessage.getMessageId();
        return Mono
                .<Void>create(sink -> {
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
        endpoint.close();
        disconnectConsumer.accept(this);
    }

    public AuthenticationRequest getAuthenticationRequest() {
        return MqttAuthenticationRequest.builder()
                .clientId(getClientId())
                .username(endpoint.auth().getUsername())
                .password(endpoint.auth().getPassword())
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
