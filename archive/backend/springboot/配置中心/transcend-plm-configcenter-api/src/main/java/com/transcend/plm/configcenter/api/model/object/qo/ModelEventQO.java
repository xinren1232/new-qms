package com.transcend.plm.configcenter.api.model.object.qo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @version: 1.0
 * @date 2021/11/18 09:48
 */
@Accessors(chain = true)
@Getter
@Setter
@ToString
@ApiModel(value = "事件表")
public class ModelEventQO {

    /**
     * 事件名称
     */
    @ApiModelProperty(value="事件名称")
    private String eventName;

    /**
     * 说明
     */
    @ApiModelProperty(value="说明")
    private String description;


    @ApiModelProperty(value="使用状态，禁用disable，启用enable，未启用off(默认off)")
    private Integer enableFlag;
}
