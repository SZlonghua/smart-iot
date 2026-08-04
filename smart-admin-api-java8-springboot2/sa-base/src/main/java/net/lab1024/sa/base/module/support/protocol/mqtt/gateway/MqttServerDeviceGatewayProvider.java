package net.lab1024.sa.base.module.support.protocol.mqtt.gateway;

import lombok.Getter;
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
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttServerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Objects;


@Getter
public class MqttServerDeviceGatewayProvider implements DeviceGatewayProvider {
    private static final Logger log = LoggerFactory.getLogger(MqttServerDeviceGatewayProvider.class);
    private final NetworkManager networkManager;

    private final DeviceRegistry registry;

    private final DeviceSessionManager sessionManager;

    private final DecodedClientMessageHandler messageHandler;

    private final ProtocolSupportManager protocolSupportManager;


    public MqttServerDeviceGatewayProvider(NetworkManager networkManager,
                                           DeviceRegistry registry,
                                           DeviceSessionManager sessionManager,
                                           DecodedClientMessageHandler messageHandler,
                                           ProtocolSupportManager protocolSupportManager) {
        this.networkManager = networkManager;
        this.registry = registry;
        this.sessionManager = sessionManager;
        this.messageHandler = messageHandler;
        this.protocolSupportManager = protocolSupportManager;
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
        return networkManager.<MqttServerNetwork>getNetwork(getNetworkType(),properties.getComponentId())
                .map(mqttServerNetwork -> new MqttServerDeviceGateway(
                        properties.getId(),
                        registry,
                        sessionManager,
                        mqttServerNetwork,
                        messageHandler,
                        protocolSupportManager.getProtocol(properties.getProtocol())));
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
