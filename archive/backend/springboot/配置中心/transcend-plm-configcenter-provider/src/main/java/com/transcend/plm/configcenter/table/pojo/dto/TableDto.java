package com.transcend.plm.configcenter.table.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class TableDto {
    @ApiModelProperty("表名称")
    private String tableName;
    @ApiModelProperty("根据cloumnDtos转换成数据库可识别的列信息")
    private List<String> createCloumns;
    @ApiModelProperty("描述")
    private String tableDesc;
    @ApiModelProperty("列信息")
    private List<CloumnDto> cloumnDtos;
    @ApiModelProperty("需要创建索引的字段信息")
    private List<CloumnDto> cloumnIndexs;
    @ApiModelProperty("索引信息")
    private List<String> createIndexs;
}
