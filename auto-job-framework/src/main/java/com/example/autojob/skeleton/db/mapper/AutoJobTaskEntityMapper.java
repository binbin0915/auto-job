package com.example.autojob.skeleton.db.mapper;

import com.example.autojob.api.task.params.MethodTaskEditParams;
import com.example.autojob.api.task.params.ScriptTaskEditParams;
import com.example.autojob.api.task.params.TaskEditParams;
import com.example.autojob.skeleton.db.entity.AutoJobTaskEntity;
import com.example.autojob.skeleton.db.entity.AutoJobTriggerEntity;
import com.example.autojob.skeleton.model.builder.AttributesBuilder;
import com.example.autojob.util.bean.ObjectUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 任务持久层对象mapper
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/17 16:46
 */
public class AutoJobTaskEntityMapper extends BaseMapper<AutoJobTaskEntity> {


    public AutoJobTaskEntityMapper() {
        super(AutoJobTaskEntity.class);
    }

    /**
     * 所有列
     */
    public static final String ALL_COLUMNS = "id, alias, annotation_id, method_class_name, method_name,params, content, method_object_factory, script_content, script_path, script_file_name, script_cmd, trigger_id, type, is_child_task, run_lock, task_level, version, belong_to, status, create_time, del_flag";
    /**
     * 表名
     */
    public static final String TABLE_NAME = "aj_auto_job";


    /**
     * 判断一个任务是否被上锁
     *
     * @param taskId 任务id
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/8/18 11:05
     */
    public boolean isLock(long taskId) {
        AutoJobTaskEntity taskEntity = selectById(taskId);
        if (taskEntity == null) {
            return false;
        }
        return taskEntity.getRunLock() == 1;
    }

    /**
     * 尝试对任务进行上锁
     *
     * @param taskId 要上锁的任务Id
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/8/18 11:04
     */
    public boolean lock(long taskId) {
        AutoJobTaskEntity entity = selectById(taskId);
        AutoJobTriggerEntity triggerEntity = AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.selectOneByTaskId(taskId);

        if (entity == null || ObjectUtil.isNull(entity)) {
            return true;
        }
        if (entity.getRunLock() == 1 || (triggerEntity != null && triggerEntity.getIsPause() == 1) || entity.getStatus() == 0) {
            return false;
        }
        int count = updateOne(getUpdateExpression() + " set run_lock = 1 where del_flag = 0 and id = ?", taskId);
        return count == 1;
    }

    public boolean unLock(long taskId) {
        return updateOne(getUpdateExpression() + " set run_lock = 0 where del_flag = 0 and id = ?", taskId) == 1;
    }

    /**
     * 查询未来时间内会执行的任务
     *
     * @param nearTime 未来时间段
     * @param unit     时间单位
     * @return java.util.List<com.example.autojob.skeleton.db.entity.AutoJobTaskEntity>
     * @author Huang Yongxiang
     * @date 2022/8/26 9:50
     */
    public List<AutoJobTaskEntity> selectNearTask(long nearTime, TimeUnit unit) {
        String sql = getSelectExpression() + " where (id in (SELECT task_id FROM `aj_trigger` where next_triggering_time >= ? and next_triggering_time <= ? and del_flag = 0 and is_pause = 0)) and del_flag = 0 and status = 1";
        return queryList(sql, System.currentTimeMillis(), System.currentTimeMillis() + unit.toMillis(nearTime));
    }

    @Override
    public List<AutoJobTaskEntity> page(int pageNum, int size) {
        int skip = (pageNum - 1) * size;
        String sql = getSelectExpression() + String.format(" where id in ( SELECT max( id ) FROM aj_auto_job WHERE del_flag = 0 GROUP BY annotation_id ) limit %d, %d", skip, size);
        return queryList(sql);
    }

    public int count() {
        String sql = "SELECT count(*) FROM aj_auto_job WHERE id in (SELECT max( id ) FROM aj_auto_job WHERE del_flag = 0 GROUP BY annotation_id)";
        return conditionalCount(sql);
    }


