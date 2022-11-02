package com.example.autojob.skeleton.framework.launcher;

import com.example.autojob.api.task.DBTaskAPI;
import com.example.autojob.api.task.MemoryTaskAPI;
import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.logging.model.AutoJobLogContainer;
import com.example.autojob.logging.model.AutoJobLogContext;
import com.example.autojob.logging.model.consumer.AutoJobLogConsumer;
import com.example.autojob.logging.model.consumer.ILogSaveStrategyDelegate;
import com.example.autojob.logging.model.memory.AutoJobLogCache;
import com.example.autojob.logging.model.memory.AutoJobRunLogCache;
import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.skeleton.annotation.AutoJobProcessorScan;
import com.example.autojob.skeleton.annotation.AutoJobScan;
import com.example.autojob.skeleton.cluster.model.AutoJobClusterManager;
import com.example.autojob.skeleton.cluster.model.AutoJobTaskTransferManager;
import com.example.autojob.skeleton.db.DataSourceHolder;
import com.example.autojob.skeleton.framework.config.AutoJobConfig;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.config.AutoJobExecutorPoolConfig;
import com.example.autojob.skeleton.framework.config.ClusterConfig;
import com.example.autojob.skeleton.framework.container.MemoryTaskContainer;
import com.example.autojob.skeleton.framework.mq.ExpirationListenerPolicy;
import com.example.autojob.skeleton.framework.mq.MessageQueueContext;
import com.example.autojob.skeleton.framework.network.AutoJobNetWorkManager;
import com.example.autojob.skeleton.framework.pool.AbstractAutoJobPool;
import com.example.autojob.skeleton.framework.pool.DefaultRefuseHandler;
import com.example.autojob.skeleton.framework.processor.*;
import com.example.autojob.skeleton.framework.task.TaskRunningContext;
import com.example.autojob.skeleton.lifecycle.TaskEventHandlerLoader;
import com.example.autojob.skeleton.lifecycle.listener.TaskListenerLoader;
import com.example.autojob.skeleton.model.alert.AlertEventHandlerLoader;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.handler.DefaultEndProcessor;
import com.example.autojob.skeleton.model.register.*;
import com.example.autojob.skeleton.model.scheduler.*;
import com.example.autojob.skeleton.model.tq.AutoJobTaskQueue;
import com.example.autojob.util.bean.ObjectUtil;
import com.example.autojob.util.mail.MailHelper;
import com.example.autojob.util.thread.ThreadPoolExecutorHelper;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 应用启动构建器，构建一个完整的AutoJob应用，该应用为全局单例
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/12 16:29
 */
@Slf4j
public class AutoJobLauncherBuilder {
    /**
     * 配置源
     */
    private final AutoJobConfigHolder configHolder;
    /**
     * 运行上下文
     */
    private final AutoJobApplication runningContext;
    /**
     * 处理器扫描器
     */
    private final AutoJobProcessorScanner processorScanner;
    /**
     * 是否自动扫描处理器
     */
    private boolean isAutoScanProcessor;
    /**
     * 是否关闭内存任务调度器
     */
    private boolean isCloseMemoryScheduler;
    /**
     * 是否关闭DB任务调度器
     */
    private boolean isCloseDBScheduler;

    /**
     * AutoJob扫描器
     */
    private final AutoJobScanner autoJobScanner;

    private ILogSaveStrategyDelegate<AutoJobLog> logSaveStrategyDelegate;

    private ILogSaveStrategyDelegate<AutoJobRunLog> runLogSaveStrategyDelegate;

    public AutoJobLauncherBuilder(Class<?> applicationEntrance) {
        this(new AutoJobConfigHolder("auto-job.yml", "auto-job.properties"), applicationEntrance);
    }


