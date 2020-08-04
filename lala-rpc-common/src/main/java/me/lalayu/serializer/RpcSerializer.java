package me.lalayu.serializer;

import java.io.IOException;

/**
 * @author lalayu
 */
public interface RpcSerializer {

    <T> byte[] serialize(T target) throws IOException;

    <T> Object deserialize(byte[] bytes, Class<T> targetClass) throws IOException;
}
