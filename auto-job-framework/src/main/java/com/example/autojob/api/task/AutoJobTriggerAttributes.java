package com.example.autojob.api.task;

import com.example.autojob.skeleton.framework.task.AutoJobTrigger;
import lombok.Data;

import java.util.List;

/**
 * API的触发器属性
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/27 11:12
 * @Email 1158055613@qq.com
 */
@Data
public class AutoJobTriggerAttributes {
    /**
     * 触发时间
     */
    private Long triggeringTime;
    /**
     * cron like表达式
     */
    private String cronExpression;
    /**
     * 重复次数
     */
    private Integer repeatTimes;
    /**
     * 已完成次数
     */
    private Integer finishedTimes;
    /**
     * 上次触发时间
     */
    private Long lastTriggeringTime;
    /**
     * 上次是否运行成功
     */
    private Boolean isLastSuccess;
    /**
     * 任务主键
     */
    private Long taskId;
    /**
     * 子任务的ID列表
     */
    private List<Long> childTask;
    /**
     * 周期
     */
    private Long cycle;
    /**
     * 最大运行时长
     */
    private Long maximumExecutionTime;
    /**
     * 上次运行时间
     */
    private Long lastRunTime;
    /**
     * 是否暂停
     */
    private Boolean isPause = false;

    public AutoJobTriggerAttributes(AutoJobTrigger trigger) {
        triggeringTime = trigger.getTriggeringTime();
        cronExpression = trigger.getCronExpression();
        repeatTimes = trigger.getRepeatTimes();
        finishedTimes = trigger.getFinishedTimes();
        lastTriggeringTime = trigger.getLastTriggeringTime();
        isLastSuccess = trigger.getIsLastSuccess();
        taskId = trigger.getTaskId();
        childTask = trigger.getChildTask();
        cycle = trigger.getCycle();
        maximumExecutionTime = trigger.getMaximumExecutionTime();
        lastRunTime = trigger.getLastRunTime();
        isPause = trigger.getIsPause();
    }

    public AutoJobTriggerAttributes() {
    }

    public AutoJobTrigger convert() {
        AutoJobTrigger trigger = new AutoJobTrigger();
        trigger.setTriggeringTime(triggeringTime);
        trigger.setIsPause(isPause);
        trigger.setRepeatTimes(repeatTimes);
        trigger.setChildTask(childTask);
        trigger.setTaskId(taskId);
        trigger.setCronExpression(cronExpression);
        if (finishedTimes != null) {
            trigger.setFinishedTimes(finishedTimes);
        }
        trigger.setLastTriggeringTime(lastTriggeringTime);
        trigger.setIsLastSuccess(isLastSuccess);
        trigger.setCycle(cycle);
        trigger.setMaximumExecutionTime(maximumExecutionTime);
        return trigger;
    }
}
