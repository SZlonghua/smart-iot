package net.lab1024.sa.base.cluster.support;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import net.lab1024.sa.base.cluster.ClusterManager;
import net.lab1024.sa.base.cluster.ServerNode;
import net.lab1024.sa.base.cluster.support.task.RemoteServiceInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClusterManager Hazelcast 实现：
 * - 节点注册表：分布式 IMap（nodeId → ServerNode），构造时注册本节点，成员退出自动清理
 * - 本地服务注册表：本节点真实服务实例（ConcurrentHashMap，接口 → 实例）
 * - 远程代理：getService 远程节点时返回 JDK 动态代理，方法调用 → RemoteServiceInvoker
 *   提交到目标节点（IExecutorService）执行 → 结果回传
 *
 * @Author 廖涛
 * @Date 2026/08/23
 * @Copyright 1024创新实验室
 */
public class HazelcastClusterManager implements ClusterManager {

    /** 远程调用超时（毫秒）— 需大于业务方法自身超时（如设备回复 10s），否则目标节点尚未完成即被截断 */
    private static final long DEFAULT_RPC_TIMEOUT = 15_000;

    private static final String NODE_REGISTRY_MAP = "cluster:node-registry";
    private static final Logger log = LoggerFactory.getLogger(HazelcastClusterManager.class);

    private final HazelcastInstance hazelcast;
    private final IMap<String, ServerNode> nodeMap;                   // 分布式节点注册表
    private final Map<Class<?>, Object> localServices = new ConcurrentHashMap<>();  // 本节点服务
    private final RemoteServiceInvoker remoteInvoker;                 // 远程调用器（代理方法调用 → 目标节点执行）

    // ===== 本节点管理器持有 =====

    /** 本 JVM 集群管理器 — 远程任务在目标节点执行时经此取本节点服务（构造时写入） */
    private static volatile HazelcastClusterManager CURRENT;

    /** 本 JVM 集群管理器 — 供 RemoteInvokeTask 在目标节点执行时获取本节点管理器 */
    public static HazelcastClusterManager getCurrent() {
        return CURRENT;
    }

    public HazelcastClusterManager(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
        this.nodeMap = hazelcast.getMap(NODE_REGISTRY_MAP);
        this.remoteInvoker = new RemoteServiceInvoker(this,
                hazelcast.getExecutorService("cluster-task"),
                DEFAULT_RPC_TIMEOUT);
        // 1. 写入本 JVM 静态持有器（远程任务在目标节点执行时经此取本节点管理器）
        CURRENT = this;
        // 2. 注册本节点到分布式节点注册表
        registerNode();
        // 3. 成员退出 → 清理该节点注册表项（节点异常下线兜底）
        hazelcast.getCluster().addMembershipListener(new MemberRemovedCleaner());
    }

    @Override
    public String getCurrentNodeId() {
        return hazelcast.getCluster().getLocalMember().getUuid().toString();
    }

    @Override
    public ServerNode getCurrentNode() {
        // 构造时已 registerNode() 写入分布式注册表，直接读取本节点项
        return nodeMap.get(getCurrentNodeId());
    }

    @Override
    public List<ServerNode> getClusterNodes() {
        return new ArrayList<>(nodeMap.values());
    }

    @Override
    public ServerNode getServerNode(String nodeId) {
        return nodeMap.get(nodeId);
    }

    /** 按 nodeId 获取 Hazelcast 实时成员（远程提交 submitToMember 用）— 节点已退出返回 null。
     *  ServerNode 不直接存 Member（Member 不可 Java 序列化，无法进分布式 Map），按 UUID 实时解析 */
    @Override
    public Member getServerMember(String nodeId) {
        for (Member member : hazelcast.getCluster().getMembers()) {
            if (member.getUuid().toString().equals(nodeId)) {
                return member;
            }
        }
        return null;
    }

    @Override
    public <T> void register(Class<T> serviceClass, T service) {
        localServices.put(serviceClass, service);
    }

    @Override
    public <T> Mono<T> getService(String nodeId, Class<T> serviceClass) {
        if (nodeId == null || serviceClass == null) {
            return Mono.empty();
        }
        // 本节点 → 真实实例（未注册返回 empty）
        if (nodeId.equals(getCurrentNodeId())) {
            return Mono.justOrEmpty(getLocalService(serviceClass));
        }
        // 远程节点 → 动态代理
        return Mono.just(createRemoteProxy(nodeId, serviceClass));
    }

    // ===== 服务实例获取 =====

    /** 本节点真实实例 — 未注册返回 null（与远程分支 createRemoteProxy 对应的本地分支） */
    @Override
    public <T> T getLocalService(Class<T> serviceClass) {
        return serviceClass.cast(localServices.get(serviceClass));
    }

    // ===== 远程代理 =====

    /** 创建远程代理 — 方法调用经 RemoteServiceInvoker 转发到目标节点执行，调用方无感知 */
    private <T> T createRemoteProxy(String nodeId, Class<T> serviceClass) {
        return serviceClass.cast(Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class<?>[]{serviceClass},
                (proxy, method, args) -> remoteInvoker.invoke(nodeId, serviceClass, method, args)));
    }

    /** 注册本节点到分布式节点注册表 */
    private void registerNode() {
        ServerNode node = new ServerNode();
        node.setNodeId(getCurrentNodeId());
        node.setHost(hazelcast.getCluster().getLocalMember().getAddress().getHost());
        node.setRegisterTime(System.currentTimeMillis());
        nodeMap.put(node.getNodeId(), node);
    }

    // ===== 成员退出清理 =====

    /** 成员退出 → 移除该节点的注册表项（节点异常下线兜底）。内部类，直接访问外部类 nodeMap */
    private class MemberRemovedCleaner implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent event) {
            // 成员加入无需处理 — 其注册表项由该节点自身 registerNode() 写入
            log.info("memberAdded: {} address: {}" , event.getMember().getUuid(), event.getMember().getAddress());
        }

        @Override
        public void memberRemoved(MembershipEvent event) {
            String nodeId = event.getMember().getUuid().toString();
            nodeMap.remove(nodeId);
            // 本地服务注册表无需清理 — 成员退出后 getService 只会对存活节点创建代理
            log.info("memberRemoved: {} address: {}" , event.getMember().getUuid(), event.getMember().getAddress());
        }
    }
}
