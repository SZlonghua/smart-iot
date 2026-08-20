package net.lab1024.sa.base.common.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Topic + payload 载体 — 编码结果的统一承载。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class TopicPayload {

    private String topic;

    private byte[] payload;
}
