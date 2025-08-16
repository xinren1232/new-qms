package com.transcend.plm.datadriven.apm.event.config;

import lombok.Data;

/**
 * ex:迭代选取需求,需要查询需求关联的任务,将任务关联到迭代
 * querySourceModel = 需求
 * queryTargetModel = 任务
 * queryRelModel = 需求-任务
 * parentRelModel = 迭代-任务
 * @author yinbin
 * @version:
 * @date 2023/10/28 16:03
 */
@Data
public class NotifyRevisionRelTaskConfig {
    private String querySourceModel;
    private String queryTargetModel;
    private String queryRelModel;
    private String parentRelModel;
}
