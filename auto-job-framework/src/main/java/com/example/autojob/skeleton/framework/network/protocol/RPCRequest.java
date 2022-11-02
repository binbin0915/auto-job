package com.example.autojob.skeleton.framework.network.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC请求类
 *
 * @Author Huang Yongxiang
 * @Date 2022/09/09 16:15
 */
@Data
public class RPCRequest implements Serializable {
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数列表
     */
    private Object[] params;
    /**
     * 参数类型
     */
    private Class<?>[] paramTypes;
    /**
     * 支持泛型List和Map的参数封装类型
     */
    private RPCRequestParamsType[] paramsTypes;
}
