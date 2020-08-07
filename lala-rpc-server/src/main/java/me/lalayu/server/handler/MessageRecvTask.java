package me.lalayu.server.handler;

import me.lalayu.exception.MethodMatchException;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.protocol.ResponseMessagePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 **/
public class MessageRecvTask implements Callable<Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRecvTask.class);

    private RequestMessagePacket request;
    private ResponseMessagePacket response;
    private final Map<String, Object> serviceMap;

    public MessageRecvTask(Map<String, Object> serviceMap, RequestMessagePacket request, ResponseMessagePacket response) {
        this.request = request;
        this.response = response;
        this.serviceMap = serviceMap;
    }

    @Override
    public Boolean call() throws Exception {
        LOGGER.info("Receive request id: {}", request.getSerialNumber());
        response.setSerialNumber(request.getSerialNumber());
        try {
            Object result = handle(request);
            response.setStatusCode(200L);
            response.setMessage("success");
            response.setPayload(result);
        } catch (Throwable t) {
            response.setStatusCode(500L);
            response.setMessage(t.toString());
            LOGGER.error("error handling the request", t);
        }
        return Boolean.TRUE;
    }

    private Object handle(RequestMessagePacket request) {
        String interfaceName = request.getInterfaceName();
        Object serviceBean = serviceMap.get(interfaceName);
        if (serviceBean == null) {
            LOGGER.error("Can not find the service named{}", interfaceName);
            return null;
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getMethodArguments();

        LOGGER.debug(serviceClass.getName());
        LOGGER.debug(methodName);
        for (Class<?> clazz : parameterTypes) {
            LOGGER.debug(clazz.getName());
        }
        for (Object parameter : parameters) {
            LOGGER.debug(parameter.toString());
        }

        Method targetMethod = ReflectionUtils.findMethod(serviceClass, methodName, parameterTypes);
        if (targetMethod != null) {
            return ReflectionUtils.invokeMethod(targetMethod, serviceBean, parameters);
        } else {
            LOGGER.error("can not find target method name:{}, parameterTypes:{}", methodName, Arrays.toString(parameterTypes));
            throw new MethodMatchException("target method not found!");
        }

    }
}
