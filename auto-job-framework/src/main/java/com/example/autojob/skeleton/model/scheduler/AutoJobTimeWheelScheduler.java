package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lang.WithDaemonThread;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.skeleton.model.tq.AutoJobTimeWheel;
import com.example.autojob.util.id.SystemClock;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description 时间轮调度器
 * @Author Huang Yongxiang
 * @Date 2022/08/07 17:51
 */
@Slf4j
public class AutoJobTimeWheelScheduler extends AbstractScheduler implements WithDaemonThread {
    private final AutoJobTimeWheel timeWheel;

    private final ScheduleTaskUtil startSchedulerThread;

    private final ScheduleTaskUtil transferSchedulerThread;

    private static final int ADVANCE_TIME = 5000;


    public AutoJobTimeWheelScheduler(AutoJobTaskExecutorPool executorPool, IAutoJobRegister register, AutoJobConfigHolder configHolder) {
        super(executorPool, register, configHolder);
        this.startSchedulerThread = ScheduleTaskUtil.build(false, "startSchedulerThread");
        this.transferSchedulerThread = ScheduleTaskUtil.build(false, "transferSchedulerThread");
        this.timeWheel = new AutoJobTimeWheel();
    }


    @Override
    public void startWork() {
        Runnable start = () -> {
            try {
                int second = (int) ((SystemClock.now() / 1000) % 60);
                //log.info("时间轮下标：{}", second);
                List<AutoJobTask> tasks = timeWheel.getSecondTasks(second);
                //当前时间前3S的任务也会被执行，防止任务周期过短导致的missFire
                for (int i = 1; i <= 3; i++) {
                    tasks.addAll(timeWheel.getSecondTasks((int) (((SystemClock.now() - 1000 * i) / 1000) % 60)));
                }
                if (tasks.size() > 0) {
                    //log.warn("得到下标：{}下{}条任务", second, tasks.size());
                    tasks.forEach(item -> {
                        //log.warn("任务：{}时间轮触发成功", item.getId());
                        if (item.getType() == AutoJobTask.TaskType.DB_TASK && lock(item.getId())) {
                            submitTask(item);
                        } else if (item.getType() == AutoJobTask.TaskType.MEMORY_TASk) {
                            submitTask(item);
                        }
                    });
                }
                //if (second == 59) {
                //    List<AutoJobTask> missFireTask = timeWheel.getAllTasks();
                //    if (missFireTask.size() > 0) {
                //        log.warn("本轮存在：{}个任务没被触发", missFireTask.size());
                //        timeWheel.clear();
                //    }
                //}

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Runnable schedule = () -> {
            try {
                //提前10S将任务加载进时间轮
                AutoJobTask headTask = register.readTask();
                if (headTask != null && (headTask.getIsStart() == null || !headTask.getIsStart())) {
                    if (headTask
                            .getTrigger()
                            .getIsPause()) {
                        register.takeTask();
                        return;
                    }
                    if (headTask
                            .getTrigger()
                            .isNearTriggeringTime(ADVANCE_TIME)) {
                        if (timeWheel.joinTask(headTask)) {
                            //log.warn("任务：{}，启动时间：{}调度进时间轮成功", headTask.getId(), DateUtils.formatDateTime(new Date(headTask
                            //        .getTrigger()
                            //        .getTriggeringTime())));
                        }
                        register.takeTask();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        startSchedulerThread.EFixedRateTask(start, 0, 1, TimeUnit.SECONDS);
        transferSchedulerThread.EFixedRateTask(schedule, 0, 1, TimeUnit.MILLISECONDS);
    }


    @Override
    public void execute() {
        startWork();
    }

    @Override
    public void destroy() {
        startSchedulerThread.shutdown();
        transferSchedulerThread.shutdown();
    }
}
