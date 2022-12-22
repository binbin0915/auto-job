package com.example.autojob.skeleton.model.task.script;

import com.example.autojob.logging.model.producer.AutoJobLogHelper;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.task.TaskExecutable;
import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.thread.SyncHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 脚本任务可执行对象
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/21 11:05
 */
@Slf4j
public class ScriptTaskExecutable implements TaskExecutable {
    private final ScriptTask scriptTask;
    private final AutoJobLogHelper logHelper = new AutoJobLogHelper();

    public ScriptTaskExecutable(ScriptTask scriptTask) {
        this.scriptTask = scriptTask;
        logHelper.setSlf4jProxy(log);
    }

    @Override
    public AutoJobTask getAutoJobTask() {
        return scriptTask;
    }

    @Override
    public boolean isExecutable() {
        if (scriptTask.isCmd()) {
            return !StringUtils.isEmpty(scriptTask.getCmd());
        }
        File file = new File(scriptTask.getPath());
        return file.exists();
    }

    @Override
    public Object execute(Object... params) throws Exception {
        Process process = null;
        List<String> cmdList = new ArrayList<>();
        if (scriptTask.isScriptFile() && (!scriptTask.isNeedWrite() || scriptTask.write())) {
            cmdList.add(scriptTask.getCmd());
            cmdList.add(scriptTask.getPath());
            if (params != null) {
                cmdList.addAll(Arrays
                        .stream(params)
                        .filter(param -> param instanceof String)
                        .map(String::valueOf)
                        .collect(Collectors.toList()));
            }
            process = Runtime
                    .getRuntime()
                    .exec(cmdList.toArray(new String[0]));
        } else if (scriptTask.isCmd()) {
            process = Runtime
                    .getRuntime()
                    .exec(scriptTask.getCmd());
        }
        Process finalProcess = process;
        if (process != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream(), "GBK"));
            Thread thread = new Thread(() -> {
                do {
                    try {
                        SyncHelper.sleepQuietly(1, TimeUnit.MILLISECONDS);
                        String log = reader.readLine();
                        if (!StringUtils.isEmpty(log)) {
                            logHelper.info(log);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (finalProcess.isAlive());
            });
            thread.setName(String.format("%d-scriptTaskLogThread", scriptTask.getId()));
            thread.start();
            return process.waitFor();
        }
        return null;
    }

    @Override
    public Object[] getExecuteParams() {
        return scriptTask.getParams();
    }
}
