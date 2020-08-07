package me.lalayu.serializer.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import me.lalayu.serializer.RpcSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public enum HessianSerializer implements RpcSerializer {
    /**
     * 单例模式
     */
    S;


    @Override
    public <T> byte[] serialize(T target) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);
        try {
            out.startMessage();
            out.writeObject(target);
            out.completeMessage();
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            bos.close();
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> targetClass) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        Hessian2Input in = new Hessian2Input(bin);

        try {
            in.startMessage();
            return in.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            in.close();
            bin.close();
        }
    }
}
