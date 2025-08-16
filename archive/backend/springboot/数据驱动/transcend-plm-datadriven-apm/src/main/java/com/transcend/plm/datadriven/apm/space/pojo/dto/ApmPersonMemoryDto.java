package com.transcend.plm.datadriven.apm.space.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 用户个性设置
 * @date 2023/10/11 11:36
 **/
public class ApmPersonMemoryDto {

    /**
     * 主键ID
     */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Long id;
    /**
     * 业务id
     */
    @NotBlank(message="[业务id]不能为空")
    @Size(max= 32,message="编码长度不能超过32")
    @ApiModelProperty("业务id")
    @Length(max= 32,message="编码长度不能超过32")
    private String bid;
    /**
     * 空间业务id
     */
    @NotBlank(message="[空间业务id]不能为空")
    @Size(max= 32,message="编码长度不能超过32")
    @ApiModelProperty("空间业务id")
    @Length(max= 32,message="编码长度不能超过32")
    private String spaceBid;
    /**
     * 空间应用业务id
     */
    @NotBlank(message="[空间应用业务id]不能为空")
    @Size(max= 32,message="编码长度不能超过32")
    @ApiModelProperty("空间应用业务id")
    @Length(max= 32,message="编码长度不能超过32")
    private String spaceAppBid;
    /**
     * 视图类别
     */
    @Size(max= 66,message="编码长度不能超过66")
    @ApiModelProperty("视图类别")
    @Length(max= 66,message="编码长度不能超过66")
    private String category;
    /**
     * 标识
     */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("标识")
    @Length(max= 255,message="编码长度不能超过255")
    private String code;
    /**
     * 工号
     */
    @Size(max= 66,message="编码长度不能超过66")
    @ApiModelProperty("工号")
    @Length(max= 66,message="编码长度不能超过66")
    private String jobNumber;
    /**
     * 姓名
     */
    @Size(max= 66,message="编码长度不能超过66")
    @ApiModelProperty("姓名")
    @Length(max= 66,message="编码长度不能超过66")
    private String jobName;
    /**
     * 视图内容
     */
    @ApiModelProperty("视图内容")
    private Object content;
    /**
     * 删除标志
     */
    @ApiModelProperty("删除标志")
    private Boolean deleteFlag;
    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    @ApiModelProperty("启用标志，0未启用，1启用，2禁用")
    private Integer enableFlag;
    /**
     * 创建时间
     */
    @NotNull(message="[创建时间]不能为空")
    @ApiModelProperty("创建时间")
    private Date createdTime;
    /**
     * 更新时间
     */
    @NotNull(message="[更新时间]不能为空")
    @ApiModelProperty("更新时间")
    private Date updatedTime;
    /**
     * 创建人
     */
    @NotBlank(message="[创建人]不能为空")
    @Size(max= 32,message="编码长度不能超过32")
    @ApiModelProperty("创建人")
    @Length(max= 32,message="编码长度不能超过32")
    private String createdBy;
    /**
     * 更新人
     */
    @NotBlank(message="[更新人]不能为空")
    @Size(max= 32,message="编码长度不能超过32")
    @ApiModelProperty("更新人")
    @Length(max= 32,message="编码长度不能超过32")
    private String updatedBy;
    /**
     * 租户ID
     */
    @NotNull(message="[租户ID]不能为空")
    @ApiModelProperty("租户ID")
    private Integer tenantId;
    /**
     *
     */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("")
    @Length(max= 255,message="编码长度不能超过255")
    private String description;
}
