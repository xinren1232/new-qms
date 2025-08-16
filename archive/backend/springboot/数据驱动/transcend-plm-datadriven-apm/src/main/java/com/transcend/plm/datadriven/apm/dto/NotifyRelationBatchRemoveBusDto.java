package com.transcend.plm.datadriven.apm.dto;

import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 批量新增异步事件处理类
 * @date 2024/04/23 10:11
 **/
@Data
@Accessors(chain = true)
public class NotifyRelationBatchRemoveBusDto  {
    /**
     * 对象编码
     */
    private String modelCode;
    /**
     * 业务数据
     */
    private CfgObjectRelationVo relationInfo;

    /**
     * 业务数据
     */
    RelationDelAndRemParamAo relationDelAndRemParamAo;

    public static NotifyRelationBatchRemoveBusDto of(String modelCode, CfgObjectRelationVo relationInfo, RelationDelAndRemParamAo data) {
        return new NotifyRelationBatchRemoveBusDto()
                .setModelCode(modelCode)
                .setRelationInfo(relationInfo)
                .setRelationDelAndRemParamAo(data);
    }
}
