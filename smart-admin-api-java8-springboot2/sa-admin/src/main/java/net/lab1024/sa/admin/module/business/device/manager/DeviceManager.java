package net.lab1024.sa.admin.module.business.device.manager;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.lab1024.sa.admin.module.business.device.dao.DeviceDao;
import net.lab1024.sa.admin.module.business.device.domain.entity.DeviceEntity;
import org.springframework.stereotype.Service;

/**
 * 设备 Manager
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class DeviceManager extends ServiceImpl<DeviceDao, DeviceEntity> {
}
