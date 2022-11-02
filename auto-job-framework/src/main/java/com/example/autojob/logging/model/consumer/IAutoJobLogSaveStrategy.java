package com.example.autojob.logging.model.consumer;

import java.util.List;

/**
 * 日志保存策略，本框架提供内存Cache和DB的保存策略，你可以实现该接口，新增类型Redis，文件等策略，为了新增的策略能被选择到，你还需要实现ILogSaveStrategyDelegate接口
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/12 16:26
 * @see ILogSaveStrategyDelegate
 */
public interface IAutoJobLogSaveStrategy<L> {
    void doHandle(String taskPath, List<L> logList);
}
