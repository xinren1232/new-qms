package com.transcend.plm.datadriven.apm.space.model;

/**
 * 对象模型配置常量
 *
 * @author unknown
 * @author jie.luo1
 */
public enum AppViewModelEnum {

    /*    { code: 'tableView', name: '表格' },
        { code: 'treeView', name: '树' },
        { code: 'multiTreeView', name: '多对象' },
        { code: 'kanbanView', name: '看板' },
        { code: 'ganttView', name: '甘特图' }*/
    /**
     *表格
     */
    TABLE_VIEW("tableView", "表格", "table"),
    /**
     *层级
     */
    TREE_VIEW("treeView", "层级", "tree"),
    /**
     *多维表格
     */
    MULTI_TREE_VIEW("multiTreeView", "多维表格", "multiTree"),
    /**
     *看板
     */
    KANBAN_VIEW("kanbanView", "看板", "kanban"),
    /**
     *甘特图
     */
    GANTT_VIEW("ganttView", "甘特图", "gantt"),
    /**
     *概览
     */

    OVER_VIEW("overview", "概览", "overview"),
    /**
     *泳道图
     */
    SWIMLANE_DIAGRAM("swimlaneDiagram", "泳道图", "swimlaneDiagram"),
    /**
     * 高级筛选器
     */
    ADVANCED_FILTER("advancedFilter", "高级筛选器", "advancedFilter"),
    /**
     * 初始需求视图
     */
    IR_View ("IRView", "初始需求视图", "IRView"),
    /**
     * 特性数视图
     */
    IRSF_VIEW  ("IRSFView", "特性数视图", "IRSFView"),
    ;

    /**
     *code
     */
    String code;
    /**
     *zh
     */
    String zh;
    /**
     *en
     */
    String en;

    AppViewModelEnum(String code, String zh, String en) {
        this.code = code;
        this.zh = zh;
        this.en = en;
    }

    public String getCode() {
        return this.code;
    }

    public String getEn() {
        return this.en;
    }

    public String getZh() {
        return this.zh;
    }
}
