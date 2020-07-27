package me.lalayu.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import me.lalayu.protocol.serialize.Serializer;
import me.lalayu.utils.ByteBufferUtils;

/**
 * @author lalayu
 **/
@RequiredArgsConstructor
public class ResponseMessagePacketEncoder extends MessageToByteEncoder<ResponseMessagePacket> {
    private final Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseMessagePacket packet, ByteBuf out) throws Exception {
        packet.baseEncode(out);

        out.writeLong(packet.getStatusCode());

        String message = packet.getMessage();
        ByteBufferUtils.S.encodeUtf8CharSequence(out, message);

        byte[] bytes = serializer.encode(packet.getPayload());
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
