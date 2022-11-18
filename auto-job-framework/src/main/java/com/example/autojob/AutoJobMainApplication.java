package com.example.autojob;

import com.example.autojob.api.task.AutoJobScriptTaskAttributes;
import com.example.autojob.skeleton.annotation.AutoJobProcessorScan;
import com.example.autojob.skeleton.annotation.AutoJobScan;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.framework.launcher.AutoJobBootstrap;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.model.builder.AutoJobScriptTaskBuilder;
import com.example.autojob.skeleton.model.task.script.ScriptTask;
import com.example.autojob.util.id.IdGenerator;

import java.util.concurrent.TimeUnit;

@AutoJobScan("com.example.autojob.job")
@AutoJobProcessorScan("com.example.autojob")
public class AutoJobMainApplication {
    public static void main(String[] args) {
        new AutoJobBootstrap(AutoJobMainApplication.class)
                .withAutoScanProcessor()
                .build()
                .run();
        System.out.println("==================================>系统创建完成");
        ScriptTask scriptTask = new AutoJobScriptTaskBuilder()
                .setTaskId(IdGenerator.getNextIdAsLong())
                .setTaskAlias("测试脚本任务")
                .setTaskType(AutoJobTask.TaskType.MEMORY_TASk)
                .addADelayTrigger(5, TimeUnit.SECONDS)
                .createNew("python", "D:\\work\\创立\\项目源代码\\DEC_OCR", "server.py", ".py");
        boolean flag = AutoJobApplication
                .getInstance()
                .getMemoryTaskAPI()
                .registerTask(new AutoJobScriptTaskAttributes(scriptTask));
        System.out.println("flag = " + flag);
    }
}
