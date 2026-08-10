package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import net.lab1024.sa.base.device.AuthenticationRequest;

public interface MqttAuth {

    String getClientId();

    String getUsername();

    String getPassword();

    default AuthenticationRequest toAuthenticationRequest() {
        return MqttAuthenticationRequest.builder()
                .clientId(getClientId())
                .username(getUsername())
                .password(getPassword())
                .build();
    }
}
