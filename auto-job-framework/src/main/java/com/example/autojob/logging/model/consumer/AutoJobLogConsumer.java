package com.example.autojob.logging.model.consumer;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.logging.model.AutoJobLogContainer;
import com.example.autojob.logging.model.factory.AutoJobRunLogFactory;
import com.example.autojob.logging.model.handler.AutoJobLogHandler;
import com.example.autojob.skeleton.framework.config.TimeConstant;
import com.example.autojob.skeleton.framework.mq.MessagePublishedListener;
import com.example.autojob.skeleton.framework.mq.MessageQueueContext;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.ITaskEventHandler;
import com.example.autojob.skeleton.lifecycle.event.TaskEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskAfterRunEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskBeforeRunEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskFinishedEvent;
import com.example.autojob.util.json.JsonUtil;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import lombok.extern.slf4j.Slf4j;

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
    private static final String LISTENER_PREFIX = "LOG_HANDLE_LISTENER_";

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
            AutoJobLogHandler handler = logHandlerMap.get(event
                    .getTask()
                    .getId());
            if (handler == null) {
                handler = new AutoJobLogHandler(event.getTask(), logSaveStrategyDelegate, runLogSaveStrategyDelegate);
                logHandlerMap.put(event
                        .getTask()
                        .getId(), handler);
            } else {
                handler.refresh();
            }
            AutoJobLogHandler finalHandler = handler;
            saveLogScheduler.EOneTimeTask(() -> {
                finalHandler.saveSchedulingRecord();
                return null;
            }, 0, TimeUnit.MILLISECONDS);
            logMessageQueueContext.addMessagePublishedListener(event
                    .getTask()
                    .getId() + "", new HandleMessageListener(handler, event.getTask()));

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
            logMessageQueueContext.takeAllMessageNoBlock(event
                    .getTask()
                    .getId() + "", true);

            //handler.addAllLogs(logList);
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
            if (task
                    .getTrigger()
                    .nextTriggeringTime() - System.currentTimeMillis() > TimeConstant.A_MINUTE * 30) {
                logHandlerMap.remove(task.getId());
            }

            logMessageQueueContext.removeMessagePublishedListener(task.getId() + "", LISTENER_PREFIX + task.getId());
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

    private static class HandleMessageListener implements MessagePublishedListener<AutoJobLog> {
        AutoJobLogHandler handler;
        AutoJobTask task;

        public HandleMessageListener(AutoJobLogHandler handler, AutoJobTask task) {
            this.handler = handler;
            this.task = task;
        }

        @Override
        public void onMessagePublished(AutoJobLog message) {
            //log.info("添加日志");
            handler.addLog(message);
        }

        @Override
        public String listenerName() {
            return LISTENER_PREFIX + task.getId();
        }
    }

    @Override
    public int getHandlerLevel() {
        return Integer.MIN_VALUE;
    }
}
