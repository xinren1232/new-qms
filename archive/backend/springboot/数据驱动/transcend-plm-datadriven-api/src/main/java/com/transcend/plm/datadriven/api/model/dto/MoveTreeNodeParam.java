package com.transcend.plm.datadriven.api.model.dto;

import com.transcend.plm.datadriven.api.model.MObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 对象模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@ApiModel("移动树节点入参")
@Data
public class MoveTreeNodeParam extends HashMap<String,Object> {

    @ApiModelProperty("bid")
    private String bid;

    @ApiModelProperty("父bid")
    private String parentBid;

    @ApiModelProperty("排序")
    private String sort;



}
