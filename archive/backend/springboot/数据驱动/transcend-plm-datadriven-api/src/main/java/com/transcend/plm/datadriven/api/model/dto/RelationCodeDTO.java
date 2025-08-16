package com.transcend.plm.datadriven.api.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author unknown
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "关系实体", description = "关系实体")
public class RelationCodeDTO {

    private String superiorCode;
    private String associatedCode;
    private String superiorStructureBid;
    private String associatedStructureBid;
    private String sourceKey;
    private String sourceValue;
    private String targetKey;
    private String targetValue;
}
