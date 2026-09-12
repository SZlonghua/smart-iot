package net.lab1024.sa.base.storage.tdengine;

import com.alibaba.druid.pool.DruidDataSource;
import net.lab1024.sa.base.storage.timeseries.TimeseriesChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * TDengine 存储策略自动装配 — 类级条件 iot.storage.enabled=true；@ConditionalOnProperty 不可重复，
 * 第二条件 iot.storage.tdengine.enabled（默认开启）注解在 tdengine 专属 bean 上。
 * <p>
 * 独立 tdengineDataSource（非 @Primary，不干扰 MySQL 主数据源；initial-size/min-idle=0 惰性建连，
 * TDengine 未启动不影响应用启动）；行/单值列两策略共享同一 {@link TdengineChannel} 通道 bean
 * （建库 @PostConstruct 仅执行一次），策略叶子按 id 注册被注册器收集 ——
 * 将来 influxdb 等新时序库按同构新增 AutoConfiguration（通道 + 策略叶子），通用装配零改动。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Configuration
@EnableConfigurationProperties(TdengineProperties.class)
@ConditionalOnProperty(prefix = "iot.storage", name = "enabled", havingValue = "true")
public class TdengineAutoConfiguration {

    // @ConditionalOnProperty 不可重复，tdengine.enabled（默认开）作为第二条件注解在 tdengine 专属 bean 上

    /** TDengine 独立数据源 — url 带库名（查询/写入不限表名；库由通道引导连接先行创建，惰性建连顺序安全） */
    @Bean(name = "tdengineDataSource", initMethod = "init", destroyMethod = "close")
    @ConditionalOnProperty(prefix = "iot.storage.tdengine", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DruidDataSource tdengineDataSource(TdengineProperties properties) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setName("tdengine");
        dataSource.setDriverClassName(properties.getDriverClassName());
        dataSource.setUrl(properties.getJdbcUrl());
        dataSource.setUsername(properties.getUser());
        dataSource.setPassword(properties.getPassword());
        dataSource.setInitialSize(properties.getInitialSize());
        dataSource.setMinIdle(properties.getMinIdle());
        dataSource.setMaxActive(properties.getMaxActive());
        dataSource.setMaxWait(properties.getMaxWait());
        dataSource.setValidationQuery("SELECT SERVER_VERSION()");
        return dataSource;
    }

    /** TDengine 物理读写通道 — 两策略共享（initStorage 仅执行一次） */
    @Bean
    @ConditionalOnProperty(prefix = "iot.storage.tdengine", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TdengineChannel tdengineChannel(TdengineProperties properties,
                                           @Qualifier("tdengineDataSource") DataSource dataSource) {
        return new TdengineChannel(properties, dataSource);
    }

    /** TDengine 行布局存储策略（tdengine-row） */
    @Bean
    @ConditionalOnProperty(prefix = "iot.storage.tdengine", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TdengineRowStrategy tdengineRowStrategy(@Qualifier("tdengineChannel") TimeseriesChannel channel) {
        return new TdengineRowStrategy(channel);
    }

    /** TDengine 单值列布局存储策略（tdengine-column） */
    @Bean
    @ConditionalOnProperty(prefix = "iot.storage.tdengine", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TdengineColumnStrategy tdengineColumnStrategy(@Qualifier("tdengineChannel") TimeseriesChannel channel) {
        return new TdengineColumnStrategy(channel);
    }
}
