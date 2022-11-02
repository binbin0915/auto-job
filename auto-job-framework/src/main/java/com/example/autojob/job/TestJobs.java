package com.example.autojob.job;

import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.util.thread.SyncHelper;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 用于测试的方法
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/23 9:19
 */
@Slf4j
public class TestJobs {
    private static final AutoJobLogHelper logger = new AutoJobLogHelper(log);
    private static final SecureRandom random = new SecureRandom();

    //@AutoJob(asType = AutoJobTask.TaskType.DB_TASK, id = 1, alias = "任务1", defaultStartTime = StartTime.THEN_HALF_HOUR, repeatTimes = 3, cycle = 10)
    private static int job1() {
        logger.info("job1 start");
        SyncHelper.sleepQuietly(random.nextInt(10), TimeUnit.SECONDS);
        logger.info("job1 end");
        return random.nextInt();
    }

    //@AutoJob(asType = AutoJobTask.TaskType.DB_TASK, id = 2, alias = "任务2", defaultStartTime = StartTime.NOW, repeatTimes = 3, cycle = 10)
    void job2() {
        logger.info("job2 start");
        SyncHelper.sleepQuietly(random.nextInt(20), TimeUnit.SECONDS);
        logger.info("job2 end");
    }

    public void child1(Boolean isRun) {
        if (isRun) {
            logger.info("child1 run");
        } else {
            logger.info("child1 can not run");
        }
    }

    public void child2() {
        logger.info("child2 start");
        SyncHelper.sleepQuietly(random.nextInt(5), TimeUnit.SECONDS);
        logger.info("child2 end");
    }

    public void error1() {
        int m = 3 / 0;
    }

    public void error2() {
        child1(null);
    }

    //@AutoJob(defaultStartTime = StartTime.NOW, id = 1, repeatTimes = -1, cycle = 10, unit = TimeUnit.SECONDS, asType = AutoJobTask.TaskType.DB_TASK)
    public void long1() {
        logger.info("long1 start");
        SyncHelper.sleepQuietly(1, TimeUnit.MINUTES);
        logger.info("long1 end");
    }

    //@AutoJob(defaultStartTime = StartTime.NOW, id = 1, repeatTimes = -1, cycle = 15, asType = AutoJobTask.TaskType.DB_TASK)
    public void long2() {
        logger.info("long2 start");
        SyncHelper.sleepQuietly(1, TimeUnit.MINUTES);
        logger.info("long2 end");
    }

    //@AutoJob(defaultStartTime = StartTime.NOW, id = 3, repeatTimes = -1, cycle = 10, unit = TimeUnit.SECONDS, asType = AutoJobTask.TaskType.DB_TASK)
    public void long3() {
        logger.info("long3 start");
        SyncHelper.sleepQuietly(1, TimeUnit.MINUTES);
        logger.info("long3 end");
    }

    //@AutoJob(defaultStartTime = StartTime.NOW, id = 4, repeatTimes = -1, cycle = 10, unit = TimeUnit.SECONDS, asType = AutoJobTask.TaskType.DB_TASK)
    public void long4() {
        logger.info("long4 start");
        SyncHelper.sleepQuietly(1, TimeUnit.MINUTES);
        logger.info("long24 end");
    }


}
