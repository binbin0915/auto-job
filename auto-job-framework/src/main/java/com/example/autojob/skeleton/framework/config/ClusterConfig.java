package com.example.autojob.skeleton.framework.config;

import com.example.autojob.util.io.PropertiesHolder;
import lombok.Getter;

/**
 * @Description 集群配置
 * @Author Huang Yongxiang
 * @Date 2022/07/22 16:44
 */
@Getter
public class ClusterConfig extends AbstractAutoJobConfig {
    private Integer port;

    private String clusterPublicKey;

    private Integer clientPoolSize;

    private Double getClientTimeout;

    private Double getDataTimeout;

    private Double connectTimeout;

    private Double keepAliveTimeout;

    private String clusterToken;

    private Boolean enableAnnotation;

    private Boolean enableAuth;

    private Boolean enableProtectedModel;

    private Double openProtectedModelThreshold;

    private String clusterNodeUrl;

    private Double clusterAllowMaxJetLag;

    private Double nodeSyncCycle;

    private Integer nodeOffLineThreshold;

    public ClusterConfig(PropertiesHolder propertiesHolder) {
        super(propertiesHolder);
        if (propertiesHolder != null) {
            port = propertiesHolder.getProperty("autoJob.cluster.port", Integer.class, "8026");
            clientPoolSize = propertiesHolder.getProperty("autoJob.cluster.pool.size", Integer.class, "3");
            getClientTimeout = propertiesHolder.getProperty("autoJob.cluster.pool.size", Double.class, "3");
            connectTimeout = propertiesHolder.getProperty("autoJob.cluster.client.pool.connectTimeout", Double.class, "10");
            getDataTimeout = propertiesHolder.getProperty("autoJob.cluster.client.pool.getDataTimeout", Double.class, "5");
            keepAliveTimeout = propertiesHolder.getProperty("autoJob.cluster.client.pool.keepAliveTimeout", Double.class, "1");
            clusterPublicKey = propertiesHolder.getProperty("autoJob.cluster.auth.publicKey", "");
            clusterToken = propertiesHolder.getProperty("autoJob.cluster.auth.token", "");
            enableAnnotation = propertiesHolder.getProperty("autoJob.cluster.config.annotations.enable", Boolean.class, "false");
            enableAuth = propertiesHolder.getProperty("autoJob.cluster.auth.enable", Boolean.class, "true");
            enableProtectedModel = propertiesHolder.getProperty("autoJob.cluster.config.protectedMode.enable", Boolean.class, "true");
            openProtectedModelThreshold = propertiesHolder.getProperty("autoJob.cluster.config.protectedMode.threshold", Double.class, "0.5");
            clusterNodeUrl = propertiesHolder.getProperty("autoJob.cluster.client.nodeUrl");
            clusterAllowMaxJetLag = propertiesHolder.getProperty("autoJob.cluster.client.allowMaxJetLag", Double.class, "3");
            nodeSyncCycle = propertiesHolder.getProperty("autoJob.cluster.client.nodeSync.cycle", Double.class, "10");
            nodeOffLineThreshold = propertiesHolder.getProperty("autoJob.cluster.client.nodeSync.offLineThreshold", Integer.class, "3");
        }
    }

    public ClusterConfig() {
    }


}
