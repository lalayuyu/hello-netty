package me.lalayu.client;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.protocol.ResponseMessagePacket;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;

/**
 *
 **/
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<ResponseMessagePacket> {

    private ConcurrentMap<String, ResponseFuture> responseFutureConcurrentMap = Maps.newConcurrentMap();
    private volatile Channel channel;
    private SocketAddress remoteAddr;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remoteAddr = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseMessagePacket response) throws Exception {
        String responseId = response.getSerialNumber();
        ResponseFuture responseFuture = responseFutureConcurrentMap.get(responseId);
        if (null != responseFuture) {
            responseFutureConcurrentMap.remove(responseId);
            responseFuture.finish(response);
            log.info("接受到服务器响应:{}", JSON.toJSONString(response));
        } else {
            log.warn("ID{}对应的RequestFuture未找到", response.getSerialNumber());
        }
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("Client caught exception!");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    public ResponseFuture sendRequest(RequestMessagePacket request) {
        ResponseFuture responseFuture = new ResponseFuture(request);
        responseFutureConcurrentMap.put(request.getSerialNumber(), responseFuture);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.info("Success send the request:{}", JSON.toJSONString(request));
            }
        });
        return responseFuture;
    }
}
