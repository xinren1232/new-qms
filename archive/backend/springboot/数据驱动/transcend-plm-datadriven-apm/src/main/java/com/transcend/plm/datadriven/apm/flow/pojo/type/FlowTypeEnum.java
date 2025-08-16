package com.transcend.plm.datadriven.apm.flow.pojo.type;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 流程类型
 *
 * @author Alan Wu
 * @version 1.0
 * createdAt 2024/9/9 13:39
 */
@AllArgsConstructor
@Getter
public enum FlowTypeEnum {
    /**
     * 状态流程
     */
    STATE("state"),
    /**
     * 任务流程
     */
    TASK("task");

    private final String code;
}
