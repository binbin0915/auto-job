package com.example.autojob.skeleton.model.builder;

import com.example.autojob.skeleton.db.entity.AutoJobTaskEntity;
import com.example.autojob.skeleton.db.entity.AutoJobTriggerEntity;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.db.mapper.TransactionEntry;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobTrigger;
import com.example.autojob.skeleton.model.executor.DefaultMethodObjectFactory;
import com.example.autojob.skeleton.model.executor.IMethodObjectFactory;
import com.example.autojob.skeleton.model.interpreter.AutoJobAttributeContext;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.util.convert.StringUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 任务构建类，构建一个完整的方法型任务
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/27 9:28
 * @Email 1158055613@qq.com
 */
public class AutoJobMethodTaskBuilder {
    private static final AttributesBuilder ATTRIBUTES_BUILDER = new AttributesBuilder();
    /**
     * 任务ID，该ID将会作为调度的唯一键
     */
    private long taskId;

    /**
     * 任务所在的类
     */
    private final Class<?> taskClass;

    /**
     * 任务方法名
     */
    private final String methodName;

    /**
     * 参数字符串
     */
    private String paramsString;

    /**
     * 任务类型，默认是内存型任务
     */
    private AutoJobTask.TaskType taskType;

    /**
     * 任务别名
     */
    private String taskAlias;

    /**
     * 任务级别
     */
    private int taskLevel;

    /**
     * 任务方法类构建工厂
     */
    private IMethodObjectFactory methodObjectFactory;

    /**
     * 触发器
     */
    private AutoJobTrigger trigger;

    /**
     * 所属
     */
    private Long belongTo;

    private Object[] params;

    private boolean isChildTask;

    /**
     * 如果任务时DB型任务是否保存
     */
    private boolean isSaveWhenDB;

    public AutoJobMethodTaskBuilder(Class<?> taskClass, String methodName) {
        this.taskClass = taskClass;
        this.methodName = methodName;
        this.taskId = -1;
        this.taskLevel = 0;
        this.taskAlias = String.format("%s.%s", taskClass.getName(), methodName);
        this.taskType = AutoJobTask.TaskType.MEMORY_TASk;
        this.methodObjectFactory = new DefaultMethodObjectFactory();
    }

    public AutoJobMethodTaskBuilder setTaskId(long taskId) {
        this.taskId = taskId;
        return this;
    }

    public AutoJobMethodTaskBuilder setTaskType(AutoJobTask.TaskType taskType) {
        this.taskType = taskType;
        return this;
    }

    public AutoJobMethodTaskBuilder setTaskAlias(String taskAlias) {
        this.taskAlias = taskAlias;
        return this;
    }

    public AutoJobMethodTaskBuilder setTaskLevel(int taskLevel) {
        this.taskLevel = taskLevel;
        return this;
    }

    public AutoJobMethodTaskBuilder setMethodObjectFactory(IMethodObjectFactory methodObjectFactory) {
        this.methodObjectFactory = methodObjectFactory;
        return this;
    }

    public AutoJobMethodTaskBuilder setBelongTo(Long belongTo) {
        this.belongTo = belongTo;
        return this;
    }

    public AutoJobMethodTaskBuilder isSaveWhenDB(boolean saveWhenDB) {
        isSaveWhenDB = saveWhenDB;
        return this;
    }

    /**
     * 添加一个任务参数，参数类型为枚举中的类型，注意参数顺序将会按照添加顺序注入
     *
     * @param type  参数类型
     * @param value 参数值
     * @return com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder
     * @author Huang Yongxiang
     * @date 2022/10/27 9:53
     */
    public AutoJobMethodTaskBuilder addParam(AttributesBuilder.AttributesType type, Object value) {
        ATTRIBUTES_BUILDER.addParams(type, value);
        return this;
    }

    /**
     * 添加一个参数，该参数类型为可被Json序列化/反序列化的对象类型，注意参数顺序将会按照添加顺序注入
     *
     * @param paramType 参数类型
     * @param value     参数值
     * @return com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder
     * @author Huang Yongxiang
     * @date 2022/10/27 9:54
     */
    public AutoJobMethodTaskBuilder addParam(Class<?> paramType, Object value) {
        ATTRIBUTES_BUILDER.addParams(paramType, value);
        return this;
    }

    public AutoJobMethodTaskBuilder setParams(String paramsString) {
        if (StringUtils.isEmpty(paramsString)) {
            return this;
        }
        AutoJobAttributeContext context = new AutoJobAttributeContext(paramsString);
        this.params = context.getAttributeEntity();
        this.paramsString = paramsString;
        return this;
    }

