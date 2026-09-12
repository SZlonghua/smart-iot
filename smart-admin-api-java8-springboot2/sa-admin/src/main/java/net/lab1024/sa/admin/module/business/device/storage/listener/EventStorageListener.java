package net.lab1024.sa.admin.module.business.device.storage.listener;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.storage.service.DeviceEventDataService;
import net.lab1024.sa.base.common.message.ChildDeviceMessage;
import net.lab1024.sa.base.common.message.EventMessage;
import net.lab1024.sa.base.device.DeviceProductOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.metadata.EventMetadata;
import net.lab1024.sa.base.metadata.ThingsMetadata;
import net.lab1024.sa.base.metadata.type.DataType;
import net.lab1024.sa.base.module.support.json.JsonUtil;
import net.lab1024.sa.base.storage.model.EventData;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 事件上报存储监听器 — 消息 → EventData 的完整转换（含设备上下文与物模型回填）在本类一次完成。
 * <p>
 * productId/deviceName 只能来自设备上下文，type（info/warning/error）/dataType 只能来自产品物模型
 * （事件未定义 → 告警跳过不落库）—— 本类负责解析后转换并全字段填齐，转换结果直接交数据服务落库
 * （{@link DeviceEventDataService#store} 只做策略路由 + 持久化，不再回填）。事件按原始消息存储，
 * 不等同告警日志。任何异常仅记日志，绝不阻断消息流。
 * <p>
 * Spring @EventListener 经事件总线桥接收上行消息（publishAsync 已在异步线程执行，本类内 block 不阻塞
 * 消息处理链路）；子设备（网关代理）上报经泛型精准匹配单独入口（ChildDeviceMessage 实现
 * ResolvableTypeProvider），解包后按内层消息处理（headers/deviceId 均取内层）。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/09/07
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
@Component
public class EventStorageListener extends CommonStorageListener {

    public EventStorageListener(DeviceRegistry deviceRegistry, DeviceEventDataService eventDataService) {
        super(deviceRegistry);
        this.eventDataService = eventDataService;
    }
    private final DeviceEventDataService eventDataService;

    /** 事件上报（设备直连） */
    @EventListener
    public void onEventMessage(EventMessage message) {
        handle(message);
    }

    /** 子设备事件上报 — 泛型精准匹配，无需 instanceof */
    @EventListener
    public void onChildDeviceMessage(ChildDeviceMessage<EventMessage> wrapper) {
        handle(wrapper.getChildDeviceMessage());
    }

    /** 重复逻辑收敛点 — ignore 跳过 → 上下文/物模型解析（设备或事件缺失跳过）→ 完整转换 → 服务 store */
    private void handle(EventMessage message) {
        if (ignoreStorage(message)) {
            return;
        }
        try {
            StoreContext context = resolveStoreContext(message.getDeviceId());
            if (context == null) {
                log.warn("事件上报设备不存在或未绑定产品，跳过存储: deviceId={}", message.getDeviceId());
                return;
            }
            EventMetadata eventMetadata = eventMetadata(context.getProduct(), message.getEvent());
            if (eventMetadata == null) {
                log.warn("事件 [{}] 未在产品物模型中定义，跳过存储: deviceId={}", message.getEvent(), message.getDeviceId());
                return;
            }
            eventDataService.store(toEventData(context, eventMetadata, message));
        } catch (Exception e) {
            log.warn("事件上报存储异常，跳过: {}", e.getMessage());
        }
    }


    /** 产品物模型中事件定义（产品无物模型 → null） */
    private static EventMetadata eventMetadata(DeviceProductOperator product, String event) {
        ThingsMetadata metadata = product.getMetadata().block();
        return metadata == null ? null : metadata.getEventOrNull(event);
    }

    /** 消息 → EventData 完整转换 — 上下文回填 productId/deviceName，物模型回填 type/dataType，全字段一次填齐（timestamp 取报文消息时间，落 create_time 列） */
    private EventData toEventData(StoreContext context, EventMetadata eventMetadata, EventMessage message) {
        EventData data = new EventData();
        data.setDeviceId(message.getDeviceId());
        data.setProductId(context.getProduct().getId());
        data.setDeviceName(context.getDeviceName());
        data.setMessageId(message.getMessageId());
        data.setTimestamp(message.getTimestamp());
        data.setEvent(message.getEvent());
        data.setType(eventMetadata.getType());
        DataType valueType = eventMetadata.getValueType();
        data.setDataType(valueType == null ? null : valueType.getType());
        data.setData(JsonUtil.toJson(message));
        return data;
    }

}
