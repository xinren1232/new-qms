package com.transcend.plm.configcenter.api.model.view.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
public class CfgViewDto implements Serializable {

    /**
     * 业务id
     */
    private String bid;

    /**
     * 所属bid
     */
    private String belongBid;

    /**
     * 视图作用域
     */
    private String viewScope;

    /**
     * 编码
     */
    @ApiModelProperty(value = "类型", example = "country", required = true)
    private String type;

    @ApiModelProperty(value = "视图类型", example = "1", required = true)
    private String viewType;

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
     * 优先级
     */
    @ApiModelProperty(value = "优先级", example = "1", required = false)
    private Integer priority;

    /**
     * 视图内容
     */
    @ApiModelProperty(value = "视图内容", example = "{}", required = false)
    private Map<String, Object> content;

    /**
     * 视图内容元数据
     */
    @ApiModelProperty(value = "视图内容元数据", example = "{}", required = false)
    private List<CfgViewMetaDto> metaList;


    /**
     * 使用状态，禁用2，启用1，未启用0
     */
    @ApiModelProperty(value = "使用状态，禁用2，启用1，未启用0", example = "0", required = false)
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

    /**
     * 复制视图1-1,（viewBidMap，viewBatchCopyMap）两者同时传入时，按照viewBatchCopyMap执行
     */
    private Map<String,String> viewBidMap;

    /**
     * 复制视图1-n （viewBidMap，viewBatchCopyMap）两者同时传入时，按照viewBatchCopyMap执行
     */
    private Map<String,List<String>> viewBatchCopyMap;
}
