package net.lab1024.sa.admin.module.business.device.listener;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.domain.constant.DeviceStatusEnum;
import net.lab1024.sa.admin.module.business.device.domain.entity.DeviceEntity;
import net.lab1024.sa.admin.module.business.device.manager.DeviceManager;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.support.ChildDeviceSession;
import net.lab1024.sa.base.device.support.DeviceOnlineStatePersistence;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 设备在线状态持久化实现 — 会话注册/注销时更新数据库 device 表。
 * <p>
 * - 子设备会话：更新 status/last_online_time/gateway_id（挂到网关）
 * - 直连设备会话：更新 status/last_online_time
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/17
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
@Component
public class DeviceOnlineStatePersistenceImpl implements DeviceOnlineStatePersistence {

    @Resource
    private DeviceManager deviceManager;

    /**
     * 会触发CRUD事件但是不会触发deviceRegistry.register 因为wrapper更新返回的数据event.getAfterData()都是null 避开数据前 数据后查询数据库提升效率 原因是获取不到主键id
     * 这里正好避开deviceRegistry.register从而避开redis操作 提升效率
     * <p>
     *
     * update(wrapper) 会发布 UpdateAfterEvent，但 wrapper 参数拿不到实体 ID → afterData 为 null → DeviceConfigsHandler 里
     * if (device == null) return 直接跳过，不会触发 register。只有当持久化改成 updateById(entity) 时事件才带真实数据，register 才会被触发
     * @param session 会话
     */
    @Override
    public void onOnline(DeviceSession session) {
        LambdaUpdateWrapper<DeviceEntity> wrapper = Wrappers.lambdaUpdate(DeviceEntity.class)
                .eq(DeviceEntity::getId, Long.valueOf(session.getDeviceId()))
                .set(DeviceEntity::getStatus, DeviceStatusEnum.ONLINE.getValue())
                .set(DeviceEntity::getLastOnlineTime, LocalDateTime.now());
        // 网关实例ID（DeviceGateway.getId()，DB gateway 表主键字符串）→ gateway_id
        if (session.getGatewayId() != null) {
            wrapper.set(DeviceEntity::getGatewayId, Long.valueOf(session.getGatewayId()));
        }
        // 子设备 → 父设备 ID → parent_device_id
        if (session instanceof ChildDeviceSession) {
            DeviceSession parent = ((ChildDeviceSession) session).getParent();
            if (parent != null) {
                wrapper.set(DeviceEntity::getParentDeviceId, Long.valueOf(parent.getDeviceId()));
            }
        }
        deviceManager.update(wrapper);
        log.info("[设备状态] 上线 — deviceId={}, gatewayId={}, parentDeviceId={}",
                session.getDeviceId(), session.getGatewayId(),
                session instanceof ChildDeviceSession ? ((ChildDeviceSession) session).getParent().getDeviceId() : null);
    }

    @Override
    public void onOffline(String deviceId) {
        LambdaUpdateWrapper<DeviceEntity> wrapper = Wrappers.lambdaUpdate(DeviceEntity.class)
                .eq(DeviceEntity::getId, Long.valueOf(deviceId))
                // 幂等护栏 — 仅当 DB 仍为"在线"时才落离线（设备可能已重连/已被正常下线链路清理）
                .eq(DeviceEntity::getStatus, DeviceStatusEnum.ONLINE.getValue())
                .set(DeviceEntity::getStatus, DeviceStatusEnum.OFFLINE.getValue())
                // 无会话对象无法判断子设备 — 网关/父设备关联无条件清空
                .set(DeviceEntity::getGatewayId, null)
                .set(DeviceEntity::getParentDeviceId, null);
        deviceManager.update(wrapper);
        log.info("[设备状态] 下线 — deviceId={}", deviceId);
    }
}
