package com.transcend.plm.datadriven.notify.vo;

import lombok.Data;

import java.util.Date;

/**
 * 通知执行时间
 *
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class NotifyExecuteTimeVo {
    /**
     * 执行时间
     */
    private Date executeTime;
    /**
     * 执行时间
     */
    private String executeTimeStr;
    /**
     * 是否现在执行
     */
    private Boolean isNow;

    /**
     * 执行次数
     */
    private Integer executeNum;
}
