package me.lalayu.client;

import me.lalayu.contract.HelloService;

/**
 *
 **/
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "hello! " + name;
    }

    public static void main(String[] args) {
        HelloService helloService = (HelloService)TestDynamicProxy.newInstance(new HelloServiceImpl());
        System.out.println(helloService.sayHello("ycx"));
    }
}
