package com.transcend.plm.datadriven.apm.event.config;

import lombok.Data;

/**
 * 跨级关系通知配置
 * ex:
 * sourceModel = 版本
 * relationModel = 版本-需求
 * betweenRelModel = 版本-迭代
 * parentRelation = 项目-需求的配置
 * @author yinbin
 * @version:
 * @date 2023/10/25 21:23
 */
@Data
public class NotifyCross {
    private String sourceModel;
    private String relationModel;
    private String betweenRelModel;
    private NotifyCross parentRelation;
}
