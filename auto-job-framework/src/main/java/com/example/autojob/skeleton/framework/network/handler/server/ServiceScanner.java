package com.example.autojob.skeleton.framework.network.handler.server;

import com.example.autojob.skeleton.annotation.AutoJobRPCService;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;

/**
 * 服务扫描器
 *
 * @Author Huang Yongxiang
 * @Date 2022/09/17 18:09
 */
public class ServiceScanner {
    private final Reflections reflections = new Reflections(Scanners.TypesAnnotated);

    public Set<Class<?>> scan() {
        return reflections.getTypesAnnotatedWith(AutoJobRPCService.class);
    }
}
