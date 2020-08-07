package me.lalayu.server.services;

import me.lalayu.server.annotation.RpcService;
import me.lalayu.services.HelloService;

/**
 * 默认实现的打招呼类
 **/
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return String.format("Hello %s, Welcome to the lala-rpc server", name);
    }
}
