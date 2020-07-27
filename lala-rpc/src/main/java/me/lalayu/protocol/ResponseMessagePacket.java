package me.lalayu.protocol;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lalayu
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class ResponseMessagePacket extends BaseMessagePacket {

    /**
     * 返回的状态代码
     */
    private Long statusCode;

    /**
     * 返回的消息
     */
    private String message;

    /**
     * 消息载荷
     */
    private Object payload;
}
