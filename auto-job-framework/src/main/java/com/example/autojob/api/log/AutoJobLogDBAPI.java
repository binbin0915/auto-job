package com.example.autojob.api.log;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于数据库存储的日志接口实现
 *
 * @Author Huang Yongxiang
 * @Date 2022/11/07 11:39
 * @Email 1158055613@qq.com
 */
public class AutoJobLogDBAPI implements AutoJobLogAPI {
    @Override
    public List<AutoJobLog> findLogsBySchedulingId(Long schedulingId) {
        return AutoJobMapperHolder.LOG_ENTITY_MAPPER
                .selectBySchedulingId(schedulingId)
                .stream()
                .map(EntityConvertor::logEntity2Log)
                .collect(Collectors.toList());
    }

    @Override
    public List<AutoJobRunLog> findRunLogsBySchedulingId(Long schedulingId) {
        return AutoJobMapperHolder.RUN_LOG_ENTITY_MAPPER
                .selectBySchedulingId(schedulingId)
                .stream()
                .map(EntityConvertor::runLogEntity2RunLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AutoJobLog> findLogsByTaskIdBetween(Long taskId, Date start, Date end) {
        return AutoJobMapperHolder.LOG_ENTITY_MAPPER
                .selectByTaskIdBetween(start, end, taskId)
                .stream()
                .map(EntityConvertor::logEntity2Log)
                .collect(Collectors.toList());
    }

    @Override
    public List<AutoJobRunLog> findRunLogsByTaskIdBetween(Long taskId, Date start, Date end) {
        return AutoJobMapperHolder.RUN_LOG_ENTITY_MAPPER
                .selectByTaskIdBetween(start, end, taskId)
                .stream()
                .map(EntityConvertor::runLogEntity2RunLog)
                .collect(Collectors.toList());
    }
}
