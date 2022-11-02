package com.example.autojob.skeleton.model.alert.event;

import com.example.autojob.skeleton.enumerate.AlertEventLevel;

/**
 * @Description 集群节点关闭保护模式事件
 * @Author Huang Yongxiang
 * @Date 2022/07/29 10:33
 */
public class ClusterCloseProtectedModelEvent extends AlertEvent {
    public ClusterCloseProtectedModelEvent(String title, String content) {
        super(title, AlertEventLevel.INFO, content);
    }
}