    /**
     * 创建一个构建者实例，构建者对所需要的组件进行了首次初始化
     *
     * @param configHolder        配置源
     * @param applicationEntrance 应用入口，一般是main方法所在的类
     * @author Huang Yongxiang
     * @date 2022/8/12 17:34
     */
    public AutoJobLauncherBuilder(AutoJobConfigHolder configHolder, Class<?> applicationEntrance) {
        if (configHolder == null) {
            throw new NullPointerException();
        }
        this.isAutoScanProcessor = false;
        this.configHolder = configHolder;
        AutoJobConfig config = configHolder.getAutoJobConfig();
        this.runningContext = AutoJobApplication.getInstance();
        this.runningContext.setConfigHolder(configHolder);

        /*=================调度组件配置=================>*/
        this.runningContext.setMemoryTaskContainer(MemoryTaskContainer
                .builder()
                .setCleanStrategy(config.getCleanStrategy())
                .setLimitSize(config.getMemoryContainerLength())
                .build());
        this.runningContext.setTaskQueue(new AutoJobTaskQueue(config.getSchedulingQueueLength(), true));
        this.runningContext.setExecutorPool(createDefaultExecutorPool());
        this.runningContext.setRegister(new AutoJobRegister(this.runningContext.getTaskQueue()));
        /*=======================Finished======================<*/

        /*=================注解扫描器配置=================>*/
        AutoJobProcessorScan processorScan = applicationEntrance.getAnnotation(AutoJobProcessorScan.class);
        AutoJobScan autoJobScan = applicationEntrance.getAnnotation(AutoJobScan.class);
        if (processorScan == null) {
            this.processorScanner = new AutoJobProcessorScanner();
        } else {
            this.processorScanner = new AutoJobProcessorScanner(processorScan.value());
        }
        if (autoJobScan == null) {
            this.autoJobScanner = new AutoJobScanner();
        } else {
            this.autoJobScanner = new AutoJobScanner(autoJobScan.value());
        }
        /*=======================Finished======================<*/

        /*=================邮件配置=================>*/
        if (config.getEnableMailAlert()) {
            MailHelper.MailType mailType = MailHelper.MailType.convert(config.getMailType());
            if (mailType != null && mailType != MailHelper.MailType.CUSTOMIZE) {
                this.runningContext.setMailHelper(new MailHelper(config.getSenderAddress(), config.getSenderToken(), config.getReceiverAddress(), mailType));
            } else if (mailType == MailHelper.MailType.CUSTOMIZE) {
                this.runningContext.setMailHelper(new MailHelper(config.getSenderAddress(), config.getSenderToken(), config.getReceiverAddress(), config.getSmtpAddress(), config.getSmtpPort()));
            } else {
                throw new IllegalArgumentException("未知的邮件类型：" + config.getMailType());
            }
        }
        /*=======================Finished======================<*/

        /*=================API配置=================>*/
        this.runningContext.setDbTaskAPI(new DBTaskAPI());
        this.runningContext.setMemoryTaskAPI(new MemoryTaskAPI());
        /*=======================Finished======================<*/

        this.runningContext.setSchedulers(new LinkedList<>());
        this.runningContext.setLoaders(new LinkedList<>());
        this.runningContext.setEnds(new LinkedList<>());

        ClusterConfig clusterConfig = configHolder.getClusterConfig();
        this.runningContext.setNetWorkManager(new AutoJobNetWorkManager(clusterConfig));
        if (config.getEnableCluster()) {
            this.runningContext
                    .getNetWorkManager()
                    .startRPCServer();
        }
        /*=================集群配置=================>*/
        if (config.getEnableCluster()) {
            AutoJobClusterManager clusterManager = new AutoJobClusterManager();
            addProcessor(clusterManager);
            this.runningContext.setTransferManager(new AutoJobTaskTransferManager(configHolder, clusterManager));
        }
        /*=======================Finished======================<*/
    }

    /**
     * 添加一个调度器，调度器需要依赖执行器池和注册器，请在调用该方法前请确认已配置，否则使用默认实现
     *
     * @param scheduler 调度器
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder
     * @author Huang Yongxiang
     * @date 2022/8/13 11:19
     */
    public <T extends AbstractScheduler> AutoJobLauncherBuilder addScheduler(Class<T> scheduler) {
        return addScheduler(createScheduler(scheduler));
    }

