package net.lab1024.sa.base.common;

public interface Wrapper {
    /**
     * 当前对象是否为指定的类型或者被包装为指定的类型
     *
     * @param type 类型
     * @return 是否为指定的类型
     */
    default boolean isWrapperFor(Class<?> type) {
        return type.isInstance(this);
    }

    /**
     * 尝试将当前对象转换为指定的类型,如果无法转换,将抛出{@link ClassCastException}
     *
     * @param type 类型
     * @param <T>  类型
     * @return 转换后的对象
     */
    default <T> T unwrap(Class<T> type) {
        return type.cast(this);
    }
}
