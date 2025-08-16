package com.transcend.plm.datadriven.apm.space.pojo.dto;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmSpaceAppCopyDto {
    @ApiParam("oldSpaceBid")
    String oldSpaceBid;
    @ApiParam("newSpaceBid")
    String newSpaceBid;
    @ApiParam("newSpaceAppBid")
    String newSpaceAppBid;
    @ApiParam("oldSpaceAppBid")
    String oldSpaceAppBid;
}
