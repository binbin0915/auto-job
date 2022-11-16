package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.lang.WithDaemonThread;
import com.example.autojob.skeleton.db.entity.AutoJobTaskEntity;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.register.AutoJobRegisterRefusedException;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * DB task调度器
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/20 18:19
 */
@Slf4j
public class AutoJobDBTaskScheduler extends AbstractScheduler implements WithDaemonThread {
    private final ScheduleTaskUtil dbScheduleThread;

    /**
     * 调度器的通用构造方法，框架自动注册调度器时会执行该构造方法
     *
     * @param executorPool 执行器池
     * @param register     注册器
     * @param configHolder 配置源
     * @author Huang Yongxiang
     * @date 2022/8/19 15:18
     */
    public AutoJobDBTaskScheduler(AutoJobTaskExecutorPool executorPool, IAutoJobRegister register, AutoJobConfigHolder configHolder) {
        super(executorPool, register, configHolder);
        this.dbScheduleThread = ScheduleTaskUtil.build(false, "dbScheduleThread");
    }


    @Override
    public void startWork() {
        //log.warn("DB调度器已启动");
        ScheduleTaskUtil
                .build(true, "DBTaskScheduler")
                .EFixedRateTask(() -> {
                    try {
                        List<AutoJobTaskEntity> taskEntities = AutoJobMapperHolder.TASK_ENTITY_MAPPER.selectNearTask(5, TimeUnit.SECONDS);
                        //log.warn("执行DB调度器");
                        if (taskEntities == null || taskEntities.size() == 0) {
                            return;
                        }
                        log.debug("查找到{}个DB任务", taskEntities.size());
                        List<AutoJobTask> tasks = taskEntities
                                .stream()
                                .map(EntityConvertor::taskEntity2Task)
                                .collect(Collectors.toList());
                        for (AutoJobTask task : tasks) {
                            if (task.getTrigger() != null && !task
                                    .getTrigger()
                                    .getIsPause()) {
                                try {
                                    //log.info("注册DB任务：{}", task.getId());
                                    register.registerTask(task);
                                } catch (AutoJobRegisterRefusedException e) {
                                    AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.pauseTaskById(task.getId());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void execute() {
        startWork();
    }

    @Override
    public void destroy() {
        dbScheduleThread.shutdownNow();
    }
}
