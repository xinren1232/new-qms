package com.transcend.plm.datadriven.api.model;

import com.transsion.framework.enums.BaseEnum;

/**
 * 对象模型配置常量
 *
 * @author jie.luo1
 * @date 2024/07/24
 */
public enum ObjectTreeEnum implements BaseEnum<String> {

    /**
     * 对象实例父bid
     */
    PARENT_BID("parentBid", "parent_bid", "对象实例父bid"),

    /**
     * 子集合
     */
    CHILDREN("children", "children","子集合"),
    /**
     *排序
     */
    SORT("sort", "sort", "排序"),
    /**
     *关系实例bid
     */
    REL_INSTANCE_BID("relInstanceBid", "rel_instance_bid","关系实例bid"),
    /**
     *选中
     */
    CHECKED("checked", "checked", "选中"),
    /**
     *多对象树bid
     */
    TREE_BID("treeBid", "tree_bid", "多对象树bid"),
    /**
     * 多对象树是否是头节点
     */
    MULTI_TREE_HEAD("multiTreeHead", "multi_tree_head","多对象树是否是头节点")
    ;
    /**
     *code
     */
    String code;
    /**
     *column
     */
    String column;
    /**
     *desc
     */
    String desc;

    /**
     * @param code
     * @param column
     * @param desc
     */
    ObjectTreeEnum(String code,String column, String desc) {
        this.code = code;
        this.column = column;
        this.desc = desc;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * @return {@link String }
     */
    public String getColumn() {
        return this.column;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getDesc() {
        return this.desc;
    }
}
