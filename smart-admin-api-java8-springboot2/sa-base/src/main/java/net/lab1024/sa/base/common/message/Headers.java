package net.lab1024.sa.base.common.message;

/**
 * 设备消息头常量枚举 — 与平台统一设备消息定义文档的常用 Headers 约定一致。
 * 当前实现/消费的只有 ASYNC（下发即成功不等回复）与 TIMEOUT（超时覆盖），其余按需启用。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public enum Headers {

    /** 是否异步，boolean 类型 — true 时下发即成功，不等待设备回复 */
    ASYNC("async"),

    /** 指定超时时间（毫秒）— 覆盖默认 10s 下发超时 */
    TIMEOUT("timeout");

    // 以下头字段平台文档已定义，当前未实现，按需启用：
    // /** 分片主消息 ID — 为下发消息的 messageId */  FRAG_MSG_ID("frag_msg_id"),
    // /** 分片总数 */                                FRAG_NUM("frag_num"),
    // /** 当前分片索引 */                            FRAG_PART("frag_part"),
    // /** 是否为最后一个分片 */                      FRAG_LAST("frag_last"),
    // /** 保持设备一直在线 — 与 DeviceOnlineMessage 配合 */            KEEP_ONLINE("keepOnline"),
    // /** 在线超时时间（秒） */                      KEEP_ONLINE_TIMEOUT_SECONDS("keepOnlineTimeoutSeconds"),
    // /** 不存储此消息数据 */                        IGNORE_STORAGE("ignoreStorage"),
    // /** 不记录此消息到日志 */                      IGNORE_LOG("ignoreLog"),
    // /** 合并最新属性数据 */                        MERGE_LATEST("mergeLatest");

    private final String value;

    Headers(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
