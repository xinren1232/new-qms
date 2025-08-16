package com.transcend.plm.datadriven.apm.event.handler.base;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;

/**
 * @author bin.yin
 * @description: transcend事件处理接口
 * @version:
 * @date 2024/06/13 13:44
 */
public interface EventHandler<T extends EventHandlerParam, R> extends Ordered {


    /**
     * 初始化入参
     * @param args aop 获取的方法入参
     * @return T
     */
    T initParam(Object[] args);


    /**
     * 前置
     * @param param 入参
     * @return 入参
     */
    default T preHandle(T param) {
        return param;
    }

    /**
     * 后置
     * @param param 入参
     * @param result 返回结果
     * @return 返回结果
     */
    default R postHandle(T param, R result) {
        return result;
    }

    /**
     * 匹配是否执行
     * @param param 入参
     * @return true:匹配上需要执行; false 匹配不上不需要执行
     */
    boolean isMatch(T param);
}
