package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.cluster.model.AutoJobTaskTransferManager;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.ITaskEventHandler;
import com.example.autojob.skeleton.lifecycle.TaskEventFactory;
import com.example.autojob.skeleton.lifecycle.TaskEventHandlerDelegate;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskErrorEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskFinishedEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRunErrorEvent;
import com.example.autojob.skeleton.lifecycle.manager.TaskEventManager;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.handler.AutoJobRetryHandler;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.util.convert.DefaultValueUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 运行异常调度器
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/19 15:16
 */
@Slf4j
public class AutoJobRunErrorScheduler extends AbstractScheduler implements ITaskEventHandler<TaskRunErrorEvent> {
    private final AutoJobTaskTransferManager manager = AutoJobApplication
            .getInstance()
            .getTransferManager();


    public AutoJobRunErrorScheduler(AutoJobTaskExecutorPool executorPool, IAutoJobRegister register, AutoJobConfigHolder configHolder) {
        super(executorPool, register, configHolder);
    }

    @Override
    public void doHandle(TaskRunErrorEvent event) {
        AutoJobTask task = event.getTask();
        if (task.getType() == AutoJobTask.TaskType.DB_TASK) {
            AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateStatus(task
                    .getTrigger()
                    .getFinishedTimes(), Long.MAX_VALUE, task
                    .getTrigger()
                    .getTriggeringTime(), task
                    .getTrigger()
                    .getLastRunTime(), false, task.getId());
            unlock(task.getId());
        }
        if (AutoJobRetryHandler
                .getInstance()
                .retry(task)) {
            log.info("任务{}重试成功", task.getId());
        } else {
            if (configHolder
                    .getAutoJobConfig()
                    .getEnableCluster() && manager.addTransferTask(task)) {
                log.info("任务最终执行失败，将尝试进行故障转移");
                /*=================故障转移=================>*/
                manager.addTransferTask(task);
                /*=======================Finished======================<*/
            } else {
                log.error("任务{}经过{}次重试后依然执行异常，任务执行失败", task.getId(), DefaultValueUtil
                        .defaultValue(task.getRetryConfig(), AutoJobApplication
                                .getInstance()
                                .getConfigHolder()
                                .getAutoJobConfig()
                                .getRetryConfig())
                        .getRetryCount());
                task.setIsFinished(true);
                task
                        .getRunResult()
                        .finish();
                AutoJobRetryHandler
                        .getInstance()
                        .remove(task.getId());
                TaskEventManager
                        .getInstance()
                        .publishTaskEvent(TaskEventFactory.newErrorEvent(task), TaskErrorEvent.class, true);
                TaskEventManager
                        .getInstance()
                        .publishTaskEvent(TaskEventFactory.newFinishedEvent(task), TaskFinishedEvent.class, true);
            }
        }


    }


    @Override
    public void execute() {
        TaskEventHandlerDelegate
                .getInstance()
                .addHandler(TaskRunErrorEvent.class, this);
    }
}
