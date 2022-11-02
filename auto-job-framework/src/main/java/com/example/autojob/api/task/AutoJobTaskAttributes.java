package com.example.autojob.api.task;

import com.example.autojob.skeleton.framework.task.AutoJobTask;
import lombok.Data;

/**
 * API的任务属性
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/27 11:09
 * @Email 1158055613@qq.com
 */
@Data
public abstract class AutoJobTaskAttributes {
    /**
     * 任务Id
     */
    protected Long id;
    /**
     * 任务别名
     */
    protected String alias;
    /**
     * 注解ID
     */
    protected Long annotationId;
    /**
     * 任务类型
     */
    protected String type;
    /**
     * 归属于
     */
    protected Long belongTo;
    /**
     * 任务优先级
     */
    protected Integer taskLevel = -1;
    /**
     * 是否是子任务
     */
    protected Boolean isChildTask = false;
    /**
     * 触发器
     */
    protected AutoJobTriggerAttributes triggerAttributes;


    public AutoJobTaskAttributes(AutoJobTask task) {
        if (task != null) {
            id = task.getId();
            alias = task.getAlias();
            annotationId = task.getAnnotationId();
            if(task.getType() != null) {
                type = task
                        .getType()
                        .toString();
            }
            belongTo = task.getBelongTo();
            taskLevel = task.getTaskLevel();
            isChildTask = task.getIsChildTask();
            if (task.getTrigger() != null) {
                triggerAttributes = new AutoJobTriggerAttributes(task.getTrigger());
            }
        }
    }

    public abstract AutoJobTask convert();
}
