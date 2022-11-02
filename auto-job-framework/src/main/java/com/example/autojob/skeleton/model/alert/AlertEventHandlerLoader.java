package com.example.autojob.skeleton.model.alert;

import com.example.autojob.skeleton.framework.processor.IAutoJobLoader;
import com.example.autojob.skeleton.lifecycle.TaskEventHandlerDelegate;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRunErrorEvent;
import com.example.autojob.skeleton.model.alert.handler.TaskRunErrorEventHandler;
import com.example.autojob.skeleton.model.alert.event.ClusterCloseProtectedModelEvent;
import com.example.autojob.skeleton.model.alert.event.ClusterOpenProtectedModelAlertEvent;
import com.example.autojob.skeleton.model.alert.event.TaskRefuseHandleEvent;
import com.example.autojob.skeleton.model.alert.event.TaskRunErrorAlertEvent;
import com.example.autojob.skeleton.model.alert.handler.ClusterCloseProtectedModelEventHandler;
import com.example.autojob.skeleton.model.alert.handler.ClusterOpenProtectedModelEventHandler;
import com.example.autojob.skeleton.model.alert.handler.TaskRefuseHandleEventHandler;
import com.example.autojob.skeleton.model.alert.handler.TaskRunErrorAlertEventHandler;

/**
 * @Description 报警事件加载器
 * @Author Huang Yongxiang
 * @Date 2022/07/30 17:11
 */
public class AlertEventHandlerLoader implements IAutoJobLoader {
    @Override
    public void load() {
        TaskEventHandlerDelegate.getInstance().addHandler(TaskRunErrorEvent.class,new TaskRunErrorEventHandler());
        AlertEventHandlerDelegate.getInstance().addHandler(TaskRunErrorAlertEvent.class, new TaskRunErrorAlertEventHandler());
        AlertEventHandlerDelegate.getInstance().addHandler(ClusterOpenProtectedModelAlertEvent.class, new ClusterOpenProtectedModelEventHandler());
        AlertEventHandlerDelegate.getInstance().addHandler(ClusterCloseProtectedModelEvent.class, new ClusterCloseProtectedModelEventHandler());
        AlertEventHandlerDelegate.getInstance().addHandler(TaskRefuseHandleEvent.class, new TaskRefuseHandleEventHandler());
    }
}
