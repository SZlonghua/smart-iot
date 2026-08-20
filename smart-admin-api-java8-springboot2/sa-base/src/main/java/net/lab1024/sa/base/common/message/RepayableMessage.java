package net.lab1024.sa.base.common.message;

/**
 * 可回复消息 —
 * 下行消息需要回复的实现此接口，通过 newReply() 生成回复骨架。 客户端模拟测试回复需要用到 这里也没用到 因为没有写客户端代码测试 直接用的桌面工具测试的
 * 上行消息需要回复也实现此接口 newReply下发给设备 目前没有用到 原因是事件消息不需要回复
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
public interface RepayableMessage<R extends MessageReply> extends Message {

    R newReply();
}
