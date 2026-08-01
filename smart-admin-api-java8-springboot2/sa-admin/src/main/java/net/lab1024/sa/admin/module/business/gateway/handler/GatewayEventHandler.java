package net.lab1024.sa.admin.module.business.gateway.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.gateway.domain.entity.GatewayEntity;
import net.lab1024.sa.base.common.event.DeleteAfterEvent;
import net.lab1024.sa.base.common.event.SaveAfterEvent;
import net.lab1024.sa.base.common.event.UpdateAfterEvent;
import net.lab1024.sa.base.common.gateway.DeviceGatewayEvent;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 网关事件处理器 — 监听 GatewayEntity CRUD 事件，转化为 DeviceGatewayEvent 发布。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
@Slf4j
@Component
public class GatewayEventHandler {

    @Resource
    private IEventBus eventBus;

    /** 新增 + 启用 → register */
    @EventListener
    public void onSaved(SaveAfterEvent<GatewayEntity> event) {
        GatewayEntity entity = event.getAfterData();
        if (entity == null || !entity.isEnabled()) return;
        eventBus.publish(DeviceGatewayEvent.of(DeviceGatewayEvent.Type.register, entity.toProperties()));
    }

    /** 修改 → 启用=reload / 禁用=unregister */
    @EventListener
    public void onUpdated(UpdateAfterEvent<GatewayEntity> event) {
        GatewayEntity entity = event.getAfterData();
        if (entity == null) return;
        DeviceGatewayEvent.Type type = entity.isEnabled()
                ? DeviceGatewayEvent.Type.reload
                : DeviceGatewayEvent.Type.unregister;
        eventBus.publish(DeviceGatewayEvent.of(type, entity.toProperties()));
    }

    /** 删除 → unregister */
    @EventListener
    public void onDeleted(DeleteAfterEvent<GatewayEntity> event) {
        DeviceGatewayProperties props = new DeviceGatewayProperties();
        props.setId(String.valueOf(event.getEntityId()));
        eventBus.publish(DeviceGatewayEvent.of(DeviceGatewayEvent.Type.unregister, props));
    }
}
