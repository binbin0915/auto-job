package com.example.spring.job;

import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.skeleton.annotation.AutoJob;
import com.example.autojob.skeleton.enumerate.SchedulingStrategy;
import com.example.autojob.skeleton.enumerate.StartTime;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.thread.SyncHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 测试任务
 *
 * @Author Huang Yongxiang
 * @Date 2022/11/02 9:53
 * @Email 1158055613@qq.com
 */
@Slf4j
public class Jobs {
    private static final AutoJobLogHelper logHelper = new AutoJobLogHelper();

    static {
        logHelper.setSlf4jProxy(log);
    }

    @AutoJob(id = 1, attributes = "{'hello autoJob'}", defaultStartTime = StartTime.NOW, repeatTimes = -1, cycle = 5, methodObjectFactory = SpringMethodObjectFactory.class)
    public void hello(String str) {
        logHelper.info(str);
    }

    @AutoJob(id = 2, startTime = "2022-11-21 12:00:00", repeatTimes = -1, cycle = 10, asType = AutoJobTask.TaskType.DB_TASK, maximumExecutionTime = 5000)
    public void longTask() {
        logHelper.info("long task start");
        SyncHelper.sleepQuietly(10, TimeUnit.SECONDS);
        logHelper.info("long task end");
    }

    @AutoJob(id = 3, schedulingStrategy = SchedulingStrategy.AS_CHILD_TASK)
    public void childTask() {
        logHelper.info("child task start");
        SyncHelper.sleepQuietly(3, TimeUnit.SECONDS);
        logHelper.info("child task end");
    }

    @AutoJob(id = 4, alias = "获取随机字符串", cronExpression = "* * 0/5 17 * * ?", repeatTimes = -1, childTasksId = "3")
    public String getRandomString() {
        return StringUtils.getRandomStr(16);
    }

    @AutoJob(id = 4, schedulingStrategy = SchedulingStrategy.ONLY_SAVE)
    public void error() {
        String str = null;
        str.length();
    }


}
