package me.lalayu.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author lalayu
 **/
public class ResponseMessagePacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ResponseMessagePacket packet = new ResponseMessagePacket();

        packet.baseDecoder(in);

        packet.setStatusCode(in.readLong());

        int messageLength = in.readInt();
        packet.setMessage(in.readCharSequence(messageLength, ProtocolConstant.UTF_8).toString());

        int payLoadLength = in.readInt();
        packet.setPayload(in.readBytes(payLoadLength));
        out.add(packet);
    }
}
