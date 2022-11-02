package com.example.autojob.skeleton.framework.network.protocol;

import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * RPC请求参数类型
 *
 * @Author Huang Yongxiang
 * @Date 2022/09/22 15:24
 */
@Getter
public class RPCRequestParamsType {
    /**
     * 参数类型
     */
    private final Class<?> type;
    /**
     * 泛型类型
     */
    private Class<?>[] genericsType;

    public RPCRequestParamsType(Class<?> type) {
        if (Collection.class.isAssignableFrom(type) && !isMap(type) && !isList(type)) {
            throw new IllegalArgumentException("集合类型只支持List和Map");
        }
        this.type = type;
    }

    public RPCRequestParamsType(Class<?> type, Class<?>... genericsType) {
        this.type = type;
        if (Collection.class.isAssignableFrom(type) && !isMap(type) && !isList(type)) {
            throw new IllegalArgumentException("集合类型只支持List和Map");
        }
        if (isMap(type) && genericsType.length != 2) {
            throw new IllegalArgumentException("Map类型参数存在且必须存在两个泛型类型");
        }
        if (isList(type) && genericsType.length != 1) {
            throw new IllegalArgumentException("List类型参数存在且必须存在一个泛型类型");
        }
        this.genericsType = genericsType;
    }

    public static boolean isList(Class<?> type) {
        return List.class.isAssignableFrom(type);
    }

    public static boolean isMap(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    public boolean isList() {
        return List.class.isAssignableFrom(type);
    }

    public boolean isMap() {
        return Map.class.isAssignableFrom(type);
    }

    public Class<?> getListGenericsType() {
        if (isList(type)) {
            return genericsType[0];
        }
        return null;
    }

    public Class<?> getMapKeyType() {
        if (isMap(type)) {
            return genericsType[0];
        }
        return null;
    }

    public Class<?> getMapValueType() {
        if (isMap(type)) {
            return genericsType[1];
        }
        return null;
    }
}
