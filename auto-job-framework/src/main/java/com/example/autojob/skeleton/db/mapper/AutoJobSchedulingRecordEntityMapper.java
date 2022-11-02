package com.example.autojob.skeleton.db.mapper;

import com.example.autojob.skeleton.db.entity.AutoJobSchedulingRecordEntity;

/**
 * 调度记录mapper
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/29 16:27
 */
public class AutoJobSchedulingRecordEntityMapper extends BaseMapper<AutoJobSchedulingRecordEntity> {
    private static final String ALL_COLUMNS = "id, scheduling_time, task_alias, task_id, is_success, is_run, result, execution_time, del_flag";

    private static final String TABLE_NAME = "aj_scheduling_record";

    public AutoJobSchedulingRecordEntityMapper() {
        super(AutoJobSchedulingRecordEntity.class);
    }

    public int updateResult(long id, boolean isSuccess, String result, long executionTime) {
        String sql = getUpdateExpression() + "set is_success = ?, result = ?,execution_time = ?, is_run = 0 where " + "del_flag = 0 and id = ?";
        return updateOne(sql, isSuccess ? 1 : 0, result, executionTime, id);
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
