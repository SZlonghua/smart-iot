package net.lab1024.sa.base.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import net.lab1024.sa.base.cluster.support.HazelcastClusterManager;
import net.lab1024.sa.base.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 集群管理测试 — 单 JVM 内模拟两个 Hazelcast 节点（TCP-IP 组网，Hazelcast 支持单 JVM 多实例），
 * 覆盖：组网/节点注册表、本节点服务获取、远程同步调用（含目标节点执行断言）、业务异常跨节点回传、
 * Mono/Flux 响应式往返、全节点服务发现、节点退出注册表清理。
 *
 * 说明：生产"一 JVM 一节点"，测试两个实例同 JVM；目标节点取本节点管理器经静态持有器
 * （HazelcastClusterManager.getCurrent()，后构造者覆盖），因此"目标节点执行"断言以执行线程名
 * （hz.&lt;实例名&gt;.cluster-task-*）为准，而非服务实现归属。
 *
 * @Author 廖涛
 * @Date 2026/08/23
 * @Copyright 1024创新实验室
 */
class HazelcastClusterManagerTest {

    /** 集群名 — 两节点必须一致 */
    private static final String CLUSTER_NAME = "cluster-test";

    /** 两节点 TCP 端口（固定端口，portAutoIncrement=false） */
    private static final int PORT_A = 15701;
    private static final int PORT_B = 15702;

    private HazelcastInstance hazelcastA;
    private HazelcastInstance hazelcastB;
    private HazelcastClusterManager managerA;
    private HazelcastClusterManager managerB;
    private String nodeAId;
    private String nodeBId;

    @BeforeEach
    void setUp() {
        hazelcastA = startHazelcast("cluster-test-a", PORT_A);
        hazelcastB = startHazelcast("cluster-test-b", PORT_B);
        // 等待两节点组网完成（TCP-IP 加入有延迟）
        assertTrue(waitUntil(() -> hazelcastA.getCluster().getMembers().size() == 2, 15_000),
                "两节点应在 15s 内完成组网");
        // 构造管理器（构造时写入节点注册表 + 本 JVM 静态持有器 CURRENT = 后构造者）
        managerA = new HazelcastClusterManager(hazelcastA);
        managerB = new HazelcastClusterManager(hazelcastB);
        nodeAId = managerA.getCurrentNodeId();
        nodeBId = managerB.getCurrentNodeId();
    }

    @AfterEach
    void tearDown() {
        if (hazelcastB != null) {
            hazelcastB.shutdown();
        }
        if (hazelcastA != null) {
            hazelcastA.shutdown();
        }
    }

    // ===== 测试用例 =====

    @Test
    void testClusterFormation() {
        // 组网：两节点互相可见，各自节点注册表含 2 个节点
        assertEquals(2, managerA.getClusterNodes().size());
        assertEquals(2, managerB.getClusterNodes().size());
        // 节点信息解析：互查对方节点
        ServerNode nodeA = managerB.getServerNode(nodeAId);
        assertNotNull(nodeA);
        assertEquals(nodeAId, nodeA.getNodeId());
        assertTrue(nodeA.getRegisterTime() > 0);
        // 实时成员解析：按 nodeId 取 Hazelcast 存活成员（submitToMember 远程提交用）
        assertEquals(nodeAId, managerB.getServerMember(nodeAId).getUuid().toString());
        // 本节点信息
        assertEquals(nodeAId, managerA.getCurrentNodeId());
        assertNotNull(managerA.getCurrentNode());
    }

    @Test
    void testLocalService() {
        ClusterTestServiceImpl impl = new ClusterTestServiceImpl();
        managerA.register(ClusterTestService.class, impl);
        // 本节点 getService → 真实实例（非代理）
        ClusterTestService local = managerA.getService(nodeAId, ClusterTestService.class).block();
        assertNotNull(local);
        assertSame(impl, local);
        assertEquals("hello world", local.hello("world").split(" @")[0]);
        // 未注册服务 → 返回 empty → null
        assertNull(managerB.getService(nodeBId, ClusterTestService.class).block());
    }

    @Test
    void testRemoteSyncInvoke() {
        managerA.register(ClusterTestService.class, new ClusterTestServiceImpl());
        managerB.register(ClusterTestService.class, new ClusterTestServiceImpl());
        // 远程获取：本节点真实实例 + 远程节点 JDK 动态代理
        ClusterTestService local = managerB.getService(nodeBId, ClusterTestService.class).block();
        ClusterTestService remote = managerB.getService(nodeAId, ClusterTestService.class).block();
        assertNotNull(local);
        assertNotNull(remote);
        assertNotSame(local, remote);
        assertTrue(Proxy.isProxyClass(remote.getClass()), "远程服务应为 JDK 动态代理");
        // 同步方法跨节点执行 — 返回值正确
        assertEquals("hello world", remote.hello("world").split(" @")[0]);
        assertEquals(5, remote.add(2, 3));
        // 目标节点执行断言：hello 携带执行线程名 — 应为 node A 的 executor 线程（hz.cluster-test-a.cluster-task-*）
        assertTrue(remote.hello("t").contains("cluster-test-a"),
                "远程方法应在目标节点（cluster-test-a）执行器线程上执行, 实际: " + remote.hello("t"));
    }

