package net.lab1024.sa.admin.module.business.gateway.service;

import net.lab1024.sa.admin.module.business.gateway.dao.GatewayDao;
import net.lab1024.sa.admin.module.business.gateway.domain.entity.GatewayEntity;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.DeviceGatewayPropertiesManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;

/**
 * DeviceGatewayPropertiesManager 实现 — 从 gateway 表读取配置。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
@Service
public class GatewayPropertiesManagerImpl implements DeviceGatewayPropertiesManager {

    @Resource
    private GatewayDao gatewayDao;

    @Override
    public Mono<DeviceGatewayProperties> getProperties(String id) {
        GatewayEntity entity = gatewayDao.selectById(Long.valueOf(id));
        return entity != null ? Mono.just(entity.toProperties()) : Mono.empty();
    }

    @Override
    public Flux<DeviceGatewayProperties> getAllProperties() {
        List<GatewayEntity> list = gatewayDao.selectList(null);
        return Flux.fromIterable(list).map(GatewayEntity::toProperties);
    }
}
