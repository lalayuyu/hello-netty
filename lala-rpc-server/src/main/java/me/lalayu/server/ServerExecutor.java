package me.lalayu.server;

import com.google.common.util.concurrent.*;
import io.netty.channel.*;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.protocol.ResponseMessagePacket;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 *
 **/
public class ServerExecutor  {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerExecutor.class);

    private static final ListeningExecutorService THREAD_POOL_EXECUTOR = MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(3, 6, 0, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(1024), new ThreadFactoryBuilder().setNameFormat("RPC-%d").build()));


//    private ServerExecutor() {
//    }

    /**
     * 利用内部静态类实现单例模式
     */
//    private static class ServerExecutorHolder {
//        static final ServerExecutor INSTANCE = new ServerExecutor();
//    }
//
//    public static ServerExecutor getInstance() {
//        return ServerExecutorHolder.INSTANCE;
//    }

    public static void submit(Callable<Boolean> task, final ChannelHandlerContext ctx,
                              final RequestMessagePacket request, final ResponseMessagePacket response) {

        ListenableFuture<Boolean> listenableFuture = THREAD_POOL_EXECUTOR.submit(task);
        Futures.addCallback(listenableFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable Boolean result) {
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        LOGGER.info("server send the response of requestId:{}", request.getSerialNumber());
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, THREAD_POOL_EXECUTOR);
    }

    public static void shutDown() {
        THREAD_POOL_EXECUTOR.shutdown();
    }


}
