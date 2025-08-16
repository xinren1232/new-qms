package com.transcend.plm.datadriven.apm.space.service.context;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.MoveGroupNodeParam;
import com.transcend.plm.datadriven.api.model.dto.MoveTreeNodeParam;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.qo.ResourceQo;
import com.transcend.plm.datadriven.apm.aspect.notify.OperateBusiService;
import com.transcend.plm.datadriven.apm.dto.DeleteRelDto;
import com.transcend.plm.datadriven.apm.dto.StateDataDriveDto;
import com.transcend.plm.datadriven.apm.event.annotation.TranscendEvent;
import com.transcend.plm.datadriven.apm.event.enums.EventHandlerTypeEnum;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowDriveRelate;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.StateDataDriveAO;
import com.transcend.plm.datadriven.apm.space.model.ApmSpaceAppDataDrivenOperationFilterBo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiObjectUpdateDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.*;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLaneGroupData;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ResourceVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transsion.framework.dto.BaseRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 对象数据驱动策略上下文
 * @author yinbin
 * @version:
 * @date 2023/12/18 17:04
 */
@Component
@Primary
public class ApmSpaceAppDataDrivenStrategyContext implements IBaseApmSpaceAppDataDrivenService {
    public static final String NORMAL = "NORMAL";
    public static final String VERSION = "VERSION";

    public static final String STRATEGY_NAME = "AppDataDrivenService";

    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private Map<String, IBaseApmSpaceAppDataDrivenService> strategyMap;

    @Resource
    private OperateBusiService operateBusiService;


    /**
     * 获取数据驱动策略
     * @param spaceAppBid 空间应用bid
     * @return
     */
    private IBaseApmSpaceAppDataDrivenService  getDataDrivenStrategy(String spaceAppBid){
        if (StringUtils.isBlank(spaceAppBid)) {
            return strategyMap.get(NORMAL + STRATEGY_NAME);
        }
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        if (apmSpaceApp == null) {
            return strategyMap.get(NORMAL + STRATEGY_NAME);
        }
        return strategyMap.get((Boolean.TRUE.equals(apmSpaceApp.getIsVersionObject()) ? VERSION : NORMAL) + STRATEGY_NAME);
    }

    private IBaseApmSpaceAppDataDrivenService getDataDrivenStrategy(String spaceBid,String modelCode){
        if (StringUtils.isBlank(spaceBid) || StringUtils.isBlank(modelCode)) {
            return strategyMap.get(NORMAL + STRATEGY_NAME);
        }
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getApmSpaceAppBySpaceBidAndModelCode(spaceBid,modelCode);
        if (apmSpaceApp == null) {
            return strategyMap.get(NORMAL + STRATEGY_NAME);
        }
        return strategyMap.get((Boolean.TRUE.equals(apmSpaceApp.getIsVersionObject()) ? VERSION : NORMAL) + STRATEGY_NAME);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.ADD)
    public MSpaceAppData add(String spaceAppBid, MSpaceAppData mSpaceAppData) {
        MSpaceAppData mSpaceAppDataRes = getDataDrivenStrategy(spaceAppBid).add(spaceAppBid, mSpaceAppData);
        //发送消息
        operateBusiService.executeNotify(spaceAppBid, mSpaceAppData);
        return mSpaceAppDataRes;
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.ADD)
    public MSpaceAppData add(String spaceAppBid, MSpaceAppData mSpaceAppData, ApmSpaceAppDataDrivenOperationFilterBo filterBo) {
        return getDataDrivenStrategy(spaceAppBid).add(spaceAppBid, mSpaceAppData, filterBo);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.LOGICAL_DELETE)
    public boolean logicalDelete(String spaceAppBid, String bid) {
        return getDataDrivenStrategy(spaceAppBid).logicalDelete(spaceAppBid, bid);
    }

    @Override
    public boolean logicDeleteWithOperateAppBid(String spaceAppBid, String bid, String operateAppBid) {
        return getDataDrivenStrategy(spaceAppBid).logicDeleteWithOperateAppBid(spaceAppBid, bid, operateAppBid);
    }

    @Override
    public boolean update(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData) {
        return getDataDrivenStrategy(spaceAppBid).update(spaceAppBid, bid, mSpaceAppData);
    }