    /**
     * 额外添加一个调度器，如果你没有对应用进行拓展请忽略此方法，应用会自动将默认的调度器注册进应用
     *
     * @param scheduler 额外添加的调度器
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder
     * @author Huang Yongxiang
     * @date 2022/8/15 18:15
     */
    public AutoJobLauncherBuilder addScheduler(AbstractScheduler scheduler) {
        if (scheduler == null) {
            return this;
        }
        if (isCloseDBScheduler && scheduler instanceof AutoJobDBTaskScheduler) {
            log.warn("DB任务调度器已被关闭！");
            return this;
        }
        if (isCloseMemoryScheduler && scheduler instanceof AutoJobMemoryTaskScheduler) {
            log.warn("Memory任务调度器已被关闭！");
            return this;
        }
        if (isCloseMemoryScheduler && isCloseDBScheduler && scheduler instanceof AutoJobTimeWheelScheduler) {
            log.warn("时间轮调度器已被关闭！");
            return this;
        }
        if (scheduler instanceof IAutoJobProcessor) {
            this.addProcessor((IAutoJobProcessor) scheduler);
        }
        this.runningContext
                .getSchedulers()
                .add(scheduler);
        return this;
    }

    /**
     * 额外添加一个处理器，果你没有对应用进行拓展请忽略此方法，应用会自动将默认的处理器注册进应用
     *
     * @param processor 要添加的默认处理器
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder
     * @author Huang Yongxiang
     * @date 2022/8/16 9:10
     */
    public AutoJobLauncherBuilder addProcessor(IAutoJobProcessor processor) {
        if (processor == null) {
            return this;
        }
        if (AutoJobProcessorContext
                .getInstance()
                .containsProcessor(processor.getClass())) {
            return this;
        }
        if (processor instanceof IAutoJobLoader) {
            this.runningContext
                    .getLoaders()
                    .add((IAutoJobLoader) processor);
        }
        if (processor instanceof IAutoJobEnd) {
            this.runningContext
                    .getEnds()
                    .add((IAutoJobEnd) processor);
        }
        AutoJobProcessorContext
                .getInstance()
                .addProcessor(processor);
        return this;
    }

    /**
     * 使用动态处理器扫描，扫描到的处理器会调用其无参构造方法创建处理器，该方法仅会扫描非默认的处理器
     *
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder
     * @author Huang Yongxiang
     * @date 2022/8/16 9:12
     */
    public AutoJobLauncherBuilder withAutoScanProcessor() {
        isAutoScanProcessor = true;
        return this;
    }

