package com.transcend.plm.datadriven.apm.tools;

import com.alibaba.fastjson.TypeReference;
import com.transcend.plm.datadriven.apm.space.pojo.dto.FieldConditionParam;

import java.util.List;

/**
 * @author unknown
 */
public class FieldConditionListTypeHandler extends ListTypeHandler<FieldConditionParam> {

    @Override
    protected TypeReference<List<FieldConditionParam>> specificType() {
        return new TypeReference<List<FieldConditionParam>>() {
        };
    }

}