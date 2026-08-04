package net.lab1024.sa.base.common.message.config;

import net.lab1024.sa.base.common.message.support.DefaultDecodedClientMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageAutoConfiguration {
    @Bean
    public DefaultDecodedClientMessageHandler decodedClientMessageHandler() {
        return new DefaultDecodedClientMessageHandler();
    }
}
