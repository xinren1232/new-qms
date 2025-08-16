package com.transcend.plm.configcenter.api.model.objectrelation.qo;

import lombok.Data;

@Data
public class CfgObjectRelationQo {
    /**
     * 关系名称
     */
    private String name;

    /**
     * tab名称
     */
    private String tabName;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Integer enableFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Integer deleteFlag;

    /**
     * 源对象编码
     */
    private String sourceModelCode;

    /**
     * 目标对象编码
     */
    private String targetModelCode;

    /**
     * 关系表自身编码
     */
    private String modelCode;
}
