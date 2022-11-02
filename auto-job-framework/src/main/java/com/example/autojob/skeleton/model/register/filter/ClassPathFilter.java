package com.example.autojob.skeleton.model.register.filter;

import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.model.register.AbstractRegisterFilter;
import com.example.autojob.skeleton.model.task.method.MethodTask;
import com.example.autojob.util.convert.RegexUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 类路径过滤器
 * @Author Huang Yongxiang
 * @Date 2022/07/11 16:48
 */
@Slf4j
public class ClassPathFilter extends AbstractRegisterFilter {

    public ClassPathFilter() {
        super(AutoJobApplication
                .getInstance()
                .getConfigHolder()
                .getAutoJobConfig());
    }

    @Override
    public void doHandle(AutoJobTask task) {
        if (task.getIsAllowRegister() && task instanceof MethodTask) {
            if (config != null) {
                task.setIsAllowRegister(config
                        .getFilterClassPathList()
                        .stream()
                        .anyMatch(item -> RegexUtil.isMatch(task
                                .getMethodClass()
                                .getName(), RegexUtil.wildcardToRegexString(item))));
            } else {
                log.error("类路径过滤器异常，AutoJobConfig为null，此时将阻止任何任务通过");
                task.setIsAllowRegister(false);
            }
        }
        if (chain != null) {
            chain.doHandle(task);
        }
    }
}
