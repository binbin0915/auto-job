package com.example.autojob.skeleton.framework.network.codec;

import com.example.autojob.skeleton.framework.config.ClusterConfig;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.network.protocol.RPCHeader;
import com.example.autojob.skeleton.framework.network.protocol.RPCProtocol;
import com.example.autojob.util.json.JsonUtil;
import com.example.autojob.util.encrypt.AESUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 编码器
 *
 * @Author Huang Yongxiang
 * @Date 2022/09/09 17:20
 */
@Slf4j
public class RPCEncoder extends MessageToByteEncoder<RPCProtocol<Object>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RPCProtocol<Object> msg, ByteBuf out) throws Exception {
        log.debug("=================RPC Start Encoding=================>");
        ClusterConfig clusterConfig = AutoJobApplication
                .getInstance()
                .getConfigHolder()
                .getClusterConfig();
        RPCHeader header = msg.getHeader();
        String headerJson = JsonUtil.pojoToJsonString(header);
        String contentJson = JsonUtil.pojoToJsonString(msg.getContent());
        String encryptHeaderJson = clusterConfig.getEnableAuth() ? AESUtil
                .build(clusterConfig.getClusterPublicKey())
                .aesEncrypt(headerJson) : headerJson;
        String encryptContentJson = clusterConfig.getEnableAuth() ? AESUtil
                .build(header.getRandomKey())
                .aesEncrypt(contentJson) : contentJson;
        //按顺序写入数据
        byte[] headerBytes = encryptHeaderJson.getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes = encryptContentJson.getBytes(StandardCharsets.UTF_8);
        //log.info("写入头长度：{}，内容长度：{}", headerBytes.length, contentBytes.length);
        out
                .writeInt(12 + headerBytes.length + contentBytes.length)
                .writeInt(headerBytes.length)
                .writeBytes(headerBytes)
                .writeInt(contentBytes.length)
                .writeBytes(contentBytes);
        log.debug("=================RPC Encoding Done=================<");
    }
}
