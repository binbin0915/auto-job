package com.example.autojob.skeleton.framework.container;

import com.example.autojob.skeleton.lang.WithDaemonThread;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.util.cache.LocalCacheManager;
import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.thread.ScheduleTaskUtil;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.jodah.expiringmap.ExpirationPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 内存任务容器
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/14 14:17
 */
public class MemoryTaskContainer implements WithDaemonThread {
    private final Map<String, AutoJobTask> memoryTaskContainer = new ConcurrentHashMap<>();
    private final AtomicInteger size = new AtomicInteger(0);
    private LocalCacheManager<String, AutoJobTask> finishedTaskCache;
    private CleanStrategy cleanStrategy;
    private int limitSize;

    private MemoryTaskContainer() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public void insert(AutoJobTask task) {
        if (size.get() >= limitSize) {
            throw new AutoJobContainerException("已超出内存任务容器容量：" + limitSize);
        }
        if (task == null || task.getTrigger() == null || task.getId() == null) {
            throw new IllegalArgumentException("任务和触发器必须存在");
        }
        if (task.getId() == null && StringUtils.isEmpty(task.getAlias()) && task.getAnnotationId() == null) {
            throw new IllegalArgumentException("任务ID、注解ID、任务别名必须存在一个");
        }
        if (task.getType() != AutoJobTask.TaskType.MEMORY_TASk) {
            throw new IllegalArgumentException("只能插入内存任务");
        }
        boolean flag = false;
        if (task.getId() != null) {
            memoryTaskContainer.put(task.getId() + "", task);
            flag = true;
        }
        if (task.getAnnotationId() != null && !memoryTaskContainer.containsKey(task.getAnnotationId() + "")) {
            memoryTaskContainer.put(task.getAnnotationId() + "", task);
            flag = true;
        }
        if (!StringUtils.isEmpty(task.getAlias()) && !memoryTaskContainer.containsKey(task.getAlias())) {
            memoryTaskContainer.put(task.getAlias(), task);
            flag = true;
        }
        if (flag) {
            size.incrementAndGet();
        }
    }

    public AutoJobTask getById(long taskId) {
        return memoryTaskContainer.get(taskId + "");
    }

    public AutoJobTask getByAlias(String alias) {
        return memoryTaskContainer.get(alias);
    }

    public AutoJobTask getByAnnotationId(long annotationId) {
        return memoryTaskContainer.get(annotationId + "");
    }

    public AutoJobTask removeById(long taskId) {
        return memoryTaskContainer.remove(taskId + "");
    }

    public AutoJobTask removeByAlias(String alias) {
        return memoryTaskContainer.remove(alias);
    }

    public AutoJobTask removeByAnnotationId(long annotationId) {
        return memoryTaskContainer.remove(annotationId + "");
    }

    public AutoJobTask remove(AutoJobTask task) {
        AutoJobTask removed = removeById(task.getId());
        removeByAlias(task.getAlias());
        removeByAnnotationId(task.getAnnotationId());
        return removed;
    }

    public List<AutoJobTask> getFutureRun(long futureTime, TimeUnit unit) {
        return memoryTaskContainer
                .values()
                .stream()
                .filter(task -> !task
                        .getTrigger()
                        .getIsPause())
                .filter(task -> task.getIsFinished() == null || !task.getIsFinished())
                .filter(task -> task.getIsStart() == null || !task.getIsStart())
                .filter(task -> task
                        .getTrigger()
                        .getIsRunning() == null || !task
                        .getTrigger()
                        .getIsRunning())
                .filter(task -> task
                        .getTrigger()
                        .isNearTriggeringTime(unit.toMillis(futureTime)) || task.getIsChildTask())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<AutoJobTask> list() {
        return new ArrayList<>(memoryTaskContainer.values());
    }

    public int size() {
        return memoryTaskContainer.size() + (finishedTaskCache == null ? 0 : finishedTaskCache.size());
    }

    public LocalCacheManager<String, AutoJobTask> getFinishedTaskCache() {
        return finishedTaskCache;
    }

    @Override
    public void startWork() {
        ScheduleTaskUtil
                .build(true, "memoryContainerDaemon")
                .EFixedRateTask(() -> {
                    for (Map.Entry<String, AutoJobTask> entry : memoryTaskContainer.entrySet()) {
                        AutoJobTask task = entry.getValue();
                        if (task.getIsFinished() != null && task.getIsFinished()) {
                            memoryTaskContainer.remove(entry.getKey());
                            if (cleanStrategy == CleanStrategy.KEEP_FINISHED_TASK) {
                                finishedTaskCache.set(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }, 1000 - System.currentTimeMillis() % 1000, 1, TimeUnit.MILLISECONDS);
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {
        /**
         * 容器大小
         */
        private int limitSize = 100;

        /**
         * 清理策略
         */
        private CleanStrategy cleanStrategy = CleanStrategy.CLEAN_FINISHED_TASK;

        public MemoryTaskContainer build() {
            MemoryTaskContainer container = new MemoryTaskContainer();
            container.limitSize = limitSize;
            container.cleanStrategy = cleanStrategy;
            if (cleanStrategy == CleanStrategy.KEEP_FINISHED_TASK) {
                container.finishedTaskCache = LocalCacheManager
                        .builder()
                        .setExpiringTime(24, TimeUnit.HOURS)
                        .setEntriesExpiration(true)
                        .setMaxLength(limitSize * 2)
                        .setPolicy(ExpirationPolicy.CREATED)
                        .build();
            }
            return container;
        }
    }
}
