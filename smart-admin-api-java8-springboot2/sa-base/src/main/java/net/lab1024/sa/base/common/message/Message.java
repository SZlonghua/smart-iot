package net.lab1024.sa.base.common.message;

import org.apache.commons.collections.MapUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface Message extends Serializable {

    /**
     * 消息的唯一标识,用于在请求响应模式下对请求和响应进行关联.
     * <p>
     * 注意: 此消息ID为全系统唯一. 但是在很多情况下,设备可能不支持此类型的消息ID,
     * 此时需要在协议包中做好映射关系,比如使用:{@link java.util.concurrent.ConcurrentHashMap}进行消息绑定.
     *
     * @return 消息ID
     */
    String getMessageId();

    /**
     * @return 毫秒时间戳
     * @see System#currentTimeMillis()
     */
    long getTimestamp();

    /**
     * 消息头,用于自定义一些消息行为
     *
     * @return headers or null
     */
    @Nullable
    Map<String, Object> getHeaders();


    /**
     * 添加一个header
     *
     * @param header header
     * @param value  value
     * @return this
     */
    Message addHeader(String header, Object value);

    /**
     * 添加header,如果header已存在则放弃
     *
     * @param header header key
     * @param value  header 值
     * @return this
     */
    Message addHeaderIfAbsent(String header, Object value);

    Object computeHeader(String key, BiFunction<String, Object, Object> computer);


    /**
     * 删除一个header
     *
     * @param header header
     * @return this
     */
    Message removeHeader(String header);

    default Object getHeaderOrElse(String header, @Nullable Supplier<Object> orElse) {
        Map<String, Object> headers = getHeaders();
        if (MapUtils.isEmpty(headers) || header == null) {
            return orElse == null ? null : orElse.get();
        }
        Object val = headers.get(header);
        if (val != null) {
            return val;
        }
        return orElse == null ? null : orElse.get();
    }

    default Optional<Object> getHeader(String header) {
        return Optional.ofNullable(getHeaderOrElse(header, null));
    }

}
