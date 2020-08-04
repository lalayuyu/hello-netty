package me.lalayu.serializer.impl;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.common.collect.Maps;
import me.lalayu.serializer.RpcSerializer;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;

public enum ProtostuffSerializer implements RpcSerializer {
    /**
     * 单例模式
     */
    S;


    private static final Map<Class<?>, Schema<?>> CACHED_SCHEMA = Maps.newConcurrentMap();

    private static Objenesis objenesis = new ObjenesisStd(true);


    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        return (Schema<T>) CACHED_SCHEMA.computeIfAbsent(cls, RuntimeSchema::createFrom);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> byte[] serialize(T target) {
        Class<T> cls = (Class<T>) target.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(target, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> targetClass) {
        try {
            T data = (T) objenesis.newInstance(targetClass);
            Schema<T> schema = getSchema(targetClass);
            ProtostuffIOUtil.mergeFrom(bytes, data, schema);
            return data;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
