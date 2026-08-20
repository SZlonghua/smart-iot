package net.lab1024.sa.base.common.message.config;

import net.lab1024.sa.base.common.message.support.DefaultDecodedClientMessageHandler;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.device.support.DeviceOnlineStatePersistence;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageAutoConfiguration {
    @Bean
    public DefaultDecodedClientMessageHandler decodedClientMessageHandler(IEventBus eventBus,
                                                                         DeviceSessionManager sessionManager,
                                                                         ObjectProvider<DeviceOnlineStatePersistence> onlineStatePersister) {
        return new DefaultDecodedClientMessageHandler(eventBus, sessionManager, onlineStatePersister.getIfAvailable());
    }
}
