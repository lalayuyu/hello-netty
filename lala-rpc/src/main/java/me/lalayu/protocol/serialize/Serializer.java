package me.lalayu.protocol.serialize;

/**
 * @author lalayu
 */
public interface Serializer {

    byte[] encode(Object target);

    Object decode(byte[] bytes, Class<?> targetClass);
}
