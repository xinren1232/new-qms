package com.transcend.plm.datadriven.apm.space.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.constantenum.RelationBehaviorEnum;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectRelationRuleQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.MoveTreeNodeParam;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.relation.delete.StructureRelDel;
import com.transcend.plm.datadriven.api.model.relation.qo.QueryPathQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationParentQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationTreeQo;
import com.transcend.plm.datadriven.api.model.relation.vo.QueryPathVo;
import com.transcend.plm.datadriven.apm.constants.TreeTypeConstant;
import com.transcend.plm.datadriven.apm.event.config.NotifyCrossRelationConfig;
import com.transcend.plm.datadriven.apm.event.context.EventData;
import com.transcend.plm.datadriven.apm.event.context.TranscendEventContext;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplate;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowTemplateService;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MObjectCopyAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;
import com.transcend.plm.datadriven.apm.space.model.*;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmMultiTreeDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLifeCycleStateVO;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.*;
import com.transcend.plm.datadriven.apm.space.service.context.ApmSpaceAppRelationDataDrivenStrategyContext;
import com.transcend.plm.datadriven.apm.strategy.AddOuterStrategy;
import com.transcend.plm.datadriven.common.constant.RelationConst;
import com.transcend.plm.datadriven.common.constant.RelationObjectConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyCrossRelationEventBusDto;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyReversionRelTaskBusDto;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.BaseObjectTreeService;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import com.transcend.plm.datadriven.domain.object.base.RelationModelDomainService;
import com.transcend.plm.datadriven.domain.object.base.VersionModelDomainService;
import com.transcend.plm.datadriven.infrastructure.draft.po.DraftPO;
import com.transcend.plm.datadriven.infrastructure.draft.repository.DraftRepository;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.REVISION_DEMAND_REL_MODEL_CODE;

/**
 * @author unknown
 */
@Service(ApmSpaceAppRelationDataDrivenStrategyContext.VERSION + ApmSpaceAppRelationDataDrivenStrategyContext.STRATEGY_NAME)
@Slf4j
public class ApmSpaceAppRelationVersionDataDrivenServiceImpl implements IApmSpaceAppRelationDataDrivenService {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private ObjectModelDomainService objectModelDomainService;
    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;

    @Resource
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;
    @Resource
    private CfgObjectFeignClient cfgObjectClient;
    @Resource
    private BaseObjectTreeService baseObjectTreeService;
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
    private VersionModelDomainService versionModelDomainService;
    @Resource
    private ApmFlowTemplateService apmFlowTemplateService;

    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationClient;

    @Resource
    private DraftRepository draftRepository;

