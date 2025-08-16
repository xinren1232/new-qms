package com.transcend.plm.configcenter.view.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

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
@ApiModel("标准属性")
public class CfgViewVo implements Serializable {
    /**
     * 业务id
     */
    private String bid;

    /**
     * 编码
     */
    @ApiModelProperty(value = "类型", example = "country", required = true)
    private String type;

    /**
     * 客户端类型（web,app）
     */
    @ApiModelProperty(value = "客户端类型（web,app）", example = "country", required = true)
    private String clientType;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称", example = "项目视图", required = true)
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty(value = "description", example = "xxxx", required = false)
    private String description;

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
     * 视图内容
     */
    @ApiModelProperty(value = "视图内容", example = "{}", required = false)
    private Map<String, Object> content;

    /**
     * 使用状态，禁用2，启用1，未启用0(默认0)
     */
    private Integer enableFlag;

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

    private static final long serialVersionUID = 1L;
}
