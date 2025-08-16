package com.transcend.plm.configcenter.api.model.table.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 公用表
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2020/11/5 20:04
 */
@ApiModel(value = "公用表", description = "公用表")
@ToString
@Data
public class CfgTableAttributeVo{
    private static final long serialVersionUID = 1L;
    /** 生成类属性 */
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("业务id")
    private String bid;

    @ApiModelProperty("表配置业务id")
    private String tableBid;

    @ApiModelProperty("是否基础字段(1-是，0-否)")
    private Boolean baseFlag;

    @ApiModelProperty("数据库列")
    private String columnName;

    @ApiModelProperty("字段code")
    private String property;

    @ApiModelProperty("索引")
    private Boolean index;

    @ApiModelProperty("租户ID")
    private Long tenantId;

    @ApiModelProperty("创建人")
    private String createdBy;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty("数据类型")
    private String type;

    @ApiModelProperty("处理数据类型")
    private String handleType;

    @ApiModelProperty("默认值")
    private String defaultValue;

    @ApiModelProperty("描述")
    private String comment;

    @ApiModelProperty("是否必填")
    private Boolean required;
}
