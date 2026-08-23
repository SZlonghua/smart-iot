package net.lab1024.sa.base.cluster.config;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import net.lab1024.sa.base.cluster.ClusterManager;
import net.lab1024.sa.base.cluster.support.HazelcastClusterManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集群自动装配 — 仅 iot.cluster.enabled=true 时装配 Hazelcast；
 * 单机部署（默认）不装配，无 Hazelcast 运行时依赖。
 *
 * @Author 廖涛
 * @Date 2026/08/22
 * @Copyright 1024创新实验室
 */
@Configuration
@EnableConfigurationProperties(ClusterProperties.class)
@ConditionalOnProperty(prefix = "iot.cluster", name = "enabled", havingValue = "true")
public class HazelcastClusterAutoConfiguration {

    /** Hazelcast 实例 — 集群名/成员列表来自配置；destroyMethod 优雅停机 */
    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance(ClusterProperties properties) {
        Config config = new Config();
        config.setClusterName(properties.getHazelcast().getClusterName());
        config.getNetworkConfig().getJoin()
                .getTcpIpConfig().setEnabled(true)
                .setMembers(properties.getHazelcast().getMembers());
        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public ClusterManager clusterManager(HazelcastInstance hazelcastInstance) {
        return new HazelcastClusterManager(hazelcastInstance);
    }
}
