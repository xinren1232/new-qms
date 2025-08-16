package com.transcend.plm.configcenter.api.model.dictionary.vo;

import lombok.Data;

import java.util.Date;

@Data
public class CfgDictionaryDetail {
    private String name;
    private String code;
    private Integer enableFlag;
    private String updatedBy;
    private Date updatedTime;
    private String keyCode;
    private String zhValue;
    private String enValue;
}
