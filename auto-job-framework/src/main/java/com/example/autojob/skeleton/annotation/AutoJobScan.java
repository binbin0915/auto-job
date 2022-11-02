package com.example.autojob.skeleton.annotation;

import java.lang.annotation.*;

/**
 * 扫描任务的包路径，支持子包扫描
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/12 17:44
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoJobScan {
    String[] value();
}
