package me.lalayu.client;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import me.lalayu.exception.SendRequestException;
import me.lalayu.protocol.MessageType;
import me.lalayu.protocol.ProtocolConstant;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.protocol.ResponseMessagePacket;
import me.lalayu.protocol.serialize.Serializer;
import me.lalayu.protocol.serialize.SerializerImpl;
import me.lalayu.utils.ByteBufferUtils;
import me.lalayu.utils.SerialNumberUtils;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 *
 **/
@Slf4j
public class ContractProxyFactory {

    private static final RequestArgumentExtractor EXTRACTOR = new DefaultRequestArgumentExtractor();


    private static final ConcurrentMap<Class<?>, Object> OBJECT_CACHE = Maps.newConcurrentMap();

    /* 对调用的future结果进行缓存 */
    static final ConcurrentMap<String, ResponseFuture> RESPONSE_FUTURE_TABLE= Maps.newConcurrentMap();

    private static final long REQUEST_TIMEOUT = 3000;
    private static final ExecutorService EXECUTOR;
    private static final ScheduledExecutorService CLIENT_HOUSE_KEEPER;
    private static final Serializer SERIALIZER = SerializerImpl.S;

    @SuppressWarnings("unchecked")
    public static <T> T ofProxy(Class<T> interfaceKlass) {
        return (T) OBJECT_CACHE.computeIfAbsent(interfaceKlass, x ->
                Proxy.newProxyInstance(interfaceKlass.getClassLoader(), new Class[]{interfaceKlass}, (target, method, args) -> {
                    RequestArgumentExtractInput input = new RequestArgumentExtractInput();
                    input.setInterfaceKlass(interfaceKlass);
                    input.setMethod(method);
                    RequestArgumentExtractOutput output = EXTRACTOR.extract(input);

                    RequestMessagePacket packet = new RequestMessagePacket();
                    packet.setMagicNumber(ProtocolConstant.MAGIC_NUMBER);
                    packet.setVersion(ProtocolConstant.VERSION);
                    packet.setSerialNumber(SerialNumberUtils.S.generateSerialNumber());
                    packet.setMessageType(MessageType.REQUEST);
                    packet.setInterfaceName(output.getInterfaceName());
                    packet.setMethodName(output.getMethodName());
                    packet.setMethodArgumentSignatures(output.getMethodArgumentSignatures().toArray(new String[0]));
                    packet.setMethodArguments(args);
                    Channel channel = ClientChannelHolder.CHANNEL_REFERENCE.get();
//                 发起请求
                    channel.writeAndFlush(packet);
                    return String.format("[%s#%s]调用成功,发送了[%s]到NettyServer[%s]", output.getInterfaceName(),
                            output.getMethodName(), JSON.toJSONString(packet), channel.remoteAddress());
                }));
    }

    static Object sendRequestSync(Channel channel, RequestMessagePacket packet, Class<?> returnType) {
        long beginTimeStamp = System.currentTimeMillis();
        ResponseFuture responseFuture = new ResponseFuture(packet.getSerialNumber(), REQUEST_TIMEOUT);
        RESPONSE_FUTURE_TABLE.put(packet.getSerialNumber(), responseFuture);
        try {
            Future<ResponseMessagePacket> packetFuture = EXECUTOR.submit(() -> {
                channel.writeAndFlush(packet).addListener(
                        (ChannelFutureListener)future -> responseFuture.setSendRequestSucceed(true)
                );
                return responseFuture.waitResponse(REQUEST_TIMEOUT - (System.currentTimeMillis() - beginTimeStamp));
            });
            ResponseMessagePacket response = packetFuture.get(
                    REQUEST_TIMEOUT - (System.currentTimeMillis() - beginTimeStamp), TimeUnit.MILLISECONDS);
            if (null == response) {
                throw new SendRequestException(String.format("获取ResponseMessagePacket超时,请求ID: %s", packet.getSerialNumber()));
            } else {
                ByteBuf payload =(ByteBuf) response.getPayload();
                byte[] bytes = ByteBufferUtils.S.readBytes(payload);
                return SERIALIZER.decode(bytes, returnType);
            }
        } catch (Exception e) {
            log.error("同步发送请求失败,最后尝试发送的请求:{}", JSON.toJSONString(packet), e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new SendRequestException(e);
            }
        }
    }

    static void scanResponseFutureTable() {
        log.info("开始清理ResponseFutureTable中的任务......");
        Iterator<Map.Entry<String, ResponseFuture>> iterator = RESPONSE_FUTURE_TABLE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ResponseFuture> entry = iterator.next();
            ResponseFuture responseFuture = entry.getValue();
            if (responseFuture.timeout()) {
                iterator.remove();
                log.warn("请求{}已过期", entry.getKey());
            }
        }
        log.info("清理完成");
    }

    static {
        int n = Runtime.getRuntime().availableProcessors();
        EXECUTOR = new ThreadPoolExecutor(n * 2, n *2, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(64), runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("CLIENT_REQUEST_EXECUTOR");
            return thread;
        });
        CLIENT_HOUSE_KEEPER = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("CLIENT_HOUSE_KEEPER");
            return thread;
        });
        CLIENT_HOUSE_KEEPER.scheduleWithFixedDelay(ContractProxyFactory::scanResponseFutureTable, 5, 5, TimeUnit.SECONDS);
    }
}


