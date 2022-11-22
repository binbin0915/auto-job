package com.example.spring;

import com.example.autojob.skeleton.annotation.AutoJobRegisterPreProcessorScan;
import com.example.autojob.skeleton.annotation.AutoJobScan;
import com.example.autojob.skeleton.framework.launcher.AutoJobBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AutoJobScan("com.example.spring.job")
@AutoJobRegisterPreProcessorScan("com.example.spring")
public class AutoJobSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoJobSpringApplication.class, args);
        System.out.println("==================================>Spring应用已启动完成");
        //可以通过任意方式启动
        new AutoJobBootstrap(AutoJobSpringApplication.class)
                .withAutoScanProcessor()
                .build()
                .run();
        System.out.println("==================================>AutoJob应用已启动完成");
    }

}
