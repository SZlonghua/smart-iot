package net.lab1024.sa.base.storage.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息日志业务模型（继承 {@link StorageData} 公共字段）— 平台下发与设备回复双向，一条消息一个对象。
 * <p>
 * 时序库只追加绝不 UPDATE：下发行与回复行各自独立（同 messageId 关联），
 * 执行结果（success/code/message）含在回复行 content JSON 内，不单独落列。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageLogData extends StorageData {

    /** 设备上报（回复） */
    public static final String DIRECTION_UP = "up";

    /** 平台下发 */
    public static final String DIRECTION_DOWN = "down";

    /** 方向 up=设备回复 / down=平台下发 */
    private String direction;

    /** 命令类型 = TopicMessageCodec 枚举名（readProperty/writeProperty/functionInvoke/disconnect，回复追加 Reply 后缀） */
    private String commandType;

    /** 消息内容 JSON 字符串（整条消息全量序列化，含 headers 不清空） */
    private String content;
}
