package com.transcend.plm.datadriven.apm.permission.enums;

import com.transsion.framework.enums.BaseEnum;

import java.util.Arrays;
import java.util.List;

/**
 * @author unknown
 */
public enum OperatorEnum implements BaseEnum<String> {
    /**
     *新增
     */
    ADD("ADD", "新增"),
    /**
     *删除
     */
    DELETE("DELETE", "删除"),
    /**
     *编辑
     */
    EDIT("EDIT", "编辑"),
    /**
     *详情
     */
    DETAIL("DETAIL", "详情"),
    /**
     *修订
     */

    REVISE("REVISE", "修订"),

    /**
     *提升
     */
    PROMOTE("PROMOTE", "提升"),
    /**
     *列表查询
     */
    LIST("LIST", "列表查询"),
    /**
     *新增
     */
    IMPORT("IMPORT", "导入"),
    /**
     *删除
     */
    EXPORT("EXPORT", "导出"),

    /**
     *锁定
     */
    LOCK("LOCK", "锁定"),
    /**
     *解锁
     */
    UNLOCK("UNLOCK", "解锁"),

    /**
     *同步
     */
    SYNC("SYNC", "同步"),

    /**
     *提升
     */
    MOVE("MOVE", "移动"),
    /**
     *视图模式-列表
     */
    VIEW_MODEL_tableView("VIEW_MODEL_tableView", "视图模式-列表"),
    /**
     *视图模式-层级
     */
    VIEW_MODEL_treeView("VIEW_MODEL_treeView", "视图模式-层级"),
    /**
     *视图模式-多维表格
     */
    VIEW_MODEL_multiTreeView("VIEW_MODEL_multiTreeView", "视图模式-多维表格"),
    /**
     *视图模式-概览
     */
    VIEW_MODEL_overview("VIEW_MODEL_overview", "视图模式-概览"),
    /**
     *视图模式-度量报表
     */
    VIEW_MODEL_measureReport("VIEW_MODEL_measureReport", "视图模式-度量报表"),
    /**
     *视图模式-度量报表
     */
    VIEW_MODEL_kanbanView("VIEW_MODEL_kanbanView", "视图模式-看板"),    /**
     *视图模式-度量报表
     */
    VIEW_MODEL_ganttView("VIEW_MODEL_ganttView", "视图模式-甘特图"),
    /**
     *视图模式（全）
     */
    VIEW_MODEL_ALL("VIEW_MODEL_ALL", "视图模式（全）")
    ;
    /**
     *code
     */
    String code;
    /**
     *desc
     */
    String desc;

    OperatorEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    /**
     *  * @param code code
     */
    @Override
    public String getCode() {
        return this.code;
    }
    /**
     *  * @param desc desc
     */
    @Override
    public String getDesc() {
        return this.desc;
    }

    public static final List<String> OPERATOR_ORDER_LIST = Arrays.asList("ADD", "EDIT","NAME-EDIT", "DELETE","ADDBATCH","COPYADD","COPY","DETAIL", "MOVE", "REVISE","PROMOTE","LIST");
}
