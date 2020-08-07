package me.lalayu.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import me.lalayu.protocol.PacketDecoder;
import me.lalayu.protocol.PacketEncoder;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.protocol.ResponseMessagePacket;
import me.lalayu.serializer.RpcSerializer;
import me.lalayu.server.handler.MessageHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *
 **/
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private Map<String, Object> serviceMap;
    private RpcSerializer serializer;

    public ServerInitializer(Map<String, Object> serviceMap, RpcSerializer serializer) {
        this.serviceMap = serviceMap;
        this.serializer = serializer;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,  0, 4));
        cp.addLast(new PacketDecoder(RequestMessagePacket.class, serializer));
        cp.addLast(new PacketEncoder(ResponseMessagePacket.class, serializer));
        cp.addLast(new MessageHandler(serviceMap));
    }
}
