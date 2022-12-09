package com.example.autojob.skeleton.model.task.method;

import com.example.autojob.skeleton.lang.IAutoJobFactory;
import com.example.autojob.skeleton.model.builder.AttributesBuilder;
import com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobTrigger;
import com.example.autojob.skeleton.model.executor.IMethodObjectFactory;
import com.example.autojob.util.convert.DateUtils;
import com.example.autojob.util.convert.RegexUtil;
import com.example.autojob.util.id.IdGenerator;
import com.example.autojob.util.id.SystemClock;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * method型任务工厂，如果你需要更细致的构造，请使用{@link AutoJobMethodTaskBuilder}
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/06 10:49
 */
@Slf4j
public class MethodTaskFactory implements IAutoJobFactory {
    public static MethodTask createTask(Class<?> methodClass, String methodName, String attributes, IMethodObjectFactory factory) {
        MethodTask methodTask = new MethodTask();
        methodTask.setMethodObjectFactory(factory);
        methodTask.setMethodClassName(methodClass.getName());
        methodTask
                .setId(IdGenerator.getNextIdAsLong())
                .setMethodClass(methodClass)
                .setParamsString(attributes)
                .setMethodName(methodName);
        return methodTask;
    }

    /**
     * 创建一个DB型任务
     *
     * @param methodClass 任务所在类
     * @param methodName  方法名
     * @param attributes  参数字符串，你可以使用AttributeBuilder辅助你
     * @param factory     对象工厂
     * @return com.example.autojob.skeleton.model.task.method.MethodTask
     * @author Huang Yongxiang
     * @date 2022/10/9 15:20
     * @see AttributesBuilder
     */
    public static MethodTask createDBTypeTask(Class<?> methodClass, String methodName, String attributes, IMethodObjectFactory factory) {
        MethodTask methodTask = createTask(methodClass, methodName, attributes, factory);
        methodTask.setType(AutoJobTask.TaskType.DB_TASK);
        return methodTask;
    }

    public static MethodTask createMemoryTypeTask(Class<?> methodClass, String methodName, String attributes, IMethodObjectFactory factory) {
        MethodTask methodTask = createTask(methodClass, methodName, attributes, factory);
        methodTask.setType(AutoJobTask.TaskType.MEMORY_TASk);
        methodTask.setAnnotationId(IdGenerator.getNextIdAsLong());
        return methodTask;
    }

    /**
     * 初始化任务调度器信息，该方法使用简单的启动时间-周期-重复执行次数来初始化一个触发器
     *
     * @param task        初始化的任务
     * @param startTime   启动时间 yyyy-MM-dd HH:mm:ss
     * @param repeatTimes 重复执行次数，一个任务的总执行次数=1+repeatTimes，-1将会被永久执行
     * @param cycle       周期
     * @param cycleUnit   周期的时间单温
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/8/23 17:40
     */
    public static boolean connectSimpleTriggerTask(MethodTask task, String startTime, int repeatTimes, long cycle, TimeUnit cycleUnit) {
        if (task == null) {
            log.error("连接任务失败，task为null");
            return false;
        }
        if (!RegexUtil.isMatch(startTime, RegexUtil.Type.DATE_TIME_YYYY_MM_DD_HH_MM_SS) || (repeatTimes > 0 && cycle <= 0)) {
            log.error("参数校验失败，请检查启动时间是否符合格式或者周期任务的周期是否未指定");
            return false;
        }
        Date startDate = null;
        try {
            startDate = DateUtils.parseDate(startTime, "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate == null) {
            return false;
        }
        AutoJobTrigger trigger = new AutoJobTrigger(startDate.getTime(), repeatTimes, cycleUnit.toMillis(cycle));
        task
                .setIsStart(false)
                .setIsFinished(false)
                .setTrigger(trigger);
        return true;
    }

    /**
     * 初始化调度器信息，该方法通过cron-like表达式来初始化一个触发器
     *
     * @param task           任务
     * @param cronExpression cron like表达式
     * @param repeatTimes    重复执行次数，一个任务的总执行次数=1+repeatTimes，-1将会被永久执行
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/8/23 17:41
     */
    public static boolean connectCronExpressionTriggerTask(MethodTask task, String cronExpression, int repeatTimes) {
        if (task == null) {
            return false;
        }
        AutoJobTrigger trigger = new AutoJobTrigger(cronExpression, repeatTimes);
        trigger.refresh();
        task
                .setIsStart(false)
                .setIsFinished(false)
                .setTrigger(trigger);
        return true;
    }

    public static boolean connectChildTriggerTask(MethodTask task, MethodTask parentTask) {
        if (task == null) {
            return false;
        }
        AutoJobTrigger trigger = new AutoJobTrigger();
        trigger.setTriggeringTime(Long.MAX_VALUE);
        trigger.setFinishedTimes(0);
        trigger.setRepeatTimes(parentTask
                .getTrigger()
                .getRepeatTimes());
        task
                .setIsStart(false)
                .setIsFinished(false)
                .setTrigger(trigger);
        return true;
    }

    /**
     * 初始化一个立即执行的调度器，为了能被准确调度，执行反应在5秒后
     *
     * @param task        任务
     * @param repeatTimes 重复执行次数，一个任务的总执行次数=1+repeatTimes，-1将会被永久执行
     * @param cycle       周期
     * @param cycleUnit   周期的时间单位
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/8/23 17:43
     */
    public static boolean connectRunNowTask(MethodTask task, int repeatTimes, long cycle, TimeUnit cycleUnit) {
        return connectSimpleTriggerTask(task, DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", new Date(System.currentTimeMillis() + 5000)), repeatTimes, cycle, cycleUnit);
    }
}
