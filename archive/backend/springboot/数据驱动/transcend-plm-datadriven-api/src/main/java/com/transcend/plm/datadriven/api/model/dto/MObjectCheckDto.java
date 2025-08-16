package com.transcend.plm.datadriven.api.model.dto;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.relation.qo.CrossRelationPathChainQO;
import lombok.Data;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class MObjectCheckDto {
    private ModelMixQo modelMixQo;
    private String sourceBid;
    private String relationModelCode;

}
