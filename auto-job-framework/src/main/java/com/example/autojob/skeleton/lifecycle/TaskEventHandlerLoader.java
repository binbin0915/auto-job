package com.example.autojob.skeleton.lifecycle;

import com.example.autojob.logging.model.AutoJobLogContext;
import com.example.autojob.skeleton.framework.processor.IAutoJobLoader;
import com.example.autojob.skeleton.lifecycle.event.TaskEvent;

/**
 * 任务事件处理器加载器
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/13 11:08
 */
public class TaskEventHandlerLoader implements IAutoJobLoader {
    @Override
    public void load() {
        TaskEventHandlerDelegate
                .getInstance()
                .addHandler(TaskEvent.class, AutoJobLogContext
                        .getInstance()
                        .getLogManager());
    }
}
