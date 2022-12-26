package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.container.MemoryTaskContainer;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.register.AutoJobRegisterRefusedException;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author Huang Yongxiang
 * @Date 2022/10/14 15:55
 */
@Slf4j
public class AutoJobMemoryTaskScheduler extends AbstractScheduler {
    private ScheduleTaskUtil scheduleTaskUtil;

    /**
     * 调度器的通用构造方法，框架自动注册调度器时会执行该构造方法
     *
     * @param executorPool 执行器池
     * @param register     注册器
     * @param configHolder 配置源
     * @author Huang Yongxiang
     * @date 2022/8/19 15:18
     */
    public AutoJobMemoryTaskScheduler(AutoJobTaskExecutorPool executorPool, IAutoJobRegister register, AutoJobConfigHolder configHolder) {
        super(executorPool, register, configHolder);
    }

    @Override
    public void execute() {
        log.debug("内存任务调度器启动");
        MemoryTaskContainer memoryTaskContainer = AutoJobApplication
                .getInstance()
                .getMemoryTaskContainer();
        scheduleTaskUtil = ScheduleTaskUtil.build(true, "memoryTaskScheduler");
        scheduleTaskUtil.EFixedRateTask(() -> {
            //log.info("running");
            List<AutoJobTask> tasks = memoryTaskContainer.getFutureRun(5, TimeUnit.SECONDS);
            //log.info("查找到{}个Memory任务", tasks.size());
            if (tasks.size() > 0) {
                for (AutoJobTask task : tasks) {
                    if (task.getTrigger() != null && !task
                            .getTrigger()
                            .getIsPause()) {
                        try {
                            //log.warn("注册任务：{}", task.getId());
                            register.registerTask(task);
                        } catch (AutoJobRegisterRefusedException e) {
                            memoryTaskContainer.remove(task);
                        }
                    }
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() {
        scheduleTaskUtil.shutdown();
    }

}
