package com.example.autojob.skeleton.framework.config;

import com.example.autojob.util.io.PropertiesHolder;

/**
 * @Author Huang Yongxiang
 * @Date 2022/08/12 14:17
 */
public abstract class AbstractAutoJobConfig {
    protected PropertiesHolder propertiesHolder;

    public AbstractAutoJobConfig(PropertiesHolder propertiesHolder) {
        this.propertiesHolder = propertiesHolder;
    }

    public AbstractAutoJobConfig() {
        this.propertiesHolder= PropertiesHolder.builder().build();
    }
}
