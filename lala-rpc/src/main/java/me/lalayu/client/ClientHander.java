package me.lalayu.client;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import me.lalayu.protocol.ResponseMessagePacket;
import me.lalayu.protocol.serialize.SerializerImpl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 *
 **/
@Slf4j
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClientHander extends SimpleChannelInboundHandler<ResponseMessagePacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseMessagePacket packet) throws Exception {
        Object targetPayLoad = packet.getPayload();
        if (targetPayLoad instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) targetPayLoad;
            int readableByteLength = byteBuf.readableBytes();
            byte[] bytes = new byte[readableByteLength];
            byteBuf.readBytes(bytes);
            targetPayLoad = SerializerImpl.S.decode(bytes, String.class);
            byteBuf.release();
        }
        packet.setPayload(targetPayLoad);
        log.info("接收到服务端的消息,消息内容{}", JSON.toJSONString(packet));
    }
}
