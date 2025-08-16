package com.transcend.plm.datadriven.api.model.dto;

import com.transcend.plm.datadriven.api.model.MBaseData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 更新部分模型实例数据入参
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@Data
public class PartialUpdateModelDto implements Serializable {

    @ApiModelProperty("需要更新的部分属性")
    Set<String> properties;

    @ApiModelProperty("模型编码")
    MBaseData data;

}
