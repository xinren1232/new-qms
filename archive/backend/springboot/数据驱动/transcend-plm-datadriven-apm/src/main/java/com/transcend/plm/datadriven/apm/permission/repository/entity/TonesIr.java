package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.datadriven.api.model.vo.FileVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author unknown
 * @TableName apm_role_identity
 */
@TableName(value ="ir_0919_tt")
@Data
@Accessors(chain = true)
public class TonesIr implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户需求ID
     */
    @TableField(value = "old_code")
    private String oldCode;

    /**
     * 生命周期编码(状态)
     */
    @TableField(value = "title")
    private String title;

    /**
     * 创建时间
     */
    @TableField(value = "requirement_description")
    private String requirementDescription;

    /**
     * 需求名称
     */
    @TableField(value = "demand_origin")
    private String demandOrigin;

    /**
     * 需求详细描述
     */
    @TableField(value = "product_line")
    private String productLine;

    /**
     * 来源渠道
     */
    @TableField(value = "value_direction")
    private String valueDirection;

    /**
     * 来源国家
     */
    @TableField(value = "demand_priority")
    private String demandPriority;

    /**
     * 版本号/型号
     */
    @TableField(value = "experience_proposition")
    private String experienceProposition;

    /**
     * OS版本号
     */
    @TableField(value = "product_area")
    private String productArea;

    /**
     * 品牌
     */
    @TableField(value = "child_product_area")
    private String childProductArea;

    /**
     * 模块
     */
    @TableField(value = "module")
    private String module;

    /**
     * 产品线
     */
    @TableField(value = "created_by")
    private String createdBy;

    /**
     * 分发时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 价值评估（评估结果）
     */
    @TableField(value = "person_responsible")
    private String personResponsible;

    /**
     * 价值评估时间(评估时间)
     */
    @TableField(value = "files")
    private String files;

    /**
     * 价值评估人(评估人)
     */
    @TableField(value = "auth_desc")
    private String authDesc;

    /**
     * 价值评估人(评估人)
     */
    @TableField(exist = false)
    private String requirementcoding;

}