package com.example.autojob.skeleton.db.entity;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.logging.domain.AutoJobSchedulingRecord;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.task.AutoJobTrigger;
import com.example.autojob.skeleton.model.executor.IMethodObjectFactory;
import com.example.autojob.skeleton.model.interpreter.AutoJobAttributeContext;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.skeleton.model.task.script.ScriptTask;
import com.example.autojob.util.bean.ObjectUtil;
import com.example.autojob.util.convert.DateUtils;
import com.example.autojob.util.convert.DefaultValueUtil;
import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.id.IdGenerator;
import com.example.autojob.util.id.SystemClock;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 实体对象转化
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/20 17:38
 */
public class EntityConvertor {
    public static AutoJobTaskEntity task2TaskEntity(AutoJobTask task, Long triggerId) {
        AutoJobTaskEntity entity = new AutoJobTaskEntity();
        entity.setId(task.getId());
        entity.setMethodClassName(task.getMethodClass() == null ? "" : task
                .getMethodClass()
                .getName());
        if (task instanceof MethodTask) {
            entity.setType(0);
            MethodTask methodTask = (MethodTask) task;
            entity.setMethodName(task.getMethodName());
            entity.setMethodObjectFactory(methodTask
                    .getMethodObjectFactory()
                    .getClass()
                    .getName());
        } else if (task instanceof ScriptTask) {
            entity.setType(1);
            ScriptTask scriptTask = (ScriptTask) task;
            entity.setMethodName("scriptTask");
            if (!StringUtils.isEmpty(scriptTask.getScriptFilename())) {
                if (scriptTask
                        .getScriptFilename()
                        .lastIndexOf(".") > 0) {
                    entity.setScriptFileName(scriptTask.getScriptFilename());
                } else {
                    entity.setScriptFileName(scriptTask
                            .getScriptFilename()
                            .concat(scriptTask.getScriptFileSuffix()));
                }
            }
            entity.setScriptCmd(scriptTask.getCmd());
            entity.setScriptContent(scriptTask.getScriptContent());
            entity.setScriptPath(scriptTask.getScriptPath());
        }
        if (task.getAnnotationId() != null) {
            AutoJobTaskEntity latestVersion = AutoJobMapperHolder.TASK_ENTITY_MAPPER.selectLatestAnnotationTask(task.getAnnotationId());
            if (latestVersion != null && latestVersion.getVersion() != null) {
                entity.setVersion(latestVersion.getVersion() + 1);
            } else {
                entity.setVersion(0L);
            }
            entity.setAnnotationId(task.getAnnotationId());
        }
        entity.setBelongTo(task.getBelongTo());
        entity.setTriggerId(triggerId);
        entity.setRunLock(0);
        entity.setStatus(1);
        entity.setCreateTime(new Timestamp(SystemClock.now()));
        entity.setAlias(task.getAlias());
        entity.setIsChildTask(task.getIsChildTask() != null && task.getIsChildTask() ? 1 : 0);
        entity.setTaskLevel(task.getTaskLevel());
        entity.setParams(task.getParamsString());
        return entity;
    }

    public static AutoJobTask taskEntity2Task(AutoJobTaskEntity entity) {
        if (ObjectUtil.isNull(entity)) {
            return null;
        }
        AutoJobTriggerEntity triggerEntity = AutoJobMapperHolder.TRIGGER_ENTITY_MAPPER.selectOneByTaskId(entity.getId());
        if (entity.getType() == 0) {
            MethodTask task = new MethodTask();
            task.setId(entity.getId());
            task.setTrigger(triggerEntity2Trigger(triggerEntity));
            task.setTaskLevel(entity.getTaskLevel());
            task.setType(AutoJobTask.TaskType.DB_TASK);
            task.setMethodClassName(entity.getMethodClassName());
            task.setMethodClass(ObjectUtil.classPath2Class(entity.getMethodClassName()));
            task.setMethodName(entity.getMethodName());
            if (!StringUtils.isEmpty(entity.getMethodObjectFactory())) {
                task.setMethodObjectFactory((IMethodObjectFactory) ObjectUtil.getClassInstance(ObjectUtil.classPath2Class(entity.getMethodObjectFactory())));
            }
            task.setParamsString(entity.getParams());
            if (!StringUtils.isEmpty(entity.getParams())) {
                task.setParams(new AutoJobAttributeContext(task).getAttributeEntity());
            }
            task.setBelongTo(entity.getBelongTo());
            task.setAnnotationId(entity.getAnnotationId());
            task.setAlias(entity.getAlias());
            task.setIsAllowRegister(true);
            task.setIsFinished(false);
            task.setIsStart(false);
            return task;

        } else if (entity.getType() == 1) {
            ScriptTask task = new ScriptTask();
            task.setTrigger(triggerEntity2Trigger(triggerEntity));
            task.setTaskLevel(entity.getTaskLevel());
            task.setIsChildTask(entity.getIsChildTask() != null && entity.getIsChildTask() == 1);
            task.setMethodName(entity.getMethodName());
            task.setBelongTo(entity.getBelongTo());
            task.setAnnotationId(entity.getAnnotationId());
            task.setType(AutoJobTask.TaskType.DB_TASK);
            task.setIsAllowRegister(true);
            task.setAlias(entity.getAlias());
            task.setIsFinished(false);
            task.setIsStart(false);

            task.setCmd(entity.getScriptCmd());
            if (!StringUtils.isEmpty(entity.getScriptFileName())) {
                task.setScriptFilename(entity.getScriptFileName());
                task.setScriptFileSuffix(entity
                        .getScriptFileName()
                        .substring(entity
                                .getScriptFileName()
                                .lastIndexOf(".")));
                task.setScriptFile(true);
                task.setNeedWrite(false);
            } else {
                task.setScriptFile(false);
                task.setNeedWrite(false);
                task.setIsCmd(true);
            }
            task.setScriptContent(entity.getScriptContent());
            task.setScriptPath(entity.getScriptPath());
            return task;
        }

        return null;
    }

