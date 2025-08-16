package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto;

import com.baomidou.mybatisplus.extension.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@ApiModel("生命周期状态")
@Data
public class LifeCycleStateDto implements Serializable {
    /**业务id
     **/
    @ApiModelProperty("业务id")
    private String bid;
    /**
     * 编码
     */
    @ApiModelProperty("编码")
    private String code;

    /**
     * 颜色
     */
    @ApiModelProperty("颜色")
    private String color;

    /**
     * 名称
     */
    @ApiModelProperty("名称")
    private String name;


    /**
     * 所属组编码
     */
    @ApiModelProperty("所属组编码")
    private String groupCode;


    /**
     * 说明
     */
    @ApiModelProperty("说明")
    private String description;


    /**
     * 是否被绑定，0未被绑定，1被绑定
     */
    @ApiModelProperty(value = "bindingFlag",example = "true 被绑定 false 未被绑定")
    private Boolean bindingFlag;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标识
     */
    private Integer deleteFlag;

    /**
     * 状态（启用标志，0未启用，1启用，2禁用）
     */
    private Integer enableFlag;

    private Long id;
}
