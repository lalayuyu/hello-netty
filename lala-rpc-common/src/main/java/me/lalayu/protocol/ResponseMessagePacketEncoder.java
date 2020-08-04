package me.lalayu.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.lalayu.serializer.RpcSerializer;
import me.lalayu.serializer.impl.HessianSerializer;
import me.lalayu.utils.ByteBufferUtils;

/**
 * @author lalayu
 **/
public class ResponseMessagePacketEncoder extends MessageToByteEncoder<ResponseMessagePacket> {
    private final RpcSerializer serializer;

    public ResponseMessagePacketEncoder() {
        this.serializer = HessianSerializer.S;
    }

    public ResponseMessagePacketEncoder(RpcSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseMessagePacket packet, ByteBuf out) throws Exception {
        packet.baseEncode(out);

        out.writeLong(packet.getStatusCode());

        String message = packet.getMessage();
        ByteBufferUtils.S.encodeUtf8CharSequence(out, message);

        byte[] bytes = serializer.serialize(packet.getPayload());
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
