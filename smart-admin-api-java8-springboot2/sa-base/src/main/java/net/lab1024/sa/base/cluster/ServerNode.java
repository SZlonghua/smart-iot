package net.lab1024.sa.base.cluster;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 集群节点描述 — 注册到 Hazelcast 分布式 Map，供节点发现。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/22
 * &#064;Copyright  1024创新实验室
 */
@Data
public class ServerNode implements Serializable {

    /** 节点 ID（Hazelcast Member UUID，集群内唯一） */
    private String nodeId;

    /** 节点主机地址 */
    private String host;

    /** 服务端口（用途标识，可空） */
    private Integer port;

    /** 注册时间 */
    private long registerTime;

    /** 扩展元数据（如承载的网关实例列表，预留） */
    private Map<String, Object> metadata = new HashMap<>();
}
