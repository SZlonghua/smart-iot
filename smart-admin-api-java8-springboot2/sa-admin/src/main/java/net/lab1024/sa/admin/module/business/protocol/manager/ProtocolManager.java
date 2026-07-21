package net.lab1024.sa.admin.module.business.protocol.manager;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.lab1024.sa.admin.module.business.protocol.dao.ProtocolDao;
import net.lab1024.sa.admin.module.business.protocol.domain.entity.ProtocolEntity;
import org.springframework.stereotype.Service;

/**
 * 协议 Manager
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class ProtocolManager extends ServiceImpl<ProtocolDao, ProtocolEntity> {
}
