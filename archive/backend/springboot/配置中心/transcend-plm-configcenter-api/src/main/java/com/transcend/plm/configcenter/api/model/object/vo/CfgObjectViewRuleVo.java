package com.transcend.plm.configcenter.api.model.object.vo;

import com.transcend.plm.configcenter.api.model.object.dto.CfgPropertyMatchDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 对象视图参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 11:36
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象视图参数")
public class CfgObjectViewRuleVo implements Serializable {

    public static CfgObjectViewRuleVo of() {
        return new CfgObjectViewRuleVo();
    }

    @ApiModelProperty(value = "业务id", example = "00000001")
    private String bid;

    @ApiModelProperty(value = "视图bid", example = "00000001")
    private String viewBid;

    @ApiModelProperty(value = "角色类型", example = "1")
    private Byte roleType;

    @ApiModelProperty(value = "角色编码", example = "ALL")
    private List<String> roleCodes;

    @ApiModelProperty(value = "modelCode", example = "A04")
    private String modelCode;

    @ApiModelProperty(value = "字段匹配")
    private CfgPropertyMatchDto propertyMatchParams;

    @ApiModelProperty(value = "优先级", example = "1")
    private Integer priority;

    @ApiModelProperty(value = "描述", example = "ALL")
    private String description;

    @ApiModelProperty(value = "标签", example = "template")
    private LinkedHashSet<String> tags;

    @ApiModelProperty(value = "是否继承", example = "false")
    private Boolean inherit;

    private static final long serialVersionUID = 1L;

}
