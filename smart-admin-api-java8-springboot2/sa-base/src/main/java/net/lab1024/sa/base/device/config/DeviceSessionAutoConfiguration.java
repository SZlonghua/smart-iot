package net.lab1024.sa.base.device.config;


import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.device.session.support.LocalDeviceSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceSessionAutoConfiguration {
    @Bean
    public DeviceSessionManager deviceSessionManager() {
        return new LocalDeviceSessionManager();
    }
}
