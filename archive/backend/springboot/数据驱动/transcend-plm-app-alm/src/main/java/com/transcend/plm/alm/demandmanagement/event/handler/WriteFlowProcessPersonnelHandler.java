package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.apm.flow.controller.ApmFlowRuntimeController;
import com.transcend.plm.datadriven.apm.flow.pojo.event.ApmFlowInstanceNodeChangeEvent;
import com.transcend.plm.datadriven.apm.flow.pojo.event.ApmFlowInstanceRoleUserChangeEvent;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowApplicationService;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogAddParam;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigDrivenService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * 流程处理人变更时写入实例数据处理人字段
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/3 09:36
 */
@Log4j2
@Service
public class WriteFlowProcessPersonnelHandler {
    @Value("${transcend.alm.writeFlowProcessPersonnel.spaceAppBidList:}")
    private Set<String> writeFlowProcessPersonnel;

    @Resource
    private ObjectModelStandardI<MObject> objectModelCrudI;
    @Resource
    private ApmFlowApplicationService apmFlowApplicationService;
    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;

    @Resource
    private IApmSpaceAppConfigManageService iAmSpaceAppConfigManageService;
    @Resource
    private OperationLogEsService operationLogEsService;

    @Resource
    private IApmSpaceAppConfigDrivenService apmSpaceAppConfigDrivenService;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void handle(ApmFlowInstanceNodeChangeEvent event) {
        Optional.ofNullable(event.getEntityList()).filter(list -> !list.isEmpty())
                .map(list -> list.stream().map(entity -> new Parameter(entity.getSpaceAppBid(), entity.getInstanceBid()))
                        .filter(param -> isSupport(param.spaceAppBid)).distinct())
                .ifPresent(list -> list.forEach(this::write));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void handle(ApmFlowInstanceRoleUserChangeEvent event) {
        Optional.ofNullable(event.getEntityList()).filter(list -> !list.isEmpty())
                .map(list -> list.stream().map(entity -> new Parameter(entity.getSpaceAppBid(), entity.getInstanceBid()))
                        .filter(param -> isSupport(param.spaceAppBid)).distinct())
                .ifPresent(list -> list.forEach(this::write));
    }


    /**
     * 是否支持判断方法
     *
     * @param spaceAppBid 空间应用Bid
     * @return true/false
     */
    private boolean isSupport(String spaceAppBid) {
        return writeFlowProcessPersonnel.contains(spaceAppBid);
    }


    /**
     * 写入当前处理人到实例数据中
     *
     * @param parameter 参数信息
     */
    private void write(Parameter parameter) {
        try {

            Map<String, Set<String>> empMap = apmFlowApplicationService.queryNodeUsersByInstanceBids(
                    Collections.singletonList(parameter.instanceBid));
            // 更新当前处理人到实例数据中
            MSpaceAppData data = new MSpaceAppData();
            data.setBid(parameter.instanceBid);
            data.put(ObjectEnum.HANDLER.getCode(), empMap.get(parameter.instanceBid));

            ApmSpaceApp spaceApp = apmSpaceApplicationService.getSpaceAppByBid(parameter.spaceAppBid);
            MObject oldMObject = objectModelCrudI.getByBid(spaceApp.getModelCode(), parameter.instanceBid);
            objectModelCrudI.updateByBid(spaceApp.getModelCode(), parameter.instanceBid, data);
            //保存修改处理人日志
            Throwable throwable = new Throwable();
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            if (Stream.of(stackTrace).anyMatch(v-> v.getClassName().equals(ApmFlowRuntimeController.class.getName()) &&
                            v.getMethodName().equals("updateRoleUser"))){
                addLog(parameter.instanceBid, spaceApp, empMap.get(parameter.instanceBid), oldMObject);
            }
        } catch (Exception e) {
            log.error("更新当前处理人到实例数据中失败", e);
        }
    }

    private void addLog(String instanceBid, ApmSpaceApp spaceApp, Object value, MObject oldMObject) {
        CfgViewVo cfgViewVo = iAmSpaceAppConfigManageService.baseViewGet(spaceApp.getBid());
        List<CfgViewMetaDto> cfgViewMetaList = Optional.ofNullable(cfgViewVo)
                .map(CfgViewVo::getMetaList)
                .orElse(Collections.emptyList());
        assert cfgViewVo != null;
        Map<String, Object> properties = apmSpaceAppConfigDrivenService.getProperties(cfgViewVo.getContent(), ObjectEnum.HANDLER.getCode());

        CfgViewMetaDto cfgViewMetaDto = cfgViewMetaList.stream().filter(cfgViewMeta -> cfgViewMeta.getName().equals(ObjectEnum.HANDLER.getCode())).findFirst().orElse(CfgViewMetaDto.builder().build());
        OperationLogAddParam operationLogAddParam = OperationLogAddParam.builder().modelCode(spaceApp.getModelCode()).cfgViewMetaDto(cfgViewMetaDto).properties(properties).instanceBid(instanceBid)
                .fieldName(ObjectEnum.HANDLER.getCode()).fieldValue(value).isAppView(true).spaceAppBid(spaceApp.getBid()).spaceBid(spaceApp.getSpaceBid()).build();
        try {
            operationLogAddParam.setCreatedBy(SsoHelper.getJobNumber());
            //日志异步提交
            CompletableFuture.runAsync(() -> operationLogEsService.save(operationLogAddParam, oldMObject), SimpleThreadPool.getInstance());
        } catch (Exception e) {
            log.error("更新数据Bid[" + instanceBid + "],记录日志失败", e);
        }
    }


    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Parameter {
        /**
         * 空间应用Bid
         */
        String spaceAppBid;
        /**
         * 实例数据Bid
         */
        String instanceBid;
    }
}
