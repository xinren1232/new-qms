package com.transcend.plm.datadriven.apm.strategy;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.constants.DemandConstant;
import com.transcend.plm.datadriven.apm.constants.IrLifeCycleConstant;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.common.ObjectUtil;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 修改属性限制校验策略
 */
@Slf4j
@Component
public class UpdatePropertyLimitStrategy {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    private Map<String, BiConsumer<ApmSpaceApp, MSpaceAppData>> modelCodeStrategyMap;

    /**
     * 空间项目的 模板bid
     */
    public static String SPACE_TEMPLATE_BID = "templateBid";

    /**
     * 空间项目的 模板bid
     */
    public static String SPACE_TEMPLATE_BID_SOFT = "1360304098869075968";


    @PostConstruct
    public void init() {
        modelCodeStrategyMap = new HashMap<>(16);
        modelCodeStrategyMap.put("A00", this::rrPreHandler);
        modelCodeStrategyMap.put("A01", this::irPreHandler);
        modelCodeStrategyMap.put("A02", this::srPreHandler);
    }

    public void preHandler(ApmSpaceApp apmSpaceApp, MSpaceAppData mSpaceAppData) {
        BiConsumer<ApmSpaceApp, MSpaceAppData> strategy = modelCodeStrategyMap.get(apmSpaceApp.getModelCode());
        if (strategy == null) {
            return;
        }
        strategy.accept(apmSpaceApp, mSpaceAppData);
    }

    private void irPreHandler(ApmSpaceApp apmSpaceApp, MSpaceAppData mSpaceAppData) {
        // IR进入复核节点必须判断评审信息必填 字段：需求分析结论-requireAnaCon   需求决策结论requireDecnCon
        MObject mObject = objectModelCrudI.getByBid(apmSpaceApp.getModelCode(), mSpaceAppData.getBid());
        if (mObject == null) {
            return;
        }
        if ("RECHECK".equals(mSpaceAppData.getLifeCycleCode())) {
            if(ObjectUtil.isEmpty(mObject.get("requireAnaCon")) || ObjectUtil.isEmpty(mObject.get("requireDecnCon"))){
                throw new BusinessException("[需求分析结论、需求决策结论]必填");
            }
        }
        // 加锁且是锁定状态以后的需求才能编辑挂载项目
        if (ObjectUtil.isNotEmpty(mSpaceAppData.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()))) {
            if (!Arrays.asList(IrLifeCycleConstant.LOCK,IrLifeCycleConstant.DEVELOP,IrLifeCycleConstant.TEST,IrLifeCycleConstant.CHECK,IrLifeCycleConstant.COMPLETE).contains((mObject.getLifeCycleCode()))
                    || !DemandConstant.YES_LOCK.equals(mObject.get(DemandConstant.LOCK_FLAG))) {
                throw new BusinessException("只有到达锁定状态且加锁的需求才能关联项目");
            }
        }
    }

    private void srPreHandler(ApmSpaceApp apmSpaceApp, MSpaceAppData mSpaceAppData) {
        MObject mObject = objectModelCrudI.getByBid(apmSpaceApp.getModelCode(), mSpaceAppData.getBid());
        if (mObject == null) {
            return;
        }
        // SR进入复核节点必须判断评审信息必填 字段：需求分析结论-requireAnaCon   需求决策结论requireDecnCon
        if ("TEST".equals(mSpaceAppData.getLifeCycleCode())) {
            //查询项目模板
            MObject spaceObjectVo = objectModelCrudI.getByBid("100", apmSpaceApp.getSpaceBid());
            if (spaceObjectVo != null && SPACE_TEMPLATE_BID_SOFT.equals(spaceObjectVo.get(SPACE_TEMPLATE_BID))){
                //SR由开发状态变更为测试状态时，必须校验UI检视结果是否填写，没有填写无法更改状态，并给出提示：UI检视结果没有填写，请填写后再变更状态
                if (ObjectUtil.isEmpty(mObject.get("uiinspectionResult"))) {
                    throw new BusinessException("UI检视结果没有填写，请填写后再变更状态");
                }
                // 当研发选择需求管控策略为“Flag-运行时”，只校验FLAG名称一栏是否填写了内容，没填写不能修改到测试状态，当研发选择需求管控策略为“NA”，只校验原因说明一栏，如果原因说明一栏没有填写内容，也不能修改到测试状态
                if ("runtime".equals(mObject.get("requirementControlStrategy"))) {
                    if (ObjectUtil.isEmpty(mObject.get("flagMacroControlName"))) {
                        throw new BusinessException("[Flag宏控名称]必填");
                    }
                } else if ("NotDefined".equals(mObject.get("requirementControlStrategy"))) {
                    if (ObjectUtil.isEmpty(mObject.get("explanation"))) {
                        throw new BusinessException("[原因说明]必填");
                    }
                } else if(ObjectUtil.isEmpty(mObject.get("requirementControlStrategy")) || ObjectUtil.isEmpty(mObject.get("flagMacroControlName")) ||
                        ObjectUtil.isEmpty(mObject.get("explanation"))){
                    throw new BusinessException("[需求配置]必填");
                }
            }
        }
        // 加锁的需求才能编辑挂载项目
        if (ObjectUtil.isNotEmpty(mSpaceAppData.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()))) {
            if (!DemandConstant.YES_LOCK.equals(mObject.get(DemandConstant.LOCK_FLAG))) {
                throw new BusinessException("只有加锁的需求才能关联项目");
            }
        }
    }

    private void rrPreHandler(ApmSpaceApp apmSpaceApp, MSpaceAppData mSpaceAppData) {
        // RR如果是被重复需求，退回原因不能是重复需求
        if ("redundant".equals(mSpaceAppData.get("returnReason"))
                || "Y".equals(mSpaceAppData.get("repetitiveDemand")) || ObjectUtil.isNotEmpty(mSpaceAppData.get("duplicateRequirementNumber"))) {
            MObject mObject = objectModelCrudI.getByBid(apmSpaceApp.getModelCode(), mSpaceAppData.getBid());
            if (mObject == null) {
                return;
            }
            if("Y".equals(mObject.get("isItADuplicateRequirement"))){
                throw new BusinessException("该需求已被标记重复，请勿二次关联！");
            }
        }
    }
}
