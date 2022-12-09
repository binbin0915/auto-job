package com.example.autojob.skeleton.framework.network.codec;

import com.example.autojob.skeleton.framework.config.ClusterConfig;
import com.example.autojob.skeleton.framework.boot.AutoJobApplication;
import com.example.autojob.skeleton.framework.network.enums.ReqType;
import com.example.autojob.skeleton.framework.network.protocol.RPCHeader;
import com.example.autojob.skeleton.framework.network.protocol.RPCProtocol;
import com.example.autojob.skeleton.framework.network.protocol.RPCRequest;
import com.example.autojob.skeleton.framework.network.protocol.RPCResponse;
import com.example.autojob.util.json.ClassCodec;
import com.example.autojob.util.json.JsonUtil;
import com.example.autojob.util.encrypt.AESUtil;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 解码器
 *
 * @Author Huang Yongxiang
 * @Date 2022/09/09 17:20
 */
@Slf4j
public class RPCDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        log.debug("=================RPC Start Decoding=================>");
        ClusterConfig clusterConfig = AutoJobApplication.getInstance().getConfigHolder().getClusterConfig();
        int totalLen = in.readInt() - 4;
        //log.info("数据总长度：{}，可读取长度：{}", totalLen, in.readableBytes());
        if (in.readableBytes() < totalLen) {
            in.resetReaderIndex();
            return;
        }
        int headerLen = in.readInt();
        //log.info("可读取长度：{}", in.readableBytes());
        byte[] encryptHeaderBytes = new byte[Math.min(in.readableBytes(), headerLen)];
        in.readBytes(encryptHeaderBytes);
        //log.info("可读取长度：{}", in.readableBytes());
        int contentLen = in.readInt();
        byte[] encryptContentBytes = new byte[Math.min(in.readableBytes(), contentLen)];
        in.readBytes(encryptContentBytes);
        //log.info("可读取长度：{}", in.readableBytes());
        String encryptHeaderJson = new String(encryptHeaderBytes, StandardCharsets.UTF_8);
        String encryptContentJson = new String(encryptContentBytes, StandardCharsets.UTF_8);
        String decryptHeaderJson = clusterConfig.getEnableAuth() ? AESUtil.build(clusterConfig.getClusterPublicKey()).aesDecrypt(encryptHeaderJson) : encryptHeaderJson;
        RPCHeader header = JsonUtil.jsonStringToPojo(decryptHeaderJson, RPCHeader.class);
        ReqType type = ReqType.findByTypeCode(header.getReqType());
        String decryptContentJson = clusterConfig.getEnableAuth() ? AESUtil.build(header.getRandomKey()).aesDecrypt(encryptContentJson) : encryptContentJson;
        switch (Objects.requireNonNull(type)) {
            case REQUEST: {
                RPCRequest request = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).registerTypeAdapter(RPCRequest.class, new RPCRequestJsonDeserializer()).create().fromJson(decryptContentJson, RPCRequest.class);
                RPCProtocol<RPCRequest> rpcProtocol = new RPCProtocol<>();
                rpcProtocol.setContent(request);
                rpcProtocol.setHeader(header);
                out.add(rpcProtocol);
                break;
            }
            case RESPONSE: {
                RPCResponse response = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).registerTypeAdapter(RPCResponse.class, new RPCResponseJsonDeserializer()).create().fromJson(decryptContentJson, RPCResponse.class);
                RPCProtocol<RPCResponse> rpcProtocol = new RPCProtocol<>();
                rpcProtocol.setContent(response);
                rpcProtocol.setHeader(header);
                out.add(rpcProtocol);
                break;
            }
            case HEARTBEAT: {
                break;
            }
            default:
                break;
        }
        log.debug("=================RPC Decoding Done=================<");
    }
}
