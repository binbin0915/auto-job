package com.example.autojob.skeleton.framework.config;

import com.example.autojob.util.io.PropertiesHolder;
import lombok.Getter;
import lombok.Setter;

/**
 * 重试配置
 *
 * @author Huang Yongxiang
 * @date 2022-12-30 14:08
 * @email 1158055613@qq.com
 */
@Getter
@Setter
public class AutoJobRetryConfig extends AbstractAutoJobConfig {
    /**
     * 是否启用重试机制
     */
    private Boolean enable;
    /**
     * 重试次数
     */
    private Integer retryCount;
    /**
     * 重试间隔：分钟
     */
    private Double interval;


    public AutoJobRetryConfig(PropertiesHolder propertiesHolder) {
        super(propertiesHolder);
        enable = propertiesHolder.getProperty("autoJob.scheduler.finished.error.retry.enable", Boolean.class, "true");
        retryCount = propertiesHolder.getProperty("autoJob.scheduler.finished.error.retry.retryCount", Integer.class, "3");
        interval = propertiesHolder.getProperty("autoJob.scheduler.finished.error.retry.interval", Double.class, "5");
    }
}
