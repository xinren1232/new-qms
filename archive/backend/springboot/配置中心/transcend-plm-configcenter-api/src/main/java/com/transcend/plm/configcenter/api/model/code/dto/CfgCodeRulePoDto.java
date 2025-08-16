package com.transcend.plm.configcenter.api.model.code.dto;

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
public class CfgCodeRulePoDto extends BaseDto {

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 是否已被绑定
     */
    private boolean binding;
}
