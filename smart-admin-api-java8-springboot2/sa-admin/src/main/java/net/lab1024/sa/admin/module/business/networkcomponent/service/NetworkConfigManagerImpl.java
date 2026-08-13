package net.lab1024.sa.admin.module.business.networkcomponent.service;

import net.lab1024.sa.admin.module.business.networkcomponent.dao.NetworkComponentDao;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.entity.NetworkComponentEntity;
import net.lab1024.sa.base.common.network.NetworkConfigManager;
import net.lab1024.sa.base.common.network.NetworkProperties;
import net.lab1024.sa.base.common.network.NetworkType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;

/**
 * NetworkConfigManager 实现 — 从 network_component 表读取配置。
 *
 * @Author 廖涛
 * @Date 2026/08/01
 * @Copyright 1024创新实验室
 */
@Service
public class NetworkConfigManagerImpl implements NetworkConfigManager {

    @Resource
    private NetworkComponentDao networkComponentDao;

    @Override
    public Mono<NetworkProperties> getConfig(@Nullable NetworkType networkType,
                                             @Nonnull String id) {
        NetworkComponentEntity entity = networkComponentDao.selectById(Long.valueOf(id));
        return entity != null ? Mono.just(entity.toProperties()) : Mono.empty();
    }

    @Override
    public Flux<NetworkProperties> getAllConfigs() {
        List<NetworkComponentEntity> list = networkComponentDao.selectList(null);
        return Flux.fromIterable(list).map(NetworkComponentEntity::toProperties);
    }
}
