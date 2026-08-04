package net.lab1024.sa.base.common.network;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DefaultNetworkType implements NetworkType {

    TCP_CLIENT("TCP客户端"),
    TCP_SERVER("TCP服务"),

    MQTT_CLIENT("MQTT客户端"),
    MQTT_SERVER("MQTT服务"),

    HTTP_CLIENT("HTTP客户端"),
    HTTP_SERVER("HTTP服务"),

    WEB_SOCKET_CLIENT("WebSocket客户端"),
    WEB_SOCKET_SERVER("WebSocket服务"),

    UDP("UDP"),

    COAP_CLIENT("CoAP客户端"),
    COAP_SERVER("CoAP服务"),

            ;

    static {
        NetworkTypes.register(Arrays.asList(DefaultNetworkType.values()));
    }

    private final String name;

    DefaultNetworkType(String name) {
        this.name = name;
    }

    public String getId() {
        return this.name;
    }

}
