package com.transcend.plm.datadriven.apm.space.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmStateVO {
    /**
     * 状态编码
     */
    private String lifeCycleCode;

    /**
     * 状态名称
     */

    private String lifeCycleName;

    /**
     * 视图bid
     */
    private String viewBid;

    /**
     * 状态节点bid
     */
    private String nodeBid;

    /**
     * 处理人员工号
     */
    private List<String> jobNumbers;

    /**
     * 第一个角色bid
     */
    private String roleBid;

    /**
     * 组装前端字段
     */
    private String bid;
    private String name;
    private String life_cycle_code;
    private String keyCode;
    private String zh;

    private String color;
}
