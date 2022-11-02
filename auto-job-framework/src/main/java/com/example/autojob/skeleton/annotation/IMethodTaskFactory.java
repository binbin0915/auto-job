package com.example.autojob.skeleton.annotation;

import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.util.io.PropertiesHolder;

/**
 * 任务工厂
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/18 12:53
 */
public interface IMethodTaskFactory {
    MethodTask newTask(PropertiesHolder propertiesHolder, Class<?> methodClass, String methodName);
}
