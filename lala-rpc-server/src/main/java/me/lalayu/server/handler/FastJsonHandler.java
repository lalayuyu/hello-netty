package me.lalayu.server.handler;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;

/**
 *
 **/
public class FastJsonHandler implements ServerHandler{
//    @Override
//    public void handle(Map<String, Object> serviceMap, ChannelPipeline pipeline) {
//        pipeline.addLast(new IdleStateHandler(0, 0,))
//        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, ))
//    }
}
