package com.transcend.plm.datadriven.apm.space.pojo.vo;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ResourceVo {


    private String spaceBid;
    private String spaceAppBid;
    private String lifeCycleCode;
    private String bid;

    private String modelCode;
    /**
     * 名称
     */
    private String name;

    /**
     * 责任人
     */
    private String personResponsible;

    /**
     * 工时
     */
    private Double workingHours;

    /**
     * 剩余工时
     */
    private Double  residualWorkingHours;

    /**
     * 换算的工作量
     */
    private Double calWorkingHours;

    /**
     * 计划开始时间
     */
    private String planStartTime;

    /**
     * 计划结束时间
     */
    private String planEndTime;

    /**
     * 状态
     */
    private String state;

    /**
     * 完成度
     */
    private Double completeRate;

    /**
     * 参数工作日时间
     */
    private long paramWorkHours;


}
