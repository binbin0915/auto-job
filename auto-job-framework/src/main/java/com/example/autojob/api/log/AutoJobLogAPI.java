package com.example.autojob.api.log;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;

import java.util.Date;
import java.util.List;

/**
 * 日志API接口
 *
 * @Author Huang Yongxiang
 * @Date 2022/11/02 14:51
 * @Email 1158055613@qq.com
 */
public interface AutoJobLogAPI {
    /**
     * 通过调度ID查询指定调度的日志，该接口返回的日志是实时的
     *
     * @param schedulingId 调度ID
     * @return java.util.List<com.example.autojob.logging.domain.AutoJobLog>
     * @author Huang Yongxiang
     * @date 2022/11/7 14:47
     */
    List<AutoJobLog> findLogsBySchedulingId(Long schedulingId);

    /**
     * 通过调度ID查询指定调度的运行日志，该接口返回的日志是实时的
     *
     * @param schedulingId 调度ID
     * @return java.util.List<com.example.autojob.logging.domain.AutoJobRunLog>
     * @author Huang Yongxiang
     * @date 2022/11/7 14:49
     */
    List<AutoJobRunLog> findRunLogsBySchedulingId(Long schedulingId);

    List<AutoJobLog> findLogsByTaskIdBetween(Long taskId, Date start, Date end);

    List<AutoJobRunLog> findRunLogsByTaskIdBetween(Long taskId, Date start, Date end);
}
