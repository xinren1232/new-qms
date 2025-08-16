package com.transcend.plm.datadriven.apm.event.enums;

import com.transcend.plm.datadriven.apm.event.handler.instance.*;
import com.transcend.plm.datadriven.apm.event.handler.relation.*;
import lombok.Getter;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/06/13 16:48
 */
@Getter
public enum EventHandlerTypeEnum {
    /* ==============================实例类型==============================*/
    /** 新增实例 **/
    ADD("add", AbstractAddEventHandler.class, "新增实例"),
    /** 检出实例 **/
    CHECK_OUT("checkOut", AbstractCheckOutEventHandler.class, "检出实例"),
    /** 取消检出实例 **/
    CANCEL_CHECK_OUT("cancelCheckOut", AbstractCancelCheckOutEventHandler.class, "取消检出实例"),
    /** 检入实例 **/
    CHECK_IN("checkIn", AbstractCheckInEventHandler.class, "检入实例"),
    /** 批量检出实例 **/
    BATCH_CHECK_OUT("batchCheckOut", AbstractBatchCheckOutEventHandler.class, "检出实例"),
    /** 批量取消检出实例 **/
    BATCH_CANCEL_CHECK_OUT("batchCancelCheckOut", AbstractBatchCancelCheckOutEventHandler.class, "取消检出实例"),
    /** 批量检入实例 **/
    BATCH_CHECK_IN("batchCheckIn", AbstractBatchCheckInEventHandler.class, "检入实例"),
    /** 暂存实例 **/
    SAVE_DRAFT("saveDraft", AbstractSaveDraftEventHandler.class, "暂存实例"),
    /** 批量暂存实例 **/
    BATCH_SAVE_DRAFT("batchSaveDraft", AbstractBatchSaveDraftEventHandler.class, "批量暂存实例"),
    /** 修订实例 **/
    REVISE("revise", AbstractReviseEventHandler.class, "修订实例"),
    /** 生命周期提升 **/
    PROMOTE("promote", AbstractPromoteEventHandler.class, "实例生命周期提升"),
    /** 逻辑删除实例 **/
    LOGICAL_DELETE("logicalDelete", AbstractLogicalDeleteEventHandler.class, "单个逻辑删除"),
    /** 更新实例 **/
    UPDATE("update", AbstractUpdateEventHandler.class, "更新实例"),
    /** 批量更新实例 **/
    BATCH_UPDATE("batch_update", AbstractBatchUpdateEventHandler.class, "批量更新实例"),
    /** 分页查询实例 **/
    PAGE("page", AbstractPageEventHandler.class, "分页查询"),
    /** 批量新增实例 **/
    BATCH_ADD("batchAdd", AbstractBatchAddEventHandler.class, "批量新增"),
    /** 批量删除实例 **/
    BATCH_DELETE("batchDelete", AbstractBatchDeleteEventHandler.class, "批量删除"),

    BATCH_LOGIC_DELETE("batchLogicDelete", AbstractBatchLogicDeleteEventHandler.class, "批量删除"),
    /** 保存草稿 **/
    SAVE_COMMON_DRAFT("saveCommonDraft", AbstractSaveCommonDraftEventHandler.class, "保存草稿"),
    /* ==============================实例类型==============================*/



    /* ==============================关系类型==============================*/

    /**
     * 关系新增或选取
     */
    RELATION_ADD("relationAdd", AbstractRelationAddEventHandler.class, "关系新增或选取"),

    /**
     * 多对象树选取
     */
    RELATION_BATCH_SELECT("relationBatchSelect", AbstractRelationBatchSelectEventHandler.class, "多对象树选取"),
    /**
     *删除关系和目标实例数据
     */
    RELATION_BATCH_DELETE("relationBatchDelete", AbstractRelationBatchDeleteEventHandler.class, "删除关系和目标实例数据"),
    /**
     *移除关系
     */
    RELATION_BATCH_REMOVE("relationBatchRemove", AbstractRelationBatchRemoveEventHandler.class, "移除关系"),
    /**
     *关系列表查询
     */
    RELATION_LIST("listRelation", AbstractRelationListEventHandler.class, "关系列表查询"),
    /**
     *关系分页查询
     */
    RELATION_PAGE("pageRelation", AbstractRelationPageEventHandler.class, "关系分页查询"),

    /**
     *关系树查询
     */
    RELATION_TREE("treeRelation", AbstractRelationTreeEventHandler.class, "关系树查询"),

    /* ==============================流程类型==============================*/
    /**
     * 完成节点
     */
    COMPLETE_FLOW_NODE("completeFlowNode", AbstractCompleteFlowNodeEventHandler.class, "实例流程完成节点"),
    /**
     * 回退节点
     */
    ROLLBACK_FLOW_NODE("rollbackFlowNode", AbstractRollbackFlowNodeEventHandler.class, "实例流程回退节点"),
    /**
     * 多对象批量编辑
     */
    MULTI_OBJECT_UPDATE("multiObjectUpdate", AbstractMultiObjectUpdateEventHandler.class, "实例流程回退节点"),
    ;

    EventHandlerTypeEnum(String code, Class<?> baseHandlerClass, String desc) {
        this.code = code;
        this.baseHandlerClass = baseHandlerClass;
        this.desc = desc;
    }
    /**
     *code
     */
    private final String code;
    /**
     *baseHandlerClass
     */
    private final Class<?> baseHandlerClass;
    /**
     *desc
     */
    private final String desc;
}
