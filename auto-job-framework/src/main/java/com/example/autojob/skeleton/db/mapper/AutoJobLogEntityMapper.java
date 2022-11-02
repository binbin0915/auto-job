package com.example.autojob.skeleton.db.mapper;

import com.example.autojob.skeleton.db.entity.AutoJobLogEntity;

import java.util.Date;
import java.util.List;

/**
 * 任务日志持久层对象mapper
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/17 16:47
 */
public class AutoJobLogEntityMapper extends BaseMapper<AutoJobLogEntity> {
    /**
     * 所有列
     */
    public static final String ALL_COLUMNS = "id, scheduling_id, task_id, write_time, log_level, message, del_flag";
    /**
     * 表名
     */
    public static final String TABLE_NAME = "aj_job_logs";


    public AutoJobLogEntityMapper() {
        super(AutoJobLogEntity.class);
    }

    public List<AutoJobLogEntity> selectLogByTaskId(long taskId) {
        String condition = " where del_flag = 0 and task_id = ?";
        return queryList(getSelectExpression() + condition, taskId);
    }

    public int deleteByTaskId(long taskId) {
        String condition = " where del_flag = 0";
        return updateOne(getLogicDeleteExpression() + condition);
    }

    public List<AutoJobLogEntity> selectByTaskIdBetween(Date startTime, Date endTime, long taskId) {
        String condition = " where task_id = ? AND del_flag = 0 AND UNIX_TIMESTAMP( write_time ) >= ? AND UNIX_TIMESTAMP( write_time ) <= ?";
        return queryList( getSelectExpression() + condition, taskId, startTime.getTime(), endTime.getTime());
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
