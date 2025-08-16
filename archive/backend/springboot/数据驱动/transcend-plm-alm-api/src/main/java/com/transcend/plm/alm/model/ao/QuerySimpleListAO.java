package com.transcend.plm.alm.model.ao;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 查询SR列表参数对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/5 14:18
 */
@Data
public class QuerySimpleListAO implements Serializable {

    @ApiModelProperty("SR编码列表")
    private List<String> codeList;

    /**
     * 设置返回条数，不传或者传入null 默认200
     */
    @ApiModelProperty("返回条数")
    private Integer returnSize;

}
