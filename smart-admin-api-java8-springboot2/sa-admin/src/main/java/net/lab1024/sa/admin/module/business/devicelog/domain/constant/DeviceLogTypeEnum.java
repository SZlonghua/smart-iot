package net.lab1024.sa.admin.module.business.devicelog.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 设备日志类型 枚举
 *
 * @Author 廖涛
 * @Date 2026/06/20
 * @Copyright 1024创新实验室
 */
@AllArgsConstructor
@Getter
public enum DeviceLogTypeEnum implements BaseEnum {

    ONLINE("online", "上线"),
    OFFLINE("offline", "离线"),
    REGISTER("register", "注册"),
    UNREGISTER("unregister", "注销"),
    DISCONNECT("disconnect", "断开连接"),
    PROPERTIES_REPORT("properties_report", "属性上报"),
    PROPERTIES_READ("properties_read", "读取属性"),
    PROPERTIES_WRITE("properties_write", "设置属性"),
    EVENT("event", "事件"),
    COMMAND("command", "命令"),
    ;

    private final String value;
    private final String desc;
}
