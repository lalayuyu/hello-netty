package me.lalayu.server;

/**
 * @author lalayu
 **/
public interface MethodMatcher {

    /**
     * 根据输入参数寻找一个最合适的方法
     * @param input 调用的方法相关信息
     * @return 匹配的方法
     */
    MethodMatchResult selectBestMatchedMethod(MethodMatchInfo input);

}
