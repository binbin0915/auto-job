package com.example.autojob.skeleton.model.handler;

import com.example.autojob.skeleton.annotation.AutoJob;
import com.example.autojob.skeleton.enumerate.SchedulingStrategy;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobWrapper;
import com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder;
import com.example.autojob.util.bean.ObjectUtil;
import com.example.autojob.util.convert.DefaultValueUtil;
import com.example.autojob.util.id.IdGenerator;

import java.lang.reflect.Method;

/**
 * AutoJob注解包装器
 *
 * @author Huang Yongxiang
 * @date 2022-12-03 18:04
 * @email 1158055613@qq.com
 */
public class AutoJobAnnotationWrapper implements AutoJobWrapper {
    @Override
    public AutoJobTask wrapper(Method method) {
        AutoJob autoJob = method.getAnnotation(AutoJob.class);
        if (autoJob == null) {
            return null;
        }
        long taskId;
        if (autoJob.id() < 0 || autoJob.asType() == AutoJobTask.TaskType.DB_TASK) {
            taskId = IdGenerator.getNextIdAsLong();
        } else {
            taskId = autoJob.id();
        }
        return new AutoJobMethodTaskBuilder(method.getDeclaringClass(), method.getName())
                .setTaskId(taskId)
                .setTaskAlias(DefaultValueUtil.chooseString("Default".equals(autoJob.alias()), method.getName(), autoJob.alias()))
                .setParams(autoJob.attributes())
                .setMethodObjectFactory(ObjectUtil.getClassInstance(autoJob.methodObjectFactory()))
                .setTaskType(autoJob.asType())
                .setSchedulingStrategy(autoJob.schedulingStrategy())
                .setTaskLevel(autoJob.taskLevel())
                .build()
                .setTrigger(autoJob
                        .schedulingStrategy()
                        .createTrigger(taskId, autoJob))
                .setAnnotationId((Long) DefaultValueUtil.chooseNumber(autoJob.id() == -1 || autoJob.asType() == AutoJobTask.TaskType.MEMORY_TASk, null, autoJob.id()))
                .setIsChildTask(autoJob.schedulingStrategy() == SchedulingStrategy.AS_CHILD_TASK);
    }

}
