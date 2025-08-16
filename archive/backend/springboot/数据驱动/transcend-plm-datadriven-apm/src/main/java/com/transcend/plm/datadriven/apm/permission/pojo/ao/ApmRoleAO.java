package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 角色AO
 * @createTime 2023-09-20 15:01:00
 */
@Data
public class ApmRoleAO {
    private Integer id;

    /**
     *
     */
    private String bid;

    /**
     * 父级bid
     */
    private String pbid;

    /**
     * 编码
     */
    private String code;


    /**
     * 类型
     */
    private String type;

    /**
     * 名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String name;

    /**
     * 父级bid
     */
    private String parentBid;

    /**
     * 角色来源(自定义/系统内置)
     */
    private String roleOrigin;

    /**
     * 域id
     */
    private String sphereBid;

    /**
     * 业务BID
     */
    @ApiModelProperty(value = "* 业务bid: 空间时，为bid;对象时，为SpaceAppId(应用Bid);对象实例时，为bid")
    private String bizBid;

    /**
     * bizType
     */
    @ApiModelProperty(value = "类型 :space,空间;object,对象;instance,对象实例")
    private String bizType;

    /**
     * 空间下对象实例bid
     */
    private String instanceBid;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 更新人
     */
    private String updatedBy;
}
