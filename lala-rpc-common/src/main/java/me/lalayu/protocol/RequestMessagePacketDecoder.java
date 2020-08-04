package me.lalayu.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author lalayu
 **/
public class RequestMessagePacketDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(RequestMessagePacketDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        RequestMessagePacket packet = new RequestMessagePacket();

        packet.baseDecoder(in);

        int interfaceNameLength = in.readInt();
        packet.setInterfaceName(in.readCharSequence(interfaceNameLength, ProtocolConstant.UTF_8).toString());

        int methodNameLength = in.readInt();
        packet.setMethodName(in.readCharSequence(methodNameLength, ProtocolConstant.UTF_8).toString());

        int methodArgumentSignatureArrayLength = in.readInt();
        if (methodArgumentSignatureArrayLength > 0) {
            String[] methodArgumentSignatures = new String[methodArgumentSignatureArrayLength];
            for (int i = 0; i < methodArgumentSignatureArrayLength; i++) {
                int methodArgumentSignatureLength = in.readInt();
                methodArgumentSignatures[i] = in.readCharSequence(methodArgumentSignatureLength, ProtocolConstant.UTF_8).toString();
            }
            packet.setMethodArgumentSignatures(methodArgumentSignatures);
        }

        int methodArgumentArrayLength = in.readInt();
        if (methodArgumentArrayLength > 0) {
            Object[] methodArguments = new Object[methodArgumentArrayLength];
            for (int i = 0; i < methodArgumentArrayLength; i++) {
                int byteLength = in.readInt();
                methodArguments[i] = in.readBytes(byteLength);
            }
            packet.setMethodArguments(methodArguments);
        }
        out.add(packet);
    }
}
