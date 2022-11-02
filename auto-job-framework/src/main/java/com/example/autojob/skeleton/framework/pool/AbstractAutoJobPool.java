package com.example.autojob.skeleton.framework.pool;

import com.example.autojob.skeleton.lang.WithDaemonThread;
import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import com.example.autojob.util.thread.ThreadPoolExecutorHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AutoJob执行池，任何实现了Executable接口的类都可交由该执行器池执行，该池内置一个可根据流量动态调节的线程池作为执行的线程资源
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/02 13:10
 */
@Slf4j
public abstract class AbstractAutoJobPool implements WithDaemonThread {
    protected String poolName;
    protected AtomicInteger totalExecutorCount;
    protected AtomicInteger inUseExecutorCount;
    protected AtomicInteger freeExecutorCount;
    protected int maxQueueLength;
    protected List<AutoJobPoolExecutor> executorList;
    protected volatile AutoJobPoolExecutor freeExecutor;
    protected Queue<Entry> waitQueue;
    /**
     * 慢线程池
     */
    private final ThreadPoolExecutorHelper slowThreadPool;
    /**
     * 快线程池
     */
    private final ThreadPoolExecutorHelper fastThreadPool;
    protected IRefuseHandler refusedHandler;
    private final ScheduleTaskUtil autoJobPoolDaemonThread;


    public AbstractAutoJobPool(String poolName, int totalExecutorCount, int maxQueueLength, IRefuseHandler refusedHandler, double changeThreshold, int initialThreadCount, int keepAliveTime, long trafficCycle, TimeUnit unit) {
        if (maxQueueLength <= 0 || trafficCycle <= 0) {
            throw new IllegalArgumentException("数值必须为非负数");
        }
        if (changeThreshold < 0 || changeThreshold > 1) {
            throw new IllegalArgumentException("更新阈值必须在0-1以内");
        }
        this.totalExecutorCount = new AtomicInteger(totalExecutorCount);
        this.maxQueueLength = maxQueueLength + totalExecutorCount;
        this.executorList = new ArrayList<>();
        this.refusedHandler = refusedHandler;
        this.inUseExecutorCount = new AtomicInteger(0);
        this.freeExecutorCount = new AtomicInteger(totalExecutorCount);
        this.poolName = StringUtils.isEmpty(poolName) ? "AbstractAutoJobPool" : poolName;
        this.waitQueue = new LinkedBlockingQueue<>(maxQueueLength);
        for (int i = 0; i < totalExecutorCount; i++) {
            AutoJobPoolExecutor executor = new AutoJobPoolExecutor();
            executor.setExecutorName(createExecutorName(poolName, i));
            executorList.add(executor);
        }
        this.freeExecutor = executorList.get(0);
        this.fastThreadPool = ThreadPoolExecutorHelper
                .classicBuilder()
                .setAllowMaxCoreThreadCount(totalExecutorCount)
                .setAllowMaxThreadCount(totalExecutorCount)
                .setCoreThreadCount(initialThreadCount)
                .setMaxThreadCount(initialThreadCount * 2)
                .setAllowMinCoreThreadCount(initialThreadCount)
                .setAllowMinThreadCount(initialThreadCount * 2)
                .setAllowUpdate(true)
                .setKeepAliveTime(keepAliveTime)
                .setThreadFactory(new NamedThreadFactory(this.poolName + "-fastPool"))
                .setUpdateThreshold(changeThreshold)
                .setQueueLength(maxQueueLength + totalExecutorCount)
                .setTrafficListenerCycle(trafficCycle, unit)
                .setUpdateType(ThreadPoolExecutorHelper.UpdateType.USE_FLOW)
                .build();
        this.slowThreadPool = ThreadPoolExecutorHelper
                .classicBuilder()
                .setAllowUpdate(false)
                .setCoreThreadCount(initialThreadCount)
                .setMaxThreadCount(totalExecutorCount)
                .setKeepAliveTime(TimeUnit.SECONDS.toMillis(Long.MAX_VALUE))
                .setQueueLength(maxQueueLength + totalExecutorCount)
                .setThreadFactory(new NamedThreadFactory(this.poolName + "-slowPool"))
                .build();
        this.autoJobPoolDaemonThread = ScheduleTaskUtil.build(true, "autoJobPoolDaemonThread");
        startWork();
    }

