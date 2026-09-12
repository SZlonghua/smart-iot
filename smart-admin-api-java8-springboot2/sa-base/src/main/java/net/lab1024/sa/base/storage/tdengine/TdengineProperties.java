package net.lab1024.sa.base.storage.tdengine;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TDengine 存储策略配置 — iot.storage.tdengine.*
 * <p>
 * 默认驱动 com.taosdata.jdbc.ws.WebSocketDriver（纯 Java WebSocket，jdbc:TAOS-WS://host:6041，
 * 经 taosadapter，无需本机原生库）；备选 com.taosdata.jdbc.TSDBDriver（原生 JNI，
 * jdbc:TAOS://host:6030，需 taos.dll 在 java.library.path）。url 由 driver-class-name 自动推导，
 * 无需单独配置。
 * <p>
 * 连接 url 按官方形态带库名（{@link #getJdbcUrl()}），WebSocket 驱动附加 varcharAsString=true
 * （VARCHAR 列按 String 返回，否则 getObject 返回 byte[]）；无模式写入/查询共用该连接
 * （写入经 {@link com.taosdata.jdbc.AbstractConnection#write}，落连接默认库）；库不存在时带库名
 * url 无法建连，首次建库用不带库名的引导 url（{@link #getBootstrapJdbcUrl()}）。
 *
 * @Author 廖涛
 * @Date 2026/09/06
 * @Copyright 1024创新实验室
 */
@Data
@ConfigurationProperties(prefix = "iot.storage.tdengine")
public class TdengineProperties {

    /** 纯 Java WebSocket 驱动类名（查询 jdbc:TAOS-WS://，无模式写入经 jdbc:TAOS-RS:// 同端口 WebSocket 传输） */
    private static final String DRIVER_WEBSOCKET = "com.taosdata.jdbc.ws.WebSocketDriver";

    /** 原生 JNI 驱动类名（查询/写入均 jdbc:TAOS://，需本机原生库） */
    private static final String DRIVER_JNI = "com.taosdata.jdbc.TSDBDriver";

    /** 策略开关 — storage 总开关开启后再看本开关；默认开启（关闭 = 不装配本策略，消息不落库仅告警一次） */
    private boolean enabled = true;

    /** JDBC 驱动类（WebSocket 纯 Java: com.taosdata.jdbc.ws.WebSocketDriver / 原生 JNI: com.taosdata.jdbc.TSDBDriver） */
    private String driverClassName = DRIVER_WEBSOCKET;

    /** 服务器地址（WebSocket 驱动经 taosadapter 6041；原生 JNI 6030） */
    private String host = "127.0.0.1";

    /** 服务器端口 */
    private int port = 6041;

    /** 用户名（建库需有创建权限，默认 root） */
    private String user = "root";

    /** 密码 */
    private String password = "taosdata";

    /** 目标库名 — 无模式写入时自动建库，表按产品自动建（表数 = 产品数） */
    private String database = "iot_data";

    /** 数据保留天数（建库 KEEP，0 = 永久） */
    private int keep = 3650;

    /** 数据文件保存天数（建库 DURATION） */
    private int days = 30;

    /** 写入 ttl（秒），0 = 使用库 keep */
    private int ttl = 0;

    /** 连接池初始连接数（0 = 惰性建连，TDengine 未启动不影响应用启动） */
    private int initialSize = 0;

    /** 连接池最小空闲连接数 */
    private int minIdle = 0;

    /** 连接池最大活跃连接数 */
    private int maxActive = 10;

    /** 获取连接最大等待时间（毫秒） */
    private long maxWait = 60000;

    /**
     * JDBC url（官方形态，连接池用）— 带库名，查询/写入均不限表名；WebSocket 驱动附
     * varcharAsString=true（官方 WebSocket 驱动形态，VARCHAR 列 getObject 返回 String 而非 byte[]）。
     * 注意：带库名 url 要求库已存在，否则建连报 auth failure:Database not exist —— 首次建库须先经
     * {@link #getBootstrapJdbcUrl()} 引导（建库完成后池才会创建首个物理连接，initial-size=0 惰性建连）
     */
    public String getJdbcUrl() {
        String url = urlPrefix() + "://" + host + ":" + port + "/" + database;
        return DRIVER_WEBSOCKET.equals(driverClassName) ? url + "?varcharAsString=true" : url;
    }

    /** 引导 JDBC url（不带库名，仅建库用）— 库尚不存在时带库名 url 无法建连 */
    public String getBootstrapJdbcUrl() {
        return urlPrefix() + "://" + host + ":" + port;
    }

    /** url 协议前缀 — WebSocket 纯 Java（taosadapter 6041）/ 原生 JNI（6030）/ 其余纯 Java 驱动如 rs.RestfulDriver（taosadapter 6041） */
    private String urlPrefix() {
        if (DRIVER_WEBSOCKET.equals(driverClassName)) {
            return "jdbc:TAOS-WS";
        }
        return isJniDriver() ? "jdbc:TAOS" : "jdbc:TAOS-RS";
    }

    /** 是否原生 JNI 驱动 */
    private boolean isJniDriver() {
        return DRIVER_JNI.equals(driverClassName);
    }
}
