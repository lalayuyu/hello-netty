package me.lalayu.server;

import lombok.Data;

/**
 * @author lalayu
 **/
@Data
public class HostClassMethodInfo {

    private Class<?> hostClass;

    private Class<?> hostUserClass;

    private Object hostTarget;
}
