package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.annotation.*;
import com.example.autojob.skeleton.db.entity.AutoJobTaskEntity;
import com.example.autojob.skeleton.db.entity.AutoJobTriggerEntity;
import com.example.autojob.skeleton.db.entity.EntityConvertor;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.db.mapper.TransactionEntry;
import com.example.autojob.skeleton.enumerate.SchedulingStrategy;
import com.example.autojob.skeleton.framework.config.AutoJobConfigHolder;
import com.example.autojob.skeleton.framework.config.TimeConstant;
import com.example.autojob.skeleton.framework.container.MemoryTaskContainer;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobTrigger;
import com.example.autojob.skeleton.framework.task.TaskRunningContext;
import com.example.autojob.skeleton.model.executor.AutoJobTaskExecutorPool;
import com.example.autojob.skeleton.model.executor.IMethodObjectFactory;
import com.example.autojob.skeleton.model.register.AutoJobRegisterRefusedException;
import com.example.autojob.skeleton.model.register.IAutoJobRegister;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.skeleton.model.task.method.MethodTaskFactory;
import com.example.autojob.util.bean.ObjectUtil;
import com.example.autojob.util.convert.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 注解调度器
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/15 15:50
 */
@Slf4j
public class AutoJobAnnotationScheduler extends AbstractScheduler {
    private Set<Method> methods;
    private Set<Class<?>> classSet;
    private final MemoryTaskContainer memoryTaskContainer = AutoJobApplication
            .getInstance()
            .getMemoryTaskContainer();

    public AutoJobAnnotationScheduler(AutoJobTaskExecutorPool executorPool, IAutoJobRegister register, AutoJobConfigHolder configHolder) {
        super(executorPool, register, configHolder);
    }

    public void scan(AutoJobScanner scanner) {
        this.methods = scanner.scanMethods();
        //this.classSet = scanner.scanClasses();
    }

    private int registerMethods() {
        return registerMethods(this.methods.toArray(new Method[]{}));
    }