    public AbstractAutoJobPool(String poolName, int totalExecutorCount, int maxQueueLength, IRefuseHandler refusedHandler, ThreadPoolExecutorHelper fastThreadPool, ThreadPoolExecutorHelper slowThreadPool) {
        if (fastThreadPool == null || slowThreadPool == null) {
            throw new NullPointerException();
        }
        if (maxQueueLength <= 0) {
            throw new IllegalArgumentException("数值必须为非负数");
        }
        this.totalExecutorCount = new AtomicInteger(totalExecutorCount);
        this.maxQueueLength = maxQueueLength + totalExecutorCount;
        this.executorList = new ArrayList<>();
        this.refusedHandler = refusedHandler;
        this.inUseExecutorCount = new AtomicInteger(0);
        this.freeExecutorCount = new AtomicInteger(totalExecutorCount);
        this.poolName = StringUtils.isEmpty(poolName) ? "AbstractAutoJobPool" : poolName;
        this.waitQueue = new LinkedBlockingQueue<>(maxQueueLength);
        for (int i = 0; i < totalExecutorCount; i++) {
            AutoJobPoolExecutor executor = new AutoJobPoolExecutor();
            executor.setExecutorName(createExecutorName(poolName, i));
            executorList.add(executor);
        }
        this.slowThreadPool = slowThreadPool;
        this.fastThreadPool = fastThreadPool;
        this.autoJobPoolDaemonThread = ScheduleTaskUtil.build(true, "autoJobPoolDaemonThread");
        startWork();
    }


    public AbstractAutoJobPool setPoolName(String poolName) {
        this.poolName = poolName;
        return this;
    }

    public void submit2FastPool(Executable executable, RunnablePostProcessor postProcessor) {
        if (executable == null) {
            throw new NullPointerException();
        }
        AutoJobPoolExecutor executor = getFreeExecutor(executable);
        if (executor != null && executor.isReady()) {
            executor.setRunnablePostProcessor(postProcessor);
            fastThreadPool.submit(executor, true);
        } else {
            if (waitQueue.size() < maxQueueLength) {
                waitQueue.offer(new Entry(executable, postProcessor, true));
            } else {
                if (refusedHandler != null) {
                    refusedHandler.doHandle(executable, postProcessor, this);
                } else {
                    log.error("提交失败，执行器池已达到最大负载");
                }
            }
        }
    }

    public void submit2SlowPool(Executable executable, RunnablePostProcessor postProcessor) {
        if (executable == null) {
            throw new NullPointerException();
        }
        AutoJobPoolExecutor executor = getFreeExecutor(executable);
        if (executor != null && executor.isReady()) {
            executor.setRunnablePostProcessor(postProcessor);
            slowThreadPool.submit(executor, true);
        } else {
            if (waitQueue.size() < maxQueueLength) {
                waitQueue.offer(new Entry(executable, postProcessor, false));
            } else {
                if (refusedHandler != null) {
                    refusedHandler.doHandle(executable, postProcessor, this);
                } else {
                    log.error("提交失败，执行器池已达到最大负载");
                }
            }
        }
    }

