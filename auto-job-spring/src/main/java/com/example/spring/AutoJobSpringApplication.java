package com.example.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AutoJobSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoJobSpringApplication.class, args);
        System.out.println("==================================>Spring应用已启动完成");
    }

}
