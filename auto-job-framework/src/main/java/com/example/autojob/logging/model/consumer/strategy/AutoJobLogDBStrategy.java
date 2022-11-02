package com.example.autojob.logging.model.consumer.strategy;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.model.consumer.IAutoJobLogSaveStrategy;
import com.example.autojob.skeleton.db.entity.AutoJobLogEntity;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobLogEntityMapper;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.util.thread.InterruptThreadHelper;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 将日志保存到数据库
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/12 16:51
 */
@Slf4j
public class AutoJobLogDBStrategy implements IAutoJobLogSaveStrategy<AutoJobLog> {

    @Override
    public void doHandle(String taskPath, List<AutoJobLog> logList) {
        List<AutoJobLogEntity> entities = logList.stream().map(EntityConvertor::log2LogEntity).collect(Collectors.toList());
        if (InterruptThreadHelper.isInterrupt()) {
            ScheduleTaskUtil.build(true, "logDBStrategyThread").EOneTimeTask(() -> {
                AutoJobLogEntityMapper mapper = AutoJobMapperHolder.LOG_ENTITY_MAPPER;
                int count = mapper.insertList(entities);
                log.debug("成功保存任务：{}的日志{}条", taskPath, count);
                return null;
            }, 0, TimeUnit.MILLISECONDS);
        } else {
            AutoJobLogEntityMapper mapper = AutoJobMapperHolder.LOG_ENTITY_MAPPER;
            int count = mapper.insertList(entities);
            log.debug("成功保存任务：{}的日志{}条", taskPath, count);
        }
    }
}
