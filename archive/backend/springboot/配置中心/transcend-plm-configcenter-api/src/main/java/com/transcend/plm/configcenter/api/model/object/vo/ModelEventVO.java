package com.transcend.plm.configcenter.api.model.object.vo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * model_event
 * @author
 */
@Accessors(chain = true)
@Getter
@Setter
@ToString
@ApiModel(value="事件表")
public class ModelEventVO extends BaseDto implements Serializable {

    /**
     * 事件id
     */
    @ApiModelProperty(value="事件id")
    private String eventId;

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


    private static final long serialVersionUID = 1L;
}
