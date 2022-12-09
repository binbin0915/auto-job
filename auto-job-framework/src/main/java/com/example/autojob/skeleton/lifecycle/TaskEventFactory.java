package com.example.autojob.skeleton.lifecycle;

import com.example.autojob.skeleton.cluster.model.ClusterNode;
import com.example.autojob.skeleton.lang.IAutoJobFactory;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.event.TaskEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.*;
import com.example.autojob.util.convert.DefaultValueUtil;
import com.example.autojob.util.id.SystemClock;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 任务事件工厂
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/12 9:58
 */
public class TaskEventFactory implements IAutoJobFactory {
    public static TaskEvent newTaskEvent(AutoJobTask task, String message) {
        return new TaskEvent(task).setMessage(message);
    }

    public static TaskBeforeRegisterEvent newBeforeRegisterEvent(AutoJobTask task) {
        TaskBeforeRegisterEvent taskBeforeRegisterEvent = new TaskBeforeRegisterEvent(task);
        taskBeforeRegisterEvent.setMessage(String.format("任务：%d准备注册", task.getId()));
        return taskBeforeRegisterEvent;
    }

    public static TaskRegisteredEvent newRegisteredEvent(AutoJobTask task) {
        TaskRegisteredEvent registeredEvent = new TaskRegisteredEvent(task);
        registeredEvent.setMessage(String.format("任务：%d注册完成", task.getId()));
        return registeredEvent;
    }

    public static TaskBeforeRunEvent newBeforeRunEvent(AutoJobTask task) {
        TaskBeforeRunEvent taskBeforeRunEvent = new TaskBeforeRunEvent(task);
        taskBeforeRunEvent.setMessage(String.format("任务：%d准备启动运行", task.getId()));
        taskBeforeRunEvent.setStartTime(System.currentTimeMillis());
        return taskBeforeRunEvent;
    }

    public static TaskAfterRunEvent newAfterRunEvent(AutoJobTask task) {
        TaskAfterRunEvent afterRunEvent = new TaskAfterRunEvent(task);
        afterRunEvent.setMessage(String.format("任务：%d运行完成", task.getId()));
        afterRunEvent.setEndTime(System.currentTimeMillis());
        return afterRunEvent;
    }

    public static TaskRunSuccessEvent newSuccessEvent(AutoJobTask task) {
        TaskRunSuccessEvent successEvent = new TaskRunSuccessEvent(task);
        successEvent.setMessage(String.format("任务：%d执行成功", task.getId()));
        successEvent.setTriggeringTime(task
                .getTrigger()
                .getTriggeringTime());
        return successEvent;
    }

    public static TaskRunErrorEvent newRunErrorEvent(AutoJobTask task, Throwable throwable) {
        TaskRunErrorEvent errorEvent = new TaskRunErrorEvent(task);
        errorEvent.setMessage(String.format("任务：%d执行异常：%s", task.getId(), DefaultValueUtil.defaultObjectWhenNull(throwable, "")));
        if (throwable != null) {
            errorEvent.setErrorStack(ExceptionUtils.getStackTrace(throwable));
        }
        errorEvent.setLevel("ERROR");
        return errorEvent;
    }

    public static TaskFinishedEvent newFinishedEvent(AutoJobTask task) {
        TaskFinishedEvent finishedEvent = new TaskFinishedEvent(task);
        finishedEvent.setMessage(String.format("任务：%d执行完成", task.getId()));
        return finishedEvent;
    }

    public static TaskForbiddenEvent newForbiddenEvent(AutoJobTask task) {
        TaskForbiddenEvent forbiddenEvent = new TaskForbiddenEvent(task);
        forbiddenEvent.setMessage(String.format("任务：%d被禁止运行", task.getId()));
        forbiddenEvent.setLevel("ERROR");
        return forbiddenEvent;
    }


    public static TaskErrorEvent newErrorEvent(AutoJobTask task) {
        TaskErrorEvent errorEvent = new TaskErrorEvent(task);
        errorEvent.setMessage(String.format("任务：%d执行失败", task.getId()));
        return errorEvent;
    }

    public static TaskTransferEvent newTaskTransferEvent(AutoJobTask task, ClusterNode transferTo, String logKey) {
        TaskTransferEvent event = new TaskTransferEvent();
        event.setTransferTo(transferTo);
        event.setTask(task);
        event.setLogKey(logKey);
        event.setMessage(String.format("任务：%d被转移到节点：%s运行", task.getId(), transferTo.toString()));
        return event;
    }

    public static TaskReceivedEvent newTaskReceivedEvent(AutoJobTask task, ClusterNode transferFrom) {
        TaskReceivedEvent event = new TaskReceivedEvent();
        event.setTask(task);
        event.setTransferFrom(transferFrom);
        event.setMessage(String.format("接收到来自节点：%s的转移任务：%d", transferFrom.toString(), task.getId()));
        return event;
    }


}
