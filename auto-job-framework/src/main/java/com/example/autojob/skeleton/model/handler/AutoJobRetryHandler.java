package com.example.autojob.skeleton.model.handler;

import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.config.AutoJobRetryConfig;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.lifecycle.TaskEventFactory;
import com.example.autojob.skeleton.lifecycle.event.imp.TaskRetryEvent;
import com.example.autojob.skeleton.lifecycle.manager.TaskEventManager;
import com.example.autojob.util.convert.DefaultValueUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务重试管理器
 *
 * @author Huang Yongxiang
 * @date 2022-12-30 14:05
 * @email 1158055613@qq.com
 */
@Slf4j
public class AutoJobRetryHandler {
    private final Map<Long, AtomicInteger> retryMap = new ConcurrentHashMap<>();

    private AutoJobRetryHandler() {
    }

    public static AutoJobRetryHandler getInstance() {
        return InstanceHolder.HANDLER;
    }

    public boolean retry(AutoJobTask task) {
        AutoJobRetryConfig config = DefaultValueUtil.defaultValue(task.getRetryConfig(), AutoJobApplication
                .getInstance()
                .getConfigHolder()
                .getAutoJobConfig()
                .getRetryConfig());
        if (config == null) {
            throw new NullPointerException();
        }
        if (!retryMap.containsKey(task.getId())) {
            retryMap.put(task.getId(), new AtomicInteger(0));
        }
        if (retryMap
                .get(task.getId())
                .get() >= config.getRetryCount()) {
            return false;
        }
        try {
            task.setIsRetrying(true);
            task
                    .getTrigger()
                    .setTriggeringTime((long) (System.currentTimeMillis() + config.getInterval() * 60 * 1000));
            if (task
                    .getTrigger()
                    .isNearTriggeringTime(5000)) {
                AutoJobApplication
                        .getInstance()
                        .getRegister()
                        .registerTask(task);
            }
            if (task.getType() == AutoJobTask.TaskType.DB_TASK) {
                AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.updateTriggeringTime(task.getId(), task
                        .getTrigger()
                        .getTriggeringTime());
            }
            retryMap
                    .get(task.getId())
                    .incrementAndGet();
            TaskEventManager
                    .getInstance()
                    .publishTaskEventSync(TaskEventFactory.newTaskRetryEvent(task, retryMap
                            .get(task.getId())
                            .get(), task
                            .getTrigger()
                            .getTriggeringTime(), config.getRetryCount()), TaskRetryEvent.class, true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void remove(long taskId) {
        retryMap.remove(taskId);
    }

    public static class InstanceHolder {
        private static final AutoJobRetryHandler HANDLER = new AutoJobRetryHandler();
    }
}
