package com.transcend.plm.configcenter.api.model.object.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author:jie.luo
 * 权限-对象表
 */
@Setter
@Getter
@Accessors(chain = true)
@ApiModel("权限详情VO")
public class CfgObjectPermissionVo extends BasePermission implements Serializable{

    /**
     * 业务id
     */
    @ApiModelProperty(value = "权限业务BID(关联权限的bid)", example = "234232423")
    private String bid;

    /**
     * 权限绑定的业务id
     */
    @ApiModelProperty(value = "权限绑定的对象业务id", example = "0394234234")
    private String objBid;
    /**
     * 基类对象类型
     */
    @ApiModelProperty(value = "对象类型（project,doc）", example = "1")
    private String baseModel;

    /**
     * modelCode
     */
    private String modelCode;
    /**
     * 角色类型（1-系统，0-业务）
     */
    @ApiModelProperty(value = "角色类型（1-系统，0-业务）", example = "0-否，1-是")
    private Byte roleType;
    /**
     * 角色表bid
     */
    @ApiModelProperty(value = "角色表bid", example = "134232423")
    private String roleCode;

    /**
     * 是否继承
     */
    @ApiModelProperty(value = "是否继承", example = "false")
    private Boolean inherit;

    public static CfgObjectPermissionVo of(){
        return new CfgObjectPermissionVo();
    }

    private static final long serialVersionUID = 1L;
}