package me.lalayu.server;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import me.lalayu.protocol.MessageType;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.protocol.ResponseMessagePacket;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 *
 **/
@Slf4j
@Component
//@ChannelHandler.Sharable
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ServerHandler extends SimpleChannelInboundHandler<RequestMessagePacket> {

    private final MethodMatcher methodMatcher;

    private final MethodArgumentConverter methodArgumentConverter;

    @Autowired
    public ServerHandler(MethodMatcher methodMatcher, MethodArgumentConverter converter) {
        this.methodMatcher = methodMatcher;
        this.methodArgumentConverter = converter;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessagePacket packet) throws Exception {
        log.info("服务端接受到:{}", packet);
        MethodMatchInfo matcherInput = new MethodMatchInfo();
        matcherInput.setInterfaceName(packet.getInterfaceName());
        matcherInput.setMethodName(packet.getMethodName());
        matcherInput.setMethodArgumentSignatures(Optional.ofNullable(packet.getMethodArgumentSignatures())
                                        .map(Lists::newArrayList).orElse(Lists.newArrayList()));
        Object[] methodArguments = packet.getMethodArguments();
        matcherInput.setMethodArgumentSize(null != methodArguments ? methodArguments.length : 0);

        MethodMatchResult matcherOutput = methodMatcher.selectBestMatchedMethod(matcherInput);
        log.info("查找目标实现方法成功,目标类:{},宿主类:{},宿主方法:{}",
                matcherOutput.getTargetClass().getCanonicalName(),
                matcherOutput.getTargetUserClass().getCanonicalName(),
                matcherOutput.getTargetMethod().getName()
        );

        Method targetMethod = matcherOutput.getTargetMethod();
        ArgumentConvertInput convertInput = new ArgumentConvertInput();
        convertInput.setArguments(matcherInput.getMethodArgumentSize() > 0 ? Lists.newArrayList(methodArguments) : Lists.newArrayList());
        convertInput.setMethod(matcherOutput.getTargetMethod());
        convertInput.setParameterTypes(matcherOutput.getParameterTypes());
        ArgumentConvertOutput convertOutput = methodArgumentConverter.convert(convertInput);
        ReflectionUtils.makeAccessible(targetMethod);

        Object result = targetMethod.invoke(matcherOutput.getTarget(), convertOutput.getArguments());

        ResponseMessagePacket response = createResponse(packet);
        response.setPayload(JSON.toJSONString(result));
        log.info("服务端输出:{}", JSON.toJSONString(response));
        ctx.writeAndFlush(response);
    }

    private ResponseMessagePacket createResponse(RequestMessagePacket packet) {
        ResponseMessagePacket response = new ResponseMessagePacket();
        response.setMagicNumber(packet.getMagicNumber());
        response.setVersion(packet.getVersion());
        response.setSerialNumber(packet.getSerialNumber());
        response.setAttachments(packet.getAttachments());
        response.setMessageType(MessageType.RESPONSE);
        response.setStatusCode(200L);
        response.setMessage("Success");
        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端出现异常", cause);
        super.exceptionCaught(ctx, cause);
    }
}
