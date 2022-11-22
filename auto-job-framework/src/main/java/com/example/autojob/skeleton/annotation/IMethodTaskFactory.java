package com.example.autojob.skeleton.annotation;

import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.model.task.method.MethodTask;

/**
 * 任务工厂
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/18 12:53
 */
public interface IMethodTaskFactory {
    MethodTask newTask(AutoJobConfigHolder configHolder, Class<?> methodClass, String methodName);
}
