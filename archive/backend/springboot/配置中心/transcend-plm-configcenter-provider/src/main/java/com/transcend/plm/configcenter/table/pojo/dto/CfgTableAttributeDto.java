package com.transcend.plm.configcenter.table.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 
 * 表属性定义
 */
@ToString
@Data
@Accessors(chain = true)
public class CfgTableAttributeDto implements Serializable {
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

    @ApiModelProperty("是否索引")
    private Boolean index;

    @ApiModelProperty("租户ID")
    private Long tenantId;

    @ApiModelProperty("创建人")
    private String createdBy;

    @ApiModelProperty("创建时间")
    private Date createdTime;

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

    public static CfgTableAttributeDto of() {
        return new CfgTableAttributeDto();
    }
}