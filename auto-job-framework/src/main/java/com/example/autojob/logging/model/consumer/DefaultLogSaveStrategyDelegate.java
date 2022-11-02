package com.example.autojob.logging.model.consumer;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.model.consumer.strategy.AutoJobLogDBStrategy;
import com.example.autojob.logging.model.consumer.strategy.AutoJobLogMemoryStrategy;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;

/**
 * 默认任务日志保存策略委派者
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/26 11:46
 */
public class DefaultLogSaveStrategyDelegate implements ILogSaveStrategyDelegate<AutoJobLog> {
    @Override
    public IAutoJobLogSaveStrategy<AutoJobLog> doDelegate(AutoJobConfigHolder configHolder, Class<AutoJobLog> type) {
        if (configHolder.getLogConfig().getEnableMemory()) {
            return new AutoJobLogMemoryStrategy();
        }
        return new AutoJobLogDBStrategy();
    }
}
