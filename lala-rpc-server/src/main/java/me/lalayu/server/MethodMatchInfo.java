package me.lalayu.server;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author lalayu
 **/
@Data
@EqualsAndHashCode
public class MethodMatchInfo {
    /**
     * 调用接口名
     */
    private String interfaceName;

    /**
     * 调用方法名
     */
    private String methodName;

    /**
     * 调用的方法的签名
     */
    private List<String> methodArgumentSignatures;

    /**
     * 调用方法的参数
     */
    private int methodArgumentSize;

}