    @Override
    public MSpaceAppData get(String spaceAppBid, String bid,boolean checkPermission){

        return getDataDrivenStrategy(spaceAppBid).get(spaceAppBid, bid,checkPermission);
    }

    @Override
    public MSpaceAppData getUpdatePropertyStatus(String spaceAppBid, String bid, boolean checkPermission) {
        return getDataDrivenStrategy(spaceAppBid).getUpdatePropertyStatus(spaceAppBid, bid,checkPermission);
    }

    @Override
    public List<MSpaceAppData> listByModelMixQo(String spaceAppBid, ModelMixQo modelMixQo) {
        return getDataDrivenStrategy(spaceAppBid).listByModelMixQo(spaceAppBid, modelMixQo);
    }

    @Override
    public List<MSpaceAppData> batchGet(String spaceAppBid, List<String> bids) {
        return getDataDrivenStrategy(spaceAppBid).batchGet(spaceAppBid, bids);
    }

    @Override
    public Map<String, List<MSpaceAppData>> batchSpaceAppMapGet(Map<String, List<String>> bids) {
        return getDataDrivenStrategy(null).batchSpaceAppMapGet(bids);
    }

    @Override
    public List<MSpaceAppData> list(String spaceAppBid, QueryCondition queryCondition) {
        return getDataDrivenStrategy(spaceAppBid).list(spaceAppBid, queryCondition);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.PAGE)
    public PagedResult<MSpaceAppData> page(String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        return getDataDrivenStrategy(spaceAppBid).page(spaceAppBid, pageQo, filterRichText);
    }

    @Override
    public PagedResult<MSpaceAppData> pageWithoutPermission(String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        return getDataDrivenStrategy(spaceAppBid).pageWithoutPermission(spaceAppBid, pageQo, filterRichText);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.UPDATE)
    public Boolean updatePartialContent(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData) {
        Boolean res = getDataDrivenStrategy(spaceAppBid).updatePartialContent(spaceAppBid, bid, mSpaceAppData);
        //发送消息
        operateBusiService.executeNotifyUpdate(spaceAppBid,bid, mSpaceAppData);
        return res;
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.UPDATE)
    public Boolean updatePartialContent(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData, Boolean isRecordLog) {
        return getDataDrivenStrategy(spaceAppBid).updatePartialContent(spaceAppBid, bid, mSpaceAppData,isRecordLog);
    }

    @Override
    public GroupListResult<MSpaceAppData> groupList(String spaceAppBid, String groupProperty, QueryCondition queryCondition) {
        return getDataDrivenStrategy(spaceAppBid).groupList(spaceAppBid, groupProperty, queryCondition);
    }

    @Override
    public List<MSpaceAppData> sampleTree(String spaceAppBid, List<QueryWrapper> wrappers) {
        return getDataDrivenStrategy(spaceAppBid).sampleTree(spaceAppBid, wrappers);
    }

    @Override
    public List<MSpaceAppData> signObjectAndMultiSpaceTree(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean filterRichText) {
        return getDataDrivenStrategy(spaceAppBid).signObjectAndMultiSpaceTree(spaceBid,spaceAppBid, modelMixQo, filterRichText);
    }

    @Override
    public List<MObjectTree> signObjectAndMultiSpaceTreeList(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean filterRichText) {
        return getDataDrivenStrategy(spaceAppBid).signObjectAndMultiSpaceTreeList(spaceBid,spaceAppBid, modelMixQo, filterRichText);
    }

    @Override
    public Boolean moveTreeNode(String spaceAppBid, List<MoveTreeNodeParam> moveTreeNodeParams) {
        return getDataDrivenStrategy(spaceAppBid).moveTreeNode(spaceAppBid, moveTreeNodeParams);
    }

    @Override
    public Boolean moveTreeNodeByModelCode(String spaceBid, String modelCode, List<MoveTreeNodeParam> moveTreeNodeParams) {
        return getDataDrivenStrategy(spaceBid,modelCode).moveTreeNodeByModelCode(spaceBid, modelCode, moveTreeNodeParams);
    }

    @Override
    public List<MObjectTree> multiTree(String spaceBid, String spaceAppBid, ApmMultiTreeModelMixQo modelMixQo,
                                       boolean filterRichText, Boolean checkPermission) {
        return getDataDrivenStrategy(spaceAppBid).multiTree(spaceBid, spaceAppBid, modelMixQo, filterRichText, checkPermission);
    }

