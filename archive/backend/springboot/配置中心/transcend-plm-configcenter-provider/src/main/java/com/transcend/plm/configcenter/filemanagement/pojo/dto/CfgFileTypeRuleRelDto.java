package com.transcend.plm.configcenter.filemanagement.pojo.dto;

import lombok.Data;

import java.util.List;

@Data

public class CfgFileTypeRuleRelDto {
    private String ruleBid;
    private List<String> fileTypeBids;

    private String fileTypeBid;
    private List<String> ruleBids;

    // 操作类型 add, delete

    private String operateType;
}
