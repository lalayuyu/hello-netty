package me.lalayu.server;

/**
 * @author lalayu
 */
public interface MethodArgumentConverter {
    /**
     * @param input 解包后的字节数据
     * @return 转化之后的结果
     */
    ArgumentConvertOutput convert(ArgumentConvertInput input);
}
