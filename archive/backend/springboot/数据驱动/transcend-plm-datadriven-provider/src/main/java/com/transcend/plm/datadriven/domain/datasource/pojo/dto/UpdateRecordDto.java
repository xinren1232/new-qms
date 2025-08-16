package com.transcend.plm.datadriven.domain.datasource.pojo.dto;

import lombok.Data;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class UpdateRecordDto {
    private String bid;
    private String modelCode;
    private String tableName;
    private String updateContent;
    private String jobNumber;
}
