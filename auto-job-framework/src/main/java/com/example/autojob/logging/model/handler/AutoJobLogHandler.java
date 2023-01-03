package com.example.autojob.logging.model.handler;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.logging.domain.AutoJobSchedulingRecord;
import com.example.autojob.logging.model.consumer.DefaultLogSaveStrategyDelegate;
import com.example.autojob.logging.model.consumer.DefaultRunLogSaveStrategyDelegate;
import com.example.autojob.logging.model.consumer.ILogSaveStrategyDelegate;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lang.WithDaemonThread;
import com.example.autojob.util.convert.DefaultValueUtil;
import com.example.autojob.util.id.IdGenerator;
import com.example.autojob.util.thread.SyncHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 一个任务一次完整的调度日志
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/21 14:30
 */
@Slf4j
public class AutoJobLogHandler implements WithDaemonThread {
    /**
     * 调度ID
     */
    private long schedulingId;
    /**
     * 任务ID
     */
    private final long taskId;
    /**
     * 调度记录
     */
    private AutoJobSchedulingRecord record;
    /**
     * 运行日志
     */
    private final List<AutoJobRunLog> runLogs = new ArrayList<>();
    /**
     * 处理的任务
     */
    private final AutoJobTask handleTask;
    /**
     * 任务日志
     */
    private final List<AutoJobLog> logs = new ArrayList<>();
    private final ILogSaveStrategyDelegate<AutoJobLog> logSaveStrategyDelegate;
    private final ILogSaveStrategyDelegate<AutoJobRunLog> runLogSaveStrategyDelegate;

    /**
     * 保存周期，一定周期保存一次日志
     */
    private long saveCycle;

    /**
     * 最大缓冲长度，日志达到该长度后自动保存
     */
    private int maxBufferLength;

    private volatile boolean isFinished = false;

    public AutoJobLogHandler(AutoJobTask task) {
        this(task, null, null);
    }

    public AutoJobLogHandler(AutoJobTask task, ILogSaveStrategyDelegate<AutoJobLog> logSaveStrategyDelegate, ILogSaveStrategyDelegate<AutoJobRunLog> runLogSaveStrategyDelegate) {
        schedulingId = IdGenerator.getNextIdAsLong();
        record = new AutoJobSchedulingRecord(task);
        record.setSchedulingId(schedulingId);
        taskId = task.getId();
        saveCycle = 5000;
        maxBufferLength = 10;
        handleTask = task;
        this.logSaveStrategyDelegate = DefaultValueUtil.defaultValue(logSaveStrategyDelegate, new DefaultLogSaveStrategyDelegate());
        this.runLogSaveStrategyDelegate = DefaultValueUtil.defaultValue(runLogSaveStrategyDelegate, new DefaultRunLogSaveStrategyDelegate());
        startWork();
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

    public synchronized void saveRunLogs() {
        runLogs.forEach(log -> log.setSchedulingId(schedulingId));
        runLogSaveStrategyDelegate
                .doDelegate(AutoJobApplication
                        .getInstance()
                        .getConfigHolder(), AutoJobRunLog.class)
                .doHandle(taskId + "", runLogs);
        runLogs.clear();
    }

    public synchronized void saveLogs() {
        logs.forEach(log -> log.setSchedulingId(schedulingId));
        logSaveStrategyDelegate
                .doDelegate(AutoJobApplication
                        .getInstance()
                        .getConfigHolder(), AutoJobLog.class)
                .doHandle(taskId + "", logs);

        logs.clear();
    }

    public void finishScheduling(boolean isSuccess, String result, long executingTime) {
        AutoJobMapperHolder.SCHEDULING_RECORD_ENTITY_MAPPER.updateResult(schedulingId, isSuccess, result, executingTime);
        saveLogs();
        saveRunLogs();
        isFinished = true;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public AutoJobLogHandler setSaveCycle(long saveCycle, TimeUnit unit) {
        this.saveCycle = unit.toMillis(saveCycle);
        return this;
    }

    public AutoJobLogHandler setMaxBufferLength(int maxBufferLength) {
        this.maxBufferLength = maxBufferLength;
        return this;
    }

    public void refresh() {
        schedulingId = IdGenerator.getNextIdAsLong();
        record = new AutoJobSchedulingRecord(handleTask);
        record.setSchedulingId(schedulingId);
    }

    @Override
    public void startWork() {
        AtomicLong lastSaveTime = new AtomicLong(System.currentTimeMillis());
        Thread thread = new Thread(() -> {
            try {
                while (!isFinished) {
                    SyncHelper.sleepQuietly(1, TimeUnit.SECONDS);
                    if (System.currentTimeMillis() - lastSaveTime.get() >= saveCycle || logs.size() >= maxBufferLength) {
                        //log.info("自动日志保存：{}条", logs.size());
                        saveLogs();
                        lastSaveTime.set(System.currentTimeMillis());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setName("logHandler");
        thread.setDaemon(true);
        thread.start();
    }
}
