package com.transcend.plm.datadriven.apm.extension.service;

import com.transcend.plm.configcenter.api.model.view.dto.RelationInfo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description XPM KPI业务特殊逻辑处理
 * @date 2024/04/23 13:50
 **/
public interface XpmKpiLogicService {

    /**
     * 计算指标得分，以及指标得分的计算逻辑
     *
     * @param relationInfo 关系信息
     * @param sourceBid    源数据业务ID
     */
    void collectItemScore(RelationInfo relationInfo, String sourceBid);

    /**
     * 更新实例数据判断是否从新计算指标得分
     *
     * @param modelCode     对象编码
     * @param bid           数据业务ID
     * @param mSpaceAppData 实例数据
     */
    void updateInsThenIsCollect(String modelCode, String bid, MSpaceAppData mSpaceAppData);

}
