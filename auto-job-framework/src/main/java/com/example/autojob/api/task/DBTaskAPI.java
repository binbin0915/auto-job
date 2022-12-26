package com.example.autojob.api.task;

import com.example.autojob.api.task.params.TaskEditParams;
import com.example.autojob.api.task.params.TriggerEditParams;
import com.example.autojob.skeleton.annotation.AutoJobRPCService;
import com.example.autojob.skeleton.db.AutoJobSQLException;
import com.example.autojob.skeleton.db.entity.AutoJobTaskEntity;
import com.example.autojob.skeleton.db.entity.AutoJobTriggerEntity;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.db.mapper.TransactionEntry;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
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
    public Boolean editTrigger(Long taskId, TriggerEditParams triggerEditParams) {
        if (ObjectUtil.isNull(triggerEditParams)) {
            return null;
        }
        if (pause(taskId)) {
            boolean flag = false;
            try {
                flag = AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateByTaskId(triggerEditParams, taskId) >= 0;
            } finally {
                unpause(taskId);
            }
            return flag;
        }
        return false;
    }

    @Override
    public Boolean bindingTrigger(Long taskId, AutoJobTrigger trigger) {
        if (taskId == null || trigger == null) {
            return false;
        }
        try {
            if (pause(taskId)) {
                trigger.setTaskId(taskId);
                AutoJobTriggerEntity entity = EntityConvertor.trigger2TriggerEntity(trigger);
                TransactionEntry insertTrigger = connection -> AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.insertList(Collections.singletonList(entity));
                TransactionEntry bindingTrigger = connection -> {
                    boolean flag = AutoJobMapperHolder.TASK_ENTITY_MAPPER.bindingTrigger(entity.getId(), taskId);
                    //绑定失败回滚
                    if (!flag) {
                        throw new AutoJobSQLException();
                    }
                    return 1;
                };
                return AutoJobMapperHolder.TASK_ENTITY_MAPPER.doTransaction(new TransactionEntry[]{insertTrigger, bindingTrigger});
            }
        } finally {
            unpause(taskId);
        }
        return false;
    }

    /**
     * 修改DB任务信息，任务ID，注解ID和任务类型不允许修改
     *
     * @param taskId         任务ID
     * @param taskEditParams 要修改的内容
     * @return java.lang.Boolean
     * @author Huang Yongxiang
     * @date 2022/10/27 17:52
     */
    @Override
    public Boolean editTask(Long taskId, TaskEditParams taskEditParams) {
        if (pause(taskId) && !ObjectUtil.isNull(taskEditParams)) {
            boolean flag = false;
            try {
                flag = AutoJobMapperHolder.TASK_ENTITY_MAPPER.updateById(taskEditParams, taskId) >= 0;
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
        AutoJobTask task = EntityConvertor.taskEntity2Task(AutoJobMapperHolder.TASK_ENTITY_MAPPER.selectById(taskId));
        if (task == null || task.getTrigger() == null) {
            return false;
        }
        task
                .getTrigger()
                .setIsPause(false);
        TransactionEntry updateTriggeringTime = connection -> {
            if (task
                    .getTrigger()
                    .getTriggeringTime() != null && task
                    .getTrigger()
                    .getTriggeringTime() < System.currentTimeMillis() && task
                    .getTrigger()
                    .refresh()) {
                AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateTriggeringTime(taskId, task
                        .getTrigger()
                        .getTriggeringTime());
            }
            return 1;
        };
        TransactionEntry unpause = connection -> {
            AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.unpauseTaskById(taskId);
            return 1;
        };
        return AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.doTransaction(new TransactionEntry[]{updateTriggeringTime, unpause});
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
