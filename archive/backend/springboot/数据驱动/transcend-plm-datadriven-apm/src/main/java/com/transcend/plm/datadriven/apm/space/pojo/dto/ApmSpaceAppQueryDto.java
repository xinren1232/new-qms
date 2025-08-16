package com.transcend.plm.datadriven.apm.space.pojo.dto;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmSpaceAppQueryDto {
    /**
     * 当前空间bid
     */
    private String nowSpaceBid;

    /**
     * 需要查询的应用modelCode
     */
    private String appModelCode;

    /**
     * 当前实例bid
     */
    private String instanceBid;
}
