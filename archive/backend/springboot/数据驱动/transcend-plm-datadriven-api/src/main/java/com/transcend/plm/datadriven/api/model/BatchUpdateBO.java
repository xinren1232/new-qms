package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author jinpeng.bai
 * @date 2023/09/28 13:47
 */
@Data
@AllArgsConstructor
public class BatchUpdateBO<T> {
    @ApiModelProperty(value = "数据")
    T baseData;
    @ApiModelProperty(value = "查询条件")
    List<QueryWrapper> wrappers;

    /**
     *
     */
    public BatchUpdateBO() {

    }

}
