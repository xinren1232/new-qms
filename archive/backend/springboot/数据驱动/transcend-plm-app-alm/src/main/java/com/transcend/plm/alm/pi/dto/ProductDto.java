package com.transcend.plm.alm.pi.dto;

import lombok.Data;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024-10-14
 * @description 产品实体
 */
@Data
public class ProductDto {
    /**产品ID*/
    private String id;
    /**产品名称*/
    private String productName;
    /**产品简称*/
    private String productShortName;
    /**产品英文名称*/
    private String productEnName;
    /**产品介绍*/
    private String description;

    /**创建人**/
    private String createBy;
    /**更新人**/
    private String updatedBy;

    /**产品状态 1=开发中 2=使用中 3=计划下线 4=已下线*/
    private String status;
    /**产品类型 1=自研 2=开源 3=外购(有源码) 4=外购(Saas) 5=外购(无源码)*/
    private String type;
    /**产品头像*/
    private String avatar;
    /**租户ID*/
    private String tenantId;
    /**产品线关联Id*/
    private String fieldId;
    /**产品线编码*/
    private String fieldCode;

    /**IT项目名称*/
    private String itProjectName;
    /**产品域名消息*/
    private List<DomainDto> domains;
    /**产品成员信息*/
    private List<MemberDto> members;




}
