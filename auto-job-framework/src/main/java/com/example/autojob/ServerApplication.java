package com.example.autojob;

import com.example.autojob.skeleton.framework.launcher.AutoJobLauncherBuilder;

/**
 * @Author Huang Yongxiang
 * @Date 2022/09/19 17:18
 */
public class ServerApplication {
    public static void main(String[] args) {
        new AutoJobLauncherBuilder(AutoJobMainApplication.class)
                .withAutoScanProcessor()
                .build()
                .run();
        System.out.println("RPC Server已启动");
    }
}
