package com.transcend.plm.datadriven.apm.space.pojo.qo;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmAppViewConfigQo {
    /**
     * 空间BID
     */
    private String spaceBid;

    /**
     * 应用BID
     */
    private String spaceAppBid;


    /**
     * Tab的BID
     */
    private String tabBid;
}
