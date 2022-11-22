package com.example.spring.job;

import com.example.autojob.skeleton.model.executor.IMethodObjectFactory;
import com.example.autojob.util.bean.ObjectUtil;
import com.example.spring.util.SpringUtil;

/**
 * @Author Huang Yongxiang
 * @Date 2022/11/18 14:20
 * @Email 1158055613@qq.com
 */
public class SpringMethodObjectFactory implements IMethodObjectFactory {
    public Object createMethodObject(Class<?> methodClass) {
        Object bean = SpringUtil.getBean(methodClass);
        if (bean == null) {
            return ObjectUtil.getClassInstance(methodClass);
        }
        return bean;
    }
}
