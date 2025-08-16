package com.transcend.plm.alm.demandmanagement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

/**
 * 模型编码
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/25 10:15
 */
@Getter
@AllArgsConstructor
public enum TranscendModel {
    /**
     * 原始需求
     */
    RR("A00"),
    /**
     * 初始需求
     */
    IR("A01"),
    /**
     * SR
     */
    SR("A02"),
    /**
     * AR
     */
    AR("A03"),
    /**
     * 领域组件
     */
    DOMAIN_COMPONENT("A0K"),

    /**
     * 领域
     */
    DOMAIN("A06"),

    /**
     * 模块
     */
    MODULE("A07"),
    /**
     * 需求管理责任人
     */
    RM_RESPONSIBLE_PERSON("A08"),
    /**
     * 责任田
     */
    DUTY_FIELD("A05"),
    /**
     * 责任田模块
     */
    DUTY_FIELD_MODULE("A0I"),

    /**
     * RR-RR关系
     */
    RELATION_RR_RR("A0B"),
    /**
     * RR-IR关系
     */
    RELATION_RR_IR("A0A"),
    /**
     * IR-IR关系
     */
    RELATION_IR_IR("A0C"),
    /**
     * IR-SR关系
     */
    RELATION_IR_SR("A0D"),
    /**
     * SR-AR关系
     */
    RELATION_SR_AR("A0E"),

    /**
     * 开发管理-IR关系
     */
    RELATION_DEVELOPMENT_IR("202"),

    /**
     * 项目管理
     */
    PROJECT("100"),

    /**
     * 开发管理
     */
    DEVELOPMENT("201"),
    /**
     * 全局系统特性
     */
    SF("A1AA00"),
    /**
     * 关联系统特性
     */
    RSF("A1AA01"),
    /**
     * Tos特性树——关联特性
     */
    RELATION_TOS_RSF("A1D"),

    /**
     * 关联特性——IR
     */
    RELATION_RSF_IR("A1L"),

    /**
     * IR-关联特性
     */
    RELATION_IR_RSF("A1N"),


    /**
     * tos版本特性树
     */
    TOS_VERSION_FEATURE_TREE("A1B"),

    /**
     * 交付版本 A1M
     */
    TOS_DELIVER_VERSION("A1M"),

    /**
     * 需求分类
     */
    REQUIREMENT_CLASSIFICATION("A1O"),
    ;

    /**
     * 模型编码
     */
    private final String code;


    /**
     * 匹配模型编码
     *
     * @param code      被匹配的编码
     * @param matchList 需要匹配的编码
     * @return 匹配结果
     */
    public static boolean matchCode(String code, TranscendModel... matchList) {
        if (StringUtils.isNotBlank(code) && matchList != null) {
            for (TranscendModel model : matchList) {
                if (model.code.equals(code)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    @Nullable
    public static TranscendModel fromCode(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (TranscendModel model : TranscendModel.values()) {
                if (model.code.equals(code)) {
                    return model;
                }
            }
        }
        return null;
    }
}
