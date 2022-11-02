package com.example.autojob.skeleton.annotation;

import java.lang.annotation.*;

/**
 * 标注一个类是RPC服务
 *
 * @Author Huang Yongxiang
 * @Date 2022/09/17 18:16
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoJobRPCService {
    /**
     * 服务名称
     */
    String value();
}
