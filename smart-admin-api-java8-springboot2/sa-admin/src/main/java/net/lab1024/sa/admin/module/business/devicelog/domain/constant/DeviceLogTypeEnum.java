package net.lab1024.sa.admin.module.business.devicelog.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;
import net.lab1024.sa.base.common.topic.TopicMessageCodec;

/**
 * 设备日志类型 枚举 — 值即 TopicMessageCodec 枚举名（与存储 commandType、前端枚举同源），
 * 查询多选直传无需映射；命令与回复成对相邻（回复为名称带 Reply 后缀的上行消息）
 *
 * @Author 廖涛
 * @Date 2026/06/20
 * @Copyright 1024创新实验室
 */
@AllArgsConstructor
@Getter
public enum DeviceLogTypeEnum implements BaseEnum {

    ONLINE(TopicMessageCodec.online.name(), "上线"),
    OFFLINE(TopicMessageCodec.offline.name(), "离线"),
    REGISTER(TopicMessageCodec.register.name(), "注册"),
    UNREGISTER(TopicMessageCodec.unregister.name(), "注销"),
    DISCONNECT(TopicMessageCodec.disconnect.name(), "断开连接"),
    DISCONNECT_REPLY(TopicMessageCodec.disconnectReply.name(), "断开连接回复"),
    PROPERTIES_REPORT(TopicMessageCodec.reportProperty.name(), "属性上报"),
    PROPERTIES_READ(TopicMessageCodec.readProperty.name(), "读取属性"),
    PROPERTIES_READ_REPLY(TopicMessageCodec.readPropertyReply.name(), "读取属性回复"),
    PROPERTIES_WRITE(TopicMessageCodec.writeProperty.name(), "设置属性"),
    PROPERTIES_WRITE_REPLY(TopicMessageCodec.writePropertyReply.name(), "设置属性回复"),
    EVENT(TopicMessageCodec.event.name(), "事件"),
    COMMAND(TopicMessageCodec.functionInvoke.name(), "命令"),
    COMMAND_REPLY(TopicMessageCodec.functionInvokeReply.name(), "命令回复"),
    ;

    private final String value;
    private final String desc;
}
