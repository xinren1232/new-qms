package com.transcend.plm.configcenter.api.model.lifecycle.qo;

import lombok.Data;

@Data
public class CfgLifeCycleTemplateNodeQo {
    /**
     * 生命周期模板id
     */
    private String templateBid;

    /**
     * 版本号
     */
    private String version;

    /**
     * 生命周期状态编码
     */
    private String lifeCycleCode;
}
