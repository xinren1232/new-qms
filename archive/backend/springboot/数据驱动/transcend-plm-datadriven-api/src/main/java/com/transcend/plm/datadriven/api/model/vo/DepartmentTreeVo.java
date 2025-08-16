package com.transcend.plm.datadriven.api.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class DepartmentTreeVo{

    private Long companyId;

    /**
     * 主键Id
     */
    private Long id;

    /**
     * 部门编码
     */
    private String deptNo;

    /**
     * 名称
     */
    private String name;

    /**
     * 父部门Id
     */
    private Long parentId;

    /**
     * 父部门编码
     */
    private String parentNo;

    /**
     * 所在层级
     */
    private Integer level;

    /**
     * 子节点
     */
    private List<DepartmentTreeVo> children;




}
