package com.transcend.plm.datadriven.apm.dto;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transsion.framework.dto.BaseRequest;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ProjectSetQueryDto {

    /**
     * 源bid
     */
    private String sourceInstanceBid;

    /**
     *关系modelCode
     */
    private String relationModelCode;

    /**
     * 分页信息
     */
    private BaseRequest<ModelMixQo> pageInfo;

    /**
     * 需求查询信息
     */
    private ModelMixQo modelMixQo;
}
