package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("视图规则匹配入参")
public class CfgViewRuleMatchDto implements Serializable {

    @ApiModelProperty(value = "角色类型", example = "1")
    private Byte roleType;

    @ApiModelProperty(value = "角色编码", example = "ALL")
    private Set<String> roleCodeSet;
    /**
     * 视图bid
     */
    @ApiModelProperty(value = "视图bid", example = "xxx", required = false)
    private String viewBid;

    /**
     * 对象code
     */
    @ApiModelProperty(value = "对象code", example = "xxx", required = false)
    private String modelCode;

    /**
     * tag
     */
    @ApiModelProperty(value = "tag", example = "tag", required = true)
    private String tag;


    /**
     * 生命周期code
     */
    @ApiModelProperty(value = "生命周期code", example = "{}", required = false)
    private String lcStateCode;

    private static final long serialVersionUID = 1L;
}
