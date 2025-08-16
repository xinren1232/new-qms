package com.transcend.plm.configcenter.api.model.dictionary.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 自定义字典项
 * @author yinbin
 * @version:
 * @date 2023/10/08 10:59
 */
@Data
@Builder(toBuilder = true)
public class CfgOptionItemDto {

    @ApiModelProperty("自定义下拉名称")
    private String label;
    @ApiModelProperty("自定义下拉值")
    private String value;
}
