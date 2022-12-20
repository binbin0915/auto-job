package com.example.autojob.skeleton.model.handler;

import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobWrapper;
import com.example.autojob.skeleton.model.scheduler.AutoJobScanner;
import com.example.autojob.util.id.IdGenerator;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Huang Yongxiang
 * @date 2022-12-03 17:46
 * @email 1158055613@qq.com
 */
@Slf4j
public class AutoJobAnnotationTaskHandler extends AbstractAnnotationTaskHandler {
    private final Class<? extends Annotation> type;

    public AutoJobAnnotationTaskHandler(AbstractAnnotationFilter filter, AutoJobWrapper wrapper, Class<? extends Annotation> type) {
        super(AbstractAnnotationFilter
                .builder()
                .addHandler(new ConditionFilter())
                .addHandler(new ClasspathFilter())
                .build()
                .add(filter), wrapper);
        this.type = type;
    }

    public AutoJobAnnotationTaskHandler(AutoJobWrapper wrapper, Class<? extends Annotation> type) {
        this(null, wrapper, type);
    }

    @Override
    public Set<Method> scanMethods(String... pattern) {
        AutoJobScanner scanner = new AutoJobScanner(pattern);
        return scanner.scanMethods(type);
    }

    @Override
    public int load(Set<Method> filteredMethods, AutoJobWrapper wrapper) {
        List<AutoJobTask> memoryTypeMethods = new ArrayList<>();

        List<AutoJobTask> dbTypeMethods = new ArrayList<>();
        for (Method method : filteredMethods) {
            AutoJobTask task = wrapper.wrapper(method);
            if (task == null) {
                continue;
            }
            if (!task.isExecutable()) {
                log.error("方法：{}包装失败", method.getName());
                continue;
            }
            if (task.getSchedulingStrategy() == null) {
                throw new IllegalArgumentException("方法：" + task.getAlias() + "没有指定调度策略");
            }
            if (task.getId() == null) {
                task.setId(IdGenerator.getNextIdAsLong());
            }
            if (task.getAnnotationId() == null) {
                task.setAnnotationId((long) Math.abs(task
                        .getReference()
                        .hashCode()));
            }
            if (task.getType() == AutoJobTask.TaskType.MEMORY_TASk) {
                memoryTypeMethods.add(task);
            } else if (task.getType() == AutoJobTask.TaskType.DB_TASK) {
                dbTypeMethods.add(task);
            }
        }
        return new MemoryTaskLoader().load(memoryTypeMethods) + new DBTaskLoader().load(dbTypeMethods);
    }
}
