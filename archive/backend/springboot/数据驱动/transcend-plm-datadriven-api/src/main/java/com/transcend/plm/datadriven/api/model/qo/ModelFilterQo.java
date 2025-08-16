package com.transcend.plm.datadriven.api.model.qo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * ModelFilterQuery 前端查询对象过滤条件
 *
 * @author yss
 * @version: 1.0
 * @date 2022/06/01 20:16
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Api(value = "领域对象属性QO", tags = "操作领域对象QO")
public class ModelFilterQo {

    /**
     * 多对象情况下，查询条件应用于哪个对象
     */
    @ApiModelProperty(value = "查询条件应用的对象")
    private String modelCode;

    /**
     * 属性名称
     * 如：meterCode
     */
    private String property;

    /**
     * in  exist  like
     */
    private String condition;

    /**
     * 属性值
     */
    private Object value;

    /**
     * string  number  date
     */
    private String type;

    /**
     * ["234324","wewe"]
     */
    private List<Object> values;

}
