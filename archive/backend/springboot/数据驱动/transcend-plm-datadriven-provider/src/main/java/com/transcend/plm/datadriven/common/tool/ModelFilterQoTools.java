package com.transcend.plm.datadriven.common.tool;

import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.common.dynamic.fields.DynamicFieldConverter;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelFilterQoTools {

    /**
     * 动态字段解析操作
     *
     * @param queries 查询条件
     */
    public static void analysis(List<ModelFilterQo> queries) {
        if (CollectionUtils.isNotEmpty(queries)) {
            DynamicFieldConverter converter = PlmContextHolder.getBean(DynamicFieldConverter.class);
            queries.forEach(query -> query.setValue(converter.convert(query.getValue())));
        }
    }
}
