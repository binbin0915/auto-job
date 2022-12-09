package com.example.autojob.skeleton.enumerate;

import com.example.autojob.skeleton.annotation.AutoJob;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.config.AutoJobConfig;
import com.example.autojob.skeleton.framework.task.AutoJobTrigger;
import com.example.autojob.util.convert.DateUtils;
import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.cron.util.CronUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 调度策略
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/26 17:31
 */
@Slf4j
public enum SchedulingStrategy {
    /**
     * 仅作保存，该任务必须为DB任务且该策略下任务只会保存到数据库，不会创建触发器
     */
    ONLY_SAVE {
        public AutoJobTrigger createTrigger(long taskId, AutoJob autoJob) {
            return null;
        }

        public AutoJobTrigger createTrigger(long taskId, String startTime, int repeatTimes, long cycle, TimeUnit cycleUnit) {
            return null;
        }

        public AutoJobTrigger createTrigger(long taskId, String cronExpression, int repeatTimes) {
            return null;
        }
    },
    /**
     * 加入调度，该策略对DB任务和内存任务都有效，该策略会使用给定配置创建一个触发器，如果没有配置触发器相关信息将会按照配置的默认延迟时间执行一次
     */
    JOIN_SCHEDULING {
        public AutoJobTrigger createTrigger(long taskId, AutoJob autoJob) {
            List<Long> childTaskList = null;
            if (!StringUtils.isEmpty(autoJob.childTasksId())) {
                String[] childTask = autoJob
                        .childTasksId()
                        .split(",");
                childTaskList = Arrays
                        .stream(childTask)
                        .map(Long::new)
                        .collect(Collectors.toList());
            }
            if (!StringUtils.isEmpty(autoJob.cronExpression())) {
                return new AutoJobTrigger(autoJob.cronExpression(), autoJob.repeatTimes())
                        .setTaskId(taskId)
                        .setChildTask(childTaskList);
            }
            if (!StringUtils.isEmpty(autoJob.startTime())) {
                long triggeringTime = DateUtils
                        .parseDate(autoJob.startTime())
                        .getTime();
                return new AutoJobTrigger(triggeringTime, autoJob.repeatTimes(), autoJob
                        .cycleUnit()
                        .toMillis(autoJob.cycle()))
                        .setTaskId(taskId)
                        .setChildTask(childTaskList);
            }
            if (autoJob.defaultStartTime() != StartTime.EMPTY) {
                return new AutoJobTrigger(autoJob
                        .defaultStartTime()
                        .valueOf(), autoJob.repeatTimes(), autoJob
                        .cycleUnit()
                        .toMillis(autoJob.cycle()))
                        .setTaskId(taskId)
                        .setChildTask(childTaskList);
            }
            long defaultDelay = System.currentTimeMillis() + (long) (AutoJobApplication
                    .getInstance()
                    .getConfigHolder()
                    .getAutoJobConfig()
                    .getAnnotationDefaultDelayTime() * 60 * 1000);

            return new AutoJobTrigger(defaultDelay, 0, 0L)
                    .setTaskId(taskId)
                    .setChildTask(childTaskList);
        }

        public AutoJobTrigger createTrigger(long taskId, String startTime, int repeatTimes, long cycle, TimeUnit cycleUnit) {
            if (repeatTimes > 0 && cycle <= 0) {
                throw new IllegalArgumentException("重复次数大于0时周期必须大于0");
            }
            if (StringUtils.isEmpty(startTime)) {
                throw new NullPointerException();
            }
            long triggeringTime = DateUtils
                    .parseDate(startTime)
                    .getTime();
            return new AutoJobTrigger(triggeringTime, repeatTimes, cycleUnit.toMillis(cycle)).setTaskId(taskId);
        }

        public AutoJobTrigger createTrigger(long taskId, String cronExpression, int repeatTimes) {
            return new AutoJobTrigger(cronExpression, repeatTimes).setTaskId(taskId);
        }
    },
    /**
     * 延迟触发，该策略对DB任务和内存任务都有效，该策略下任务只会在给定延迟后触发一次，如果配置的有未来的触发时间，则会到时启动执行一次，否则使用默认延迟时间执行一次，这种策略下配置的重复次数将会失效
     */
    DELAY_SCHEDULE {
        public AutoJobTrigger createTrigger(long taskId, AutoJob autoJob) {
            List<Long> childTaskList = null;
            if (!StringUtils.isEmpty(autoJob.childTasksId())) {
                String[] childTask = autoJob
                        .childTasksId()
                        .split(",");
                childTaskList = Arrays
                        .stream(childTask)
                        .map(Long::new)
                        .collect(Collectors.toList());
            }
            AutoJobConfig config = AutoJobApplication
                    .getInstance()
                    .getConfigHolder()
                    .getAutoJobConfig();
            if (!StringUtils.isEmpty(autoJob.cronExpression())) {
                return createTrigger(taskId, autoJob.cronExpression(), 0).setChildTask(childTaskList);
            } else if (!StringUtils.isEmpty(autoJob.startTime())) {
                return createTrigger(taskId, autoJob.startTime(), 0, 0, TimeUnit.MILLISECONDS).setChildTask(childTaskList);
            } else if (autoJob.defaultStartTime() != StartTime.EMPTY) {
                return new AutoJobTrigger(autoJob
                        .defaultStartTime()
                        .valueOf(), 0, 0)
                        .setTaskId(taskId)
                        .setChildTask(childTaskList);
            }

            return new AutoJobTrigger(System.currentTimeMillis() + (long) (config.getAnnotationDefaultDelayTime() * 60 * 1000), 0, 0)
                    .setTaskId(taskId)
                    .setChildTask(childTaskList);
        }

        public AutoJobTrigger createTrigger(long taskId, String startTime, int repeatTimes, long cycle, TimeUnit cycleUnit) {
            AutoJobConfig config = AutoJobApplication
                    .getInstance()
                    .getConfigHolder()
                    .getAutoJobConfig();
            if (!StringUtils.isEmpty(startTime)) {
                long triggeringTime = DateUtils
                        .parseDate(startTime)
                        .getTime();
                return new AutoJobTrigger(triggeringTime, 0, 0).setTaskId(taskId);
            }
            return new AutoJobTrigger(System.currentTimeMillis() + (long) (config.getAnnotationDefaultDelayTime() * 60 * 1000), 0, 0).setTaskId(taskId);
        }

        public AutoJobTrigger createTrigger(long taskId, String cronExpression, int repeatTimes) {
            AutoJobConfig config = AutoJobApplication
                    .getInstance()
                    .getConfigHolder()
                    .getAutoJobConfig();
            if (!StringUtils.isEmpty(cronExpression)) {
                long triggeringTime = CronUtil
                        .next(cronExpression, new Date())
                        .getTime();
                return new AutoJobTrigger(triggeringTime, 0, 0).setTaskId(taskId);
            }
            return new AutoJobTrigger(System.currentTimeMillis() + (long) (config.getAnnotationDefaultDelayTime() * 60 * 1000), 0, 0).setTaskId(taskId);
        }
    },

