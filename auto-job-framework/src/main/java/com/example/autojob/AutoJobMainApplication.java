package com.example.autojob;

import com.example.autojob.skeleton.annotation.AutoJobProcessorScan;
import com.example.autojob.skeleton.annotation.AutoJobScan;
import com.example.autojob.skeleton.framework.launcher.AutoJobBootstrap;

@AutoJobScan("com.example.autojob.job")
@AutoJobProcessorScan("com.example.autojob")
public class AutoJobMainApplication {
    public static void main(String[] args) {
        new AutoJobBootstrap(AutoJobMainApplication.class)
                .withAutoScanProcessor()
                .closeDBTaskScheduler()
                .closeMemoryTaskScheduler()
                .build()
                .run();
        System.out.println("==================================>系统创建完成");
    }
}
