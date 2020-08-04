package me.lalayu.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.lalayu.serializer.RpcSerializer;
import me.lalayu.serializer.impl.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author lalayu
 **/
public class RequestMessagePacketEncoder extends MessageToByteEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMessagePacketEncoder.class);

    private final RpcSerializer serializer;

    public RequestMessagePacketEncoder() {
        this.serializer = HessianSerializer.S;
    }

    public RequestMessagePacketEncoder(RpcSerializer serializer) {
        this.serializer = serializer;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, Object packet, ByteBuf out) throws Exception {
        try {
            byte[] data = serializer.serialize(packet);
            out.writeInt(data.length);
            out.writeBytes(data);
        } catch (Exception e) {
            LOGGER.error("Encode error:" + e.toString());
        }
    }
}