    @Override
    public GroupListResult<MSpaceAppData> multiTreeGroupBy(String spaceBid, String spaceAppBid, String groupProperty, List<QueryWrapper> wrappers, List<Order> orders) {
        return getDataDrivenStrategy(spaceAppBid).multiTreeGroupBy(spaceBid, spaceAppBid, groupProperty, wrappers, orders);
    }

    @Override
    public boolean copyMultiTree(MObjectTree mObjectTree, String copyInstanceBid, String spaceBid, String spaceAppBid) {
        return getDataDrivenStrategy(spaceAppBid).copyMultiTree(mObjectTree, copyInstanceBid, spaceBid, spaceAppBid);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.BATCH_DELETE)
    public Boolean batchLogicalDeleteByBids(String spaceAppBid, List<String> bids) {
        return getDataDrivenStrategy(spaceAppBid).batchLogicalDeleteByBids(spaceAppBid, bids);
    }

    @Override
    public Boolean batchLogicalDeleteByBids(String spaceAppBid, List<String> bids, Boolean checkPermission) {
        return getDataDrivenStrategy(spaceAppBid).batchLogicalDeleteByBids(spaceAppBid, bids, checkPermission);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.BATCH_LOGIC_DELETE)
    public Boolean batchLogicalDelete(String spaceAppBid, BatchLogicDelAndRemQo qo) {
        return getDataDrivenStrategy(spaceAppBid).batchLogicalDelete(spaceAppBid, qo);
    }

    @Override
    public Boolean deleteRel(String spaceAppBid,DeleteRelDto dto) {
        return getDataDrivenStrategy(spaceAppBid).deleteRel(spaceAppBid, dto);
    }

    @Override
    public Boolean addBatch(String spaceBid, String spaceAppBid, List<MSpaceAppData> targetMObjects, ApmSpaceAppDataDrivenOperationFilterBo filterBo) {
        return getDataDrivenStrategy(spaceAppBid).addBatch(spaceBid, spaceAppBid, targetMObjects, filterBo);
    }

    @Override
    public Boolean moveGroupNode(String spaceAppBid, String groupProperty, List<MoveGroupNodeParam> moveGroupNodeParams) {
        return getDataDrivenStrategy(spaceAppBid).moveGroupNode(spaceAppBid, groupProperty, moveGroupNodeParams);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.COMPLETE_FLOW_NODE)
    public Boolean updatePartialContentAndCompleteFlowNode(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData) {
        return getDataDrivenStrategy(spaceAppBid).updatePartialContentAndCompleteFlowNode(spaceAppBid, bid, nodeBid, mSpaceAppData);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.ROLLBACK_FLOW_NODE)
    public Boolean updatePartialContentAndRollbackFlowNode(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData, boolean runEvent) {
        return getDataDrivenStrategy(spaceAppBid).updatePartialContentAndRollbackFlowNode(spaceAppBid, bid, nodeBid, mSpaceAppData, runEvent);
    }

    @Override
    public MObject executeAction(String spaceAppBid, String instanceBid, String actionBid, MSpaceAppData mSpaceAppData) {
        return getDataDrivenStrategy(spaceAppBid).executeAction(spaceAppBid, instanceBid, actionBid, mSpaceAppData);
    }

    @Override
    public List<ResourceVo> getProjectResources(ResourceQo resourceQo) {
        return getDataDrivenStrategy(null).getProjectResources(resourceQo);
    }

    @Override
    public List<ResourceVo> getSpaceResources(String spaceBid, ResourceQo resourceQo) {
        return getDataDrivenStrategy(null).getSpaceResources(spaceBid, resourceQo);
    }

    @Override
    public ApmLaneGroupData listLane(String spaceBid, String spaceAppBid, ApmLaneQO apmLaneQO) {
        return getDataDrivenStrategy(spaceAppBid).listLane(spaceBid, spaceAppBid, apmLaneQO);
    }

    @Override
    public List<StateDataDriveDto> updateDriveState(StateDataDriveAO stateDataDriveAO, ApmFlowDriveRelate apmFlowDriveRelate) {
        return getDataDrivenStrategy(null).updateDriveState(stateDataDriveAO, apmFlowDriveRelate);
    }

