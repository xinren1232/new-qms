package com.transcend.plm.datadriven.apm.enums;

import com.transcend.plm.datadriven.apm.dto.ExpressParam;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

/**
 * @author Mickey Qiu
 * @desc 条件表达式枚举
 * @date 2025/3/15
 */
@Getter
@AllArgsConstructor
public enum ConditionExpressEnum {



    EQ("eq", ConditionExpressEnum::eqExpress),
    ;


    private final String type;

    private final Function<ExpressParam<?>, Boolean> func;

    private static Boolean eqExpress(ExpressParam<?> expressParam) {
        return null;
    }

}
