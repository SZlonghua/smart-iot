package net.lab1024.sa.base.module.support.protocol.mqtt.network;

import lombok.Getter;
import lombok.Setter;
import net.lab1024.sa.base.common.network.ServerNetworkConfig;
import net.lab1024.sa.base.common.network.NetworkTransport;

@Getter
@Setter
public class MqttServerConfig implements ServerNetworkConfig {
    private String id;
    private String host;
    private int port;

    @Override
    public NetworkTransport getTransport() {
        return NetworkTransport.TCP;
    }

    @Override
    public String getSchema() {
        return "mqtt";
    }
}
