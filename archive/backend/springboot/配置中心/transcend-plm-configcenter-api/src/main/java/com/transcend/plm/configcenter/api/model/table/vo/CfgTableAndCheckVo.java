package com.transcend.plm.configcenter.api.model.table.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/** 从CfgTableDto拷贝属性*/
@ApiModel("对象信息")
@Data
public class CfgTableAndCheckVo{
    @ApiModelProperty("业务ID")
    private String bid;

    @ApiModelProperty("表内容")
    private String comment;

    @ApiModelProperty("逻辑表名称")
    private String logicTableName;

    @ApiModelProperty("逻辑表后缀")
    private String tableNameSuffix;

    @ApiModelProperty("策略编码（NONE,HASH,COLUMN,TIME）")
    private String strategyCode = "NONE";

    @ApiModelProperty("分片字段")
    private String shardColumn;

    @ApiModelProperty("分片数量")
    private String tableNum;

    @ApiModelProperty("策略规则")
    private String rule;

    @ApiModelProperty("租户")
    private Long tenantId;

    @ApiModelProperty("租户编码")
    private String tenantCode;

    @ApiModelProperty("直接使用表名")
    private Byte useLogicTableName = 0;

    /**
     * 未启用0，启用1，禁用2
     */
    private Integer enableFlag;

    private List<CfgTableAttributeVo> attributes;
}