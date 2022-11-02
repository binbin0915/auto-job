package com.example.autojob.skeleton.framework.network;

import com.example.autojob.skeleton.framework.config.ClusterConfig;
import com.example.autojob.skeleton.framework.network.client.RPCClient;
import com.example.autojob.skeleton.framework.network.client.RPCClientPool;
import com.example.autojob.skeleton.framework.network.handler.client.RPCClientProxy;
import com.example.autojob.skeleton.framework.network.server.RPCServer;
import com.example.autojob.util.servlet.InetUtil;

import java.util.concurrent.TimeUnit;

/**
 * 通信管理器
 *
 * @Author Huang Yongxiang
 * @Date 2022/10/19 14:11
 */
public class AutoJobNetWorkManager {
    private final RPCClientPool clientPool;
    private final RPCServer serverApplication;

    public AutoJobNetWorkManager(ClusterConfig clusterConfig) {
        this.clientPool = RPCClientPool
                .builder()
                .setGetClientTimeout((long) (clusterConfig.getGetClientTimeout() * 1000), TimeUnit.MILLISECONDS)
                .setPoolSize(clusterConfig.getClientPoolSize())
                .setKeepAliveTime((long) (clusterConfig.getKeepAliveTimeout() * 1000), TimeUnit.MILLISECONDS)
                .setMinIdle(3)
                .setMaxIdle(5)
                .setConnectTimeout((long) (clusterConfig.getConnectTimeout() * 1000), TimeUnit.MILLISECONDS)
                .setGetDataTimeout((long) (clusterConfig.getGetDataTimeout() * 1000), TimeUnit.MILLISECONDS)
                .build();
        this.serverApplication = new RPCServer(clusterConfig.getPort());
    }

    public RPCClient getRPCClient() {
        return clientPool.getClient();
    }

    public void closeClient(RPCClient client) {
        clientPool.release(client);
    }

    public void startRPCServer() {
        if (!serverApplication.isStart()) {
            serverApplication.startServer();
        }
    }

    public <T> T getProxyInterface(Class<T> interfaceType, String host, int port) {
        RPCClientProxy<T> proxy = new RPCClientProxy<>("localhost".equals(host) ? InetUtil.getLocalhostIp() : host, port, interfaceType);
        return proxy.clientProxy();
    }
}
