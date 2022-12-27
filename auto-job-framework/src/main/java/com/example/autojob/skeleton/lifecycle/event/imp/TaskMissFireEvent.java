package com.example.autojob.skeleton.lifecycle.event.imp;

import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.event.TaskEvent;

/**
 * 任务MissFire事件
 *
 * @author Huang Yongxiang
 * @date 2022-12-27 11:27
 * @email 1158055613@qq.com
 */
public class TaskMissFireEvent extends TaskEvent {
    public TaskMissFireEvent(AutoJobTask task) {
        super(task);
        this.level = "WARN";
    }
}
