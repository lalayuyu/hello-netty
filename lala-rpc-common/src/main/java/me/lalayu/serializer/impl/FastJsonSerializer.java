package me.lalayu.serializer.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import me.lalayu.serializer.RpcSerializer;

/**
 * @author lalayu
 **/
public enum FastJsonSerializer implements RpcSerializer {
    /**
     * 利用枚举实现单例模式
     */
    S;


    @Override
    public <T> byte[] serialize(T target) {
        return JSON.toJSONBytes(target, SerializerFeature.SortField);
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> targetClass) {
        return JSON.parseObject(bytes, targetClass, Feature.SortFeidFastMatch);
    }
}
