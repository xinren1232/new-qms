package com.transcend.plm.datadriven.core.route;

import com.transcend.plm.datadriven.common.exception.PlmBizException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 核心执行代理路由
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/6/15 17:50
 * @since 1.0
 */
@Slf4j
public class CoreProxyRouteInvocationHandler<T> implements InvocationHandler {

    // 目标代理缓存实例 TODO

    private Object target;

    public T bing(T target){
        this.target = target;
        return (T)Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        // 1.find event method config (analysis annotation  from method )

        // 2.do before event method
        try {
            // 3.do target method
            result = method.invoke(target, args);
        }catch (Exception e){
            /*Exception*/
            log.error("执行 方法 调用失败", e);
            if (e instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) e).getTargetException();
                if (targetException instanceof PlmBizException) {
                    throw (PlmBizException) targetException;
                }
                throw targetException;
            }

        }
        // 4.do after event method
        return result;
    }


}
