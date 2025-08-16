package com.transcend.plm.datadriven.core.action;

import com.transcend.plm.datadriven.core.route.CoreProxyRouteInvocationHandler;

/**
 * 对象模型控制器
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/18 17:07
 * @since 1.0
 */

public abstract class AbstractModelController<I> {

    protected I getObjectModelStandardI() {
        CoreProxyRouteInvocationHandler<I> h = new CoreProxyRouteInvocationHandler<>();
        return h.bing(getAim());
    }

    /**
     * 获取目标对象模型。
     *
     * @return 目标对象模型
     */
    public abstract I getAim();

}
