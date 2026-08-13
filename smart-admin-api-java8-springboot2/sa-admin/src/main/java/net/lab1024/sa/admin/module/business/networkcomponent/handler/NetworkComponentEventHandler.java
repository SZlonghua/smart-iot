package net.lab1024.sa.admin.module.business.networkcomponent.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.gateway.dao.GatewayDao;
import net.lab1024.sa.admin.module.business.gateway.service.GatewayService;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.entity.NetworkComponentEntity;
import net.lab1024.sa.base.common.event.DeleteAfterEvent;
import net.lab1024.sa.base.common.event.DeleteBeforeEvent;
import net.lab1024.sa.base.common.event.SaveAfterEvent;
import net.lab1024.sa.base.common.event.UpdateAfterEvent;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.network.NetworkConfigEvent;
import net.lab1024.sa.base.common.network.NetworkProperties;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 网络组件事件处理器 — 监听 NetworkComponentEntity CRUD 事件，发布 NetworkConfigEvent。
 *
 * @Author 廖涛
 * @Date 2026/08/01
 * @Copyright 1024创新实验室
 */
@Slf4j
@Component
public class NetworkComponentEventHandler {

    @Resource
    private IEventBus eventBus;

    @Resource
    private GatewayService gatewayService;

    /** 新增 → reload */
    @EventListener
    public void onSaved(SaveAfterEvent<NetworkComponentEntity> event) {
        NetworkComponentEntity entity = event.getAfterData();
        if (entity == null || !entity.isEnabled()) return;
        eventBus.publish(NetworkConfigEvent.of(NetworkConfigEvent.Type.reload, entity.toProperties()));
        log.info("[NetworkEventHandler] 发布 reload 事件(新增) — id={}", entity.getId());
    }

    /** 修改 → 启用=reload / 禁用=destroy */
    @EventListener
    public void onUpdated(UpdateAfterEvent<NetworkComponentEntity> event) {
        NetworkComponentEntity entity = event.getAfterData();
        if (entity == null) return;
        NetworkConfigEvent.Type type = entity.isEnabled()
                ? NetworkConfigEvent.Type.reload
                : NetworkConfigEvent.Type.destroy;
        eventBus.publish(NetworkConfigEvent.of(type, entity.toProperties()));
        log.info("[NetworkEventHandler] 发布 {} 事件(修改) — id={}", type, entity.getId());
    }

    /** 删除前 → 校验网关引用 */
    @EventListener
    public void onBeforeDelete(DeleteBeforeEvent<NetworkComponentEntity> event) {
        // TODO: 查询引用此组件的网关数量
         long refCount = gatewayService.countByComponentId(event.getEntityId());
         if (refCount > 0) throw new BusinessException("该网络组件被 " + refCount + " 个网关引用，无法删除");
    }

    /** 删除后 → destroy */
    @EventListener
    public void onDeleted(DeleteAfterEvent<NetworkComponentEntity> event) {
        NetworkComponentEntity entity = event.getBeforeData();
        if (entity == null) return;
        eventBus.publish(NetworkConfigEvent.of(NetworkConfigEvent.Type.destroy, entity.toProperties()));
        log.info("[NetworkEventHandler] 发布 destroy 事件(删除) — id={}", event.getEntityId());
    }
}
