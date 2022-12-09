package com.example.autojob.skeleton.model.handler;

import com.example.autojob.skeleton.annotation.FactoryAutoJob;
import com.example.autojob.skeleton.annotation.IMethodTaskFactory;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobWrapper;
import com.example.autojob.util.bean.ObjectUtil;

import java.lang.reflect.Method;

/**
 * FactoryAutoJob注解包装器
 *
 * @author Huang Yongxiang
 * @date 2022-12-03 18:06
 * @email 1158055613@qq.com
 */
public class FactoryAutoJobAnnotationWrapper implements AutoJobWrapper {
    @Override
    public AutoJobTask wrapper(Method method) {
        FactoryAutoJob factoryAutoJob = method.getAnnotation(FactoryAutoJob.class);
        if (factoryAutoJob == null) {
            return null;
        }
        IMethodTaskFactory methodTaskFactory = ObjectUtil.getClassInstance(factoryAutoJob.value());
        return methodTaskFactory.newTask(AutoJobApplication
                .getInstance()
                .getConfigHolder(), method);
    }

}
