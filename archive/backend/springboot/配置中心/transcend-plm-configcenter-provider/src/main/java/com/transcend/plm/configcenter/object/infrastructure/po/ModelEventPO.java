package com.transcend.plm.configcenter.object.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * model_event
 * @author
 */
@Accessors(chain = true)
@Getter
@Setter
@ToString
@ApiModel(value="事件表")
@TableName("cfg_object_event_method")
public class ModelEventPO extends BasePoEntity {

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


}
