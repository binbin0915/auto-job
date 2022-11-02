package com.example.autojob.skeleton.framework.config;

import com.example.autojob.util.io.PropertiesHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 配置持有
 *
 * @Author Huang Yongxiang
 * @Date 2022/08/12 9:52
 */
@Slf4j
public class AutoJobConfigHolder {
    /**
     * 框架基础配置
     */
    private final AutoJobConfig autoJobConfig;
    /**
     * 日志配置
     */
    private final AutoJobLogConfig logConfig;
    /**
     * 集群配置
     */
    private final ClusterConfig clusterConfig;
    private final PropertiesHolder propertiesHolder;

    /**
     * 使用配置持有者创建一个配置源
     *
     * @param propertiesHolder 配置持有者
     * @author Huang Yongxiang
     * @date 2022/8/12 17:16
     */
    public AutoJobConfigHolder(PropertiesHolder propertiesHolder) {
        this.propertiesHolder = propertiesHolder;
        autoJobConfig = new AutoJobConfig(propertiesHolder);
        logConfig = new AutoJobLogConfig(propertiesHolder);
        clusterConfig = new ClusterConfig(propertiesHolder);
        log.debug("本次加载配置文件：{}", propertiesHolder.getProperty("configFiles"));
    }

    /**
     * 使用资源路径创建一个配置源，支持classpath下的yaml和properties格式
     *
     * @param resourceNamePattern 资源名，支持模式匹配，如application-*.yml
     * @author Huang Yongxiang
     * @date 2022/8/12 17:14
     */
    public AutoJobConfigHolder(String... resourceNamePattern) {
        this(PropertiesHolder.builder().addAllPropertiesFile(Arrays.asList(resourceNamePattern)).build());
    }

    public AutoJobConfig getAutoJobConfig() {
        if (autoJobConfig == null) {
            return new AutoJobConfig(propertiesHolder);
        }
        return autoJobConfig;
    }

    public AutoJobLogConfig getLogConfig() {
        if (logConfig == null) {
            return new AutoJobLogConfig(propertiesHolder);
        }
        return logConfig;
    }

    public ClusterConfig getClusterConfig() {
        if (clusterConfig == null) {
            return new ClusterConfig(propertiesHolder);
        }
        return clusterConfig;
    }

    public PropertiesHolder getPropertiesHolder() {
        return propertiesHolder;
    }
}
