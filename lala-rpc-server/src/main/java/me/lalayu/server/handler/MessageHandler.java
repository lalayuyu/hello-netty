package me.lalayu.server.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.lalayu.exception.MethodMatchException;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.protocol.ResponseMessagePacket;
import me.lalayu.server.ServerExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 **/
public class MessageHandler extends SimpleChannelInboundHandler<RequestMessagePacket> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    private final Map<String, Object> serviceMap;

    public MessageHandler(Map<String, Object> serviceMap, ThreadPoolExecutor serverHandlerPool) {
        this.serviceMap = serviceMap;
    }

    public MessageHandler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessagePacket request) throws Exception {

        ResponseMessagePacket response = new ResponseMessagePacket();
        MessageRecvTask task = new MessageRecvTask(serviceMap, request, response);
        ServerExecutor.submit(task, ctx, request, response);
//        serverHandlerPool.execute(() -> {
//            LOGGER.info("Receive request id: {}", request.getSerialNumber());
//            ResponseMessagePacket response = new ResponseMessagePacket();
//            response.setSerialNumber(request.getSerialNumber());
//            try {
//                Object result = handle(request);
//                response.setStatusCode(200L);
//                response.setMessage("success");
//                response.setPayload(result);
//            } catch (Throwable t) {
//                response.setStatusCode(500L);
//                response.setMessage(t.toString());
//                LOGGER.error("error handling the request", t);
//            }
//            ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    LOGGER.info("response to the client, id:{}", request.getSerialNumber());
//                }
//            });
//        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
