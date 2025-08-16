package com.transcend.plm.datadriven.apm.strategy;

import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;

/**
 *  获取详情参数转换器
 *  @author unknown
 */
public interface GetDetailParamTransformer {
    /**
     * 转换ApmSpaceApp的ModelCode
     *
     * @param apmSpaceApp 需要处理的ApmSpaceApp对象
     * @return 处理后的ModelCode
     */
    Boolean transform(ApmSpaceApp apmSpaceApp);
}
