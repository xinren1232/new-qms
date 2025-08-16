package com.transcend.plm.configcenter.api.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Describe 视图枚举类
 * @Author yuhao.qiu
 * @Date 2024/9/4
 */
@AllArgsConstructor
@Getter
public enum ViewEnums {
    /**
     * 新增视图
     */
    ADD("add", "新增视图"),
    /**
     * 编辑视图
     */
    EDIT("edit", "编辑视图"),
    /**
     * 详情视图
     */
    DETAIL("detail", "详情视图"),
    /**
     * 默认视图
     */
    DEFAULT("default", "默认视图"),
    /**
     * 筛选视图
     */
    FILTER("filter", "筛选视图");


    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