    @Test
    void testRemoteBusinessException() {
        // 两节点都注册（单 JVM 内目标节点经静态持有器取管理器，后构造者覆盖 — 见类说明）
        managerA.register(ClusterTestService.class, new ClusterTestServiceImpl());
        managerB.register(ClusterTestService.class, new ClusterTestServiceImpl());
        ClusterTestService remote = managerB.getService(nodeAId, ClusterTestService.class).block();
        // 业务异常跨节点回传：调用方收到与目标节点抛出相同的异常类型与消息（recreate() 还原）
        BusinessException ex = assertThrows(BusinessException.class, remote::throwBusinessException);
        assertEquals("业务异常测试", ex.getMessage());
    }

    @Test
    void testRemoteReactiveMono() {
        // 两节点都注册（单 JVM 内目标节点经静态持有器取管理器，后构造者覆盖 — 见类说明）
        managerA.register(ClusterTestService.class, new ClusterTestServiceImpl());
        managerB.register(ClusterTestService.class, new ClusterTestServiceImpl());
        ClusterTestService remote = managerB.getService(nodeAId, ClusterTestService.class).block();
        // Mono 方法：目标节点 block 出真实值 → 调用方包装还原为 Mono → 调用方 block 取值
        assertEquals("async world", remote.asyncHello("world").block());
    }

    @Test
    void testRemoteReactiveFlux() {
        // 两节点都注册（单 JVM 内目标节点经静态持有器取管理器，后构造者覆盖 — 见类说明）
        managerA.register(ClusterTestService.class, new ClusterTestServiceImpl());
        managerB.register(ClusterTestService.class, new ClusterTestServiceImpl());
        ClusterTestService remote = managerB.getService(nodeAId, ClusterTestService.class).block();
        // Flux 方法：目标节点 collectList 出 List → 调用方包装还原为 Flux
        assertEquals(Arrays.asList(1, 2, 3), remote.range(3).collectList().block());
    }

    @Test
    void testGetServicesAcrossNodes() {
        managerA.register(ClusterTestService.class, new ClusterTestServiceImpl());
        managerB.register(ClusterTestService.class, new ClusterTestServiceImpl());
        // 全节点服务发现：本节点真实实例 + 远程节点代理，共 2 个，均可调用
        List<ClusterTestService> services = managerB.getServices(ClusterTestService.class)
                .collectList().block();
        assertNotNull(services);
        assertEquals(2, services.size());
        for (ClusterTestService service : services) {
            assertEquals("hello world", service.hello("world").split(" @")[0]);
            assertEquals(5, service.add(2, 3));
        }
    }

    @Test
    void testMemberRemovedCleanup() {
        managerA.register(ClusterTestService.class, new ClusterTestServiceImpl());
        managerB.register(ClusterTestService.class, new ClusterTestServiceImpl());
        // node A 下线 → node B 的 MemberRemovedCleaner 清理 A 的注册表项
        hazelcastA.shutdown();
        assertTrue(waitUntil(() -> managerB.getServerNode(nodeAId) == null, 10_000),
                "节点退出后注册表项应在 10s 内被清理");
        assertEquals(1, managerB.getClusterNodes().size());
        // 对已退出节点的调用 → 明确业务异常（目标节点不可用）
        ClusterTestService remote = managerB.getService(nodeAId, ClusterTestService.class).block();
        assertNotNull(remote);
        BusinessException ex = assertThrows(BusinessException.class, () -> remote.hello("t"));
        assertTrue(ex.getMessage().contains("目标节点不可用"));
    }

    // ===== 测试辅助 =====

    /** 启动 Hazelcast 实例 — TCP-IP 组网（关组播、固定端口、同一成员列表）；实例名用于执行线程名断言 */
    private static HazelcastInstance startHazelcast(String instanceName, int port) {
        Config config = new Config();
        config.setInstanceName(instanceName);
        config.setClusterName(CLUSTER_NAME);
        config.getNetworkConfig()
                .setPort(port)
                .setPortAutoIncrement(false);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig()
                .setEnabled(true)
                .setMembers(Arrays.asList("127.0.0.1:" + PORT_A, "127.0.0.1:" + PORT_B));
        return Hazelcast.newHazelcastInstance(config);
    }

    /** 轮询等待条件成立（集群事件异步到达，需轮询；避免固定 sleep 造成不稳定） */
    private static boolean waitUntil(BooleanSupplier condition, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    // ===== 测试服务 =====

    /** 测试服务接口 — 覆盖同步返回 / Mono / Flux / 业务异常，供跨节点调用验证 */
    public interface ClusterTestService {

        /** 同步返回 — 返回值携带执行线程名（hz.&lt;实例名&gt;.cluster-task-*），用于"目标节点执行"断言 */
        String hello(String name);

        /** 同步返回 — 基础运算 */
        int add(int a, int b);

        /** 响应式返回 — Mono（目标节点 block 出真实值，调用方还原为 Mono） */
        Mono<String> asyncHello(String name);

        /** 响应式返回 — Flux（目标节点 collectList 出 List，调用方还原为 Flux） */
        Flux<Integer> range(int n);

        /** 业务异常 — 跨节点回传后调用方仍收到 BusinessException */
        void throwBusinessException();
    }

    /** 测试服务实现 — 挂在所属节点管理器；行为与节点无关（值断言），执行位置以线程名断言 */
    public static class ClusterTestServiceImpl implements ClusterTestService {

        @Override
        public String hello(String name) {
            return "hello " + name + " @" + Thread.currentThread().getName();
        }

        @Override
        public int add(int a, int b) {
            return a + b;
        }

        @Override
        public Mono<String> asyncHello(String name) {
            return Mono.just("async " + name);
        }

        @Override
        public Flux<Integer> range(int n) {
            return Flux.range(1, n);
        }

        @Override
        public void throwBusinessException() {
            throw new BusinessException("业务异常测试");
        }
    }
}
