package com.transcend.plm.datadriven.apm.space.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2023/10/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FieldConditionParam {

    private String fieldName;

    private Object fieldVal;

    private String condition;

    private String propertyZh;

    private String operatorZh;

    private String valueZh;

    private String type;
}
