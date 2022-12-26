package com.example.autojob.api.task;

import com.example.autojob.api.task.params.TaskEditParams;
import com.example.autojob.api.task.params.TriggerEditParams;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobTrigger;
import com.example.autojob.skeleton.framework.task.TaskRunningContext;

import java.util.List;

/**
 * 任务操作的API接口
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/14 13:52
 */
public interface AutoJobAPI {
    /**
     * 执行分页查询，列举出所有任务
     *
     * @param pageNum 页号
     * @param size    每页条数
     * @return java.util.List<com.example.autojob.skeleton.framework.task.AutoJobTask>
     * @author Huang Yongxiang
     * @date 2022/10/18 15:02
     */
    List<AutoJobTaskAttributes> page(Integer pageNum, Integer size);

    /**
     * 返回当前任务数目
     *
     * @return int
     * @author Huang Yongxiang
     * @date 2022/11/1 15:36
     */
    Integer count();

    /**
     * 注册一个任务
     *
     * @param taskAttributes 要注册的任务
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/10/14 14:02
     */
    Boolean registerTask(AutoJobTaskAttributes taskAttributes);

    /**
     * 立即执行一个任务，并且只执行一次
     *
     * @param taskAttributes 要立即执行的任务
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/10/14 14:03
     */
    Boolean runTaskNow(AutoJobTaskAttributes taskAttributes);

    /**
     * 通过任务ID查找到某个任务
     *
     * @param taskId 任务Id
     * @return com.example.autojob.skeleton.framework.task.AutoJobTask
     * @author Huang Yongxiang
     * @date 2022/10/14 14:03
     */
    AutoJobTaskAttributes find(Long taskId);

    /**
     * 对任务的调度信息进行编辑
     *
     * @param taskId            任务ID
     * @param triggerEditParams 调度器信息，存在属性将会作为修改项
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/10/14 14:04
     */
    Boolean editTrigger(Long taskId, TriggerEditParams triggerEditParams);

    /**
     * 绑定触发器到指定任务上，原有的触发器将会被覆盖
     *
     * @param taskId  任务ID
     * @param trigger 触发器
     * @return java.lang.Boolean
     * @author Huang Yongxiang
     * @date 2022/12/22 17:26
     */
    Boolean bindingTrigger(Long taskId, AutoJobTrigger trigger);

    /**
     * 对任务的基本信息进行编辑
     *
     * @param taskId         任务Id
     * @param taskEditParams 任务信息，存在的属性会作为修改项
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/10/17 14:33
     */
    Boolean editTask(Long taskId, TaskEditParams taskEditParams);

    /**
     * 停止一个任务的后续调度
     *
     * @param taskId 任务ID
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/10/14 14:05
     */
    Boolean pause(Long taskId);

    /**
     * 恢复一个任务的调度
     *
     * @param taskId 任务ID
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/10/14 14:05
     */
    Boolean unpause(Long taskId);

    /**
     * 删除一个任务，包含其调度器信息
     *
     * @param taskId 任务ID
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/10/14 14:06
     */
    Boolean delete(Long taskId);

    /**
     * 判断是否存在该任务
     *
     * @param taskId 任务ID
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/10/14 14:06
     */
    Boolean isExist(Long taskId);

    /**
     * 获取任务类型
     *
     * @param taskId 任务ID
     * @return AutoJobTask.TaskType
     * @author Huang Yongxiang
     * @date 2022/12/2 11:45
     */
    default AutoJobTask.TaskType getTaskType(Long taskId) {
        if (AutoJobApplication
                .getInstance()
                .getMemoryTaskContainer()
                .getById(taskId) != null) {
            return AutoJobTask.TaskType.MEMORY_TASk;
        }
        if (AutoJobMapperHolder.TASK_ENTITY_MAPPER
                .selectById(taskId)
                .getId() != null) {
            return AutoJobTask.TaskType.DB_TASK;
        }
        return null;
    }

    /**
     * 判断一个任务是否正在运行
     *
     * @param taskId 任务ID
     * @return boolean 任务不存在或不在运行返回false
     * @author Huang Yongxiang
     * @date 2022/10/14 14:08
     */
    default Boolean isRunning(Long taskId) {
        return TaskRunningContext
                .getRunningTask()
                .containsKey(taskId);
    }

    /**
     * 尝试停止一个正在运行的任务
     *
     * @param taskId 任务ID
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/10/14 14:08
     */
    default Boolean stopRunningTask(Long taskId) {
        return TaskRunningContext.stopRunningTask(taskId);
    }
}
