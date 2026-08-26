package net.lab1024.sa.base.device.config;

import net.lab1024.sa.base.cluster.ClusterManager;
import net.lab1024.sa.base.common.protocol.ProtocolSupportManager;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.DeviceSendOperator;
import net.lab1024.sa.base.device.DeviceMessageSender;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.device.support.ClusterDeviceMessageSender;
import net.lab1024.sa.base.device.support.DefaultDeviceSendOperator;
import net.lab1024.sa.base.device.support.LocalDeviceMessageSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 设备下发装配 — 按 iot.cluster.enabled 装配发送器：
 * 单机（默认）→ LocalDeviceMessageSender；集群 → ClusterDeviceMessageSender（构造时注册本节点 DeviceMessageSendService）。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
@Configuration
public class DeviceMessageSenderAutoConfiguration {

    /** 本地发送器 — 单机核心（也是集群中"设备所在节点"执行的部分），同时实现 DeviceMessageReplyHandler 供回复回调注入 */
    @Bean
    public LocalDeviceMessageSender localDeviceMessageSender(DeviceSessionManager sessionManager,
                                                             DeviceRegistry registry,
                                                             ProtocolSupportManager protocolSupportManager) {
        return new LocalDeviceMessageSender(sessionManager, registry, protocolSupportManager);
    }

    /** 下发门面 — 业务层只依赖接口，不感知本地/集群 */
    @Bean
    public DeviceSendOperator deviceSendOperator(DeviceMessageSender messageSender, DeviceRegistry registry) {
        return new DefaultDeviceSendOperator(messageSender, registry);
    }

    /**
     * 单机（默认）：发送器 = 本地发送器
     * &#064;Primary：localDeviceMessageSender  也实现 DeviceMessageSender，同接口双候选时按 Primary 解析（单机/集群条件互斥，运行时仅一个 @Primary 生效）
     */
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "iot.cluster", name = "enabled", havingValue = "false", matchIfMissing = true)
    public DeviceMessageSender deviceMessageSender(LocalDeviceMessageSender localSender) {
        return localSender;
    }

    /**
     * 集群：发送器 = 集群路由发送器（构造时注册本节点 DeviceMessageSendService，供其他节点代理调用）
     * &#064;Primary：与单机方法同理，见上
     */
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "iot.cluster", name = "enabled", havingValue = "true")
    public DeviceMessageSender clusterDeviceMessageSender(LocalDeviceMessageSender localSender,
                                                          ClusterManager clusterManager,
                                                          DeviceRegistry registry,
                                                          DeviceSessionManager sessionManager) {
        return new ClusterDeviceMessageSender(clusterManager, localSender, registry, sessionManager);
    }
}
