package com.example.autojob.job;

import com.example.autojob.skeleton.annotation.AutoJobRPCService;
import com.example.autojob.skeleton.framework.network.handler.client.RPCRequestHelper;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.util.id.IdGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author Huang Yongxiang
 * @Date 2022/09/19 17:07
 */
@AutoJobRPCService("serverTest")
@Slf4j
public class ServerTest implements IServerTest {
    public String hello(String str) {
        System.out.println(RPCRequestHelper.getCurrentHeader());
        return "hello " + str;
    }

    @Override
    public List<MethodTask> tasks(MethodTask task) {
        List<MethodTask> tasks = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            MethodTask methodTask = new MethodTask();
            methodTask.setId(IdGenerator.getNextIdAsLong());
            methodTask.setAlias(task.getAlias());
            tasks.add(methodTask);
        }
        return tasks;
    }

    @Override
    public Map<String, MethodTask> mapTasks(List<MethodTask> tasks) {
        Map<String, MethodTask> result = new HashMap<>();
        if (tasks == null || tasks.size() == 0) {
            return result;
        }
        for (MethodTask task : tasks) {
            result.put(task.getAlias(), task);
        }
        return result;
    }


}
