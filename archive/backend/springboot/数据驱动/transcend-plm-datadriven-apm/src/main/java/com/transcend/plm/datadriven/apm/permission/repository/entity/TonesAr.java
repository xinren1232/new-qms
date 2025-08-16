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
@TableName(value ="Ar_0919_tt")
@Data
@Accessors(chain = true)
public class TonesAr implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户需求ID
     */
    @TableField(value = "sr_old_code")
    private String srOldCode;

    /**
     * 生命周期编码(状态)
     */
    @TableField(value = "old_code")
    private String oldCode;

    /**
     * 创建时间
     */
    @TableField(value = "title")
    private String title;

    /**
     * 需求名称
     */
    @TableField(value = "product_requirement_description")
    private String productRequirementDescription;

    /**
     * 需求详细描述
     */
    @TableField(value = "child_domain")
    private String childDomain;

    /**
     * 来源渠道
     */
    @TableField(value = "module")
    private String module;

    /**
     * 来源国家
     */
    @TableField(value = "responsibility_field")
    private String responsibilityField;

    /**
     * 版本号/型号
     */
    @TableField(value = "rd_module")
    private String rdModule;

    /**
     * OS版本号
     */
    @TableField(value = "responsibility_field_owner")
    private String responsibilityFieldOwner;

    /**
     * 品牌
     */
    @TableField(value = "created_by")
    private String createdBy;

    /**
     * 产品线
     */
    @TableField(value = "person_responsible")
    private String personResponsible;

    /**
     * 机型
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * Android版本号
     */
    @TableField(value = "estimatedrdworkload")
    private String estimatedrdworkload;


}