package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.dto.RelationInfo;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.MoveTreeNodeParam;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.relation.delete.StructureRelDel;
import com.transcend.plm.datadriven.api.model.relation.qo.QueryPathQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationParentQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationTreeQo;
import com.transcend.plm.datadriven.api.model.relation.vo.QueryPathVo;
import com.transcend.plm.datadriven.apm.aspect.notify.OperateBusiService;
import com.transcend.plm.datadriven.apm.constants.SpaceConstant;
import com.transcend.plm.datadriven.apm.constants.TreeTypeConstant;
import com.transcend.plm.datadriven.apm.dto.NotifyRelationBatchAddBusDto;
import com.transcend.plm.datadriven.apm.dto.NotifyRelationBatchRemoveBusDto;
import com.transcend.plm.datadriven.apm.event.config.NotifyCrossRelationConfig;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.log.model.dto.GenericLogAddParam;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MObjectCopyAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionCheckService;
import com.transcend.plm.datadriven.apm.space.enums.PermissionCheckEnum;
import com.transcend.plm.datadriven.apm.space.model.*;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmMultiTreeDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLifeCycleStateVO;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpace;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceService;
import com.transcend.plm.datadriven.apm.space.service.*;
import com.transcend.plm.datadriven.apm.space.service.context.ApmSpaceAppRelationDataDrivenStrategyContext;
import com.transcend.plm.datadriven.apm.space.utils.RelationUtils;
import com.transcend.plm.datadriven.apm.strategy.AddOuterStrategy;
import com.transcend.plm.datadriven.apm.tools.StayDurationHandler;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyCrossRelationEventBusDto;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyReversionRelTaskBusDto;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.tool.ObjectTreeTools;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.EsUtil;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.RelationModelDomainService;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.REVISION_DEMAND_REL_MODEL_CODE;

/**
 * @author unknown
 */
@Service(ApmSpaceAppRelationDataDrivenStrategyContext.NORMAL + ApmSpaceAppRelationDataDrivenStrategyContext.STRATEGY_NAME)
@Slf4j
public class ApmSpaceAppRelationDataDrivenServiceImpl implements IApmSpaceAppRelationDataDrivenService {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;

    @Resource
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;
    @Resource
    private CfgObjectFeignClient cfgObjectClient;

    @Resource
    private OperateBusiService operateBusiService;
    @Resource
    private RelationModelDomainService relationModelDomainService;
    @Resource
    private IApmSpaceAppConfigManageService iApmSpaceAppConfigManageService;
    @Resource
    private NotifyCrossRelationConfig notifyCrossRelationConfig;

    @Resource
    private AddOuterStrategy addOuterStrategy;

    @Resource
    private IAppExcelTemplateService appExcelTemplateService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private ApmSpaceService apmSpaceService;

    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationClient;

    @Resource
    private IApmSpaceAppConfigDrivenService iApmSpaceAppConfigDrivenService;

    @Resource
    private OperationLogEsService operationLogEsService;


    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;
    @Resource
    private IAppDataService appDataService;
    @Resource
    private IPermissionCheckService permissionCheckService;

    @Resource
    private DictionaryApplicationService dictionaryApplicationService;

    private final Map<String, Function<AddExpandAo, Object>> strategyMap;

    {
        strategyMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        strategyMap.put("outer", this::addOuter);
        strategyMap.put("inner", this::addInner);
    }

    @Transactional(rollbackFor = Exception.class)
    public Object addOuter(AddExpandAo addExpandAo) {
        Object result = addOuterStrategy.execute(addExpandAo);
        return result;
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean addInner(AddExpandAo addExpandAo) {
        // 添加方法内部调用还是走以前的逻辑
        Boolean selected = addExpandAo.getSelected();
        SpaceAppRelationAddParam param =  SpaceAppRelationAddParam.builder()
                .relationModelCode(addExpandAo.getRelationModelCode())
                .sourceBid(addExpandAo.getSourceBid())
                .sourceDataBid(addExpandAo.getSourceDataBid())
                .spaceAppBid(addExpandAo.getSpaceAppBid())
                .spaceBid(addExpandAo.getSpaceBid())
                .viewModelCode(addExpandAo.getViewModelCode())
                //浅克隆，避免后置事件拿不到方法入参
                .targetMObjects(new ArrayList<>(addExpandAo.getTargetMObjects()))
                .targetModelCode(addExpandAo.getTargetModelCode())
                .sourceSpaceAppBid(addExpandAo.getSourceSpaceAppBid())
                .treeType(addExpandAo.getTreeType())
                .selected(selected)
                .build();
        // 选择需要进行权限校验
        // 1.选择的前提
        if (Boolean.TRUE.equals(selected)) {
            checkRelationSelectPermission(param);
        }

        return add(param);
    }

    /**
     * 检查关系选择权限校验
     * @param param 参数对象
     */
    private void checkRelationSelectPermission(SpaceAppRelationAddParam param) {
        List<MSpaceAppData> targetMObjects = param.getTargetMObjects();

        // 1. 按 spaceAppBid 分组，分别进行权限校验
        Map<String, List<MSpaceAppData>> groupSpaceAppDataMap =
                targetMObjects.stream()
                        .collect(Collectors.groupingBy(MSpaceAppData::getSpaceAppBid));

        // 用于收集所有分组的异常信息
        List<String> allErrorMessages = new ArrayList<>();

        groupSpaceAppDataMap.forEach((spaceAppBid, spaceAppDataList) -> {
            try {
                // 提取当前分组的所有 bid
                List<String> bids = spaceAppDataList.stream()
                        .map(MSpaceAppData::getBid)
                        .collect(Collectors.toList());

                // 构造查询条件
                BaseRequest<QueryCondition> pageQo = buildQueryCondition(bids);

                // 调用服务接口获取用户有权限的数据
                PagedResult<MSpaceAppData> page = apmSpaceAppDataDrivenService.page(spaceAppBid, pageQo, true);
                List<MSpaceAppData> authorizedData = page.getData();

                // 2. 比较权限数据与输入数据，找出无权限的数据
                List<String> unauthorizedBids = findUnauthorizedBids(bids, authorizedData);

                if (!unauthorizedBids.isEmpty()) {
                    // 3. 构造详细的提示信息
                    String unauthorizedNames = spaceAppDataList.stream()
                            .filter(data -> unauthorizedBids.contains(data.getBid()))
                            .map(MSpaceAppData::getName)
                            .collect(Collectors.joining(", "));

                    String spaceBid = spaceAppDataList.get(0).getSpaceBid();

                    ApmSpace apmSpace = apmSpaceService.getOne(Wrappers.<ApmSpace>lambdaQuery().eq(ApmSpace::getBid, spaceBid));
                    // 收集异常信息
                    allErrorMessages.add(
                            String.format("您当前账号在项目 [%s] 中，无权限访问以下数据：%s", apmSpace.getName(), unauthorizedNames)
                    );
                }
            } catch (Exception e) {
                // 捕获分组校验中的异常，避免中断后续校验
                allErrorMessages.add(
                        String.format("空间应用 [%s] 校验失败：%s", spaceAppBid, e.getMessage())
                );
            }
        });

        // 4. 如果存在异常信息，统一抛出异常
        if (!allErrorMessages.isEmpty()) {
            String errorMessage = String.join("; ", allErrorMessages);
            throw new TranscendBizException("权限校验失败：" + errorMessage);
        }
    }

    /**
     * 构造查询条件
     * @param bids 待查询的 bid 列表
     * @return 查询条件对象
     */
    private BaseRequest<QueryCondition> buildQueryCondition(List<String> bids) {
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        QueryCondition queryCondition = new QueryCondition();
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), bids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        queryCondition.setQueries(queryWrappers);
        pageQo.setParam(queryCondition);
        pageQo.setCurrent(1);
        pageQo.setSize(1000); // 支持最多 1000 条数据
        return pageQo;
    }

