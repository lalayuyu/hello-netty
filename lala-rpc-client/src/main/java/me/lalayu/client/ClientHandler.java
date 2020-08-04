package me.lalayu.client;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import me.lalayu.protocol.ResponseMessagePacket;

/**
 *
 **/
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<ResponseMessagePacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseMessagePacket packet) throws Exception {
        log.info("接受到服务器响应:{}", JSON.toJSONString(packet));
        ResponseFuture responseFuture = ContractProxyFactory.RESPONSE_FUTURE_TABLE.get(packet.getSerialNumber());
        if (null != responseFuture) {
            responseFuture.putResponse(packet);
        } else {
            log.warn("ID{}对应的RequestFuture未找到", packet.getSerialNumber());
        }
    }
}
