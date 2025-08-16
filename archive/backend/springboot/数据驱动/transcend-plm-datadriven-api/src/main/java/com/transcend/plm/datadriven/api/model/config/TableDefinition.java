package com.transcend.plm.datadriven.api.model.config;

import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 表定义
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@Getter
@Setter
@Accessors(chain = true)
public class TableDefinition {

    @ApiModelProperty("业务ID")
    private String bid;

    @ApiModelProperty("逻辑表名称")
    private String logicTableName;

    @ApiModelProperty("逻辑表后缀")
    private String tableNameSuffix;

    @ApiModelProperty("策略编码")
    private String strategyCode;

    @ApiModelProperty("分片字段")
    private String shardColumn;

    @ApiModelProperty("分片数量")
    private String shardNum;

    @ApiModelProperty("策略规则")
    private String rule;

    @ApiModelProperty("租户")
    private Long tenantId;

    @ApiModelProperty("租户编码")
    private String tenantCode;

    @ApiModelProperty("直接使用表名")
    private Byte useLogicTableName;

    private List<TableAttributeDefinition> tableAttributeDefinitions;

    private List<TableAttributeDefinition> fullTableAttributeDefinitions;

    @ApiModelProperty("模型编码")
    private String modelCode;

    /**
     * @return {@link TableDefinition }
     */
    public static TableDefinition of(){
        TableDefinition tableDefinition = new TableDefinition();
        // 默认为不分表
        return tableDefinition.setStrategyCode(DataBaseConstant.tableStrategyNone);
    }

}
