package com.example.autojob.skeleton.model.handler;

import com.example.autojob.skeleton.enumerate.SchedulingStrategy;
import com.example.autojob.skeleton.framework.container.MemoryTaskContainer;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Huang Yongxiang
 * @date 2022-12-04 10:59
 * @email 1158055613@qq.com
 */
public class MemoryTaskLoader implements AutoJobTaskLoader {
    @Override
    public int load(List<AutoJobTask> tasks) {
        MemoryTaskContainer container = AutoJobApplication
                .getInstance()
                .getMemoryTaskContainer();
        tasks.forEach(container::insert);
        List<AutoJobTask> soon = tasks
                .stream()
                .filter(task -> task.getSchedulingStrategy() != SchedulingStrategy.ONLY_SAVE && task.getSchedulingStrategy() != SchedulingStrategy.AS_CHILD_TASK && task
                        .getTrigger()
                        .isNearTriggeringTime(5000))
                .collect(Collectors.toList());
        soon.forEach(AutoJobApplication
                .getInstance()
                .getRegister()::registerTask);
        return soon.size();
    }
}
