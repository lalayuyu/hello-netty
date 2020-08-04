package me.lalayu.protocol;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;
import me.lalayu.serializer.impl.FastJsonSerializer;
import me.lalayu.utils.SerialNumberUtils;

/**
 *
 **/
@Slf4j
public class ProtocolClientTest {
    public static void main(String[] args) throws Exception {
        int port = 8887;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap  = new Bootstrap();
        try {
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                    ch.pipeline().addLast(new LengthFieldPrepender(4));
                    ch.pipeline().addLast(new RequestMessagePacketEncoder(FastJsonSerializer.S));
                    ch.pipeline().addLast(new ResponseMessagePacketDecoder());
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<ResponseMessagePacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, ResponseMessagePacket packet) throws Exception {
                            Object payload = packet.getPayload();
                            if (payload instanceof ByteBuf) {
                                ByteBuf byteBuf = (ByteBuf) payload;
                                int readableByteLength = byteBuf.readableBytes();
                                byte[] bytes = new byte[readableByteLength];
                                byteBuf.readBytes(bytes);
                                payload = FastJsonSerializer.S.deserialize(bytes, String.class);
                                byteBuf.release();
                            }
                            packet.setPayload(payload);
                            log.info("接受到服务端的消息,内容:{}", JSON.toJSONString(packet));
                        }
                    });
                }
            });
            ChannelFuture future = bootstrap.connect("localhost", port).sync();
            log.info("启动客户端成功");
            Channel channel = future.channel();
            RequestMessagePacket packet = new RequestMessagePacket();
            packet.setMagicNumber(ProtocolConstant.MAGIC_NUMBER);
            packet.setVersion(ProtocolConstant.VERSION);
            packet.setSerialNumber(SerialNumberUtils.S.generateSerialNumber());
            packet.setMessageType(MessageType.REQUEST);
            packet.setInterfaceName("me.lalayu.contract.HelloService");
            packet.setMethodName("sayHello");
            packet.setMethodArgumentSignatures(new String[]{"java.lang.String"});
            packet.setMethodArguments(new Object[]{"doge"});
            channel.writeAndFlush(packet);
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }
}
