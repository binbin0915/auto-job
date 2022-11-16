package com.example.spring.job;

import com.example.autojob.logging.model.producer.AutoJobLogHelper;
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

    //@AutoJob(id = 1, attributes = "{'hello autoJob'}", defaultStartTime = StartTime.NOW, repeatTimes = -1, cycle = 5)
    public void hello(String str) {
        logHelper.info(str);
    }

    //@AutoJob(id = 2, defaultStartTime = StartTime.NOW, repeatTimes = -1, cycle = 10, asType = AutoJobTask.TaskType.DB_TASK)
    public void longTask() {
        logHelper.info("long task start");
        SyncHelper.sleepQuietly(10, TimeUnit.SECONDS);
        logHelper.info("long task end");
    }

    //@AutoJob(id = 3, schedulingStrategy = SchedulingStrategy.AS_CHILD_TASK)
    public void childTask() {
        logHelper.info("child task start");
        SyncHelper.sleepQuietly(3, TimeUnit.SECONDS);
        logHelper.info("child task end");
    }

    //@AutoJob(id = 4, defaultStartTime = StartTime.NOW)
    public void error() {
        String str = null;
        str.length();
    }
}
