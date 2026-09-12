package net.lab1024.sa.admin.module.business.device.storage.listener;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.storage.service.MessageLogDataService;
import net.lab1024.sa.base.common.message.AbstractDeviceMessage;
import net.lab1024.sa.base.common.message.ChildDeviceMessage;
import net.lab1024.sa.base.common.message.ChildDeviceMessageReply;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.common.topic.TopicMessageCodec;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.module.support.json.JsonUtil;
import net.lab1024.sa.base.storage.model.MessageLogData;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 消息日志存储监听器 — 平台下发（down）/设备回复（up）命令日志 → MessageLogData 的完整转换
 * （含设备上下文回填）在本类一次完成。
 * <p>
 * 单方法 @EventListener(AbstractDeviceMessage) 覆盖全部设备消息（14 类日志消息 + 子设备包装均继承
 * AbstractDeviceMessage）：类型判定（direction/commandType、子设备递归解包取内层）与设备上下文解析后
 * 转换全字段填齐，结果直接交数据服务落库（{@link MessageLogDataService#store} 只做策略路由 + 持久化，
 * 不再回填）。commandType = TopicMessageCodec 枚举名（与 DeviceLogTypeEnum、前端枚举同源）：命令族
 * readProperty/writeProperty/functionInvoke/disconnect、回复带 Reply 后缀，属性/事件上报、注册/上下线等
 * 按各自枚举名落库（不同消息类型的 content 结构不同，查询侧按类型区分）。时序库只追加绝不 UPDATE：
 * 下发行与回复行各自独立，同 messageId 关联还原命令生命周期，执行结果含在回复行 content JSON 内。
 * 任何异常仅记日志，绝不阻断消息流。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/09/07
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
@Component
public class MessageLogStorageListener extends CommonStorageListener {

    public MessageLogStorageListener(DeviceRegistry deviceRegistry, MessageLogDataService messageLogDataService) {
        super(deviceRegistry);
        this.messageLogDataService = messageLogDataService;
    }

    private final MessageLogDataService messageLogDataService;

    /** 全部设备消息入口 — 类型判定（无法匹配 codec 跳过）→ 内层消息 ignore 跳过 → 上下文解析（设备不存在跳过）→ 完整转换 → 服务 store */
    @EventListener
    public void onDeviceMessage(AbstractDeviceMessage message) {
        try {
            MessageLogType type = dispatchMessageLog(message);
            if (type == null) {
                return;
            }
            AbstractDeviceMessage logMessage = type.getMessage();
            if (ignoreStorage(logMessage)) {
                return;
            }
            StoreContext context = resolveStoreContext(logMessage.getDeviceId());
            if (context == null) {
                log.warn("消息日志设备不存在或未绑定产品，跳过存储: deviceId={}", logMessage.getDeviceId());
                return;
            }
            messageLogDataService.store(toMessageLogData(context, type, logMessage));
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("消息日志存储异常，跳过: {}", e.getMessage());
        }
    }

    /**
     * 消息日志类型判定 — 子设备包装递归解包至内层（判定结果携带内层消息：deviceId/messageId/timestamp/headers
     * 均取内层，外层是网关三元与网关连接信息）后，经 {@link TopicMessageCodec} 统一判定：
     * direction = 枚举 upstream 标记（true 上行 up / false 下行 down），commandType = 枚举名
     * （全部消息类型落库，回复带 Reply 后缀）；无法匹配 codec → null 跳过
     */
    private static MessageLogType dispatchMessageLog(AbstractDeviceMessage message) {
        if (message instanceof ChildDeviceMessage) {
            Message inner = ((ChildDeviceMessage<?>) message).getChildDeviceMessage();
            return inner instanceof AbstractDeviceMessage ? dispatchMessageLog((AbstractDeviceMessage) inner) : null;
        }
        if (message instanceof ChildDeviceMessageReply) {
            Message inner = ((ChildDeviceMessageReply<?>) message).getChildDeviceMessage();
            return inner instanceof AbstractDeviceMessage ? dispatchMessageLog((AbstractDeviceMessage) inner) : null;
        }
        return TopicMessageCodec.fromMessage(message)
                .map(codec -> new MessageLogType(message,
                        codec.isUpstream() ? MessageLogData.DIRECTION_UP : MessageLogData.DIRECTION_DOWN,
                        codec.name()))
                .orElse(null);
    }

    /** 消息 → MessageLogData 完整转换 — 判定消息（子设备为解包后内层）为 deviceId/messageId 来源，上下文回填 productId/deviceName（timestamp 取报文消息时间，落 create_time 列） */
    private MessageLogData toMessageLogData(StoreContext context, MessageLogType type, AbstractDeviceMessage logMessage) {
        MessageLogData data = new MessageLogData();
        data.setDeviceId(logMessage.getDeviceId());
        data.setProductId(context.getProduct().getId());
        data.setDeviceName(context.getDeviceName());
        data.setMessageId(logMessage.getMessageId());
        data.setTimestamp(logMessage.getTimestamp());
        data.setDirection(type.getDirection());
        data.setCommandType(type.getCommandType());
        data.setContent(JsonUtil.toJson(logMessage));
        return data;
    }

    /** 消息日志类型判定结果（message = 子设备解包后的内层消息，直连设备即自身） */
    @Getter
    private static class MessageLogType {

        private final AbstractDeviceMessage message;
        private final String direction;
        private final String commandType;

        MessageLogType(AbstractDeviceMessage message, String direction, String commandType) {
            this.message = message;
            this.direction = direction;
            this.commandType = commandType;
        }
    }
}
