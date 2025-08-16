package com.transcend.plm.datadriven.apm.space.pojo.dto;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmSpaceAppViewModelCopyDto {
    @ApiParam("oldSpaceAppBid")
    private String oldSpaceAppBid;
    @ApiParam("oldSpaceBid")
    private String oldSpaceBid;
    @ApiParam("newSpaceBid")
    private String newSpaceBid;
    @ApiParam("modelCode")
    private String modelCode;
    @ApiParam("newSpaceAppBid")
    private String newSpaceAppBid;
    @ApiParam("openModel")
    private String openModel;
    @ApiParam("copyInstanceModel")
    private String copyInstanceModel;

}
