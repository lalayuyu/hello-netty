package me.lalayu.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import me.lalayu.client.ClientHandler;
import me.lalayu.protocol.PacketDecoder;
import me.lalayu.protocol.PacketEncoder;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.protocol.ResponseMessagePacket;
import me.lalayu.serializer.RpcSerializer;
import me.lalayu.serializer.impl.HessianSerializer;


/**
 *
 **/
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private final RpcSerializer serializer;

    public ClientInitializer(RpcSerializer serializer) {
        this.serializer = serializer;
    }

    public ClientInitializer() {
        this.serializer = HessianSerializer.S;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        cp.addLast(new PacketEncoder(RequestMessagePacket.class, serializer));
        cp.addLast(new PacketDecoder(ResponseMessagePacket.class, serializer));
        cp.addLast(new ClientHandler());
    }
}
