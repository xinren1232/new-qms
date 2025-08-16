package com.transcend.plm.configcenter.api.model.filemanagement.vo;

import lombok.Data;

@Data
public class PlanTimeParam {
    /**
     * 类型，如：天day、周week、月month
     */
    private String type;

    /**
     * 第几天
     */
    private Integer day;

    /**
     * 周几，1到7
     */

    private Integer week;
    /**
     * 时
     */
    private Integer hour;
    /**
     * 分
     */
    private Integer minute;

    /**
     * 秒
     */
    private Integer second;
}
