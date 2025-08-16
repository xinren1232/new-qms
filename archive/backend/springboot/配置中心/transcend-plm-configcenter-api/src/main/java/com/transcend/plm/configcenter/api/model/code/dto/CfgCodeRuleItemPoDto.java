package com.transcend.plm.configcenter.api.model.code.dto;

import com.alibaba.fastjson.JSONObject;
import com.transcend.plm.configcenter.api.model.base.BaseDto;
import lombok.Data;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
@Data
public class CfgCodeRuleItemPoDto extends BaseDto {

    private String codeRuleBid;
    private String name;
    private String description;
    private String type;
    private JSONObject value;
    private String beforeSeparator;
    private String afterDelimiter;
    private Integer sort;
    private static final long serialVersionUID = 1L;
}
