package com.transcend.plm.configcenter.api.model.object.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author:jie.luo
 * 权限操作
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
public class CfgObjectOperationVo {
    private String code;
    @ApiModelProperty(value = "显示名称", example = "创建")
    private String name;
    @ApiModelProperty(value = "对象类型", example = "doc")
    private String baseModel;

    public static CfgObjectOperationVo of(){
        return new CfgObjectOperationVo();
    }

    private static final long serialVersionUID = 1L;
}
