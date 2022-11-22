package com.example.spring.job;

import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.util.convert.StringUtils;
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

    //@AutoJob(id = 1, attributes = "{'hello autoJob'}", defaultStartTime = StartTime.NOW, repeatTimes = -1, cycle = 5, methodObjectFactory = SpringMethodObjectFactory.class)
    public void hello(String str) {
        AutoJobLogHelper logHelper = new AutoJobLogHelper();
        logHelper.setSlf4jProxy(log);
        logHelper.info(str);
    }

    //@AutoJob(id = 2, startTime = "2022-11-21 12:00:00", repeatTimes = -1, cycle = 10, asType = AutoJobTask.TaskType.DB_TASK, maximumExecutionTime = 5000)
    public void longTask() {
        AutoJobLogHelper logHelper = new AutoJobLogHelper();
        logHelper.setSlf4jProxy(log);
        logHelper.info("long task start");
        SyncHelper.sleepQuietly(10, TimeUnit.SECONDS);
        logHelper.info("long task end");
    }

    //@AutoJob(id = 3, schedulingStrategy = SchedulingStrategy.AS_CHILD_TASK)
    public void childTask() {
        AutoJobLogHelper logHelper = new AutoJobLogHelper();
        logHelper.setSlf4jProxy(log);
        logHelper.info("child task start");
        SyncHelper.sleepQuietly(3, TimeUnit.SECONDS);
        logHelper.info("child task end");
    }

    //@AutoJob(id = 4, alias = "获取随机字符串", cronExpression = "* * 0/5 17 * * ?", repeatTimes = -1, childTasksId = "3")
    public String getRandomString() {
        return StringUtils.getRandomStr(16);
    }

    //@AutoJob(id = 4, schedulingStrategy = SchedulingStrategy.ONLY_SAVE)
    public void error() {
        String str = null;
        str.length();
    }

    //@AutoJob(id = 5, defaultStartTime = StartTime.NOW, methodObjectFactory = SpringMethodObjectFactory.class)
    public void mapperTest() {
        //由于该Bean在AutoJob上下文初始化前注入，因此不能使用全局AutoJobLogHelper
        AutoJobLogHelper logHelper = new AutoJobLogHelper();
        logHelper.info("mapper = {}", mapper);
        if (mapper != null) {
            logHelper.info("count = {}", mapper.count());
        }
    }


}
