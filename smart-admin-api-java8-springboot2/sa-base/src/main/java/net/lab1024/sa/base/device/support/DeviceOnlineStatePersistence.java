package net.lab1024.sa.base.device.support;

import net.lab1024.sa.base.device.session.DeviceSession;

/**
 * 设备在线状态持久化 — 会话注册/注销时更新数据库 device 表。
 * <p>
 * 由业务模块（sa-admin）实现注入；无实现时仅缓存在线状态（Redis），不落库。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/17
 * &#064;Copyright  1024创新实验室
 */
public interface DeviceOnlineStatePersistence {

    /**
     * 设备上线 — 更新 status/last_online_time（子设备会话额外更新 gateway_id）
     *
     * @param session 会话
     */
    void onOnline(DeviceSession session);

    /**
     * 设备下线 — 更新 status（子设备会话清空 gateway_id）
     *
     * @param session 会话
     */
    void onOffline(DeviceSession session);
}
