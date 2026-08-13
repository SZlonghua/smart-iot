package net.lab1024.sa.base.common.message.raw;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

public interface EncodedMessage {

    @Nonnull
    ByteBuf getPayload();

    default String payloadAsString() {
        return getPayload().toString(StandardCharsets.UTF_8);
    }

    default JSONObject payloadAsJson() {
        return (JSONObject) JSON.parse(payloadAsBytes());
    }

    default JSONArray payloadAsJsonArray() {
        return (JSONArray) JSON.parse(payloadAsBytes());
    }

    default byte[] payloadAsBytes() {
        return ByteBufUtil.getBytes(getPayload());
    }

    @Deprecated
    default byte[] getBytes() {
        return ByteBufUtil.getBytes(getPayload());
    }

    default byte[] getBytes(int offset, int len) {
        return ByteBufUtil.getBytes(getPayload(), offset, len);
    }
}
