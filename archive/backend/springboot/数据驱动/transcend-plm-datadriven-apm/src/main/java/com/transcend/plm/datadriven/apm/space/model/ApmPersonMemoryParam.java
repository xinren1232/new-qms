package com.transcend.plm.datadriven.apm.space.model;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户个性缓存更新参数
 * @author unknown
 * @TableName apm_person_memory
 */
@Data
@Builder(toBuilder = true)
public class ApmPersonMemoryParam implements Serializable {

    /**
     * 业务id
     */
    @ApiModelProperty(value = "业务id")
    private String bid;

    /**
     * 视图类别
     */
    @ApiModelProperty(value = "视图类别")
    private String category;

    /**
     * 标识
     */
    @ApiModelProperty(value = "标识")
    private String code;

    /**
     * 视图内容
     */
    @ApiModelProperty(value = "视图内容")
    private JSONObject content;

    /**
     * 需要删除的key列表
     */
    @ApiModelProperty(value = "需要删除的key列表")
    private List<String> removeKeys;
}