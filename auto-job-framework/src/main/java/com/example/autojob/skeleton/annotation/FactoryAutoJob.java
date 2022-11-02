package com.example.autojob.skeleton.annotation;

import com.example.autojob.skeleton.model.task.method.MethodTaskFactory;

import java.lang.annotation.*;

/**
 * 工厂任务注解，被该注解注释的方法将被认为是一个AutoJob，同时该任务的各项参数实现将由用户通过接口实现，该注解优先级比@AutoJob更高，可使用{@link MethodTaskFactory}帮助你
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/18 12:56
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FactoryAutoJob {
    Class<? extends IMethodTaskFactory> value();
}
