package com.transcend.plm.datadriven.api.model.qo;

import lombok.Data;

import java.util.List;

/**
 * @author peng.qin
 * @date 2024/07/24
 */
@Data
public class ModelExpression {

    /**
     * and or
     */
    private String relation = "and";

    /**
     * 属性名 如：meterCode
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
     * 是否嵌套
     */
    private Boolean nested = Boolean.FALSE;

    /**
     * 嵌套条件
     */
    private List<ModelExpression> expressions;

}
