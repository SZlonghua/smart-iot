package net.lab1024.sa.base.cluster;

import com.hazelcast.cluster.Member;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 集群管理器 — 每节点一个实例。
 * 节点管理 + 服务注册/获取：本节点服务注册到本地注册表；
 * 获取远程节点服务时返回动态代理，方法调用经 Hazelcast 转发到目标节点执行。
 * 通用设施，与具体业务无关 — 任何服务（设备发送、网关启停、后续其他）都可注册。
 *
 * @Author 廖涛
 * @Date 2026/08/22
 * @Copyright 1024创新实验室
 */
public interface ClusterManager {

    /** ① 获取当前节点 ID */
    String getCurrentNodeId();

    /** ② 获取当前节点信息（ServerNode，含 host/注册时间/元数据等） */
    ServerNode getCurrentNode();

    /** ③ 获取所有集群节点（含本节点） */
    List<ServerNode> getClusterNodes();

    /** ④ 按 nodeId 获取节点信息 — 节点不存在/已退出返回 null */
    ServerNode getServerNode(String nodeId);

    /** 按 nodeId 获取 Hazelcast 实时成员（远程提交 submitToMember 用）— 节点已退出返回 null */
    Member getServerMember(String nodeId);

    /** ⑤ 注册服务到本节点（服务类型 + 实例），其他节点经 getService 发现并调用 */
    <T> void register(Class<T> serviceClass, T service);

    /** ⑥ 通过 nodeId 获取服务实例 — 本节点返回真实实例，远程节点返回动态代理；未注册返回 empty */
    <T> Mono<T> getService(String nodeId, Class<T> serviceClass);


    <T> T getLocalService(Class<T> serviceClass);

    /** ⑦ 获取所有节点的指定服务实例 — 本节点真实实例 + 远程节点代理（Flux 流式返回）。
     *  默认方法：由 getClusterNodes + getService 组合实现，实现类无需改动。
     *  未注册该服务的节点自动跳过（本节点 empty 过滤；远程节点代理调用时在目标节点报错，由调用方容忍） */
    default <T> Flux<T> getServices(Class<T> serviceClass) {
        return Flux.fromIterable(getClusterNodes())
                .map(ServerNode::getNodeId)
                .flatMap(nodeId -> getService(nodeId, serviceClass));
    }
}
