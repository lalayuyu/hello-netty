package me.lalayu.server;

import com.google.common.collect.Maps;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.lalayu.serializer.impl.HessianSerializer;
import me.lalayu.server.annotation.RpcService;
import me.lalayu.server.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Optional;

/**
 *
 **/
public class RpcSever implements ApplicationContextAware, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerExecutor.class);

    private String serverAddress;
    private static final String DELIMITER = ":";
    private ServiceRegistry serviceRegistry;

    private final Map<String, Object> serviceMap = Maps.newConcurrentMap();

    public RpcSever(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcSever(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> tempServiceMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (!tempServiceMap.isEmpty()) {
            for (Object bean : tempServiceMap.values()) {
                String interfaceName = bean.getClass().getAnnotation(RpcService.class).value().getName();
                serviceMap.put(interfaceName, bean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    //    netty服务端的启动
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer(serviceMap, HessianSerializer.S))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] ipAddress = serverAddress.split(DELIMITER);
            if (ipAddress.length == 2) {
                String host = ipAddress[0];
                int serverPort = Integer.parseInt(ipAddress[1]);
                ChannelFuture future = bootstrap.bind(host, serverPort).sync();

                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            serviceRegistry.registerService(serverAddress);
                            LOGGER.info("lala-Rpc Sever registered successfully");
                            LOGGER.info("lala-Rpc Server start success on port {}", serverPort);
                        }
                    }
                });
                future.channel().closeFuture().sync();
            }

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                LOGGER.error("the server remoting stopped");
            } else {
                LOGGER.error("unexpected error!");
            }
        } finally {
            serviceRegistry.unregisterService();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void stop() {
        ServerExecutor.shutDown();
    }

    public Map<String, Object> getServiceMap() {
        return serviceMap;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }
}
