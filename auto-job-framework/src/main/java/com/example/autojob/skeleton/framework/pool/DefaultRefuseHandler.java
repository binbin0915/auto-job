package com.example.autojob.skeleton.framework.pool;

import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.alert.AlertEventHandlerDelegate;
import com.example.autojob.skeleton.model.alert.event.AlertEventFactory;
import com.example.autojob.skeleton.model.alert.event.TaskRefuseHandleEvent;
import com.example.autojob.skeleton.model.task.TaskExecutable;

/**
 * 默认拒绝处理器
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/17 11:20
 */
public class DefaultRefuseHandler implements IRefuseHandler {

    @Override
    public void doHandle(Executable executable, RunnablePostProcessor runnablePostProcessor, AbstractAutoJobPool pool) {
        if (executable instanceof TaskExecutable) {
            AutoJobTask task = ((TaskExecutable) executable).getAutoJobTask();
            TaskRefuseHandleEvent event = AlertEventFactory.newTaskRefuseHandleEvent(task);
            AlertEventHandlerDelegate
                    .getInstance()
                    .doHandle(event, true);
        }
    }
}