    /**
     * 找出无权限的 bid
     * @param inputBids 输入的 bid 列表
     * @param authorizedData 用户有权限的数据列表
     * @return 无权限的 bid 列表
     */
    private List<String> findUnauthorizedBids(List<String> inputBids, List<MSpaceAppData> authorizedData) {
        Set<String> authorizedBids = authorizedData.stream()
                .map(MSpaceAppData::getBid)
                .collect(Collectors.toSet());
        return inputBids.stream()
                .filter(bid -> !authorizedBids.contains(bid))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(String spaceBid, RelationDelAndRemParamAo relationDelAndRemParamAo) {
        ApmSpaceApp targetApp = apmSpaceAppService.getByBid(relationDelAndRemParamAo.getTargetSpaceAppBid());
        List<MObject> list = objectModelCrudI.listByBids(relationDelAndRemParamAo.getTargetBids(), targetApp.getModelCode());
        boolean hasPermission = permissionCheckService.checkInstancesPermission(list,targetApp.getSpaceBid(), OperatorEnum.DELETE.getCode());
        if(!hasPermission){
            throw new BusinessException("没有删除权限");
        }
        // 关系ModelCode查询关系对象
        CfgObjectRelationVo relationVo =
                cfgObjectRelationClient.getRelation(relationDelAndRemParamAo.getModelCode()).getCheckExceptionData();
        // 使用代理对象，保障调用的本类方法能够被事务拦截
        IApmSpaceAppRelationDataDrivenService proxy = (IApmSpaceAppRelationDataDrivenService) AopContext.currentProxy();
        if (judgeTargetIsVersionObject(relationDelAndRemParamAo.getTargetSpaceAppBid())) {
            // 如果为版本对象，则不支持删除实例，只支持删除关系，与移除逻辑一致
            return proxy.batchRemove(spaceBid, relationDelAndRemParamAo);
        }
        // 如果不是版本对象，则支持删除实例和关系
        List<String> targetBids = relationDelAndRemParamAo.getTargetBids();
        // 删除目标实例
        Boolean delInsRes = objectModelCrudI.batchDeleteByBids(relationVo.getTargetModelCode(), targetBids);
        // 删除关系
        boolean result = Boolean.TRUE.equals(delInsRes) && Boolean.TRUE.equals(proxy.batchRemove(spaceBid, relationDelAndRemParamAo));
        // 记录移除的操作日志
        String instanceNames = list.stream().map(MObject::getName).collect(Collectors.joining(","));
        try {
            ApmSpaceApp sourceApp = apmSpaceAppService.getByBid(relationDelAndRemParamAo.getSourceSpaceAppBid());
            GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                    .logMsg("删除了[" + instanceNames + "]的实例数据")
                    .modelCode(sourceApp.getModelCode())
                    .instanceBid(relationDelAndRemParamAo.getSourceBid())
                    .type(EsUtil.EsType.LOG.getType())
                    .spaceBid(spaceBid)
                    .build();
            operationLogEsService.genericSave(genericLogAddParam);

            // 20250527保存记录空间应用的删除日志
            GenericLogAddParam spaceAppGenericLogAddParam = GenericLogAddParam.builder()
                    .logMsg("删除了[" + instanceNames + "]的实例数据")
                    .modelCode(sourceApp.getModelCode())
                    .instanceBid(relationDelAndRemParamAo.getTargetSpaceAppBid())
                    .type(EsUtil.EsType.LOG.getType())
                    .spaceBid(spaceBid)
                    .build();
            operationLogEsService.genericSave(spaceAppGenericLogAddParam);
        } catch (Exception e) {
            log.error("记录删除了[" + instanceNames + "]的实例数据日志失败", e);
        }
        //发布事件
        NotifyEventBus.EVENT_BUS.post(NotifyRelationBatchRemoveBusDto.of(relationDelAndRemParamAo.getModelCode(),relationVo,relationDelAndRemParamAo));
        return result;
    }

    @Override
    public Boolean batchRemove(String spaceBid, RelationDelAndRemParamAo relationDelAndRemParamAo) {
        ApmSpaceApp targetApp = apmSpaceAppService.getByBid(relationDelAndRemParamAo.getTargetSpaceAppBid());
        List<MObject> list = objectModelCrudI.listByBids(relationDelAndRemParamAo.getTargetBids(), targetApp.getModelCode());
        // 删除关系实例表数据
        Boolean deleteRel = objectModelCrudI.batchLogicalDeleteByBids(relationDelAndRemParamAo.getModelCode(), relationDelAndRemParamAo.getRelationBids());
        // 存在目标对象的属性值从源对象中获取，需要把源对象从目标对象属性中移除
        List<CfgViewMetaDto> cfgViewMetaDtos = iApmSpaceAppConfigDrivenService.queryRelationMetaList(relationDelAndRemParamAo.getTargetSpaceAppBid(), new ArrayList<>())
                .stream().filter(dto -> relationDelAndRemParamAo.getModelCode().equals(dto.getRelationModelCode())).collect(Collectors.toList());
        CfgObjectRelationVo relationVo =
                cfgObjectRelationClient.getRelation(relationDelAndRemParamAo.getModelCode()).getCheckExceptionData();
        if (CollectionUtils.isNotEmpty(cfgViewMetaDtos)) {
            //查询要删除的目标对象实例
            List<MObject> targetMObjects = objectModelCrudI.listByBids(relationDelAndRemParamAo.getTargetBids(), relationVo.getTargetModelCode());
            if (CollectionUtils.isEmpty(targetMObjects)) {
                return Boolean.TRUE.equals(deleteRel);
            }
            int totalTasks = cfgViewMetaDtos.size() * targetMObjects.size();
            CountDownLatch countDownLatch = new CountDownLatch(totalTasks);
            for (CfgViewMetaDto dto : cfgViewMetaDtos) {
                for (MObject obj : targetMObjects) {
                    String bid = obj.getBid();
                    MSpaceAppData mSpaceAppData = new MSpaceAppData();
                    // 多选属性
                    if (Boolean.TRUE.equals(dto.getMultiple())) {
                        List<Object> objectList = new ArrayList<>((Collection<?>) obj.get(dto.getName()));
                        objectList.remove(relationDelAndRemParamAo.getSourceBid());
                        mSpaceAppData.put(dto.getName(), objectList);
                    } else {
                        mSpaceAppData.put(dto.getName(), null);
                    }
                    List<String> bids = new ArrayList<>();
                    bids.add(bid);

                    //多线程处理
                    SimpleThreadPool.getInstance().execute(() -> {
                        try {
                            //更新字段 
                            objectModelCrudI.batchUpdatePartialContentByIds(targetApp.getModelCode(), mSpaceAppData, bids);
                            //apmSpaceAppDataDrivenService.updatePartialContent(relationDelAndRemParamAo.getTargetSpaceAppBid(), bid, mSpaceAppData);                        } finally {
                            countDownLatch.countDown();
                        }catch (Exception e){

                        }
                    });
                }
            }
            //等待所有线程执行完毕
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //发布事件
        }
        NotifyEventBus.EVENT_BUS.post(NotifyRelationBatchRemoveBusDto.of(relationDelAndRemParamAo.getModelCode(),relationVo,relationDelAndRemParamAo));

        // 记录移除的操作日志
        String instanceNames = list.stream().map(MObject::getName).collect(Collectors.joining(","));
        try {
            ApmSpaceApp sourceApp = apmSpaceAppService.getByBid(relationDelAndRemParamAo.getSourceSpaceAppBid());
            GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                    .logMsg("关系中移除了[" + instanceNames + "]的实例数据")
                    .modelCode(sourceApp.getModelCode())
                    .instanceBid(relationDelAndRemParamAo.getSourceBid())
                    .type(EsUtil.EsType.LOG.getType())
                    .spaceBid(spaceBid)
                    .build();
            operationLogEsService.genericSave(genericLogAddParam);
        } catch (Exception e) {
            log.error("日志记录，关系中移除了[" + instanceNames + "]的实例数据失败", e);
        }
        return Boolean.TRUE.equals(deleteRel);
    }

    @Override
    public Boolean removeAllRelation(SpaceAppRelationAddParam spaceAppRelationAddParam) {
        List<String> bidList = spaceAppRelationAddParam.getTargetMObjects().stream().map(MSpaceAppData::getBid).collect(Collectors.toList());
        //移除关联的实例
        RelationMObject relationMObject = RelationMObject.builder().relationModelCode(spaceAppRelationAddParam.getRelationModelCode())
                .sourceBid(spaceAppRelationAddParam.getSourceBid()).targetBids(bidList).build();
        objectModelCrudI.logicalDeleteRelationsByTargetBids(relationMObject);
        // 存在目标对象的属性值从源对象中获取，需要把源对象从目标对象属性中移除
        List<CfgViewMetaDto> cfgViewMetaDtos = iApmSpaceAppConfigDrivenService.queryRelationMetaList(spaceAppRelationAddParam.getSpaceAppBid(), new ArrayList<>())
                .stream().filter(dto -> spaceAppRelationAddParam.getRelationModelCode().equals(dto.getRelationModelCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(cfgViewMetaDtos)) {
            int totalTasks = cfgViewMetaDtos.size() * spaceAppRelationAddParam.getTargetMObjects().size();
            CountDownLatch countDownLatch = new CountDownLatch(totalTasks);
            for (CfgViewMetaDto dto : cfgViewMetaDtos) {
                for (MSpaceAppData obj : spaceAppRelationAddParam.getTargetMObjects()) {
                    String bid = obj.getBid();
                    MSpaceAppData mSpaceAppData = new MSpaceAppData();
                    // 多选属性
                    if (Boolean.TRUE.equals(dto.getMultiple())) {
                        List<Object> objectList = new ArrayList<>((Collection<?>) obj.get(dto.getName()));
                        objectList.remove(spaceAppRelationAddParam.getSourceBid());
                        mSpaceAppData.put(dto.getName(), objectList);
                    } else {
                        mSpaceAppData.put(dto.getName(), null);
                    }
                    //多线程处理
                    SimpleThreadPool.getInstance().execute(() -> {
                        try {
                            apmSpaceAppDataDrivenService.updatePartialContent(spaceAppRelationAddParam.getSpaceAppBid(), bid, mSpaceAppData);                        } finally {
                            countDownLatch.countDown();
                        }
                    });
                }
            }
            //等待所有线程执行完毕
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
        return true;
    }

    private boolean judgeTargetIsVersionObject(String targetSpaceAppBid) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(targetSpaceAppBid);
        if (apmSpaceApp == null) {
            throw new PlmBizException("待删除实例的空间应用不存在！");
        }
        // 拆包防空指针
        return Boolean.TRUE.equals(apmSpaceApp.getIsVersionObject());
    }

    @Override
    public Boolean moveTreeNode(String spaceBid, String spaceAppBid, String targetSpaceAppBid, String sourceBid, List<MoveTreeNodeParam> moveTreeNodeParams) {
        return null;
    }

    @Override
    public Object addExpand(String spaceBid, String spaceAppBid, String source, AddExpandAo addExpandAo) {
        // 判断source为内部还是外部
        Function<AddExpandAo, Object> targetFunction = Optional.ofNullable(source)
                .map(val -> strategyMap.getOrDefault(val, this::addInner))
                .orElseThrow(() -> new PlmBizException("source不能为空"));
        addExpandAo.setSpaceBid(spaceBid);
        if(CollectionUtils.isNotEmpty(addExpandAo.getTargetMObjects())){
            for(MSpaceAppData mSpaceAppData: addExpandAo.getTargetMObjects()){
                mSpaceAppData.remove("id");
            }
        }
        return targetFunction.apply(addExpandAo);
    }

    /**
     * 对象实例复制
     * @param mObjectCopyAo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyMObject(MObjectCopyAo mObjectCopyAo){
        //查询复制源对象关系数据
        RelationMObject relationMObjectCopy = RelationMObject.builder().relationModelCode(mObjectCopyAo.getRelationModelCode()).sourceBid(mObjectCopyAo.getCopySourceBid()).build();
        List<MObject> copyRelations = objectModelCrudI.listOnlyRelationMObjects(relationMObjectCopy);
        if(CollectionUtils.isEmpty(copyRelations)){
            throw new PlmBizException("复制源对象关系数据不存在");
        }
        //查询复制目标对象关系实例数据
        List<String> targetBids = new ArrayList<>();
        for (MObject mObject : copyRelations) {
            mObject.put(RelationEnum.SOURCE_BID.getCode(), mObjectCopyAo.getSourceBid());
            mObject.put(RelationEnum.SOURCE_DATA_BID.getCode(), mObjectCopyAo.getSourceDataBid());
            mObject.setBid(SnowflakeIdWorker.nextIdStr());
            mObject.setId(null);
            mObject.put(TranscendModelBaseFields.DATA_BID, mObject.getBid());
            targetBids.add(mObject.get(RelationEnum.TARGET_BID.getCode()) + "");
        }
        List<MObject> targetDatas = objectModelCrudI.listByBids(targetBids, mObjectCopyAo.getTargetModelCode());
        if(CollectionUtils.isEmpty(targetDatas)){
            throw new PlmBizException("复制目标对象关系实例数据不存在");
        }
        Map<String,String> targetBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        List<String> attrs = mObjectCopyAo.getAttrs();
        if(CollectionUtils.isEmpty(attrs)){
            attrs = new ArrayList<>();
        }
        //设置内置字段
        attrs.add(TranscendModelBaseFields.MODEL_CODE);
        attrs.add(TranscendModelBaseFields.NAME);
        attrs.add(TranscendModelBaseFields.TENANT_ID);
        attrs.add(TranscendModelBaseFields.PARENT_BID);
        //初始生命周期编码
        ApmLifeCycleStateVO apmLifeCycleStateVO = apmSpaceAppConfigManageService.getLifeCycleState(mObjectCopyAo.getTargetSpaceAppBid());
        //加工数据
        for(MObject mObject: targetDatas){
            String bid = SnowflakeIdWorker.nextIdStr();
            targetBidMap.put(mObject.getBid(),bid);
            for(Map.Entry<String,Object> entry : mObject.entrySet()){
                if(!attrs.contains(entry.getKey())){
                    mObject.put(entry.getKey(),null);
                }
            }
            //设置内置字段
            mObject.setBid(bid);
            mObject.put(TranscendModelBaseFields.DATA_BID,bid);
            mObject.put(TranscendModelBaseFields.SPACE_APP_BID,mObjectCopyAo.getTargetSpaceAppBid());
            mObject.put(TranscendModelBaseFields.SPACE_BID,mObjectCopyAo.getSpaceBid());
            if(mObject.get(ObjectEnum.LIFE_CYCLE_CODE.getCode()) == null){
                mObject.setLifeCycleCode(apmLifeCycleStateVO.getInitState());
            }
            mObject.setLcTemplBid(apmLifeCycleStateVO.getLcTemplBid());
            mObject.setLcTemplVersion(apmLifeCycleStateVO.getLcTemplVersion());
            mObject.put(TranscendModelBaseFields.LC_MODEL_CODE,mObject.getLifeCycleCode()+":"+mObject.getModelCode());
            mObject.setCreatedBy(SsoHelper.getJobNumber());
            mObject.setDeleteFlag(false);
            mObject.setId(null);
            mObject.setCreatedTime(LocalDateTime.now());
            mObject.setUpdatedBy(mObject.getCreatedBy());
            mObject.setUpdatedTime(mObject.getCreatedTime());
        }
        for (int i = copyRelations.size()-1;i>=0;i--){
            MObject mObject = copyRelations.get(i);
            mObject.put(RelationEnum.TARGET_BID.getCode(),targetBidMap.get(mObject.get(RelationEnum.TARGET_BID.getCode())+""));
            if(mObject.get(RelationEnum.TARGET_BID.getCode()) == null || StringUtils.isEmpty(mObject.get(RelationEnum.TARGET_BID.getCode())+"")){
                copyRelations.remove(i);
            }
        }
        for(MObject mObject: targetDatas){
            if(mObject.containsKey(TranscendModelBaseFields.PARENT_BID)){
                mObject.put(TranscendModelBaseFields.PARENT_BID,targetBidMap.get(mObject.get(TranscendModelBaseFields.PARENT_BID)+""));
            }
        }
        appDataService.addBatch(mObjectCopyAo.getRelationModelCode(),copyRelations);
        return appDataService.addBatch(mObjectCopyAo.getTargetModelCode(),targetDatas);
    }

    @Override
    public void exportExcel(String spaceAppBid, String type, RelationMObject relationMObject, HttpServletResponse response) {
        //根据type获取关系数据
        List<MObject> mObjects;
        if(DefaultAppExcelTemplateService.TEMPLATE_TYPE_TREE.equals(type)){
            mObjects = objectModelCrudI.relationTree(relationMObject);
        }else{
            mObjects = objectModelCrudI.listRelationMObjects(relationMObject);
        }
        String fileName = null;
        try {
            fileName = URLEncoder.encode(com.transcend.framework.common.util.SnowflakeIdWorker.nextIdStr()+".xlsx", StandardCharsets.UTF_8.name()).replace("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new TranscendBizException("生成文件名称错误",e);
        }
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename="+fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        try (ByteArrayOutputStream outputStream = appExcelTemplateService.exportExcel(spaceAppBid, type,mObjects)) {
            response.getOutputStream().write(outputStream.toByteArray());
        } catch (IOException e) {
            throw new TranscendBizException("生成Excel失败",e);
        }
    }

    @Override
    public List<MObject> listRelation(String spaceBid ,String spaceAppBid, RelationMObject relationMObject) {
        return objectModelCrudI.listRelationMObjects(false,relationMObject);
    }

    @Override
    public PagedResult<MObject> listRelationPage(String spaceBid, String spaceAppBid, BaseRequest<RelationMObject> relationMObject, boolean filterRichText) {
        PagedResult<MObject> pagedResult = objectModelCrudI.listRelationMObjectsPage(false, relationMObject, spaceAppBid, filterRichText);

        Optional.ofNullable(pagedResult).map(PagedResult::getData).ifPresent(StayDurationHandler::handle);

        //根据属性的字典顺序排序
        if (relationMObject.getParam().getModelMixQo() != null) {
            List<Order> orders = relationMObject.getParam().getModelMixQo().getOrders();
            Map<String, Map<String, Integer>> dictSortMap = dictionaryApplicationService.getDictSortMap(orders);
            if (CollectionUtils.isNotEmpty(dictSortMap)) {
                pagedResult.setData(ObjectTreeTools.sortObjectList(orders, pagedResult.getData(), dictSortMap));
            }
        }
        return pagedResult;
    }

    @Override
    public List<MObjectTree> tree(String spaceBid, String spaceAppBid, RelationMObject relationMObject, boolean filterRichText) {
        return objectModelCrudI.relationTree(false, relationMObject, filterRichText);
    }


    @Override
    public Boolean add(SpaceAppRelationAddParam relationMObject) {
        List<MObject> relMObjects = new ArrayList<>();
        String spaceBid = relationMObject.getSpaceBid();
        String spaceAppBid = relationMObject.getSpaceAppBid();
        String sourceBid = relationMObject.getSourceBid();
        String relationModelCode = relationMObject.getTargetModelCode();
        String jobNumber = SsoHelper.getJobNumber();
        String employeeName = SsoHelper.getName();
        Long tenantId = SsoHelper.getTenantId();
        List<MSpaceAppData> targetMObjects = relationMObject.getTargetMObjects();
        // 保留一份目标实例的复制体，用于日志记录
        List<MSpaceAppData> targetMObjectsCopy = Lists.newArrayList();
        targetMObjectsCopy.addAll(relationMObject.getTargetMObjects());
        List<MSpaceAppData> copyTargetMObjects = new ArrayList<>(targetMObjects);
        ApmSpaceApp targetSpaceApp = new ApmSpaceApp();
        if(StringUtils.isNotEmpty(relationMObject.getTargetModelCode())){
            targetSpaceApp = apmSpaceAppService.getBySpaceBidAndModelCode(spaceBid,relationMObject.getTargetModelCode());
        }
        for (int i = targetMObjects.size() - 1; i >= 0; i--) {
            MObject mObject = targetMObjects.get(i);
            String bid = mObject.getBid();
            String dataBid = mObject.get(TranscendModelBaseFields.DATA_BID) + "";
            boolean isSelected = false;
            if(relationMObject.getSelected() != null){
                isSelected = relationMObject.getSelected();
            }
            if (StringUtils.isEmpty(mObject.getBid()) || mObject.get(TranscendModelBaseFields.DATA_BID) == null) {
                bid = SnowflakeIdWorker.nextIdStr();
                dataBid = bid;
            }else{
                if(relationMObject.getSelected() == null){
                    isSelected = false;
                }
            }
            mObject.setBid(bid);
            mObject.setUpdatedBy(jobNumber);
            mObject.setCreatedBy(mObject.getUpdatedBy());
            mObject.put(TranscendModelBaseFields.DATA_BID, dataBid);
            if(mObject.get(SpaceAppDataEnum.SPACE_BID.getCode()) == null){
                mObject.put(SpaceAppDataEnum.SPACE_BID.getCode(),spaceBid);
            }
            if(mObject.get(SpaceAppDataEnum.SPACE_APP_BID.getCode()) == null){
                mObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(),targetSpaceApp.getBid());
            }
            mObject.setUpdatedTime(LocalDateTime.now());
            mObject.setCreatedTime(LocalDateTime.now());
            MObject relMObject = RelationUtils.collectRelationPo(relationMObject.getRelationModelCode(), sourceBid, relationMObject.getSourceDataBid(), bid, dataBid, spaceBid, tenantId);
            relMObjects.add(relMObject);
            if (isSelected) {
                targetMObjects.remove(i);
            }
        }
        if (TreeTypeConstant.TREE_TYPE_TREE.equals(relationMObject.getTreeType())) {
            Map<String, String> sequenceBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            for (MSpaceAppData targetMObject : relationMObject.getTargetMObjects()) {
                sequenceBidMap.put(targetMObject.get("sequence") + "", targetMObject.getBid());
            }
            for (MSpaceAppData targetMObject : relationMObject.getTargetMObjects()) {
                String sequence = targetMObject.get("sequence") + "";
                int lastedIndexOf = sequence.lastIndexOf(".");
                if (lastedIndexOf == -1) {
                    continue;
                }
                String parentBid = sequenceBidMap.get(sequence.substring(0, lastedIndexOf));
                targetMObject.put(TranscendModelBaseFields.PARENT_BID, parentBid);
            }
        }
        if (CollectionUtils.isNotEmpty(targetMObjects)) {
            List<MSpaceAppData> targetAppDatas = new ArrayList<>();
            for (MObject mObject : targetMObjects) {
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                // 填充对象实例
                mSpaceAppData.putAll(mObject);
                mObject.put(TranscendModelBaseFields.ENABLE_FLAG, 0);
                mObject.put(TranscendModelBaseFields.DELETE_FLAG, 0);
                mObject.put(TranscendModelBaseFields.TENANT_ID, SsoHelper.getTenantId());
                mObject.put(TranscendModelBaseFields.CREATED_BY, SsoHelper.getJobNumber());
                mObject.put(TranscendModelBaseFields.UPDATED_BY, SsoHelper.getJobNumber());
                mObject.put(TranscendModelBaseFields.CREATED_TIME, new Date());
                mObject.put(TranscendModelBaseFields.UPDATED_TIME, new Date());
                targetAppDatas.add(mSpaceAppData);
                if (StringUtil.isBlank(mObject.getModelCode())) {
                    mSpaceAppData.setModelCode(relationModelCode);
                }
            }

            // 获取里边的关系modelCode = 新增的实例数据的值，需要进行移除
            ApmSpaceAppDataDrivenOperationFilterBo filterBo = ApmSpaceAppDataDrivenOperationFilterBo.of()
                    .setIgnoreRelationModelCodes(
                            Sets.newHashSet(relationMObject.getRelationModelCode())
                    );
            // 新增，调用目标对象批量新增目的是要初始化目标对象绑定视图的角色人员，流程等信息
            apmSpaceAppDataDrivenService.addBatch(spaceBid, spaceAppBid, targetAppDatas, filterBo);
            for(MSpaceAppData mSpaceAppData : targetAppDatas){
                //发送通知
                operateBusiService.executeNotify(spaceAppBid, mSpaceAppData);
            }
        }
        if (CollectionUtils.isNotEmpty(relMObjects)) {
            // 树形视图，是全覆盖，所以需要先删除，再新增
            String relationModelCode1 = relationMObject.getRelationModelCode();
            if (AppViewModelEnum.TREE_VIEW.getCode().equals(relationMObject.getViewModelCode())) {
                // 删除源之前绑定的关系数据
                relationModelDomainService.logicalDeleteBySourceBid(
                        relationModelCode1, sourceBid);
            }
            // 存在目标对象的属性值从源对象中获取，需要把源对象添加(覆盖)到目标对象属性中
            List<CfgViewMetaDto> cfgViewMetaDtos = iApmSpaceAppConfigDrivenService.queryRelationMetaList(spaceAppBid, new ArrayList<>()).stream().filter(dto -> relationModelCode1.equals(dto.getRelationModelCode())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(cfgViewMetaDtos)) {
                int totalTasks = cfgViewMetaDtos.size() * copyTargetMObjects.size();
                CountDownLatch countDownLatch = new CountDownLatch(totalTasks);
                for (CfgViewMetaDto dto : cfgViewMetaDtos) {
                    for (MSpaceAppData obj : copyTargetMObjects) {
                        if(obj == null){
                            continue;
                        }
                        MSpaceAppData mSpaceAppData = new MSpaceAppData();
                        // 多选属性
                        if (Boolean.TRUE.equals(dto.getMultiple())) {
                            List<Object> objectList = new ArrayList<>();
                            Object value = obj.get(dto.getName());
                            if(value != null){
                                if(value instanceof List){
                                    objectList.addAll((Collection<?>) value);
                                }else{
                                    objectList.add(value);
                                }
                            }
                            objectList.add(sourceBid);
                            mSpaceAppData.put(dto.getName(), objectList.stream().distinct().collect(Collectors.toList()));
                        } else {
                            mSpaceAppData.put(dto.getName(), sourceBid);
                        }
                        mSpaceAppData.setSpaceBid(obj.getSpaceBid());
                        mSpaceAppData.setSpaceAppBid(obj.getSpaceAppBid());
                        //多线程处理 把主线程事务传播下去
                        CompletableFuture.runAsync(() -> {
                            try {
                                mSpaceAppData.put(PermissionCheckEnum.CHECK_PERMISSION.getCode(),false);
                                apmSpaceAppDataDrivenService.updatePartialContent(obj.getSpaceAppBid(), obj.getBid(), mSpaceAppData);
                            } finally {countDownLatch.countDown();
                            }
                        }, SimpleThreadPool.getInstance()).exceptionally(e -> {
                            throw new PlmBizException(e.getMessage());
                        });
                    }
                }
                //等待所有线程执行完毕
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (CollUtil.isNotEmpty(targetMObjectsCopy)) {
                    // 目标实例添加成功后，为源实例记录日志
                    String instanceNames = targetMObjectsCopy.stream()
                            .map(MSpaceAppData::getName)
                            .collect(Collectors.joining(","));
                    try {
                        ApmSpaceApp app = apmSpaceAppService.getByBid(relationMObject.getSourceSpaceAppBid());
                        GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                                .logMsg("关系中关联了[" + instanceNames + "]的实例数据")
                                .modelCode(app.getModelCode())
                                .instanceBid(sourceBid)
                                .type(EsUtil.EsType.LOG.getType())
                                .spaceBid(spaceBid)
                                .build();
                        operationLogEsService.genericSave(genericLogAddParam);
                    } catch (Exception e) {
                        log.error("关系中关联了[" + instanceNames + "]的实例数据", e);
                    }
                }
                // 存在多对多的情况，因此不能不管关系的新增了，不能 return
//                return true;
            }
            // 新增关系之前检查一下有哪些已经关联过了，就不能再关联
            List<MObject> insertRelMObjects  = obtainRemoveExitRelation(relationModelCode1,relMObjects);
            appDataService.addBatch(relationModelCode1, insertRelMObjects);
            RelationInfo relationInfo = new RelationInfo();
            relationInfo.setSourceModelCode(apmSpaceApplicationService.getModelCodeByAppBid(relationMObject.getSourceSpaceAppBid()));
            relationInfo.setModelCode(relationModelCode1);
            relationInfo.setTargetModelCode(relationMObject.getTargetModelCode());
            //发布事件
            NotifyEventBus.EVENT_BUS.post(NotifyRelationBatchAddBusDto.of(relationModelCode1,relationInfo,insertRelMObjects));
            if (CollUtil.isNotEmpty(targetMObjectsCopy)) {
                // 目标实例添加成功后，为源实例记录日志
                String instanceNames = targetMObjectsCopy.stream()
                        .map(MSpaceAppData::getName)
                        .collect(Collectors.joining(","));
                try {
                    ApmSpaceApp app = apmSpaceAppService.getByBid(relationMObject.getSourceSpaceAppBid());
                    GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                            .logMsg("关系中关联了[" + instanceNames + "]的实例数据")
                            .modelCode(app.getModelCode())
                            .instanceBid(sourceBid)
                            .type(EsUtil.EsType.LOG.getType())
                            .spaceBid(spaceBid)
                            .build();
                    operationLogEsService.genericSave(genericLogAddParam);
                } catch (Exception e) {
                    log.error("关系中关联了[" + instanceNames + "]的实例数据", e);
                }
            }
        }

        return true;
    }

    /**
     * 移除已经存在的关系
     * @param relationModelCode1 模型编码
     * @param relMObjects 关系实例对象集合
     * @return 过滤后的关系实例对象集合
     */
    private List<MObject> obtainRemoveExitRelation(String relationModelCode1, List<MObject> relMObjects) {
        // 如果输入的关系实例对象集合为空，直接返回空列表
        if (CollUtil.isEmpty(relMObjects)) {
            return Lists.newArrayList();
        }

        // 收集源对象BID，确保非空且避免空指针异常
        List<String> sourceBids = relMObjects.stream()
                .map(r -> Optional.ofNullable(r.get(RelationEnum.SOURCE_BID.getCode()))
                        .map(Object::toString)
                        .orElse(null))
                .filter(Objects::nonNull) // 过滤掉空值
                .collect(Collectors.toList());

        // 如果没有有效的源对象BID，直接返回空列表
        if (CollUtil.isEmpty(sourceBids)) {
            return Lists.newArrayList();
        }

        // 构建查询条件
        QueryWrapper relQo = new QueryWrapper();
        relQo.in(RelationEnum.SOURCE_BID.getColumn(), sourceBids)
                .and().eq(BaseDataEnum.DELETE_FLAG.getCode(), SpaceConstant.DELETE_FLAG_NO);
        List<QueryWrapper> relWrappers = QueryWrapper.buildSqlQo(relQo);
        QueryCondition relQueryCondition = new QueryCondition();
        relQueryCondition.setQueries(relWrappers);

        // 查询已存在的关系
        List<MRelationObject> exitRelations = relationModelDomainService.list(relationModelCode1, relQueryCondition);

        // 如果没有已存在的关系，直接返回原始的关系实例对象集合
        if (CollUtil.isEmpty(exitRelations)) {
            return relMObjects;
        }

        // 构建源对象BID为KEY，目标对象BID为VALUE的映射
        Map<String, Set<String>> sourceTargetMap = exitRelations.stream()
                .collect(Collectors.groupingBy(
                        MRelationObject::getSourceBid,
                        Collectors.mapping(MRelationObject::getTargetBid, Collectors.toSet())
                ));

        // 过滤掉已经存在的关系
        List<MObject> insertRelMObjects = new ArrayList<>(relMObjects);
        insertRelMObjects.removeIf(r -> {
            String sourceBid = Optional.ofNullable(r.get(RelationEnum.SOURCE_BID.getCode()))
                    .map(Object::toString)
                    .orElse(null);
            String targetBid = Optional.ofNullable(r.get(RelationEnum.TARGET_BID.getCode()))
                    .map(Object::toString)
                    .orElse(null);

            // 如果源对象BID或目标对象BID为空，保留该关系（不过滤）
            if (StringUtil.isBlank(sourceBid)
                    || StringUtil.isBlank(targetBid)) {
                return false;
            }

            // 如果目标对象BID存在于已有的映射中，则过滤掉
            Set<String> targetBids = sourceTargetMap.get(sourceBid);
            return CollUtil.isNotEmpty(targetBids) && targetBids.contains(targetBid);
        });

        return insertRelMObjects;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean multiBatchSelect(String spaceBid, String relationSpaceAppBid, ApmRelationMultiTreeAddParam apmRelationMultiTreeAddParam) {
        List<MObject> targetList = apmRelationMultiTreeAddParam.getTargetList();
        String relationModelCode = apmRelationMultiTreeAddParam.getRelationModelCode();
        String jobNumber = SsoHelper.getJobNumber();
        Long tenantId = SsoHelper.getTenantId();
        String sourceBid = apmRelationMultiTreeAddParam.getSourceBid();
        String sourceDataBid = apmRelationMultiTreeAddParam.getSourceDataBid();

        // 查询之前绑定的关系数据，需要更新目标数据的关系组件值
        List<MObject> relationList = objectModelCrudI.listRelationMObjects(RelationMObject.builder().sourceBid(sourceBid)
                .relationModelCode(relationModelCode).targetModelCode(apmRelationMultiTreeAddParam.getTargetModelCode())
                .sourceModelCode(apmRelationMultiTreeAddParam.getSourceModelCode())
                .build());
        Set<String> hasExitBid;
        if(CollectionUtils.isNotEmpty(relationList)){
            hasExitBid = relationList.stream().map(MObject::getBid).collect(Collectors.toSet());
        } else {
            hasExitBid = new HashSet<>();
        }
        // 目标实例数据bid
        List<String> bidList = Lists.newArrayList();
        // 关系数据
        List<MObject> relationObjectList = Lists.newArrayList();
        targetList.forEach(mObjectTree -> {
            //已存在关系数据不做新增
            if(!hasExitBid.contains(mObjectTree.getBid())){
                bidList.add(mObjectTree.getBid());
                MObject relationObject = new MObject();
                relationObject.setBid(SnowflakeIdWorker.nextIdStr());
                relationObject.put(RelationEnum.DATA_BID.getCode(), SnowflakeIdWorker.nextIdStr());
                relationObject.put(SpaceAppDataEnum.SPACE_BID.getCode(), spaceBid);
                relationObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), "0");
                relationObject.put(RelationObjectEnum.SOURCE_BID.getCode(), sourceBid);
                relationObject.put(RelationObjectEnum.SOURCE_DATA_BID.getCode(), apmRelationMultiTreeAddParam.getSourceDataBid());
                relationObject.put(RelationObjectEnum.TARGET_BID.getCode(), mObjectTree.getBid());
                relationObject.put(RelationObjectEnum.TARGET_DATA_BID.getCode(), mObjectTree.getOrDefault(RelationEnum.DATA_BID.getCode(), mObjectTree.getBid()));
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
        });

        // 新增新的关系数据
        if (CollectionUtils.isNotEmpty(relationObjectList)) {
            // 更新需求的关系组件值,如果关系组件是单选，表示只能有一个关系删除原来关联的关系数据，如果是多选,则增加当前关系数据
            updateRelationComponentValue(sourceBid, relationSpaceAppBid, relationModelCode, apmRelationMultiTreeAddParam.getTargetModelCode(), relationObjectList);
            // 绑定新的关系
            appDataService.addBatch(relationModelCode, relationObjectList);
            // 迭代-需求 版本-需求 关系时，特殊处理
            notifyCrossRelation(NotifyCrossRelationEventBusDto.builder()
                    .spaceBid(spaceBid)
                    .spaceAppBid(relationSpaceAppBid)
                    .currentRelationModelCode(relationModelCode)
                    .currentSourceModelCode(apmRelationMultiTreeAddParam.getSourceModelCode())
                    .currentTargetModelCode(apmRelationMultiTreeAddParam.getTargetModelCode())
                    .config(notifyCrossRelationConfig.getConfig()).currentSourceBid(sourceBid)
                    .currentSourceDataBid(sourceDataBid)
                    .currentTargetList(targetList).jobNumber(jobNumber).build());
        }
        return true;
    }


    /**
     * 通知方法 - 目前方法只针对迭代-需求，版本-需求关系
     * 当处理的关系为 迭代-需求时， 1.将新增的数据 同步到 版本-需求 和  项目-需求 2.将需求下面的用户故事的任务 同步到当前迭代下面
     * 当处理关系为 版本-需求时，将新增的数据 同步到 项目-需求
     * @version: 1.0
     * @date: 2023/10/25 11:10
     * @author: bin.yin
     */
    private void notifyCrossRelation(NotifyCrossRelationEventBusDto dto) {
        // 当前关系为 迭代-需求 或者 版本-需求
        if (REVISION_DEMAND_REL_MODEL_CODE.equals(dto.getCurrentRelationModelCode()) || "A2V".equals(dto.getCurrentRelationModelCode())) {
            if (REVISION_DEMAND_REL_MODEL_CODE.equals(dto.getCurrentRelationModelCode())) {
                // 当前关系为 迭代-需求，同步需求的开发任务到当前迭代下面
                NotifyReversionRelTaskBusDto copy = BeanUtil.copyProperties(dto, NotifyReversionRelTaskBusDto.class);
                copy.setConfig(notifyCrossRelationConfig.getRevisionRelTaskConfig());
                NotifyEventBus.publishReversionRelTask(copy);
            }
            NotifyEventBus.publishCrossEvent(dto);
        }
    }

    /**
     * 如果有关系组件，更新关系组件对应属性的值
     * @param sourceBid sourceBid 源数据bid
     * @param spaceAppBid spaceAppBid 空间应用bid
     * @param relationModelCode relationModelCode 关系modelCode
     * @param targetModelCode targetModelCode 目标modelCode
     * @param targetMObjectList targetMObjectList
     * @version: 1.0
     * @date: 2023/10/26 10:15
     * @author: bin.yin
     */
    private void updateRelationComponentValue(String sourceBid, String spaceAppBid, String relationModelCode, String targetModelCode, List<MObject> targetMObjectList) {
        CfgViewVo cfgViewVo = iApmSpaceAppConfigManageService.baseViewGet(spaceAppBid);
        if (Objects.isNull(cfgViewVo)) {
            return;
        }
        // 找出关系modelCode相同的关系组件
        Optional<CfgViewMetaDto> first = cfgViewVo.getMetaList().stream().filter(metaVo -> ViewComponentEnum.RELATION_CONSTANT.equals(metaVo.getType()) && relationModelCode.equals(metaVo.getRelationModelCode())).findFirst();
        if (first.isPresent()) {
            CfgViewMetaDto cfgViewMetaDto = first.get();
            for (MObject obj : targetMObjectList) {
                if(obj == null){
                    continue;
                }
                MObject mObject = new MObject();
                // 多选属性
                if (Boolean.TRUE.equals(cfgViewMetaDto.getMultiple())) {
                    List<Object> objectList = new ArrayList<>();
                    if(obj.get(cfgViewMetaDto.getName()) != null){
                        objectList.addAll((Collection<?>) obj.get(cfgViewMetaDto.getName()));
                    }
                    objectList.add(sourceBid);
                    mObject.put(cfgViewMetaDto.getName(), objectList.stream().distinct().collect(Collectors.toList()));
                } else {
                    mObject.put(cfgViewMetaDto.getName(), sourceBid);
                    //单选属性需要删除目标对象原来的关系数据
                    relationModelDomainService.logicalDeleteByTargetBid(relationModelCode, obj.get("targetBid") + "");
                }
                objectModelCrudI.batchUpdatePartialContentByIds(targetModelCode, mObject, Collections.singletonList(obj.get("targetBid") + ""));
            }
        }
    }

    @Override
    public List<MObjectTree> listMultiTree(ApmMultiTreeDto apmMultiTreeDto, boolean filterRichText) {

        //获取所有数据
        List<MObjectTree> list = apmSpaceAppDataDrivenService.multiTree(
                apmMultiTreeDto.getSpaceBid(), apmMultiTreeDto.getTargetSpaceAppBid() ,
                ApmMultiTreeModelMixQo.of(apmMultiTreeDto), filterRichText, true);

        if (CollectionUtils.isNotEmpty(list)) {
            //获取已经选中的数据
            RelationMObject relationMObject = RelationMObject.builder().sourceBid(apmMultiTreeDto.getSourceBid())
                    .relationModelCode(apmMultiTreeDto.getRelationModelCode()).build();
            List<MObject> mRelationObjectList = objectModelCrudI.listOnlyRelationMObjects(relationMObject);
            if (CollectionUtils.isNotEmpty(mRelationObjectList)) {
                //树形结构选中处理
                Map<String, String> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
                for (MObject mObject : mRelationObjectList) {
                    relModelMap.put(mObject.get("targetBid") + "", mObject.get(TranscendModelBaseFields.BID) + "");
                }
                handleTree(list, relModelMap, false, 0);
            }
            if (apmMultiTreeDto.isFilterUnchecked()) {
                //过滤未选中的数据
                List<MObjectTree> rrList = new ArrayList<>();
                listCheckedData(list, rrList);
                List<Order> orders = Optional.ofNullable(apmMultiTreeDto.getModelMixQo())
                        .map(ModelMixQo::getOrders).orElseGet(ArrayList::new);
                rrList = ObjectTreeTools.sortObjectList(orders, rrList, dictionaryApplicationService.getDictSortMap(orders));
                Map<String, List<MObjectTree>> parentMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
                Map<String, MObjectTree> bidAndParMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
                for (MObjectTree mObjectTree : rrList) {
                    List<MObjectTree> parList = parentMap.get(mObjectTree.getParentBid());
                    if (parList == null) {
                        parList = new ArrayList<>();
                    }
                    parList.add(mObjectTree);
                    parentMap.put(mObjectTree.getParentBid(), parList);
                    bidAndParMap.put(mObjectTree.getBid(), mObjectTree);
                }
                //找第一层节点
                List<MObjectTree> firstList = new ArrayList<>();
                for (MObjectTree mObjectTree : rrList) {
                    if (!bidAndParMap.containsKey(mObjectTree.getParentBid()) && (mObjectTree.get("notSelect") == null
                            || !((Boolean) mObjectTree.get("notSelect"))) ) {
                        //是根节点
                        setChildrenTree(mObjectTree, parentMap);
                        firstList.add(mObjectTree);
                    }
                }
                return firstList;
            }
        }
        return list;
    }



    @Override
    public List<MObjectTree> listMultiTreeGroupBy(ApmMultiTreeDto apmMultiTreeDto) {
        if(CollectionUtils.isEmpty(apmMultiTreeDto.getTree())){
            return new ArrayList<>();
        }
        List<MObjectTree> list = new ArrayList<>();
        //平铺数据
        filterNotContain(apmMultiTreeDto.getTree(),list);
        List<MObjectTree> groupList = list.stream().filter(
                e-> String.valueOf(e.get(apmMultiTreeDto.getGroupProperty()) == null?"":e.get(apmMultiTreeDto.getGroupProperty()))
                        .contains(apmMultiTreeDto.getGroupPropertyValue())).collect(Collectors.toList());
        Map<String,MObjectTree> groupMap = groupList.stream().collect(Collectors.toMap(MObjectTree::getBid, Function.identity()));
        Map<String,List<MObjectTree>> parentGroupMap = groupList.stream().collect(Collectors.groupingBy(MObjectTree::getParentBid));
        List<MObjectTree> groupTree = new ArrayList<>();
        groupList.forEach(e->{
            if("0".equals(e.getParentBid())|| !groupMap.containsKey(e.getParentBid())){
                groupTree.add(e);
            }
            e.setChildren(parentGroupMap.get(e.getBid()));
        });
        return groupTree;
    }

    @Override
    public GroupListResult<MSpaceAppData> listMultiTreeGroupKb(ApmMultiTreeDto apmMultiTreeDto, boolean filterRichText) {

        //获取所有数据
        List<MObjectTree> list = apmSpaceAppDataDrivenService.multiTree(
                apmMultiTreeDto.getSpaceBid(), apmMultiTreeDto.getTargetSpaceAppBid() ,
                ApmMultiTreeModelMixQo.of(apmMultiTreeDto), filterRichText, true);

        if (CollectionUtils.isNotEmpty(list)) {

            //获取已经选中的数据
            RelationMObject relationMObject = RelationMObject.builder().sourceBid(apmMultiTreeDto.getSourceBid()).relationModelCode(apmMultiTreeDto.getRelationModelCode()).build();
            List<MObject> mRelationObjectList = objectModelCrudI.listOnlyRelationMObjects(relationMObject);
            if (CollectionUtils.isNotEmpty(mRelationObjectList)) {
                //树形结构选中处理
                Map<String, String> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
                for (MObject mObject : mRelationObjectList) {
                    relModelMap.put(mObject.get("targetBid") + "", mObject.get(TranscendModelBaseFields.BID) + "");
                }
                handleTree(list, relModelMap, false, 0);
            }
            //过滤未选中的数据
            List<MObjectTree> rrList = new ArrayList<>();
            listCheckedData(list, rrList);
            Map<String, List<MObjectTree>> parentMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            Map<String, MObjectTree> bidAndParMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            for (MObjectTree mObjectTree : rrList) {
                mObjectTree.setChildren(null);
                List<MObjectTree> parList = parentMap.get(mObjectTree.getParentBid());
                if (parList == null) {
                    parList = new ArrayList<>();
                }
                parList.add(mObjectTree);
                parentMap.put(mObjectTree.getParentBid(), parList);
                bidAndParMap.put(mObjectTree.getBid(), mObjectTree);
            }
            //找第一层节点
            List<MObjectTree> firstList = new ArrayList<>();
            for (MObjectTree mObjectTree : rrList) {
                if (!bidAndParMap.containsKey(mObjectTree.getParentBid()) && (mObjectTree.get("notSelect") == null || !((Boolean) mObjectTree.get("notSelect")))) {
                    //是根节点
                    setChildrenTree(mObjectTree, parentMap);
                    firstList.add(mObjectTree);
                }
            }
            apmMultiTreeDto.setTree(firstList);
        }
        List<MObjectTree> allList = new ArrayList<>();
        //平铺数据
        filterNotContain(apmMultiTreeDto.getTree(),allList);
        Map<String, List<MObject>> groupList = allList.stream().collect(Collectors.groupingBy(mObject -> String.valueOf(mObject.get(apmMultiTreeDto.getGroupProperty()))));
        if (groupList.isEmpty()) {
            return new GroupListResult<>();
        }
        // 转换类型
        Map<String, List<MSpaceAppData>> groupListCover = groupList.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            // 转换类型
            return entry.getValue().stream().map(mObject -> {
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.putAll(mObject);
                return mSpaceAppData;
            }).collect(Collectors.toList());
        }));
        GroupListResult<MSpaceAppData> groupListResult = new GroupListResult<>();
        groupListResult.putAll(groupListCover);
        return groupListResult;
    }
    private void checkObjectContainGroupAttr(Set<String> modelCodes,String groupProperty){
        if(CollectionUtils.isEmpty(modelCodes)){
            return;
        }
        modelCodes.forEach(e->{
            CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(e).getCheckExceptionData();
            if (cfgObjectVo == null) {
                throw new TranscendBizException(String.format("模型[%s]不存在", e));
            }
            if (CollectionUtils.isEmpty(cfgObjectVo.getAttrList()) || cfgObjectVo.getAttrList().stream().noneMatch(attr -> groupProperty.equals(attr.getCode()))) {
                throw new TranscendBizException(String.format("模型[%s]中不存在[%s]分组属性", e, groupProperty));
            }
        });
    }
    private void filterNotContain(List<MObjectTree> tree, List<MObjectTree> list) {
        for(int i = 0; i < tree.size(); i++){
            MObjectTree newObject = new MObjectTree();
            newObject.putAll(tree.get(i));
            if(CollectionUtils.isNotEmpty(newObject.getChildren())){
                filterNotContain(newObject.getChildren(),list);
            }
            newObject.setChildren(null);
            list.add(newObject);
        }
    }

    @Override
    public GroupListResult<MSpaceAppData> groupList(String groupProperty, RelationMObject relationMObject) {
        GroupListResult<MSpaceAppData> groupListResult = new GroupListResult<>();
        List<MObject> list = objectModelCrudI.listRelationMObjects(relationMObject);
        if (CollectionUtils.isEmpty(list)) {
            return groupListResult;
        }
        Map<String, List<MObject>> groupList = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (StringUtil.isNotBlank(groupProperty)) {
            //验证分组属性是否存在
            CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(relationMObject.getTargetModelCode()).getCheckExceptionData();
            if (cfgObjectVo == null) {
                throw new TranscendBizException(String.format("模型[%s]不存在", relationMObject.getTargetModelCode()));
            }
            if (CollectionUtils.isEmpty(cfgObjectVo.getAttrList()) || cfgObjectVo.getAttrList().stream().noneMatch(attr -> groupProperty.equals(attr.getCode()))) {
                throw new TranscendBizException(String.format("模型[%s]中不存在[%s]分组属性", relationMObject.getTargetModelCode(), groupProperty));
            }
            groupList  = list.stream().collect(Collectors.groupingBy(mObject -> String.valueOf(mObject.get(groupProperty))));
        }else {
            groupList.put("default", list);
        }
        // 转换类型
        groupListResult.putAll(groupList.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            // 转换类型
            return entry.getValue().stream().map(mObject -> {
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.putAll(mObject);
                return mSpaceAppData;
            }).collect(Collectors.toList());
        })));
        return groupListResult;
    }

    @Override
    public List<MObjectTree> listRelationTree(String spaceBid, String spaceAppBid, RelationTreeQo qo) {
        return Collections.emptyList();
    }

    @Override
    public List<MObject> listRelParent(String spaceBid, String spaceAppBid, RelationParentQo qo) {
        return Collections.emptyList();
    }

    @Override
    public boolean structureBatchRemoveRel(String spaceBid, String spaceAppBid, StructureRelDel structureRelDel) {
        return false;
    }

    @Override
    public List<QueryPathVo> queryCadPath(String spaceBid, String spaceAppBid, QueryPathQo queryPathQo) {
        return Collections.emptyList();
    }


    private void setChildrenTree(MObjectTree mObjectTree, Map<String, List<MObjectTree>> parentMap) {
        List<MObjectTree> list = parentMap.get(mObjectTree.getBid());
        if (CollectionUtils.isNotEmpty(list)) {
            mObjectTree.setChildren(list);
            for (MObjectTree mObjectTree1 : list) {
                setChildrenTree(mObjectTree1, parentMap);
            }
        }
    }

    /**
     * 获取选中的树
     *
     * @param list
     * @param resList
     */
    private void listCheckTree(List<MObjectTree> list, List<MObjectTree> resList) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (MObjectTree mObjectTree : list) {
                if (mObjectTree.getChecked()) {
                    resList.add(mObjectTree);
                } else {
                    listCheckTree(mObjectTree.getChildren(), resList);
                }
            }
        }
    }



    private void listCheckedData(List<MObjectTree> list, List<MObjectTree> resList) {
        if (CollectionUtils.isNotEmpty(list)) {
            // 这里逆序是为了让resList保持和list排序相同
            Collections.reverse(list);
            for (int i = list.size() - 1; i >= 0; i--) {
                MObjectTree mObjectTree = list.get(i);
                if (!mObjectTree.getChecked() && (mObjectTree.get("notSelect") == null || (Boolean) mObjectTree.get("notSelect") == false)) {
                    list.remove(i);
                } else {
                    resList.add(mObjectTree);
                }
//                listCheckedData(mObjectTree.getChildren(), resList);
            }
        }
    }

    /**
     * 处理树是否选中
     *
     * @param list
     * @param map
     * @param chChecked
     */
    private void handleTree(List<MObjectTree> list, Map<String, String> map, boolean chChecked, int i) {
        i++;
        if (CollectionUtils.isNotEmpty(list)) {
            if (chChecked) {
                for (MObjectTree objectTree : list) {
                    objectTree.setChecked(true);
                    handleTree(objectTree.getChildren(), map, true, i);
                }
            } else {
                for (MObjectTree mObjectTree : list) {

                    if (i == 1) {
                        List<String> bidList = Optional.ofNullable(mObjectTree.getTreeBid())
                                .map(l->l.stream().filter(Objects::nonNull).collect(Collectors.toList()))
                                .filter(CollUtil::isNotEmpty)
                                .orElseGet(()-> ListUtil.toList(mObjectTree.getBid()));
                        Optional<String> bidOpt = bidList.stream().filter(map::containsKey).findFirst();

                        if (bidOpt.isPresent()){
                            mObjectTree.setChecked(true);
                            mObjectTree.setRelInstanceBid(map.get(bidOpt.get()));
                            handleTree(mObjectTree.getChildren(), map, false, i);
                            continue;
                        }

                    }

                    mObjectTree.setChecked(false);
                    handleTree(mObjectTree.getChildren(), map, false, i);

                }
            }
        }
    }

}
