package net.lab1024.sa.base.cluster.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群配置 — iot.cluster.*
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/22
 * &#064;Copyright  1024创新实验室
 */
@Data
@ConfigurationProperties(prefix = "iot.cluster")
public class ClusterProperties {

    /** 集群开关 — 默认单机；多节点部署置 true */
    private boolean enabled;

    /** Hazelcast 配置 */
    private Hazelcast hazelcast = new Hazelcast();

    @Data
    public static class Hazelcast {

        /** 集群名 — 相同集群名的实例才能组网 */
        private String clusterName;

        /** TCP-IP 成员列表（host:port，集群内所有节点填同一份，含自己） */
        private List<String> members = new ArrayList<>();
    }
}
