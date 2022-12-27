package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.db.entity.AutoJobTaskEntity;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.container.MemoryTaskContainer;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.ITaskEventHandler;
import com.example.autojob.skeleton.lifecycle.TaskEventFactory;
import com.example.autojob.skeleton.lifecycle.TaskEventHandlerDelegate;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskFinishedEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRunSuccessEvent;
import com.example.autojob.skeleton.lifecycle.manager.TaskEventManager;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 运行成功调度器
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/19 14:41
 */
@Slf4j
public class AutoJobRunSuccessScheduler extends AbstractScheduler implements ITaskEventHandler<TaskRunSuccessEvent> {
    private final ScheduleTaskUtil childTaskScheduleThread;
    private final MemoryTaskContainer container = AutoJobApplication
            .getInstance()
            .getMemoryTaskContainer();

    public AutoJobRunSuccessScheduler(AutoJobTaskExecutorPool executorPool, IAutoJobRegister register, AutoJobConfigHolder configHolder) {
        super(executorPool, register, configHolder);
        this.childTaskScheduleThread = ScheduleTaskUtil.build(true, "childTaskScheduleThread");
    }

    @Override
    public void doHandle(TaskRunSuccessEvent event) {
        AutoJobTask task = event.getTask();
        //DB型任务释放锁
        if (task.getType() == AutoJobTask.TaskType.DB_TASK) {
            unlock(task.getId());
        }

        //异步处理子任务，保证调度的高效性
        childTaskScheduleThread.EOneTimeTask(() -> {
            //处理子任务
            if (task
                    .getTrigger()
                    .hasChildTask()) {
                findChildTask(task).forEach(c -> {
                    if (c.getType() == AutoJobTask.TaskType.MEMORY_TASk || (c.getType() == AutoJobTask.TaskType.DB_TASK && lock(c.getId()))) {
                        submitTask(c);
                    }
                });
            }
            return null;
        }, 0, TimeUnit.MILLISECONDS);
        if (!task.getIsChildTask()) {
            if (task
                    .getTrigger()
                    .refresh()) {
                //log.info("任务{}刷新成功，{}", task.getAlias(), DateUtils.formatDateTime(task
                //        .getTrigger()
                //        .getTriggeringTime()));
                if (task
                        .getTrigger()
                        .isNearTriggeringTime(5000)) {
                    register.registerTask(task);
                }
                if (task.getType() == AutoJobTask.TaskType.DB_TASK) {
                    //异步更新DB任务的状态
                    childTaskScheduleThread.EOneTimeTask(() -> {
                        AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateStatus(task
                                .getTrigger()
                                .getFinishedTimes(), event.getTriggeringTime(), task
                                .getTrigger()
                                .getTriggeringTime(), task
                                .getTrigger()
                                .getLastRunTime(), true, task.getId());
                        return null;
                    }, 0, TimeUnit.MILLISECONDS);
                }
            } else {
                if (task
                        .getTrigger()
                        .getIsPause()) {
                    return;
                }
                if (task.getType() == AutoJobTask.TaskType.DB_TASK) {
                    childTaskScheduleThread.EOneTimeTask(() -> AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateStatus(task
                            .getTrigger()
                            .getFinishedTimes(), event.getTriggeringTime(), Long.MAX_VALUE, task
                            .getTrigger()
                            .getLastRunTime(), true, task.getId()), 0, TimeUnit.MILLISECONDS);
                }
                task
                        .getRunResult()
                        .setFinishedTime(System.currentTimeMillis());
                task.setIsFinished(true);
                TaskEventManager
                        .getInstance()
                        .publishTaskEvent(TaskEventFactory.newFinishedEvent(event.getTask()), TaskFinishedEvent.class, true);
            }
        } else {
            if (task.getType() == AutoJobTask.TaskType.DB_TASK) {
                //异步更新DB任务的状态
                childTaskScheduleThread.EOneTimeTask(() -> {
                    AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateStatus(task
                            .getTrigger()
                            .getFinishedTimes(), event.getTriggeringTime(), Long.MAX_VALUE, task
                            .getTrigger()
                            .getLastRunTime(), true, task.getId());
                    return null;
                }, 0, TimeUnit.MILLISECONDS);
            }
        }
    }

    private List<AutoJobTask> findChildTask(AutoJobTask parent) {
        if (parent
                .getTrigger()
                .hasChildTask()) {
            List<Long> childTaskIds = parent
                    .getTrigger()
                    .getChildTask();
            if (parent.getType() == AutoJobTask.TaskType.MEMORY_TASk) {
                return childTaskIds
                        .stream()
                        .map(id -> {
                            AutoJobTask task = container.getById(id);
                            if (task == null) {
                                task = container.getByAnnotationId(id);
                            }
                            return task;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            } else {
                List<AutoJobTaskEntity> entities = AutoJobMapperHolder.TASK_ENTITY_MAPPER.selectChildTasks(childTaskIds);
                return entities
                        .stream()
                        .filter(Objects::nonNull)
                        .map(EntityConvertor::taskEntity2Task)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }


    @Override
    public int getHandlerLevel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void execute() {
        TaskEventHandlerDelegate
                .getInstance()
                .addHandler(TaskRunSuccessEvent.class, this);
    }

    @Override
    public void destroy() {
        childTaskScheduleThread.shutdown();
    }
}
