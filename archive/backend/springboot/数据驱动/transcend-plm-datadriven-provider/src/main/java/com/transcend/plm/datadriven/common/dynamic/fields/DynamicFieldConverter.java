package com.transcend.plm.datadriven.common.dynamic.fields;

import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgOptionItemDto;
import com.transcend.plm.datadriven.common.strategy.MultipleStrategyContext;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 动态字段转换器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 10:10
 */
@Getter
@Component
public class DynamicFieldConverter extends MultipleStrategyContext<String, DynamicFields> {

    /**
     * 选项列表
     */
    private final List<CfgOptionItemDto> optionList;

    public DynamicFieldConverter(@NonNull List<DynamicFields> strategyList) {
        super(strategyList);
        this.optionList = strategyList.stream()
                .map(field -> CfgOptionItemDto.builder().label(field.getName()).value(field.getPlaceholder()).build())
                .collect(Collectors.toList());
    }

    /**
     * 字段转换操作
     *
     * @param value 字段值
     * @return 字段值
     */
    public Object convert(Object value) {
        if (value == null) {
            return null;
        }

        //替换操作
        value = replace(value);

        //集合值替换
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            value = collection.stream().map(this::replace).collect(Collectors.toList());
        }

        //数组值替换
        if (value instanceof String[]) {
            Object[] array = (Object[]) value;
            for (int i = 0; i < array.length; i++) {
                array[i] = replace(array[i]);
            }
        }

        return value;
    }

    @Nullable
    private Object replace(Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            DynamicFields service = this.getStrategyService(strValue, false);
            if (service != null) {
                Object dynamicValue = service.getValue();
                if (dynamicValue instanceof String) {
                    return strValue.replace(service.getPlaceholder(), (String) dynamicValue);
                }
                if (dynamicValue instanceof Collection) {
                    value = dynamicValue;
                }
            }
        }
        return value;
    }

}
