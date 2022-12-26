package com.example.spring.job;

import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.skeleton.annotation.AutoJob;
import com.example.autojob.skeleton.enumerate.SchedulingStrategy;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.util.thread.SyncHelper;
import com.example.spring.mapper.TestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 测试任务
 *
 * @Author Huang Yongxiang
 * @Date 2022/11/02 9:53
 * @Email 1158055613@qq.com
 */
@Slf4j
@Component
public class Jobs {
    @Autowired(required = false)
    private TestMapper mapper;

    //@AutoJob(attributes = "{'hello autoJob'}", defaultStartTime = StartTime.NOW, repeatTimes = -1, cycle = 10, methodObjectFactory = SpringMethodObjectFactory.class, id = 1)
    public void hello(String str) {
        AutoJobLogHelper logHelper = AutoJobLogHelper.getInstance();
        logHelper.setSlf4jProxy(log);
        logHelper.info(str);
    }

    @AutoJob(schedulingStrategy = SchedulingStrategy.ONLY_SAVE, asType = AutoJobTask.TaskType.DB_TASK)
    public void longTask() {
        AutoJobLogHelper logHelper = AutoJobLogHelper.getInstance();
        logHelper.setSlf4jProxy(log);
        logHelper.info("long task start");
        SyncHelper.sleepQuietly(10, TimeUnit.SECONDS);
        logHelper.info("long task end");
    }

    //@AutoJob(schedulingStrategy = SchedulingStrategy.AS_CHILD_TASK, id = 1, childTasksId = "2")
    public void childTask() {
        AutoJobLogHelper logHelper = AutoJobLogHelper.getInstance();
        logHelper.setSlf4jProxy(log);
        logHelper.info("child task start");
        SyncHelper.sleepQuietly(3, TimeUnit.SECONDS);
        logHelper.info("child task end");
    }

    //@AutoJob(alias = "获取随机字符串", cronExpression = "* * 0/5 17 * * ?", repeatTimes = -1, childTasksId = "3")
    //public String getRandomString() {
    //    return StringUtils.getRandomStr(16);
    //}

    //@AutoJob(id = 4, schedulingStrategy = SchedulingStrategy.ONLY_SAVE)
    public void error() {
        String str = null;
        str.length();
    }

    //@AutoJob(id = 5, defaultStartTime = StartTime.NOW, methodObjectFactory = SpringMethodObjectFactory.class)
    public void mapperTest() {
        AutoJobLogHelper logHelper = AutoJobLogHelper.getInstance();
        logHelper.info("mapper = {}", mapper);
        if (mapper != null) {
            logHelper.info("count = {}", mapper.count());
        }
    }


}
