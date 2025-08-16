package com.transcend.plm.datadriven.apm.task.vo;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmTaskNumVO {
    /**
     * 任务类型，undo.待办任务，done.已完成任务
     */
    private String taskType;
    /**
     * 任务状态，0.未处理，2.已完成
     */
    private int taskState;

    /**
     * 任务数量
     */
    private long num;
}
