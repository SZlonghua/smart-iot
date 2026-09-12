package net.lab1024.sa.base.storage.tdengine;

import com.taosdata.jdbc.AbstractConnection;
import com.taosdata.jdbc.enums.SchemalessProtocolType;
import com.taosdata.jdbc.enums.SchemalessTimestampType;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.storage.timeseries.TimeseriesChannel;
import net.lab1024.sa.base.storage.timeseries.TimeseriesLayout;
import org.apache.commons.text.StringSubstitutor;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TDengine 时序通道（{@link TimeseriesChannel} 唯一当前实现）— 无模式行协议写入（schemaless LINE
 * 自动建表）与 SQL 查询执行，由行/单值列策略叶子共享同一通道实例。
 * <p>
 * 行协议字符串由家族构建器生成（measurement = 逻辑表名）；写入按官方形态借连接池连接
 * （{@link Connection#unwrap} 出 {@link AbstractConnection} 后 write，WebSocket 驱动经 taosadapter 6041，
 * 落连接默认库，连接 URL 已带库名）；查询同一连接池（连接默认库即目标库，SQL 不限表名；
 * 行键即列名，下划线小写一套命名；动态属性列名经 {@link TimeseriesLayout#rowKey} 归一化；
 * 主时间列 _ts 驱动返回 Timestamp 已归一为毫秒 long；VARCHAR 列经连接 varcharAsString=true 按 String 返回）。
 * 建库用不带库名的引导连接（连接池 URL 带库名、库不存在时无法建连）；表/列不存在
 * （产品尚无数据、属性从未上报）→ 空列表而非报错。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Slf4j
public class TdengineChannel implements TimeseriesChannel {

    /** 建库 SQL 模板 — 命名占位符 {database}/{keep}/{days} 字面量替换（PRECISION 'ms'：毫秒精度） */
    private static final String CREATE_DATABASE_SQL =
            "CREATE DATABASE IF NOT EXISTS {database} PRECISION 'ms' KEEP {keep} DURATION {days}";

    private final TdengineProperties properties;
    private final DataSource dataSource;

    public TdengineChannel(TdengineProperties properties, DataSource dataSource) {
        this.properties = properties;
        this.dataSource = dataSource;
    }

    /**
     * 初始化存储 — 自动建库（无模式写入/查询不限表名的前提）；失败仅告警不阻断
     * （TDengine 未启动/无权限时应用照常启动，写入时再暴露）。
     * 用不带库名的引导连接执行（连接池 URL 带库名，库不存在时建连报 Database not exist；
     * 池 initial-size=0 惰性建连，建库先于首个物理连接，顺序天然安全）
     */
    @Override
    @PostConstruct
    public void initStorage() {
        Map<String, Object> params = new HashMap<>(4);
        params.put("database", properties.getDatabase());
        params.put("keep", properties.getKeep());
        params.put("days", properties.getDays());

        String sql = StringSubstitutor.replace(CREATE_DATABASE_SQL, params, "{", "}");

        try (Connection connection = DriverManager.getConnection(properties.getBootstrapJdbcUrl(),
                properties.getUser(), properties.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log.info("TDengine 数据库就绪: {}（keep={}天 days={}天）", properties.getDatabase(), properties.getKeep(), properties.getDays());
        } catch (SQLException e) {
            log.warn("TDengine 建库失败（{}，不影响应用启动，消息写入将失败并记录日志）: {}", properties.getDatabase(), e.getMessage());
        }
    }

    @Override
    public void writeLines(List<String> lines) {
        log.info("TDengine lines {}",lines);
        // 官方无模式写入形态：连接 unwrap 出 AbstractConnection 后 write（WebSocket/JNI/REST 连接均继承；
        // 无需 SchemalessWriter 每次新建连接），落连接默认库（池 URL 已带库名）
        try (Connection connection = dataSource.getConnection()) {
            AbstractConnection taosConnection = connection.unwrap(AbstractConnection.class);
            taosConnection.write(lines.toArray(new String[0]), SchemalessProtocolType.LINE,
                    SchemalessTimestampType.MILLI_SECONDS, properties.getTtl(), 0L);
        } catch (SQLException e) {
            throw new IllegalStateException("TDengine 写入失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        log.info("TDengine sql {}",sql);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            List<Map<String, Object>> rows = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    Object value = rs.getObject(i);
                    if (value == null) {
                        continue;
                    }
                    // 主时间列 _ts（TIMESTAMP）驱动返回 java.sql.Timestamp → 归一为毫秒 long（行解码按毫秒取值）
                    if (value instanceof Timestamp) {
                        value = ((Timestamp) value).getTime();
                    }
                    // 列名/tag 名归一化为行键（下划线小写形态，与行解码约定一致；VARCHAR 列经连接 varcharAsString=true 按 String 返回）
                    row.put(TimeseriesLayout.rowKey(meta.getColumnLabel(i)), value);
                }
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            // 表/列不存在（产品尚无数据、属性从未上报等）→ 空结果而非报错，策略层无需判表/列存在
            if (isNotExist(e)) {
                return new ArrayList<>();
            }
            throw new IllegalStateException("TDengine 查询失败: " + e.getMessage(), e);
        }
    }

    /** 表/列不存在判定 — 表不存在报 "not exist"（Table/Database does not exist）、列不存在报 "Invalid column name"（0x2602，属性从未上报），均视为无记录 */
    private static boolean isNotExist(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        String lowerCaseMessage = message.toLowerCase();
        return lowerCaseMessage.contains("not exist") || lowerCaseMessage.contains("invalid column name");
    }
}
