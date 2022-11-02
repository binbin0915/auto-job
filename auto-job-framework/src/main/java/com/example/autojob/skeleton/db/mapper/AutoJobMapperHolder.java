package com.example.autojob.skeleton.db.mapper;

/**
 * Mapper持有者
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/19 15:27
 */
public class AutoJobMapperHolder {
    public static final AutoJobTaskEntityMapper TASK_ENTITY_MAPPER = new AutoJobTaskEntityMapper();

    public static final AutoJobRunLogEntityMapper RUN_LOG_ENTITY_MAPPER = new AutoJobRunLogEntityMapper();

    public static final AutoJobLogEntityMapper LOG_ENTITY_MAPPER = new AutoJobLogEntityMapper();

    public static final AutoJobTriggerEntityMapper TRIGGER_ENTITY_MAPPER = new AutoJobTriggerEntityMapper();

    public static final AutoJobSchedulingRecordEntityMapper SCHEDULING_RECORD_ENTITY_MAPPER = new AutoJobSchedulingRecordEntityMapper();
}
