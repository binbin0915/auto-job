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
import lombok.extern.slf4j.Slf4j;

/**
 * 任务执行后置处理器的默认实现，原则上无需自己实现，如果确实需要在任务开始、完成等完成一些操作，请优先使用任务事件处理器{@link ITaskEventHandler}
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/03 16:43
 */
@Slf4j
public class DefaultRunnablePostProcessor implements RunnablePostProcessor {
    AutoJobLogHelper logHelper = AutoJobLogHelper.getInstance();

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
                    AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateOperatingStatus(true, autoJobTask.getId());
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
                autoJobTask.setIsFinished(false);
                AutoJobLogContainer
                        .getInstance()
                        .getMessageQueueContext(AutoJobLog.class)
                        .registerMessageQueue(autoJobTask.getId() + "");
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newBeforeRunEvent(autoJobTask), TaskBeforeRunEvent.class, true);
                logHelper.info("Auto-Job-Start=========================>任务：{}即将开始执行", autoJobTask.getId());
            }
        }
    }

    @Override
    public void afterRun(final Executable executable, AutoJobPoolExecutor executor, Object result) {
        if (executable instanceof TaskExecutable) {
            TaskExecutable taskExecutable = (TaskExecutable) executable;
            AutoJobTask autoJobTask = taskExecutable.getAutoJobTask();
            if (autoJobTask != null) {
                log.debug("任务{}已在执行器{}执行完成，执行成功", autoJobTask.getId(), executor.getExecutorName());
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
                        .setIsSuccess(true);
                autoJobTask
                        .getRunResult()
                        .setIsError(false);
                autoJobTask
                        .getTrigger()
                        .update();
                autoJobTask
                        .getRunResult()
                        .setResult(result);
                autoJobTask.setLogHelper(null);
                /*=================更新状态=================>*/
                if (autoJobTask.getType() == AutoJobTask.TaskType.DB_TASK) {
                    AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateOperatingStatus(false, autoJobTask.getId());
                }
                /*=======================Finished======================<*/
                logHelper.info("Auto-Job-End=========================>任务：{}执行完成", autoJobTask.getId());
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newAfterRunEvent(autoJobTask), TaskAfterRunEvent.class, true);
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newSuccessEvent(autoJobTask), TaskRunSuccessEvent.class, true);
                autoJobTask
                        .getTrigger()
                        .setIsRunning(false);
            }
        }
    }

    @Override
    public void runError(final Executable executable, AutoJobPoolExecutor executor, Throwable throwable, Object result) {
        if (executable instanceof TaskExecutable) {
            TaskExecutable taskExecutable = (TaskExecutable) executable;
            AutoJobTask autoJobTask = taskExecutable.getAutoJobTask();
            if (autoJobTask != null) {
                log.error("任务{}已在执行器{}执行完成，执行失败", autoJobTask.getAnnotationId() != null ? autoJobTask.getAnnotationId() : autoJobTask.getId(), executor.getExecutorName());
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
                        .setIsSuccess(false);
                autoJobTask
                        .getRunResult()
                        .setIsError(true);
                //autoJobTask.getTrigger().update();
                autoJobTask
                        .getRunResult()
                        .setResult(result);
                autoJobTask
                        .getRunResult()
                        .setThrowable(throwable);
                autoJobTask.setLogHelper(null);
                /*=================更新状态=================>*/
                if (autoJobTask.getType() == AutoJobTask.TaskType.DB_TASK) {
                    AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateOperatingStatus(false, autoJobTask.getId());
                }
                /*=======================Finished======================<*/
                logHelper.error("Auto-Job-Error=========================>任务：{}执行异常：{}", autoJobTask.getId(), throwable.toString());
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newAfterRunEvent(autoJobTask), TaskAfterRunEvent.class, true);
                TaskEventManager
                        .getInstance()
                        .publishTaskEventSync(TaskEventFactory.newRunErrorEvent(autoJobTask, throwable), TaskRunErrorEvent.class, true);
                autoJobTask
                        .getTrigger()
                        .setIsRunning(false);
            }
        }
    }
}