    /**
     * 设置一个连接池，默认使用Druid连接池
     *
     * @param dataSource 连接池
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder
     * @author Huang Yongxiang
     * @date 2022/8/25 15:38
     */
    public AutoJobLauncherBuilder setDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException();
        }
        this.runningContext.setDataSourceHolder(new DataSourceHolder(dataSource));
        return this;
    }

    /**
     * 设置注册器
     *
     * @param register 注册器
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder
     * @author Huang Yongxiang
     * @date 2022/8/16 9:13
     */
    public AutoJobLauncherBuilder setRegister(IAutoJobRegister register) {
        if (register == null) {
            throw new NullPointerException();
        }
        this.runningContext.setRegister(register);
        return this;
    }

    /**
     * 设置注册器
     *
     * @param register 注册器
     * @param handler  注册处理器
     * @param filter   注册过滤器
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder
     * @author Huang Yongxiang
     * @date 2022/8/16 9:13
     */
    public AutoJobLauncherBuilder setRegister(IAutoJobRegister register, AbstractRegisterHandler handler, AbstractRegisterFilter filter) {
        if (register == null) {
            throw new NullPointerException();
        }
        register.setFilter(filter);
        register.setHandler(handler);
        this.runningContext.setRegister(register);
        return this;
    }

    public AutoJobLauncherBuilder setExecutorPool(AutoJobTaskExecutorPool executorPool) {
        if (executorPool == null) {
            throw new NullPointerException();
        }
        this.runningContext.setExecutorPool(executorPool);
        return this;
    }

    public AutoJobLauncherBuilder setTaskQueue(AutoJobTaskQueue taskQueue) {
        if (taskQueue == null) {
            throw new NullPointerException();
        }
        this.runningContext.setTaskQueue(taskQueue);
        return this;
    }

    /**
     * 关闭内存任务调度器，关闭后将不会停止对内存任务进行调度，如果同时关闭DB调度器，在没有添加自定义调度器的情况下框架会被禁用任务调度功能，即配置的所有任务将不会被执行
     *
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder
     * @author Huang Yongxiang
     * @date 2022/11/2 16:32
     */
    public AutoJobLauncherBuilder closeMemoryTaskScheduler() {
        isCloseMemoryScheduler = true;
        return this;
    }

    /**
     * 关闭DB任务调度器，关闭后将不会对DB任务进行调度，如果同时关闭Memory调度器，在没有添加自定义调度器的情况下框架会被禁用任务调度功能，即配置的所有任务将不会被执行
     *
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder
     * @author Huang Yongxiang
     * @date 2022/11/2 16:33
     */
    public AutoJobLauncherBuilder closeDBTaskScheduler() {
        isCloseDBScheduler = true;
        return this;
    }

    protected void createLogContext() {

        /*=================添加日志消息队列=================>*/
        AutoJobConfig config = configHolder.getAutoJobConfig();
        AutoJobLogContainer
                .getInstance()
                .addMessageQueueContext(AutoJobLog.class, MessageQueueContext
                        .builder()
                        .setListenerPolicy(ExpirationListenerPolicy.SINGLE_THREAD)
                        .setAllowSetEntryExpired(true)
                        .setDefaultExpiringTime(24, TimeUnit.HOURS)
                        .setAllowMaxTopicCount(config
                                .getExecutorPoolConfig()
                                .getFastPoolMaxThreadCount() + config
                                .getExecutorPoolConfig()
                                .getSlowPoolMaxCoreThreadCount())
                        .setAllowMaxMessageCountPerQueue(10000)
                        .build());
        /*=======================Finished======================<*/

        /*=================创建日志上下文=================>*/
        AutoJobLogContext
                .getInstance()
                .setLogContainer(AutoJobLogContainer.getInstance())
                .setLogManager(new AutoJobLogConsumer(logSaveStrategyDelegate, runLogSaveStrategyDelegate))
                .setLogHelper(new AutoJobLogHelper())
                .setLogCache(new AutoJobLogCache(configHolder.getLogConfig()))
                .setRunLogCache(new AutoJobRunLogCache(configHolder.getLogConfig()));
        runningContext.setLogContext(AutoJobLogContext.getInstance());
        /*=======================Finished======================<*/
    }

    protected void createDefaultScheduler() {
        if (configHolder
                .getAutoJobConfig()
                .getEnableAnnotation()) {
            AutoJobAnnotationScheduler annotationScheduler = new AutoJobAnnotationScheduler(this.runningContext.getExecutorPool(), this.runningContext.getRegister(), configHolder);
            annotationScheduler.scan(autoJobScanner);
            addScheduler(annotationScheduler);
        }
        this
                .addScheduler(createScheduler(AutoJobRunSuccessScheduler.class))
                .addScheduler(createScheduler(AutoJobRunErrorScheduler.class))
                .addScheduler(createScheduler(AutoJobDBTaskScheduler.class))
                .addScheduler(createScheduler(AutoJobTimeWheelScheduler.class))
                .addScheduler(createScheduler(AutoJobMemoryTaskScheduler.class))
                .addScheduler(createScheduler(TaskRunningContext.class));
    }

    protected void createDefaultProcessor() {
        this
                .addProcessor(new AlertEventHandlerLoader())
                .addProcessor(new TaskEventHandlerLoader())
                .addProcessor(new TaskListenerLoader())
                .addProcessor(new AutoJobRegisterLoader())
                .addProcessor(new DefaultEndProcessor());
    }

    protected void createDefaultDataSource() {
        if (this.runningContext.getDataSourceHolder() == null) {
            this.runningContext.setDataSourceHolder(new DataSourceHolder());
        }
    }

    public AutoJobLauncherBuilder setLogSaveStrategyDelegate(ILogSaveStrategyDelegate<AutoJobLog> logSaveStrategyDelegate) {
        this.logSaveStrategyDelegate = logSaveStrategyDelegate;
        return this;
    }

    public AutoJobLauncherBuilder setRunLogSaveStrategyDelegate(ILogSaveStrategyDelegate<AutoJobRunLog> runLogSaveStrategyDelegate) {
        this.runLogSaveStrategyDelegate = runLogSaveStrategyDelegate;
        return this;
    }

    /**
     * 创建一个默认的执行器池实现，具体参数来源于配置源
     *
     * @return com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool
     * @author Huang Yongxiang
     * @date 2022/8/25 15:41
     */
    protected AutoJobTaskExecutorPool createDefaultExecutorPool() {
        AutoJobExecutorPoolConfig config = configHolder
                .getAutoJobConfig()
                .getExecutorPoolConfig();
        /*=================fast pool自动装配=================>*/
        ThreadPoolExecutorHelper fastPool = ThreadPoolExecutorHelper
                .classicBuilder()
                .setAllowUpdate(config.getEnableFastPoolUpdate())
                .setAllowMaxCoreThreadCount(config.getFastPoolMaxCoreThreadCount())
                .setAllowMinCoreThreadCount(config.getFastPoolMinCoreThreadCount())
                .setCoreThreadCount(config.getFastPoolInitialCoreThreadCount())
                .setMaxThreadCount(config.getFastPoolInitialThreadCount())
                .setAllowMaxThreadCount(config.getFastPoolMaxThreadCount())
                .setAllowMinThreadCount(config.getFastPoolMinThreadCount())
                .setTrafficListenerCycle((config.getFastPoolTrafficUpdateCycle()).longValue())
                .setThreadFactory(new AbstractAutoJobPool.NamedThreadFactory("TaskExecutorPool-fastPool"))
                .setQueueLength(1)
                .setUpdateType(ThreadPoolExecutorHelper.UpdateType.USE_FLOW)
                .setUpdateThreshold(config.getFastPoolAdjustedThreshold())
                .build();
        /*=======================Finished======================<*/

        /*=================end pool自动装配=================>*/
        ThreadPoolExecutorHelper slowPool = ThreadPoolExecutorHelper
                .classicBuilder()
                .setAllowUpdate(config.getEnableSlowPoolUpdate())
                .setAllowMaxCoreThreadCount(config.getSlowPoolMaxCoreThreadCount())
                .setAllowMinCoreThreadCount(config.getSlowPoolMinCoreThreadCount())
                .setThreadFactory(new AbstractAutoJobPool.NamedThreadFactory("TaskExecutorPool-slowPool-"))
                .setCoreThreadCount(config.getSlowPoolInitialCoreThreadCount())
                .setMaxThreadCount(config.getSlowPoolInitialThreadCount())
                .setAllowMaxThreadCount(config.getSlowPoolMaxThreadCount())
                .setAllowMinThreadCount(config.getSlowPoolMinThreadCount())
                .setTrafficListenerCycle((config.getSlowPoolTrafficUpdateCycle()).longValue())
                .setQueueLength(1)
                .setUpdateType(ThreadPoolExecutorHelper.UpdateType.USE_FLOW)
                .setUpdateThreshold(config.getSlowPoolAdjustedThreshold())
                .build();
        /*=======================Finished======================<*/
        return new AutoJobTaskExecutorPool(config.getFastPoolMaxThreadCount() + config.getSlowPoolMaxCoreThreadCount(), config.getQueueLength(), new DefaultRefuseHandler(), fastPool, slowPool);
    }

    /**
     * 如果设置了处理器自动扫描，该方法会自动扫描处理器相关的类并且调用无参构造方法创建对象
     *
     * @return void
     * @author Huang Yongxiang
     * @date 2022/8/15 18:11
     */
    protected void scanProcessor() {
        if (isAutoScanProcessor) {
            Set<Class<? extends IAutoJobProcessor>> processors = processorScanner.scanClass();
            for (Class<? extends IAutoJobProcessor> clazz : processors) {
                if (clazz == AutoJobClusterManager.class) {
                    continue;
                }
                IAutoJobProcessor processor = ObjectUtil.getClassInstance(clazz);
                if (processor != null) {
                    this.addProcessor(processor);
                }
            }
            log.debug("自动扫描到：{}个处理器", processors.size());
        }
    }

    /**
     * 创建一个应用实例，注意该实例是全局单例
     *
     * @return com.example.autojob.skeleton.framework.launcher.AutoJobApplication
     * @author Huang Yongxiang
     * @date 2022/8/17 15:55
     */
    public AutoJobApplication build() {
        this.createDefaultScheduler();
        this.createDefaultProcessor();
        this.scanProcessor();
        this.createDefaultDataSource();
        this.createLogContext();
        this.runningContext.setStatus(AutoJobApplication.CREATED);
        return runningContext;
    }

    protected <T extends AbstractScheduler> T createScheduler(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor(AutoJobTaskExecutorPool.class, IAutoJobRegister.class, AutoJobConfigHolder.class);
            return constructor.newInstance(this.runningContext.getExecutorPool(), this.runningContext.getRegister(), configHolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
