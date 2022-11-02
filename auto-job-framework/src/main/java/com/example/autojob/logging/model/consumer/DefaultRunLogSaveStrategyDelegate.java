package com.example.autojob.logging.model.consumer;

import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.logging.model.consumer.strategy.AutoJobRunLogDBStrategy;
import com.example.autojob.logging.model.consumer.strategy.AutoJobRunLogMemoryStrategy;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;

/**
 * 默认的运行日志策略委派者实现
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/26 14:54
 */
public class DefaultRunLogSaveStrategyDelegate implements ILogSaveStrategyDelegate<AutoJobRunLog> {
    @Override
    public IAutoJobLogSaveStrategy<AutoJobRunLog> doDelegate(AutoJobConfigHolder configHolder, Class<AutoJobRunLog> type) {
        //开启内存模式就使用内存策略
        if (configHolder.getLogConfig().getEnableRunLogMemory()) {
            return new AutoJobRunLogMemoryStrategy();
        }
        //否则使用DB策略
        return new AutoJobRunLogDBStrategy();
    }
}