    public static AutoJobTrigger triggerEntity2Trigger(AutoJobTriggerEntity entity) {
        if (ObjectUtil.isNull(entity)) {
            return null;
        }
        AutoJobTrigger trigger = new AutoJobTrigger();
        if (!StringUtils.isEmpty(entity.getChildTasksId())) {
            trigger.setChildTask(Arrays
                    .stream(entity
                            .getChildTasksId()
                            .split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList()));
        }
        trigger.setIsPause(DefaultValueUtil.defaultValue(entity.getIsPause(), 0) == 1);
        trigger.setIsRunning(DefaultValueUtil.defaultValue(entity.getIsRun(), 0) == 1);
        trigger.setMaximumExecutionTime(entity.getMaximumExecutionTime());
        trigger.setLastTriggeringTime(entity.getLastTriggeringTime());
        trigger.setTriggeringTime(entity.getNextTriggeringTime());
        trigger.setCronExpression(entity.getCronExpression());
        trigger.setIsLastSuccess(entity.getIsLastSuccess() == 1);
        trigger.setCycle(entity.getCycle());
        trigger.setTaskId(entity.getTaskId());
        trigger.setLastRunTime(entity.getLastRunTime());
        trigger.setRepeatTimes(entity.getRepeatTimes());
        trigger.setFinishedTimes(entity.getFinishedTimes());
        return trigger;
    }

    public static AutoJobTriggerEntity trigger2TriggerEntity(AutoJobTrigger trigger) {
        AutoJobTriggerEntity entity = new AutoJobTriggerEntity();
        entity.setId(IdGenerator.getNextIdAsLong());
        if (trigger.hasChildTask()) {
            StringBuilder children = new StringBuilder();
            trigger
                    .getChildTask()
                    .forEach(id -> {
                        children
                                .append(id)
                                .append(",");
                    });
            children.deleteCharAt(children.length() - 1);
            entity.setChildTasksId(children.toString());
        }
        entity.setIsRun(DefaultValueUtil.defaultValue(trigger.getIsRunning(), false) ? 1 : 0);
        entity.setMaximumExecutionTime(trigger.getMaximumExecutionTime());
        entity.setCreateTime(new Timestamp(SystemClock.now()));
        entity.setCronExpression(trigger.getCronExpression());
        entity.setCycle(trigger.getCycle());
        entity.setIsPause(trigger.getIsPause() != null && trigger.getIsPause() ? 1 : 0);
        entity.setLastRunTime(trigger.getLastRunTime());
        entity.setFinishedTimes(trigger.getFinishedTimes());
        entity.setIsLastSuccess(trigger.getIsLastSuccess() != null && trigger.getIsLastSuccess() ? 1 : 0);
        entity.setNextTriggeringTime(trigger.getTriggeringTime());
        entity.setLastTriggeringTime(trigger.getLastTriggeringTime());
        entity.setRepeatTimes(trigger.getRepeatTimes());
        entity.setTaskId(trigger.getTaskId());
        return entity;
    }

