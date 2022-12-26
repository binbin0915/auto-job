package com.example.spring.job;

import com.example.autojob.skeleton.annotation.AutoJobScan;
import com.example.autojob.skeleton.framework.boot.AutoJobBootstrap;
import com.example.spring.processor.SpringStartProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 启动AutoJob应用
 *
 * @author Huang Yongxiang
 * @date 2022-12-11 11:15
 * @email 1158055613@qq.com
 */
@Component
@AutoJobScan("com.example.spring.job")
@Slf4j
public class AutoJobRunner implements SpringStartProcessor {
    @Override
    public void onStart() {
        new AutoJobBootstrap(AutoJobRunner.class)
                .withAutoScanProcessor()
                .build()
                .run();
    }
}
