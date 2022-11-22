package com.example.autojob.skeleton.model.executor;

import com.example.autojob.util.bean.ObjectUtil;

/**
 * 默认工厂，直接调用类的无参构造方法创建类对象
 *
 * @Author Huang Yongxiang
 * @Date 2022/07/14 14:38
 */
public class DefaultMethodObjectFactory implements IMethodObjectFactory {
    @Override
    public Object createMethodObject(Class<?> methodClass) {
        try {
            if (methodClass != null) {
                return ObjectUtil.getClassInstance(methodClass);
            }
        } catch (Exception ignored) {

        }
        return null;
    }
}
