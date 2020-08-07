package me.lalayu.test.client;

import me.lalayu.client.RpcClient;
import me.lalayu.client.discovery.ServiceDiscovery;
import me.lalayu.services.HelloService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 **/
public class RpcClientTest {
    public static void main(String[] args) throws InterruptedException{
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
        final RpcClient rpcClient = new RpcClient(serviceDiscovery);

        int threadNum = 5;
        final int requestNum = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        Thread[] threads = new Thread[threadNum];
        long startTime = System.currentTimeMillis();

        //benchmark for sync call
        for (int i = 0; i < threadNum; ++i) {
//            threadPool.execute(() -> {
//                for (int j = 0; j < requestNum; j++) {
//                    HelloService helloService = RpcClient.createService(HelloService.class);
//                    String result = helloService.sayHello(String.valueOf(j));
//                    System.out.println(result + j);
//                    try {
//                        Thread.sleep(20 * 1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
            threads[i] = new Thread(()->{
                for (int j = 0; j < requestNum; j++) {
                    HelloService helloService = RpcClient.createService(HelloService.class);
                    String result = helloService.sayHello(String.valueOf(j));
                    System.out.println(result + j);
                    try {
                        Thread.sleep(20 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < threadNum; i++) {
            threads[i].join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Sync call total-time-cost:%sms, req/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        rpcClient.stop();
    }
}
