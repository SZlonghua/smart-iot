package net.lab1024.sa.base.message.codec;

public interface Transport {

    /**
     * @return 唯一标识
     */
    String getId();

    /**
     * @return 名称，默认和ID一致
     */
    default String getName() {
        return getId();
    }
}
