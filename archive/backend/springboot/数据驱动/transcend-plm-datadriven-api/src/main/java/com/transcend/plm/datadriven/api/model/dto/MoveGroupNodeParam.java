package com.transcend.plm.datadriven.api.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;

/**
 * 对象模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@ApiModel("移动分组节点入参")
public class MoveGroupNodeParam extends HashMap<String,Object> {

    @ApiModelProperty("bid")
    private String bid;

    @ApiModelProperty("排序")
    private String sort;

    @ApiModelProperty("排序字段内容")
    private Object groupProperty;


    /**
     * @return {@link Object }
     */
    public Object getGroupProperty() {
        return get("groupProperty");
    }
}
