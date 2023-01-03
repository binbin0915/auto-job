package com.example.autojob.skeleton.model.task;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.model.AutoJobLogContainer;
import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.pool.AutoJobPoolExecutor;
import com.example.autojob.skeleton.framework.pool.Executable;
import com.example.autojob.skeleton.framework.pool.RunnablePostProcessor;
import com.example.autojob.skeleton.framework.task.AutoJobRunResult;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.TaskRunningContext;
import com.example.autojob.skeleton.lifecycle.ITaskEventHandler;
import com.example.autojob.skeleton.lifecycle.TaskEventFactory;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskAfterRunEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskBeforeRunEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRunErrorEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRunSuccessEvent;
import com.example.autojob.skeleton.lifecycle.manager.TaskEventManager;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 任务执行后置处理器的默认实现，原则上无需自己实现，如果确实需要在任务开始、完成等完成一些操作，请优先使用任务事件处理器{@link ITaskEventHandler}
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/03 16:43
 */
@Slf4j
public class DefaultRunnablePostProcessor implements RunnablePostProcessor {

    @Override
    public void beforeRun(final Executable executable, AutoJobPoolExecutor executor, Object... params) {
        if (executable instanceof TaskExecutable) {
            TaskExecutable taskExecutable = (TaskExecutable) executable;
            AutoJobTask autoJobTask = taskExecutable.getAutoJobTask();
            if (autoJobTask != null) {
                log.debug("任务{}已与执行器{}建立连接", autoJobTask.getId(), executor.getExecutorName());
                /*=================绑定到任务上下文=================>*/
                TaskRunningContext
                        .getContextHolder()
                        .set(autoJobTask.getId());
                TaskRunningContext.registerRunningTask(autoJobTask);
                TaskRunningContext
                        .getConcurrentThreadTask()
                        .set(autoJobTask);
                /*=======================Finished======================<*/
                /*=================更新状态=================>*/
                if (autoJobTask.getType() == AutoJobTask.TaskType.DB_TASK) {
                    ScheduleTaskUtil.oneTimeTask(() -> {
                        AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateOperatingStatus(true, autoJobTask.getId());
                        return null;
                    }, 0, TimeUnit.SECONDS);

                }
                autoJobTask
                        .getTrigger()
                        .setIsRunning(true);
                autoJobTask.setLogHelper(new AutoJobLogHelper(null, autoJobTask));
                /*=======================Finished======================<*/
                autoJobTask
                        .getTrigger()
                        .start();
                autoJobTask
                        .getTrigger()
                        .setLastTriggeringTime(autoJobTask
                                .getTrigger()
                                .getTriggeringTime());
                if (autoJobTask.getRunResult() == null) {
                    autoJobTask.setRunResult(new AutoJobRunResult());
                } else {
                    autoJobTask
                            .getRunResult()
                            .reset();
                }
                autoJobTask.setIsStart(true);
                autoJobTask.setIsWaiting(false);
                autoJobTask.setIsFinished(false);
                AutoJobLogContainer
                        .getInstance()
                        .getMessageQueueContext(AutoJobLog.class)
                        .registerMessageQueue(autoJobTask.getId() + "");
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newBeforeRunEvent(autoJobTask), TaskBeforeRunEvent.class, true);
                autoJobTask
                        .getLogHelper()
                        .info("Auto-Job-Start=========================>任务：{}即将开始执行", autoJobTask.getId());
            }
        }
    }

    @Override
    public void afterRun(final Executable executable, AutoJobPoolExecutor executor, Object result) {
        if (executable instanceof TaskExecutable) {
            TaskExecutable taskExecutable = (TaskExecutable) executable;
            AutoJobTask autoJobTask = taskExecutable.getAutoJobTask();
            if (autoJobTask != null) {
                TaskRunningContext.removeRunningTask(autoJobTask);
                autoJobTask
                        .getTrigger()
                        .finished();
                autoJobTask
                        .getTrigger()
                        .setIsLastSuccess(true);
                autoJobTask.setIsStart(false);
                autoJobTask
                        .getRunResult()
                        .success(result);
                autoJobTask
                        .getTrigger()
                        .update();
                autoJobTask.setIsRetrying(false);
                /*=================更新状态=================>*/
                if (autoJobTask.getType() == AutoJobTask.TaskType.DB_TASK) {
                    ScheduleTaskUtil.oneTimeTask(() -> {
                        AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateOperatingStatus(false, autoJobTask.getId());
                        return null;
                    }, 0, TimeUnit.SECONDS);
                }
                /*=======================Finished======================<*/
                autoJobTask
                        .getLogHelper()
                        .setSlf4jProxy(null)
                        .info("Auto-Job-End=========================>任务：{}执行完成", autoJobTask.getId());
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newAfterRunEvent(autoJobTask), TaskAfterRunEvent.class, true);
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newSuccessEvent(autoJobTask), TaskRunSuccessEvent.class, true);
                autoJobTask
                        .getTrigger()
                        .setIsRunning(false);
                autoJobTask.setLogHelper(null);
            }
        }
    }

    @Override
    public void runError(final Executable executable, AutoJobPoolExecutor executor, Throwable throwable, Object result) {
        if (executable instanceof TaskExecutable) {
            TaskExecutable taskExecutable = (TaskExecutable) executable;
            AutoJobTask autoJobTask = taskExecutable.getAutoJobTask();
            if (autoJobTask != null) {
                TaskRunningContext.removeRunningTask(autoJobTask);
                autoJobTask
                        .getTrigger()
                        .finished();
                autoJobTask
                        .getTrigger()
                        .setIsLastSuccess(false);
                autoJobTask.setIsStart(false);
                autoJobTask
                        .getRunResult()
                        .error(throwable, result);
                /*=================更新状态=================>*/
                if (autoJobTask.getType() == AutoJobTask.TaskType.DB_TASK) {
                    ScheduleTaskUtil.oneTimeTask(() -> {
                        AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateOperatingStatus(false, autoJobTask.getId());
                        return null;
                    }, 0, TimeUnit.SECONDS);
                }
                /*=======================Finished======================<*/
                autoJobTask
                        .getLogHelper()
                        .setSlf4jProxy(null)
                        .error("Auto-Job-Error=========================>任务：{}执行异常：{}", autoJobTask.getId(), throwable.getCause() == null ? throwable.toString() : throwable
                                .getCause()
                                .toString());
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newAfterRunEvent(autoJobTask), TaskAfterRunEvent.class, true);
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newRunErrorEvent(autoJobTask, throwable), TaskRunErrorEvent.class, true);
                autoJobTask
                        .getTrigger()
                        .setIsRunning(false);
                autoJobTask.setLogHelper(null);
            }
        }
    }
}
