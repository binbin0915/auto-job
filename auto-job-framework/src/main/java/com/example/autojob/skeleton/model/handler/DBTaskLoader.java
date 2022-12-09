package com.example.autojob.skeleton.model.handler;

import com.example.autojob.skeleton.db.entity.AutoJobTaskEntity;
import com.example.autojob.skeleton.db.entity.AutoJobTriggerEntity;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.db.mapper.TransactionEntry;
import com.example.autojob.skeleton.enumerate.SchedulingStrategy;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Huang Yongxiang
 * @date 2022-12-04 10:59
 * @email 1158055613@qq.com
 */
public class DBTaskLoader implements AutoJobTaskLoader {
    @Override
    public int load(List<AutoJobTask> tasks) {
        List<AutoJobTriggerEntity> triggerEntities = new ArrayList<>();
        List<AutoJobTaskEntity> taskEntities = new ArrayList<>();
        tasks.forEach(task -> {
            AutoJobTriggerEntity triggerEntity = EntityConvertor.trigger2TriggerEntity(task.getTrigger());
            triggerEntities.add(triggerEntity);
            AutoJobTaskEntity taskEntity = EntityConvertor.task2TaskEntity(task, triggerEntity.getId());
            taskEntities.add(taskEntity);
        });
        TransactionEntry insertTriggers = connection -> AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.insertList(triggerEntities);
        TransactionEntry insertTasks = connection -> AutoJobMapperHolder.TASK_ENTITY_MAPPER.insertList(taskEntities);
        AutoJobMapperHolder.TASK_ENTITY_MAPPER.doTransaction(new TransactionEntry[]{insertTriggers, insertTasks});
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
