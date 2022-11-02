package com.example.autojob.skeleton.lifecycle;

import com.example.autojob.skeleton.framework.event.AbstractEventHandlerDelegate;
import com.example.autojob.skeleton.framework.event.AutoJobEvent;
import com.example.autojob.skeleton.framework.event.IEventHandler;
import com.example.autojob.skeleton.lifecycle.event.TaskEvent;
import com.example.autojob.util.id.SystemClock;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 任务事件处理器的委托者
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/05 9:37
 */
@Slf4j
public class TaskEventHandlerDelegate extends AbstractEventHandlerDelegate<TaskEvent> {

    public TaskEventHandlerDelegate(Map<Class<? extends AutoJobEvent>, List<Object>> handlerContainer) {
        super(handlerContainer);
    }

    @Override
    public boolean isParentEvent(Class<? extends AutoJobEvent> eventClass) {
        return eventClass == TaskEvent.class;
    }

    public static TaskEventHandlerDelegate getInstance() {
        return InstanceHolder.handlerDelegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doHandle(TaskEvent event, boolean isAllowBubbling) {
        long start = SystemClock.now();
        if (isAllowBubbling) {
            parentHandler
                    .stream()
                    .sorted(new TaskEventHandlerComparator())
                    .collect(Collectors.toList())
                    .forEach(item -> {
                        IEventHandler<TaskEvent> handler = (IEventHandler<TaskEvent>) item;
                        handler.doHandle(event);
                    });
        }
        if (handlerContainer.containsKey(event.getClass())) {
            handlerContainer
                    .get(event.getClass())
                    .stream()
                    .sorted(new TaskEventHandlerComparator())
                    .collect(Collectors.toList())
                    .forEach(item -> {
                        IEventHandler<TaskEvent> handler = (IEventHandler<TaskEvent>) item;
                        handler.doHandle(event);
                    });
        }
    }

    private static class InstanceHolder {
        private static final TaskEventHandlerDelegate handlerDelegate = new TaskEventHandlerDelegate(new ConcurrentHashMap<>());
    }

    private static class TaskEventHandlerComparator implements Comparator<Object> {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            if (o1 instanceof ITaskEventHandler && o2 instanceof ITaskEventHandler) {
                ITaskEventHandler<TaskEvent> handler1 = (ITaskEventHandler<TaskEvent>) o1;
                ITaskEventHandler<TaskEvent> handler2 = (ITaskEventHandler<TaskEvent>) o2;
                return Integer.compare(handler2.getHandlerLevel(), handler1.getHandlerLevel());
            }
            return 0;
        }
    }


}
