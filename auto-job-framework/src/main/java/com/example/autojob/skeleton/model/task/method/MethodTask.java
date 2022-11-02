package com.example.autojob.skeleton.model.task.method;

import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.executor.IMethodObjectFactory;
import com.example.autojob.skeleton.model.task.TaskExecutable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Method型任务
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/02 17:43
 */
@Getter
@Setter
@Slf4j
public class MethodTask extends AutoJobTask {
    /**
     * 运行方法所在的类名
     */
    private String methodClassName;
    /**
     * 创建方法所在的运行对象工厂
     */
    private IMethodObjectFactory methodObjectFactory;
    /**
     * 可执行对象
     */
    private TaskExecutable taskExecutable;


    @Override
    public TaskExecutable getExecutable() {
        if (taskExecutable != null) {
            //log.info("返回任务：{}的已存在可执行对象：{}", id, taskExecutable.getClass().getName());
            return taskExecutable;
        }
        try {
            taskExecutable = new MethodTaskExecutable(this);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("任务：{}创建可执行对象发生异常：{}", this, e.getMessage());
        }
        return taskExecutable;
    }
}
