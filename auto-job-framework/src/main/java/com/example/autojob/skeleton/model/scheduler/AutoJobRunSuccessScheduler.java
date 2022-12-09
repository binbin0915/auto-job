package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.db.entity.AutoJobTaskEntity;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.container.MemoryTaskContainer;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.ITaskEventHandler;
import com.example.autojob.skeleton.lifecycle.TaskEventFactory;
import com.example.autojob.skeleton.lifecycle.TaskEventHandlerDelegate;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskFinishedEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRunSuccessEvent;
import com.example.autojob.skeleton.lifecycle.manager.TaskEventManager;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.util.id.SystemClock;
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
                findChildTask(task).forEach(this::submitTask);
            }
            return null;
        }, 0, TimeUnit.MILLISECONDS);


        //非子任务尝试刷新下次执行时间
        if (!task.getIsChildTask() && task
                .getTrigger()
                .refresh()) {
            if (task.getType() == AutoJobTask.TaskType.MEMORY_TASk) {
                if (task
                        .getTrigger()
                        .isNearTriggeringTime(5000)) {
                    register.registerTask(task);
                }
            } else {
                if (task
                        .getTrigger()
                        .isNearTriggeringTime(5000)) {
                    register.registerTask(task);
                }
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
            if (task.getIsChildTask() || task
                    .getTrigger()
                    .getIsPause()) {
                return;
            }
            //如果任务已结束
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
                        .map(container::getById)
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
