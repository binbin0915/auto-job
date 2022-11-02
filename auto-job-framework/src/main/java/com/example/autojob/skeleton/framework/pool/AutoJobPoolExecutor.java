package com.example.autojob.skeleton.framework.pool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * 执行器，执行器是对可执行对象的封装
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/02 13:50
 */
@Slf4j
public class AutoJobPoolExecutor implements Callable<Object>, RunnablePostProcessor {
    private String executorName;
    private volatile Executable executable;
    private Object[] params;
    private Throwable throwable;
    private Object result;
    private RunnablePostProcessor runnablePostProcessor;
    private volatile int status = WAIT;
    private static final int WAIT = 0;
    private static final int READY = 1;
    private static final int RUNNING = 2;
    private static final int SUCCESS_OVER = 3;
    private static final int ERROR_OVER = 4;

    public AutoJobPoolExecutor(Executable executable) {
        this.executable = executable;
    }

    public AutoJobPoolExecutor(Executable executable, RunnablePostProcessor runnablePostProcessor) {
        this(executable, null, runnablePostProcessor);
    }

    public AutoJobPoolExecutor(Executable executable, Object[] params) {
        this(executable, params, null);
    }

    public AutoJobPoolExecutor(Executable executable, Object[] params, RunnablePostProcessor runnablePostProcessor) {
        this.executable = executable;
        this.params = params;
        this.runnablePostProcessor = runnablePostProcessor;
    }

    public AutoJobPoolExecutor() {
    }

    public void setRunnablePostProcessor(RunnablePostProcessor runnablePostProcessor) {
        this.runnablePostProcessor = runnablePostProcessor;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutable(Executable executable) {
        if (status == 2) {
            throw new IllegalStateException("该执行器正在运行");
        }
        this.executable = executable;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public synchronized boolean connect(Executable executable, Object... params) {
        if (executable == null) {
            log.error("无法建立与执行器{}的连接，可执行对象为null", executorName);
            return false;
        }
        if (isFreeExecutor() && !isRunning()) {
            this.executable = executable;
            this.params = params;
            if (!isFreeExecutor()) {
                status = READY;
                return true;
            }
        }
        return false;
    }

    @Override
    public Object call() {
        if (executable != null) {
            try {
                beforeRun(executable, this, params);
                result = executable.execute(params);
                afterRun(executable, this, result);
            } catch (Exception e) {
                e.printStackTrace();
                throwable = e;
                runError(executable, this, throwable, result);
            } finally {
                //log.warn("执行器{}已被重置", executorName);
                reset();
            }
        } else {
            log.error("执行器{}无法执行，因为要执行的可执行对象Executable为null", executorName);
        }
        return result;
    }

    @Override
    public void beforeRun(final Executable executable, AutoJobPoolExecutor executor, Object... params) {
        status = RUNNING;
        if (runnablePostProcessor != null) {
            runnablePostProcessor.beforeRun(executable, this, params);
        }

    }

    @Override
    public void afterRun(final Executable executable, AutoJobPoolExecutor executor, Object result) {
        status = SUCCESS_OVER;
        if (runnablePostProcessor != null) {
            runnablePostProcessor.afterRun(executable, this, result);
        }
    }

    @Override
    public void runError(final Executable executable, AutoJobPoolExecutor executor, Throwable throwable, Object result) {
        status = ERROR_OVER;
        if (runnablePostProcessor != null) {
            runnablePostProcessor.runError(executable, this, this.throwable, result);
        }
    }

    public boolean isFreeExecutor() {
        return executable == null && status == WAIT;
    }

    public boolean isRunning() {
        return status == RUNNING;
    }

    public boolean isFinished() {
        return status > 2;
    }

    public boolean isReady() {
        return status == READY && executable != null;
    }

    public boolean isSuccess() {
        return status == SUCCESS_OVER;
    }

    public void reset() {
        if (status > 2) {
            status = WAIT;
            executable = null;
            params = null;
            throwable = null;
            result = null;
            runnablePostProcessor = null;
        }
    }
}
