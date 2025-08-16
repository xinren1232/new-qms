package com.transcend.plm.configcenter.api.model.objectrelation.qo;

import lombok.Data;

@Data
public class ObjectRelationRuleQo {
    /**
     * 关系对象modelCode
     */
    private String relationModelCode;
    /**
     * 目标modelCode
     */
    private String targetModelCode;
    /**
     * 提升之前生命周期状态
     */
    private String fromLifeCycleCode;
    /**
     * 提升之后生命周期状态
     */
    private String toLifeCycleCode;
    /**
     * 当前实例生命周期模板id
     */
    private String lcTemplateBid;
    /**
     * 当前实例生命周期模板版本
     */
    private String version;
}
