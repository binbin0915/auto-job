package com.example.autojob.skeleton.framework.config;

import com.example.autojob.skeleton.framework.container.CleanStrategy;
import com.example.autojob.util.io.PropertiesHolder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 框架配置
 *
 * @Author Huang Yongxiang
 * @Date 2022/06/29 17:52
 */
@Getter
public class AutoJobConfig extends AbstractAutoJobConfig {

    private Integer schedulingQueueLength;

    private Integer memoryContainerLength;

    private CleanStrategy cleanStrategy;

    private Boolean allowCheckExist;

    private Boolean enableAnnotation;

    private Double annotationDefaultDelayTime;

    private Boolean enableRegisterFilter;

    private List<String> filterClassPathList;

    private Boolean enableErrorRetry;

    private Integer errorRetryCount;

    private Double errorRetryInterval;

    private Boolean enableCluster;

    private Boolean enableMailAlert;

    private String senderAddress;

    private String senderToken;

    private String[] receiverAddress;

    private String mailType;

    private String smtpAddress;

    private Integer smtpPort;

    private Boolean taskRunErrorAlert;

    private Boolean clusterOpenProtectedModeAlert;

    private Boolean clusterCloseProtectedModeAlert;

    private Boolean taskRefuseHandleAlert;

    private AutoJobExecutorPoolConfig executorPoolConfig;

    public AutoJobConfig(PropertiesHolder propertiesHolder) {
        super(propertiesHolder);
        if (propertiesHolder != null) {
            schedulingQueueLength = propertiesHolder.getProperty("autoJob.context.schedulingQueue.length", Integer.class, "1000");
            memoryContainerLength = propertiesHolder.getProperty("autoJob.context.memoryContainer.length", Integer.class, "200");
            cleanStrategy = CleanStrategy.findWithName(propertiesHolder.getProperty("autoJob.context.memoryContainer.cleanStrategy", String.class, "CLEAN_FINISHED"));
            allowCheckExist = propertiesHolder.getProperty("autoJob.context.allowCheckExist", Boolean.class, "true");
            enableAnnotation = propertiesHolder.getProperty("autoJob.annotation.enable", Boolean.class, "true");
            annotationDefaultDelayTime = propertiesHolder.getProperty("autoJob.annotation.defaultDelayTime", Double.class, "30");
            enableRegisterFilter = propertiesHolder.getProperty("autoJob.register.filter.enable", Boolean.class, "false");
            filterClassPathList = Arrays.asList(propertiesHolder.getProperty("autoJob.register.filter.classPath", "")
                                                                .split(","));
            enableErrorRetry = propertiesHolder.getProperty("autoJob.scheduler.finished.error.retry.enable", Boolean.class, "true");
            errorRetryCount = propertiesHolder.getProperty("autoJob.scheduler.finished.error.retry.retryCount", Integer.class, "3");
            errorRetryInterval = propertiesHolder.getProperty("autoJob.scheduler.finished.error.retry.interval", Double.class, "5");
            enableCluster = propertiesHolder.getProperty("autoJob.cluster.enable", Boolean.class, "false");
            enableMailAlert = propertiesHolder.getProperty("autoJob.emailAlert.enable", Boolean.class, "false");
            senderAddress = propertiesHolder.getProperty("autoJob.emailAlert.auth.sender");
            senderToken = propertiesHolder.getProperty("autoJob.emailAlert.auth.token");
            receiverAddress = propertiesHolder.getProperty("autoJob.emailAlert.auth.receiver", "")
                                              .split(",");
            mailType = propertiesHolder.getProperty("autoJob.emailAlert.auth.type");
            smtpAddress = propertiesHolder.getProperty("autoJob.emailAlert.auth.customize.smtpAddress", "");
            smtpPort = propertiesHolder.getProperty("autoJob.emailAlert.auth.customize.smtpPort", Integer.class, "0");
            taskRunErrorAlert = propertiesHolder.getProperty("autoJob.emailAlert.config" + ".taskRunError", Boolean.class, "true");
            clusterOpenProtectedModeAlert = propertiesHolder.getProperty("autoJob.emailAlert" + ".config.clusterOpenProtectedMode", Boolean.class, "true");
            clusterCloseProtectedModeAlert = propertiesHolder.getProperty("autoJob.emailAlert" + ".config.clusterCloseProtectedMode", Boolean.class, "true");
            taskRefuseHandleAlert = propertiesHolder.getProperty("autoJob.emailAlert.config" + ".taskRefuseHandle", Boolean.class, "true");
            executorPoolConfig = new AutoJobExecutorPoolConfig(propertiesHolder);
        }
    }

    public AutoJobConfig() {
        super();
    }
}