    /**
     * 添加一个简单触发器，添加多个触发器时前者将被后者覆盖
     *
     * @param firstTriggeringTime 首次触发时间
     * @param repeatTimes         重复次数，任务总触发次数=1+repeatTimes，-1表示无限次触发
     * @param cycle               周期
     * @param unit                时间单位
     * @return com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder
     * @author Huang Yongxiang
     * @date 2022/10/27 9:59
     */
    public AutoJobMethodTaskBuilder addASimpleTrigger(long firstTriggeringTime, int repeatTimes, long cycle, TimeUnit unit) {
        this.trigger = AutoJobTriggerFactory
                .newSimpleTrigger(firstTriggeringTime, repeatTimes, cycle, unit)
                .setTaskId(taskId);
        return this;
    }

    /**
     * 添加一个cron-like表达式的触发器，添加多个触发器时前者将被后者覆盖
     *
     * @param cronExpression cron-like表达式
     * @param repeatTimes    重复次数，任务总触发次数=1+repeatTimes，-1表示无限次触发
     * @return com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder
     * @author Huang Yongxiang
     * @date 2022/10/27 10:03
     */
    public AutoJobMethodTaskBuilder addACronExpressionTrigger(String cronExpression, int repeatTimes) {
        this.trigger = AutoJobTriggerFactory
                .newCronExpressionTrigger(cronExpression, repeatTimes)
                .setTaskId(taskId);
        return this;
    }

    /**
     * 添加一个子任务触发器，该任务将会作为一个子任务参与调度，添加多个触发器时前者将被后者覆盖
     *
     * @return com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder
     * @author Huang Yongxiang
     * @date 2022/10/27 10:09
     */
    public AutoJobMethodTaskBuilder addAChildTaskTrigger() {
        this.trigger = AutoJobTriggerFactory
                .newChildTrigger()
                .setTaskId(taskId);
        isChildTask = true;
        return this;
    }

    /**
     * 添加一个延迟触发器，任务将会在给定延迟后触发一次，添加多个触发器时前者将被后者覆盖
     *
     * @param delay 距离现在延迟执行的时间
     * @param unit  时间单位
     * @return com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder
     * @author Huang Yongxiang
     * @date 2022/10/27 10:12
     */
    public AutoJobMethodTaskBuilder addADelayTrigger(long delay, TimeUnit unit) {
        this.trigger = AutoJobTriggerFactory
                .newDelayTrigger(delay, unit)
                .setTaskId(taskId);
        return this;
    }

    public MethodTask build() {
        MethodTask methodTask = new MethodTask();
        methodTask.setAnnotationId(taskId);
        methodTask.setId(taskId);
        methodTask.setBelongTo(belongTo);
        methodTask.setAlias(taskAlias);
        methodTask.setTrigger(trigger);
        methodTask.setIsChildTask(isChildTask);
        methodTask.setParamsString(paramsString);
        methodTask.setMethodObjectFactory(methodObjectFactory);
        if (StringUtils.isEmpty(methodTask.getParamsString())) {
            methodTask.setParamsString(ATTRIBUTES_BUILDER.getAttributesString());
        }
        methodTask.setType(taskType);
        methodTask.setMethodName(methodName);
        methodTask.setMethodClass(taskClass);
        methodTask.setTaskLevel(taskLevel);
        methodTask.setMethodClassName(taskClass.getName());
        if (params == null) {
            AutoJobAttributeContext context = new AutoJobAttributeContext(methodTask);
            methodTask.setParams(context.getAttributeEntity());
        } else {
            methodTask.setParams(this.params);
        }
        if (taskType == AutoJobTask.TaskType.DB_TASK && isSaveWhenDB) {
            AutoJobTriggerEntity triggerEntity = EntityConvertor.trigger2TriggerEntity(trigger);
            AutoJobTaskEntity taskEntity = EntityConvertor.task2TaskEntity(methodTask, triggerEntity.getId());
            TransactionEntry insertTask = connection -> AutoJobMapperHolder.TASK_ENTITY_MAPPER.insertList(Collections.singletonList(taskEntity));
            TransactionEntry insertTrigger = connection -> AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.insertList(Collections.singletonList(triggerEntity));
            AutoJobMapperHolder.TASK_ENTITY_MAPPER.doTransaction(new TransactionEntry[]{insertTask, insertTrigger});
        }
        return methodTask;
    }


}
