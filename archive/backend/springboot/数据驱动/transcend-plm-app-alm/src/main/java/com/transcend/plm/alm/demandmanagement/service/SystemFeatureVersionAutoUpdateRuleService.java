package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.alm.demandmanagement.entity.ao.SfTreeDataSyncCopyAo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;

import java.util.List;

/**
 * 特性自动版本更新规则服务
 *
 * @author jie.luo1  <jie.luo1@transsion.com>
 * @version 1.0
 * createdAt 2025/5/26 16:05
 */
public interface SystemFeatureVersionAutoUpdateRuleService {

    /**
     * 同步数据到TOS 的版本变化的规则：
     * 把全集特性树同步过来，并且把TOS 版本号回写 全集特性树中版本号
     *
     * @param ao           请求参数
     * @param collector
     * @param syncDataList
     * @return boolean
     */
    boolean syncTosSystemFeature(SfTreeDataSyncCopyAo ao, SystemFeatureSyncDataChangeCollector collector, List<MObject> syncDataList);

    /**
     * IR状态变化触发特性状态变化：
     * IR状态变更为【完成】，则tos特性的版本号 补充
     * 从IR获取的 交付版本（比如：tOS16.1.2） 替换到 特性的版本号中 【1.0.0】=> 【16.1.2】
     * （注：一二特性都需要拼接（全集也要跟着变） 三级特性需要看IR是否绑定了对应的三级特性，空不拼接（回退暂不考虑））
     * ？？？ 全集特性会有多个层级。子层级变化，上层也变？挂载不同tos是否存（这个会引发子层级变化，上层级版本错乱）？ 可能不存在
     * TOS（关联项目的界面）的那个版本号是否需要变更》？？？？？
     * @param event      事件内容
     * @param lifeCycleCode IR生命周期代码
     * @return boolean
     */
    boolean irStateUpdateTrigger(BaseDataUpdateEvent event, String lifeCycleCode);





}
