package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName(value ="transcend_model_rr_20240722_tt")
@Data
@Accessors(chain = true)
public class TonesRr implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户需求ID
     */
    @TableField(value = "tones_id")
    private String tonesId;

    /**
     * 生命周期编码(状态)
     */
    @TableField(value = "life_cycle_code")
    private String lifeCycleCode;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 需求名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 需求详细描述
     */
    @TableField(value = "demand_desc")
    private String demandDesc;

    /**
     * 来源渠道
     */
    @TableField(value = "source_channel")
    private String sourceChannel;

    /**
     * 来源国家
     */
    @TableField(value = "source_country")
    private String sourceCountry;

    /**
     * 版本号/型号
     */
    @TableField(value = "version_number_model")
    private String versionNumberModel;

    /**
     * OS版本号
     */
    @TableField(value = "banbenno")
    private String banbenno;

    /**
     * 品牌
     */
    @TableField(value = "brand")
    private String brand;

    /**
     * 产品线
     */
    @TableField(value = "product_line")
    private String productLine;

    /**
     * 机型
     */
    @TableField(value = "model")
    private String model;

    /**
     * Android版本号
     */
    @TableField(value = "android_ver_no")
    private String androidVerNo;

    /**
     *反馈人数
     */
    @TableField(value = "number_of_feedback")
    private Integer numberOfFeedback;


    /**
     * JIRA编号
     */
    @TableField(value = "jira_code")
    private String jiraCode;

    /**
     * 优先级
     */
    @TableField(value = "priority")
    private String priority;

    /**
     * 提报人
     */
    @TableField(value = "reported_by")
    private String reportedBy;

    /**
     * 原始提出人
     */
    @TableField(value = "original_proposer")
    private String originalProposer;

    /**
     * 小模块
     */
    @TableField(value = "xiao_mo_kuai")
    private String xiaoMoKuai;

    /**
     * 需求类型分类
     */
    @TableField(value = "classification_of_demand_types")
    private String classificationOfDemandTypes;

    /**
     * 需求类型分类子项
     */
    @TableField(value = "requirement_type_classification_sub_items")
    private String requirementTypeClassificationSubItems;

    /**
     * 新功能子项
     */
    @TableField(value = "new_feature_sub_items")
    private String newFeatureSubItems;

    /**
     * 需求创建人
     */
    @TableField(value = "created_by")
    private String createdBy;

    /**
     * 运营备注
     */
    @TableField(value = "yun_ying_bei_zhu")
    private String yunYingBeiZhu;

    /**
     * 分发人
     */
    @TableField(value = "fen_fa_ren")
    private String fenFaRen;

    /**
     * 分发时间
     */
    @TableField(value = "fen_fa_shi_jian")
    private Date fenFaShiJian;

    /**
     * 价值评估（评估结果）
     */
    @TableField(value = "value_assessment")
    private String valueAssessment;

    /**
     * 价值评估时间(评估时间)
     */
    @TableField(value = "value_assessment_time")
    private Date valueAssessmentTime;

    /**
     * 价值评估人(评估人)
     */
    @TableField(value = "value_assessment_people")
    private String valueAssessmentPeople;

    /**
     * 评估不通过原因(评估拒绝原因)
     */
    @TableField(value = "reason_for_failure_in_evaluation")
    private String reasonForFailureInEvaluation;

    /**
     * 评估不通过原因说明（评估拒绝原因说明）
     */
    @TableField(value = "explanation_of_reasons_for_failure_ln_evaluation")
    private String explanationOfReasonsForFailureLnEvaluation;

    /**
     * 需求预估排期（计划完成时间）
     */
    @TableField(value = "estimated_demand_scheduling")
    private Date estimatedDemandScheduling;

    /**
     * 预计导入项目
     */
    @TableField(value = "yujidaoyuxiangmu")
    private String yujidaoyuxiangmu;

    /**
     * 预计发布的OS版本
     */
    @TableField(value = "yujifabuosbanben")
    private String yujifabuosbanben;

    /**
     * 预计发布独立应用版本号
     */
    @TableField(value = "yujifabubanbenhao")
    private String yujifabubanbenhao;


    /**
     * 验证环境-已导入的整机版本号
     */
    @TableField(value = "zhengjibanbenhao")
    private String zhengjibanbenhao;

    /**
     * 验证环境-导入项目
     */
    @TableField(value = "daoruxiangmu")
    private String daoruxiangmu;
    /**
     * 是否通过（验证结果）
     */
    @TableField(value = "passed_or_not")
    private String passedOrNot;
    /**
     * verifier
     */
    @TableField(value = "verifier")
    private String verifier;
    /**
     * verify_time
     */
    @TableField(value = "verify_time")
    private Date verifyTime;
    /**
     * 问题说明（验证不通过原因）
     */
    @TableField(value = "problem_description")
    private String problemDescription;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(value = "files", typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<FileVO> files;

    /**
     * fileResult
     */
    @TableField(value = "file_result")
    private String fileResult;

    /**
     * descResult
     */
    @TableField(value = "desc_result")
    private String descResult;


    /**
     * Tone产品经理
     */
    @TableField(value = "tones_product_manager")
    private String tonesProductManager;

    /**
     * RR编码
     */
    @TableField(value = "requirementcoding")
    private String requirementcoding;

    /**
     * 不通过原因类型（最近一次驳回原因）
     */
    @TableField(value = "reason_type_f_or_failure")
    private String reasonTypeFOrFailure;

    /**
     * 申诉评估不通过原因说明（退回原因补充说明）
     */
    @TableField(value = "explanation_of_reasons_for_failure_to_pass")
    private String explanationOfReasonsForFailureToPass;

    /**
     * 评论（留言）
     */
    @TableField(value = "comment")
    private String comment;


}