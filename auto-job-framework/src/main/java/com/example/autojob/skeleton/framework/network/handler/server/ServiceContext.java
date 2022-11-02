package com.example.autojob.skeleton.framework.network.handler.server;

import com.example.autojob.skeleton.annotation.AutoJobRPCService;
import com.example.autojob.util.bean.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务上下文
 *
 * @Author Huang Yongxiang
 * @Date 2022/09/17 18:08
 */
@Slf4j
public class ServiceContext {
    private final Map<String, Object> serviceContainer = new ConcurrentHashMap<>();

    public static ServiceContext getInstance() {
        return InstanceHolder.CONTEXT;
    }

    private ServiceContext() {
        Set<Class<?>> serviceClasses = new ServiceScanner().scan();
        for (Class<?> clazz : serviceClasses) {
            Object instance = ObjectUtil.getClassInstance(clazz);
            if (instance != null) {
                AutoJobRPCService ASRPCService = clazz.getAnnotation(AutoJobRPCService.class);
                if (serviceContainer.containsKey(ASRPCService.value())) {
                    log.error("服务：{}已存在一个实例", ASRPCService.value());
                    throw new IllegalArgumentException();
                }
                serviceContainer.put(ASRPCService.value(), instance);
            }
        }
    }

    public Object getServiceInstance(String serviceName) {
        if (isExist(serviceName)) {
            return serviceContainer.get(serviceName);
        }
        return null;
    }

    public boolean isExist(String serviceName) {
        return serviceContainer.containsKey(serviceName);
    }

    public Method getServiceMethod(String serviceName, String methodName, Class<?>... paramsType) {
        if (!isExist(serviceName)) {
            throw new NoSuchServiceException("没有服务：" + serviceName);
        }
        Object service = getServiceInstance(serviceName);
        if (paramsType != null && paramsType.length > 0) {
            return ObjectUtil.findMethod(methodName, service.getClass(), paramsType);
        } else {
            return ObjectUtil.findMethod(methodName, service.getClass());
        }
    }

    public boolean withReturn(String serviceName, String methodName, Class<?>... paramsType) {
        return !((getReturnType(serviceName, methodName, paramsType) == Void.TYPE));
    }

    public Class<?> getReturnType(String serviceName, String methodName, Class<?>... paramsType) {
        Method method = getServiceMethod(serviceName, methodName, paramsType);
        if (method == null) {
            throw new NoSuchServiceMethodException();
        }
        return method.getReturnType();
    }

    public Object invokeServiceMethod(String serviceName, String methodName, Class<?>[] paramsType, Object... params) throws Exception {
        if (!isExist(serviceName)) {
            throw new NoSuchServiceException("没有服务：" + serviceName);
        }
        Object service = getServiceInstance(serviceName);
        Method method = getServiceMethod(serviceName, methodName, paramsType);
        if (method != null) {
            try {
                return method.invoke(service, params);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        throw new NoSuchServiceMethodException("服务" + serviceName + "没有方法：" + methodName);
    }

    public Object removeAndGet(String serviceName) {
        return serviceContainer.remove(serviceName);
    }

    private static class InstanceHolder {
        private static final ServiceContext CONTEXT = new ServiceContext();
    }
}
