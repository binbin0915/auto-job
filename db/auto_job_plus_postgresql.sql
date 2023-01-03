CREATE TABLE "aj_auto_job"
(
    "id"                    int8 NOT NULL,
    "alias"                 varchar(255)  DEFAULT NULL,
    "annotation_id"         int8          DEFAULT NULL,
    "method_class_name"     varchar(255)  DEFAULT NULL,
    "method_name"           varchar(255)  DEFAULT NULL,
    "params"                text          DEFAULT NULL,
    "content"               text          DEFAULT NULL,
    "method_object_factory" varchar(255)  DEFAULT NULL,
    "script_content"        text          DEFAULT NULL,
    "script_path"           text          DEFAULT NULL,
    "script_file_name"      varchar(255)  DEFAULT NULL,
    "script_cmd"            varchar(255)  DEFAULT NULL,
    "trigger_id"            int8          DEFAULT NULL,
    "type"                  int4 NOT NULL DEFAULT 0,
    "is_child_task"         int4          DEFAULT NULL,
    "run_lock"              int4 NOT NULL DEFAULT 0,
    "task_level"            int4          DEFAULT -1,
    "version"               int8          DEFAULT NULL,
    "belong_to"             int8          DEFAULT NULL,
    "status"                int4 NOT NULL DEFAULT 1,
    "create_time"           timestamp(0)  DEFAULT NULL,
    "del_flag"              int4          DEFAULT 0,
    PRIMARY KEY ("id")
)
    WITHOUT OIDS;
COMMENT ON TABLE "aj_auto_job" IS '任务表';
COMMENT ON COLUMN "aj_auto_job"."id" IS '主键ID';
COMMENT ON COLUMN "aj_auto_job"."alias" IS '任务别名';
COMMENT ON COLUMN "aj_auto_job"."annotation_id" IS '注解ID';
COMMENT ON COLUMN "aj_auto_job"."method_class_name" IS '任务所在类路径';
COMMENT ON COLUMN "aj_auto_job"."method_name" IS '任务名称';
COMMENT ON COLUMN "aj_auto_job"."params" IS '任务参数';
COMMENT ON COLUMN "aj_auto_job"."content" IS '预留字段，GLUE模式';
COMMENT ON COLUMN "aj_auto_job"."method_object_factory" IS '方法运行类工厂路径';
COMMENT ON COLUMN "aj_auto_job"."script_content" IS '任务内容，用于存放脚本任务的脚本';
COMMENT ON COLUMN "aj_auto_job"."script_path" IS '脚本路径';
COMMENT ON COLUMN "aj_auto_job"."script_file_name" IS '脚本文件名';
COMMENT ON COLUMN "aj_auto_job"."script_cmd" IS '脚本命令行';
COMMENT ON COLUMN "aj_auto_job"."trigger_id" IS '任务对应的触发器';
COMMENT ON COLUMN "aj_auto_job"."type" IS '任务类型，目前已占用的类型有：0-方法型任务 1-脚本型任务';
COMMENT ON COLUMN "aj_auto_job"."is_child_task" IS '是否是子任务';
COMMENT ON COLUMN "aj_auto_job"."run_lock" IS '启动锁 0-未上锁 1-已上锁';
COMMENT ON COLUMN "aj_auto_job"."task_level" IS '任务优先级';
COMMENT ON COLUMN "aj_auto_job"."version" IS '版本号';
COMMENT ON COLUMN "aj_auto_job"."belong_to" IS '预留字段，所属于';
COMMENT ON COLUMN "aj_auto_job"."status" IS '状态 0-已停用 1-已启用';

CREATE TABLE "aj_job_logs"
(
    "id"              int8 NOT NULL,
    "scheduling_id"   int8          DEFAULT NULL,
    "task_id"         int8 NOT NULL,
    "write_timestamp" int8          DEFAULT NULL,
    "write_time"      timestamp(0)  DEFAULT NULL,
    "log_level"       varchar(10)   DEFAULT NULL,
    "message"         text          DEFAULT NULL,
    "del_flag"        int4 NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
)
    WITHOUT OIDS;
COMMENT ON TABLE "aj_job_logs" IS '任务日志表';
COMMENT ON COLUMN "aj_job_logs"."id" IS '主键ID';
COMMENT ON COLUMN "aj_job_logs"."scheduling_id" IS '调度id';
COMMENT ON COLUMN "aj_job_logs"."task_id" IS '任务ID';
COMMENT ON COLUMN "aj_job_logs"."write_timestamp" IS '录入时间戳';
COMMENT ON COLUMN "aj_job_logs"."write_time" IS '写入时间';
COMMENT ON COLUMN "aj_job_logs"."log_level" IS '日志级别';
COMMENT ON COLUMN "aj_job_logs"."message" IS '记录信息';

CREATE TABLE "aj_run_logs"
(
    "id"              int8         NOT NULL,
    "scheduling_id"   int8                  DEFAULT NULL,
    "task_id"         int8         NOT NULL,
    "task_type"       varchar(10)  NOT NULL,
    "run_status"      int4         NOT NULL,
    "schedule_times"  int4                  DEFAULT 1,
    "message"         text                  DEFAULT NULL,
    "result"          varchar(255)          DEFAULT NULL,
    "error_stack"     text                  DEFAULT NULL,
    "write_timestamp" int8                  DEFAULT NULL,
    "write_time"      timestamp(0) NOT NULL,
    "del_flag"        int4         NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
)
    WITHOUT OIDS;
