package com.transcend.plm.datadriven.apm.dto;

import com.transcend.plm.configcenter.api.model.view.dto.RelationInfo;
import com.transcend.plm.datadriven.api.model.MObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 批量新增异步事件处理类
 * @date 2024/04/23 10:11
 **/
@Data
@Accessors(chain = true)
public class NotifyRelationBatchAddBusDto  {
    /**
     * 对象编码
     */
    private String modelCode;

    private RelationInfo relationInfo;
    /**
     * 业务数据
     */
    private List<MObject> data;

    public static NotifyRelationBatchAddBusDto of(String modelCode, RelationInfo relationInfo, List<MObject> data) {
        return new NotifyRelationBatchAddBusDto()
                .setModelCode(modelCode)
                .setRelationInfo(relationInfo)
                .setData(data);
    }
}
