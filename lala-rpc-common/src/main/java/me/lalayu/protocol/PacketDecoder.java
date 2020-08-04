package me.lalayu.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.lalayu.serializer.RpcSerializer;
import me.lalayu.serializer.impl.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 **/
public class PacketDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketDecoder.class);

    private Class<?> outClass;

    private RpcSerializer serializer;

    public PacketDecoder(Class<?> outClass) {
        this.outClass = outClass;
        this.serializer = HessianSerializer.S;
    }

    public PacketDecoder(Class<?> outClass, RpcSerializer serializer) {
        this.outClass = outClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object obj = null;
        try {
            obj = serializer.deserialize(data, outClass);
            out.add(obj);
        } catch (Exception e) {
            LOGGER.error("Decode error: " + e.toString());
        }
    }
}
