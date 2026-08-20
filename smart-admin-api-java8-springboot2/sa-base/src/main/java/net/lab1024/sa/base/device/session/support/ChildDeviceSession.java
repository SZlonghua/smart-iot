package net.lab1024.sa.base.device.session.support;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.DeviceSessionManager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 子设备会话 — 不持有实际网络连接，通过网关（父设备）代理通信。
 * <p>
 * 持有父设备会话引用，alive/心跳跟随父会话；
 * 携带子设备烧录标识（childProductKey/childDeviceKey），
 * 会话注册后子设备消息直接命中会话，无需重复查询 registry。
 * <p>
 * 构造时监听父会话关闭事件：父连接断开 → 级联移除自己。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/17
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
@Getter
public class ChildDeviceSession extends AbstractDeviceSession {

    /** 父（网关）设备会话 — 连通性依赖 */
    private final DeviceSession parent;

    /** 会话管理器 — close() 时据此移除自己 */
    private final DeviceSessionManager sessionManager;

    /** 幂等保护 — 会话管理器 remove() 内部会再次调用 close()，防止循环和重复清理 */
    private final AtomicBoolean closed = new AtomicBoolean();

    /** 注册在父会话上的关闭监听器 — close() 时注销，否则父会话存活期间本实例被强引用无法回收 */
    private final Consumer<DeviceSession> parentCloseListener = closed -> close();

    public ChildDeviceSession(String deviceId, DeviceOperator operator, Transport transport,
                              String childProductKey, String childDeviceKey, DeviceSession parent,
                              String gatewayId, DeviceSessionManager sessionManager) {
        super(deviceId, operator, transport, childProductKey, childDeviceKey, gatewayId);
        this.parent = parent;
        this.sessionManager = sessionManager;
        // 父会话关闭（父连接断开/被替换）→ 级联关闭自己；close() 幂等 + predicate 防误删后续新会话
        parent.onClose(parentCloseListener);
    }

    @Override
    public long lastPingTime() {
        return parent == null ? 0 : parent.lastPingTime();
    }

    @Override
    public boolean isAlive() {
        return parent != null && parent.isAlive();
    }

    @Override
    public void close() {
        // 幂等 — remove() 内部会再调 close()，第二次进入直接返回，避免循环和重复 notifyClose
        if (closed.getAndSet(true)) {
            return;
        }
        // 注销父会话上的监听器 — 否则父会话存活期间本实例被强引用无法回收（内存泄漏）
        parent.removeOnClose(parentCloseListener);
        // 子设备无实际连接，只移除自己（不能关父连接，否则子会话被替换时会误断网关）
        sessionManager.remove(getDeviceId(), session -> session == this).subscribe();
        notifyClose();
    }
}
