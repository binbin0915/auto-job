package com.example.autojob.skeleton.framework.config;

import com.example.autojob.skeleton.annotation.HotLoadable;
import com.example.autojob.skeleton.enumerate.DatabaseType;
import com.example.autojob.skeleton.framework.container.CleanStrategy;
import com.example.autojob.util.io.PropertiesHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 框架配置
 *
 * @Author Huang Yongxiang
 * @Date 2022/06/29 17:52
 */
@Getter
@Slf4j
public class AutoJobConfig extends AbstractAutoJobConfig {

    private Integer schedulingQueueLength;

    private Integer memoryContainerLength;

    private CleanStrategy cleanStrategy;

    private Boolean enableAnnotation;

    private Double annotationDefaultDelayTime;

    private DatabaseType databaseType;

    @HotLoadable
    private Boolean enableRegisterFilter;

    @HotLoadable
    private List<String> filterClassPathList;

    private AutoJobRetryConfig retryConfig;

    private Boolean enableCluster;

    private Boolean enableMailAlert;

    private String senderAddress;

    private String senderToken;

    private String[] receiverAddress;

    private String mailType;

    private String smtpAddress;

    private Integer smtpPort;

    @HotLoadable
    private Boolean taskRunErrorAlert;

    @HotLoadable
    private Boolean clusterOpenProtectedModeAlert;

    @HotLoadable
    private Boolean clusterCloseProtectedModeAlert;

    @HotLoadable
    private Boolean taskRefuseHandleAlert;

    private AutoJobExecutorPoolConfig executorPoolConfig;

    public AutoJobConfig(PropertiesHolder propertiesHolder) {
        super(propertiesHolder);
        if (propertiesHolder != null) {
            schedulingQueueLength = propertiesHolder.getProperty("autoJob.context.schedulingQueue.length", Integer.class, "1000");
            memoryContainerLength = propertiesHolder.getProperty("autoJob.context.memoryContainer.length", Integer.class, "200");
            cleanStrategy = CleanStrategy.findWithName(propertiesHolder.getProperty("autoJob.context.memoryContainer.cleanStrategy", String.class, "KEEP_FINISHED"));
            databaseType = DatabaseType.findByName(propertiesHolder.getProperty("autoJob.database.type", String.class, "mysql"));
            if (databaseType == null) {
                log.warn("未知的数据库类型：{}", propertiesHolder.getProperty("autoJob.database.type", String.class));
            }
            enableAnnotation = propertiesHolder.getProperty("autoJob.annotation.enable", Boolean.class, "true");
            annotationDefaultDelayTime = propertiesHolder.getProperty("autoJob.annotation.defaultDelayTime", Double.class, "30");
            enableRegisterFilter = propertiesHolder.getProperty("autoJob.register.filter.enable", Boolean.class, "false");
            filterClassPathList = Arrays.asList(propertiesHolder
                    .getProperty("autoJob.register.filter.classPath", "")
                    .split(","));
            enableCluster = propertiesHolder.getProperty("autoJob.cluster.enable", Boolean.class, "false");
            enableMailAlert = propertiesHolder.getProperty("autoJob.emailAlert.enable", Boolean.class, "false");
            senderAddress = propertiesHolder.getProperty("autoJob.emailAlert.auth.sender");
            senderToken = propertiesHolder.getProperty("autoJob.emailAlert.auth.token");
            receiverAddress = propertiesHolder
                    .getProperty("autoJob.emailAlert.auth.receiver", "")
                    .split(",");
            mailType = propertiesHolder.getProperty("autoJob.emailAlert.auth.type");
            smtpAddress = propertiesHolder.getProperty("autoJob.emailAlert.auth.customize.smtpAddress", "");
            smtpPort = propertiesHolder.getProperty("autoJob.emailAlert.auth.customize.smtpPort", Integer.class, "0");
            taskRunErrorAlert = propertiesHolder.getProperty("autoJob.emailAlert.config" + ".taskRunError", Boolean.class, "true");
            clusterOpenProtectedModeAlert = propertiesHolder.getProperty("autoJob.emailAlert" + ".config.clusterOpenProtectedMode", Boolean.class, "true");
            clusterCloseProtectedModeAlert = propertiesHolder.getProperty("autoJob.emailAlert" + ".config.clusterCloseProtectedMode", Boolean.class, "true");
            taskRefuseHandleAlert = propertiesHolder.getProperty("autoJob.emailAlert.config" + ".taskRefuseHandle", Boolean.class, "true");
            executorPoolConfig = new AutoJobExecutorPoolConfig(propertiesHolder);
            retryConfig = new AutoJobRetryConfig(propertiesHolder);
        }
    }
}
