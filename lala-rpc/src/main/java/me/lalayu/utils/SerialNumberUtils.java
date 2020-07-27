package me.lalayu.utils;

import java.util.UUID;

/**
 *
 **/
public enum SerialNumberUtils {
    /**
     * 单例
     */
    S;

    public String generateSerialNumber() {
            return UUID.randomUUID().toString().replace("-", "");
        }
}
