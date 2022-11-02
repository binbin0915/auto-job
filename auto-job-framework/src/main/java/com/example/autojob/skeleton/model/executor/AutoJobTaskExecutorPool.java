package com.example.autojob.skeleton.model.executor;

import com.example.autojob.skeleton.framework.pool.AbstractAutoJobPool;
import com.example.autojob.skeleton.framework.pool.IRefuseHandler;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.task.TaskExecutable;
import com.example.autojob.util.thread.ThreadPoolExecutorHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 任务执行器池
 * @Author Huang Yongxiang
 * @Date 2022/08/02 15:36
 */
public class AutoJobTaskExecutorPool extends AbstractAutoJobPool {
    private static final String POOL_NAME = "TaskExecutorPool";

    public AutoJobTaskExecutorPool( int totalExecutorCount, int maxQueueLength, IRefuseHandler refusedHandler, ThreadPoolExecutorHelper fastThreadPool, ThreadPoolExecutorHelper slowThreadPool) {
        super(POOL_NAME, totalExecutorCount, maxQueueLength, refusedHandler, fastThreadPool, slowThreadPool);
    }


    public List<AutoJobTask> getWaitQueue() {
        return waitQueue.stream().map(item -> {
            if (item instanceof TaskExecutable) {
                TaskExecutable taskExecutable = (TaskExecutable) item;
                return taskExecutable.getAutoJobTask();
            }
            return null;
        }).collect(Collectors.toList());
    }

    @Override
    protected String createExecutorName(String poolName, int executorId) {
        return String.format("%s-%d", poolName, executorId);
    }
    

}
