package com.example.autojob;

import com.example.autojob.api.AutoJobAPI;
import com.example.autojob.api.AutoJobMethodTaskAttributes;
import com.example.autojob.api.AutoJobTaskAttributes;
import com.example.autojob.api.AutoJobTriggerAttributes;
import com.example.autojob.job.Jobs;
import com.example.autojob.skeleton.model.builder.AutoJobMethodTaskBuilder;
import com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder;
import com.example.autojob.skeleton.framework.network.handler.client.RPCClientProxy;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.util.id.SystemClock;
import com.example.autojob.util.servlet.InetUtil;
import com.example.autojob.util.thread.SyncHelper;

import java.util.concurrent.TimeUnit;

/**
 * @Author Huang Yongxiang
 * @Date 2022/10/25 13:58
 */
public class AutoJobRemoteAPIApplication {
    public static void main(String[] args) {
        new AutoJobLauncherBuilder(AutoJobMainApplication.class)
                .withAutoScanProcessor()
                .build()
                .run();
        RPCClientProxy<AutoJobAPI> proxy = new RPCClientProxy<>(InetUtil.getLocalhostIp(), 7777, AutoJobAPI.class);
        AutoJobAPI autoJobAPI = proxy.clientProxy();
        MethodTask task = new AutoJobMethodTaskBuilder(Jobs.class, "job2")
                .addASimpleTrigger(SystemClock.now() + 10000, -1, 5, TimeUnit.SECONDS)
                .setTaskAlias("远程任务")
                .setTaskId(1L)
                .build();
        System.out.println("注册任务：" + autoJobAPI.registerTask(new AutoJobMethodTaskAttributes(task)));

        SyncHelper.sleepQuietly(15, TimeUnit.SECONDS);
        AutoJobTriggerAttributes attributes = new AutoJobTriggerAttributes();
        attributes.setCycle(10000L);
        System.out.println(autoJobAPI.find(1L));
        SyncHelper.sleepQuietly(3, TimeUnit.SECONDS);
        System.out.println("修改任务：" + autoJobAPI.editTrigger(1L, attributes));

        AutoJobTaskAttributes taskAttributes = new AutoJobMethodTaskAttributes(new MethodTask());
        taskAttributes.setAlias("修改后别称");
        System.out.println("修改任务别称：" + autoJobAPI.editTask(1L, taskAttributes));
        System.out.println(autoJobAPI.find(1L));

        SyncHelper.sleepQuietly(3, TimeUnit.SECONDS);
        System.out.println(autoJobAPI.find(1L));
        System.out.println("停止任务：" + autoJobAPI.stopRunningTask(1L));
        proxy.destroyProxy();
    }
}