    private int registerMethods(Method[] methods) {
        int count = 0;
        List<AutoJobTask> dbTasks = new ArrayList<>();
        List<AutoJobTask> saveTasks = new ArrayList<>();
        List<MethodTask> childTasks = new ArrayList<>();
        Map<Long, MethodTask> parentTaskMap = new HashMap<>();
        for (Method method : methods) {
            method.setAccessible(true);
            Conditional conditional = method.getAnnotation(Conditional.class);
            FactoryAutoJob factoryAutoJob = method.getAnnotation(FactoryAutoJob.class);
            AutoJob autoJob = method.getAnnotation(AutoJob.class);
            /*=================验证注册条件=================>*/
            boolean init = true;
            if (null != conditional) {
                Class<? extends IAutoJobCondition>[] classes = conditional.value();
                for (Class<? extends IAutoJobCondition> condition : classes) {
                    if (!ObjectUtil
                            .getClassInstance(condition)
                            .matches(configHolder.getPropertiesHolder(), AutoJobApplication.getInstance())) {
                        log.warn("任务：{}.{}将不会被注入", method
                                .getDeclaringClass()
                                .getName(), method.getName());
                        init = false;
                        break;
                    }
                }
            }
            if (!init) {
                continue;
            }

            /*=======================Finished======================<*/

            /*=================工厂任务单独处理=================>*/
            if (null != factoryAutoJob) {
                IMethodTaskFactory factory = ObjectUtil.getClassInstance(factoryAutoJob.value());
                AutoJobTask factoryTask = factory.newTask(configHolder, method.getDeclaringClass(), method.getName());
                if (factoryTask.getTrigger() == null || factoryTask.getMethodClass() == null || StringUtils.isEmpty(factoryTask.getMethodName())) {
                    throw new NullPointerException();
                }
                if (factoryTask.getType() == AutoJobTask.TaskType.DB_TASK) {
                    dbTasks.add(factoryTask);
                    if (factoryTask
                            .getTrigger()
                            .isReachTriggerTime()) {
                        try {
                            register.registerTask(factoryTask);
                        } catch (AutoJobRegisterRefusedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        memoryTaskContainer.insert(factoryTask);
                    } catch (Exception e) {
                        log.error("插入内存任务：{}时发生异常：{}", factoryTask.getReference(), e.getMessage());
                        throw e;
                    }

                }
            }
            /*=======================Finished======================<*/

            else if (null != autoJob) {
                /*=================基础验证=================>*/
                if (autoJob.id() != -1 && autoJob.id() < 0) {
                    throw new IllegalArgumentException("任务的Id必须为非负数：" + method);
                }
                if (autoJob.asType() == AutoJobTask.TaskType.MEMORY_TASk && autoJob.id() == -1) {
                    throw new IllegalArgumentException("内存任务必须指定ID：" + method);
                }
                if (autoJob.schedulingStrategy() == SchedulingStrategy.AS_CHILD_TASK && autoJob.id() == -1) {
                    throw new IllegalArgumentException("子任务必须有给定ID：" + method);
                }
                if (autoJob.schedulingStrategy() == SchedulingStrategy.ONLY_SAVE && autoJob.asType() == AutoJobTask.TaskType.MEMORY_TASk) {
                    throw new UnsupportedOperationException("内存任务不支持ONLY_SAVE调度策略：" + method);
                }
                /*=======================Finished======================<*/

                IMethodObjectFactory factory = ObjectUtil.getClassInstance(autoJob.methodObjectFactory());
                MethodTask task;
                /*=================创建任务基本信息=================>*/
                if (autoJob.asType() == AutoJobTask.TaskType.MEMORY_TASk) {
                    task = MethodTaskFactory.createMemoryTypeTask(method.getDeclaringClass(), method.getName(), autoJob.attributes(), factory);
                } else {
                    task = MethodTaskFactory.createDBTypeTask(method.getDeclaringClass(), method.getName(), autoJob.attributes(), factory);
                }
                task.setTaskLevel(autoJob.taskLevel());
                if (autoJob.id() != -1) {
                    if (task.getType() == AutoJobTask.TaskType.MEMORY_TASk) {
                        task.setId(autoJob.id());
                    }
                    task.setAnnotationId(autoJob.id());
                }
                task.setAlias("Default".equals(autoJob.alias()) ? task.getReference() : autoJob.alias());
                /*=======================Finished======================<*/

                /*=================处理ONLY_SAVE策略=================>*/
                if (autoJob.schedulingStrategy() == SchedulingStrategy.ONLY_SAVE) {
                    saveTasks.add(task);
                    continue;
                }
                /*=======================Finished======================<*/

                /*=================创建调度器信息=================>*/
                AutoJobTrigger trigger = autoJob
                        .schedulingStrategy()
                        .createTrigger(task.getId(), autoJob);
                if (trigger != null) {
                    task.setTrigger(trigger);
                    task
                            .getTrigger()
                            .setMaximumExecutionTime(autoJob.maximumExecutionTime() == -1 ? TimeConstant.A_DAY : autoJob.maximumExecutionTime());
                } else {
                    throw new NullPointerException();
                }
                if (autoJob.schedulingStrategy() == SchedulingStrategy.AS_CHILD_TASK) {
                    task.setIsChildTask(true);
                    childTasks.add(task);
                } else if (task
                        .getTrigger()
                        .getTriggeringTime() == null) {
                    task
                            .getTrigger()
                            .refresh();
                }

                /*=======================Finished======================<*/

                /*=================验证子任务=================>*/
                if (!StringUtils.isEmpty(autoJob.childTasksId())) {
                    String[] children = autoJob
                            .childTasksId()
                            .trim()
                            .split(",");
                    task
                            .getTrigger()
                            .setChildTask(new ArrayList<>());
                    for (String child : children) {
                        long id = Long.parseLong(child);
                        if (id != task.getAnnotationId()) {
                            parentTaskMap.put(id, task);
                            task
                                    .getTrigger()
                                    .getChildTask()
                                    .add(id);
                        } else {
                            log.warn("任务：{}的子任务不能是本身", id);
                        }
                    }
                }
                /*=======================Finished======================<*/

                /*=================验证DB任务=================>*/
                if (task.getType() == AutoJobTask.TaskType.DB_TASK) {
                    dbTasks.add(task);
                    try {
                        if (task
                                .getTrigger()
                                .isNearTriggeringTime(5000) && register.registerTask(task)) {
                            count++;
                        }
                    } catch (AutoJobRegisterRefusedException e) {
                        e.printStackTrace();
                        task
                                .getTrigger()
                                .setIsPause(true);
                    }
                }
                /*=======================Finished======================<*/
                else {
                    try {
                        memoryTaskContainer.insert(task);
                        try {
                            if (task
                                    .getTrigger()
                                    .isNearTriggeringTime(5000) && register.registerTask(task)) {
                                count++;
                            }
                        } catch (AutoJobRegisterRefusedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        log.error("插入内存任务：{}时发生异常：{}", task.getReference(), e.getMessage());
                        throw e;
                    }
                }
            }
        }
        /*=================加载子任务=================>*/
        childTasks.forEach(child -> {
            MethodTask parentTask = parentTaskMap.get(child.getAnnotationId());
            if (parentTask == null) {
                throw new IllegalArgumentException(String.format("子任务：%s没有父任务", child.getAnnotationId() == null ? child.getId() : child.getAnnotationId()));
            }
            child
                    .getTrigger()
                    .setRepeatTimes(parentTask
                            .getTrigger()
                            .getRepeatTimes());
            if (child.getType() == AutoJobTask.TaskType.DB_TASK) {
                dbTasks.add(child);
            } else {
                try {
                    memoryTaskContainer.insert(child);
                } catch (Exception e) {
                    log.error("插入内存子任务：{}时发生异常：{}", child.getReference(), e.getMessage());
                    throw e;
                }
            }
        });
        /*=======================Finished======================<*/

        /*=================保存ONLY_SAVE任务=================>*/
        if (saveTasks.size() > 0) {
            List<AutoJobTaskEntity> entities = saveTasks
                    .stream()
                    .map(task -> EntityConvertor.task2TaskEntity(task, null))
                    .collect(Collectors.toList());
            int saveCount = AutoJobMapperHolder.TASK_ENTITY_MAPPER.insertList(entities);
            log.info("注册调度器成功保存{}个ONLY_SAVE任务", saveCount);
        }
        /*=======================Finished======================<*/

        /*=================保存DB任务=================>*/
        if (dbTasks.size() > 0) {
            List<AutoJobTriggerEntity> triggerEntities = new ArrayList<>();
            List<AutoJobTaskEntity> taskEntities = new ArrayList<>();
            dbTasks.forEach(task -> {
                //没有指定注解ID的任务放入上下文，这部分任务在退出时将会被删除
                if (task.getAnnotationId() == null || task.getAnnotationId() == -1) {
                    TaskRunningContext
                            .getAnnotationDBTask()
                            .put(task.getId(), task);
                }
                AutoJobTriggerEntity triggerEntity = EntityConvertor.trigger2TriggerEntity(task.getTrigger());
                triggerEntities.add(triggerEntity);
                AutoJobTaskEntity taskEntity = EntityConvertor.task2TaskEntity(task, triggerEntity.getId());
                taskEntities.add(taskEntity);
            });
            TransactionEntry insertTriggers = connection -> AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.insertList(triggerEntities);
            TransactionEntry insertTasks = connection -> AutoJobMapperHolder.TASK_ENTITY_MAPPER.insertList(taskEntities);
            if (AutoJobMapperHolder.TASK_ENTITY_MAPPER.doTransaction(new TransactionEntry[]{insertTriggers, insertTasks})) {
                log.info("注解调度器成功处理{}条DB任务", dbTasks.size());
            }
        }
        /*=======================Finished======================<*/
        return count;
    }


    @Override
    public int getSchedulerLevel() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void execute() {
        int count = 0;
        count += registerMethods();
        log.info("注解调度器成功注册{}个任务", count);
    }


}
