package com.transcend.plm.datadriven.apm.space.pojo.vo;

import lombok.Data;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 泳道关系TAB
 * @createTime 2023-11-04 11:30:00
 */
@Data
public class ApmLaneTabVO {
    private String sourceAppBid;
    private String sourceModelCode;
    private String sourceAppName;
    private String relationModelCode;
    private String relationTabName;
    private String targetAppBid;
    private String targetModelCode;
    private String targetAppName;

    public String getRelationTabName() {
        if (this.relationTabName == null || this.relationTabName.isEmpty()) {
            return this.sourceAppName + "-" + this.targetAppName;
        }
        return relationTabName;
    }
}
