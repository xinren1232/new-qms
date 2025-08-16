package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author unknown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MObjectCopyAo {
    /**
     * 空间bid
     */
    private String spaceBid;

    /**
     * 源空间app bid
     */
    private String spaceAppBid;

    private String sourceBid;

    private String sourceDataBid;

    /**
     * 关系ModelCode
     */
    private String relationModelCode;

    /**
     * 需要定制化处理的ModelCode
     */
    private String targetModelCode;

    private String targetSpaceAppBid;

    private String sourceModelCode;

    private String copySourceBid;

    private List<String> attrs;
}