    @Resource
    private IApmSpaceAppConfigDrivenService iApmSpaceAppConfigDrivenService;
    @Resource
    private IAppDataService appDataService;



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
        SpaceAppRelationAddParam param =  SpaceAppRelationAddParam.builder()
                .relationModelCode(addExpandAo.getRelationModelCode())
                .sourceBid(addExpandAo.getSourceBid())
                .sourceDataBid(addExpandAo.getSourceDataBid())
                .spaceAppBid(addExpandAo.getSpaceAppBid())
                .spaceBid(addExpandAo.getSpaceBid())
                .viewModelCode(addExpandAo.getViewModelCode())
                .relationMObject(addExpandAo.getRelationMObject())
                .targetMObjects(addExpandAo.getTargetMObjects())
                .targetModelCode(addExpandAo.getTargetModelCode())
                .treeType(addExpandAo.getTreeType())
                .sourceSpaceAppBid(addExpandAo.getSourceSpaceAppBid())
                .build();
        return add(param);
    }


    /**
     * 源为有版本对象删除关系
     * @param spaceBid 空间Bid
     * @param relationDelAndRemParamAo 删除关系参数
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(String spaceBid, RelationDelAndRemParamAo relationDelAndRemParamAo) {
        // 关系ModelCode查询关系对象
        CfgObjectRelationVo relationVo =
                cfgObjectRelationClient.getRelation(relationDelAndRemParamAo.getModelCode()).getCheckExceptionData();
        // 判断源实例是否检出 或者 当前操作人不是检出人
        judgeIsCheckOutAndCheckOutBy(relationDelAndRemParamAo.getSourceBid(), relationVo.getSourceModelCode());
        // 使用代理对象，保障调用的本类方法能够被事务拦截
        IApmSpaceAppRelationDataDrivenService proxy = (IApmSpaceAppRelationDataDrivenService) AopContext.currentProxy();
        // 判断目标对象是否为版本对象
        if (judgeTargetIsVersionObject(relationDelAndRemParamAo.getTargetSpaceAppBid())) {
            // 如果为版本对象，则不支持删除实例，只支持删除关系，与移除逻辑一致
            return proxy.batchRemove(spaceBid, relationDelAndRemParamAo);
        }
        // 如果不是版本对象，则支持删除实例和关系
        List<String> targetBids = relationDelAndRemParamAo.getTargetBids();
        // 删除目标实例
        Boolean delInsRes = objectModelCrudI.batchDeleteByBids(relationVo.getTargetModelCode(), targetBids);
        // 删除关系
        return Boolean.TRUE.equals(delInsRes) && Boolean.TRUE.equals(proxy.batchRemove(spaceBid, relationDelAndRemParamAo));
    }

    /**
     * 源为有版本对象移除关系
     * @param spaceBid 空间Bid
     * @param relationDelAndRemParamAo 操作参数
     * @return 是否成功
     */
    @Override
    public Boolean batchRemove(String spaceBid, RelationDelAndRemParamAo relationDelAndRemParamAo) {
        if(CollectionUtils.isEmpty(relationDelAndRemParamAo.getRelationBids())){
            return Boolean.TRUE;
        }
        CfgObjectRelationVo relationVo =
                cfgObjectRelationClient.getRelation(relationDelAndRemParamAo.getModelCode()).getCheckExceptionData();
        // 判断源实例是否检出以及当前操作人是否是检出人
        judgeIsCheckOutAndCheckOutBy(relationDelAndRemParamAo.getSourceBid(), relationVo.getSourceModelCode());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in(RelationObjectConstant.BID, relationDelAndRemParamAo.getRelationBids());
        if (judgeTargetIsVersionObject(relationDelAndRemParamAo.getTargetSpaceAppBid())) {
            // 目标实例是版本对象，删除的是关系实例表中的草稿数据
            queryWrapper.and();
            queryWrapper.eq(RelationObjectEnum.DRAFT.getCode(), 1);
        }
        // 删除关系实例表数据
        Boolean deleteRel = versionModelDomainService.delete(relationDelAndRemParamAo.getModelCode(), QueryWrapper.buildSqlQo(queryWrapper));
        // 存在目标对象的属性值从源对象中获取，需要把源对象从目标对象属性中移除
        List<CfgViewMetaDto> cfgViewMetaDtos = iApmSpaceAppConfigDrivenService.queryRelationMetaList(relationDelAndRemParamAo.getTargetSpaceAppBid(), new ArrayList<>())
                .stream().filter(dto -> relationDelAndRemParamAo.getModelCode().equals(dto.getRelationModelCode())).collect(Collectors.toList());
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
                    //多线程处理
                    SimpleThreadPool.getInstance().execute(() -> {
                        try {
                            apmSpaceAppDataDrivenService.updatePartialContent(relationDelAndRemParamAo.getTargetSpaceAppBid(), bid, mSpaceAppData);                        } finally {
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
        }
        return Boolean.TRUE.equals(deleteRel);
    }

    @Override
    public Boolean removeAllRelation(SpaceAppRelationAddParam spaceAppRelationAddParam) {
        return null;
    }

    /**
     * 判断实例是否检出 以及 当前登录人是否是检出人
     * @param sourceBid 源实例bid
     * @param sourceModelCode 源实例模型code
     * @return true:未检出或者检出人不是当前登录人 false:已检出且检出人是当前登录人
     */
    private void judgeIsCheckOutAndCheckOutBy(String sourceBid, String sourceModelCode) {
        MVersionObject sourceVObj = versionModelDomainService.getByBid(sourceModelCode, sourceBid);
        DraftPO draftPO = draftRepository.getByBid(sourceBid);
        String checkoutBy = sourceVObj.getCheckoutBy();
        String jobNumber = SsoHelper.getJobNumber();
        if (!Objects.equals(checkoutBy, jobNumber)) {
            // 当前操作人不是检出人
            throw new PlmBizException("当前操作人与检出人不一致，请检查后重试！");
        }
//        if (!Boolean.TRUE.equals(sourceVObj.getOrDefault(RelationObjectEnum.DRAFT.getCode(), 0))) {
//            // 源实例未检出
//            throw new PlmBizException("当前源实例未检出，请检出后再操作");
//        }
        if (Objects.isNull(draftPO)) {
            // 源实例未检出
            throw new PlmBizException("当前源实例未检出，请检出后再操作");
        }
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
            fileName = URLEncoder.encode(SnowflakeIdWorker.nextIdStr()+".xlsx", StandardCharsets.UTF_8.name()).replace("\\+", "%20");
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
        return objectModelCrudI.listRelationMObjects(true,relationMObject);
    }

    @Override
    public PagedResult<MObject> listRelationPage(String spaceBid, String spaceAppBid, BaseRequest<RelationMObject> relationMObject, boolean filterRichText) {
        String relationBehavior = getRelationBehavior(relationMObject.getParam());
        return objectModelCrudI.listRelationMObjectsPage(true,relationMObject,relationBehavior,spaceAppBid, filterRichText);
    }

    private String getRelationBehavior(RelationMObject param){
        //查询源实例应用bid
        MObject sourceInstance = objectModelCrudI.getByBid(param.getSourceModelCode(), param.getSourceBid());
        if (sourceInstance == null) {
            throw new PlmBizException("复制目标对象关系实例数据不存在");
        }
        String spaceAppBid = (String)sourceInstance.get(SpaceAppDataEnum.SPACE_APP_BID.getCode());
        //先查询是否有流程状态
        List<ApmFlowTemplate> apmFlowTemplateList = apmFlowTemplateService.listStateFlowBySpaceAppBid(spaceAppBid);
        if(CollectionUtils.isNotEmpty(apmFlowTemplateList)){
            CfgObjectRelationVo objectRelationVo = cfgObjectRelationClient.getRelation(param.getRelationModelCode()).getCheckExceptionData();
           if (objectRelationVo.getBehavior().toUpperCase().contains(RelationBehaviorEnum.FIXED.getCode())){
               return RelationBehaviorEnum.FIXED.getCode();
           }else {
               return RelationBehaviorEnum.FLOAT.getCode();
           }
        }
        ObjectRelationRuleQo objectRelationRuleQo = new ObjectRelationRuleQo();
        objectRelationRuleQo.setLcTemplateBid(sourceInstance.get(TranscendModelBaseFields.LC_TEMPL_BID) + "");
        objectRelationRuleQo.setVersion(sourceInstance.get(TranscendModelBaseFields.LC_TEMPL_VERSION) + "");
        objectRelationRuleQo.setFromLifeCycleCode(sourceInstance.getLifeCycleCode());
        objectRelationRuleQo.setRelationModelCode(param.getRelationModelCode());
        objectRelationRuleQo.setTargetModelCode(sourceInstance.getModelCode());
        return cfgObjectRelationClient.getRelationRuleRes(objectRelationRuleQo).getCheckExceptionData();
    }

    @Override
    public List<MObjectTree> tree(String spaceBid, String spaceAppBid, RelationMObject relationMObject, boolean filterRichText) {
        return objectModelCrudI.relationTree(true, relationMObject, filterRichText);
    }

    @Override
    public List<MObjectTree> listRelationTree(String spaceBid, String spaceAppBid, RelationTreeQo qo) {
        String sourceBid = qo.getSourceBid();
        String targetModelCode = apmSpaceAppService.getByBid(spaceAppBid).getModelCode();
        String relationModelCode = qo.getRelationModelCode();
        MObject root = objectModelCrudI.getByBid(targetModelCode, sourceBid);
        TranscendEventContext.set(new EventData<>(root));
        root.put("isBorrow", false);
        QueryWrapper instanceQo = new QueryWrapper();
        List<QueryWrapper> instanceWrappers = QueryWrapper.buildSqlQo(instanceQo);
        QueryCondition queryCondition = new QueryCondition();
        // 默认更新时间倒序
        queryCondition.setOrders(org.assertj.core.util.Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
        queryCondition.setQueries(instanceWrappers);
        String jobNumber = SsoHelper.getJobNumber();
        // 查询当前应用下 所有cad文档 最新历史 和关系  最新和历史
        List<MObject> currentList = objectModelDomainService.list(targetModelCode, queryCondition);
        // 收集检出人为当前登录人的dataBid数据，查询草稿数据
        List<String> dataBids = currentList.stream()
                .filter(data -> jobNumber.equals(data.get(VersionObjectEnum.CHECKOUT_BY.getCode())))
                .map(data -> (String) data.get(VersionObjectEnum.DATA_BID.getCode())).collect(Collectors.toList());
        // 草稿数据map  key=dataBid value=bid
        Map<String, DraftPO> draftDataBidWithBidMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(dataBids)) {
            List<DraftPO> draftPoList = draftRepository.getByDataBids(dataBids);
            draftDataBidWithBidMap = draftPoList.stream().collect(Collectors.toMap(DraftPO::getDataBid, Function.identity(), (k1, k2) -> {
                if (k1.getCreatedTime().compareTo(k2.getCreatedTime()) <= 0) {
                    return k2;
                }else{
                    return k1;
                }
            }));
        }
        // 实例最新map  key=dataBid value=data
        Map<String, MObject> currentDataBidWithDataMap = currentList.stream().collect(Collectors.toMap(e -> (String) e.get(RelationEnum.DATA_BID.getCode()), Function.identity()));
        List<MObject> hisList = objectModelDomainService.listHis(targetModelCode, queryCondition);
        // 实例历史map  key=bid value=data
        Map<String, MObject> hisBidWithDataMap = hisList.stream().collect(Collectors.toMap(MObject::getBid, Function.identity(), (k1, k2) -> k1));
        QueryCondition relQueryCondition = new QueryCondition();
        // 默认更新时间倒序
        relQueryCondition.setOrders(org.assertj.core.util.Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
        List<MObject> relCurrentList = objectModelDomainService.list(relationModelCode, relQueryCondition);
        List<MObject> relHisList = objectModelDomainService.listHis(relationModelCode, relQueryCondition);
        // rel sourceBid -> targetBidList Map
        Map<String, List<MRelationObject>> relMap = relCurrentList.stream().map(e -> {
            MRelationObject mRelationObject = new MRelationObject();
            mRelationObject.putAll(e);
            return mRelationObject;
        }).collect(Collectors.groupingBy(MRelationObject::getSourceBid));
        // rel his sourceBid -> targetBidList Map
        Map<String, List<MRelationObject>> relHisMap = relHisList.stream().map(e -> {
            MRelationObject mRelationObject = new MRelationObject();
            mRelationObject.putAll(e);
            return mRelationObject;
        }).collect(Collectors.groupingBy(MRelationObject::getSourceBid));
        Map<String, String> checkOutMap = new HashMap<>(16);
        for (MObject mObject : currentList) {
            if (mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null && StringUtil.isNotBlank(mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "")) {
                checkOutMap.put(mObject.getBid(), mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "");
            }
        }
        List<MObjectTree> result = org.apache.commons.compress.utils.Lists.newArrayList();
        recursiveRelationTree(result, root, currentDataBidWithDataMap, hisBidWithDataMap, relMap, relHisMap, draftDataBidWithBidMap, checkOutMap);
        return result;
    }

    @Override
    public List<QueryPathVo> queryCadPath(String spaceBid, String spaceAppBid, QueryPathQo queryPathQo) {
        List<MObjectTree> tree = listRelationTree(spaceBid, spaceAppBid, RelationTreeQo.builder().sourceBid(queryPathQo.getSourceBid())
                .relationModelCode(queryPathQo.getRelationModelCode()).build());
        List<String> queryDataBids = queryPathQo.getQueryDataBids();
        List<QueryPathVo> pathVoList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(queryDataBids)) {
            return pathVoList;
        }
        EventData<MObject> eventData = TranscendEventContext.get();
        MObject root = eventData.getData();
        MObjectTree rootTree = new MObjectTree();
        rootTree.putAll(root);
        rootTree.setChildren(tree);
        Map<String, List<String>> pathsByBids = getPathsByBids(rootTree, queryDataBids);
        pathsByBids.forEach((dataBid, pathList) -> {
            QueryPathVo queryPathVo = new QueryPathVo(dataBid, pathList);
            pathVoList.add(queryPathVo);
        });
        return pathVoList;
    }

    public Map<String, List<String>> getPathsByBids(MObjectTree root, List<String> targetDataBids) {
        Map<String, List<String>> pathsMap = new HashMap<>(16);
        if (root == null || targetDataBids == null || targetDataBids.isEmpty()) {
            return pathsMap;
        }

        for (String targetDataBid : targetDataBids) {
            List<String> paths = getPathByDataBid(root, targetDataBid);
            if (CollectionUtils.isNotEmpty(paths)) {
                pathsMap.put(targetDataBid, paths);
            }
        }

        return pathsMap;
    }

    private List<String> getPathByDataBid(MObjectTree root, String targetDataBid) {
        List<String> paths = new ArrayList<>();
        if (root == null || targetDataBid == null) {
            return paths;
        }

        if (root.get(VersionObjectEnum.DATA_BID.getCode()).equals(targetDataBid)) {
            paths.add(root.getName());
            return paths;
        }

        for (MObjectTree child : root.getChildren()) {
            List<String> childPaths = getPathByDataBid(child, targetDataBid);
            if (!childPaths.isEmpty()) {
                for (String path : childPaths) {
                    paths.add(root.getName() + "=>" + path);
                }
            }
        }

        return paths;
    }

    private void recursiveRelationTree(List<MObjectTree> result, MObject parent,
                                       Map<String, MObject> currentDataBidWithDataMap, Map<String, MObject> hisBidWithDataMap,
                                       Map<String, List<MRelationObject>> relMap, Map<String, List<MRelationObject>> relHisMap,
                                       Map<String, DraftPO> draftDataBidWithBidMap, Map<String, String> checkOutMap) {
        boolean isHis = "history".equals(parent.get("dataSource"));
        boolean parentPathIsBorrow = (boolean) parent.get("isBorrow");
        // 是否历史 使用 关系当前map 还是 使用 关系历史map
        List<MRelationObject> relList = isHis ? relHisMap.get(parent.getBid()) : relMap.get(parent.getBid());
        if (CollectionUtils.isNotEmpty(relList)) {
            relList.forEach(rel -> {
                // 父数据路径是否借用
                // 子数据是否借用 1=借用 其他=非借用
                boolean isBorrow = "1".equals(String.valueOf(rel.get("isBorrow")));
                MObjectTree mObjectTree = new MObjectTree();
                MObject mObject;
                Boolean isInstance = getTargetModelRoute(rel.getRelBehavior(), isHis);
                if (isInstance) {
                    String targetDataBid = rel.getTargetDataBid();
                    mObject = currentDataBidWithDataMap.get(targetDataBid);
                    if (draftDataBidWithBidMap.containsKey(targetDataBid)) {
                        mObject.putAll(JSON.parseObject(draftDataBidWithBidMap.get(targetDataBid).getContent(), MObject.class));
                        mObject.put("dataSource", "draft");
                    } else {
                        mObject.put("dataSource", "instance");
                    }
                } else {
                    String targetBid = rel.getTargetBid();
                    mObject = hisBidWithDataMap.get(targetBid);
                    if (checkOutMap.containsKey(mObject.getBid())) {
                        mObject.put("dataSource", "instance");
                    } else {
                        mObject.put("dataSource", "history");
                    }
                    mObject.put(VersionObjectEnum.CHECKOUT_BY.getCode(), checkOutMap.get(targetBid));
                }
                mObject.put(RelationConst.RELATION_LIST_RELATION_TAG, rel);
                boolean isBorrowFlag = parentPathIsBorrow || isBorrow;
                // 用于记录 父路径 是否借用
                mObject.put("isBorrow", parentPathIsBorrow || isBorrow);
                // 当前实例是否可以检出，父路径不是借用，当前数据不是历史数据和草稿数据
                mObject.put("canCheckOut", !isBorrowFlag && "instance".equals(mObject.get("dataSource")));
                mObjectTree.putAll(mObject);
                mObjectTree.setParentBid(parent.getBid());
                result.add(mObjectTree);
                List<MObjectTree> children = org.apache.commons.compress.utils.Lists.newArrayList();
                recursiveRelationTree(children, mObject, currentDataBidWithDataMap, hisBidWithDataMap, relMap, relHisMap, draftDataBidWithBidMap, checkOutMap);
                mObjectTree.setChildren(children);
            });
        }
    }

    private Boolean getTargetModelRoute(String relationBehavior, Boolean isHis) {
        // 源对象是历史实例，目标查历史表
        if (isHis) {
            return Boolean.FALSE;
        }
        // 关系行为为浮动，目标查实例表
        if (RelationBehaviorEnum.FLOAT.getCode().equals(relationBehavior)) {
            return Boolean.TRUE;
        }
        //关系行为为固定，目标查历史表
        return Boolean.FALSE;
    }

    @Override
    public List<MObject> listRelParent(String spaceBid, String spaceAppBid, RelationParentQo qo) {
        // 根据targetBid查询关系表
        String relationModelCode = qo.getRelationModelCode();
        String sourceModelCode = apmSpaceAppService.getByBid(spaceAppBid).getModelCode();
        String targetBid = qo.getTargetBid();
        String targetDataBid = qo.getTargetDataBid();
        List<MObject> result = Lists.newArrayList();
        if ("instance".equals(qo.getDataSource()) || "draft".equals(qo.getDataSource())) {
            Set<String> sourceBids = listRelation(relationModelCode, targetBid, targetDataBid);
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.in(RelationEnum.BID.getColumn(), sourceBids);
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(queryWrapper);
            result = objectModelCrudI.list(sourceModelCode, queryWrappers);
            if (CollectionUtils.isNotEmpty(sourceBids)) {
                List<DraftPO> draftPoList = draftRepository.getByBids(Lists.newArrayList(sourceBids));
                for (DraftPO draftPo : draftPoList) {
                    MObject mObject = JSON.parseObject(draftPo.getContent(), MObject.class);
                    if (SsoHelper.getJobNumber().equals(mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()))) {
                        result.add(mObject);
                    }
                }
            }

        } else {
            // 目标数据为历史数据,按照固定 查历史
            result = objectModelCrudI.listHisSourceMObjects(relationModelCode, sourceModelCode, Lists.newArrayList(targetBid));
        }
        return result;
    }

    private Set<String> listRelation(String relationModelCode, String targetBid, String targetDataBid) {
        // 收集到返回的sourceBid
        Set<String> sourceBidSets = org.assertj.core.util.Sets.newHashSet();
        // 需要查询关系历史表的sourceDataBids
        Set<String> sourceDataBidByHis = org.assertj.core.util.Sets.newHashSet();
        // 通过targetDataBid 查询关系表
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.TARGET_DATA_BID.getColumn(), targetDataBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MRelationObject> mRelationObjectList;
        try {
            mRelationObjectList = relationModelDomainService.list(relationModelCode, queryWrappers);
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", relationModelCode));
        }
        if(CollectionUtils.isNotEmpty(mRelationObjectList)) {
            // 找到数据的情况, 按sourceDataBid分组
            Map<String, List<MRelationObject>> sourceDataBidMap = mRelationObjectList.stream().collect(Collectors.groupingBy(MRelationObject::getSourceDataBid));
            sourceDataBidMap.forEach((sourceDataBid, list) -> {
                // targetBid比查出来的大,返回空,比查出来的list小,查询关系历史表
                Optional<MRelationObject> queryHisOptional = list.stream().filter(e -> Long.parseLong(targetBid) < Long.parseLong(e.getTargetBid())).findAny();
                list.forEach(e -> {
                    // 每个源数据匹配targetBid,如果匹配到数据则返回,匹配不到则看关联行为
                    if (targetBid.equals(e.getTargetBid())) {
                        sourceBidSets.add(e.getSourceBid());
                    } else {
                        // 判断关联行为
                        if (RelationBehaviorEnum.FLOAT.getCode().equals(e.getRelBehavior())) {
                            // 浮动
                            sourceBidSets.add(e.getSourceBid());
                        } else {
                            // 固定, targetBid比查出来的大,返回空,比查出来的list小,查询关系历史表
                            if (queryHisOptional.isPresent()) {
                                sourceDataBidByHis.add(e.getSourceDataBid());
                            }
                        }
                    }
                });
            });
            if (CollectionUtils.isNotEmpty(sourceDataBidByHis)) {
                // 查询关系历史表
                listHisRelation(sourceBidSets, relationModelCode, sourceDataBidByHis, targetBid);
            }
        }
        return sourceBidSets;
    }

    private void listHisRelation(Set<String> sourceBids, String relationModelCode, Set<String> sourceDataBids, String targetBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.TARGET_DATA_BID.getColumn(), targetBid).and().in(RelationEnum.SOURCE_DATA_BID.getColumn(), sourceDataBids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MRelationObject> mRelationObjectList = relationModelDomainService.listHis(relationModelCode, queryWrappers);
        if (CollectionUtils.isNotEmpty(mRelationObjectList)) {
            // 按照sourceDataBid分组
            Map<String, List<MRelationObject>> sourceDataBidMap = mRelationObjectList.stream().collect(Collectors.groupingBy(MRelationObject::getSourceDataBid));
            sourceDataBidMap.forEach((sourceDataBid, list) -> {
                // 找targetBid唯一,如果有 则找到源数据, 如果没有,找到比targetBid大的第一个
                Optional<MRelationObject> first = list.stream().sorted(Comparator.comparing(MRelationObject::getTargetBid).reversed())
                        .filter(e -> Long.parseLong(e.getTargetBid()) >= Long.parseLong(targetBid)).findFirst();
                first.ifPresent(mRelationObject -> sourceBids.add(mRelationObject.getSourceBid()));
            });
        }
    }


    /**
     * TODO 性能待优化
     * 递归查询所有子关系数据
     * @param list list
     * @param sourceBid sourceBid
     * @param sourceModelCode sourceModelCode
     * @param relationModelCode relationModelCode
     * @param targetModelCode targetModelCode
     * @version: 1.0
     * @date: 2024/5/13 16:57
     * @author: bin.yin
     **/
    public void recursiveRelation(List<MObjectTree> list, String sourceBid, String sourceModelCode, String relationModelCode, String targetModelCode, ModelMixQo qo) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(e -> {
                e.setParentBid(sourceBid);
                List<MObject> childrenList = objectModelCrudI.listRelationMObjects(true, RelationMObject.builder().sourceBid(e.getBid()).sourceModelCode(sourceModelCode)
                        .relationModelCode(relationModelCode).targetModelCode(targetModelCode).build());
                List<MObjectTree> childrenTreeList = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(childrenList)) {
                    childrenList.forEach(children -> {
                        MObjectTree tree = new MObjectTree();
                        tree.putAll(children);
                        childrenTreeList.add(tree);
                    });
                    e.setChildren(childrenTreeList);
                    recursiveRelation(childrenTreeList, e.getBid(), sourceModelCode, relationModelCode, targetModelCode, qo);
                }
            });
        }
    }

    @Override
    public boolean structureBatchRemoveRel(String spaceBid, String spaceAppBid, StructureRelDel structureRelDel) {
        // 针对CAD文档的逻辑校验
        String targetModelCode = apmSpaceAppService.getByBid(spaceAppBid).getModelCode();
        String relationModelCode = structureRelDel.getRelationModelCode();
        Set<String> delRelPathSet = structureRelDel.getDelRelPathSet();
        String currentBid = structureRelDel.getCurrentBid();
        if (CollectionUtils.isEmpty(delRelPathSet)) {
            throw new TranscendBizException("请先选择需要移除的数据");
        }
        MObject root = objectModelCrudI.getByBid(targetModelCode, currentBid);
        // 查询当前currentBid 的树形结构
        List<MObjectTree> tree = listRelationTree(spaceBid, spaceAppBid, RelationTreeQo.builder().sourceBid(currentBid)
                .relationModelCode(relationModelCode).targetModelCode(targetModelCode).build());
        StringBuilder errorMsg = new StringBuilder();
        Map<String, List<String>> isBorrowMap = Maps.newHashMap();
        List<String> needRemoveRelBids = Lists.newArrayList();
        // 树路径
        StringBuilder currentPath = new StringBuilder(currentBid);
        // 深度优先遍历树
        recursiveTree(root, false, isBorrowMap, tree, currentPath, delRelPathSet, errorMsg, needRemoveRelBids);
        if (StringUtils.isNotBlank(errorMsg.toString())) {
            throw new TranscendBizException(errorMsg.deleteCharAt(errorMsg.length() - 1).toString());
        }
        if (CollectionUtils.isEmpty(needRemoveRelBids)) {
            throw new TranscendBizException("未找到移除的数据,可能数据已改变,请刷新数据重试");
        }
        // 移除关系
        objectModelCrudI.batchDeleteByBids(relationModelCode, needRemoveRelBids);
        return true;
    }

    private void recursiveTree(MObject parent, boolean isBorrowFlag, Map<String, List<String>> isBorrowMap, List<MObjectTree> childrenList,
                               StringBuilder currentPath, Set<String> delRelPathSet, StringBuilder errorMsg, List<String> needRemoveRelBids) {
        if (CollectionUtils.isNotEmpty(childrenList)) {
            for (MObjectTree child : childrenList) {
                StringBuilder path = new StringBuilder(currentPath);
                path.append(",").append(child.getBid());
                MRelationObject relationObject = (MRelationObject) child.get(RelationConst.RELATION_LIST_RELATION_TAG);
                // 是否借用,记录整个路径链路为借用的文档名称
                boolean currentFlag = "1".equals(String.valueOf(relationObject.get("isBorrow")));
                if (isBorrowFlag || currentFlag) {
                    List<String> nameList = isBorrowMap.get(currentPath.toString());
                    if (currentFlag) {
                        if (CollectionUtils.isEmpty(nameList)) {
                            nameList = Lists.newArrayList(child.getName());
                        } else {
                            nameList.add(child.getName());
                        }
                    }
                    isBorrowMap.put(path.toString(), nameList);
                }
                if (delRelPathSet.contains(path.toString())) {
                    // 校验整个父级【路径】文档是否是借用数据
                    if (isBorrowFlag) {
                        List<String> nameList = isBorrowMap.get(path.toString());
                        nameList.forEach(e -> errorMsg.append("【").append(e).append("】是借用数据;"));
                        return;
                    }
                    // 校验父级是否是历史数据
                    if ("history".equals(parent.get("dataSource"))) {
                        errorMsg.append("所选文档的父级【").append(parent.getName()).append("】是历史数据;");
                        return;
                    }
                    // 校验父级文档是否非当前登陆人检出
                    String checkOutBy  = (String) parent.get(VersionObjectEnum.CHECKOUT_BY.getCode());
                    if (StringUtils.isNotBlank(checkOutBy) && !SsoHelper.getJobNumber().equals(checkOutBy)) {
                        errorMsg.append("所选文档的父级【").append(parent.getName()).append("】非本人检出;");
                        return;
                    }
                    // 记录需要删除的关系bid
                    needRemoveRelBids.add(relationObject.getBid());
                }
                // 递归
                recursiveTree(child, isBorrowFlag, isBorrowMap, child.getChildren(), path, delRelPathSet, errorMsg, needRemoveRelBids);
            }
        }
    }

    @Override
    public Boolean add(SpaceAppRelationAddParam relationMObject) {
        List<MRelationObject> relMObjects = new ArrayList<>();
        String spaceBid = relationMObject.getSpaceBid();
        String spaceAppBid = relationMObject.getSpaceAppBid();
        String sourceBid = relationMObject.getSourceBid();
        String jobNumber = SsoHelper.getJobNumber();
        Long tenantId = SsoHelper.getTenantId();
        List<MSpaceAppData> targetMObjects = relationMObject.getTargetMObjects();
        List<MSpaceAppData> newTargetMObjects = new ArrayList<>();
        for (int i = targetMObjects.size() - 1; i >= 0; i--) {
            MSpaceAppData mObject = relationMObject.getTargetMObjects().get(i);
            String bid = mObject.getBid();
            String dataBid = mObject.get(TranscendModelBaseFields.DATA_BID) + "";
            boolean isSelected = false;
            if (StringUtils.isEmpty(mObject.getBid()) || mObject.get(TranscendModelBaseFields.DATA_BID) == null) {
                bid = SnowflakeIdWorker.nextIdStr();
                dataBid = bid;
            } else {
                isSelected = true;
            }
            mObject.setBid(bid);
            mObject.setUpdatedBy(jobNumber);
            mObject.setCreatedBy(mObject.getUpdatedBy());
            mObject.put(TranscendModelBaseFields.DATA_BID, dataBid);
            mObject.setUpdatedTime(LocalDateTime.now());
            mObject.setCreatedTime(LocalDateTime.now());
            MRelationObject relMObject = new MRelationObject();
            relMObject.setBid(bid);
            relMObject.setCreatedBy(mObject.getCreatedBy());
            relMObject.setUpdatedBy(mObject.getUpdatedBy());
            // 补充空间和空间应用bid
            relMObject.put(SpaceAppDataEnum.SPACE_BID.getCode(), spaceBid);
            relMObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), relationMObject.getSpaceAppBid());
            relMObject.put("sourceBid", relationMObject.getSourceBid());
            relMObject.put("sourceBid", relationMObject.getSourceBid());
            relMObject.put("targetBid", bid);
            relMObject.put("sourceDataBid", relationMObject.getSourceDataBid());
            relMObject.put("targetDataBid", dataBid);
            relMObject.put("draft", true);
            relMObject.put(TranscendModelBaseFields.BID, SnowflakeIdWorker.nextIdStr());
            relMObject.put(TranscendModelBaseFields.DATA_BID, SnowflakeIdWorker.nextIdStr());
            relMObject.setModelCode(relationMObject.getRelationModelCode());
            relMObject.put(TranscendModelBaseFields.DATA_BID, SnowflakeIdWorker.nextIdStr());
            relMObject.setCreatedBy(mObject.getCreatedBy());
            relMObject.setUpdatedBy(mObject.getCreatedBy());
            relMObject.setUpdatedTime(LocalDateTime.now());
            relMObject.setCreatedTime(LocalDateTime.now());
            relMObject.setEnableFlag(true);
            relMObject.setDeleteFlag(false);
            relMObject.setTenantId(tenantId);
            if (CollectionUtils.isNotEmpty(relationMObject.getRelationMObject())) {
                relMObject.putAll(relationMObject.getRelationMObject());
            }
            relMObjects.add(relMObject);
            if (!isSelected) {
                newTargetMObjects.add(mObject);
            }
        }
        if (TreeTypeConstant.TREE_TYPE_TREE.equals(relationMObject.getTreeType())) {
            DefaultAppExcelTemplateService.handlerParentBid(newTargetMObjects);
        }
        if (CollectionUtils.isNotEmpty(newTargetMObjects)) {
            ObjectModelLifeCycleVO objectModelLifeCycleVO = cfgObjectClient.findObjectLifecycleByModelCode(relationMObject.getTargetModelCode())
                    .getCheckExceptionData();
            List<MSpaceAppData> targetAppDatas = new ArrayList<>();
            for (MObject mObject : newTargetMObjects) {
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                // 填充对象实例
                mSpaceAppData.putAll(mObject);
                targetAppDatas.add(mSpaceAppData);
                objectModelCrudI.setMObjectDefaultValue(mSpaceAppData, objectModelLifeCycleVO, relationMObject.getTargetModelCode());
                if (StringUtil.isBlank(mObject.getModelCode())) {
                    mSpaceAppData.setModelCode(relationMObject.getTargetModelCode());
                }
            }
            // 获取里边的关系modelCode = 新增的实例数据的值，需要进行移除
            ApmSpaceAppDataDrivenOperationFilterBo filterBo = ApmSpaceAppDataDrivenOperationFilterBo.of()
                    .setIgnoreRelationModelCodes(
                            Sets.newHashSet(relationMObject.getRelationModelCode())
                    );
            // 新增，调用目标对象批量新增目的是要初始化目标对象绑定视图的角色人员，流程等信息
            apmSpaceAppDataDrivenService.addBatch(spaceBid, relationMObject.getSpaceAppBid(), targetAppDatas, filterBo);
        }
        if (CollectionUtils.isNotEmpty(relMObjects)) {
            // 树形视图，是全覆盖，所以需要先删除，再新增
            if (AppViewModelEnum.TREE_VIEW.getCode().equals(relationMObject.getViewModelCode())){
                // 删除源之前绑定的关系数据,包括历史表
                relationModelDomainService.logicalDeleteBySourceBid(
                        relationMObject.getRelationModelCode(), sourceBid);
            }
            // 存在目标对象的属性值从源对象中获取，需要把源对象添加(覆盖)到目标对象属性中
            List<CfgViewMetaDto> cfgViewMetaDtos = iApmSpaceAppConfigDrivenService.queryRelationMetaList(spaceAppBid, new ArrayList<>())
                    .stream().filter(dto -> relationMObject.getRelationModelCode().equals(dto.getRelationModelCode())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(cfgViewMetaDtos)) {
                int totalTasks = cfgViewMetaDtos.size() * targetMObjects.size();
                CountDownLatch countDownLatch = new CountDownLatch(totalTasks);
                for (CfgViewMetaDto dto : cfgViewMetaDtos) {
                    for (MSpaceAppData obj : targetMObjects) {
                        MSpaceAppData mSpaceAppData = new MSpaceAppData();
                        // 多选属性
                        if (Boolean.TRUE.equals(dto.getMultiple())) {
                            List<Object> objectList = new ArrayList<>((Collection<?>) obj.get(dto.getName()));
                            objectList.add(sourceBid);
                            mSpaceAppData.put(dto.getName(), objectList.stream().distinct().collect(Collectors.toList()));
                        } else {
                            mSpaceAppData.put(dto.getName(), sourceBid);
                        }
                        mSpaceAppData.setSpaceAppBid(obj.getSpaceAppBid());
                        mSpaceAppData.setSpaceBid(obj.getSpaceBid());
                        //多线程处理
                        SimpleThreadPool.getInstance().execute(() -> {
                            try {
                                apmSpaceAppDataDrivenService.updatePartialContent(obj.getSpaceAppBid(), obj.getBid(), mSpaceAppData);
                            } finally {
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
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(relationMObject.getSourceSpaceAppBid());
            RelationMObject param = RelationMObject.builder().sourceBid(sourceBid)
                    .sourceModelCode(apmSpaceApp.getModelCode())
                    .relationModelCode(relationMObject.getRelationModelCode()).build();
            // 查询 源实例 与目标实例的关联行为
            String relBehavior = getRelationBehavior(param);
            // 类型转换
            List<MObject> mObjects = new ArrayList<>();
            relMObjects.forEach(relationObject ->{
                MObject mObject = new MObject();
                mObject.putAll(relationObject);
                mObject.put(RelationEnum.REL_BEHAVIOR.getCode(), relBehavior);
                mObjects.add(mObject);
            });
            appDataService.addBatch(relationMObject.getRelationModelCode(), mObjects);
        }
        return true;
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
           if(!hasExitBid.contains(mObjectTree.getBid())) {
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
            // 绑定新的关系
            appDataService.addBatch(relationModelCode, relationObjectList);
            // 更新需求的关系组件值,如果关系组件是单选，表示只能有一个关系删除原来关联的关系数据，如果是多选,则增加当前关系数据
            updateRelationComponentValue(sourceBid, relationSpaceAppBid, relationModelCode, apmRelationMultiTreeAddParam.getTargetModelCode(), relationObjectList);
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
                    relationModelDomainService.logicalDeleteByTargetBid(relationModelCode, obj.getBid());
                }
                objectModelCrudI.batchUpdatePartialContentByIds(targetModelCode, mObject, Collections.singletonList(obj.getBid()));
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
                Map<String, String> relModelMap = new HashMap<>(16);
                for (MObject mObject : mRelationObjectList) {
                    relModelMap.put(mObject.get("targetBid") + "", mObject.get(TranscendModelBaseFields.BID) + "");
                }
                handleTree(list, relModelMap, false);
            }
            if (apmMultiTreeDto.isFilterUnchecked()) {
                //过滤未选中的数据
                List<MObjectTree> rrList = new ArrayList<>();
                listCheckedData(list, rrList);
                Map<String, List<MObjectTree>> parentMap = new HashMap<>(16);
                Map<String, MObjectTree> bidAndParMap = new HashMap<>(16);
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
                    if (!bidAndParMap.containsKey(mObjectTree.getParentBid()) && (mObjectTree.get("notSelect") == null
                            || (Boolean) mObjectTree.get("notSelect") == false)) {
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
        return null;
    }

    @Override
    public GroupListResult<MSpaceAppData> listMultiTreeGroupKb(ApmMultiTreeDto apmMultiTreeDto, boolean filterRichText) {
        return null;
    }

    @Override
    public GroupListResult<MSpaceAppData> groupList(String groupProperty, RelationMObject relationMObject) {
        GroupListResult<MSpaceAppData> groupListResult = new GroupListResult<>();
        List<MObject> list = objectModelCrudI.listRelationMObjects(relationMObject);
        if (CollectionUtils.isEmpty(list)) {
            return groupListResult;
        }
        Map<String, List<MObject>> groupList = new HashMap<>(16);
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


    private void listCheckedTree(List<MObjectTree> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = list.size() - 1; i >= 0; i--) {
                MObjectTree mObjectTree = list.get(i);
                if (!mObjectTree.getChecked() && (mObjectTree.get("notSelect") == null || (Boolean) mObjectTree.get("notSelect") == false)) {
                    list.remove(i);
                    list.addAll(mObjectTree.getChildren());
                } else {
                    listCheckedTree(mObjectTree.getChildren());
                }
            }
        }
    }

    private void listCheckedData(List<MObjectTree> list, List<MObjectTree> resList) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = list.size() - 1; i >= 0; i--) {
                MObjectTree mObjectTree = list.get(i);
                if (!mObjectTree.getChecked() && (mObjectTree.get("notSelect") == null || (Boolean) mObjectTree.get("notSelect") == false)) {
                    list.remove(i);
                } else {
                    resList.add(mObjectTree);
                }
                listCheckedData(mObjectTree.getChildren(), resList);
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
    private void handleTree(List<MObjectTree> list, Map<String, String> map, boolean chChecked) {
        if (CollectionUtils.isNotEmpty(list)) {
            if (chChecked) {
                for (MObjectTree objectTree : list) {
                    objectTree.setChecked(true);
                    handleTree(objectTree.getChildren(), map, true);
                }
            } else {
                for (MObjectTree mObjectTree : list) {
                    if (map.containsKey(mObjectTree.getBid())) {
                        mObjectTree.setChecked(true);
                        mObjectTree.setRelInstanceBid(map.get(mObjectTree.getBid()));
                        handleTree(mObjectTree.getChildren(), map, false);
                    } else {
                        mObjectTree.setChecked(false);
                        handleTree(mObjectTree.getChildren(), map, false);
                    }
                }
            }
        }
    }
}
