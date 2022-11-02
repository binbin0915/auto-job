package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.cluster.model.AutoJobTaskTransferManager;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.container.MemoryTaskContainer;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.ITaskEventHandler;
import com.example.autojob.skeleton.lifecycle.TaskEventFactory;
import com.example.autojob.skeleton.lifecycle.TaskEventHandlerDelegate;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskErrorEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskFinishedEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRunErrorEvent;
import com.example.autojob.skeleton.lifecycle.manager.TaskEventManager;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.util.convert.DateUtils;
import com.example.autojob.util.id.SystemClock;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 运行异常调度器
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/19 15:16
 */
@Slf4j
public class AutoJobRunErrorScheduler extends AbstractScheduler implements ITaskEventHandler<TaskRunErrorEvent> {
    private final Map<AutoJobTask, AtomicInteger> retryMap = new ConcurrentHashMap<>();
    private final AutoJobTaskTransferManager manager = AutoJobApplication
            .getInstance()
            .getTransferManager();
    private final MemoryTaskContainer container = AutoJobApplication
            .getInstance()
            .getMemoryTaskContainer();


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
        if (configHolder
                .getAutoJobConfig()
                .getEnableErrorRetry()) {
            if (!retryMap.containsKey(task)) {
                retryMap.put(task, new AtomicInteger(0));
            }
            retryMap
                    .get(task)
                    .incrementAndGet();
            if (retryMap
                    .get(task)
                    .get() <= configHolder
                    .getAutoJobConfig()
                    .getErrorRetryCount()) {
                task
                        .getTrigger()
                        .setTriggeringTime((long) (SystemClock.now() + configHolder
                                .getAutoJobConfig()
                                .getErrorRetryInterval() * 60 * 1000));
                log.warn("任务{}完成失败，{}min后{}第{}次重试", task.getId(), configHolder
                        .getAutoJobConfig()
                        .getErrorRetryInterval(), DateUtils.formatDateTime(new Date(task
                        .getTrigger()
                        .getTriggeringTime())), retryMap
                        .get(task)
                        .get());
                if (task
                        .getTrigger()
                        .isNearTriggeringTime(5000)) {
                    //log.warn("任务：{}下次执行时间在5S内，直接放入缓冲区", task.getId());
                    register.registerTask(task);
                }
                if (task.getType() == AutoJobTask.TaskType.DB_TASK) {
                    AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateTriggeringTime(task.getId(), task
                            .getTrigger()
                            .getTriggeringTime());
                }
            } else {
                if (configHolder
                        .getAutoJobConfig()
                        .getEnableCluster() && manager.addTransferTask(task)) {
                    log.info("任务最终执行失败，将尝试进行故障转移");
                    manager.addTransferTask(task);
                } else {
                    log.error("任务{}经过{}次重试后依然执行异常，任务执行失败", task.getId(), configHolder
                            .getAutoJobConfig()
                            .getErrorRetryCount());
                    task.setIsFinished(true);
                    task
                            .getRunResult()
                            .setFinishedTime(SystemClock.now());
                    TaskEventManager
                            .getInstance()
                            .publishTaskEvent(TaskEventFactory.newErrorEvent(task), TaskErrorEvent.class, true);
                    TaskEventManager
                            .getInstance()
                            .publishTaskEvent(TaskEventFactory.newFinishedEvent(task), TaskFinishedEvent.class, true);
                    retryMap.remove(task);
                }
            }
        } else {
            if (configHolder
                    .getAutoJobConfig()
                    .getEnableCluster() && manager.addTransferTask(task)) {

                log.info("任务最终执行失败，将尝试进行故障转移");
                /*=================故障转移=================>*/
                manager.addTransferTask(task);
                /*=======================Finished======================<*/
            } else {
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
