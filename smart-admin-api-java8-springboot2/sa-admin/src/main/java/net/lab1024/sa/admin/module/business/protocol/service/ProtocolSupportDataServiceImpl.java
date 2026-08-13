package net.lab1024.sa.admin.module.business.protocol.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.protocol.dao.ProtocolDao;
import net.lab1024.sa.admin.module.business.protocol.domain.entity.ProtocolEntity;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDataService;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDefinition;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;

/**
 * ProtocolSupportDataService 实现 — 从 protocol 表中读取协议定义。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
@Slf4j
@Service
public class ProtocolSupportDataServiceImpl implements ProtocolSupportDataService {

    @Resource
    private ProtocolDao protocolDao;

    @Override
    public Flux<ProtocolSupportDefinition> getAll() {
        List<ProtocolEntity> list = protocolDao.selectList(null);
        return Flux.fromIterable(list).map(ProtocolEntity::toDefinition);
    }

    @Override
    public Mono<ProtocolSupportDefinition> getById(String id) {
        ProtocolEntity entity = protocolDao.selectById(Long.valueOf(id));
        if (entity == null) {
            return Mono.empty();
        }
        return Mono.just(entity.toDefinition());
    }

}
