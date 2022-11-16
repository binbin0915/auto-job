package com.example.autojob.api.log;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;

import java.util.Date;
import java.util.List;

/**
 * 内存日志API
 *
 * @Author Huang Yongxiang
 * @Date 2022/11/07 14:56
 * @Email 1158055613@qq.com
 */
public class AutoJobLogMemoryAPI implements AutoJobLogAPI {
    @Override
    public List<AutoJobLog> findLogsBySchedulingId(Long schedulingId) {
        return null;
    }

    @Override
    public List<AutoJobRunLog> findRunLogsBySchedulingId(Long schedulingId) {
        return null;
    }

    @Override
    public List<AutoJobLog> findLogsByTaskIdBetween(Long taskId, Date start, Date end) {
        return null;
    }

    @Override
    public List<AutoJobRunLog> findRunLogsByTaskIdBetween(Long taskId, Date start, Date end) {
        return null;
    }
}
