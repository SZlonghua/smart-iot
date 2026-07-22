package net.lab1024.sa.admin.module.business.device.listener;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.domain.entity.DeviceEntity;
import net.lab1024.sa.base.common.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 设备 CRUD 事件监听测试用例 — 泛型 + ResolvableTypeProvider，无需 isEntityType 守卫，无需强转。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
@Slf4j
@Component
public class DeviceEventListener {

    @EventListener
    public void onSaveBefore(SaveBeforeEvent<DeviceEntity> event) {
        DeviceEntity device = event.getAfterData();
        log.info("[设备事件] 新增前 — id={}, name={}, productId={}",
                event.getEntityId(), device != null ? device.getName() : "?", device != null ? device.getProductId() : null);
    }

    @EventListener
    public void onSaveAfter(SaveAfterEvent<DeviceEntity> event) {
        DeviceEntity device = event.getAfterData();
        log.info("[设备事件] 新增后 — id={}, name={}, table={}, eventTime={}",
                event.getEntityId(), device != null ? device.getName() : "?", event.getTableName(), event.getEventTime());
    }

    @EventListener
    public void onUpdateBefore(UpdateBeforeEvent<DeviceEntity> event) {
        DeviceEntity before = event.getBeforeData();
        DeviceEntity after = event.getAfterData();
        log.info("[设备事件] 修改前 — id={}, beforeName={}, afterName={}",
                event.getEntityId(),
                before != null ? before.getName() : "null",
                after != null ? after.getName() : "null");
    }

    @EventListener
    public void onUpdateAfter(UpdateAfterEvent<DeviceEntity> event) {
        DeviceEntity before = event.getBeforeData();
        DeviceEntity after = event.getAfterData();
        log.info("[设备事件] 修改后 — id={}, before={}, after={}",
                event.getEntityId(),
                before != null ? before.getName() : "null",
                after != null ? after.getName() : "null");
    }

    @EventListener
    public void onDeleteBefore(DeleteBeforeEvent<DeviceEntity> event) {
        DeviceEntity device = event.getBeforeData();
        log.info("[设备事件] 删除前 — id={}, name={}, physicalDelete={}, status={}",
                event.getEntityId(),
                device != null ? device.getName() : "?",
                event.isPhysicalDelete(),
                device != null ? device.getStatus() : "?");
    }

    @EventListener
    public void onDeleteAfter(DeleteAfterEvent<DeviceEntity> event) {
        log.info("[设备事件] 删除后 — id={}, table={}, physicalDelete={}, eventTime={}",
                event.getEntityId(), event.getTableName(), event.isPhysicalDelete(), event.getEventTime());
    }
}
