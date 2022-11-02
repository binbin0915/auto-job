package com.example.autojob;

import com.example.autojob.job.IServerTest;
import com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder;
import com.example.autojob.skeleton.framework.network.handler.client.RPCClientProxy;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.util.servlet.InetUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Huang Yongxiang
 * @Date 2022/09/19 17:18
 */
public class ClientApplication {
    public static void main(String[] args) {
        new AutoJobLauncherBuilder(AutoJobMainApplication.class)
                .withAutoScanProcessor()
                .build()
                .run();
        RPCClientProxy<IServerTest> proxy = new RPCClientProxy<>(InetUtil.getLocalhostIp(), 7777,IServerTest.class);
        IServerTest serverTest = proxy.clientProxy();
        MethodTask task = new MethodTask();
        task.setAlias("测试别名1");
        MethodTask task1 = new MethodTask();
        task1.setAlias("测试别名2");
        System.out.println(serverTest.tasks(task));
        System.out.println(serverTest.hello("许莉"));
        List<MethodTask> tasks = new ArrayList<>();
        tasks.add(task);
        tasks.add(task1);
        System.out.println(serverTest.mapTasks(tasks));
        proxy.destroyProxy();
        System.out.println(serverTest.mapTasks(tasks));
    }
}
