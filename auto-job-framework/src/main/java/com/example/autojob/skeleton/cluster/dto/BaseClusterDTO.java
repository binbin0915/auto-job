package com.example.autojob.skeleton.cluster.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @Description 集群通信传输对象
 * @Author Huang Yongxiang
 * @Date 2022/07/25 16:29
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class BaseClusterDTO {
    /**
     * 节点主机地址
     */
    private String host;
    /**
     * 节点TCP端口号
     */
    private Integer port;
    /**
     * 节点服务器时间
     */
    private Long serverTime;
    /**
     * 节点身份token
     */
    private String token;
}
