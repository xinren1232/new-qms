package com.transcend.plm.configcenter.view.pojo.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 同步视图查询参数
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/19 10:45
 */
@Data
public class SyncViewContentQo implements Serializable {

    @ApiModelProperty("同步源数据")
    private String sourceBid;

    @ApiModelProperty("同步目标数据")
    private List<String> targetBids;

}
