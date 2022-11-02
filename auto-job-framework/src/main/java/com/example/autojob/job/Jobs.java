package com.example.autojob.job;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.util.id.IdGenerator;
import com.example.autojob.util.thread.SyncHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Description 测试方法类
 * @Auther Huang Yongxiang
 * @Date 2021/12/16 10:30
 */
@Slf4j
public class Jobs {
    private static final AutoJobLogHelper logger = AutoJobLogHelper.getInstance();

    //@AutoJob(startTime = "2021-01-01 00:00:00", id = 1, childTasksId = "2,3")
    private void helloAutoJob() {

    }

    //@AutoJob(cronExpression = "0/5 * * * * ?")
    //@AutoJob(defaultStartTime = StartTime.NOW, cycle = 5, repeatTimes = -1, id = 4)
    public void job1() {
        log.info("我是job1");
    }

    //@AutoJob(cronExpression = "0/5 * * * * ?", repeatTimes = -1)
    public void job2() {
        logger.info("我是job2");
        job1();
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        job3();

    }

    //@AutoJob(cronExpression = "0/5 * * * * ?")
    public void job3() {
        log.info("我是job3");
    }


    /**
     * 参数注入演示
     *
     * @return void
     * @author Huang Yongxiang
     * @date 2022/7/14 14:57
     */
    //@AutoJob(attributes = "{'hello AutoJob'}", cronExpression = "5/7 * * * * ?")
    //@AutoJob(attributes = "{'hello AutoJob'}", id = 2)
    public void print(String str) {
        AutoJobLogHelper
                .getInstance()
                .info("print方法成功执行:{}，这是一条任务日志", str);
    }

    /**
     * 错误方法重试演示
     *
     * @return void
     * @author Huang Yongxiang
     * @date 2022/7/14 14:58
     */
    //@AutoJob(startTime = "2022-3-24 15:00:00")
    //@AutoJob(id = 3)
    //@AutoJob(cronExpression = "5/7 * * * * ?")
    public void errorMethod() {
        log.info("error方法成功执行");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("这是一条debug日志");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.error("这是一条error日志");
        String s = null;
        if (s.equals("abc")) {
            log.info("遇见鬼了");
        }
    }

    //@AutoJob(cronExpression = "* * 7,19,21 * * ? *", cycle = 5, id = 6, alias = "错误任务")
    public void errorMethod1() {
        int m = 3 / 0;
    }

    //@AutoJob(schedulingStrategy = SchedulingStrategy.AS_CHILD_TASK, id = 4, alias = "子任务")
    public void childTask() {
        AutoJobLogHelper logger = new AutoJobLogHelper();
        logger.setSlf4jProxy(log);
        logger.info("child Task run");
    }

    //@AutoJob(id = 1, defaultStartTime = StartTime.NOW, repeatTimes = 3, cycle = 5, cycleUnit = TimeUnit.SECONDS, maximumExecutionTime = 60, alias = "长任务")
    public void longTask() {
        logger.info("long task start");
        SyncHelper.sleepQuietly(2, TimeUnit.MINUTES);
        logger.info("long task end");
    }


    //@AutoJob(defaultStartTime = StartTime.NOW, id = 1)
    public void pauseAndRun() {
        //SyncHelper.sleepQuietly(10, TimeUnit.SECONDS);
        //AutoJobTask latest = EntityConvert.taskEntity2Task(AutoJobMapperHolder.TASK_ENTITY_MAPPER.selectLatestAnnotationTask(2));
        //assert latest != null;
        //logger.info("准备停止任务{}", latest.getId());
        //if (AutoJobApplication.getInstance()
        //                      .getApiScheduler()
        //                      .pauseTask(latest.getId())) {
        //    logger.info("任务{}停止成功", latest.getId());
        //} else {
        //    logger.info("任务{}停止失败", latest.getId());
        //}
        //SyncHelper.sleepQuietly(10, TimeUnit.SECONDS);
        //logger.info("准备启动任务{}", latest.getId());
        //if (AutoJobApplication.getInstance()
        //                      .getApiScheduler()
        //                      .startUpPausedTask(latest.getId())) {
        //    logger.info("任务{}启动成功", latest.getId());
        //} else {
        //    logger.info("任务{}启动失败", latest.getId());
        //}

    }

    /**
     * 周期性任务演示
     *
     * @return void
     * @author Huang Yongxiang
     * @date 2022/7/14 14:58
     */
    //@AutoJob(attributes = "{'我爱你，心连心',12.5,12,true}", cycle = 2, cycleUnit = TimeUnit.MINUTES, id = 2, alias = "参数测试任务", repeatTimes = -1, defaultStartTime = StartTime.NOW)
    public void formatAttributes(String string, Double decimal, Integer num, Boolean flag) {
        AutoJobLogHelper logger = new AutoJobLogHelper();
        logger.setSlf4jProxy(log);
        logger.info("string={}", string);
        logger.info("decimal={}", decimal);
        logger.info("num={}", num);
        logger.info("flag={}", flag);
    }

    //@AutoJob(defaultStartTime = StartTime.NOW, repeatTimes = -1, cycle = 1, cycleUnit = TimeUnit.MINUTES, id = 3, alias = "结果返回任务", childTasksId = "4", asType = AutoJobTask.TaskType.DB_TASK)
    public AutoJobLog returnResult() {
        AutoJobLog log = new AutoJobLog();
        log
                .setId(IdGenerator.getNextIdAsLong())
                .setMessage("test");
        logger.info("结果返回任务已执行");
        return log;
    }


}
