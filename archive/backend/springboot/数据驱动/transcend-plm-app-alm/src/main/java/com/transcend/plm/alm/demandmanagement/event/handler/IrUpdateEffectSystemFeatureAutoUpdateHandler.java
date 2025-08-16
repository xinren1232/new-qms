package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureStatusAutoUpdateRuleService;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureVersionAutoUpdateRuleService;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.apm.constants.IrLifeCycleConstant;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 特性状态自动更新处理器
 *
 * @author jie.luo1 <jie.luo1@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 11:59
 */
@Slf4j
@Component
@AllArgsConstructor
public class IrUpdateEffectSystemFeatureAutoUpdateHandler {


    private final SystemFeatureStatusAutoUpdateRuleService systemFeatureStatusAutoUpdateRuleService;
    private final SystemFeatureVersionAutoUpdateRuleService systemFeatureVersionAutoUpdateRuleService;

    private final String irModelCode = TranscendModel.IR.getCode();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataUpdateEvent event) {
        MBaseData data = event.getData();
        if (data == null) {
            return;
        }
        TranscendObjectWrapper objectWrapper = new TranscendObjectWrapper(data);
        //补充模型编码
        objectWrapper.setModelCode(event.getModelCode());
        /*
         * IR（A01）状态变化触发特性状态变化：
         * 任意IR（A01）到开发（DEVELOP），且SF（A1AA01）无条件，则自动到实现中(BEING_IMPLEMENTED) ,并且IR关联的三级特性-状态变更为实现中（BEING_IMPLEMENTED）
         * 全部IR（A01）到全部完成（COMPLETE），且SF（A1AA01）无条件，则自动到已实现(REALIZED),并且IR关联的三级特性-状态变更为已实现（REALIZED）
         * 拆解为事件：
         * 1.modelCode = A01
         * 2.lifeCycleCode = DEVELOP/COMPLETE
         */
        // 待更新的生命周期
        String lifeCycleCode = objectWrapper.getLifeCycleCode();
        if (irModelCode.equals(objectWrapper.getModelCode()) && (IrLifeCycleConstant.DEVELOP.equals(lifeCycleCode)
                || IrLifeCycleConstant.COMPLETE.equals(lifeCycleCode))) {
            // 执行更新逻辑 TODO
            systemFeatureStatusAutoUpdateRuleService.irStateUpdateTrigger(event, lifeCycleCode);
        }



        // 特性树版本：IR状态更新触发
        /**
         * IR状态变化触发特性状态变化：
         * IR状态变更为【完成】，则tos特性的版本号 补充
         * 从IR获取的 交付版本（比如：tOS16.1.2） 替换到 特性的版本号中 【1.0.0】=> 【16.1.2】
         * （注：一二特性都需要拼接（全集也要跟着变） 三级特性需要看IR是否绑定了对应的三级特性，空不拼接（回退暂不考虑））
         * ？？？ 全集特性会有多个层级。子层级变化，上层也变？挂载不同tos是否存（这个会引发子层级变化，上层级版本错乱）？ 可能不存在
         * TOS（关联项目的界面）的那个版本号是否需要变更》？？？？？
         * 拆解为事件：
         * 1.modelCode = A01
         * 2.lifeCycleCode = COMPLETE
         **/
        if (irModelCode.equals(objectWrapper.getModelCode()) && IrLifeCycleConstant.COMPLETE.equals(lifeCycleCode)) {
            systemFeatureVersionAutoUpdateRuleService.irStateUpdateTrigger(event, lifeCycleCode);
        }


    }


}
