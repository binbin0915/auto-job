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
    public Object createMethodObject(String classPath) {
        try {
            Class<?> clazz = ObjectUtil.classPath2Class(classPath);
            if (clazz != null) {
                return ObjectUtil.getClassInstance(clazz);
            }
        } catch (Exception ignored) {

        }
        return null;
    }
}
