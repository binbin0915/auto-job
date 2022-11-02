package com.example.autojob.skeleton.lifecycle.event.imp;

import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.event.TaskEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description 任务注册前的事件
 * @Auther Huang Yongxiang
 * @Date 2021/12/15 14:43
 */
@Setter
@Getter
public class TaskBeforeRegisterEvent extends TaskEvent {
    public TaskBeforeRegisterEvent(AutoJobTask task) {
        super(task);
        level="INFO";
    }
}
