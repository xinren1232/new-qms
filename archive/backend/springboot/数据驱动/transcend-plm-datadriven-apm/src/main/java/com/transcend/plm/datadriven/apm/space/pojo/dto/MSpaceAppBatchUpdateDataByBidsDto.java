package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新空间应用数据DTO
 * 入参：空间bid列表，
 * 出参：空间应用数据枚举对应的值
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2025/5/6 17:44
 * @since 1.0
 */
@ApiModel("批量更新空间应用数据DTO")
@Data
public class MSpaceAppBatchUpdateDataByBidsDto {

    @ApiModelProperty("实例bids")
    private List<String> bids;

    @ApiModelProperty("待更新的数据内容")
    private MSpaceAppData data;

}
