package me.lalayu.server;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 **/
@Data
public class ArgumentConvertInput {

    private Method method;

    private List<Class<?>> parameterTypes;

    private List<Object> arguments;
}
