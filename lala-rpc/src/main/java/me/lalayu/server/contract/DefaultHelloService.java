package me.lalayu.server.contract;

import me.lalayu.contract.HelloService;
import org.springframework.stereotype.Service;

/**
 *
 **/
@Service
public class DefaultHelloService implements HelloService {

    @Override
    public String sayHello(String name) {
        return String.format("hello %s", name);
    }
}
