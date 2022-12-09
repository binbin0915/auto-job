package com.example.autojob.skeleton.model.task.method;

import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.executor.IMethodObjectFactory;
import com.example.autojob.skeleton.model.task.TaskExecutable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Method型任务，一个Method型任务代表Java中的一个方法，强烈建议任务方法不要重载，因为很可能会在参数注入时找不到目标方法
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
            if (type == TaskType.MEMORY_TASk) {
                AutoJobApplication
                        .getInstance()
                        .getMemoryTaskAPI()
                        .delete(id);
            } else {
                AutoJobApplication
                        .getInstance()
                        .getDbTaskAPI()
                        .delete(id);
            }
        }
        return taskExecutable;
    }
}
