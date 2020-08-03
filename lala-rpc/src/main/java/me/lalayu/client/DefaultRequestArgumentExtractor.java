package me.lalayu.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import me.lalayu.server.MethodMatchResult;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 *
 **/
public class DefaultRequestArgumentExtractor implements RequestArgumentExtractor{

    private final ConcurrentMap<CacheKey, RequestArgumentExtractOutput> cache = Maps.newConcurrentMap();

    @Override
    public RequestArgumentExtractOutput extract(RequestArgumentExtractInput input) {
        Class<?> interfaceKlass = input.getInterfaceKlass();
        Method method = input.getMethod();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        return cache.computeIfAbsent(new CacheKey(interfaceKlass.getName(), methodName, Lists.newArrayList(parameterTypes)),
            x -> {
                RequestArgumentExtractOutput output = new RequestArgumentExtractOutput();
                output.setInterfaceName(interfaceKlass.getName());
                List<String> methodArgumentsSignatures = Lists.newArrayList();
                for (Class<?> klass : parameterTypes) {
                    methodArgumentsSignatures.add(klass.getName());
                }
                output.setMethodArgumentSignatures(methodArgumentsSignatures);
                output.setMethodName(methodName);
                return output;
            });
    }

    @RequiredArgsConstructor
    private static class CacheKey {
        private final String interfaceName;

        private final String methodName;

        private final List<Class<?>> parameterTypes;

        @Override
        public int hashCode() {
            return Objects.hash(interfaceName, methodName, parameterTypes);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            CacheKey cacheKey = (CacheKey)obj;
            return Objects.equals(interfaceName, cacheKey.interfaceName) &&
                    Objects.equals(methodName, cacheKey.methodName) &&
                    Objects.equals(parameterTypes, cacheKey.parameterTypes);
        }
    }
}
