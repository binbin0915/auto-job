package com.example.autojob.skeleton.cluster.model;

import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.servlet.InetUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Description 集群节点
 * @Author Huang Yongxiang
 * @Date 2022/07/26 9:09
 */
@Getter
@Setter
@Accessors(chain = true)
public class ClusterNode {
    /**
     * 节点主机地址
     */
    private String host;
    /**
     * 节点TCP端口号
     */
    private Integer port;
    /**
     * 上次响应时长
     */
    private Long lastResponseTime;
    /**
     * 上次请求是否成功
     */
    private Boolean isLastRequestSuccess;
    /**
     * 是否在线
     */
    private Boolean isOnline;

    public static ClusterNode getLocalHostNode() {
        ClusterNode clusterNode = new ClusterNode();
        clusterNode.setHost(InetUtil.getLocalhostIp());
        clusterNode.setPort(InetUtil.getPort());
        return clusterNode;
    }

    public static boolean isLocalHostNode(ClusterNode node) {
        if (node == null || StringUtils.isEmpty(node.getHost()) || node.getPort() == null) {
            return false;
        }
        return node.getPort() == InetUtil.getPort() && node.getHost().equals(InetUtil.getLocalhostIp());
    }

    @Override
    public String toString() {
        return String.format("%s:%d", host, port);
    }
}
