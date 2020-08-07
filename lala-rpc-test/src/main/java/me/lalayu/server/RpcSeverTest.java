package me.lalayu.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 **/
public class RpcSeverTest {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("rpc-server.xml");
    }
}
