package me.lalayu.protocol;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lalayu
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class RequestMessagePacket extends BaseMessagePacket{

    /**
     * 调用接口名
     */
    private String interfaceName;

    /**
     * 调用方法名
     */
    private String methodName;

    /**
     * 方法参数签名
     */
    private Class<?>[] parameterTypes;

    /**
     * 方法参数
     */
    private Object[] methodArguments;
}
