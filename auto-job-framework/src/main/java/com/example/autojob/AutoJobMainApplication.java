package com.example.autojob;

import com.example.autojob.skeleton.annotation.AutoJobProcessorScan;
import com.example.autojob.skeleton.annotation.AutoJobScan;
import com.example.autojob.skeleton.db.mapper.AutoJobMapperHolder;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.framework.launcher.AutoJobBootstrap;
import com.example.autojob.util.thread.SyncHelper;

@AutoJobScan("com.example.autojob.job")
@AutoJobProcessorScan("com.example.autojob")
public class AutoJobMainApplication {
    public static void main(String[] args) {
        new AutoJobBootstrap(AutoJobMainApplication.class)
                .withAutoScanProcessor()
                .build()
                .run();
        System.out.println("==================================>系统创建完成");
        SyncHelper.aWaitQuietly(() -> AutoJobApplication
                .getInstance()
                .isRunning());
        System.out.println(AutoJobMapperHolder.SCHEDULING_RECORD_ENTITY_MAPPER.count());
    }
}
