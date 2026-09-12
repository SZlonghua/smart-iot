package net.lab1024.sa.base.module.support.protocol.mqtt.gateway;

import lombok.Getter;
import net.lab1024.sa.base.cluster.ClusterManager;
import net.lab1024.sa.base.common.gateway.DeviceGateway;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.common.message.DecodedClientMessageHandler;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.common.network.DefaultNetworkType;
import net.lab1024.sa.base.common.network.NetworkManager;
import net.lab1024.sa.base.common.network.NetworkType;
import net.lab1024.sa.base.common.protocol.ProtocolSupportManager;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttServerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Objects;


@Getter
public class MqttServerDeviceGatewayProvider implements DeviceGatewayProvider {
    private static final Logger log = LoggerFactory.getLogger(MqttServerDeviceGatewayProvider.class);
    private final NetworkManager networkManager;

    private final DeviceRegistry registry;

    private final DeviceSessionManager sessionManager;

    private final DecodedClientMessageHandler messageHandler;

    /** 事件总线 — 网关发布连接建立/断开型上/下线消息 */
    private final IEventBus eventBus;

    private final ProtocolSupportManager protocolSupportManager;

    /** 集群管理器 — 网关创建会话时取当前节点 ID；单机部署（未装配集群）为 null */
    @Nullable
    private final ClusterManager clusterManager;


    public MqttServerDeviceGatewayProvider(NetworkManager networkManager,
                                           DeviceRegistry registry,
                                           DeviceSessionManager sessionManager,
                                           DecodedClientMessageHandler messageHandler,
                                           IEventBus eventBus,
                                           ProtocolSupportManager protocolSupportManager,
                                           @Nullable ClusterManager clusterManager) {
        this.networkManager = networkManager;
        this.registry = registry;
        this.sessionManager = sessionManager;
        this.messageHandler = messageHandler;
        this.eventBus = eventBus;
        this.protocolSupportManager = protocolSupportManager;
        this.clusterManager = clusterManager;
    }

    @Override
    public String getId() {
        return "mqtt-server";
    }

    @Override
    public String getName() {
        return "MQTT Server 网关";
    }

    @Override
    public Transport getTransport() {
        return DefaultTransport.MQTT;
    }

    public NetworkType getNetworkType() {
        return DefaultNetworkType.MQTT_SERVER;
    }

    @Override
    public Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties) {
        return networkManager.<MqttServerNetwork>getNetwork(getNetworkType(), properties.getComponentId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("获取MQTT Server网络失败(组件不存在或类型不匹配) — gatewayId={}, componentId={}",
                            properties.getId(), properties.getComponentId());
                    return Mono.empty();
                }))
                .map(mqttServerNetwork -> new MqttServerDeviceGateway(
                        properties.getId(),
                        registry,
                        sessionManager,
                        mqttServerNetwork,
                        messageHandler,
                        eventBus,
                        protocolSupportManager.getProtocol(properties.getProtocol()),
                        clusterManager));
    }

    @Override
    public Mono<? extends DeviceGateway> reloadDeviceGateway(DeviceGateway gateway,
                                                             DeviceGatewayProperties properties) {
        MqttServerDeviceGateway deviceGateway = ((MqttServerDeviceGateway) gateway);

        String networkId = properties.getComponentId();
        //网络组件发生了变化
        if (deviceGateway.isChangeNetwork(networkId)) {
            return gateway
                    .shutdown()
                    .then(this
                            .createDeviceGateway(properties));
//                            .flatMap(gate -> gate.startup().thenReturn(gate)));
        }
        if (deviceGateway.isChangeProtocol(properties.getProtocol())){
            deviceGateway.setProtocolSupport(protocolSupportManager.getProtocol(properties.getProtocol()));
        }
        return Mono.just(deviceGateway);
    }
}
