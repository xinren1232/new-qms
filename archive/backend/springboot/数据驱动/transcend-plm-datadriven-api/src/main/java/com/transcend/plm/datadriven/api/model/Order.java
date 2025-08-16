package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * 排序查询
 *
 * @author yss
 * @date 2022/06/01 20:16
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Api(value = "领域对象属性QO",tags = "操作领域对象QO")
public class Order {

    /**
     * 如：meterCode
     */
    private String property;

    /**
     * 如：meterCode
     */
    private String column;

    private Boolean desc;

    private String type;

    private String remoteDictType;

    /**
     * @return {@link Order }
     */
    public static Order of() {
        return new Order();
    }





}