    /**
     * 尝试获取一个空执行器并且建立执行器和执行任务的连接
     *
     * @param executable 待执行的任务
     * @return com.example.autojob.skeleton.framework.pool.AutoJobPoolExecutor
     * @author Huang Yongxiang
     * @date 2022/8/2 15:01
     */
    protected AutoJobPoolExecutor getFreeExecutor(Executable executable) {
        if (freeExecutor == null || !freeExecutor.isFreeExecutor() || freeExecutor.isRunning()) {
            return null;
        }
        synchronized (AutoJobPoolExecutor.class) {
            if (freeExecutor == null || !freeExecutor.isFreeExecutor() || freeExecutor.isRunning()) {
                return null;
            }
            if (connect2Executor(executable, freeExecutor)) {
                if (freeExecutor.isReady()) {
                    updateFreeStatus(false);
                    return freeExecutor;
                } else {
                    return null;
                }

            } else {
                return null;
            }
        }
    }

    /**
     * 更新执行器池的状态
     *
     * @param isAdd 是否为空闲更新
     * @return void
     * @author Huang Yongxiang
     * @date 2022/8/2 15:03
     */
    private void updateFreeStatus(boolean isAdd) {
        if (isAdd) {
            if (freeExecutorCount.get() == totalExecutorCount.get() || inUseExecutorCount.get() == 0) {
                return;
            }
            freeExecutorCount.incrementAndGet();
            inUseExecutorCount.addAndGet(-1);
        } else {
            if (freeExecutorCount.get() == 0 || inUseExecutorCount.get() == totalExecutorCount.get()) {
                return;
            }
            freeExecutorCount.addAndGet(-1);
            inUseExecutorCount.incrementAndGet();
        }
    }

    @Override
    public void startWork() {
        Runnable runnable = () -> {
            try {
                executeWaitExecutable();
                for (AutoJobPoolExecutor executor : executorList) {
                    if (!executor.isRunning() && executor.isFreeExecutor()) {
                        updateFreeStatus(true);
                        if (freeExecutor == null || !freeExecutor.isFreeExecutor() || freeExecutor.isRunning()) {
                            freeExecutor = executor;
                            executeWaitExecutable();
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        autoJobPoolDaemonThread.EFixedRateTask(runnable, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        fastThreadPool.shutdown();
        slowThreadPool.shutdown();
        autoJobPoolDaemonThread.shutdown();
    }

    public void shutdownNow() {
        fastThreadPool.shutdownNow();
        slowThreadPool.shutdownNow();
        autoJobPoolDaemonThread.shutdown();
    }


    protected boolean connect2Executor(Executable executable, AutoJobPoolExecutor executor) {
        return executor.connect(executable, executable.getExecuteParams());
    }

    protected abstract String createExecutorName(String poolName, int executorId);

    /**
     * 尝试提交一个等待队列的任务，如果有的话
     *
     * @return void
     * @author Huang Yongxiang
     * @date 2022/8/2 14:56
     */
    private void executeWaitExecutable() {
        if (waitQueue.size() > 0) {
            Entry entry = waitQueue.peek();
            if (entry != null && entry.executable != null) {
                Executable executable = entry.executable;
                AutoJobPoolExecutor executor = getFreeExecutor(executable);
                if (executor != null && executor.isReady()) {
                    executor.setRunnablePostProcessor(entry.runnablePostProcessor);
                    if (entry.isFastTask) {
                        fastThreadPool.submit(executor, true);
                    } else {
                        slowThreadPool.submit(executor, true);
                    }
                    waitQueue.poll();
                }
            }
        }
    }

    @Getter
    protected static class Entry {
        Executable executable;
        RunnablePostProcessor runnablePostProcessor;
        boolean isFastTask;

        public Entry(Executable executable, RunnablePostProcessor runnablePostProcessor, boolean isFastTask) {
            if (executable == null) {
                log.error("创建Entry的可执行对象为空");
            }
            this.executable = executable;
            this.runnablePostProcessor = runnablePostProcessor;
            this.isFastTask = isFastTask;
        }
    }

    public static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup threadGroup;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public final String namePrefix;

        public NamedThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            threadGroup = (s != null) ? s.getThreadGroup() : Thread
                    .currentThread()
                    .getThreadGroup();
            if (null == name || "".equals(name.trim())) {
                name = "pool";
            }
            namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(threadGroup, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }


}
