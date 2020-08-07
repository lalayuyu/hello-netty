package me.lalayu.client;

import me.lalayu.client.discovery.ConnectManager;
import me.lalayu.client.discovery.ServiceDiscovery;
import me.lalayu.client.proxy.ContractProxyFactory;

import java.lang.reflect.Proxy;

/**
 *
 **/
public class RpcClient {

    private final ServiceDiscovery serviceDiscovery;

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public static <T> T createService(Class<T> interfaceClass) {
        return (T)Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ContractProxyFactory()
        );
    }

    public void stop() {
        serviceDiscovery.stop();
        ConnectManager.getInstance().stop();

    }
}
