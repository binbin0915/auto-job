package com.example.autojob.skeleton.model.handler;

import com.example.autojob.skeleton.framework.task.AutoJobWrapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Huang Yongxiang
 * @date 2022-12-03 17:29
 * @email 1158055613@qq.com
 */
@Slf4j
public abstract class AbstractAnnotationTaskHandler {
    private final AbstractAnnotationFilter filter;
    private final AutoJobWrapper wrapper;


    public AbstractAnnotationTaskHandler(AbstractAnnotationFilter filter, AutoJobWrapper wrapper) {
        this.filter = filter;
        this.wrapper = wrapper;
    }

    public abstract Set<Method> scanMethods(String... pattern);

    public final void doFilter(Set<Method> scannedMethods) {
        if (filter != null) {
            filter.doFilter(scannedMethods);
        }
    }

    public abstract int load(Set<Method> filteredMethods, AutoJobWrapper wrapper);

    public int handle(String... patterns) {
        Set<Method> scannedMethods = scanMethods(patterns);
        doFilter(scannedMethods);
        return load(scannedMethods, wrapper);
    }


}