    /**
     * 子任务调度，子任务的调度依赖于父任务的调度，父任务每完成一次，子任务都将执行一次
     */
    AS_CHILD_TASK {
        public AutoJobTrigger createTrigger(long taskId, AutoJob autoJob) {
            List<Long> childTaskList = null;
            if (!StringUtils.isEmpty(autoJob.childTasksId())) {
                String[] childTask = autoJob
                        .childTasksId()
                        .split(",");
                childTaskList = Arrays
                        .stream(childTask)
                        .map(Long::new)
                        .collect(Collectors.toList());
            }
            return new AutoJobTrigger(Long.MAX_VALUE, 0, 0)
                    .setTaskId(taskId)
                    .setChildTask(childTaskList);
        }

        public AutoJobTrigger createTrigger(long taskId, String startTime, int repeatTimes, long cycle, TimeUnit cycleUnit) {
            return new AutoJobTrigger(Long.MAX_VALUE, 0, 0).setTaskId(taskId);
        }

        public AutoJobTrigger createTrigger(long taskId, String cronExpression, int repeatTimes) {
            return new AutoJobTrigger(Long.MAX_VALUE, 0, 0).setTaskId(taskId);
        }
    };

    public AutoJobTrigger createTrigger(long taskId, AutoJob autoJob) {
        throw new UnsupportedOperationException();
    }

    public AutoJobTrigger createTrigger(long taskId, String startTime, int repeatTimes, long cycle, TimeUnit cycleUnit) {
        throw new UnsupportedOperationException();
    }

    public AutoJobTrigger createTrigger(long taskId, String cronExpression, int repeatTimes) {
        throw new UnsupportedOperationException();
    }
}
