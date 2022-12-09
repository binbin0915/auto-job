package com.example.spring.controller;

import com.example.autojob.api.task.AutoJobAPI;
import com.example.autojob.api.task.AutoJobMethodTaskAttributes;
import com.example.autojob.api.task.AutoJobTaskAttributes;
import com.example.autojob.api.task.params.MethodTaskEditParams;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.util.bean.ObjectUtil;
import com.example.autojob.util.convert.MessageMaster;
import com.example.autojob.util.id.IdGenerator;
import com.example.autojob.util.id.SystemClock;
import com.example.spring.query.MethodTaskQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 测试使用Rest接口操作任务
 *
 * @Author Huang Yongxiang
 * @Date 2022/11/02 16:48
 * @Email 1158055613@qq.com
 */
@RestController
@RequestMapping("/auto_job")
public class TestController {

    //http://localhost:8080/auto_job/run_task_now
    @PostMapping(value = "/run_task_now", produces = "application/json;charset=UTF-8")
    public String runTaskNow(@RequestBody(required = false) MethodTaskQuery query) {
        if (query == null) {
            return MessageMaster.DefaultMessage.EMPTY_PARAMS.toString();
        }
        Class<?> methodClass = ObjectUtil.classPath2Class(query.getTaskClass());
        if (methodClass == null) {
            return MessageMaster.getMessage(MessageMaster.Code.BAD_REQUEST, query.getTaskClass() + "不存在");
        }
        MethodTask task = new AutoJobMethodTaskBuilder(methodClass, query.getTaskName())
                .addASimpleTrigger(SystemClock.now() + 5000, query.getRepeatTimes(), query.getCycle(), TimeUnit.MILLISECONDS)
                .setTaskAlias(query.getTaskName())
                .setTaskId(IdGenerator.getNextIdAsLong())
                .setParams(query.getAttributes())
                .build();
        if (!task.isExecutable()) {
            return MessageMaster.getMessage(MessageMaster.Code.BAD_REQUEST, "运行失败");
        }
        boolean flag = AutoJobApplication
                .getInstance()
                .getMemoryTaskAPI()
                .runTaskNow(new AutoJobMethodTaskAttributes(task));
        if (flag) {
            return MessageMaster.getMessage(MessageMaster.Code.OK, "运行成功");
        }
        return MessageMaster.getMessage(MessageMaster.Code.BAD_REQUEST, "运行失败");
    }

    @GetMapping(value = "/page_task", produces = "application/json;charset=UTF-8")
    public String page(@RequestParam(value = "pageNum", required = false) Integer pageNum, @RequestParam(value = "pageSize", required = false) Integer pageSize, @RequestParam(value = "type", required = false) Integer type) {
        if (pageNum == null || pageSize == null) {
            return MessageMaster.DefaultMessage.EMPTY_PARAMS.toString();
        }
        AutoJobAPI api;
        if (type == null || type == 1) {
            api = AutoJobApplication
                    .getInstance()
                    .getMemoryTaskAPI();
        } else {
            api = AutoJobApplication
                    .getInstance()
                    .getDbTaskAPI();
        }
        List<AutoJobTaskAttributes> taskAttributes = api.page(pageNum, pageSize);
        int count = api.count();
        MessageMaster master = new MessageMaster();
        master.setCode(MessageMaster.Code.OK);
        master.setMessage("查找成功");
        master.setData(taskAttributes);
        master.insertNewMessage("totalNum", count);
        return master.toString();
    }

    @PostMapping(value = "/edit_method_task", produces = "application/json;charset=UTF-8")
    public String editMethodTask(@RequestBody(required = false) MethodTaskEditParams params, @RequestParam(required = false, value = "TASK_ID") Long taskId) {
        if (params == null || ObjectUtil.isNull(params) || taskId == null) {
            return MessageMaster.DefaultMessage.EMPTY_PARAMS.toString();
        }
        AutoJobAPI autoJobAPI = AutoJobApplication
                .getInstance()
                .getMatchedAPI(taskId);
        if (autoJobAPI == null) {
            return MessageMaster.getMessage(MessageMaster.Code.BAD_REQUEST, "不存在任务：" + taskId);
        }
        if (autoJobAPI.editTask(taskId, params)) {
            return MessageMaster.getMessage(MessageMaster.Code.OK, "修改成功");
        }
        return MessageMaster.getMessage(MessageMaster.Code.ERROR, "修改失败");
    }


}
