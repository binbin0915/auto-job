package com.example.autojob.skeleton.framework.boot;

import com.example.autojob.api.task.AutoJobAPI;
import com.example.autojob.api.task.DBTaskAPI;
import com.example.autojob.api.task.MemoryTaskAPI;
import com.example.autojob.logging.model.AutoJobLogContext;
import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.skeleton.annotation.ProcessorLevel;
import com.example.autojob.skeleton.cluster.model.AutoJobTaskTransferManager;
import com.example.autojob.skeleton.db.DataSourceHolder;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.container.MemoryTaskContainer;
import com.example.autojob.skeleton.framework.network.AutoJobNetWorkManager;
import com.example.autojob.skeleton.framework.processor.IAutoJobEnd;
import com.example.autojob.skeleton.framework.processor.IAutoJobLoader;
import com.example.autojob.skeleton.framework.processor.IAutoJobProcessor;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.TaskRunningContext;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.skeleton.model.scheduler.AbstractScheduler;
import com.example.autojob.skeleton.model.tq.AutoJobTaskQueue;
import com.example.autojob.util.id.SystemClock;
import com.example.autojob.util.mail.MailHelper;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import com.example.autojob.util.thread.SyncHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AutoJob程序，包含AutoJob运行的上下文，该程序为全局单例，通过{@link AutoJobBootstrap}构建该应用，构建后你可以通过{@link AutoJobApplication#getInstance()}来获取单例，如果你要使用该应用内的某个组件，请务必保证应用状态{@link AutoJobApplication#status}为RUNNING时再调用
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/13 10:41
 */
@Setter(AccessLevel.PACKAGE)
@Getter
@Slf4j
public class AutoJobApplication implements Closeable {
    private Class<?> application;
    /**
     * 内存任务容器
     */
    private MemoryTaskContainer memoryTaskContainer;
    /**
     * 任务调度队列
     */
    private AutoJobTaskQueue taskQueue;
    /**
     * 任务执行器池
     */
    private AutoJobTaskExecutorPool executorPool;
    /**
     * 任务注册器
     */
    private IAutoJobRegister register;
    /**
     * 加载器
     */
    private List<IAutoJobLoader> loaders;
    /**
     * 关闭处理器
     */
    private List<IAutoJobEnd> ends;
    /**
     * 所有的调度器
     */
    private List<AbstractScheduler> schedulers;
    /**
     * DB任务API服务类
     */
    private DBTaskAPI dbTaskAPI;
    /**
     * 内存任务API服务类
     */
    private MemoryTaskAPI memoryTaskAPI;
    /**
     * 任务转移管理器
     */
    private AutoJobTaskTransferManager transferManager;
    /**
     * 任务运行上下文
     */
    private TaskRunningContext taskRunningContext;
    /**
     * 日志上下文
     */
    private AutoJobLogContext logContext;
    /**
     * 配置源
     */
    private AutoJobConfigHolder configHolder;
    /**
     * 通信管理器
     */
    private AutoJobNetWorkManager netWorkManager;
    /**
     * 邮件处理器
     */
    private MailHelper mailHelper;
    /**
     * 连接池
     */
    private DataSourceHolder dataSourceHolder;
    public static final int NO_CREATE = 0;
    public static final int CREATED = 1;
    public static final int RUNNING = 2;
    public static final int CLOSE = 3;
    private int status = NO_CREATE;


    public void run() {
        if (status == NO_CREATE) {
            throw new IllegalStateException("应用还未初始化，请先初始化应用");
        }
        printLogo();
        loaders = loaders
                .stream()
                .sorted(new ProcessorComparator())
                .collect(Collectors.toList());
        ends = ends
                .stream()
                .sorted(new ProcessorComparator())
                .collect(Collectors.toList());
        schedulers = schedulers
                .stream()
                .sorted(new SchedulerComparator())
                .collect(Collectors.toList());
        ScheduleTaskUtil
                .build(false, "AutoJobRunThread")
                .EOneTimeTask(() -> {
                    try {
                        if (status == RUNNING) {
                            throw new IllegalStateException("应用已在运行");
                        }
                        log.info("==================================>AutoJob starting");
                        long start = System.currentTimeMillis();
                        //执行所有启动器
                        for (IAutoJobLoader loader : loaders) {
                            loader.load();
                            log.debug("加载器：{}加载完成", loader
                                    .getClass()
                                    .getName());
                        }
                        //启动所有调度器
                        for (AbstractScheduler scheduler : schedulers) {
                            scheduler.beforeExecute();
                            try {
                                scheduler.execute();
                            } catch (Exception e) {
                                scheduler.executeError(e);
                                throw e;
                            } finally {
                                scheduler.afterExecute();
                            }
                            log.debug("调度器：{}加载完成", scheduler
                                    .getClass()
                                    .getName());
                        }
                        log.info("AutoJob成功执行：{}个加载器，{}个调度器，共计用时：{}ms", loaders.size(), schedulers.size(), System.currentTimeMillis() - start);
                        status = RUNNING;
                        log.info("==================================>AutoJob started in {} ms", System.currentTimeMillis() - start);
                    } catch (Exception e) {
                        log.error("AutoJob start failed:{}", e.getMessage());
                        e.printStackTrace();
                    }
                    return 0;
                }, 0, TimeUnit.MILLISECONDS);
        SyncHelper.aWaitQuietly(() -> status == RUNNING);
    }

    private AutoJobApplication() {
        Thread overThread = new Thread(() -> {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        overThread.setDaemon(false);
        overThread.setName("AutoJobCloseThread");
        Runtime
                .getRuntime()
                .addShutdownHook(overThread);
    }

    public boolean isRunning() {
        return status == RUNNING;
    }

    public boolean isClosed() {
        return status == CLOSE;
    }

    public boolean isCreated() {
        return status == CREATED;
    }

    public static AutoJobApplication getInstance() {
        return InstanceHolder.CONTEXT;
    }

    @Override
    public void close() throws IOException {
        if (status != RUNNING) {
            throw new IllegalStateException("应用还未启动或已关闭");
        }
        try {
            AutoJobLogHelper logger = AutoJobLogHelper.getInstance();
            logger.info("==================================>AutoJob ending");
            long start = System.currentTimeMillis();
            //执行所有的关闭处理器
            for (IAutoJobEnd end : ends) {
                end.end();
                log.debug("关闭处理器：{}执行完成", end
                        .getClass()
                        .getName());
            }
            //摧毁所有的调度器
            for (AbstractScheduler scheduler : schedulers) {
                scheduler.beforeDestroy();
                try {
                    scheduler.destroy();
                } catch (Exception e) {
                    scheduler.destroyError(e);
                    throw e;
                } finally {
                    scheduler.afterDestroy();
                }

                log.debug("调度器：{}已摧毁", scheduler
                        .getClass()
                        .getName());
            }
            logger.info("AutoJob成功执行：{}个关闭处理器，摧毁：{}个调度器，共计用时：{}ms", ends.size(), schedulers.size(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } finally {
            status = CLOSE;
        }
    }

    public AutoJobAPI getMatchedAPI(long taskId) {
        AutoJobTask.TaskType taskType = memoryTaskAPI.getTaskType(taskId);
        if (taskType == null) {
            return null;
        }
        return taskType == AutoJobTask.TaskType.MEMORY_TASk ? memoryTaskAPI : dbTaskAPI;
    }

    private static class InstanceHolder {
        private static final AutoJobApplication CONTEXT = new AutoJobApplication();
    }

    /**
     * 处理器优先级比较器，子类应该按照使用该比较器对所有加载器有序加载
     */
    protected static class ProcessorComparator implements Comparator<IAutoJobProcessor> {
        @Override
        public int compare(IAutoJobProcessor o1, IAutoJobProcessor o2) {
            ProcessorLevel o1ProcessorLevel = o1
                    .getClass()
                    .getAnnotation(ProcessorLevel.class);
            ProcessorLevel o2ProcessorLevel = o2
                    .getClass()
                    .getAnnotation(ProcessorLevel.class);
            if (o1ProcessorLevel == null && o2ProcessorLevel != null) {
                return Integer.compare(o2ProcessorLevel.value(), 0);
            } else if (o1ProcessorLevel != null && o2ProcessorLevel == null) {
                return Integer.compare(0, o1ProcessorLevel.value());
            } else if (o1ProcessorLevel == null) {
                return 0;
            } else {
                return Integer.compare(o2ProcessorLevel.value(), o1ProcessorLevel.value());
            }
        }
    }

    public static class SchedulerComparator implements Comparator<AbstractScheduler> {

        @Override
        public int compare(AbstractScheduler o1, AbstractScheduler o2) {
            return Integer.compare(o2.getSchedulerLevel(), o1.getSchedulerLevel());
        }
    }

    private void printLogo() {
        System.out.println("  _____          __              ____.     ___.     ");
        System.out.println("  /  _  \\  __ ___/  |_  ____     |    | ____\\_ |__  ");
        System.out.println(" /  /_\\  \\|  |  \\   __\\/  _ \\    |    |/  _ \\| __ \\ ");
        System.out.println("/    |    \\  |  /|  | (  <_> )\\__|    (  <_> ) \\_\\ \\");
        System.out.println("\\____|__  /____/ |__|  \\____/\\________|\\____/|___  /");
        System.out.println("        \\/                                       \\/ ");
    }

}
