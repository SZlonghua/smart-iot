package net.lab1024.sa.admin.module.business.alarmlog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.alarmlog.constant.AlarmStatusEnum;
import net.lab1024.sa.admin.module.business.alarmlog.dao.AlarmLogDao;
import net.lab1024.sa.admin.module.business.alarmlog.domain.entity.AlarmLogEntity;
import net.lab1024.sa.admin.module.business.alarmlog.domain.form.AlarmLogHandleForm;
import net.lab1024.sa.admin.module.business.alarmlog.domain.form.AlarmLogQueryForm;
import net.lab1024.sa.admin.module.business.alarmlog.domain.vo.AlarmLogVO;
import net.lab1024.sa.admin.module.business.alarmlog.manager.AlarmLogManager;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警日志 Service
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
@Slf4j
public class AlarmLogService {

    @Resource
    private AlarmLogDao alarmLogDao;

    @Resource
    private AlarmLogManager alarmLogManager;

    /**
     * 分页查询告警日志
     */
    public ResponseDTO<PageResult<AlarmLogVO>> queryPage(AlarmLogQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<AlarmLogVO> list = alarmLogDao.queryPage(page, queryForm);
        PageResult<AlarmLogVO> pageResult = SmartPageUtil.convert2PageResult(page, list);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 处理告警
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> handle(AlarmLogHandleForm handleForm) {
        AlarmLogEntity entity = alarmLogManager.getById(handleForm.getId());
        if (entity == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST, "告警记录不存在");
        }
        if (!entity.getStatus().equals(AlarmStatusEnum.PENDING.getValue())) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST, "该告警已处理，请勿重复操作");
        }

        entity.setStatus(handleForm.getStatus());
        entity.setHandleTime(LocalDateTime.now());
        entity.setHandler(SmartRequestUtil.getRequestUser().getUserName());
        entity.setHandleNote(handleForm.getHandleNote());
        alarmLogManager.updateById(entity);

        return ResponseDTO.ok();
    }
}
