package me.lalayu.client;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import me.lalayu.contract.HelloService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 *
 **/
public class TestDynamicProxy implements InvocationHandler{

private final Object obj;

    public static Object newInstance(Object obj) {
       return Proxy.newProxyInstance(
               obj.getClass().getClassLoader(),
               obj.getClass().getInterfaces(),
               new TestDynamicProxy(obj));
    }

    TestDynamicProxy(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        try {
            System.out.println("方法调用前:");
            System.out.println(String.format("[%s$%s]方法被调用,参数列表: %s", obj.getClass().getName(), method.getName(), JSON.toJSONString(args)));
            result = method.invoke(obj, args);
        } finally {
            System.out.println("方法调用完毕");
        }
        return result;
    }
}
