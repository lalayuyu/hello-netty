package me.lalayu.protocol.serialize;


import com.alibaba.fastjson.JSON;

/**
 * @author lalayu
 **/
public enum  SerializerImpl implements Serializer {
    /**
     * 利用枚举实现单例模式
     */
    S;


    @Override
    public byte[] encode(Object target) {
        return JSON.toJSONBytes(target);
    }

    @Override
    public Object decode(byte[] bytes, Class<?> targetClass) {
        return JSON.parseObject(bytes, targetClass);
    }
}