COMMENT ON TABLE "aj_run_logs" IS '任务调度日志表';
COMMENT ON COLUMN "aj_run_logs"."id" IS '主键';
COMMENT ON COLUMN "aj_run_logs"."scheduling_id" IS '调度id';
COMMENT ON COLUMN "aj_run_logs"."task_id" IS '任务ID';
COMMENT ON COLUMN "aj_run_logs"."task_type" IS '任务类型：MEMORY_TASK：内存型任务 DB_TASK：数据库任务';
COMMENT ON COLUMN "aj_run_logs"."run_status" IS '1：运行成功 0：运行失败';
COMMENT ON COLUMN "aj_run_logs"."schedule_times" IS '调度次数';
COMMENT ON COLUMN "aj_run_logs"."message" IS '信息';
COMMENT ON COLUMN "aj_run_logs"."result" IS '任务结果';
COMMENT ON COLUMN "aj_run_logs"."error_stack" IS '错误堆栈';
COMMENT ON COLUMN "aj_run_logs"."write_timestamp" IS '录入时间戳';
COMMENT ON COLUMN "aj_run_logs"."write_time" IS '录入时间';
COMMENT ON COLUMN "aj_run_logs"."del_flag" IS '删除标识';

CREATE TABLE "aj_scheduling_record"
(
    "id"              int8 NOT NULL,
    "write_timestamp" int8          DEFAULT NULL,
    "scheduling_time" timestamp(0)  DEFAULT NULL,
    "task_alias"      varchar(255)  DEFAULT NULL,
    "task_id"         int8 NOT NULL,
    "is_success"      int4          DEFAULT 1,
    "is_run"          int4 NOT NULL DEFAULT 0,
    "is_retry"        int4 NOT NULL DEFAULT 0,
    "result"          text          DEFAULT NULL,
    "execution_time"  int8          DEFAULT NULL,
    "del_flag"        int4 NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
)
    WITHOUT OIDS;
COMMENT ON TABLE "aj_scheduling_record" IS '调度记录表';
COMMENT ON COLUMN "aj_scheduling_record"."id" IS '主键';
COMMENT ON COLUMN "aj_scheduling_record"."write_timestamp" IS '写入时间戳';
COMMENT ON COLUMN "aj_scheduling_record"."scheduling_time" IS '调度时间';
COMMENT ON COLUMN "aj_scheduling_record"."task_alias" IS '任务别名';
COMMENT ON COLUMN "aj_scheduling_record"."task_id" IS '任务Id';
COMMENT ON COLUMN "aj_scheduling_record"."is_success" IS '是否执行成功 0-否 1-是';
COMMENT ON COLUMN "aj_scheduling_record"."is_run" IS '是否正在运行 1-是 0-否';
COMMENT ON COLUMN "aj_scheduling_record"."is_retry" IS '是否是重试调度 0-否 1-是';
COMMENT ON COLUMN "aj_scheduling_record"."result" IS '任务结果 JSON序列化';
COMMENT ON COLUMN "aj_scheduling_record"."execution_time" IS '执行时长:ms';
COMMENT ON COLUMN "aj_scheduling_record"."del_flag" IS '删除标识';

CREATE TABLE "aj_trigger"
(
    "id"                     int8 NOT NULL,
    "cron_expression"        varchar(255)  DEFAULT NULL,
    "last_run_time"          int8          DEFAULT NULL,
    "last_triggering_time"   int8          DEFAULT NULL,
    "next_triggering_time"   int8          DEFAULT NULL,
    "is_last_success"        int4          DEFAULT NULL,
    "repeat_times"           int4          DEFAULT 1,
    "finished_times"         int4          DEFAULT 0,
    "cycle"                  int8          DEFAULT NULL,
    "task_id"                int8          DEFAULT NULL,
    "child_tasks_id"         varchar(255)  DEFAULT NULL,
    "maximum_execution_time" int8          DEFAULT NULL,
    "is_run"                 int4 NOT NULL DEFAULT 0,
    "is_pause"               int4 NOT NULL DEFAULT 0,
    "create_time"            timestamp(0)  DEFAULT NULL,
    "del_flag"               int4          DEFAULT 0,
    PRIMARY KEY ("id")
)
    WITHOUT OIDS;
COMMENT ON TABLE "aj_trigger" IS '触发器表';
COMMENT ON COLUMN "aj_trigger"."id" IS '主键ID';
COMMENT ON COLUMN "aj_trigger"."cron_expression" IS 'cronlike表达式';
COMMENT ON COLUMN "aj_trigger"."last_run_time" IS '上次运行时长';
COMMENT ON COLUMN "aj_trigger"."last_triggering_time" IS '上次触发时间';
COMMENT ON COLUMN "aj_trigger"."next_triggering_time" IS '下次触发时间';
COMMENT ON COLUMN "aj_trigger"."is_last_success" IS '上次调度是否成功 0-否 1-是';
COMMENT ON COLUMN "aj_trigger"."repeat_times" IS '重复次数';
COMMENT ON COLUMN "aj_trigger"."finished_times" IS '已完成次数';
COMMENT ON COLUMN "aj_trigger"."cycle" IS '任务周期';
COMMENT ON COLUMN "aj_trigger"."task_id" IS '任务Id';
COMMENT ON COLUMN "aj_trigger"."child_tasks_id" IS '子任务ID，多个逗号分割';
COMMENT ON COLUMN "aj_trigger"."maximum_execution_time" IS '最大运行时长';
COMMENT ON COLUMN "aj_trigger"."is_run" IS '是否正在运行 0-否 1-是';
COMMENT ON COLUMN "aj_trigger"."is_pause" IS '是否暂停调度 0-否 1-是';
COMMENT ON COLUMN "aj_trigger"."create_time" IS '创建时间';