    /**
     * 通过id查询子任务
     *
     * @param id 任务id或版本id
     * @return com.example.autojob.skeleton.db.entity.AutoJobTaskEntity
     * @author Huang Yongxiang
     * @date 2022/8/26 9:50
     */
    public AutoJobTaskEntity selectChildTask(long id) {
        String condition = " where is_child_task = 1 and del_flag = 0 and (id = ? or annotation_id = ?) and status = 1";
        return queryOne(getSelectExpression() + condition, id, id);
    }

    /**
     * 批量查询子任务
     *
     * @param ids 版本ID或任务ID
     * @return java.util.List<com.example.autojob.skeleton.db.entity.AutoJobTaskEntity>
     * @author Huang Yongxiang
     * @date 2022/12/27 10:48
     */
    public List<AutoJobTaskEntity> selectChildTasks(List<Long> ids) {
        String sql = getSelectExpression() + String.format(" where id in ( select max(id) from %s where (id in (%s) or annotation_id in (%s)) and is_child_task = 1 and del_flag = 0 and status = 1)", getTableName(), idRepeat(ids), idRepeat(ids));
        return queryList(sql);
    }

    /**
     * 查找最新版本的注解任务
     *
     * @param annotationId 注解上给定的id
     * @return com.example.autojob.skeleton.db.entity.AutoJobTaskEntity
     * @author Huang Yongxiang
     * @date 2022/8/26 9:51
     */
    public AutoJobTaskEntity selectLatestAnnotationTask(long annotationId) {
        String sql = getSelectExpression() + " where id = (select max(id) from " + getTableName() + " where annotation_id = ? and del_flag = 0 and status = 1)";
        return queryOne(sql, annotationId);
    }

    /**
     * 通过id删除任务
     *
     * @param ids 注解id
     * @return int
     * @author Huang Yongxiang
     * @date 2022/8/26 9:36
     */
    public int deleteTasksById(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return 0;
        }
        String condition = " where id in (" + idRepeat(ids) + ")";
        return updateOne(getDeleteExpression() + condition);
    }

    public int deleteTasksByIds(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return 0;
        }
        String condition = " where id in (" + idRepeat(ids) + ")";
        return updateOne(getDeleteExpression() + condition);
    }

    public boolean bindingTrigger(long triggerId, long taskId) {
        String sql = getUpdateExpression() + " set trigger_id = ? where id = ? and del_flag = 0";
        return updateOne(sql, triggerId, taskId) == 1;
    }

    /**
     * 查询被暂停的任务
     *
     * @param taskId 任务id
     * @return com.example.autojob.skeleton.db.entity.AutoJobTaskEntity
     * @author Huang Yongxiang
     * @date 2022/8/26 9:51
     */
    public AutoJobTaskEntity selectPausedTaskById(long taskId) {
        String condition = " where exists (select id from " + AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.getTableName() + " where task_id = ? and del_flag = 0 and is_pause = 1) and del_flag = 0 and status = 1 and id = ?";
        return queryOne(getSelectExpression() + condition, taskId, taskId);
    }

    public int updateById(TaskEditParams editParams, long taskId) {
        if (editParams == null) {
            return -1;
        }
        AutoJobTaskEntity entity = new AutoJobTaskEntity();
        entity.setAlias(editParams.getAlias());
        entity.setBelongTo(editParams.getBelongTo());
        entity.setTaskLevel(editParams.getTaskLevel());
        if (editParams instanceof MethodTaskEditParams) {
            MethodTaskEditParams methodTaskEditParams = (MethodTaskEditParams) editParams;
            entity.setMethodObjectFactory(methodTaskEditParams.getMethodObjectFactory());
            entity.setParams(methodTaskEditParams.getParamsString());
        }
        if (editParams instanceof ScriptTaskEditParams && ((ScriptTaskEditParams) editParams).getAttributes() != null) {
            AttributesBuilder builder = new AttributesBuilder();
            ((ScriptTaskEditParams) editParams)
                    .getAttributes()
                    .forEach(param -> {
                        builder.addParams(AttributesBuilder.AttributesType.STRING, param);
                    });
            entity.setParams(builder.getAttributesString());
        }
        return updateEntity(entity, "id = ?", taskId);
    }


    @Override
    public String getAllColumns() {
        return ALL_COLUMNS;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

}
