package me.lalayu.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import me.lalayu.protocol.serialize.Serializer;

/**
 * @author lalayu
 **/
@RequiredArgsConstructor
public class RequestMessagePacketEncoder extends MessageToByteEncoder<RequestMessagePacket> {

    private final Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, RequestMessagePacket packet, ByteBuf out) throws Exception {
        packet.baseEncode(out);

        out.writeInt(packet.getInterfaceName().length());
        out.writeCharSequence(packet.getInterfaceName(), ProtocolConstant.UTF_8);

        out.writeInt(packet.getMethodName().length());
        out.writeCharSequence(packet.getMethodName(), ProtocolConstant.UTF_8);

        if (packet.getMethodArgumentSignatures() != null) {
            int len = packet.getMethodArgumentSignatures().length;
            out.writeInt(len);
            for (int i = 0; i < len; i++) {
                String signature = packet.getMethodArgumentSignatures()[i];
                out.writeInt(signature.length());
                out.writeCharSequence(signature, ProtocolConstant.UTF_8);
            }
        } else {
            out.writeInt(0);
        }

        if (packet.getMethodArguments() != null) {
            int len = packet.getMethodArguments().length;
            out.writeInt(len);
            for (int i = 0; i < len; i++) {
                byte[] bytes = serializer.encode(packet.getMethodArguments()[i]);
                out.writeInt(bytes.length);
                out.writeBytes(bytes);
            }
        } else {
            out.writeInt(0);
        }
    }
}
