package net.lab1024.sa.protocol;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;
import net.lab1024.sa.base.common.protocol.CompositeProtocolSupport;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.spi.ProtocolSupportProvider;
import net.lab1024.sa.base.common.spi.ServiceContext;
import reactor.core.publisher.Mono;


/**
 * 演示用协议支持提供者。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/11
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
public class SmartIoTProtocolSupportProvider implements ProtocolSupportProvider {

    @Override
    public Mono<? extends ProtocolSupport> create(ServiceContext context) {
        log.info("[SmartProtocol] 创建协议支持");
        CompositeProtocolSupport protocolSupport = new CompositeProtocolSupport();
        protocolSupport.setId("smart-iot");
        protocolSupport.setName("智能物联网协议");
        protocolSupport.setDescription("智能物联网协议");

        protocolSupport.addMessageCodec(new MqttDeviceMessageCodec());

        protocolSupport.addAuthenticator(DefaultTransport.MQTT, new MqttAuthenticator());

        return Mono.just(protocolSupport);
    }

    @Override
    public void dispose() {
        log.info("[DemoProtocol] dispose");
    }
}
