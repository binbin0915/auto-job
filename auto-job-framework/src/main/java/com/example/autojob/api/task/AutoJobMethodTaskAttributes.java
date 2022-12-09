package com.example.autojob.api.task;

import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.executor.IMethodObjectFactory;
import com.example.autojob.skeleton.model.interpreter.AutoJobAttributeContext;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.util.bean.ObjectUtil;
import com.example.autojob.util.convert.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author Huang Yongxiang
 * @Date 2022/10/27 13:47
 * @Email 1158055613@qq.com
 */
@Getter
@Setter
public class AutoJobMethodTaskAttributes extends AutoJobTaskAttributes {
    private String methodClassName;
    /**
     * 运行方法名
     */
    private String methodName;
    /**
     * 参数字符串
     */
    private String paramsString;
    /**
     * 任务运行类工厂
     */
    private String methodObjectFactory;


    public AutoJobMethodTaskAttributes(MethodTask task) {
        super(task);
        if (task != null) {
            methodName = task.getMethodName();
            paramsString = task.getParamsString();
            methodClassName = task.getMethodClassName();
            if (task.getMethodObjectFactory() != null) {
                methodObjectFactory = task
                        .getMethodObjectFactory()
                        .getClass()
                        .getName();
            }
        }
    }

    @Override
    public MethodTask convert() {
        MethodTask methodTask = new MethodTask();
        if (!StringUtils.isEmpty(methodClassName)) {
            methodTask.setMethodClass(ObjectUtil.classPath2Class(methodClassName));
        }
        methodTask.setTaskLevel(taskLevel);
        if (triggerAttributes != null) {
            methodTask.setTrigger(triggerAttributes.convert());
        }
        methodTask.setAlias(alias);
        methodTask.setId(id);
        methodTask.setAnnotationId(annotationId);
        methodTask.setParamsString(paramsString);
        AutoJobAttributeContext context = new AutoJobAttributeContext(methodTask);
        methodTask.setParams(context.getAttributeEntity());
        if (!StringUtils.isEmpty(methodObjectFactory)) {
            methodTask.setMethodObjectFactory((IMethodObjectFactory) ObjectUtil.getClassInstanceObject(ObjectUtil.classPath2Class(methodObjectFactory)));
        }
        methodTask.setMethodName(methodName);
        methodTask.setType(AutoJobTask.TaskType.convert(type));
        methodTask.setBelongTo(belongTo);
        methodTask.setIsChildTask(isChildTask);
        return methodTask;
    }
}
