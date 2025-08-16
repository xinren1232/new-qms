package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;

import java.util.List;

/**
 * 特性状态自动更新规则服务
 *
 * @author jie.luo1  <jie.luo1@transsion.com>
 * @version 1.0
 * createdAt 2025/5/26 16:05
 */
public interface SystemFeatureStatusAutoUpdateRuleService {


    /**
     * IR（A01）状态变化触发特性状态变化：
     * 任意IR（A01）到开发（DEVELOP），且SF（A1AA01）无条件，则自动到实现中(BEING_IMPLEMENTED) ,并且已同步四级特性-状态变更为实现中（BEING_IMPLEMENTED）
     * 全部IR（A01）到完成（COMPLETE），且SF（A1AA01）无条件，则自动到已实现(REALIZED),并且已同步四级特性-状态变更为已实现（REALIZED）
     *
     * @param event     IR的BID列表
     * @param lifeCycleCode 生命周期编码
     * @return boolean
     */
    boolean irStateUpdateTrigger(BaseDataUpdateEvent event, String lifeCycleCode);







}
