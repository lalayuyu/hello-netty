package me.lalayu.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.lalayu.serializer.RpcSerializer;
import me.lalayu.serializer.impl.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 **/
public class PacketEncoder extends MessageToByteEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketEncoder.class);

    private Class<?> inClass;

    private RpcSerializer serializer;

    public PacketEncoder(Class<?> clazz) {
        this.inClass = clazz;
        this.serializer = HessianSerializer.S;
    }

    public PacketEncoder(Class<?> clazz, RpcSerializer serializer) {
        this.inClass = clazz;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (inClass.isInstance(in)) {
            try {
                byte[] data = serializer.serialize(in);
                out.writeInt(data.length);
                out.writeBytes(data);
            } catch (Exception e) {
                LOGGER.error("Encode error: " + e.toString());
            }
        }
    }
}
