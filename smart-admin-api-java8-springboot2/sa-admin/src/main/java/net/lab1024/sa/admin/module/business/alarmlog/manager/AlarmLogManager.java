package net.lab1024.sa.admin.module.business.alarmlog.manager;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.lab1024.sa.admin.module.business.alarmlog.dao.AlarmLogDao;
import net.lab1024.sa.admin.module.business.alarmlog.domain.entity.AlarmLogEntity;
import org.springframework.stereotype.Service;

/**
 * 告警日志 Manager
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class AlarmLogManager extends ServiceImpl<AlarmLogDao, AlarmLogEntity> {
}
