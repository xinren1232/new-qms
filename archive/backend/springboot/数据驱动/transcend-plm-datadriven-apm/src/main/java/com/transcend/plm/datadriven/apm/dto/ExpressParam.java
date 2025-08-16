package com.transcend.plm.datadriven.apm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Mickey Qiu
 * @desc 条件比较表达式
 * @date 2025/3/15
 */
@Accessors(chain = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpressParam<T> {

    private T sourceVal;

    private T targetVal;

    private Class<T> type;
}



