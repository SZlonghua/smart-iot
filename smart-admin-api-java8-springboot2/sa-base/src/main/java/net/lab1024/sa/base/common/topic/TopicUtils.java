package net.lab1024.sa.base.common.topic;

/**
 * Topic 匹配工具 — `:param` 通配符逐段匹配。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
public final class TopicUtils {

    private TopicUtils() {
    }

    /**
     * pattern 与 topic 逐段比对：
     * <ul>
     *   <li>":xxx" 段匹配任意值（命名参数，Vert.x 原生语法）</li>
     *   <li>"*" 段（仅尾部）匹配剩余全部路径</li>
     * </ul>
     */
    public static boolean match(String[] pattern, String[] topic) {
        if (pattern.length > topic.length) {
            return false;
        }
        for (int i = 0; i < pattern.length; i++) {
            String p = pattern[i];
            if (p.startsWith(":")) {
                continue;
            }
            if ("*".equals(p)) {
                return true;
            }
            if (!p.equals(topic[i])) {
                return false;
            }
        }
        return pattern.length == topic.length;
    }

    /** 字符串入口：pattern 和 topic 以 "/" 分段后匹配 */
    public static boolean match(String pattern, String topic) {
        return match(pattern.split("/"), topic.split("/"));
    }
}
