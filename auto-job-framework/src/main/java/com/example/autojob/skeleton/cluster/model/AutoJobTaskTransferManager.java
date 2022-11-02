package com.example.autojob.skeleton.cluster.model;

import com.example.autojob.skeleton.framework.config.ClusterConfig;
import com.example.autojob.skeleton.lang.WithDaemonThread;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.ITaskEventHandler;
import com.example.autojob.skeleton.lifecycle.TaskEventFactory;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskErrorEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskFinishedEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskReceivedEvent;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskTransferEvent;
import com.example.autojob.skeleton.lifecycle.manager.TaskEventManager;
import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 任务故障转移和负载均衡管理器
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/01 16:38
 */
@Slf4j
public class AutoJobTaskTransferManager implements WithDaemonThread, ITaskEventHandler<TaskReceivedEvent> {
    private final AutoJobClusterManager manager;
    private final ClusterConfig config;
    private final Queue<AutoJobTask> waitTransferTaskQueue;
    private final ScheduleTaskUtil transferManagerDaemonThread;
    private final Map<Long, ClusterNode> transferFromMap;

    public AutoJobTaskTransferManager(AutoJobConfigHolder configHolder, AutoJobClusterManager manager) {
        this.config = configHolder.getClusterConfig();
        this.waitTransferTaskQueue = new LinkedBlockingQueue<>();
        this.manager = manager;
        this.transferManagerDaemonThread = ScheduleTaskUtil.build(true, "transferManagerDaemonThread");
        this.transferFromMap = new ConcurrentHashMap<>();
        startWork();
    }

    public boolean addTransferTask(AutoJobTask task) {
        if (waitTransferTaskQueue.contains(task)) {
            return true;
        }
        return waitTransferTaskQueue.offer(task);
    }

    public AutoJobClusterManager getManager() {
        return manager;
    }

    private Runnable daemonRunnable() {
        return () -> {
            List<ClusterNode> triedNodes = null;
            try {
                AutoJobTask task = waitTransferTaskQueue.peek();
                if (task == null) {
                    return;
                }
                int nodeCount = manager.getClusterContext().length();
                boolean find = false;
                triedNodes = new ArrayList<>();
                for (int i = 0; i < nodeCount; i++) {
                    //获取最优节点
                    ClusterNode node = manager.getClusterContext().getOptimal();
                    if (node != null) {
                        if (transferFromMap.containsKey(task.getId()) && node.getHost().equals(transferFromMap.get(task.getId()).getHost()) && node.getPort().equals(transferFromMap.get(task.getId()).getPort())) {
                            continue;
                        }
                        triedNodes.add(node);
                        //获取客户端
                        AutoJobClusterClient client = manager.getClusterClientMap().get(node);
                        client = client == null ? new AutoJobClusterClient(node, config) : client;
                        String key = client.transferTask(task);
                        if (!StringUtils.isEmpty(key)) {
                            log.warn("任务：{}已转移到节点：{}:{}运行", task.getId(), node.getHost(), node.getPort());
                            TaskEventManager.getInstance().publishTaskEvent(TaskEventFactory.newTaskTransferEvent(task, node, key), TaskTransferEvent.class, true);
                            find = true;
                            break;
                        }
                    }
                }
                if (!find) {
                    log.error("没有找到集群节点进行任务：{}的故障转移", task.getId());
                    TaskEventManager.getInstance().publishTaskEvent(TaskEventFactory.newErrorEvent(task), TaskErrorEvent.class, true);
                    TaskEventManager.getInstance().publishTaskEvent(TaskEventFactory.newFinishedEvent(task), TaskFinishedEvent.class, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                waitTransferTaskQueue.poll();
                //更新所有已尝试过的节点状态
                if (triedNodes != null && triedNodes.size() > 0) {
                    triedNodes.forEach(node -> {
                        manager.getClusterContext().updateStatus(node);
                    });
                }
            }
        };
    }

    public AutoJobClusterManager getClusterManager() {
        return manager;
    }

    @Override
    public void startWork() {
        log.info("任务故障转移负载均衡管理器启动");
        transferManagerDaemonThread.EFixedRateTask(daemonRunnable(), 1, 1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void doHandle(TaskReceivedEvent event) {
        transferFromMap.put(event.getTask().getId(), event.getTransferFrom());
    }
}
