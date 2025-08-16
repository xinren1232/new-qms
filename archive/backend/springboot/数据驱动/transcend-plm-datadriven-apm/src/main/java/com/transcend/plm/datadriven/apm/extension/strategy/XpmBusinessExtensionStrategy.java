package com.transcend.plm.datadriven.apm.extension.strategy;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.configcenter.api.model.view.dto.RelationInfo;
import com.transcend.plm.datadriven.apm.dto.NotifyObjectPartialContentDto;
import com.transcend.plm.datadriven.apm.dto.NotifyRelationBatchAddBusDto;
import com.transcend.plm.datadriven.apm.dto.NotifyRelationBatchRemoveBusDto;
import com.transcend.plm.datadriven.apm.extension.service.XpmKpiLogicService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.*;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description XPM KPI业务扩展策略类
 * @date 2024/04/24 09:29
 **/
@Component
@Slf4j
@DependsOn("modelCodeProperties")
public class XpmBusinessExtensionStrategy extends ObjectExtensionStrategy {

    @Resource
    private XpmKpiLogicService service;

    @Override
    @PostConstruct
    public void register() {
        strategyMap.put(KPI_SCHEDULE_MODEL_CODE, this);
        strategyMap.put(KPI_QUALITY_MODEL_CODE, this);
        strategyMap.put(KPI_BEHAVIOR_MODEL_CODE, this);
        strategyMap.put(KPI_VALUE_P_MODEL_CODE, this);
        strategyMap.put(KPI_INFO_SCHEDULE_REL_MODEL_CODE, this);
        strategyMap.put(KPI_INFO_QUALITY_REL_MODEL_CODE, this);
        strategyMap.put(KPI_INFO_BEHAVIOR_REL_MODEL_CODE, this);
        strategyMap.put(KPI_INFO_VALUE_REL_MODEL_CODE, this);
    }

    @Override
    public void batchAdd(NotifyRelationBatchAddBusDto notifyRelationBatchAddBusDto) {
        log.info("XpmBusinessExtensionStrategy batchAdd start, notifyRelationBatchAddBusDto:{}", JSON.toJSONString(notifyRelationBatchAddBusDto));
        if (CollectionUtils.isEmpty(notifyRelationBatchAddBusDto.getData()) ||
                notifyRelationBatchAddBusDto.getRelationInfo() == null
                || notifyRelationBatchAddBusDto.getData().get(0).get("sourceBid") == null) {
            return;
        }
        String sourceBid = String.valueOf(notifyRelationBatchAddBusDto.getData().get(0).get("sourceBid"));
        try {
            service.collectItemScore(notifyRelationBatchAddBusDto.getRelationInfo(), sourceBid);
        } catch (Exception e) {
            log.error("XpmBusinessExtensionStrategy#batchAdd execute error", e);
        }
    }
    @Override
    public void batchRemove(NotifyRelationBatchRemoveBusDto notifyRelationBatchRemoveBusDto) {
        log.info("XpmBusinessExtensionStrategy#batchRemove notifyRelationBatchRemoveBusDto:{}", JSON.toJSONString(notifyRelationBatchRemoveBusDto));
        try {
            RelationInfo relationInfo = new RelationInfo();
            relationInfo.setTargetModelCode(notifyRelationBatchRemoveBusDto.getRelationInfo().getTargetModelCode());
            relationInfo.setSourceModelCode(notifyRelationBatchRemoveBusDto.getRelationInfo().getSourceModelCode());
            relationInfo.setModelCode(notifyRelationBatchRemoveBusDto.getRelationInfo().getModelCode());
            log.info("XpmBusinessExtensionStrategy#batchRemove relationInfo:{}", JSON.toJSONString(relationInfo));
            log.info("XpmBusinessExtensionStrategy#batchRemove relationInfo:{}", JSON.toJSONString(notifyRelationBatchRemoveBusDto.getRelationDelAndRemParamAo()));
            service.collectItemScore(relationInfo, notifyRelationBatchRemoveBusDto.getRelationDelAndRemParamAo().getSourceBid());
        }catch (Exception e){
            log.error("XpmBusinessExtensionStrategy#batchRemove execute error:{}", e.getMessage());
        }
    }

    @Override
    public void updatePartialContent(NotifyObjectPartialContentDto notifyObjectPartialContentDto) {
        log.info("XpmBusinessExtensionStrategy#updatePartialContent notifyObjectPartialContentDto:{}", JSON.toJSONString(notifyObjectPartialContentDto));
        try {
            service.updateInsThenIsCollect(notifyObjectPartialContentDto.getModelCode(),
                    notifyObjectPartialContentDto.getBid(),notifyObjectPartialContentDto.getMSpaceAppData());
        }catch (Exception e){
            log.error("XpmBusinessExtensionStrategy#updatePartialContent execute error:{}", e.getMessage());
        }
    }
}