    public static AutoJobLog logEntity2Log(AutoJobLogEntity entity) {
        if (ObjectUtil.isNull(entity)) {
            return null;
        }
        AutoJobLog log = new AutoJobLog();
        log.setId(entity.getId() == null ? -1L : entity.getId());
        log.setMessage(entity.getMessage());
        log.setLevel(entity.getLogLevel());
        log.setSchedulingId(entity.getSchedulingId());
        log.setInputTime(DateUtils.formatDateTime(new Date(entity
                .getWriteTime()
                .getTime())));
        log.setTaskId(entity.getTaskId());
        return log;
    }

    public static AutoJobLogEntity log2LogEntity(AutoJobLog log) {
        AutoJobLogEntity entity = new AutoJobLogEntity();
        entity.setId(IdGenerator.getNextIdAsLong());
        entity.setLogLevel(log.getLevel());
        entity.setTaskId(log.getTaskId());
        entity.setSchedulingId(log.getSchedulingId());
        entity.setMessage(log.getMessage());
        Timestamp timestamp = null;
        if (StringUtils.isEmpty(log.getInputTime())) {
            timestamp = new Timestamp(SystemClock.now());
        } else {
            try {
                timestamp = new Timestamp(DateUtils
                        .parseDate(log.getInputTime(), "yyyy-MM-dd HH:mm:ss")
                        .getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        entity.setWriteTime(timestamp);
        return entity;
    }

    public static AutoJobRunLog runLogEntity2RunLog(AutoJobRunLogEntity entity) {
        if (ObjectUtil.isNull(entity)) {
            return null;
        }
        AutoJobRunLog runLog = new AutoJobRunLog();
        runLog.setId(entity.getId());
        runLog.setErrorStack(entity.getErrorStack());
        runLog.setMessage(entity.getMessage());
        runLog.setSchedulingId(entity.getSchedulingId());
        runLog.setRunStatus(entity.getRunStatus());
        runLog.setTaskId(entity.getTaskId());
        runLog.setTaskType(entity.getTaskType());
        runLog.setWriteTime(DateUtils.formatDateTime(new Date(entity
                .getWriteTime()
                .getTime())));
        return runLog;
    }

    public static AutoJobRunLogEntity runLog2RunLogEntity(AutoJobRunLog runLog) {
        AutoJobRunLogEntity entity = new AutoJobRunLogEntity();
        entity.setId(IdGenerator.getNextIdAsLong());
        entity.setErrorStack(runLog.getErrorStack());
        entity.setTaskId(runLog.getTaskId());
        entity.setSchedulingId(runLog.getSchedulingId());
        entity.setTaskType(runLog.getTaskType());
        entity.setMessage(runLog.getMessage());
        entity.setResult(runLog.getRunResult());
        Timestamp timestamp = null;
        if (StringUtils.isEmpty(runLog.getWriteTime())) {
            timestamp = new Timestamp(SystemClock.now());
        } else {
            try {
                timestamp = new Timestamp(DateUtils
                        .parseDate(runLog.getWriteTime(), "yyyy-MM-dd HH:mm:ss")
                        .getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        entity.setWriteTime(timestamp);
        entity.setRunStatus(runLog.getRunStatus());
        return entity;
    }

    public static AutoJobSchedulingRecordEntity schedulingRecord2Entity(AutoJobSchedulingRecord schedulingRecord) {
        AutoJobSchedulingRecordEntity entity = new AutoJobSchedulingRecordEntity();
        entity.setId(schedulingRecord.getSchedulingId());
        entity.setExecutionTime(schedulingRecord.getExecutionTime());
        entity.setSchedulingTime(new Timestamp(schedulingRecord
                .getSchedulingTime()
                .getTime()));
        entity.setTaskAlias(schedulingRecord.getTaskAlias());
        if (!schedulingRecord.isRun()) {
            entity.setIsRun(0);
            entity.setIsSuccess(schedulingRecord.isSuccess() ? 1 : 0);
            entity.setResult(schedulingRecord.getResult());
        } else {
            entity.setIsRun(1);
        }
        entity.setTaskId(schedulingRecord.getTaskId());
        return entity;
    }

    public static AutoJobSchedulingRecord entity2schedulingRecord(AutoJobSchedulingRecordEntity entity) {
        AutoJobSchedulingRecord record = new AutoJobSchedulingRecord();
        record.setSchedulingId(entity.getId());
        record.setExecutionTime(entity.getExecutionTime());
        record.setSuccess(entity.getIsSuccess() != null && entity.getIsSuccess() == 1);
        record.setTaskAlias(entity.getTaskAlias());
        record.setResult(entity.getResult());
        record.setTaskId(entity.getTaskId());
        record.setSchedulingTime(new Date(entity
                .getSchedulingTime()
                .getTime()));
        return record;
    }
}
