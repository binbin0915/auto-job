package com.example.autojob.skeleton.framework.task;

import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.skeleton.enumerate.SchedulingStrategy;
import com.example.autojob.skeleton.framework.pool.RunnablePostProcessor;
import com.example.autojob.skeleton.model.task.DefaultRunnablePostProcessor;
import com.example.autojob.skeleton.model.task.TaskExecutable;
import com.example.autojob.util.convert.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 任务的超类，所有任务都应该继承该类，该类及其子类作为任务调度的基本单元
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/02 16:55
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public abstract class AutoJobTask {
    /**
     * 任务Id
     */
    protected Long id;
    /**
     * 任务别名
     */
    protected String alias;
    /**
     * 注解ID
     */
    protected Long annotationId;
    /**
     * 触发器
     */
    protected AutoJobTrigger trigger;
    /**
     * 调度策略
     */
    protected SchedulingStrategy schedulingStrategy;
    /**
     * 是否已开始
     */
    protected Boolean isStart = false;
    /**
     * 运行方法所在的类
     */
    protected Class<?> methodClass;
    /**
     * 运行方法名
     */
    protected String methodName;
    /**
     * 运行结果
     */
    protected AutoJobRunResult runResult;
    /**
     * 是否已结束
     */
    protected Boolean isFinished;
    /**
     * 任务参数
     */
    protected Object[] params;
    /**
     * 参数字符串
     */
    protected String paramsString;
    /**
     * 是否允许被注册
     */
    protected Boolean isAllowRegister = true;
    /**
     * 任务类型
     */
    protected TaskType type;

    /**
     * 归属于
     */
    protected Long belongTo;

    /**
     * 任务优先级
     */
    protected Integer taskLevel = -1;
    /**
     * 是否是子任务
     */
    protected Boolean isChildTask = false;
    /**
     * 任务持有的logHelper对象实例
     */
    protected AutoJobLogHelper logHelper;

    @Override
    public boolean equals(Object o) {
        if (o instanceof AutoJobTask) {
            AutoJobTask task = (AutoJobTask) o;
            return task.id != null && task.id.equals(id);
        }
        return false;
    }

    /**
     * 任务需要能够从中获取可执行对象
     *
     * @return com.example.autojob.skeleton.framework.pool.Executable
     * @author Huang Yongxiang
     * @date 2022/8/3 13:58
     */
    public abstract TaskExecutable getExecutable();

    /**
     * 用于判断某个任务能否被执行
     *
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/11/2 17:33
     */
    public boolean isExecutable() {
        return getExecutable() != null && getExecutable().isExecutable();
    }

    /**
     * 任务需要能够获取对应的后置处理器，自定义后置处理器必须保证能执行父后置处理器的逻辑
     *
     * @return com.example.autojob.skeleton.framework.pool.RunnablePostProcessor
     * @author Huang Yongxiang
     * @date 2022/8/3 16:27
     */
    public RunnablePostProcessor getRunnablePostProcessor() {
        return new DefaultRunnablePostProcessor();
    }

    public String getReference() {
        return String.format("%s%s", methodClass == null ? "" : methodClass.getName() + ".", methodName);
    }

    public enum TaskType {
        /**
         * 内存型任务的生命周期都将在内存进行，其本身要比DB型任务的调度更为精确和快速，适合周期短的任务
         */
        MEMORY_TASk("Memory"),
        /**
         * DB型任务将会在数据库保存任务基本信息和触发器信息，短周期DB任务触发可能不会很精确，DB任务在调度前都会经历一个获取锁的过程，只有获取到锁才能被执行
         */
        DB_TASK("DB");
        String description;

        TaskType(String description) {
            this.description = description;
        }

        public static TaskType convert(String description) {
            if (StringUtils.isEmpty(description)) {
                return null;
            }
            description = description.toLowerCase();
            switch (description) {
                case "db": {
                    return DB_TASK;
                }
                case "memory": {
                    return MEMORY_TASk;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
