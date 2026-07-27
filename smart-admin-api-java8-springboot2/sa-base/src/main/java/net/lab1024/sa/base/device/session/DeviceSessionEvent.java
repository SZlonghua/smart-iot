package net.lab1024.sa.base.device.session;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DeviceSessionEvent {

    //时间戳,毫秒
    private long timestamp;

    //事件类型
    private Type type;

    //会话
    private DeviceSession session;

    public static DeviceSessionEvent of(Type type, DeviceSession session) {
        return of(System.currentTimeMillis(), type, session);
    }

    public enum Type {
        //注册
        register,
        //注销
        unregister
    }
}
