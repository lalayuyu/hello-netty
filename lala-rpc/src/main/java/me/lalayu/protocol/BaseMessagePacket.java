package me.lalayu.protocol;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.io.Serializable;
import java.net.PortUnreachableException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lalayu
 **/
@Data
public abstract class BaseMessagePacket implements Serializable {

    /**
     * 魔数
     */
    private int magicNumber;

    /**
     * 版本号
     */
    private int version;

    /**
     * 流水号
     */
    private String serialNumber;

    /**
     * 消息类型
     */
    private MessageType messageType;

    private Map<String, String> attachments = new HashMap<>();

    /**
     * 添加附件
     */
    public void addAttachment(String key, String value) {
        attachments.put(key, value);
    }

    /**
     * 对基础的协议头进行编码
     */
    public void baseEncode(ByteBuf out) {
        out.writeInt(magicNumber);
        out.writeInt(version);
        out.writeInt(getSerialNumber().length());
        out.writeCharSequence(getSerialNumber(), ProtocolConstant.UTF_8);
        out.writeByte(getMessageType().getType());

        out.writeInt(attachments.size());
        attachments.forEach((k, v) -> {
            out.writeInt(k.length());
            out.writeCharSequence(k, ProtocolConstant.UTF_8);
            out.writeInt(v.length());
            out.writeCharSequence(v, ProtocolConstant.UTF_8);
        });
    }

    /**
     * 对基础协议头进行解码
     */
    public void baseDecoder(ByteBuf in) {
        this.setMagicNumber(in.readInt());
        this.setVersion(in.readInt());
        int serialNumberLength = in.readInt();
        this.setSerialNumber(in.readCharSequence(serialNumberLength, ProtocolConstant.UTF_8).toString());
        byte messageTypeByte = in.readByte();
        this.setMessageType(MessageType.fromValue(messageTypeByte));

        Map<String, String> attachments = Maps.newHashMap();
        this.setAttachments(attachments);
        int attachmentsSize = in.readInt();
        if (attachmentsSize > 0) {
            for (int i = 0; i < attachmentsSize; i++) {
                int keyLength = in.readInt();
                String key = in.readCharSequence(keyLength, ProtocolConstant.UTF_8).toString();
                int valueLength = in.readInt();
                String value = in.readCharSequence(valueLength, ProtocolConstant.UTF_8).toString();
                attachments.put(key, value);
            }
        }
    }
}
