package net.lab1024.sa.admin.module.business.devicelog.service;

import net.lab1024.sa.admin.module.business.device.storage.service.MessageLogDataService;
import net.lab1024.sa.admin.module.business.devicelog.domain.form.DeviceLogQueryForm;
import net.lab1024.sa.admin.module.business.devicelog.domain.vo.DeviceLogVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartLocalDateUtil;
import net.lab1024.sa.base.storage.model.MessageLogData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备日志 Service — 设备消息日志保存在消息数据存储（时序库），分页查询委托
 * {@link MessageLogDataService#queryLog}；设备必选（时序表按产品隔离）。
 * <p>
 * 日志类型与存储 commandType 同一套命名（TopicMessageCodec 枚举名，见 DeviceLogTypeEnum，前端枚举同源）：
 * 表单类型多选值（含回复类型）直传查询、结果类型原样返回，无需任何映射转换。
 * deviceName 过滤为冗余字段精确匹配（名称快照），不支持模糊。
 *
 * @Author 廖涛
 * @Date 2026/09/07
 * @Copyright 1024创新实验室
 */
@Service
public class DeviceLogService {

    @Resource
    private MessageLogDataService messageLogDataService;

    /**
     * 分页查询设备日志（命令上下行，来自消息数据存储）
     */
    public ResponseDTO<PageResult<DeviceLogVO>> queryPage(DeviceLogQueryForm queryForm) {
        Long deviceId = queryForm.getDeviceId();
        if (deviceId == null) {
            throw new BusinessException("请先选择设备再查询设备日志");
        }
        PageResult<MessageLogData> logPage = messageLogDataService.queryLog(String.valueOf(deviceId),
                queryForm.getTypeList(), null, queryForm.getDeviceName(),
                SmartLocalDateUtil.toEpochMillis(queryForm.getCreateTimeBegin()),
                SmartLocalDateUtil.toEpochMillis(queryForm.getCreateTimeEnd()),
                queryForm.getIntPageNum(), queryForm.getIntPageSize());
        return ResponseDTO.ok(toVoPageResult(logPage));
    }

    /** 分页结果 MessageLogData → DeviceLogVO（类型原样透出，前端按同源枚举渲染；时序行无 updateTime） */
    private PageResult<DeviceLogVO> toVoPageResult(PageResult<MessageLogData> source) {
        PageResult<DeviceLogVO> pageResult = new PageResult<>();
        pageResult.setPageNum(source.getPageNum());
        pageResult.setPageSize(source.getPageSize());
        pageResult.setTotal(source.getTotal());
        pageResult.setPages(source.getPages());
        List<DeviceLogVO> voList = new ArrayList<>(source.getList().size());
        for (MessageLogData log : source.getList()) {
            DeviceLogVO vo = new DeviceLogVO();
            vo.setDeviceId(Long.valueOf(log.getDeviceId()));
            vo.setDeviceName(log.getDeviceName());
            vo.setType(log.getCommandType());
            vo.setContent(log.getContent());
            vo.setCreateTime(SmartLocalDateUtil.toLocalDateTime(log.getTimestamp()));
            voList.add(vo);
        }
        pageResult.setList(voList);
        pageResult.setEmptyFlag(source.getEmptyFlag());
        return pageResult;
    }
}