    @Override
    public List<MSpaceAppData> importExcel(String spaceAppBid, MultipartFile file, String spaceBid) {
        return getDataDrivenStrategy(spaceAppBid).importExcel(spaceAppBid, file, spaceBid);
    }

    @Override
    public boolean importExcelAndSave(String spaceAppBid, MultipartFile file, String spaceBid) {
        return getDataDrivenStrategy(spaceAppBid).importExcelAndSave(spaceAppBid, file, spaceBid);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.CHECK_OUT)
    public MSpaceAppData checkOut(String spaceAppBid, String bid) {
        return getDataDrivenStrategy(spaceAppBid).checkOut(spaceAppBid, bid);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.BATCH_CHECK_OUT)
    public Map<String, MSpaceAppData> batchCheckOut(String spaceAppBid, List<String> bids) {
        return getDataDrivenStrategy(spaceAppBid).batchCheckOut(spaceAppBid, bids);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.CANCEL_CHECK_OUT)
    public MSpaceAppData cancelCheckOut(String spaceAppBid, String bid) {
        return getDataDrivenStrategy(spaceAppBid).cancelCheckOut(spaceAppBid, bid);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.BATCH_CHECK_OUT)
    public Map<String, MSpaceAppData> batchCancelCheckOut(String spaceAppBid, List<String> bids) {
        return getDataDrivenStrategy(spaceAppBid).batchCancelCheckOut(spaceAppBid, bids);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.CHECK_IN)
    public MSpaceAppData checkIn(String spaceAppBid, MVersionObject mObject) {
        return getDataDrivenStrategy(spaceAppBid).checkIn(spaceAppBid, mObject);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.BATCH_CHECK_IN)
    public List<MSpaceAppData> batchCheckin(String spaceAppBid, List<MVersionObject> list) {
        return getDataDrivenStrategy(spaceAppBid).batchCheckin(spaceAppBid, list);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.SAVE_DRAFT)
    public Boolean saveDraft(String spaceAppBid, MVersionObject mObject) {
        return getDataDrivenStrategy(spaceAppBid).saveDraft(spaceAppBid, mObject);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.BATCH_SAVE_DRAFT)
    public Boolean batchSaveDraft(String spaceAppBid, List<MVersionObject> draftList) {
        return getDataDrivenStrategy(spaceAppBid).batchSaveDraft(spaceAppBid, draftList);
    }

    @Override
    public List<MSpaceAppData> listByHistory(String spaceAppBid, String dataBid, QueryCondition queryCondition) {
        return getDataDrivenStrategy(spaceAppBid).listByHistory(spaceAppBid, dataBid, queryCondition);
    }

    @Override
    public PagedResult<MSpaceAppData> pageListByHistory(String spaceAppBid, String dataBid, BaseRequest<QueryCondition> queryCondition, boolean filterRichText) {
        return getDataDrivenStrategy(spaceAppBid).pageListByHistory(spaceAppBid, dataBid, queryCondition, filterRichText);
    }

    @Override
    public List<Object> listPropertyDistinct(String spaceAppBid, String distinctProperty, QueryCondition queryCondition) {
        return getDataDrivenStrategy(spaceAppBid).listPropertyDistinct(spaceAppBid, distinctProperty, queryCondition);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.REVISE)
    public MSpaceAppData revise(String spaceAppBid, String bid) {
        return getDataDrivenStrategy(spaceAppBid).revise(spaceAppBid, bid);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.PROMOTE)
    public MSpaceAppData promote(String spaceBid, String spaceAppBid, LifeCyclePromoteDto promoteDto) {
        return getDataDrivenStrategy(spaceAppBid).promote(spaceBid, spaceAppBid, promoteDto);
    }

    @Override
    public ApmLaneGroupData listAllLane(String spaceBid, String spaceAppBid, ApmLaneAllQo laneAllQo) {
        return getDataDrivenStrategy(spaceAppBid).listAllLane(spaceBid, spaceAppBid, laneAllQo);
    }

    @Override
    public Boolean batchRemove(String spaceAppBid, BatchLogicDelAndRemQo qo) {
        return getDataDrivenStrategy(spaceAppBid).batchRemove(spaceAppBid, qo);
    }

    @Override
    public Boolean batchRemoveRelation(String spaceAppBid, BatchRemoveRelationQo qo) {
        return getDataDrivenStrategy(spaceAppBid).batchRemoveRelation(spaceAppBid, qo);
    }

