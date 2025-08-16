package com.transcend.plm.datadriven.apm.permission.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author unknown
 */
@Data
@Accessors(chain = true)
public class PermissionCheckDto {
    /**
     * 空间bid
     */
    private String spaceBid;
    /**
     * 空间应用bid
     */

    private String spaceAppBid;
    /**
     * 操作编码
     */
    private String operatorCode;
    /**
     * 实例bid
     */

    private String instanceBid;
}
