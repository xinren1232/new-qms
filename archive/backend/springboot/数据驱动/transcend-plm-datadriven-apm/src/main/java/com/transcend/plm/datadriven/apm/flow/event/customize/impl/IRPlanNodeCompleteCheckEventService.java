package com.transcend.plm.datadriven.apm.flow.event.customize.impl;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCompleteCheckEvent;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.util.FlowCheckConstant;
import com.transcend.plm.datadriven.apm.flow.util.FlowCheckEnum;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/08/13
 * @description IR计划节点校验
 */
@Service("iRPlanNodeCompleteCheckEventService")
public class IRPlanNodeCompleteCheckEventService implements IFlowCompleteCheckEvent {

    /**
     * IR评估结论关系
     */
    @Value("${transcend.plm.apm.irPgRelation.modelCode:A6A}")
    private String irPgRelationModelCode;

    @Value("${transcend.plm.apm.irPg.modelCode:A69}")
    private String targetModelCode;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;


    @Override
    public void check(ApmSpaceApp app, ApmFlowInstanceNode currentNode, MObject mObject) {
       //校验 RAT评审的需求价值、产品方案、技术可行性评估，并且单选，三种类型可重复创建，点击提交时必须校验，三种评审类型存在，并且三种评审结果为：GO或者GO with risk
        RelationMObject relationMObject = RelationMObject.builder().sourceModelCode(app.getModelCode()).targetModelCode(targetModelCode).relationModelCode(irPgRelationModelCode).sourceBid(mObject.getBid()).build();
        List<MObject> mObjects = objectModelCrudI.listRelationMObjects(relationMObject);
        if (CollectionUtils.isEmpty(mObjects)){
            throw new BusinessException("RAT价值评审未通过,请线下拉通相关领域组织RAT价值评审,通过后再提交");
        }
        //对应的IR评估结论数据进行校验
        //当前优先级
        String requirementlevel = mObject.get(FlowCheckConstant.REQUIREMENTLEVEL)+"";
        List<String> veviewResults = Arrays.asList("GO","Go with risk");
        boolean hasDemandvalue = false;
        boolean hasProductProposal = false;
        boolean hasTechnicalFeasibility = false;
        //重大资产
        double estimatedSignificantAssetInvestmentCosts = 0;
        if(mObject.get("estimatedSignificantAssetInvestmentCosts") != null){
            estimatedSignificantAssetInvestmentCosts = Double.parseDouble(mObject.get("estimatedSignificantAssetInvestmentCosts")+"");
        }
        boolean estimatedPass = false;
        if(estimatedSignificantAssetInvestmentCosts <= 50){
            estimatedPass = true;
        }
        boolean aAndSPass = false;
        for (MObject tarMobject : mObjects) {
            /*reviewType评审类型   字典：RATREVIEWRAT评审
            rmtreviewcontent RMT评审内容   字典：Demandvalue 需求价值  ProductProposal 产品方案  technicalfeasibility 技术可行性
            reviewResults 评审结果 字典： GO        Go with risk*/
            String reviewType = tarMobject.get("reviewType")+"";
            String rmtreviewcontent = tarMobject.get("rmtreviewcontent")+"";
            String reviewResults = tarMobject.get("reviewResults")+"";
            if(FlowCheckEnum.RATREVIEW.getCode().equals(reviewType) && veviewResults.contains(reviewResults)){
               if(FlowCheckConstant.DEMANDVALUE.equals(rmtreviewcontent)){
                   hasDemandvalue = true;
               }else if("ProductProposal".equals(rmtreviewcontent)){
                   hasProductProposal = true;
               }else if("technicalfeasibility".equals(rmtreviewcontent)){
                   hasTechnicalFeasibility = true;
               }
             }
            if(estimatedSignificantAssetInvestmentCosts > 50){
                //判断重大资产
                if(FlowCheckEnum.RMTREVIEW.getCode().equals(reviewType) && veviewResults.contains(reviewResults) && FlowCheckConstant.ZHONGDATOUZI.equals(rmtreviewcontent)){
                    estimatedPass = true;
                }
            }
            //判断A和S条件
            if(FlowCheckEnum.RMTREVIEW.getCode().equals(reviewType) && veviewResults.contains(reviewResults) && FlowCheckConstant.DEMANDVALUE.equals(rmtreviewcontent)){
                aAndSPass = true;
            }
        }
        if(!hasDemandvalue){
            throw new BusinessException("RAT价值评审未通过,请线下拉通相关领域组织RAT价值评审,通过后再提交");
        }
        if(!hasProductProposal){
            throw new BusinessException("RAT方案评审未通过,请线下拉通相关领域组织RAT方案与技术可行性评审会,通过后再提交");
        }
        if(!hasTechnicalFeasibility){
            throw new BusinessException("RAT方案评审未通过,请线下拉通相关领域组织RAT方案与技术可行性评审会,通过后再提交");
        }
        //判断是否满足条件
        if(estimatedSignificantAssetInvestmentCosts > 50){
            if(!estimatedPass){
                throw new BusinessException("重大资产投入超50万,需通过RMT评审,请准备材料在RMT上会");
            }
        }
        if(hasDemandvalue && hasProductProposal && hasTechnicalFeasibility){
            if(FlowCheckConstant.A_STR.equals(requirementlevel) || FlowCheckConstant.S_STR.equals(requirementlevel)){
                //A 和 S
                if(!aAndSPass){
                    throw new BusinessException("S/A类需求,需通过RMT评审,请准备材料在RMT上会");
                }
            }
        }else{
            throw new BusinessException("IR评估结论数据不满足条件");
        }

    }
}