    @Override
    public List<Map<String, Object>> userCache(String spaceAppBid, String modelCode, List<String> userIds) {
        return getDataDrivenStrategy(spaceAppBid).userCache(spaceAppBid, modelCode, userIds);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.BATCH_ADD)
    public List<MSpaceAppData> batchAdd(String spaceBid, String spaceAppBid, List<MSpaceAppData> mSpaceAppDataList) {
        return getDataDrivenStrategy(spaceAppBid).batchAdd(spaceBid, spaceAppBid, mSpaceAppDataList);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.SAVE_COMMON_DRAFT)
    public MSpaceAppData saveCommonDraft(String spaceAppBid, MSpaceAppData mSpaceAppData) {
        return getDataDrivenStrategy(spaceAppBid).saveCommonDraft(spaceAppBid, mSpaceAppData);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.ADD)
    public MSpaceAppData addTonesData(String spaceAppBid, MSpaceAppData mSpaceAppData) {
        MSpaceAppData mSpaceAppDataRes = getDataDrivenStrategy(spaceAppBid).addTonesData(spaceAppBid, mSpaceAppData);
        return mSpaceAppDataRes;
    }

    @Override
    public String batchAddForCad(String spaceBid, String spaceAppBid, List<MSpaceAppData> mSpaceAppDataList) {
        return getDataDrivenStrategy(spaceAppBid).batchAddForCad(spaceAppBid, spaceAppBid, mSpaceAppDataList);
    }

    @Override
    public List<MSpaceAppData> batchAddForCadResult(String spaceBid, String spaceAppBid, String resultId) {
        return getDataDrivenStrategy(spaceAppBid).batchAddForCadResult(spaceAppBid, spaceAppBid, resultId);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.BATCH_CHECK_IN)
    public String batchCheckinAsync(String spaceAppBid, List<MVersionObject> list) {
        return getDataDrivenStrategy(spaceAppBid).batchCheckinAsync(spaceAppBid, list);
    }

    @Override
    public Map<String, Integer> batchCheckinAsyncProgress(String spaceAppBid, String resultId) {
        return getDataDrivenStrategy(spaceAppBid).batchCheckinAsyncProgress(spaceAppBid, resultId);
    }

    /**
     * 批量更新部分内容
     *
     * @param spaceBid spaceBid
     * @param spaceAppBid   spaceAppBid
     * @param mSpaceAppData 待更新数据
     * @param bids          待更新数据的bids
     * @return {@link Boolean}
     */
    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.BATCH_UPDATE)
    public Boolean batchUpdatePartialContentByBids(String spaceBid, String spaceAppBid, List<String> bids, MSpaceAppData mSpaceAppData) {
        return getDataDrivenStrategy(spaceAppBid).batchUpdatePartialContentByBids(spaceBid, spaceAppBid, bids, mSpaceAppData);
    }

    /**
     * 批量更新部分内容
     *
     * @param spaceAppBid   spaceAppBid
     * @param multiObjectUpdateDtoList 待更新数据
     * @return {@link Boolean}
     */
    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.MULTI_OBJECT_UPDATE)
    public  Boolean multiObjectPartialContent(String spaceAppBid, List<MultiObjectUpdateDto> multiObjectUpdateDtoList) {
        return getDataDrivenStrategy(spaceAppBid).multiObjectPartialContent(spaceAppBid, multiObjectUpdateDtoList);
    }

    /**
     * 批量检查权限
     *
     * @param spaceBid
     * @param spaceAppBid
     * @param bids
     * @param operation
     * @return
     */
    @Override
    public Boolean batchOperationCheckPermission(String spaceBid, String spaceAppBid, List<String> bids, String operation) {
        return getDataDrivenStrategy(spaceAppBid).batchOperationCheckPermission(spaceBid, spaceAppBid, bids, operation);
    }

    /**
     * 批量逻辑删除(根据视图模式)
     *
     * @param viewModel   视图模式
     * @param spaceAppBid spaceAppBid
     * @param bids        bids
     * @return
     */
    @Override
    public Boolean batchViewModelLogicalDeleteByBids(String viewModel, String spaceAppBid, List<String> bids) {
        return getDataDrivenStrategy(spaceAppBid).batchViewModelLogicalDeleteByBids(viewModel, spaceAppBid, bids);
    }
}
