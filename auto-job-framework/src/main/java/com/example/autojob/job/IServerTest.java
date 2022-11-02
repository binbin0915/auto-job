package com.example.autojob.job;

import com.example.autojob.skeleton.annotation.AutoJobRPCClient;
import com.example.autojob.skeleton.model.task.method.MethodTask;

import java.util.List;
import java.util.Map;

/**
 * @Author Huang Yongxiang
 * @Date 2022/09/19 17:08
 */
@AutoJobRPCClient("serverTest")
public interface IServerTest {
    String hello(String str);

    List<MethodTask> tasks(MethodTask task);

    Map<String,MethodTask> mapTasks(List<MethodTask> tasks);
}
