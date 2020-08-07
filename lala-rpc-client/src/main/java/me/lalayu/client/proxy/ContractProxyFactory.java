package me.lalayu.client.proxy;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import me.lalayu.client.ResponseFuture;
import me.lalayu.client.discovery.ConnectManager;
import me.lalayu.client.ClientHandler;
import me.lalayu.protocol.MessageType;
import me.lalayu.protocol.ProtocolConstant;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.serializer.RpcSerializer;
import me.lalayu.serializer.impl.FastJsonSerializer;
import me.lalayu.utils.SerialNumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 *
 **/
@Slf4j
public class ContractProxyFactory implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractProxyFactory.class);

    private static final ConcurrentMap<Class<?>, Object> OBJECT_CACHE = Maps.newConcurrentMap();

    /* 对调用的future结果进行缓存 */
    static final ConcurrentMap<String, ResponseFuture> RESPONSE_FUTURE_TABLE= Maps.newConcurrentMap();

    private static final long REQUEST_TIMEOUT = 3000;
    private static final RpcSerializer SERIALIZER = FastJsonSerializer.S;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RequestMessagePacket request = createRequest(method, args);

//                 发起请求
        ClientHandler handler = ConnectManager.getInstance().chooseHandler();
        ResponseFuture responseFuture =  handler.sendRequest(request);
        return responseFuture.start();
    }

    private static RequestMessagePacket createRequest(Method method, Object[] args) {
        RequestMessagePacket packet = new RequestMessagePacket();

        packet.setMagicNumber(ProtocolConstant.MAGIC_NUMBER);
        packet.setVersion(ProtocolConstant.VERSION);
        packet.setSerialNumber(SerialNumberUtils.S.generateSerialNumber());
        packet.setMessageType(MessageType.REQUEST);
        packet.setInterfaceName(method.getDeclaringClass().getName());
        packet.setMethodName(method.getName());
        packet.setMethodArguments(args);

        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        packet.setParameterTypes(parameterTypes);
//                    Debug
        LOGGER.debug("interface name: " + method.getDeclaringClass().getName());
        LOGGER.debug("method name: " + method.getName());
        LOGGER.debug("parameterTypes: " + Arrays.toString(parameterTypes));
        LOGGER.debug("arguments: " + Arrays.toString(args));

        return packet;
    }



}


