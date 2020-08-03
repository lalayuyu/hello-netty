package me.lalayu.server;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lalayu
 **/
@Slf4j
public abstract class BaseMethodMatcher implements MethodMatcher{

    private final ConcurrentMap<MethodMatchInfo, MethodMatchResult> cache = Maps.newConcurrentMap();

    @Override
    public MethodMatchResult selectBestMatchedMethod(MethodMatchInfo input) {
        return cache.computeIfAbsent(input, in -> {
           try {
               MethodMatchResult output = new MethodMatchResult();
               Class<?> interfaceClass = Class.forName(in.getInterfaceName());
               HostClassMethodInfo info = findHostClassMethodInfo(interfaceClass);
               List<Method> targetMethods = Lists.newArrayList();

//             根据请求当中的参数内容,返回对应对象的Class
//             这里的in回调主要是访问input当中的methodArgumentSignatures信息
               List<Class<?>> inputParameterTypes = Optional.ofNullable(in.getMethodArgumentSignatures()).map( mas -> {
                   List<Class<?>> list = Lists.newArrayList();
                   mas.forEach(ma -> {
                       list.add(ClassUtils.resolveClassName(ma, null));
                   });
                   return list;
               }).orElse(Lists.newArrayList());


//             doWithMethods主要是获取宿主类(接口)的中的方法,结果使用MethodFilter进行过滤, 选取合适的方法
               ReflectionUtils.doWithMethods(info.getHostUserClass(), targetMethods::add, method -> {
                   String methodName = method.getName();
                   Class<?> declaringClass = method.getDeclaringClass();
                   List<Class<?>> parameterTypes = Lists.newArrayList(method.getParameterTypes());

//                   获取名字和类的基本匹配情况
                   boolean leastMatch = Objects.equals(methodName, in.getMethodName()) &&
                                        Objects.equals(info.getHostUserClass(), declaringClass);

//                   有参数签名则匹配参数签名,否则匹配参数数量,若都不匹配,则按照基本匹配来
                   if (!inputParameterTypes.isEmpty()) {
                       return leastMatch && Objects.equals(parameterTypes, inputParameterTypes);
                   } else if (in.getMethodArgumentSize() > 0){
                       return leastMatch && parameterTypes.size() == in.getMethodArgumentSize();
                   } else {
                       return leastMatch;
                   }

               });

               if (targetMethods.size() != 1) {
                   throw new MethodMatchException(String.format("查找到目标方法数量不等于1,interface:%s,method:%s",
                           in.getInterfaceName(), in.getMethodName()));
               }
               Method targetMethod = targetMethods.get(0);
               output.setTargetMethod(targetMethod);
               output.setParameterTypes(inputParameterTypes);
               output.setTargetClass(info.getHostClass());
               output.setTargetUserClass(info.getHostUserClass());
               output.setTarget(info.getHostTarget());
               return output;

           } catch (Exception e) {
               log.error("未找到匹配度最高的方法,输入参数:{}", JSON.toJSONString(in) , e);
               if (e instanceof MethodMatchException) {
                   throw (MethodMatchException) e;
               } else {
                   throw new MethodMatchException(e);
               }
           }
        });
    }

    /**
     * 获取宿主类的信息
     */
    abstract HostClassMethodInfo findHostClassMethodInfo(Class<?> interfaceClass);
}
