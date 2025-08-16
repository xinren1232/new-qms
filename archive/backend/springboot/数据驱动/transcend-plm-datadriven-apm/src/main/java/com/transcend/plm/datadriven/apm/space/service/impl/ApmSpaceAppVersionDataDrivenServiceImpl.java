package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.core.model.api.page.PagedResultDeserialize;
import com.transcend.framework.open.entity.EmployeeInfo;
import com.transcend.framework.open.service.OpenUserService;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.MoveGroupNodeParam;
import com.transcend.plm.datadriven.api.model.dto.MoveTreeNodeParam;
import com.transcend.plm.datadriven.api.model.dto.ReviseDto;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.qo.ResourceQo;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.dto.DeleteRelDto;
import com.transcend.plm.datadriven.apm.dto.StateDataDriveDto;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowDriveRelate;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowTemplateService;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.StateDataDriveAO;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionCheckDto;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionCheckService;
import com.transcend.plm.datadriven.apm.space.model.ApmSpaceAppDataDrivenOperationFilterBo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiObjectUpdateDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.*;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLaneGroupData;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLifeCycleStateVO;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ResourceVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.po.CadBatchAddInstanceRecord;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.CadBatchAddInstanceRecordService;
import com.transcend.plm.datadriven.apm.space.service.*;
import com.transcend.plm.datadriven.apm.space.service.context.ApmSpaceAppDataDrivenStrategyContext;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.pool.IOThreadPool;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import com.transcend.plm.datadriven.domain.object.base.VersionModelDomainService;
import com.transsion.framework.auth.IUser;
import com.transsion.framework.auth.IUserContext;
import com.transsion.framework.auth.UserContextDto;
import com.transsion.framework.auth.dto.UserLoginDto;
import com.transsion.framework.context.holder.UserContextHolder;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import com.transsion.framework.tool.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yinbin
 * @version:
 * @date 2023/12/18 17:03
 */
@Service(ApmSpaceAppDataDrivenStrategyContext.VERSION + ApmSpaceAppDataDrivenStrategyContext.STRATEGY_NAME)
@Slf4j
public class ApmSpaceAppVersionDataDrivenServiceImpl implements IApmSpaceAppVersionDataDrivenService {

    @Resource
    private VersionModelDomainService versionModelDomainService;
    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;
    @Resource
    private IApmSpaceAppConfigDrivenService IApmSpaceAppConfigDrivenService;
    @Resource
    private IRuntimeService runtimeService;

    @Resource
    private CadBatchAddInstanceRecordService cadBatchAddInstanceRecordService;
    @Resource
    private ApmFlowTemplateService apmFlowTemplateService;

    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;

    @Resource
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IRedisService iRedisService;

