package net.lab1024.sa.base.cluster.support.task;

import lombok.Data;
import net.lab1024.sa.base.cluster.ClusterManager;
import net.lab1024.sa.base.cluster.support.HazelcastClusterManager;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * 远程调用任务 — 目标节点执行单元（Spring 传输类版）：
 * 构造时由 Method/args 拆解出可序列化的 RemoteInvocation（方法名/参数类型/参数值），
 * 目标节点执行：取本节点注册的服务实例 → 反射调用 → Mono/Flux block 出真实结果 → RemoteInvocationResult 回传。
 * 调用方侧的结果处理（recreate() 还原业务异常 / Mono/Flux 包装）在 RemoteServiceInvoker.handleResult。
 * 传输层用 Spring 现成类（RemoteInvocation/RemoteInvocationResult，spring-context 自带，零新增依赖），
 * 代价是业务参数/返回值需实现 Serializable（Java 序列化跨节点传输）。
 *
 * @Author 廖涛
 * @Date 2026/08/23
 * @Copyright 1024创新实验室
 */
@Data
public class RemoteInvokeTask implements Callable<RemoteInvocationResult>, Serializable {

    /** 目标节点 */
    private final String nodeId;

    /** 本节点集群管理器（调用方注入；transient 不参与跨节点序列化 — 目标节点经 HazelcastClusterManager.getCurrent() 取本节点管理器） */
    private final transient ClusterManager clusterManager;

    /** 服务接口类型（Class 可序列化，目标节点按接口取本地注册实例） */
    private final Class<?> serviceClass;

    /** 可序列化方法载体：方法名/参数类型/参数值（Spring 传输类） */
    private final RemoteInvocation invocation;

    public RemoteInvokeTask(String nodeId, ClusterManager clusterManager,
                            Class<?> serviceClass, Method method, Object[] args) {
        this.nodeId = nodeId;
        this.clusterManager = clusterManager;
        this.serviceClass = serviceClass;
        this.invocation = new RemoteInvocation(method.getName(), method.getParameterTypes(), args);
    }

    @Override
    public RemoteInvocationResult call() {
        // 目标节点：取本节点 ClusterManager（transient 字段不可达，经本节点管理器静态持有器）→ 取本地注册的真实服务
        ClusterManager manager = clusterManager;
        if (manager == null) {
            manager = HazelcastClusterManager.getCurrent();
        }
        if (manager == null) {
            return new RemoteInvocationResult(new IllegalStateException("本节点未装配集群管理器"));
        }
        Object service = manager.getLocalService(serviceClass);
        if (service == null) {
            return new RemoteInvocationResult(new IllegalStateException("目标节点未注册服务: " + serviceClass));
        }
        try {
            // Spring ReflectionUtils 定位方法（兼容接口方法）→ 反射调用（业务异常原样抛出）
            Method targetMethod = resolveMethod(service.getClass());
            Object result = ReflectionUtils.invokeMethod(targetMethod, service, invocation.getArguments());
            // 返回 Mono/Flux → block 出真实结果（业务完整执行发生在目标节点）
            if (result instanceof Mono) {
                result = ((Mono<?>) result).block();
            } else if (result instanceof Flux) {
                result = ((Flux<?>) result).collectList().block();
            }
            return new RemoteInvocationResult(result);
        } catch (Throwable t) {
            // 业务异常（如 BusinessException 设备离线/超时）随结果回传，调用方 recreate() 还原
            return new RemoteInvocationResult(t);
        }
    }

    /** 定位方法 — findMethod 兼容接口方法（自动搜索类层次：实现类 → 接口 → 父类） */
    private Method resolveMethod(Class<?> targetClass) throws NoSuchMethodException {
        Method method = ReflectionUtils.findMethod(targetClass, invocation.getMethodName(), invocation.getParameterTypes());
        if (method == null) {
            throw new NoSuchMethodException(serviceClass + "#" + invocation.getMethodName()
                    + "(" + Arrays.toString(invocation.getParameterTypes()) + ")");
        }
        return method;
    }
}
