package com.example.autojob.logging.model.handler;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.logging.domain.AutoJobSchedulingRecord;
import com.example.autojob.logging.model.consumer.DefaultLogSaveStrategyDelegate;
import com.example.autojob.logging.model.consumer.DefaultRunLogSaveStrategyDelegate;
import com.example.autojob.logging.model.consumer.ILogSaveStrategyDelegate;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.util.id.IdGenerator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 一个任务一次完整的调度日志
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/21 14:30
 */
@Data
public class AutoJobLogHandler {
    /**
     * 调度ID
     */
    private long schedulingId;
    /**
     * 任务ID
     */
    private long taskId;
    /**
     * 调度记录
     */
    private AutoJobSchedulingRecord record;
    /**
     * 运行日志
     */
    private final List<AutoJobRunLog> runLogs = new ArrayList<>();
    /**
     * 任务日志
     */
    private final List<AutoJobLog> logs = new ArrayList<>();
    private ILogSaveStrategyDelegate<AutoJobLog> logSaveStrategyDelegate;
    private ILogSaveStrategyDelegate<AutoJobRunLog> runLogSaveStrategyDelegate;

    private volatile boolean isFinished = false;

    public AutoJobLogHandler(AutoJobTask task) {
        this(task, null, null);
    }

    public AutoJobLogHandler(AutoJobTask task, ILogSaveStrategyDelegate<AutoJobLog> logSaveStrategyDelegate, ILogSaveStrategyDelegate<AutoJobRunLog> runLogSaveStrategyDelegate) {
        schedulingId = IdGenerator.getNextIdAsLong();
        record = new AutoJobSchedulingRecord(task);
        record.setSchedulingId(schedulingId);
        taskId = task.getId();
        this.logSaveStrategyDelegate = logSaveStrategyDelegate;
        this.runLogSaveStrategyDelegate = runLogSaveStrategyDelegate;
    }

    public boolean saveSchedulingRecord() {
        return AutoJobMapperHolder.SCHEDULING_RECORD_ENTITY_MAPPER.insertList(Collections.singletonList(EntityConvertor.schedulingRecord2Entity(record))) == 1;
    }

    public void addRunLog(AutoJobRunLog runLog) {
        if (runLog == null) {
            return;
        }
        runLogs.add(runLog);
    }

    public void addAllRunLogs(List<AutoJobRunLog> runLogs) {
        this.runLogs.addAll(runLogs
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public void addLog(AutoJobLog log) {
        if (log == null) {
            return;
        }
        logs.add(log);
    }

    public void addAllLogs(List<AutoJobLog> logs) {
        this.logs.addAll(logs
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public void saveRunLogs() {
        runLogs.forEach(log -> log.setSchedulingId(schedulingId));
        if (runLogSaveStrategyDelegate == null) {
            new DefaultRunLogSaveStrategyDelegate()
                    .doDelegate(AutoJobApplication
                            .getInstance()
                            .getConfigHolder(), AutoJobRunLog.class)
                    .doHandle(taskId + "", runLogs);
        } else {
            runLogSaveStrategyDelegate
                    .doDelegate(AutoJobApplication
                            .getInstance()
                            .getConfigHolder(), AutoJobRunLog.class)
                    .doHandle(taskId + "", runLogs);
        }
        runLogs.clear();
    }

    public void saveLogs() {
        logs.forEach(log -> log.setSchedulingId(schedulingId));
        if (logSaveStrategyDelegate == null) {
            new DefaultLogSaveStrategyDelegate()
                    .doDelegate(AutoJobApplication
                            .getInstance()
                            .getConfigHolder(), AutoJobLog.class)
                    .doHandle(taskId + "", logs);
        } else {
            logSaveStrategyDelegate
                    .doDelegate(AutoJobApplication
                            .getInstance()
                            .getConfigHolder(), AutoJobLog.class)
                    .doHandle(taskId + "", logs);
        }

        logs.clear();
    }

    public void finishScheduling(boolean isSuccess, String result, long executingTime) {
        AutoJobMapperHolder.SCHEDULING_RECORD_ENTITY_MAPPER.updateResult(schedulingId, isSuccess, result, executingTime);
        saveLogs();
        saveRunLogs();
        isFinished = true;
    }
}
