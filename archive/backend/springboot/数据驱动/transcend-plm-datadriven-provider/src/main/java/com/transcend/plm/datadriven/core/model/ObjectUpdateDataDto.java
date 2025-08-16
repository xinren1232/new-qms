package com.transcend.plm.datadriven.core.model;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 更新数据入参
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/18 17:39
 * @since 1.0
 */
@Data
public class ObjectUpdateDataDto {

    @ApiModelProperty("实例数据")
    private MObject mObject;

    @ApiModelProperty("查询条件")
    private List<QueryWrapper> queryWrappers;
}