    @Resource
    private ObjectModelDomainService objectModelDomainService;
    @Resource
    private IAppDataService appDataService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private IPermissionCheckService permissionCheckService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private OpenUserService openUserService;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MSpaceAppData add(String spaceAppBid, MSpaceAppData mSpaceAppData) {
        //获取空间应用数据
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        // 初始化赋值
        mSpaceAppData.setSpaceBid(app.getSpaceBid());
        mSpaceAppData.setSpaceAppBid(spaceAppBid);
        // 初始化固定值
        String modelCode = app.getModelCode();
        MVersionObject mVersionObject = new MVersionObject();
        mVersionObject.putAll(mSpaceAppData);
        mVersionObject = versionModelDomainService.add(modelCode, mVersionObject);
        mSpaceAppData.putAll(mVersionObject);
        // 发流程
        if (mSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) != null && StringUtils.isNotEmpty(mSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) + "")) {
            runtimeService.startProcess(app.getSpaceBid(), spaceAppBid, mSpaceAppData, Boolean.FALSE);
            //检查流程 是否正常启动，对异常启动做补偿
            CompletableFuture.runAsync(() -> runtimeService.runStartNode(mSpaceAppData.getBid(),app.getSpaceBid(),spaceAppBid), SimpleThreadPool.getInstance());
        }
        return mSpaceAppData;
    }

    @Override
    public MSpaceAppData add(String spaceAppBid, MSpaceAppData mSpaceAppData, ApmSpaceAppDataDrivenOperationFilterBo filterBo) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public boolean logicalDelete(String spaceAppBid, String bid) {
        return versionModelDomainService.logicalDeleteByBid(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid), bid);
    }

    @Override
    public boolean logicDeleteWithOperateAppBid(String spaceAppBid, String bid, String operateAppBid) {
        return versionModelDomainService.logicalDeleteByBid(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid), bid);
    }

    @Override
    public boolean update(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public MSpaceAppData get(String spaceAppBid, String bid,boolean checkPermission) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        String modelCode = apmSpaceApp.getModelCode();
        if(checkPermission){
            //权限校验
            PermissionCheckDto permissionCheckDto = new PermissionCheckDto();
            permissionCheckDto.setSpaceBid(apmSpaceApp.getSpaceBid());
            permissionCheckDto.setSpaceAppBid(spaceAppBid);
            permissionCheckDto.setInstanceBid(bid);
            permissionCheckDto.setOperatorCode(OperatorEnum.EDIT.getCode());
            boolean hasPermission = permissionCheckService.checkInstancePermission(permissionCheckDto);
            if(!hasPermission){
                throw new BusinessException("没有详情权限");
            }
        }
        MVersionObject mVersionObject = versionModelDomainService.getByBid(modelCode, bid);
        return MSpaceAppData.buildFrom(mVersionObject, spaceAppBid);
    }

    @Override
    public MSpaceAppData getUpdatePropertyStatus(String spaceAppBid, String bid, boolean checkPermission) {
        return null;
    }

    @Override
    public List<MSpaceAppData> listByModelMixQo(String spaceAppBid, ModelMixQo modelMixQo) {
        return null;
    }

    @Override
    public List<MSpaceAppData> batchGet(String spaceAppBid, List<String> bids) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public Map<String, List<MSpaceAppData>> batchSpaceAppMapGet(Map<String, List<String>> bids) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public List<MSpaceAppData> list(String spaceAppBid, QueryCondition queryCondition) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public PagedResult<MSpaceAppData> pageWithoutPermission(String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        pageQo.getParam().setQueries(buildSpaceAppBid(spaceAppBid, pageQo.getParam().getQueries()));
        PagedResult pagedResult = versionModelDomainService.page(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid), pageQo, filterRichText);
        List<MObject> list = pagedResult.getData();
        pagedResult.setData(
                list.stream().map(mObject -> {
                    // 转换类型
                    MSpaceAppData mSpaceAppData = new MSpaceAppData();
                    mSpaceAppData.putAll(mObject);http://localhost:8082/apm/space/1194358961908559872/app/1198929729855250432/data-driven/update/1200080018241581056/property
                    return mSpaceAppData;
                }).collect(Collectors.toList())
        );
        return pagedResult;
    }

    @Override
    public PagedResult<MSpaceAppData> page(String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        pageQo.getParam().setQueries(buildSpaceAppBid(spaceAppBid, pageQo.getParam().getQueries()));
        PagedResult pagedResult = versionModelDomainService.page(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid), pageQo, filterRichText);
        List<MObject> list = pagedResult.getData();
        pagedResult.setData(
                list.stream().map(mObject -> {
                    // 转换类型
                    MSpaceAppData mSpaceAppData = new MSpaceAppData();
                    mSpaceAppData.putAll(mObject);http://localhost:8082/apm/space/1194358961908559872/app/1198929729855250432/data-driven/update/1200080018241581056/property
                    return mSpaceAppData;
                }).collect(Collectors.toList())
        );
        return pagedResult;
    }

    @Override
    public Boolean updatePartialContent(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData) {
        // 兼容有的方法调用 bid 传入的是 dataBid
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        MObject object = objectModelCrudI.getByBid(app.getModelCode(), bid);
        if (Objects.isNull(object) || "history".equals(object.get("dataSource"))) {
            MObject objectByDataBid = objectModelDomainService.getOneByProperty(app.getModelCode(), VersionObjectEnum.DATA_BID.getCode(), bid);
            bid = objectByDataBid.getBid();
        }
        return apmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid, bid, mSpaceAppData);
    }

    @Override
    public Boolean updatePartialContent(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData, Boolean isRecordLog) {
        return null;
    }

    @Override
    public GroupListResult<MSpaceAppData> groupList(String spaceAppBid, String groupProperty, QueryCondition queryCondition) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public List<MSpaceAppData> sampleTree(String spaceAppBid, List<QueryWrapper> wrappers) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public List<MSpaceAppData> signObjectAndMultiSpaceTree(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean filterRichText) {
        return apmSpaceAppDataDrivenService.signObjectAndMultiSpaceTree(spaceBid, spaceAppBid, modelMixQo, filterRichText);
    }

    @Override
    public List<MObjectTree> signObjectAndMultiSpaceTreeList(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean filterRichText) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public Boolean moveTreeNode(String spaceAppBid, List<MoveTreeNodeParam> moveTreeNodeParams) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public Boolean moveTreeNodeByModelCode(String spaceBid, String modelCode, List<MoveTreeNodeParam> moveTreeNodeParams) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public List<MObjectTree> multiTree(String spaceBid, String spaceAppBid, ApmMultiTreeModelMixQo modelMixQo, boolean filterRichText, Boolean checkPermission) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public GroupListResult<MSpaceAppData> multiTreeGroupBy(String spaceBid, String spaceAppBid, String groupProperty, List<QueryWrapper> wrappers, List<Order> orders) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }
    @Override
    public boolean copyMultiTree(MObjectTree mObjectTree, String copyInstanceBid, String spaceBid, String spaceAppBid) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public Boolean batchLogicalDeleteByBids(String spaceAppBid, List<String> bids) {
        return versionModelDomainService.batchLogicalDeleteByBid(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid), bids);
    }

    @Override
    public Boolean batchLogicalDeleteByBids(String spaceAppBid, List<String> bids, Boolean checkPermission) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public Boolean batchLogicalDelete(String spaceAppBid, BatchLogicDelAndRemQo qo) {
        return versionModelDomainService.batchLogicalDeleteByBid(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid), qo.getInsBids());
    }

    @Override
    public Boolean deleteRel(String spaceAppBid,DeleteRelDto dto) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public Boolean addBatch(String spaceBid, String spaceAppBid, List<MSpaceAppData> targetMObjects, ApmSpaceAppDataDrivenOperationFilterBo filterBo) {
        targetMObjects.forEach(targetMObject -> {
            add(spaceAppBid, targetMObject);
        });
        return true;
    }

    @Override
    public Boolean moveGroupNode(String spaceAppBid, String groupProperty, List<MoveGroupNodeParam> moveGroupNodeParams) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public Boolean updatePartialContentAndCompleteFlowNode(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData) {
        // 复制提交的内容
        MSpaceAppData mSpaceAppDataCopy = new MSpaceAppData();
        mSpaceAppDataCopy.putAll(mSpaceAppData);
        // 更新属性
        if (CollectionUtils.isNotEmpty(mSpaceAppData)) {
            updatePartialContent(spaceAppBid, bid, mSpaceAppData);
        }
        // 完成流程节点
        Boolean result = runtimeService.completeNode(nodeBid,null);
        // 记录历程
        runtimeService.saveFlowProcess(nodeBid, runtimeService.getVersionInstanceBid(spaceAppBid, bid), "complete", mSpaceAppDataCopy);
        // 完成流程节点
        return result;
    }

    @Override
    public Boolean updatePartialContentAndRollbackFlowNode(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData,boolean runEvent) {
        // 复制提交的内容
        MSpaceAppData mSpaceAppDataCopy = new MSpaceAppData();
        mSpaceAppDataCopy.putAll(mSpaceAppData);
        // 更新属性
        if (CollectionUtils.isNotEmpty(mSpaceAppData)) {
            updatePartialContent(spaceAppBid, bid, mSpaceAppData);
        }
        Boolean result = runtimeService.rollback(nodeBid,runEvent);
        // 记录历程
        runtimeService.saveFlowProcess(nodeBid, runtimeService.getVersionInstanceBid(spaceAppBid, bid), "rollback", mSpaceAppDataCopy);
        return result;
    }

    @Override
    public MObject executeAction(String spaceAppBid, String instanceBid, String actionBid, MSpaceAppData mSpaceAppData) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public List<ResourceVo> getProjectResources(ResourceQo resourceQo) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public List<ResourceVo> getSpaceResources(String spaceBid, ResourceQo resourceQo) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public ApmLaneGroupData listLane(String spaceBid, String spaceAppBid, ApmLaneQO apmLaneQO) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public List<StateDataDriveDto> updateDriveState(StateDataDriveAO stateDataDriveAO, ApmFlowDriveRelate apmFlowDriveRelate) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public List<MSpaceAppData> importExcel(String spaceAppBid, MultipartFile file,String spaceBid) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public boolean importExcelAndSave(String spaceAppBid, MultipartFile file, String spaceBid) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public MSpaceAppData checkOut(String spaceAppBid, String bid) {
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        MVersionObject mVersionObject = versionModelDomainService.checkOut(modelCode, bid);
        return MSpaceAppData.buildFrom(mVersionObject, spaceAppBid);
    }

    @Override
    public Map<String, MSpaceAppData> batchCheckOut(String spaceAppBid, List<String> bids) {
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        if (CollectionUtils.isEmpty(bids)) {
            throw new TranscendBizException("检出列表为空");
        }
        Map<String, MSpaceAppData> result = Maps.newHashMap();
        bids.forEach(bid -> {
            MVersionObject mVersionObject = versionModelDomainService.checkOut(modelCode, bid);
            result.put(bid, MSpaceAppData.buildFrom(mVersionObject, spaceAppBid));
        });
        return result;
    }

    @Override
    public MSpaceAppData cancelCheckOut(String spaceAppBid, String bid) {
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        MVersionObject mVersionObject = versionModelDomainService.cancelCheckOut(modelCode, bid);
        return MSpaceAppData.buildFrom(mVersionObject, spaceAppBid);
    }

    @Override
    public Map<String, MSpaceAppData> batchCancelCheckOut(String spaceAppBid, List<String> bids) {
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        if (CollectionUtils.isEmpty(bids)) {
            throw new TranscendBizException("撤销检出列表为空");
        }
        Map<String, MSpaceAppData> result = Maps.newHashMap();
        bids.forEach(bid -> {
            MVersionObject mVersionObject = versionModelDomainService.cancelCheckOut(modelCode, bid);
            result.put(bid, MSpaceAppData.buildFrom(mVersionObject, spaceAppBid));
        });
        return result;
    }

    @Override
    public MSpaceAppData checkIn(String spaceAppBid, MVersionObject mVersionObject) {
        // 获取空间应用数据
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        String modelCode = app.getModelCode();
        mVersionObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), spaceAppBid);
        mVersionObject.put(SpaceAppDataEnum.SPACE_BID.getCode(), app.getSpaceBid());
        MVersionObject result = versionModelDomainService.checkIn(modelCode, mVersionObject);
        return MSpaceAppData.buildFrom(result, spaceAppBid);
    }

    @Override
    public List<MSpaceAppData> batchCheckin(String spaceAppBid, List<MVersionObject> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new TranscendBizException("检入列表为空");
        }
        List<MSpaceAppData> result = Lists.newArrayList();
        list.forEach(mVersionObject -> result.add(iBaseApmSpaceAppDataDrivenService.checkIn(spaceAppBid, mVersionObject)));
        return result;
    }

    @Override
    public String batchCheckinAsync(String spaceAppBid, List<MVersionObject> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new TranscendBizException("检入列表为空");
        }
        String resultId = SnowflakeIdWorker.nextIdStr();
        iRedisService.set(CacheNameConstant.BATCH_CHECKIN_ASYNC_TOTAL + resultId, list.size());
        iRedisService.set(CacheNameConstant.BATCH_CHECKIN_ASYNC_COMPLETE + resultId, 0);
        CompletableFuture.runAsync(() -> {
            String jobNumber = SsoHelper.getJobNumber();
            String employeeName = SsoHelper.getName();
            ExecutorService instance = IOThreadPool.getInstance();
            CompletionService<MSpaceAppData> completionService = new ExecutorCompletionService<>(instance);
            list.forEach(versionObject -> {
                completionService.submit(() -> {
                    try {
                        UserLoginDto userLoginDto = new UserLoginDto();
                        userLoginDto.setEmployeeNo(jobNumber);
                        userLoginDto.setRealName(employeeName);
                        IUserContext<IUser> userContextDto = new UserContextDto<>(null, userLoginDto);
                        UserContextHolder.setUser(userContextDto);
                        iBaseApmSpaceAppDataDrivenService.checkIn(spaceAppBid, versionObject);
                        UserContextHolder.removeUser();
                    } catch (Exception exp){
                        log.error("批量检入异步执行失败,失败数据：{}",versionObject.getBid(),exp);
                    }
                    return null;
                });
            });
            try {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    completionService.take();
                    redisTemplate.opsForValue().increment(CacheNameConstant.BATCH_CHECKIN_ASYNC_COMPLETE + resultId);
                }
            } catch (Exception exp) {
                log.info("批量检入异步执行失败", exp);
            }
        });
        return resultId;
    }

    @Override
    public Map<String, Integer> batchCheckinAsyncProgress(String spaceAppBid, String resultId) {
        HashMap<String, Integer> progressMap = Maps.newHashMap();
        progressMap.put("total", iRedisService.get(CacheNameConstant.BATCH_CHECKIN_ASYNC_TOTAL + resultId));
        progressMap.put("complete", iRedisService.get(CacheNameConstant.BATCH_CHECKIN_ASYNC_COMPLETE + resultId));
        return progressMap;
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
    public Boolean batchUpdatePartialContentByBids(String spaceBid, String spaceAppBid, List<String> bids, MSpaceAppData mSpaceAppData) {
        return null;
    }
    @Override
    public Boolean multiObjectPartialContent(String spaceAppBid, List<MultiObjectUpdateDto> multiObjectUpdateDtoList) {
        return null;
    }

    /**
     * 批量检查权限
     *
     * @param spaceBid
     * @param spaceAppBid
     * @param bids
     * @param button
     * @return
     */
    @Override
    public Boolean batchOperationCheckPermission(String spaceBid, String spaceAppBid, List<String> bids, String operation) {
        return null;
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
        return null;
    }

    @Override
    public Boolean saveDraft(String spaceAppBid, MVersionObject mVersionObject) {
        return versionModelDomainService.saveDraft(mVersionObject);
    }

    @Override
    public Boolean batchSaveDraft(String spaceAppBid, List<MVersionObject> draftList) {
        if (CollectionUtils.isEmpty(draftList)) {
            return true;
        }
        draftList.forEach(e -> versionModelDomainService.saveDraft(e));
        return true;
    }

    @Override
    public List<MSpaceAppData> listByHistory(String spaceAppBid, String dataBid, QueryCondition queryCondition) {
        List<QueryWrapper> queryWrappers = buildDataBid(dataBid, buildSpaceAppBid(spaceAppBid, queryCondition.getQueries()));
        queryCondition.setQueries(queryWrappers);
        List<MVersionObject> mVersionObjects = versionModelDomainService.listHis(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid), queryCondition);
        return mVersionObjects.stream().map(mObject -> {
            // 转换类型
            return MSpaceAppData.buildFrom(mObject, spaceAppBid);
        }).collect(Collectors.toList());
    }

    @Override
    public PagedResult<MSpaceAppData> pageListByHistory(String spaceAppBid, String dataBid, BaseRequest<QueryCondition> queryCondition, boolean filterRichText) {
        List<QueryWrapper> queryWrappers = buildDataBid(dataBid, buildSpaceAppBid(spaceAppBid, queryCondition.getParam().getQueries()));
        queryCondition.getParam().setQueries(queryWrappers);
        PagedResult<MVersionObject> mVersionObjects = versionModelDomainService.hisPage(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid), queryCondition, filterRichText);
        PagedResult<MSpaceAppData> pagedResultDeserialize = new PagedResultDeserialize<>();
        pagedResultDeserialize.setData(mVersionObjects.getData().stream().map(mObject -> {
            // 转换类型
            return MSpaceAppData.buildFrom(mObject, spaceAppBid);
        }).collect(Collectors.toList()));
        pagedResultDeserialize.setCurrent(mVersionObjects.getCurrent());
        pagedResultDeserialize.setSize(mVersionObjects.getSize());
        pagedResultDeserialize.setTotal(mVersionObjects.getTotal());
        pagedResultDeserialize.setPages(mVersionObjects.getPages());
        return pagedResultDeserialize;
    }

    @Override
    public List<Object> listPropertyDistinct(String spaceAppBid, String distinctProperty, QueryCondition queryCondition) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public MSpaceAppData revise(String spaceAppBid, String bid) {
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        ApmLifeCycleStateVO lifeCycleState = apmSpaceAppConfigManageService.getLifeCycleState(spaceAppBid);
        MVersionObject mVersionObject = versionModelDomainService.revise(ReviseDto.builder().
                onlyChaneVersionAndCopyRelation("stateFlow".equals(lifeCycleState.getLifeCycleStateType()))
                        .initLifeCode(lifeCycleState.getInitState())
                .modelCode(modelCode).bid(bid).build());
        return MSpaceAppData.buildFrom(mVersionObject, spaceAppBid);
    }

    @Override
    public MSpaceAppData promote(String spaceBid, String spaceAppBid, LifeCyclePromoteDto promoteDto) {
        promoteDto.setModelCode(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid));
        promoteDto.setIsOnlyChangeLifeCode("stateFlow".equals(apmSpaceAppConfigManageService.getLifeCycleState(spaceAppBid).getLifeCycleStateType()));
        return appDataService.promoteVersionObject(spaceBid,spaceAppBid,promoteDto);
    }

    @Override
    public ApmLaneGroupData listAllLane(String spaceBid, String spaceAppBid, ApmLaneAllQo laneAllQo) {
        return null;
    }

    @Override
    public Boolean batchRemove(String spaceAppBid, BatchLogicDelAndRemQo qo) {
        return null;
    }

    @Override
    public Boolean batchRemoveRelation(String spaceAppBid, BatchRemoveRelationQo qo) {
        return null;
    }

    @Override
    public List<Map<String, Object>> userCache(String spaceAppBid, String modelCode, List<String> userIds) {
        if (StringUtils.isBlank(modelCode)) {
            throw new PlmBizException("应用对象编码不能为空");
        }
        // key
        String redisKey = modelCode + ":" + SsoHelper.getJobNumber() + ":cache";
        // 如果列表为空，仅查询
        if (CollectionUtils.isNotEmpty(userIds)) {
            userIds.forEach(userId -> {
                // 从左放数据，先移除元素，因为可能原来有
                redisTemplate.opsForList().remove(redisKey, 0, userId);
                redisTemplate.opsForList().leftPush(redisKey, userId);
                // 修剪，保留10个
                redisTemplate.opsForList().trim(redisKey, 0, 9);
            });
        }
        // 获取所有
        List<String> list = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        // 查询用户信息
        Map<String, EmployeeInfo> employeeInfos = openUserService.batchFindByEmoNo(list).stream().collect(Collectors.toMap(EmployeeInfo::getJobNumber, Function.identity(), (k1, k2) -> k1));
        List<Map<String, Object>> result = Lists.newArrayList();
        list.forEach(e -> {
            EmployeeInfo employeeInfo = employeeInfos.get(e);
            if (ObjectUtil.isNotEmpty(employeeInfo)) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("label", employeeInfo.getName());
                map.put("value", employeeInfo.getJobNumber());
                map.put("dep", employeeInfo.getDeptName());
                result.add(map);
            }
        });
        return result;
    }

    /**
     * 批量新增数据
     * @param spaceBid
     * @param spaceAppBid
     * @param mSpaceAppDataList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MSpaceAppData> batchAdd(String spaceBid, String spaceAppBid, List<MSpaceAppData> mSpaceAppDataList) {
        List<MSpaceAppData> result = Lists.newArrayList();
        Assert.notEmpty(mSpaceAppDataList, "数据不能为空");
        String jobNumber = SsoHelper.getJobNumber();
        String employeeName = SsoHelper.getName();
        CountDownLatch countDownLatch = new CountDownLatch(mSpaceAppDataList.size());
        mSpaceAppDataList.forEach(mSpaceAppData -> {
            //多线程处理
            SimpleThreadPool.getInstance().execute(() -> {
                if (Objects.nonNull(mSpaceAppData)) {
                    try {
                        mSpaceAppData.remove(BaseDataEnum.ID.getCode());
                        UserLoginDto userLoginDto = new UserLoginDto();
                        userLoginDto.setEmployeeNo(jobNumber);
                        userLoginDto.setRealName(employeeName);
                        IUserContext<IUser> userContextDto = new UserContextDto<>(null, userLoginDto);
                        UserContextHolder.setUser(userContextDto);
                        mSpaceAppData.setCreatedBy(jobNumber);
                        mSpaceAppData.setUpdatedBy(jobNumber);
                        result.add(add(spaceAppBid,mSpaceAppData));
                        UserContextHolder.removeUser();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        });
        //等待所有线程执行完毕
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 保存草稿数据
     * @param spaceAppBid
     * @param mSpaceAppData
     * @return
     */
    @Override
    public MSpaceAppData saveCommonDraft(String spaceAppBid, MSpaceAppData mSpaceAppData) {
        throw new PlmBizException("版本对象暂不支持该操作");
    }

    @Override
    public MSpaceAppData addTonesData(String spaceAppBid, MSpaceAppData mSpaceAppData) {
        return null;
    }

    @Override
    public String batchAddForCad(String spaceBid, String spaceAppBid, List<MSpaceAppData> mSpaceAppDataList) {
        Assert.notEmpty(mSpaceAppDataList, "数据不能为空");
        String requestId = SnowflakeIdWorker.nextIdStr();
        String totalKey = CacheNameConstant.CAD_TOTAL + requestId;
        String completeKey = CacheNameConstant.CAD_COMPLETE + requestId;
        iRedisService.set(totalKey, mSpaceAppDataList.size());
        iRedisService.set(completeKey, 0);

        CompletableFuture.runAsync(() -> {
            String jobNumber = SsoHelper.getJobNumber();
            String employeeName = SsoHelper.getName();
            ExecutorService instance = SimpleThreadPool.getInstance();
            CompletionService<MSpaceAppData> completionService = new ExecutorCompletionService<>(instance);
            mSpaceAppDataList.forEach(mSpaceAppData -> {
                completionService.submit(() -> {
                    MSpaceAppData res;
                    try {
                        mSpaceAppData.remove(BaseDataEnum.ID.getCode());
                        UserLoginDto userLoginDto = new UserLoginDto();
                        userLoginDto.setEmployeeNo(jobNumber);
                        userLoginDto.setRealName(employeeName);
                        IUserContext<IUser> userContextDto = new UserContextDto<>(null, userLoginDto);
                        UserContextHolder.setUser(userContextDto);
                        mSpaceAppData.setCreatedBy(jobNumber);
                        mSpaceAppData.setUpdatedBy(jobNumber);
                        res = add(spaceAppBid,mSpaceAppData);
                        UserContextHolder.removeUser();
                    } catch (Exception exp){
                        res = mSpaceAppData;
                    }
                    return res;
                });
            });
            try {
                int size = mSpaceAppDataList.size();
                List<MSpaceAppData> result = Lists.newArrayList();
                for (int i = 0; i < size; i++) {
                    Future<MSpaceAppData> future = completionService.take();
                    MSpaceAppData s = future.get();
                    result.add(s);
                    redisTemplate.opsForValue().increment(completeKey);
                }
                List<CadBatchAddInstanceRecord> dtos = Lists.newArrayList();
                result.forEach(e -> {
                    CadBatchAddInstanceRecord dto = new CadBatchAddInstanceRecord();
                    dto.setBid(SnowflakeIdWorker.nextIdStr());
                    dto.setSpaceBid(spaceBid);
                    dto.setSpaceAppBid(spaceAppBid);
                    dto.setRequestId(requestId);
                    dto.setInstanceData(JSON.toJSONString(e));
                    dto.setCreatedBy(jobNumber);
                    dto.setUpdatedBy(jobNumber);
                    dtos.add(dto);
                });
                cadBatchAddInstanceRecordService.saveBatchRecord(dtos);
            } catch (Exception exp) {
                log.info("cad 接口批量新增实例失败");
                exp.printStackTrace();
            }
        });
        return requestId;
    }

    @Override
    public List<MSpaceAppData> batchAddForCadResult(String spaceBid, String spaceAppBid, String resultId) {
        Integer total = iRedisService.get(CacheNameConstant.CAD_TOTAL + resultId);
        Integer complete = iRedisService.get(CacheNameConstant.CAD_COMPLETE + resultId);
        if (total.equals(complete)) {
            return cadBatchAddInstanceRecordService.getByBid(resultId).stream().map(e -> JSON.parseObject(e.getInstanceData(), MSpaceAppData.class)).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * 构建空间应用bid 查询条件
     * @param spaceAppBid spaceAppBid
     * @param wrappers wrappers
     * @return
     * @version: 1.0
     * @date: 2023/12/19 14:07
     * @author: bin.yin
     */
    private List<QueryWrapper> buildSpaceAppBid(String spaceAppBid, List<QueryWrapper> wrappers) {
        if (CollectionUtils.isEmpty(wrappers)) {
            wrappers = Lists.newArrayList();
        }
        QueryWrapper andWrapper = new QueryWrapper(Boolean.TRUE);
        andWrapper.setSqlRelation(" and ");
        QueryWrapper spaceAppWrapper = new QueryWrapper(Boolean.FALSE);
        spaceAppWrapper.setProperty(SpaceAppDataEnum.SPACE_APP_BID.getCode());
        spaceAppWrapper.setCondition("=");
        spaceAppWrapper.setValue(spaceAppBid);
        wrappers.add(andWrapper);
        wrappers.add(spaceAppWrapper);
        return wrappers;
    }

    private List<QueryWrapper> buildDataBid(String dataBid, List<QueryWrapper> wrappers) {
        if (CollectionUtils.isEmpty(wrappers)) {
            wrappers = Lists.newArrayList();
        }
        QueryWrapper andWrapper = new QueryWrapper(Boolean.TRUE);
        andWrapper.setSqlRelation(" and ");
        QueryWrapper spaceAppWrapper = new QueryWrapper(Boolean.FALSE);
        spaceAppWrapper.setProperty(RelationEnum.DATA_BID.getCode());
        spaceAppWrapper.setCondition("=");
        spaceAppWrapper.setValue(dataBid);
        wrappers.add(andWrapper);
        wrappers.add(spaceAppWrapper);
        return wrappers;
    }
}
