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
@TableName(value ="sr_0919_tt")
@Data
@Accessors(chain = true)
public class TonesSr implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户需求ID
     */
    @TableField(value = "ir_old_code")
    private String irOldCode;

    /**
     * 创建时间
     */
    @TableField(value = "old_code")
    private String oldCode;

    /**
     * 需求名称
     */
    @TableField(value = "title")
    private String title;

    /**
     * 需求详细描述
     */
    @TableField(value = "product_requirement_description")
    private String productRequirementDescription;

    /**
     * 来源渠道
     */
    @TableField(value = "project_differentiation_explanation")
    private String projectDifferentiationExplanation;

    /**
     * 来源国家
     */
    @TableField(value = "child_domain")
    private String childDomain;

    /**
     * 版本号/型号
     */
    @TableField(value = "moudule")
    private String moudule;

    /**
     * OS版本号
     */
    @TableField(value = "responsibility_field")
    private String responsibilityField;

    /**
     * 品牌
     */
    @TableField(value = "responsibility_field_owner")
    private String responsibilityFieldOwner;

    /**
     * 产品线
     */
    @TableField(value = "responsible_field_deputy_field_owner")
    private String responsibleFieldDeputyFieldOwner;

    /**
     * 机型
     */
    @TableField(value = "created_by")
    private String createdBy;

    /**
     * Android版本号
     */
    @TableField(value = "person_responsible")
    private String personResponsible;

    /**
     *反馈人数
     */
    @TableField(value = "created_time")
    private Date createdTime;


    /**
     * JIRA编号
     */
    @TableField(value = "estimated_testing_workload")
    private String estimatedTestingWorkload;



}