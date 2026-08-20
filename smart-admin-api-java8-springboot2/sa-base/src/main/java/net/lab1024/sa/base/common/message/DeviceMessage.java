package net.lab1024.sa.base.common.message;

public interface DeviceMessage extends Message {

    String getProductKey();

    String getDeviceKey();

    /** 设备数据库 ID — 解码后由上下文回填 */
    String getDeviceId();

    long getTimestamp();

    @Override
    DeviceMessage addHeader(String header, Object value);

    @Override
    DeviceMessage addHeaderIfAbsent(String header, Object value);
}
