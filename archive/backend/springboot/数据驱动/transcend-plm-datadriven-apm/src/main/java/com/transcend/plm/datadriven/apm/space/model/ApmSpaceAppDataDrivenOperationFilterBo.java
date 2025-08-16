package com.transcend.plm.datadriven.apm.space.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Set;

/**
 * 数据驱动操作过滤器业务对象
 * @author unknown
 */
@Data
@Accessors(chain = true)
public class ApmSpaceAppDataDrivenOperationFilterBo implements Serializable {

    /**
     * 忽视处理的关系modelCode
     */
    @ApiModelProperty(value = "忽视处理的关系modelCode")
    private Set<String> ignoreRelationModelCodes;

    /**
     * 视图类别
     */
    @ApiModelProperty(value = "忽视处理的角色人员")
    private Set<String> ignoreRoleUsers;


    public static ApmSpaceAppDataDrivenOperationFilterBo of() {
        return new ApmSpaceAppDataDrivenOperationFilterBo();
    }
}