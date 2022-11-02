package com.example.autojob.skeleton.framework.network.protocol;

import com.example.autojob.skeleton.framework.config.ClusterConfig;
import com.example.autojob.skeleton.framework.launcher.AutoJobApplication;
import com.example.autojob.skeleton.framework.network.enums.ReqType;
import com.example.autojob.util.convert.StringUtils;
import com.example.autojob.util.id.IdGenerator;
import com.example.autojob.util.id.SystemClock;
import com.example.autojob.util.servlet.InetUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息体Header部分
 *
 * @Author Huang Yongxiang
 * @Date 2022/09/09 12:11
 */
@Data
public class RPCHeader implements Serializable {
    /**
     * 通信token
     */
    private String token;
    /**
     * 通信公钥
     */
    private String randomKey;
    /**
     * 请求编号
     */
    private long reqId;
    /**
     * 发送的IP地址
     */
    private String sendIp;
    /**
     * 发送的端口号
     */
    private int sendPort;
    /**
     * 请求时间
     */
    private long sendTime;
    /**
     * 请求类型
     */
    private byte reqType;

    public RPCHeader(ReqType reqType) {
        ClusterConfig config = AutoJobApplication.getInstance().getConfigHolder().getClusterConfig();
        this.reqType = reqType.getType();
        this.token = config.getClusterToken();
        this.randomKey = StringUtils.getRandomStr(16);
        this.sendPort = InetUtil.getPort();
        this.sendTime = SystemClock.now();
        this.sendIp = InetUtil.getLocalhostIp();
        this.reqId = IdGenerator.getNextIdAsLong();
    }
}
