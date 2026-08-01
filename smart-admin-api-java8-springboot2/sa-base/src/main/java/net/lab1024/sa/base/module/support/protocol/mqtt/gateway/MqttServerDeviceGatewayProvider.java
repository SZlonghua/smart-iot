package net.lab1024.sa.base.module.support.protocol.mqtt.gateway;

import net.lab1024.sa.base.common.gateway.DeviceGateway;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;
import net.lab1024.sa.base.common.message.codec.Transport;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class MqttServerDeviceGatewayProvider implements DeviceGatewayProvider {

    @Override public String getId() { return "mqtt-server"; }
    @Override public String getName() { return "MQTT Server 网关"; }
    @Override public Transport getTransport() { return DefaultTransport.MQTT; }

    @Override
    public Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties) {
        return Mono.just(new MqttServerDeviceGateway(properties));
    }

    @Override
    public Mono<? extends DeviceGateway> reloadDeviceGateway(DeviceGateway gateway,
                                                             DeviceGatewayProperties properties) {
        MqttServerDeviceGateway deviceGateway = ((MqttServerDeviceGateway) gateway);

        String networkId = properties.getComponentId();
        //网络组件发生了变化
        /*if (!Objects.equals(networkId, deviceGateway.getMqttServer().getId())) {
            return gateway
                    .shutdown()
                    .then(this
                            .createDeviceGateway(properties)
                            .flatMap(gate -> gate.startup().thenReturn(gate)));
        }*/
        return Mono.just(gateway);
    }
}
