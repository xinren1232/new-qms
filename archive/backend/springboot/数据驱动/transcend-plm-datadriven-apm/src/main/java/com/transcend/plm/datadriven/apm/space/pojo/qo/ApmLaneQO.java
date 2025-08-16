package com.transcend.plm.datadriven.apm.space.pojo.qo;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import lombok.Data;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 泳道查询对象
 * @createTime 2023-11-04 15:58:00
 */
@Data
public class ApmLaneQO {
    /**
     * 从关系进入时，需要传入进入数据的bid
     */
    private String fromInstanceBid;
    /**
     * 从关系进入时，需要传入关系的modelCode
     */
    private String fromRelationModelCode;

    private String sourceAppBid;
    private String sourceModelCode;
    private String relationModelCode;
    private String targetAppBid;
    private String targetModelCode;

    private ModelMixQo mixQo;
}
/**
 * 生成jsons示例
 */
