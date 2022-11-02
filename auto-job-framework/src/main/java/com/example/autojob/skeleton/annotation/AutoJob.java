package com.example.autojob.skeleton.annotation;

import com.example.autojob.skeleton.enumerate.SchedulingStrategy;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.enumerate.StartTime;
import com.example.autojob.skeleton.model.executor.DefaultMethodObjectFactory;
import com.example.autojob.skeleton.model.executor.IMethodObjectFactory;
import com.example.autojob.skeleton.model.task.method.MethodTask;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 该注解用在某个方法上，该方法在框架启动时会被扫描到并且被包装成一个{@link MethodTask}对象参与调度
 *
 * @Auther Huang Yongxiang
 * @Date 2022/01/23 18:42
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoJob {
    /*=================基础信息=================>*/

    /**
     * 注解Id，内存任务通过该字段能更方便的查找存储到数据库中的日志，DB任务在每次框架启动时，同一个注解Id标注的任务会被认为是同一个任务的不同版本，便于历史溯源，DB
     * 任务如果不指定该字段，则在框架正常退出时会直接删除相关的任务和触发器信息
     */
    long id() default -1L;

    /**
     * 任务别名
     */
    String alias() default "Default";

    /**
     * 参数，支持simple参数
     */
    String attributes() default "";

    /**
     * 子任务ID，多个逗号分割
     */
    String childTasksId() default "";

    /**
     * 最大执行时长，超出时系统将会尝试停止，默认是24小时
     */
    long maximumExecutionTime() default -1;

    /**
     * 方法依赖的类工厂，工厂必须提供无参构造方法
     */
    Class<? extends IMethodObjectFactory> methodObjectFactory() default DefaultMethodObjectFactory.class;

    /**
     * 该job以何种类型方式调度，DB方式调度请最好指定id，否则该任务在系统结束运行前将会被删除
     */
    AutoJobTask.TaskType asType() default AutoJobTask.TaskType.MEMORY_TASk;

    /**
     * 任务级别，相同时间情况下高优先级任务会被优先调度
     */
    int taskLevel() default -1;
    /*=======================Finished======================<*/


    /*=================调度信息=================>*/

    /**
     * 调度策略
     */
    SchedulingStrategy schedulingStrategy() default SchedulingStrategy.JOIN_SCHEDULING;

    /**
     * 启动时间 yyyy-MM-dd HH:mm:ss格式
     */
    String startTime() default "";

    /**
     * 默认启动时间，和startTime同时存在时以startTime为准
     */
    StartTime defaultStartTime() default StartTime.EMPTY;

    /**
     * cron like表达式
     */
    String cronExpression() default "";

    /**
     * 重复次数，一个任务的总执行次数=1+repeatTimes，-1表示永久执行
     */
    int repeatTimes() default 0;

    /**
     * 周期：默认为秒，当重复次数大于0时该值必须大于0
     */
    long cycle() default 0;

    /**
     * 周期的时间单位，默认为秒
     */
    TimeUnit cycleUnit() default TimeUnit.SECONDS;
    /*=======================Finished======================<*/


}
