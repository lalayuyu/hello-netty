package me.lalayu.client.discovery;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.lalayu.client.ClientHandler;
import me.lalayu.client.ClientInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 **/
public class ConnectManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectManager.class);

    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 6,
            600L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1024),
            new ThreadFactoryBuilder().setNameFormat("Rpc-client-thread-%d").build());
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(3);

    private ConcurrentMap<String, ClientHandler> connectedServerNodes = Maps.newConcurrentMap();

    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    private final long WAIT_TIME_OUT = 5000;
    private final AtomicInteger roundRobin = new AtomicInteger(0);
    private volatile boolean isRunning = true;

    private ConnectManager() {}

    //    use static inner class to implement Singleton
    private static class ConnectManagerHolder {
        private static final ConnectManager INSTANCE = new ConnectManager();
    }

    public static ConnectManager getInstance() {
        return ConnectManagerHolder.INSTANCE;
    }

    public void updateConnectSever(List<String> serverNodeList) {
        if (null == serverNodeList) {
            return;
        }

        if (!serverNodeList.isEmpty()) {
            Set<String> serverNodeSet = Sets.newHashSet(serverNodeList);

//                try to connect the free serverNode
            for (final String address : serverNodeSet) {
                if (!connectedServerNodes.containsKey(address)) {
                    connectServer(address);
                }
            }

//                remove the dead serveNode from Map
            for (String address : connectedServerNodes.keySet()) {
                if (!serverNodeSet.contains(address)) {
                    LOGGER.info("Service on {} is dead!", address);
                    ClientHandler handler = connectedServerNodes.get(address);
                    if (handler != null) {
                        handler.close();
                    }
                    connectedServerNodes.remove(address);
                }
            }
        } else {
            LOGGER.error("Cannot find available server.");
            for (String address : connectedServerNodes.keySet()) {
                ClientHandler handler = connectedServerNodes.get(address);
                if (handler != null) {
                    handler.close();
                }
                connectedServerNodes.remove(address);
            }
        }
    }

    private void connectServer(String address) {
        String[] ipAddr = address.split(":");
        if (ipAddr.length != 3) {
            LOGGER.error("Invalid connect address!");
            return;
        }
        LOGGER.info("connect to the service, UUID:{}, ip:{}, port:{}", ipAddr[0], ipAddr[1], ipAddr[2]);
        int port = Integer.parseInt(ipAddr[2]);
        final InetSocketAddress server = new InetSocketAddress(ipAddr[1], port);
        threadPoolExecutor.execute(() -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .remoteAddress(server)
                    .handler(new ClientInitializer());

            ChannelFuture future = bootstrap.connect();
            future.addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    LOGGER.info("Successfully connect to server on {}", server.toString());
                    ClientHandler handler = future1.channel().pipeline().get(ClientHandler.class);
                    addHandler(address, handler);
                }
            });
        });
    }

    private void addHandler(String address, ClientHandler handler) {
        connectedServerNodes.put(address, handler);
        signalOtherHandler();
    }

    private void signalOtherHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            LOGGER.warn("Waiting for available service");
            connected.await(this.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public ClientHandler chooseHandler() {
        int size = connectedServerNodes.values().size();
        while (isRunning && size <= 0) {
            try {
                waitingForHandler();
                size = connectedServerNodes.values().size();
            } catch (InterruptedException e) {
                LOGGER.error("Waiting for available node is interrupted!", e);
            }
        }
        int index = (roundRobin.getAndAdd(1) + size) % size;
        List<ClientHandler> connectedHandlers = new ArrayList<>(connectedServerNodes.values());
        return connectedHandlers.get(index);
    }

    public void stop() {
        isRunning = false;
        for (String address : connectedServerNodes.keySet()) {
            ClientHandler handler = connectedServerNodes.get(address);
            handler.close();
            connectedServerNodes.remove(address);
        }
        signalOtherHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
