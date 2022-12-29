SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for aj_auto_job
-- ----------------------------
DROP TABLE IF EXISTS `aj_auto_job`;
CREATE TABLE `aj_auto_job`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务别名',
  `annotation_id` bigint(0) NULL DEFAULT NULL COMMENT '注解ID',
  `method_class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务所在类路径',
  `method_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务名称',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务参数',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '预留字段，GLUE模式',
  `method_object_factory` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '方法运行类工厂路径',
  `script_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务内容，用于存放脚本任务的脚本',
  `script_path` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '脚本路径',
  `script_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本文件名',
  `script_cmd` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本命令行',
  `trigger_id` bigint(0) NULL DEFAULT NULL COMMENT '任务对应的触发器',
  `type` int(0) NOT NULL DEFAULT 0 COMMENT '任务类型，目前已占用的类型有：0-方法型任务 1-脚本型任务',
  `is_child_task` int(0) NULL DEFAULT NULL COMMENT '是否是子任务',
  `run_lock` int(0) NOT NULL DEFAULT 0 COMMENT '启动锁 0-未上锁 1-已上锁',
  `task_level` int(0) NULL DEFAULT -1 COMMENT '任务优先级',
  `version` bigint(0) NULL DEFAULT NULL COMMENT '版本号',
  `belong_to` int(0) NULL DEFAULT NULL COMMENT '预留字段，所属于',
  `status` int(0) NOT NULL DEFAULT 1 COMMENT '状态 0-已停用 1-已启用',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `del_flag` int(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) COMMENT '任务表';

-- ----------------------------
-- Table structure for aj_job_logs
-- ----------------------------
DROP TABLE IF EXISTS `aj_job_logs`;
CREATE TABLE `aj_job_logs`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `scheduling_id` bigint(0) NULL DEFAULT NULL COMMENT '调度id',
  `task_id` bigint(0) NOT NULL COMMENT '任务ID',
  `write_timestamp` bigint(0) NULL DEFAULT NULL COMMENT '录入时间戳',
  `write_time` datetime(0) NULL DEFAULT NULL COMMENT '写入时间',
  `log_level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志级别',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '记录信息',
  `del_flag` int(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) COMMENT '任务日志表';

-- ----------------------------
-- Table structure for aj_run_logs
-- ----------------------------
DROP TABLE IF EXISTS `aj_run_logs`;
CREATE TABLE `aj_run_logs`  (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `scheduling_id` bigint(0) NULL DEFAULT NULL COMMENT '调度id',
  `task_id` bigint(0) NOT NULL COMMENT '任务ID',
  `task_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务类型：MEMORY_TASK：内存型任务 DB_TASK：数据库任务',
  `run_status` int(0) NOT NULL COMMENT '1：运行成功 0：运行失败',
  `schedule_times` int(0) NULL DEFAULT 1 COMMENT '调度次数',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '信息',
  `result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务结果',
  `error_stack` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误堆栈',
  `write_timestamp` bigint(0) NULL DEFAULT NULL COMMENT '录入时间戳',
  `write_time` datetime(0) NOT NULL COMMENT '录入时间',
  `del_flag` int(0) NOT NULL DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`) USING BTREE
) COMMENT '任务调度日志表';

-- ----------------------------
-- Table structure for aj_scheduling_record
-- ----------------------------
DROP TABLE IF EXISTS `aj_scheduling_record`;
CREATE TABLE `aj_scheduling_record`  (
  `id` bigint NOT NULL COMMENT '主键',
  `write_timestamp` bigint NULL DEFAULT NULL COMMENT '写入时间戳',
  `scheduling_time` datetime(0) NULL DEFAULT NULL COMMENT '调度时间',
  `task_alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务别名',
  `task_id` bigint(0) NOT NULL COMMENT '任务Id',
  `is_success` int(0) NULL DEFAULT 1 COMMENT '是否执行成功 0-否 1-是',
  `is_run` int(0) NOT NULL DEFAULT 0 COMMENT '是否正在运行 1-是 0-否',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务结果 JSON序列化',
  `execution_time` bigint(0) NULL DEFAULT NULL COMMENT '执行时长:ms',
  `del_flag` int(0) NOT NULL DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`) USING BTREE
) COMMENT '调度记录表';

-- ----------------------------
-- Table structure for aj_trigger
-- ----------------------------
DROP TABLE IF EXISTS `aj_trigger`;
CREATE TABLE `aj_trigger`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cronlike表达式',
  `last_run_time` bigint(0) NULL DEFAULT NULL COMMENT '上次运行时长',
  `last_triggering_time` bigint(0) NULL DEFAULT NULL COMMENT '上次触发时间',
  `next_triggering_time` bigint(0) NULL DEFAULT NULL COMMENT '下次触发时间',
  `is_last_success` int(0) NULL DEFAULT NULL COMMENT '上次调度是否成功 0-否 1-是',
  `repeat_times` int(0) NULL DEFAULT 1 COMMENT '重复次数',
  `finished_times` int(0) NULL DEFAULT 0 COMMENT '已完成次数',
  `cycle` bigint(0) NULL DEFAULT NULL COMMENT '任务周期',
  `task_id` bigint(0) NULL DEFAULT NULL COMMENT '任务Id',
  `child_tasks_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子任务ID，多个逗号分割',
  `maximum_execution_time` bigint(0) NULL DEFAULT NULL COMMENT '最大运行时长',
  `is_run` int(0) NOT NULL DEFAULT 0 COMMENT '是否正在运行 0-否 1-是',
  `is_pause` int(0) NOT NULL DEFAULT 0 COMMENT '是否暂停调度 0-否 1-是',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `del_flag` int(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) COMMENT '触发器表';

SET FOREIGN_KEY_CHECKS = 1;
