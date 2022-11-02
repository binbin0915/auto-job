package com.example.autojob.logging.model.consumer;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.logging.model.AutoJobLogContainer;
import com.example.autojob.logging.model.factory.AutoJobRunLogFactory;
import com.example.autojob.logging.model.handler.AutoJobLogHandler;
import com.example.autojob.skeleton.framework.mq.MessageQueueContext;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.ITaskEventHandler;
import com.example.autojob.skeleton.lifecycle.event.TaskEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskAfterRunEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskBeforeRunEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskFinishedEvent;
import com.example.autojob.util.json.JsonUtil;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import com.example.autojob.util.thread.SyncHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 任务日志消费者
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/21 14:43
 */
@Slf4j
public class AutoJobLogConsumer implements ITaskEventHandler<TaskEvent> {
    private final Map<Long, AutoJobLogHandler> logHandlerMap = new ConcurrentHashMap<>();
    private final ILogSaveStrategyDelegate<AutoJobLog> logSaveStrategyDelegate;
    private final ILogSaveStrategyDelegate<AutoJobRunLog> runLogSaveStrategyDelegate;
    private final MessageQueueContext<AutoJobLog> logMessageQueueContext;
    private final ScheduleTaskUtil saveLogScheduler = ScheduleTaskUtil.build(true, "saveLogScheduler");

    public AutoJobLogConsumer(ILogSaveStrategyDelegate<AutoJobLog> logSaveStrategyDelegate, ILogSaveStrategyDelegate<AutoJobRunLog> runLogSaveStrategyDelegate) {
        this.logSaveStrategyDelegate = logSaveStrategyDelegate;
        this.runLogSaveStrategyDelegate = runLogSaveStrategyDelegate;
        logMessageQueueContext = AutoJobLogContainer
                .getInstance()
                .getMessageQueueContext(AutoJobLog.class);
    }

    @Override
    public void doHandle(TaskEvent event) {
        if (event instanceof TaskBeforeRunEvent) {
            AutoJobLogHandler lastHandler = logHandlerMap.get(event
                    .getTask()
                    .getId());
            //log.info("等待上次调度日志保存");
            SyncHelper.aWait(() -> lastHandler == null || lastHandler.isFinished(), 1, TimeUnit.MINUTES);
            //log.info("等待结束");
            AutoJobLogHandler handler = new AutoJobLogHandler(event.getTask(), logSaveStrategyDelegate, runLogSaveStrategyDelegate);
            logHandlerMap.put(event
                    .getTask()
                    .getId(), handler);
            saveLogScheduler.EOneTimeTask(() -> {
                handler.saveSchedulingRecord();
                return null;
            }, 0, TimeUnit.MILLISECONDS);

        }
        if (logHandlerMap.containsKey(event
                .getTask()
                .getId())) {
            AutoJobLogHandler handler = logHandlerMap.get(event
                    .getTask()
                    .getId());
            handler.addRunLog(AutoJobRunLogFactory.getAutoJobRunLog(event));
            saveLogScheduler.EOneTimeTask(() -> {
                handler.saveRunLogs();
                return null;
            }, 0, TimeUnit.MILLISECONDS);
        }
        if (event instanceof TaskAfterRunEvent) {
            AutoJobLogHandler handler = logHandlerMap.get(event
                    .getTask()
                    .getId());
            AutoJobTask task = event.getTask();
            List<AutoJobLog> logList = logMessageQueueContext.takeAllMessageNoBlock(event
                    .getTask()
                    .getId() + "", true);
            handler.addAllLogs(logList);
            String result = JsonUtil.pojoToJsonString(task
                    .getRunResult()
                    .getResult());
            saveLogScheduler.EOneTimeTask(() -> {
                handler.finishScheduling(task
                        .getRunResult()
                        .isRunSuccess(), result, ((TaskAfterRunEvent) event).getEndTime() - task
                        .getTrigger()
                        .getStartRunTime());
                return null;
            }, 0, TimeUnit.MILLISECONDS);
        }
        if (event instanceof TaskFinishedEvent) {
            AutoJobLogContainer
                    .getInstance()
                    .getMessageQueueContext(AutoJobLog.class)
                    .unsubscribe(event
                            .getTask()
                            .getId() + "", 0, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int getHandlerLevel() {
        return Integer.MIN_VALUE;
    }
}
