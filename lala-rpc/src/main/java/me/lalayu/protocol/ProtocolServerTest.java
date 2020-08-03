package me.lalayu.protocol;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;
import me.lalayu.protocol.serialize.Serializer;
import me.lalayu.protocol.serialize.SerializerImpl;
import me.lalayu.server.ServerHandler;

/**
 *
 **/
@Slf4j
public class ProtocolServerTest {
    public static void main(String[] args) throws Exception {
        int port = 8887;
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                      @Override
                      protected void initChannel(SocketChannel ch) throws Exception {
                          ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                          ch.pipeline().addLast(new LengthFieldPrepender(4));
                          ch.pipeline().addLast(new RequestMessagePacketDecoder());
                          ch.pipeline().addLast(new ResponseMessagePacketEncoder(SerializerImpl.S));
                          ch.pipeline().addLast(new SimpleChannelInboundHandler<RequestMessagePacket>() {
                              @Override
                              protected void channelRead0(ChannelHandlerContext ctx, RequestMessagePacket packet) throws Exception {
                                  log.info("接受到客户端消息,内容:{}", JSON.toJSONString(packet));
                                  ResponseMessagePacket response = new ResponseMessagePacket();
                                  response.setMagicNumber(packet.getMagicNumber());
                                  response.setVersion(packet.getVersion());
                                  response.setSerialNumber(packet.getSerialNumber());
                                  response.setAttachments(packet.getAttachments());
                                  response.setMessageType(MessageType.RESPONSE);
                                  response.setStatusCode(200L);
                                  response.setMessage("Success");
                                  response.setPayload("{\"name\":\"lalayu\"}");
                                  ctx.writeAndFlush(response);
                              }
                          });
                      }
                  });
            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("启动成功...", port);
            future.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
