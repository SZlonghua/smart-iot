package net.lab1024.sa.base.cluster.support.task;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.IExecutorService;
import net.lab1024.sa.base.cluster.ClusterManager;
import net.lab1024.sa.base.common.exception.BusinessException;
import org.springframework.remoting.support.RemoteInvocationResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 远程服务调用器 — 在目标节点调用服务方法并取回结果（调用方侧全流程）。
 * 流程：构造 RemoteInvokeTask（内部打包 RemoteInvocation 参数载体）→ 提交到目标节点（IExecutorService）执行 →
 * 阻塞等待结果 → 结果处理（recreate() 还原业务异常 / 取出返回值 → 按返回类型包装回 Mono/Flux）。
 * 供远程代理（HazelcastClusterManager.createRemoteProxy）的 InvocationHandler 使用，业务方无感知。
 *
 * @Author 廖涛
 * @Date 2026/08/23
 * @Copyright 1024创新实验室
 */
public class RemoteServiceInvoker {

    private final ClusterManager clusterManager;   // 集群管理器（节点注册表 + 远程调用器）
    private final IExecutorService executorService;   // 远程执行器（提交任务到目标节点执行）
    private final long timeoutMs;                           // 远程调用超时（毫秒）

    public RemoteServiceInvoker(ClusterManager clusterManager,
                                IExecutorService executorService,
                                long timeoutMs) {
        this.clusterManager = clusterManager;
        this.executorService = executorService;
        this.timeoutMs = timeoutMs;
    }

    /**
     * 在目标节点调用服务方法并返回结果 — 远程代理 InvocationHandler 唯一入口
     *
     * @param nodeId       目标节点 ID
     * @param serviceClass 服务接口（目标节点按接口取本地注册实例）
     * @param method       被调用的方法（返回类型用于 Mono/Flux 包装还原）
     * @param args         方法参数
     * @return 返回值（Mono/Flux 还原为响应式形态）
     */
    public Object invoke(String nodeId, Class<?> serviceClass, Method method, Object[] args) {
        // 方法调用 → 构造远程任务（内部打包参数载体）→ 提交到目标节点执行 → 结果处理（handleResult）
        RemoteInvokeTask task = new RemoteInvokeTask(nodeId,
                clusterManager,
                serviceClass, method, args);
        return handleResult(submit(task, memberOf(nodeId)), method);
    }

    // ===== 远程提交 =====

    /** 提交任务到目标节点并阻塞等待结果 — 超时/中断/执行失败统一转为业务异常 */
    private RemoteInvocationResult submit(RemoteInvokeTask task, Member member) {
        try {
            return executorService.submitToMember(task, member).get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new BusinessException("远程调用超时: " + task.getNodeId());
        } catch (ExecutionException e) {
            throw new BusinessException("远程调用失败: " + e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("远程调用被中断: " + task.getNodeId());
        }
    }

    /** 目标节点成员 — 经集群管理器按 nodeId 解析 Hazelcast 实时成员（ServerNode 不存 Member）；节点不存在/已退出则报错 */
    private Member memberOf(String nodeId) {
        Member member = clusterManager.getServerMember(nodeId);
        if (member == null) {
            throw new BusinessException("目标节点不可用: " + nodeId);
        }
        return member;
    }

    // ===== 结果处理 =====

    /** 结果处理（调用方侧）：recreate() 有异常直接抛出、无异常返回 value；成功后按方法返回类型包装回 Mono/Flux 响应式形态 */
    private Object handleResult(RemoteInvocationResult result, Method method) {
        Object value;
        try {
            value = result.recreate();
        } catch (Throwable t) {
            throw asRuntimeException(t);
        }
        return wrapReactive(value, method);
    }

    /** 异常还原（调用方侧）：RuntimeException/Error 原样抛出，checked 异常包装为 RuntimeException */
    private static RuntimeException asRuntimeException(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        return new RuntimeException("远程调用失败: " + t, t);
    }

    /** Mono/Flux 包装还原（调用方侧）：远程已 block 出真实对象（Java 序列化还原），这里按方法返回类型还原为响应式形态 */
    private static Object wrapReactive(Object value, Method method) {
        Class<?> returnType = method.getReturnType();
        // Flux 方法：远程已 collectList → List 包装为 Flux
        if (Flux.class.isAssignableFrom(returnType)) {
            return Flux.fromIterable(value == null ? Collections.emptyList() : (List<?>) value);
        }
        if (Mono.class.isAssignableFrom(returnType)) {
            return Mono.justOrEmpty(value);
        }
        return value;
    }
}
