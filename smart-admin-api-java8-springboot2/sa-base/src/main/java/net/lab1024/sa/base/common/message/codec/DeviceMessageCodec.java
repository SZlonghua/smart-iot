package net.lab1024.sa.base.common.message.codec;

public interface DeviceMessageCodec extends DeviceMessageEncoder, DeviceMessageDecoder {

    /**
     * @return 返回支持的传输协议
     * @see DefaultTransport
     */
    Transport getSupportTransport();
}
