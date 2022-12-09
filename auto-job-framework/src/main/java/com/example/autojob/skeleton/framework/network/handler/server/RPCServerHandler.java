package com.example.autojob.skeleton.framework.network.handler.server;

import com.example.autojob.skeleton.framework.network.enums.ReqType;
import com.example.autojob.skeleton.framework.network.protocol.RPCHeader;
import com.example.autojob.skeleton.framework.network.protocol.RPCProtocol;
import com.example.autojob.skeleton.framework.network.protocol.RPCRequest;
import com.example.autojob.skeleton.framework.network.protocol.RPCResponse;
import com.example.autojob.skeleton.framework.pool.AbstractAutoJobPool;
import com.example.autojob.util.thread.FlowThreadPoolExecutorHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端消息处理程序
 *
 * @Author Huang Yongxiang
 * @Date 2022/09/17 17:39
 */
@Slf4j
public class RPCServerHandler extends SimpleChannelInboundHandler<RPCProtocol<RPCRequest>> {
    private static final FlowThreadPoolExecutorHelper executorHelper = FlowThreadPoolExecutorHelper
            .classicBuilder()
            .setAllowMaxCoreThreadCount(20)
            .setAllowMinCoreThreadCount(1)
            .setMaxThreadCount(50)
            .setAllowMinThreadCount(5)
            .setTrafficListenerCycle(10)
            .setQueueLength(10)
            .setThreadFactory(new AbstractAutoJobPool.NamedThreadFactory("RPCServerHandler"))
            .setAllowUpdate(true)
            .build();

    public RPCServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCProtocol<RPCRequest> msg) throws Exception {
        RPCServiceHandler handler = new RPCServiceHandler(msg);
        RPCProtocol<RPCResponse> response = new RPCProtocol<>();
        RPCResponse result = executorHelper
                .submit(handler)
                .get();
        RPCHeader header = new RPCHeader(ReqType.RESPONSE);
        header.setReqId(msg
                .getHeader()
                .getReqId());
        response.setContent(result);
        response.setHeader(header);
        ctx.writeAndFlush(response);
    }
}
