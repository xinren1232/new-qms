package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * ModelQuery
 *
 * @author yss
 * @version: 1.0
 * @date 2022/06/01 20:16
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Api(value = "领域对象属性QO",tags = "操作领域对象QO")
public class ModelQuery{

    /**
     *  如：meterCode
     */
    private String property;

    /**
     * 如：meter_code
     */
    private String column;

    private Boolean index;
    private Boolean isList;

    /**
     * 如：meter_code
     */
    private String condition;

    private Boolean isBetween;



    private Object value;

    /**
     * string  number  date
     */
    private String type;

    /**
     * ["234324","wewe"]
     */
    @Deprecated
    private List<Object> values;

    /** 是否是关系查询条件 **/
    private Boolean isRefCondition;

    /**
     * @return {@link ModelQuery }
     */
    public static ModelQuery of() {
        return new ModelQuery();
    }





}
