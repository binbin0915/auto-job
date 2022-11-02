package com.example.autojob.skeleton.model.scheduler;

import com.example.autojob.skeleton.annotation.AutoJob;
import com.example.autojob.skeleton.annotation.FactoryAutoJob;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * AutoJob扫描器
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/15 15:54
 */
public class AutoJobScanner {
    private final Reflections methodReflections;
    private final Reflections classReflections;

    public AutoJobScanner(String... scanPattern) {
        if (scanPattern != null && scanPattern.length > 0) {
            this.methodReflections = new Reflections(scanPattern, Scanners.MethodsAnnotated);
            this.classReflections = new Reflections(scanPattern, Scanners.TypesAnnotated);
        } else {
            this.methodReflections = new Reflections(Scanners.MethodsAnnotated);
            this.classReflections = new Reflections(Scanners.TypesAnnotated);
        }
    }

    public Set<Method> scanMethods() {
        Set<Method> scannedMethods = new HashSet<>();
        scannedMethods.addAll(methodReflections.getMethodsAnnotatedWith(AutoJob.class));
        scannedMethods.addAll(methodReflections.getMethodsAnnotatedWith(FactoryAutoJob.class));
        return scannedMethods;
    }

    public Set<Class<?>> scanClasses() {
        return classReflections.getTypesAnnotatedWith(AutoJob.class);
    }

}
