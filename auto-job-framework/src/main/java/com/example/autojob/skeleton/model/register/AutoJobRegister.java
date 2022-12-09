package com.example.autojob.skeleton.model.register;

import com.example.autojob.skeleton.lang.AbstractLifeCycleHook;
import com.example.autojob.skeleton.framework.config.AutoJobConfig;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.lifecycle.TaskEventFactory;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskBeforeRegisterEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRegisteredEvent;
import com.example.autojob.skeleton.lifecycle.manager.TaskEventManager;
import com.example.autojob.skeleton.model.tq.AutoJobTaskQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Description 注册器
 * @Author Huang Yongxiang
 * @Date 2022/07/04 12:55
 */
@Slf4j
public class AutoJobRegister extends AbstractLifeCycleHook implements IAutoJobRegister {
    private final AutoJobTaskQueue autoJobTaskQueue;
    private AbstractRegisterHandler handler;
    private AbstractRegisterFilter filter;
    private AutoJobConfig config;

    public AutoJobRegister setFilter(AbstractRegisterFilter filter) {
        if (filter == null) {
            throw new NullPointerException();
        }
        if (this.filter == null) {
            this.filter = filter;
        } else {
            this.filter.add(filter);
        }
        return this;
    }

    public AutoJobRegister setHandler(AbstractRegisterHandler handler) {
        if (handler == null) {
            throw new NullPointerException();
        }
        if (this.handler == null) {
            this.handler = handler;
        } else {
            this.handler.add(handler);
        }
        return this;
    }

    public AutoJobRegister(AutoJobTaskQueue autoJobTaskQueue, AbstractRegisterHandler handler, AbstractRegisterFilter filter, AutoJobConfig config) {
        this.autoJobTaskQueue = autoJobTaskQueue;
        this.handler = handler;
        this.filter = filter;
        this.config = config;
    }

    public AutoJobRegister(AutoJobTaskQueue autoJobTaskQueue) {
        this.autoJobTaskQueue = autoJobTaskQueue;
        this.config = AutoJobApplication
                .getInstance()
                .getConfigHolder()
                .getAutoJobConfig();
    }

    @Override
    public boolean registerTask(AutoJobTask task) {
        beforeInitialize(task);
        boolean flag;
        flag = autoJobTaskQueue.joinTask(task);
        afterInitialize(task);
        return flag;
    }

    @Override
    public boolean registerTask(AutoJobTask task, long waitTime, TimeUnit unit) {
        beforeInitialize(task);
        boolean flag = autoJobTaskQueue.joinTask(task, waitTime, unit);
        afterInitialize(task);
        return flag;
    }

    @Override
    public AutoJobTask takeTask() {
        return autoJobTaskQueue.getTask();
    }

    @Override
    public AutoJobTask takeTask(long waitTime, TimeUnit unit) {
        return autoJobTaskQueue.getTask(waitTime, unit);
    }

    @Override
    public AutoJobTask readTask() {
        return autoJobTaskQueue.readTask();
    }


    @Override
    public boolean removeTask(AutoJobTask remove) {
        return autoJobTaskQueue.removeTasks(Collections.singletonList(remove));
    }

    public boolean removeTask(long taskId) {
        return autoJobTaskQueue.removeTaskById(taskId);
    }

    @Override
    public AutoJobTask mergeAndReplaceTaskAndGet(long taskId, AutoJobTask newInstance) {
        AutoJobTask newTask = autoJobTaskQueue.replaceTasks(taskId, newInstance);
        if (newTask != null) {
            registerTask(newTask);
        }
        return newTask;
    }

    @Override
    public AutoJobTask removeAndGetTask(long taskId) {
        return autoJobTaskQueue.removeAndGetTask(taskId);
    }

    @Override
    public AutoJobTask getTaskById(long taskId) {
        return autoJobTaskQueue.getTaskById(taskId);
    }

    @Override
    public Iterator<AutoJobTask> iterator() {
        return autoJobTaskQueue
                .getBlockingQueue()
                .iterator();
    }

    @Override
    public List<AutoJobTask> filter(Predicate<AutoJobTask> predicate) {
        return autoJobTaskQueue
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }


    @Override
    public void beforeInitialize(Object... params) {
        if (params != null && params.length == 1 && params[0] instanceof AutoJobTask) {
            AutoJobTask task = ((AutoJobTask) params[0]);
            if (filter != null && config.getEnableRegisterFilter()) {
                filter.doHandle(task);
            }
            if (handler != null) {
                handler.doHandle(task);
            }
            TaskEventManager
                    .getInstance()
                    .publishTaskEventSync(TaskEventFactory.newBeforeRegisterEvent(task), TaskBeforeRegisterEvent.class, true);
        }
    }

    @Override
    public void afterInitialize(Object... params) {
        if (params != null && params.length == 1 && params[0] instanceof AutoJobTask) {
            AutoJobTask task = ((AutoJobTask) params[0]);
            TaskEventManager
                    .getInstance()
                    .publishTaskEvent(TaskEventFactory.newRegisteredEvent(task), TaskRegisteredEvent.class, true);
        }
    }
}
