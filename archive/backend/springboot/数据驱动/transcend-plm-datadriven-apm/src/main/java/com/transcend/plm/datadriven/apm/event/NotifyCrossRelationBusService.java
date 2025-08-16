package com.transcend.plm.datadriven.apm.event;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.eventbus.Subscribe;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.event.config.NotifyCross;
import com.transcend.plm.datadriven.apm.event.config.NotifyRevisionRelTaskConfig;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyCrossRelationEventBusDto;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyReversionRelTaskBusDto;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知跨关系的事件总线服务
 *
 * @author yinbin
 * @version:
 * @date 2023/10/25 15:35
 */
@Slf4j
@Service
public class NotifyCrossRelationBusService {


    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI<MObject> objectModelCrudI;
    @Resource
    private IApmSpaceAppConfigManageService iApmSpaceAppConfigManageService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private IAppDataService appDataService;

    public NotifyCrossRelationBusService() {
        NotifyEventBus.register(this);
    }

    @Subscribe
    public void subscribeNotifyCrossRelationEvent(NotifyCrossRelationEventBusDto notifyCrossRelationEventBusDto) {
        log.info("迭代或者版本选取需求, 跨级关系关联 subscribeEvent: {}", notifyCrossRelationEventBusDto);
        String jobNumber = notifyCrossRelationEventBusDto.getJobNumber();
        Long tenantId = SsoHelper.getTenantId();
        String config = notifyCrossRelationEventBusDto.getConfig();
        String spaceBid = notifyCrossRelationEventBusDto.getSpaceBid();
        String spaceAppBid = notifyCrossRelationEventBusDto.getSpaceAppBid();
        String currentRelationModelCode = notifyCrossRelationEventBusDto.getCurrentRelationModelCode();
        String currentTargetModelCode = notifyCrossRelationEventBusDto.getCurrentTargetModelCode();
        String sourceBid = notifyCrossRelationEventBusDto.getCurrentSourceBid();
        List<? extends MObject> currentTargetList = notifyCrossRelationEventBusDto.getCurrentTargetList();
        if (StringUtils.isBlank(config)) {
            return;
        }
        Map<String, NotifyCross> notifyCrossMap = JSON.parseObject(config, new TypeReference<Map<String, NotifyCross>>() {
        });
        // 查询currentTargetModel的对应的视图
        CfgViewVo cfgViewVoTemp = null;
        if(StringUtils.isNotBlank(spaceAppBid)){
            cfgViewVoTemp = iApmSpaceAppConfigManageService.baseViewGet(spaceAppBid);;
        }
        CfgViewVo cfgViewVo = cfgViewVoTemp;
        transactionTemplate.execute(status -> {
            String currentSourceBid;
            NotifyCross notifyCross = notifyCrossMap.get(currentRelationModelCode);
            while (Objects.nonNull(notifyCross)) {
                String sourceModelCode = notifyCross.getSourceModel();
                String relationModelCode = notifyCross.getRelationModel();
                String betweenRelModel = notifyCross.getBetweenRelModel();
                // 查询中间关系 找出上层的bid,dataBid ex：当前是版本->迭代,项目->迭代,通过迭代bid查询版本的bid和项目bid
                List<MObject> betweenRelObjectList = objectModelCrudI.listSourceMObjects(betweenRelModel, sourceModelCode, Collections.singletonList(sourceBid));
                if (CollectionUtils.isEmpty(betweenRelObjectList)) {
                    notifyCross = notifyCross.getParentRelation();
                    continue;
                }
                MObject currentObject = betweenRelObjectList.get(0);
                currentSourceBid = currentObject.getBid();
                String currentSourceDataBid = (String) currentObject.get(RelationEnum.DATA_BID.getCode());
                // 查询当前源的目标实例
                List<MObject> relationList = objectModelCrudI.listRelationMObjects(RelationMObject.builder().sourceBid(currentSourceBid)
                        .relationModelCode(relationModelCode).targetModelCode(currentTargetModelCode)
                        .sourceModelCode(sourceModelCode)
                        .build());
                Assert.notNull(relationList, "relationList is null");
                Set<String> hasAdded = relationList.stream().map(MObject::getBid).collect(Collectors.toSet());
                // 关系数据列表
                List<MObject> relationObjectList = Lists.newArrayList();
                // 目标实例数据bid
                List<String> bidList = Lists.newArrayList();
                for (MObject mObject : currentTargetList) {
                    String targetBid = mObject.getBid();
                    // 和传进来的目标bidList进行比较,绑定过的不再绑定关系,未绑定的绑定关系,并且更新目标的关系组件值
                    if (CollectionUtils.isEmpty(hasAdded) || !hasAdded.contains(targetBid)) {
                        bidList.add(mObject.getBid());
                        MObject relationObject = new MObject();
                        relationObject.setBid(SnowflakeIdWorker.nextIdStr());
                        relationObject.put(RelationEnum.DATA_BID.getCode(), SnowflakeIdWorker.nextIdStr());
                        relationObject.put(SpaceAppDataEnum.SPACE_BID.getCode(), spaceBid);
                        relationObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), spaceAppBid);
                        relationObject.put(RelationObjectEnum.SOURCE_BID.getCode(), currentSourceBid);
                        relationObject.put(RelationObjectEnum.SOURCE_DATA_BID.getCode(), currentSourceDataBid);
                        relationObject.put(RelationObjectEnum.TARGET_BID.getCode(), mObject.getBid());
                        relationObject.put(RelationObjectEnum.TARGET_DATA_BID.getCode(), mObject.getOrDefault(RelationEnum.DATA_BID.getCode(), mObject.getBid()));
                        relationObject.put(RelationObjectEnum.DRAFT.getCode(), false);
                        relationObject.setModelCode(relationModelCode);
                        relationObject.setCreatedBy(jobNumber);
                        relationObject.setUpdatedBy(jobNumber);
                        relationObject.setUpdatedTime(LocalDateTime.now());
                        relationObject.setCreatedTime(LocalDateTime.now());
                        relationObject.setEnableFlag(true);
                        relationObject.setDeleteFlag(false);
                        relationObject.setTenantId(tenantId);
                        relationObjectList.add(relationObject);
                    }
                }
                if (CollectionUtils.isNotEmpty(relationObjectList)) {
                    // 绑定新的关系
                    appDataService.addBatch(relationModelCode, relationObjectList);
                }
                if (Objects.nonNull(cfgViewVo)) {
                    // 找出关系modelCode相同的关系组件
                    Optional<CfgViewMetaDto> first = cfgViewVo.getMetaList().stream().filter(metaVo -> ViewComponentEnum.RELATION_CONSTANT.equals(metaVo.getType())
                            && relationModelCode.equals(metaVo.getRelationModelCode())).findFirst();
                    if (first.isPresent() && CollectionUtils.isNotEmpty(bidList)) {
                        CfgViewMetaDto cfgViewMetaDto = first.get();
                        MObject mObject = new MObject();
                        mObject.put(cfgViewMetaDto.getName(), currentSourceBid);
                        objectModelCrudI.batchUpdatePartialContentByIds(currentTargetModelCode, mObject, bidList);
                    }
                }
                notifyCross = notifyCross.getParentRelation();
            }
            return true;
        });
    }

    /**
     * 当前迭代 关联需求, 找出需求关联的任务,将任务绑定到当前迭代
     *
     * @param notifyReversionRelTaskBusDto notifyReversionRelTaskBusDto
     * @version: 1.0
     * @date: 2023/10/28 15:42
     * @author: bin.yin
     */
    @Subscribe
    public void subscribeNotifyCrossRelationEvent(NotifyReversionRelTaskBusDto notifyReversionRelTaskBusDto) {
        log.info("迭代选取需求,将任务关联到迭代, 跨级关系关联 subscribeEvent: {}", notifyReversionRelTaskBusDto);
        String jobNumber = notifyReversionRelTaskBusDto.getJobNumber();
        Long tenantId = SsoHelper.getTenantId();
        String config = notifyReversionRelTaskBusDto.getConfig();
        String spaceBid = notifyReversionRelTaskBusDto.getSpaceBid();
        String spaceAppBid = notifyReversionRelTaskBusDto.getSpaceAppBid();
        String currentSourceBid = notifyReversionRelTaskBusDto.getCurrentSourceBid();
        String currentSourceDataBid = notifyReversionRelTaskBusDto.getCurrentSourceDataBid();
        String currentRelationModelCode = notifyReversionRelTaskBusDto.getCurrentRelationModelCode();
        String currentTargetModelCode = notifyReversionRelTaskBusDto.getCurrentTargetModelCode();
        String currentSourceModelCode = notifyReversionRelTaskBusDto.getCurrentSourceModelCode();
        List<? extends MObject> currentTargetList = notifyReversionRelTaskBusDto.getCurrentTargetList();
        if (StringUtils.isBlank(config) || CollectionUtils.isEmpty(currentTargetList)) {
            return;
        }
        Map<String, NotifyRevisionRelTaskConfig> notifyCrossMap = JSON.parseObject(config, new TypeReference<Map<String, NotifyRevisionRelTaskConfig>>() {
        });
        NotifyRevisionRelTaskConfig notifyRevisionRelTaskConfig = notifyCrossMap.get(currentRelationModelCode);
        if (Objects.isNull(notifyRevisionRelTaskConfig)) {
            return;
        }
        // 查询currentTargetModel对应的空间应用bid
        List<ApmSpaceApp> spaceAppList = apmSpaceAppService.listSpaceAppBySpaceBidAndModelCode(spaceBid, currentTargetModelCode);
        // 需求-任务 关系modelCode
        String queryRelModel = notifyRevisionRelTaskConfig.getQueryRelModel();
        // 任务modelCode
        String queryTargetModel = notifyRevisionRelTaskConfig.getQueryTargetModel();
        // 迭代-任务 关系modelCode
        String parentRelModel = notifyRevisionRelTaskConfig.getParentRelModel();
        // 所有需求bid作为查询条件,查询所有需求关联的任务bid,dataBid
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.SOURCE_BID.getColumn(), currentTargetList.stream().map(MObject::getBid).collect(Collectors.toList()));
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        transactionTemplate.execute(status -> {
            // 查询当前迭代已经绑定的任务
            List<MObject> revisionRelTaskList = objectModelCrudI.listRelationMObjects(RelationMObject.builder().sourceBid(currentSourceBid)
                    .relationModelCode(parentRelModel).targetModelCode(queryTargetModel)
                    .sourceModelCode(currentSourceModelCode)
                    .build());
            // 已经绑定的任务bid set
            Set<String> hasAdded = revisionRelTaskList.stream().map(MObject::getBid).collect(Collectors.toSet());
            // 查询需求下的所有任务bid,dataBid
            List<MObject> demandRelTaskList = objectModelCrudI.list(queryRelModel, queryWrappers);
            // 关系数据列表
            List<MObject> relationObjectList = Lists.newArrayList();
            // 目标实例数据bid
            List<String> bidList = Lists.newArrayList();
            for (MObject mObject : demandRelTaskList) {
                String taskBid = (String) mObject.get(RelationEnum.TARGET_BID.getCode());
                // 和传进来的目标bidList进行比较,绑定过的不再绑定关系,未绑定的绑定关系,并且更新目标的关系组件值
                if (CollectionUtils.isEmpty(hasAdded) || !hasAdded.contains(taskBid)) {
                    String taskDataBid = (String) mObject.getOrDefault(RelationEnum.DATA_BID.getCode(), mObject.getBid());
                    bidList.add(taskBid);
                    MObject relationObject = new MObject();
                    relationObject.setBid(SnowflakeIdWorker.nextIdStr());
                    relationObject.put(RelationEnum.DATA_BID.getCode(), SnowflakeIdWorker.nextIdStr());
                    relationObject.put(SpaceAppDataEnum.SPACE_BID.getCode(), spaceBid);
                    relationObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), spaceAppBid);
                    relationObject.put(RelationObjectEnum.SOURCE_BID.getCode(), currentSourceBid);
                    relationObject.put(RelationObjectEnum.SOURCE_DATA_BID.getCode(), currentSourceDataBid);
                    relationObject.put(RelationObjectEnum.TARGET_BID.getCode(), taskBid);
                    relationObject.put(RelationObjectEnum.TARGET_DATA_BID.getCode(), taskDataBid);
                    relationObject.put(RelationObjectEnum.DRAFT.getCode(), false);
                    relationObject.setModelCode(parentRelModel);
                    relationObject.setCreatedBy(jobNumber);
                    relationObject.setUpdatedBy(jobNumber);
                    relationObject.setUpdatedTime(LocalDateTime.now());
                    relationObject.setCreatedTime(LocalDateTime.now());
                    relationObject.setEnableFlag(true);
                    relationObject.setDeleteFlag(false);
                    relationObject.setTenantId(tenantId);
                    relationObjectList.add(relationObject);
                }
            }
            if (CollectionUtils.isNotEmpty(relationObjectList)) {
                // 绑定新的关系
                appDataService.addBatch(parentRelModel, relationObjectList);
            }
            Optional<ApmSpaceApp> first = spaceAppList.stream().filter(e -> currentTargetModelCode.equals(e.getModelCode())).findFirst();
            if (first.isPresent()) {
                // 查询currentTargetModel的对应的视图
                CfgViewVo cfgViewVo = iApmSpaceAppConfigManageService.baseViewGet(first.get().getBid());
                if (Objects.nonNull(cfgViewVo)) {
                    // 找出关系modelCode相同的关系组件
                    Optional<CfgViewMetaDto> revisionRelTaskOptional = cfgViewVo.getMetaList().stream().filter(metaVo -> ViewComponentEnum.RELATION_CONSTANT.equals(metaVo.getType())
                            && parentRelModel.equals(metaVo.getRelationModelCode())).findFirst();
                    if (revisionRelTaskOptional.isPresent() && CollectionUtils.isNotEmpty(bidList)) {
                        CfgViewMetaDto cfgViewMetaDto = revisionRelTaskOptional.get();
                        MObject mObject = new MObject();
                        mObject.put(cfgViewMetaDto.getName(), currentSourceBid);
                        objectModelCrudI.batchUpdatePartialContentByIds(currentTargetModelCode, mObject, bidList);
                    }
                }
            }
            return true;
        });
    }
}
