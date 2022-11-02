package com.example.autojob.skeleton.lifecycle.listener;

import com.example.autojob.skeleton.annotation.ProcessorLevel;
import com.example.autojob.skeleton.framework.processor.IAutoJobLoader;
import com.example.autojob.skeleton.lifecycle.event.TaskEvent;
import com.example.autojob.skeleton.lifecycle.manager.TaskEventManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author Huang Yongxiang
 * @Date 2022/07/05 14:09
 */
@Slf4j
@ProcessorLevel(Integer.MIN_VALUE)
public class TaskListenerLoader implements IAutoJobLoader {
    @Override
    public void load() {
        log.debug("加载任务事件默认监听器");
        TaskEventManager.getInstance().addTaskEventListener(new DefaultTaskEventListener(), TaskEvent.class);
    }
}
