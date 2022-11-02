package com.example.autojob.skeleton.framework.task;

import com.example.autojob.skeleton.lang.WithDaemonThread;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.db.mapper.TransactionEntry;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.skeleton.model.scheduler.AbstractScheduler;
import com.example.autojob.util.id.SystemClock;
import com.example.autojob.util.thread.SyncHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 任务运行上下文，提供运行时的任务操作以及对内存任务的一些操作
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/04 15:12
 */
@Slf4j
public class TaskRunningContext extends AbstractScheduler implements WithDaemonThread {
    /**
     * 执行任务的线程将绑定当前线程执行的ID到这里，子线程也将继承该值
     */
    private static final InheritableThreadLocal<Long> contextHolder = new InheritableThreadLocal<>();
    /**
     * 当前线程执行的任务
     */
    private static final InheritableThreadLocal<AutoJobTask> concurrentThreadTask = new InheritableThreadLocal<>();
    /**
     * 已加锁的DB任务
     */
    private static final Map<Long, Boolean> lockedMap = new ConcurrentHashMap<>();
    /**
     * 注解调度DB任务，该部分存放的是没有注解ID的DB任务，这部分任务将会在应用退出前删除
     */
    private static final Map<Long, AutoJobTask> annotationDBTask = new ConcurrentHashMap<>();
    /**
     * 正在运行的任务
     */
    private static final Map<Long, AutoJobTask> runningTask = new ConcurrentHashMap<>();
    /**
     * 正在运行的任务对应的线程
     */
    private static final Map<Long, Thread> runningThread = new ConcurrentHashMap<>();

    private boolean isStop = false;


    public TaskRunningContext(AutoJobTaskExecutorPool executorPool, IAutoJobRegister register, AutoJobConfigHolder configHolder) {
        super(executorPool, register, configHolder);
    }

    /**
     * 添加一个运行中的任务
     *
     * @param autoJobTask 正在运行的任务
     * @return void
     * @author Huang Yongxiang
     * @date 2022/8/22 17:07
     */
    public static void registerRunningTask(AutoJobTask autoJobTask) {
        runningTask.put(autoJobTask.getId(), autoJobTask);
        runningThread.put(autoJobTask.getId(), Thread.currentThread());
    }

    /**
     * 移除一个正在运行的任务
     *
     * @param autoJobTask 要被移除的任务
     * @return void
     * @author Huang Yongxiang
     * @date 2022/8/22 17:07
     */
    public static void removeRunningTask(AutoJobTask autoJobTask) {
        runningTask.remove(autoJobTask.getId());
        runningThread.remove(autoJobTask.getId());
    }

    public static boolean deleteNoIDDBTasks() {
        List<Long> ids = annotationDBTask.values().stream().map(AutoJobTask::getId).collect(Collectors.toList());
        TransactionEntry deleteTasks = (connection) -> AutoJobMapperHolder.TASK_ENTITY_MAPPER.deleteTasksByIds(ids);
        TransactionEntry deleteTriggers = connection -> AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.deleteByTaskIds(ids);
        return AutoJobMapperHolder.TASK_ENTITY_MAPPER.doTransaction(new TransactionEntry[]{deleteTriggers, deleteTasks});
    }

    /**
     * 尝试对已经加锁的DB任务解锁
     *
     * @return int 成功解锁的任务书
     * @author Huang Yongxiang
     * @date 2022/8/22 17:06
     */
    public static int unlock() {
        int count = 0;
        for (Map.Entry<Long, Boolean> entry : lockedMap.entrySet()) {
            if (entry.getValue()) {
                if (AutoJobMapperHolder.TASK_ENTITY_MAPPER.unLock(entry.getKey())) {
                    count++;
                }
            }
        }
        return count;
    }

    public static AutoJobTask getRunningTask(long taskId) {
        return runningTask.get(taskId);
    }

    /**
     * 尝试停止一个正在运行中的任务
     *
     * @param taskId 尝试停止的任务
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/8/22 17:11
     */
    public static boolean stopRunningTask(long taskId) {
        if (runningThread.containsKey(taskId)) {
            try {
                Thread runThread = runningThread.get(taskId);
                runThread.interrupt();
                if (runThread.isAlive() && runThread.isInterrupted()) {
                    runningTask.remove(taskId);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static InheritableThreadLocal<Long> getContextHolder() {
        return contextHolder;
    }

    public static Map<Long, Boolean> getOnLockMap() {
        return lockedMap;
    }

    public static Map<Long, AutoJobTask> getAnnotationDBTask() {
        return annotationDBTask;
    }

    public static Map<Long, AutoJobTask> getRunningTask() {
        return runningTask;
    }

    public static InheritableThreadLocal<AutoJobTask> getConcurrentThreadTask() {
        return concurrentThreadTask;
    }

    @Override
    public void execute() {
        startWork();
    }

    @Override
    public void destroy() {
        isStop = true;
    }

    /**
     * 提供任务过长限制，超过给定执行时长的任务将会尝试中断
     *
     * @return void
     * @author Huang Yongxiang
     * @date 2022/8/26 14:24
     */
    @Override
    public void startWork() {
        Thread stopLongTaskThread = new Thread(() -> {
            do {
                try {
                    SyncHelper.sleepQuietly(1, TimeUnit.MILLISECONDS);
                    for (Map.Entry<Long, AutoJobTask> entry : runningTask.entrySet()) {
                        if (entry.getValue().getTrigger().getStartRunTime() == 0) {
                            continue;
                        }
                        Long maximumExecutionTime = entry.getValue().getTrigger().getMaximumExecutionTime();
                        if (maximumExecutionTime != null && maximumExecutionTime > 0) {
                            long runTime = SystemClock.now() - entry.getValue().getTrigger().getStartRunTime();
                            if (runTime > maximumExecutionTime * 1000) {
                                log.info("任务：{}已执行{}ms，最长运行时间：{}，尝试进行停止", entry.getKey(), runTime, maximumExecutionTime * 1000);
                                if (stopRunningTask(entry.getKey())) {
                                    log.info("任务{}停止成功", entry.getKey());
                                }
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (!isStop);
        });
        stopLongTaskThread.setDaemon(true);
        stopLongTaskThread.setName("stopLongTaskThread");
        stopLongTaskThread.start();
    }
}
