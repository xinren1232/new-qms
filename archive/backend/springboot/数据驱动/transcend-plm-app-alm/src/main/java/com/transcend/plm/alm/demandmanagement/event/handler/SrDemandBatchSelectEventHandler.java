package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.entity.ao.SelectAo;
import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.event.entity.RelationBatchSelectEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationBatchSelectEventHandler;
import com.transcend.plm.datadriven.apm.space.model.ApmRelationMultiTreeAddParam;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author unknown
 * SR需求新增事件处理器
 */
@Slf4j
@Component
public class SrDemandBatchSelectEventHandler extends AbstractRelationBatchSelectEventHandler {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Autowired
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;

    @Resource
    private DemandManagementService demandManagementService;

    @Override
    public int getOrder() {
        return 0;
    }
    @Override
    public Object postHandle(RelationBatchSelectEventHandlerParam param, Object result) {
        ApmRelationMultiTreeAddParam addExpandAo = param.getRelationMultiTreeAddParam();
        String sourceBid = addExpandAo.getSourceBid();
        MObject mObject = objectModelCrudI.getByBid("A5G", sourceBid,false);
        if(mObject != null){
            List<MObject> targetList = addExpandAo.getTargetList();
            List<String> targetBidList = targetList.stream().map(MObject::getBid).collect(Collectors.toList());
            List<MObject> targetMObjects = objectModelCrudI.listByBids(targetBidList, "A5F");
            List<List<String>> selectedList = new ArrayList<>();
            for (MObject targetMObject : targetMObjects) {
                List<String> selected = new ArrayList<>();
                if (ObjectUtils.isNotEmpty(targetMObject.get(DemandManagementEnum.PRODUCT_AREA.getCode()))){
                    selected.add(targetMObject.get(DemandManagementEnum.PRODUCT_AREA.getCode()).toString());
                    if (ObjectUtils.isNotEmpty(targetMObject.get("module"))){
                        selected.add(targetMObject.get("module").toString());
                    }
                    selectedList.add(selected);
                }
            }
            if (CollectionUtils.isNotEmpty(selectedList)){
                List<SelectVo> nameList = new ArrayList<>();
                List<SelectVo> domainList = demandManagementService.queryDomainSelection(param.getSpaceBid(), 2, sourceBid, null);
                if (CollectionUtils.isNotEmpty(domainList)){
                    Map<String, SelectVo> domainBidMap = domainList.stream().collect(Collectors.toMap(SelectVo::getBid, Function.identity(), (k1, k2) -> k1));
                    for (List<String> list : selectedList) {
                        SelectVo selectVo = domainBidMap.get(list.get(0));
                        if (selectVo != null){
                            nameList.add(selectVo);
                        }
                    }
                }
                SelectAo selectAo = new SelectAo();
                selectAo.setSelectedList(selectedList);
                selectAo.setNameList(nameList);
                demandManagementService.selectDomain(param.getSpaceBid(), param.getApmSpaceApp().getBid(),sourceBid, 2, selectAo,null,null);
            }
        }
        return super.postHandle(param,result);
    }

    @Override
    public boolean isMatch(RelationBatchSelectEventHandlerParam param) {
        //IR-SR关系
        String relationModelCode = "A5O";
        ApmRelationMultiTreeAddParam addExpandAo = param.getRelationMultiTreeAddParam();
        if(addExpandAo == null){
            return false;
        }
        return relationModelCode.equals(addExpandAo.getRelationModelCode());
    }
}
