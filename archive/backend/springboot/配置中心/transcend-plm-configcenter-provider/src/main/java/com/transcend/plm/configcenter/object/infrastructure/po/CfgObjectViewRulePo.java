package com.transcend.plm.configcenter.object.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.transcend.plm.configcenter.api.model.object.dto.CfgPropertyMatchDto;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author 权限-对象表
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
@TableName(value = "cfg_object_view_rule", autoResultMap = true)
public class CfgObjectViewRulePo extends BasePoEntity {


    /**
     * 视图bid
     */
    private String viewBid;

    /**
     * 对象业务id
     */
    private String objBid;

    /**
     * 模型code
     */
    private String modelCode;

    /**
     * 对象类型（project-项目，task-任务，doc-文档）
     */
    private String baseModel;

    /**
     * 角色类型（1-系统，0-业务）
     */
    private Byte roleType;

    /**
     * 角色表bid
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> roleCodes;

    /**
     * 字段匹配参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private CfgPropertyMatchDto propertyMatchParams;

    /**
     * 描述
     */
    private String description;

    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 标签
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> tags;


    private static final long serialVersionUID = 1L;

    public static CfgObjectViewRulePo of() {
        return new CfgObjectViewRulePo();
    }
}
