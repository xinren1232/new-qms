package com.transcend.plm.configcenter.code.infrastructure.repository.po;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-02 09:28
 **/
@Data
@TableName(value = "cfg_code_rule_item", autoResultMap = true)
public class CfgCodeRuleItemPo extends BasePoEntity implements Serializable {
    private String codeRuleBid;
    private String name;
    private String description;
    private String type;
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject value;
    private String beforeSeparator;
    private String afterDelimiter;
    private Integer sort;
    private static final long serialVersionUID = 1L;

}
