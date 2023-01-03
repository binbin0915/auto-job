package com.example.autojob.logging.domain;

import com.example.autojob.skeleton.framework.task.AutoJobRunResult;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.util.json.JsonUtil;
import lombok.Data;

import java.util.Date;

/**
 * 调度记录
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/29 16:30
 */
@Data
public class AutoJobSchedulingRecord {
    /**
     * 调度ID
     */
    private long schedulingId;
    /**
     * 调度时间
     */
    private Date schedulingTime;
    /**
     * 任务别名
     */
    private String taskAlias;
    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 是否成功
     */
    private boolean isSuccess;
    /**
     * 是否正在运行
     */
    private boolean isRun;
    /**
     * 是否是重试调度
     */
    private boolean isRetry;
    /**
     * 执行结果
     */
    private String result;
    /**
     * 执行时长 ms
     */
    private long executionTime;

    public AutoJobSchedulingRecord() {
    }

    public AutoJobSchedulingRecord(AutoJobTask task) {
        if (!task.getIsChildTask()) {
            schedulingTime = new Date(task
                    .getTrigger()
                    .getTriggeringTime());
        } else {
            schedulingTime = new Date();
        }
        taskAlias = task.getAlias();
        taskId = task.getId();
        isRun = task
                .getTrigger()
                .getIsRunning();
        isRetry = task.getIsRetrying();
        AutoJobRunResult runResult = task.getRunResult();
        if (runResult != null && runResult.hasResult()) {
            isSuccess = task
                    .getRunResult()
                    .isRunSuccess();
            executionTime = task
                    .getRunResult()
                    .getFinishedTime() - task
                    .getTrigger()
                    .getTriggeringTime();
            if (runResult.getResult() != null) {
                result = JsonUtil.pojoToJsonString(task
                        .getRunResult()
                        .getResult());
            }
        }
    }
}
