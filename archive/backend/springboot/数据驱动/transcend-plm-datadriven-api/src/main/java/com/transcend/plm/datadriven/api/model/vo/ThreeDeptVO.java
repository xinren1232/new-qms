package com.transcend.plm.datadriven.api.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 三级部门信息，包含一级、二级和三级部门ID及名称
 */
@Data
@Builder
public class ThreeDeptVO {

    /**
     * 一级部门ID及名称
     */
    private String firstDeptId;
    private String firstDeptName;

    /**
     * 二级部门ID及名称
     */
    private String secondDeptId;
    private String secondDeptName;


    /**
     * 三级部门ID及名称
     */
    private String thirdDeptId;
    private String thirdDeptName;
}
