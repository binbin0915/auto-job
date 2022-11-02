package com.example.autojob.skeleton.model.alert.handler;

import com.example.autojob.skeleton.lifecycle.ITaskEventHandler;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRunErrorEvent;
import com.example.autojob.skeleton.model.alert.AlertEventHandlerDelegate;
import com.example.autojob.skeleton.model.alert.event.AlertEventFactory;

/**
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/28 17:33
 */
public class TaskRunErrorEventHandler implements ITaskEventHandler<TaskRunErrorEvent> {
    @Override
    public void doHandle(TaskRunErrorEvent event) {
        AlertEventHandlerDelegate.getInstance().doHandle(AlertEventFactory.newTaskRunErrorAlertEvent(event.getTask(), event.getErrorStack()));
    }
}
