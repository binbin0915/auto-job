package com.example.spring.controller;

import com.example.autojob.api.log.AutoJobLogDBAPI;
import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.logging.domain.AutoJobSchedulingRecord;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.util.convert.MessageMaster;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试使用Rest接口查询日志
 *
 * @author Huang Yongxiang
 * @date 2022-12-27 15:10
 * @email 1158055613@qq.com
 */
@RestController
@RequestMapping("/auto_job_log")
public class AutoJobLogController {
    @GetMapping(value = "/page_scheduling_record/{taskId}", produces = "application/json;charset=UTF-8")
    public String pageSchedulingRecords(@PathVariable("taskId") Long taskId, @RequestParam(value = "PAGE_COUNT", required = false) Integer pageCount, @RequestParam(value = "SIZE", required = false) Integer size) {
        if (taskId == null) {
            return MessageMaster.DefaultMessage.EMPTY_PARAMS.toString();
        }
        if (pageCount == null || size == null) {
            return MessageMaster.getMessage(MessageMaster.Code.BAD_REQUEST, "请指定分页信息");
        }
        AutoJobLogDBAPI api = AutoJobApplication
                .getInstance()
                .getLogDbAPI();
        List<AutoJobSchedulingRecord> records = api.page(pageCount, size, taskId);
        int count = api.count(taskId);
        MessageMaster master = new MessageMaster();
        master.setFormatData(true);
        master.setCode(MessageMaster.Code.OK);
        master.setMessage("查找成功");
        master.setData(records);
        master.insertNewMessage("totalNum", count);
        return master.toString();
    }

    @GetMapping(value = "/find_log/{schedulingId}", produces = "application/json;charset=UTF-8")
    public String findLog(@PathVariable("schedulingId") Long schedulingId) {
        if (schedulingId == null) {
            return MessageMaster.DefaultMessage.EMPTY_PARAMS.toString();
        }
        AutoJobLogDBAPI api = AutoJobApplication
                .getInstance()
                .getLogDbAPI();
        List<AutoJobLog> logList = api.findLogsBySchedulingId(schedulingId);
        MessageMaster master = new MessageMaster();
        master.setFormatData(true);
        master.setCode(MessageMaster.Code.OK);
        master.setMessage("查找成功");
        master.setData(logList);
        master.insertNewMessage("totalNum", logList.size());
        return master.toString();
    }

    @GetMapping(value = "/find_run_log/{schedulingId}", produces = "application/json;charset=UTF-8")
    public String findRunLog(@PathVariable("schedulingId") Long schedulingId) {
        if (schedulingId == null) {
            return MessageMaster.DefaultMessage.EMPTY_PARAMS.toString();
        }
        AutoJobLogDBAPI api = AutoJobApplication
                .getInstance()
                .getLogDbAPI();
        List<AutoJobRunLog> logList = api.findRunLogsBySchedulingId(schedulingId);
        MessageMaster master = new MessageMaster();
        master.setFormatData(true);
        master.setCode(MessageMaster.Code.OK);
        master.setMessage("查找成功");
        master.setData(logList);
        master.insertNewMessage("totalNum", logList.size());
        return master.toString();
    }

}
