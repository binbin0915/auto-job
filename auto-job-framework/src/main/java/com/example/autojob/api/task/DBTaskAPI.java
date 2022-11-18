package com.example.autojob.api.task;

import com.example.autojob.skeleton.annotation.AutoJobRPCService;
import com.example.autojob.skeleton.db.entity.AutoJobTaskEntity;
import com.example.autojob.skeleton.db.entity.AutoJobTriggerEntity;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.db.mapper.TransactionEntry;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobTrigger;
import com.example.autojob.skeleton.model.builder.AutoJobTriggerFactory;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.skeleton.model.task.script.ScriptTask;
import com.example.autojob.util.bean.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 提供DB任务的一站式API
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/17 13:51
 */
@AutoJobRPCService("DBTaskAPI")
@Slf4j
public class DBTaskAPI implements AutoJobAPI {
    private final IAutoJobRegister register = AutoJobApplication
            .getInstance()
            .getRegister();

    @Override
    public List<AutoJobTaskAttributes> page(Integer pageNum, Integer size) {
        return AutoJobMapperHolder.TASK_ENTITY_MAPPER
                .page(pageNum, size)
                .stream()
                .map(EntityConvertor::taskEntity2Task)
                .map(task -> {
                    if (task instanceof MethodTask) {
                        return new AutoJobMethodTaskAttributes((MethodTask) task);
                    } else if (task instanceof ScriptTask) {
                        return new AutoJobScriptTaskAttributes((ScriptTask) task);
                    }
                    return null;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Integer count() {
        return AutoJobMapperHolder.TASK_ENTITY_MAPPER.count();
    }

    @Override
    public Boolean registerTask(AutoJobTaskAttributes taskAttributes) {
        if (ObjectUtil.isNull(taskAttributes)) {
            return false;
        }
        AutoJobTask task = taskAttributes.convert();
        if (task.getTrigger() == null || task.getType() != AutoJobTask.TaskType.DB_TASK) {
            return false;
        }
        AutoJobTriggerEntity triggerEntity = EntityConvertor.trigger2TriggerEntity(task.getTrigger());
        AutoJobTaskEntity taskEntity = EntityConvertor.task2TaskEntity(task, triggerEntity.getId());
        TransactionEntry insertTrigger = connection -> AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.insertList(Collections.singletonList(triggerEntity));
        TransactionEntry insertTask = connection -> AutoJobMapperHolder.TASK_ENTITY_MAPPER.insertList(Collections.singletonList(taskEntity));
        return AutoJobMapperHolder.TASK_ENTITY_MAPPER.doTransaction(new TransactionEntry[]{insertTask, insertTrigger});
    }


    @Override
    public Boolean runTaskNow(AutoJobTaskAttributes taskAttributes) {
        if (ObjectUtil.isNull(taskAttributes)) {
            return false;
        }
        AutoJobTask task = taskAttributes.convert();
        if (task.getType() != AutoJobTask.TaskType.DB_TASK) {
            return false;
        }
        AutoJobTrigger trigger = AutoJobTriggerFactory.newDelayTrigger(5, TimeUnit.SECONDS);
        taskAttributes.setTriggerAttributes(new AutoJobTriggerAttributes(trigger));
        task.setTrigger(trigger);
        registerTask(taskAttributes);
        trigger.refresh();
        return register.registerTask(task);
    }

    @Override
    public AutoJobTaskAttributes find(Long taskId) {
        AutoJobTask task = EntityConvertor.taskEntity2Task(AutoJobMapperHolder.TASK_ENTITY_MAPPER.selectById(taskId));
        if (task instanceof MethodTask) {
            return new AutoJobMethodTaskAttributes((MethodTask) task);
        } else if (task instanceof ScriptTask) {
            return new AutoJobScriptTaskAttributes((ScriptTask) task);
        }
        return null;
    }

    @Override
    public Boolean editTrigger(Long taskId, AutoJobTriggerAttributes triggerAttributes) {
        if (ObjectUtil.isNull(triggerAttributes)) {
            return null;
        }
        if (pause(taskId)) {
            boolean flag = false;
            try {
                flag = AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateByTaskId(EntityConvertor.trigger2TriggerEntity(triggerAttributes.convert()), taskId) >= 0;
            } finally {
                unpause(taskId);
            }
            return flag;
        }
        return false;
    }

    /**
     * 修改DB任务信息，任务ID，注解ID和任务类型不允许修改
     *
     * @param taskId         任务ID
     * @param taskAttributes 要修改的内容
     * @return java.lang.Boolean
     * @author Huang Yongxiang
     * @date 2022/10/27 17:52
     */
    @Override
    public Boolean editTask(Long taskId, AutoJobTaskAttributes taskAttributes) {
        if (pause(taskId) && !ObjectUtil.isNull(taskAttributes)) {
            boolean flag = false;
            try {
                AutoJobTask task = taskAttributes.convert();
                task.setId(null);
                task.setType(null);
                task.setAnnotationId(null);
                if (ObjectUtil.isNull(task)) {
                    return false;
                }
                flag = AutoJobMapperHolder.TASK_ENTITY_MAPPER.updateById(EntityConvertor.task2TaskEntity(task, null), taskId) >= 0;
            } finally {
                unpause(taskId);
            }
            return flag;
        }
        return false;
    }

    @Override
    public Boolean pause(Long taskId) {
        register.removeTask(taskId);
        return AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.pauseTaskById(taskId);
    }

    @Override
    public Boolean unpause(Long taskId) {
        return AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.unpauseTaskById(taskId);
    }

    @Override
    public Boolean delete(Long taskId) {
        TransactionEntry deleteTask = (connection) -> AutoJobMapperHolder.TASK_ENTITY_MAPPER.deleteById(taskId) ? 1 : 0;
        TransactionEntry deleteTrigger = connection -> AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.deleteByTaskIds(Collections.singletonList(taskId));
        return AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.doTransaction(new TransactionEntry[]{deleteTask, deleteTrigger});
    }

    @Override
    public Boolean isExist(Long taskId) {
        return !ObjectUtil.isNull(find(taskId));
    }
}