package net.lab1024.sa.base.common.message;

import javax.annotation.Nullable;

/**
 * 回复消息 — 带成功标志和错误信息的消息。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
public interface MessageReply extends Message {

    boolean isSuccess();

    @Nullable
    String getCode();

    @Nullable
    String getMessage();
}
