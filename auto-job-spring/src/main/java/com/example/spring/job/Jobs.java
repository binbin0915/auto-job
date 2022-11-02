package com.example.spring.job;

import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.skeleton.annotation.AutoJob;
import com.example.autojob.skeleton.enumerate.StartTime;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试任务
 *
 * @Author Huang Yongxiang
 * @Date 2022/11/02 9:53
 * @Email 1158055613@qq.com
 */
@Slf4j
public class Jobs {

    @AutoJob(id = 1, attributes = "{'hello autoJob'}", defaultStartTime = StartTime.NOW, repeatTimes = -1, cycle = 5)
    public void hello(String str) {
        AutoJobLogHelper logHelper = new AutoJobLogHelper();
        logHelper.setSlf4jProxy(log);
        logHelper.info(str);
    }
}
