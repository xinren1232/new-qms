package com.transcend.plm.datadriven.api.model.qo;

import com.transcend.plm.datadriven.api.model.Order;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author peng.qin
 * @date 2024/07/24
 */
@Builder
@Data
public class ModelExpressionQO {
    /**
     * 查询条件
     */
    @ApiModelProperty(value = "查询条件")
    private List<ModelExpression> expressions;

    /**
     * 是否任意匹配全部
     */
    @ApiModelProperty(value = "是否任意匹配全部")
    private Boolean anyMatch;

    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    private List<Order> orders;

}
