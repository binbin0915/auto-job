package com.example.autojob.skeleton.framework.config;

import com.example.autojob.util.io.PropertiesHolder;
import lombok.Getter;

/**
 * @Description
 * @Author Huang Yongxiang
 * @Date 2022/07/07 17:37
 */
@Getter
public class AutoJobLogConfig extends AbstractAutoJobConfig {

    private Boolean enableMemory;

    private Integer memoryLength;

    private Double memoryDefaultExpireTime;

    private Boolean enableRunLogMemory;

    private Integer memoryRunLogLength;

    private Double memoryRunLogDefaultExpireTime;

    public AutoJobLogConfig(PropertiesHolder propertiesHolder) {
        super(propertiesHolder);
        if (propertiesHolder != null) {
            enableMemory = propertiesHolder.getProperty("autoJob.logging.taskLog.memory.enable", Boolean.class, "true");
            memoryLength = propertiesHolder.getProperty("autoJob.logging.taskLog.memory.length", Integer.class, "100");
            memoryDefaultExpireTime = propertiesHolder.getProperty("autoJob.logging.taskLog.memory.enable", Double.class, "10");
            enableRunLogMemory = propertiesHolder.getProperty("autoJob.logging.runLog.memory.enable", Boolean.class, "true");
            memoryRunLogLength = propertiesHolder.getProperty("autoJob.logging.runLog.memory.length", Integer.class, "100");
            memoryRunLogDefaultExpireTime = propertiesHolder.getProperty("autoJob.logging.runLog.memory.defaultExpireTime", Double.class, "10");
        }
    }

    public AutoJobLogConfig() {
    }
}
