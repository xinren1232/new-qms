package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author unknown
 */
@Data
public class MultiObjectUpdateDto {

    @ApiModelProperty("需要更新的实例Bid")
    private List<String> bids;
    private String modelCode;
    @ApiModelProperty("待更新的数据内容")
    private MSpaceAppData data;
}
