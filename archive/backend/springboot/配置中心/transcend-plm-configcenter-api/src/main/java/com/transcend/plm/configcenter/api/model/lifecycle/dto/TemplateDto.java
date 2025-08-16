package com.transcend.plm.configcenter.api.model.lifecycle.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TemplateDto implements Serializable {

    /**
     * 模板bid
     */
    private String templateBid;

    /**
     * 版本
     */
    private String version;

    /**
     * 对象模型编码
     */
    private String modelCode;

    private String sourceBid;

    /**
     * 当前生命周期编码
     */
    private String currentLifeCycleCode;
}
