package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Data
public class ApmSpaceAppTabDto {
    /**
     * 空间应用业务id
     */
    private String spaceAppBid;

    /**
     * 目标空间应用业务id
     */
    private String targetSpaceAppBid;

    /**
     * 关系对象modelCode
     */
    private String relationModelCode;

    /**
     * tab名称
     */
    private String name;
    /**
     * tab名称
     */
    private String tabName;

    /**
     * 过滤条件
     */
    private ModelMixQo configContent;

    @ApiModelProperty(value = "是否显示条件")
    private List<ModelFilterQo> showConditionContent;

    private List<Map> multiTreeContent;

    /**
     * 显示 视图模式
     */
    @ApiModelProperty(value = "显示 视图模式")
    private List<String> showViewModels;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 前端组件组件类型（关系-RELATION）
     */
    private String component;

    /**
     *
     */
    private String code;

    /**
     *
     */
    private String viewModelCode;

    private boolean deleteFlag;

    /**
     * 是否每次切换都从新加载数据
     */
    private Boolean isRefresh;

    private List<MultiAppConfigDto> multiAppConfigDtos;

    private List<RelationActionPermission> relationActionPermissionList;
}
