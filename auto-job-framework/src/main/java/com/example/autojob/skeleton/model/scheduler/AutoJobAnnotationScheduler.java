package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.annotation.AutoJob;
import com.example.autojob.skeleton.annotation.AutoJobScan;
import com.example.autojob.skeleton.annotation.FactoryAutoJob;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.handler.AbstractAnnotationTaskHandler;
import com.example.autojob.skeleton.model.handler.AutoJobAnnotationTaskHandler;
import com.example.autojob.skeleton.model.handler.AutoJobAnnotationWrapper;
import com.example.autojob.skeleton.model.handler.FactoryAutoJobAnnotationWrapper;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import lombok.extern.slf4j.Slf4j;

/**
 * 注解调度器
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/15 15:50
 */
@Slf4j
public class AutoJobAnnotationScheduler extends AbstractScheduler {
    public AutoJobAnnotationScheduler(AutoJobTaskExecutorPool executorPool, IAutoJobRegister register, AutoJobConfigHolder configHolder) {
        super(executorPool, register, configHolder);
    }

    private int handleAnnotationTask() {
        AbstractAnnotationTaskHandler autoJobHandler = new AutoJobAnnotationTaskHandler(new AutoJobAnnotationWrapper(), AutoJob.class);
        AbstractAnnotationTaskHandler factoryAutoJobHandler = new AutoJobAnnotationTaskHandler(new FactoryAutoJobAnnotationWrapper(), FactoryAutoJob.class);
        Class<?> application = AutoJobApplication
                .getInstance()
                .getApplication();
        AutoJobScan autoJobScan = application.getAnnotation(AutoJobScan.class);
        if (autoJobScan != null) {
            return autoJobHandler.handle(autoJobScan.value()) + factoryAutoJobHandler.handle(autoJobScan.value());
        }
        return autoJobHandler.handle() + factoryAutoJobHandler.handle();
    }

    @Override
    public int getSchedulerLevel() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void execute() {
        if (!configHolder
                .getAutoJobConfig()
                .getEnableAnnotation()) {
            return;
        }
        int count = 0;
        count += handleAnnotationTask();
        log.info("注解调度器成功注册{}个任务", count);
    }


}
