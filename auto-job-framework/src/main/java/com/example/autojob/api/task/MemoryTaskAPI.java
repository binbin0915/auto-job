package com.example.autojob.api.task;

import com.example.autojob.skeleton.annotation.AutoJobRPCService;
import com.example.autojob.skeleton.model.builder.AutoJobTriggerFactory;
import com.example.autojob.skeleton.framework.container.MemoryTaskContainer;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobTrigger;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.skeleton.model.task.script.ScriptTask;
import com.example.autojob.util.bean.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Memory任务一站式API，该类能被框架内置RPC客户端调用
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/17 15:40
 */
@AutoJobRPCService("MemoryTaskAPI")
@Slf4j
public class MemoryTaskAPI implements AutoJobAPI {
    private final MemoryTaskContainer container = AutoJobApplication
            .getInstance()
            .getMemoryTaskContainer();

    @Override
    public List<AutoJobTaskAttributes> page(Integer pageNum, Integer size) {
        List<AutoJobTask> tasks = container.list();
        if (tasks.size() == 0) {
            return Collections.emptyList();
        }
        int skip = (pageNum - 1) * size;
        int startIndex = Math.min(tasks.size(), skip);
        return tasks
                .subList(startIndex, Math.min(tasks.size(), startIndex + size))
                .stream()
                .map(task -> {
                    if (task instanceof MethodTask) {
                        return new AutoJobMethodTaskAttributes((MethodTask) task);
                    }
                    else if(task instanceof ScriptTask){
                        return new AutoJobScriptTaskAttributes((ScriptTask) task);
                    }
                    return null;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Integer count() {
        return container.size();
    }

    @Override
    public Boolean registerTask(AutoJobTaskAttributes taskAttributes) {
        if (ObjectUtil.isNull(taskAttributes)) {
            return false;
        }
        AutoJobTask task = taskAttributes.convert();
        if (task.getTrigger() == null || task.getType() != AutoJobTask.TaskType.MEMORY_TASk) {
            return false;
        }
        try {
            container.insert(task);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean runTaskNow(AutoJobTaskAttributes taskAttributes) {
        if (ObjectUtil.isNull(taskAttributes)) {
            return false;
        }
        AutoJobTask task = taskAttributes.convert();
        if (task.getType() != AutoJobTask.TaskType.MEMORY_TASk) {
            return false;
        }
        AutoJobTrigger trigger = AutoJobTriggerFactory.newDelayTrigger(5, TimeUnit.SECONDS);
        task.setTrigger(trigger);
        try {
            container.insert(task);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public AutoJobTaskAttributes find(Long taskId) {
        AutoJobTask task = container.getById(taskId);
        if (task instanceof MethodTask) {
            return new AutoJobMethodTaskAttributes((MethodTask) task);
        }
        return null;
    }

    /**
     * 修改内存任务的触发器参数，注意该方法只允许修改cron-like表达式、重复次数、周期、触发时间、子任务、最大运行时长以及暂停调度
     *
     * @param taskId            要修改的任务
     * @param triggerAttributes 修改的信息
     * @return java.lang.Boolean
     * @author Huang Yongxiang
     * @date 2022/10/27 16:50
     */
    @Override
    public Boolean editTrigger(Long taskId, AutoJobTriggerAttributes triggerAttributes) {
        if (ObjectUtil.isNull(triggerAttributes)) {
            return false;
        }
        AutoJobTrigger trigger = triggerAttributes.convert();
        AutoJobTask task = container.getById(taskId);
        if (task == null || task.getTrigger() == null) {
            return false;
        }
        if (pause(taskId)) {
            //log.warn("任务开始修改");
            try {
                AutoJobTrigger editParams = new AutoJobTrigger();
                editParams.setCronExpression(trigger.getCronExpression());
                editParams.setRepeatTimes(trigger.getRepeatTimes());
                editParams.setCycle(trigger.getCycle());
                editParams.setTriggeringTime(trigger.getTriggeringTime());
                editParams.setChildTask(trigger.getChildTask());
                editParams.setMaximumExecutionTime(trigger.getMaximumExecutionTime());
                editParams.setIsPause(trigger.getIsPause());
                ObjectUtil.mergeObject(editParams, task.getTrigger());
            } finally {
                unpause(taskId);
            }
            //log.warn("任务修改完成");
            return true;
        }
        return false;
    }

    /**
     * 修改内存任务信息，任务ID，注解ID和任务类型不允许修改
     *
     * @param taskId         要修改的任务ID
     * @param taskAttributes 修改的内容
     * @return java.lang.Boolean
     * @author Huang Yongxiang
     * @date 2022/10/27 17:52
     */
    @Override
    public Boolean editTask(Long taskId, AutoJobTaskAttributes taskAttributes) {
        AutoJobTask edit = container.getById(taskId);
        if (edit == null || ObjectUtil.isNull(taskAttributes)) {
            return false;
        }
        AutoJobTask task = taskAttributes.convert();
        //这三个字段不允许修改
        task.setId(null);
        task.setType(null);
        task.setAnnotationId(null);
        if (ObjectUtil.isNull(task)) {
            return false;
        }
        if (pause(taskId)) {
            try {
                ObjectUtil.mergeObject(task, edit);
                return true;
            } finally {
                unpause(taskId);
            }
        }
        return false;
    }

    @Override
    public Boolean pause(Long taskId) {
        AutoJobTask task = container.getById(taskId);
        if (task == null) {
            return false;
        }
        task
                .getTrigger()
                .setIsPause(true);
        AutoJobApplication
                .getInstance()
                .getRegister()
                .removeTask(taskId);
        return true;
    }

    @Override
    public Boolean unpause(Long taskId) {
        AutoJobTask task = container.getById(taskId);
        if (task == null) {
            return false;
        }
        task
                .getTrigger()
                .setIsPause(false);
        task
                .getTrigger()
                .refresh();
        return true;
    }

    @Override
    public Boolean delete(Long taskId) {
        return pause(taskId) && container.removeById(taskId) != null;
    }

    @Override
    public Boolean isExist(Long taskId) {
        return container.getById(taskId) != null;
    }
}
