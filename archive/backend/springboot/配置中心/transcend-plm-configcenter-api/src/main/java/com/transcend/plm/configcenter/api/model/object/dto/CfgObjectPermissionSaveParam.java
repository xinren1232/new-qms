package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.LinkedHashSet;

/**
 * 保存对象权限参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 16:50
 */
@Getter
@Setter
@ToString
@ApiModel(value = "保存对象权限参数")
public class CfgObjectPermissionSaveParam implements Serializable {

    @ApiModelProperty(value = "权限业务BID(关联权限的bid)", example = "234232423")
    private String bid;

    @ApiModelProperty(value = "对象类型（project,doc）", example = "1")
    @NotBlank(message = "对象类型不能为空！")
    private String baseModel;

    @ApiModelProperty(value = "权限绑定的对象业务id", example = "0394234234")
    @NotBlank(message = "对象BID不能为空！")
    private String objBid;

    @ApiModelProperty(value = "模型code")
    @NotBlank(message = "模型code不能为空！")
    private String modelCode;

    @ApiModelProperty(value = "角色类型（1-系统，0-业务）", example = "0-否，1-是")
    private Byte roleType;

    @ApiModelProperty(value = "角色表bid", example = "134232423")
    private String roleCode;

    @ApiModelProperty(value = "生命周期状态CODE(还有 ALL)", example = "ALL")
    @NotBlank(message = "生命周期状态不能为空！")
    private String lcStateCode;

    @ApiModelProperty(value = "操作权限code集合", example = "['ADD','EDIT']")
    @Size(min = 1, message = "操作权限至少选择一个！")
    private LinkedHashSet<String> operations;

    private static final long serialVersionUID = 1L;

}
