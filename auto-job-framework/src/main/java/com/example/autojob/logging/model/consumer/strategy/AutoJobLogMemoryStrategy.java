package com.example.autojob.logging.model.consumer.strategy;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.model.consumer.IAutoJobLogSaveStrategy;
import com.example.autojob.logging.model.memory.AutoJobLogCache;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Description 保存到内存缓存
 * @Author Huang Yongxiang
 * @Date 2022/07/12 16:55
 */
@Slf4j
public class AutoJobLogMemoryStrategy implements IAutoJobLogSaveStrategy<AutoJobLog> {
    private final AutoJobLogCache logCache = AutoJobApplication.getInstance().getLogContext().getLogCache();

    @Override
    public void doHandle(String taskPath, List<AutoJobLog> logList) {
        if (logCache == null) {
            log.error("日志保存失败，AutoJobLogCache为null");
            return;
        }
        if (logCache.insertAll(taskPath, logList) && logList.size() > 0) {
            log.debug("成功保存任务：{}的日志：{}条", taskPath, logList.size());
        }

    }
}
