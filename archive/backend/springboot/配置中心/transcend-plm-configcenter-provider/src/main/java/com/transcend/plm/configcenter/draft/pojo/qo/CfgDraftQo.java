package com.transcend.plm.configcenter.draft.pojo.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 15:43
 **/
@Data
public class CfgDraftQo {
    /**
     * 业务code
     */
    @ApiModelProperty("长度为32")
    private String bizCode;

    /**
     * 类别
     */
    @ApiModelProperty("长度为16")
    private String category;

    /**
     * 内容
     */
    private String content;
}
