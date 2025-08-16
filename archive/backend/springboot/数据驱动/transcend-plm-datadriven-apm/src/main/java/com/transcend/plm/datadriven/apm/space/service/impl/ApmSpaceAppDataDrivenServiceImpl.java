package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.*;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.open.entity.EmployeeInfo;
import com.transcend.framework.open.service.OpenUserService;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.framework.sso.tool.TranscendUserContextHolder;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgViewFeignClient;
import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.MoveGroupNodeParam;
import com.transcend.plm.datadriven.api.model.dto.MoveTreeNodeParam;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.qo.ResourceQo;
import com.transcend.plm.datadriven.apm.common.ApmImplicitParameter;
import com.transcend.plm.datadriven.apm.constants.FlowEventTypeConstant;
import com.transcend.plm.datadriven.apm.constants.SpaceConstant;
import com.transcend.plm.datadriven.apm.dto.DeleteRelDto;
import com.transcend.plm.datadriven.apm.dto.NotifyObjectPartialContentDto;
import com.transcend.plm.datadriven.apm.dto.StateDataDriveDto;
import com.transcend.plm.datadriven.apm.enums.CommonEnum;
import com.transcend.plm.datadriven.apm.event.config.NotifyCrossRelationConfig;
import com.transcend.plm.datadriven.apm.flow.repository.po.*;
import com.transcend.plm.datadriven.apm.flow.service.*;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.log.model.dto.GenericLogAddParam;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogAddParam;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleUserAO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.StateDataDriveAO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.StateFlowTodoDriveAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionCheckDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAndIdentityVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmAccess;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmAccessService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.CfgWorkCalendarService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionCheckService;
import com.transcend.plm.datadriven.apm.space.enums.PermissionCheckEnum;
import com.transcend.plm.datadriven.apm.space.model.ApmSpaceAppDataDrivenOperationFilterBo;
import com.transcend.plm.datadriven.apm.space.model.AppViewModelEnum;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.pojo.bo.FieldUpdateEvent;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiAppConfig;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiAppTree;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiObjectUpdateDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.*;
import com.transcend.plm.datadriven.apm.space.pojo.vo.*;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;
import com.transcend.plm.datadriven.apm.space.repository.po.CadBatchAddInstanceRecord;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppViewModelService;
import com.transcend.plm.datadriven.apm.space.repository.service.CadBatchAddInstanceRecordService;
import com.transcend.plm.datadriven.apm.space.service.*;
import com.transcend.plm.datadriven.apm.space.service.context.ApmSpaceAppDataDrivenStrategyContext;
import com.transcend.plm.datadriven.apm.strategy.CustomBatchDeleteExtendStrategy;
import com.transcend.plm.datadriven.apm.strategy.CustomPageExtendStrategy;
import com.transcend.plm.datadriven.apm.strategy.HandleCustomFieldStrategy;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyCrossRelationEventBusDto;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.tool.ObjectTreeTools;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.EsUtil;
import com.transcend.plm.datadriven.common.util.ObjectCodeUtils;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.BaseObjectTreeService;
import com.transcend.plm.datadriven.domain.object.base.RelationModelDomainService;
import com.transsion.framework.auth.IUser;
import com.transsion.framework.auth.IUserContext;
import com.transsion.framework.auth.UserContextDto;
import com.transsion.framework.auth.dto.UserLoginDto;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.context.holder.UserContextHolder;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.api.model.RelationObjectEnum.SOURCE_BID;
import static com.transcend.plm.datadriven.api.model.RelationObjectEnum.TARGET_BID;
import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.*;
import static com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum.SPACE_BID;

/**
 * @author unknown
 */
@Slf4j
@Service(ApmSpaceAppDataDrivenStrategyContext.NORMAL + ApmSpaceAppDataDrivenStrategyContext.STRATEGY_NAME)
public class ApmSpaceAppDataDrivenServiceImpl implements IApmSpaceAppDataDrivenService {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI<MObject> objectModelCrudI;
    @Resource
    private IApmSpaceAppConfigDrivenService iApmSpaceAppConfigDrivenService;

    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;
    @Resource
    private OperationLogEsService operationLogEsService;
    @Resource
    private IApmSpaceAppConfigDrivenService apmSpaceAppConfigDrivenService;

    @Resource
    private IApmSpaceAppConfigManageService iAmSpaceAppConfigManageService;
    @Resource
    private IRuntimeService runtimeService;
    @Resource
    private ApmAccessService accessService;
    @Resource
    private DictionaryFeignClient dictionaryFeignClient;
    @Resource
    private ApmSpaceAppViewModelService apmSpaceAppViewModelService;
    @Resource
    private ApmRoleService apmRoleService;
    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private IBaseApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;
    @Resource
    private CfgObjectFeignClient cfgObjectFeignClient;
    @Resource
    private HandleCustomFieldStrategy handleCustomFieldStrategy;
    @Resource
    private CustomPageExtendStrategy customPageExtendStrategy;
    @Resource
    private BaseObjectTreeService baseObjectTreeService;
    @Resource
    private IAppDataService appDataService;
    @Resource
    private IPermissionCheckService permissionCheckService;
    @Resource
    private CadBatchAddInstanceRecordService cadBatchAddInstanceRecordService;

    @Resource
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;
    @Value("#{'${transcend.plm.apm.multiTree.notSelect.modelCode:}'.split(',')}")
    private Set<String> notSelectSet;

    @Resource
    private CustomBatchDeleteExtendStrategy customBatchDeleteExtendStrategy;

    private static final String _VIEW_BID = "_view_bid";

    /**
     * 需要修改实例 特殊字段需要回退流程，数据结构:flow_template_bid(流程bid),rollbakck node_data_bid(需要回退的节点dataBid),properties(修改的字段),updaetValue(修改的字段值),spaceAppBid(应用bid)
     */
    @Value("${transcend.plm.apm.update.rollBack.flowTemplate:1202612136402702336,1202616457924136961,isItNecessaryToEvaluateMajorAssetInvestment,Y,1195047614899425280}")
    private String rollBackTemplateBid;

    @Resource
    private CfgWorkCalendarService cfgWorkCalendarService;
    @Resource
    private CfgViewFeignClient viewFeignClient;
    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;

    @Resource
    private IAppExcelTemplateService appExcelTemplateService;

    @Resource
    private ApmFlowTemplateService apmFlowTemplateService;

    @Resource
    private ApmFlowNodeLineService apmFlowNodeLineService;

    @Resource
    private ApmFlowLineEventService apmFlowLineEventService;

    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationFeignClient;

    @Resource
    private CfgViewFeignClient cfgViewFeignClient;

    @Resource
    private RelationModelDomainService relationModelDomainService;

    @Resource
    private NotifyCrossRelationConfig notifyCrossRelationConfig;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public static final String NODE_REDIS_KEY = "node_check_redis_key";
    @Resource
    private OpenUserService openUserService;
    @Resource
    private ApmFlowApplicationService apmFlowApplicationService;

    @Value("#{'${transcend.plm.apm.blob.attr.filter:richTextContent,demandDesc,testingRecommendations,text}'.split(',')}")
    private List<String> filterBlobAttr;

    @Resource
    private DictionaryApplicationService dictionaryApplicationService;

    @Resource
    private ApmSpaceMultiTreeQueryService multiTreeQueryService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public MSpaceAppData add(String spaceAppBid, MSpaceAppData mSpaceAppData) {
        //流程没有传角色人员信息 需要组装对应的数据
        if(mSpaceAppData.containsKey(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode())){
            if(!mSpaceAppData.containsKey(SpaceAppDataEnum.ROLE_USER.getCode())){
                String workItemType = mSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode())+"";
                Map<String, List<String>> roleMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
                List<ApmRoleAndIdentityVo> apmRoleAndIdentityVos = apmFlowApplicationService.listFlowTemplateRoles(workItemType);
                if(CollectionUtils.isNotEmpty(apmRoleAndIdentityVos)){
                    for(ApmRoleAndIdentityVo apmRoleAndIdentityVo : apmRoleAndIdentityVos){
                        List<ApmUser> apmUserList = apmRoleAndIdentityVo.getApmUserList();
                        List<String> userIds = new ArrayList<>();
                        if(CollectionUtils.isNotEmpty(apmUserList)){
                            userIds = apmUserList.stream().map(ApmUser::getEmpNo).collect(Collectors.toList());
                        }
                        roleMap.put(apmRoleAndIdentityVo.getApmRoleVO().getBid(), userIds);
                    }
                }
                mSpaceAppData.put(SpaceAppDataEnum.ROLE_USER.getCode(), roleMap);
            }
        }
        //重置到达时间
        mSpaceAppData.put(ObjectEnum.REACH_TIME.getCode(), new Date());
        MSpaceAppData mSpaceAppDataRes = add(spaceAppBid, mSpaceAppData, null);
        //检查流程 是否正常启动，对异常启动做补偿
        if(mSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) != null){
            if(mSpaceAppData.isSyanCheckFlow() != null){
                if(mSpaceAppData.isSyanCheckFlow()){
                    CompletableFuture.runAsync(() -> runtimeService.runStartNode(mSpaceAppData.getBid(),mSpaceAppData.getSpaceBid(),spaceAppBid), SimpleThreadPool.getInstance());
                }else{
                    runtimeService.runStartNode(mSpaceAppData.getBid(),mSpaceAppData.getSpaceBid(),spaceAppBid);
                }
            }
        }
        return mSpaceAppDataRes;
    }

    @Override
    public MSpaceAppData add(String spaceAppBid, MSpaceAppData mSpaceAppData,
                             ApmSpaceAppDataDrivenOperationFilterBo filterBo) {
        String bid = mSpaceAppData.getBid();
        if(StringUtils.isEmpty(bid)){
            bid = SnowflakeIdWorker.nextIdStr();
            mSpaceAppData.setBid(bid);
            mSpaceAppData.put(TranscendModelBaseFields.DATA_BID, bid);
        }
        //设置初始生命周期状态
        if(mSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode()) == null || StringUtils.isEmpty(mSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode())+"")){
            ApmLifeCycleStateVO apmLifeCycleStateVO = apmSpaceAppConfigManageService.getLifeCycleState(spaceAppBid);
            if(apmLifeCycleStateVO != null){
                mSpaceAppData.setLifeCycleCode(apmLifeCycleStateVO.getInitState());
            }
        }
        // 初始化过滤器对象
        filterBo =
                filterBo == null ?
                        new ApmSpaceAppDataDrivenOperationFilterBo() :
                        filterBo;
        // 初始化校验
        checkSpaceAndAppBidNotNull(spaceAppBid);
        //获取空间应用数据
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        //任务和计划任务责任人需要自动加到流程中去
        if (TASK_MODEL_CODE.equals(app.getModelCode()) || PLAN_TASK_MODEL_CODE.equals(app.getModelCode())) {
            //任务需要设置责任人到流程
            if (mSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) != null && StringUtils.isNotEmpty(mSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) + "")) {
                setTaskPersonResponsible(mSpaceAppData);
            }
        }
        // 初始化赋值
        mSpaceAppData.setSpaceBid(app.getSpaceBid());
        mSpaceAppData.setSpaceAppBid(spaceAppBid);
        // 查询关系组件
        List<CfgViewMetaDto> relationModuleList = queryRelationMetaList(spaceAppBid, null, filterBo.getIgnoreRelationModelCodes());
        // 如果是特性和用户故事,关系组件传值了,multiTreeHead需要变为1
        extendRelationAddEventMethod(mSpaceAppData, app.getModelCode(), relationModuleList);
        // 处理特殊空间组件应用
        checkAddOrUpdateSpaceModule(app, mSpaceAppData);
        // 处理定制化字段
        handleCustomFieldStrategy.execute(app, mSpaceAppData);
        // 发起任务流程
        if (mSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) != null && StringUtils.isNotEmpty(mSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) + "")) {
            MObject mObject = new MObject();
            mObject.putAll(mSpaceAppData);
            CompletableFuture.runAsync(() -> runtimeService.startProcess(app.getSpaceBid(), spaceAppBid, mObject, Boolean.FALSE), SimpleThreadPool.getInstance());
        } else {
            //发起状态流程
            CompletableFuture.runAsync(() -> runtimeService.startStateFlow(app.getSpaceBid(), spaceAppBid, mSpaceAppData), SimpleThreadPool.getInstance());
        }
        // TODO 返回实体
        MObject mObject = appDataService.add(app.getModelCode(), mSpaceAppData);

        // 回调 TODO
        // 需求管理，创建项目时，需要初始化项目应用的一套逻辑(未来再回调函数中实现)
        if (SPACE_MODEL_CODE.equals(app.getModelCode())) {
            Object spaceTemplateBidObject = mSpaceAppData.get(SPACE_TEMPLATE_BID);
            String spaceTemplateBidString = null == spaceTemplateBidObject ? "" : spaceTemplateBidObject.toString();
            ApmSpaceDto apmSpaceApp = new ApmSpaceDto();
            apmSpaceApp.setBid(mSpaceAppData.getBid());
            apmSpaceApp.setTemplateBid(spaceTemplateBidString);
            apmSpaceApp.setInitSelfFlag(true);
            apmSpaceApp.setName(mSpaceAppData.getName());
            apmSpaceApplicationService.saveApmSpace(apmSpaceApp);
        }
        //记录操作日志
        try {
            GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder().logMsg("创建了[" + mObject.getName() + "]的实例数据")
                    .modelCode(app.getModelCode())
                    .instanceBid(mObject.getBid()).type(EsUtil.EsType.LOG.getType()).spaceBid(apmSpaceApplicationService.getSpaceBidBySpaceAppBid(spaceAppBid)).build();
            CompletableFuture.runAsync(() -> operationLogEsService.genericSave(genericLogAddParam), SimpleThreadPool.getInstance());
        } catch (Exception e) {
            log.error("创建了[" + mObject.getName() + "]的实例数据,记录日志失败", e);
        }
        //判断当前视图是否包含关系组件，如果包含关系组件需要增加关系数据
        checkAddOrUpdateRelationModule(app, relationModuleList, mSpaceAppData,
                Boolean.FALSE);
        return mSpaceAppData;
    }

    /**
     * 任务需要设置责任人到流程
     *
     * @param mSpaceAppData
     */
    private void setTaskPersonResponsible(MSpaceAppData mSpaceAppData) {
        Map<String, List<String>> roleList = (Map<String, List<String>>) mSpaceAppData.get(SpaceAppDataEnum.ROLE_USER.getCode());
        if (CollectionUtils.isNotEmpty(roleList)) {
            List<String> roleBids = new ArrayList<>(roleList.keySet());
            List<ApmRoleVO> apmRoleVOS = apmRoleService.listByRoleBids(roleBids);
            if (CollectionUtils.isNotEmpty(apmRoleVOS)) {
                Map<String, String> roleMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getBid, ApmRoleVO::getCode));
                for (Map.Entry<String, List<String>> entry : roleList.entrySet()) {
                    //判断是否是责任人角色
                    if (CommonEnum.PER_ROLE_CODE.getCode().equals(roleMap.get(entry.getKey()))) {
                        Object personResponsible = mSpaceAppData.get("personResponsible");
                        if (ObjectUtil.isNotNull(personResponsible)) {
                            if (personResponsible instanceof List) {
                                List<String> pers = (List) personResponsible;
                                entry.getValue().addAll(pers);
                            } else if (personResponsible instanceof String) {
                                entry.getValue().add((String) personResponsible);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean logicalDelete(String spaceAppBid, String bid) {
        //删除流程
        boolean deleteProcessResult = runtimeService.deleteProcess(runtimeService.getVersionInstanceBid(spaceAppBid, bid));
        //逻辑删除数据
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        boolean deleteResult = objectModelCrudI.logicalDelete(modelCode, bid);
        //逻辑删除该对象关联的关系数据
        Map<String, Set<String>> relationDeleteParams = getRelationDeleteParams(modelCode, Sets.newHashSet(bid));
        relationModelDomainService.batchLogicalDeleteBySourceBid(relationDeleteParams);
        //记录操作日志
        if (deleteResult) {
            try {
                GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder().logMsg("删除了[" + bid + "]的实例数据")
                        .modelCode(modelCode)
                        .instanceBid(bid).type(EsUtil.EsType.LOG.getType()).spaceBid(apmSpaceApplicationService.getSpaceBidBySpaceAppBid(spaceAppBid)).build();
                operationLogEsService.genericSave(genericLogAddParam);

                //20250526保存记录空间应用的删除日志
                GenericLogAddParam spaceAppGenericLogAddParam = GenericLogAddParam.builder().logMsg("删除了[" + bid + "]的实例数据")
                        .modelCode(modelCode)
                        .instanceBid(spaceAppBid).type(EsUtil.EsType.LOG.getType()).spaceBid(apmSpaceApplicationService.getSpaceBidBySpaceAppBid(spaceAppBid)).build();
                operationLogEsService.genericSave(spaceAppGenericLogAddParam);
            } catch (Exception e) {
                log.error("删除了[" + bid + "]的实例数据,记录日志失败", e);
            }
        }
        return deleteResult && deleteProcessResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean logicDeleteWithOperateAppBid(String spaceAppBid, String bid, String operateAppBid) {
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        // 先查询目标实例信息
        MObject mObject = objectModelCrudI.getByBid(app.getModelCode(), bid);
        IApmSpaceAppDataDrivenService proxy = (IApmSpaceAppDataDrivenService) AopContext.currentProxy();
        boolean result = proxy.logicalDelete(spaceAppBid, bid);
        // 记录操作日志
        if (result && operateAppBid != null) {
            try {
                // 查询树状图配置用于找到父对象实例
                ApmSpaceAppViewModelPo apmSpaceAppViewModelPo = new ApmSpaceAppViewModelPo();
                apmSpaceAppViewModelPo.setSpaceAppBid(operateAppBid);
                apmSpaceAppViewModelPo.setCode(AppViewModelEnum.MULTI_TREE_VIEW.getCode());
                List<ApmSpaceAppViewModelPo> apmSpaceAppViewModelPos = apmSpaceAppViewModelService.listByCondition(apmSpaceAppViewModelPo);
                ApmSpaceAppViewModelPo viewModelPo = apmSpaceAppViewModelPos.get(0);
                MultiAppConfig multiAppConfig = JSON.parseObject(JSON.toJSONString(viewModelPo.getConfigContent()), MultiAppConfig.class);
                MultiTreeConfigVo multiAppTreeConfig = multiAppConfig.getMultiAppTreeConfig();
                // 递归查询父对象信息
                MultiTreeConfigVo parentConfig = findParentSourceModelCode(app.getModelCode(), multiAppTreeConfig);
                if (parentConfig != null) {
                    // 通过关系获取父对象实例Bid
                    // 通过视图查找对应的属性字段
                    ViewQueryParam param = ViewQueryParam.of()
                            .setViewBelongBid(spaceAppBid);
                    List<CfgViewMetaDto> metaDtoList = cfgViewFeignClient.listMetaModels(param).getCheckExceptionData();
                    CfgViewMetaDto relationField = metaDtoList.stream()
                            .filter(dto -> "relation".equals(dto.getType()) && dto.getSourceModelCode().equals(parentConfig.getSourceModelCode()))
                            .findAny()
                            .orElseThrow(() -> new PlmBizException("未找到对应的视图属性字段！"));
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq(TARGET_BID.getCode(), bid);
                    // 通过目标的bid查询关系实例
                    List<MObject> mObjectList = objectModelCrudI.listByQueryWrapper(parentConfig.getRelationModelCode(), QueryWrapper.buildSqlQo(queryWrapper));
                    mObjectList.stream()
                            .filter(obj -> Objects.equals(obj.get(SOURCE_BID.getCode()), mObject.get(relationField.getName())))
                            .findAny()
                            .ifPresent(obj -> {
                                // 如果存在关系实例，则记录日志
                                String sourceModelCode = parentConfig.getSourceModelCode();
                                GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                                        .logMsg("删除了[" + mObject.getName() + "]的实例数据")
                                        .modelCode(sourceModelCode)
                                        .instanceBid(String.valueOf(obj.get(SOURCE_BID.getCode())))
                                        .type(EsUtil.EsType.LOG.getType())
                                        .spaceBid(app.getSpaceBid())
                                        .build();
                                operationLogEsService.genericSave(genericLogAddParam);

                                //20250527 保存记录空间应用的删除日志
                                GenericLogAddParam spaceAppGenericLogAddParam = GenericLogAddParam.builder()
                                        .logMsg("删除了[" + mObject.getName() + "]的实例数据")
                                        .modelCode(sourceModelCode)
                                        .instanceBid(String.valueOf(obj.get(TARGET_BID.getCode())))
                                        .type(EsUtil.EsType.LOG.getType())
                                        .spaceBid(app.getSpaceBid())
                                        .build();
                                operationLogEsService.genericSave(spaceAppGenericLogAddParam);
                            });
                }
            } catch (Exception e) {
                log.error("记录删除[" + mObject.getName() + "]的实例数据日志失败", e);
            }
        }
        return result;
    }


    /**
     * 通过子级ModelCode递归查询父级配置
     * @param sourceModelCode 子级ModelCode
     * @param multiAppTreeConfig 配置树
     * @return 配置信息
     */
    public MultiTreeConfigVo findParentSourceModelCode(String sourceModelCode, MultiTreeConfigVo multiAppTreeConfig) {
        if (multiAppTreeConfig == null || multiAppTreeConfig.getTargetModelCode() == null) {
            return null;
        }
        if (multiAppTreeConfig.getTargetModelCode().getSourceModelCode().equals(sourceModelCode)) {
            return multiAppTreeConfig;
        }
        return findParentSourceModelCode(sourceModelCode, multiAppTreeConfig.getTargetModelCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData) {
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        return appDataService.updateByBid(modelCode, bid, mSpaceAppData);
    }

    @Override
    public MSpaceAppData get(String spaceAppBid, String bid,boolean checkPermission) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
//        if (getDetailParamTransformer != null) {
//            getDetailParamTransformer.transform(apmSpaceApp);
//        }
        // 临时处理：201的开发管理详情，需要查询项目实例100的数据,后续通过拦截器处理,或者做数据同步 TODO
        String modelCode = apmSpaceApp.getModelCode();
        if (apmSpaceApp.getModelCode().equals("201")) {
            modelCode = "100";
        }
        MObject mObject = objectModelCrudI.getByBid(modelCode, bid,apmSpaceApp.getIsVersionObject());
        if(mObject == null){
            return null;
        }
        // TODO
        if (apmSpaceApp.getModelCode().equals("201")) {
            mObject.put(ObjectEnum.SPACE_BID.getCode(), apmSpaceApp.getSpaceBid());
            mObject.put(ObjectEnum.SPACE_APP_BID.getCode(), apmSpaceApp.getBid());
            mObject.setModelCode(apmSpaceApp.getModelCode());
        }
        if(checkPermission){
            PermissionCheckDto permissionCheckDto = new PermissionCheckDto();
            permissionCheckDto.setSpaceBid(mObject.get(RelationEnum.SPACE_BID.getCode()).toString());
            permissionCheckDto.setSpaceAppBid(mObject.get(RelationEnum.SPACE_APP_BID.getCode()).toString());
            permissionCheckDto.setInstanceBid(bid);
            permissionCheckDto.setOperatorCode(OperatorEnum.DETAIL.getCode());
            if (!permissionCheckService.checkSpaceAppPermssion(permissionCheckDto)){
                throw new BusinessException("没有查看详情权限");
            }
            //appDataService.checkPermission(mObject, OperatorEnum.DETAIL.getCode());
        }
        //获取关系组件数据
        getRelationModule(spaceAppBid, mObject);
        //流程信息补全
        getProcessRoleModule(spaceAppBid, mObject);
        //空间数据补全
        getSpaceModule(mObject);
        // 转换类型
        MSpaceAppData mSpaceAppData = new MSpaceAppData();
        mSpaceAppData.putAll(mObject);

        return mSpaceAppData;
    }

    @Override
    public MSpaceAppData getUpdatePropertyStatus(String spaceAppBid, String bid, boolean checkPermission) {
        String entries = redisTemplate.opsForValue().get(NODE_REDIS_KEY + bid);
        MSpaceAppData data = new MSpaceAppData();

        if (StringUtils.isNotEmpty(entries)) {
            JSONObject jsonObject = JSON.parseObject(entries);

            Object __UPDATE_LIST__ = jsonObject.get("__UPDATE_LIST__");
            if (ObjectUtil.isNotEmpty(__UPDATE_LIST__)) {
                data.put("__UPDATE_LIST__", __UPDATE_LIST__);
            }
            Object __UPDATE_INSTANCE__ = jsonObject.get("__UPDATE_INSTANCE__");
            if (ObjectUtil.isNotEmpty(__UPDATE_INSTANCE__)) {
                data.put("__UPDATE_INSTANCE__", __UPDATE_INSTANCE__);
            }
            Object expectedCompleteTime = jsonObject.get("expectedCompleteTime");
            JSONObject innerData = new JSONObject();
            if (ObjectUtil.isNotEmpty(expectedCompleteTime)) {
                innerData.put("expectedCompleteTime", expectedCompleteTime);
                data.put("data", innerData);
            }
            Object isOverDate = jsonObject.get("isOverDate");
            if (ObjectUtil.isNotEmpty(isOverDate)) {
                innerData.put("isOverDate", isOverDate);
                data.put("data", innerData);
            }
            Object checkPoint = jsonObject.get("checkPoint");
            if (ObjectUtil.isNotEmpty(checkPoint)) {
                innerData.put("checkPoint", checkPoint);
                data.put("data", innerData);
            }
        }

        return data;
    }

    @Override
    public List<MSpaceAppData> listByModelMixQo(String spaceAppBid, ModelMixQo modelMixQo) {
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        List<QueryWrapper> wrappers = QueryConveterTool.convert(modelMixQo).getQueries();
        QueryWrapper qo = new QueryWrapper();
        qo.eq(TranscendModelBaseFields.DELETE_FLAG, 0).and().eq(TranscendModelBaseFields.SPACE_APP_BID, spaceAppBid);
        wrappers = QueryConveterTool.appendMoreQueriesAndCondition(wrappers,QueryWrapper.buildSqlQo(qo));
        List <MObject> mObjects = objectModelCrudI.list(modelCode, wrappers);
        List<MSpaceAppData> result = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(mObjects)){
            for(MObject mObject : mObjects){
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.putAll(mObject);
                result.add(mSpaceAppData);
            }
        }
        return result;
    }

    /**
     * 查询项目人力资源信息
     *
     * @return
     */
    @Override
    public List<ResourceVo> getProjectResources(ResourceQo resourceQo) {
        String spaceAppBid = resourceQo.getSpaceAppBid();
        String instanceBid = resourceQo.getInstanceBid();
        List<String> instanceBids = resourceQo.getInstanceBids();
        Date startTime = resourceQo.getStartTime();
        Date endTime = resourceQo.getEndTime();
        List<MObject> resList = new ArrayList<>();
        //todo modelCode先写死，后面做成配置
        //项目和需求modeCode
        String pjRcModelCode = PROJECT_DEMAND_REL_MODEL_CODE;
        //用户故事和任务modelCode
        String taskRcModelCode = STORY_TASK_REL_MODEL_CODE;
        //用户故事modelCode
        String storyModelCode = DEMAND_STORY_MODEL_CODE;
        //任务modelCode
        String taskModelCode = TASK_MODEL_CODE;

        //项目和任务关系
        String pjTaskRelationCode = PROJECT_TASK_REL_MODEL_CODE;
        //查当前项目下的用户故事
        RelationMObject relationMObject = RelationMObject.builder().sourceBid(instanceBid).sourceModelCode(PROJECT_MODEL_CODE).relationModelCode(pjRcModelCode).targetModelCode(DEMAND_MODEL_CODE).build();
        if(CollectionUtils.isNotEmpty(instanceBids)){
            relationMObject = RelationMObject.builder().sourceBids(instanceBids).sourceModelCode(PROJECT_MODEL_CODE).relationModelCode(pjRcModelCode).targetModelCode(DEMAND_MODEL_CODE).build();
        }
        List<MObject> mObjectList = objectModelCrudI.listNotVersionRelationMObjects(relationMObject);
        //过滤掉非用户故事数据
        List<String> storyBids = new ArrayList<>();
        for (int i = mObjectList.size() - 1; i >= 0; i--) {
            MObject mObject = mObjectList.get(i);
            if (!storyModelCode.equals(mObject.getModelCode())) {
                mObjectList.remove(i);
            } else {
                storyBids.add(mObject.getBid());
            }
        }
        //查询用户故事下的任务 关系
        if(CollectionUtils.isNotEmpty(storyBids)){
            RelationMObject relationMObjectTask = RelationMObject.builder().relationModelCode(taskRcModelCode).sourceBids(storyBids).build();
            List<MObject> storyAndTaskRel = objectModelCrudI.listOnlyRelationMObjects(relationMObjectTask);
            //找所有任务
            Map<String, String> storyTaskMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            for (MObject mObject : storyAndTaskRel) {
                storyTaskMap.put(mObject.get(RelationEnum.SOURCE_BID.getCode()) + "", mObject.get(RelationEnum.TARGET_BID.getCode()) + "");
            }
            //过滤用户故事有任务的数据
            for (int i = mObjectList.size() - 1; i >= 0; i--) {
                MObject mObject = mObjectList.get(i);
                if (storyTaskMap.containsKey(mObject.getBid())) {
                    mObjectList.remove(i);
                }
            }
        }
        resList.addAll(mObjectList);
        //查询项目下迭代的任务
        String proDdModelCode = PROJECT_REVISION_REL_MODEL_CODE;
        String ddModelCode = REVISION_MODEL_CODE;
        RelationMObject relationDd = RelationMObject.builder().sourceBid(instanceBid).sourceModelCode(PROJECT_MODEL_CODE).relationModelCode(proDdModelCode).targetModelCode(ddModelCode).build();
        if(CollectionUtils.isNotEmpty(instanceBids)){
            relationDd = RelationMObject.builder().sourceBids(instanceBids).sourceModelCode(PROJECT_MODEL_CODE).relationModelCode(proDdModelCode).targetModelCode(ddModelCode).build();
        }
        List<MObject> mObjectDdList = objectModelCrudI.listNotVersionRelationMObjects(relationDd);
        //查询迭代下的任务
        if (CollectionUtils.isNotEmpty(mObjectDdList)) {
            List<String> ddBids = new ArrayList<>();
            for (MObject mObject : mObjectDdList) {
                ddBids.add(mObject.get(RelationEnum.BID.getCode()) + "");
            }
            //迭代和任务modelCode
            String ddTaskModelCode = REVISION_TASK_REL_MODEL_CODE;
            RelationMObject relationDdTask = RelationMObject.builder().sourceBids(ddBids).sourceModelCode(ddModelCode).relationModelCode(ddTaskModelCode).targetModelCode(taskModelCode).build();
            List<MObject> mObjectTaskList = objectModelCrudI.listNotVersionRelationMObjects(relationDdTask);
            resList.addAll(mObjectTaskList);
        }else{
            //查询项目下的任务
            String proTaskModelCode = PROJECT_TASK_REL_MODEL_CODE;
            RelationMObject relationTask = RelationMObject.builder().sourceBid(instanceBid).sourceModelCode(PROJECT_MODEL_CODE).relationModelCode(proTaskModelCode).targetModelCode(taskModelCode).build();
            if(CollectionUtils.isNotEmpty(instanceBids)){
                relationTask = RelationMObject.builder().sourceBids(instanceBids).sourceModelCode(PROJECT_MODEL_CODE).relationModelCode(proTaskModelCode).targetModelCode(taskModelCode).build();
            }
            List<MObject> taskObjectDdList = objectModelCrudI.listNotVersionRelationMObjects(relationTask);
            resList.addAll(taskObjectDdList);
        }
        Map<String, String> taskMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (CollectionUtils.isNotEmpty(resList)) {
            //过滤重复数据
            for (int i = resList.size() - 1; i >= 0; i--) {
                MObject object = resList.get(i);
                if (taskMap.containsKey(object.getBid() + "-" + object.getModelCode())) {
                    resList.remove(i);
                } else {
                    taskMap.put(object.getBid() + "-" + object.getModelCode(), object.getBid());
                }
            }
        }
        List<ResourceVo> resourceVos = mObject2ProjectResourceVo(resList, startTime, endTime);
        return resourceVos;
    }

    private List<ResourceVo> mObject2ProjectResourceVo(List<MObject> resList, Date startTime, Date endTime) {
        List<ResourceVo> resourceVos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resList)) {
            //计算时间
            long paramWorkHours = cfgWorkCalendarService.getWorkDays(startTime, endTime) * 8;
            for (MObject mObject : resList) {
                ResourceVo resourceVo = new ResourceVo();
                resourceVo.setParamWorkHours(paramWorkHours);
                resourceVo.setName(mObject.getName());
                resourceVo.setBid(mObject.getBid());
                resourceVo.setModelCode(mObject.getModelCode());
                resourceVo.setLifeCycleCode(mObject.getLifeCycleCode());
                resourceVo.setSpaceBid(mObject.get(TranscendModelBaseFields.SPACE_BID) == null ? null : mObject.get(TranscendModelBaseFields.SPACE_BID) + "");
                resourceVo.setSpaceAppBid(mObject.get(TranscendModelBaseFields.SPACE_APP_BID) == null ? null : mObject.get(TranscendModelBaseFields.SPACE_APP_BID) + "");
                if (mObject.get("completeRate") != null) {
                    resourceVo.setCompleteRate((Double) mObject.get("completeRate"));
                }
                //总工时
                Object workHour = mObject.get("workingHours");
                if (workHour != null && StringUtils.isNotEmpty(workHour + "")) {
                    resourceVo.setWorkingHours(Double.parseDouble(workHour + ""));
                }
                //剩余工时
                Object residualWorkingHours = mObject.get("residualWorkingHours");
                if (residualWorkingHours != null && StringUtils.isNotEmpty(residualWorkingHours + "")) {
                    resourceVo.setResidualWorkingHours(Double.parseDouble(residualWorkingHours + ""));
                }
                Object planTime = mObject.get("planEndTime");
                if (planTime == null) {
                    planTime = mObject.get("plannedCompletionTime");
                }
                if (planTime != null) {
                    resourceVo.setPlanEndTime(parseDate(planTime));
                }
                if (mObject.get("planStartTime") != null) {
                    resourceVo.setPlanStartTime(parseDate(mObject.get("planStartTime")));
                }
                Date planEndDate = getPladDateTime(resourceVo.getPlanEndTime());
                Date planStartDate = getPladDateTime(resourceVo.getPlanStartTime());
                boolean chkRes = checkTime(startTime, endTime, planStartDate, planEndDate);
                if (!chkRes) {
                    continue;
                }
                //计算换算的工作量
                try {
                    resourceVo.setCalWorkingHours(calWorkHours(resourceVo.getWorkingHours(), resourceVo.getResidualWorkingHours(), startTime, endTime, planStartDate, planEndDate));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                Object personResponsibleObj = mObject.get("personResponsible");
                if (personResponsibleObj != null) {
                    if (personResponsibleObj instanceof String) {
                        resourceVo.setPersonResponsible(personResponsibleObj + "");
                    } else if (personResponsibleObj instanceof List<?>) {
                        List personResponsibles = new ArrayList<>((List<?>) personResponsibleObj);
                        if (CollectionUtils.isNotEmpty(personResponsibles)) {
                            resourceVo.setPersonResponsible(personResponsibles.get(0) + "");
                        }
                    } else {
                        resourceVo.setPersonResponsible(personResponsibleObj + "");
                    }
                }
                if (StringUtils.isNotEmpty(resourceVo.getPersonResponsible())) {
                    if (resourceVo.getCompleteRate() == null) {
                        resourceVo.setCompleteRate((double) 0);
                    }
                    resourceVos.add(resourceVo);
                }
            }
        }
        return resourceVos;
    }

    /**
     * 计算换算的工作量
     *
     * @return
     */
    private Double calWorkHours(Double workHour, Double residualWorkingHours, Date startTime, Date endTime, Date planStartTime, Date planEndTime) throws ParseException {
        //统计时间需要大于当前时间
        SimpleDateFormat sd2 = new SimpleDateFormat("yyyy-MM-dd");
        if (workHour != null && startTime.getTime() >= sd2.parse(sd2.format(new Date())).getTime()) {
            if (residualWorkingHours == null) {
                residualWorkingHours = workHour;
            }
            //如果计划开始日期，计划结束日期，都在选择的范围内，换算的工作量 = 【当前剩余工作量】
            if (planStartTime.getTime() >= startTime.getTime() && planStartTime.getTime() <= endTime.getTime() && planEndTime.getTime() >= startTime.getTime() && planEndTime.getTime() <= endTime.getTime()) {
                return residualWorkingHours;
            }
            //工作项计划总天数
            long totalPlanDay = DateUtil.betweenDay(planEndTime, planStartTime, true);
            BigDecimal redisHours = new BigDecimal(residualWorkingHours);
            BigDecimal totalPlanDayBig = new BigDecimal(totalPlanDay);
            BigDecimal totalHoursBig = new BigDecimal(workHour);
            //如果计划结束日期>选择的结束日期，【总工作量（小时）】   -   {当前剩余工作量 /工作项计划总天数     * （【计划结束日期】 - 选择的结束日期）天}
            if (planEndTime.getTime() > endTime.getTime()) {
                long endDiffDay = DateUtil.betweenDay(planStartTime, endTime, true);
                BigDecimal bigDecimal = redisHours.divide(totalPlanDayBig, 2, RoundingMode.HALF_UP);
                BigDecimal bigDecima2 = bigDecimal.multiply(new BigDecimal(endDiffDay));
                BigDecimal calHours = totalHoursBig.subtract(bigDecima2).setScale(1, RoundingMode.HALF_UP);
                return calHours.doubleValue();
            }
            //如果计划开始日期<选择的开始日期，【总工作量（小时）】   -   {总工作量（小时） /工作项计划总天数     * （选择的开始日期  -  【计划开始日期】）天}
            if (planStartTime.getTime() < startTime.getTime()) {
                long endDiffDay = DateUtil.betweenDay(startTime, planStartTime, true);
                BigDecimal bigDecimal = totalHoursBig.divide(totalPlanDayBig, 2, RoundingMode.HALF_UP);
                BigDecimal bigDecima2 = bigDecimal.multiply(new BigDecimal(endDiffDay));
                BigDecimal calHours = totalHoursBig.subtract(bigDecima2).setScale(1, RoundingMode.HALF_UP);
                return calHours.doubleValue();
            }
        }
        return null;
    }

    private Date getPladDateTime(String dateTime) {
        Date date = null;
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (StringUtils.isNotEmpty(dateTime)) {
            try {
                date = sd.parse(dateTime);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new BusinessException("时间格式有误");
            }
        }
        return date;
    }

    private boolean checkTime(Date startTime, Date endTime, Date planStartTime, Date planEndTime) {
        long start = startTime.getTime();
        long end = endTime.getTime();
        if (planStartTime != null) {
            long planStart = planStartTime.getTime();
            if (planStart >= start && planStart <= end) {
                return true;
            }
        }
        if (planEndTime != null) {
            long planEnd = planEndTime.getTime();
            if (planEnd >= start && planEnd <= end) {
                return true;
            }
        }
        if (planStartTime != null && planEndTime != null) {
            long planStart = planStartTime.getTime();
            long planEnd = planEndTime.getTime();
            if (planStart <= start && planEnd >= end) {
                return true;
            }
        }
        return false;
    }

    private String parseDate(Object planTime) {
        if (planTime instanceof String) {
            return planTime + "";
        } else if (planTime instanceof LocalDateTime) {
            return LocalDateTimeUtil.format((LocalDateTime) planTime, "yyyy-MM-dd HH:mm:ss");
        } else if (planTime instanceof Date) {
            return DateUtil.format((Date) planTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else if (planTime instanceof LocalDate) {
            return LocalDateTimeUtil.format((LocalDate) planTime, "yyyy-MM-dd HH:mm:ss");
        }
        return null;
    }

    @Override
    public List<MSpaceAppData> batchGet(String spaceAppBid, List<String> bids) {
        List<MSpaceAppData> result = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in(BaseDataEnum.BID.getCode(), bids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(queryWrapper);
        try{
            return list(spaceAppBid, QueryCondition.of().setQueries(queryWrappers));
        }catch (Exception e)    {
            log.error("batchGet error", e);
        }
        return result;
    }

    @Override
    public Map<String, List<MSpaceAppData>> batchSpaceAppMapGet(Map<String, List<String>> bidMap) {

        //外层用户上下文
        log.info("=====batchSpaceAppMapGetUserContextHolder:{}",JSON.toJSONString( UserContextHolder.getUser()));

        List<Future<List<MSpaceAppData>> > objects = new ArrayList<>();
        IUserContext<IUser> user = UserContextHolder.getUser();

        for (Map.Entry<String, List<String>> map : bidMap.entrySet()) {
            Future<List<MSpaceAppData>> submit = SimpleThreadPool.getInstance().submit(() -> {
                //设置子线程数据
                try {
                    UserContextHolder.setUser(user);
                    TranscendUserContextHolder.setUser(user);
                    return batchGet(map.getKey(), map.getValue());
                } finally {
                    UserContextHolder.removeUser();
                    TranscendUserContextHolder.removeUser();
                }
            });
            objects.add(submit);
        }
        List<MSpaceAppData> all = new ArrayList<>();
        objects.forEach( t ->{
            try {
                List<MSpaceAppData> a = t.get(30, TimeUnit.SECONDS);
                all.addAll(a);
            } catch (Exception e) {
               log.error(String.valueOf(e));
            }
        });
        return  all.stream().collect(Collectors.groupingBy(MSpaceAppData::getSpaceAppBid));
    }

    @Override
    public List<MSpaceAppData> list(String spaceAppBid, QueryCondition queryCondition) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        if (apmSpaceApp == null) {
            throw new BusinessException("spaceAppBid不存在");
        }
        List<MObject> list = appDataService.list(apmSpaceApp.getSpaceBid(), spaceAppBid,
                queryCondition.setQueries(buildSpaceAppBid(spaceAppBid, queryCondition.getQueries())
                ));
        // 空直接退出
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(mObject -> {
            // 转换类型
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            mSpaceAppData.putAll(mObject);
            filterBlobAttr.forEach(mSpaceAppData::remove);
            return mSpaceAppData;
        }).collect(Collectors.toList());
    }

    @Override
    public PagedResult<MSpaceAppData> page(String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        // 前置客制化处理
        customPageExtendStrategy.preHandler(spaceAppBid, pageQo);
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        if (apmSpaceApp == null) {
            throw new BusinessException("spaceAppBid不存在");
        }
        pageQo.getParam().getQueries().add(new QueryWrapper(Boolean.TRUE).setSqlRelation(" and "));
        pageQo.getParam().setQueries(buildSpaceBid(apmSpaceApp.getSpaceBid(), pageQo.getParam().getQueries(), false));
        PagedResult pagedResult = appDataService.page(apmSpaceApp.getSpaceBid(),spaceAppBid, pageQo, filterRichText);
        List<MObject> list = pagedResult.getData();
        //重新排序
        List<Order> orders = pageQo.getParam().getOrders();
        sortDictionary(orders, list);
        // 空直接退出
        if (CollectionUtils.isEmpty(list)) {
            return pagedResult;
        }
        // 类型转换
        pagedResult.setData(
                list.stream().map(mObject -> {
                    // 转换类型
                    MSpaceAppData mSpaceAppData = new MSpaceAppData();
                    mSpaceAppData.putAll(mObject);
                    if (filterRichText) {
                        filterBlobAttr.forEach(mSpaceAppData::remove);
                    }
                    return mSpaceAppData;
                }).collect(Collectors.toList())
        );
        return pagedResult;
    }

    private void sortDictionary(List<Order> orders, List<MObject> list) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<String> dictList = orders.stream().filter(order -> StringUtils.isNotBlank(order.getProperty()) && StringUtils.isNotBlank(order.getRemoteDictType()) && order.getDesc() == null).map(Order :: getRemoteDictType).collect(Collectors.toList());
        if (dictList.isEmpty()) {
            return;
        }
        CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
        qo.setCodes(dictList);
        List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
        if (CollectionUtils.isEmpty(cfgDictionaryVos)) {
            return;
        }
        Map<String, Map<String, Integer>> dictMap = cfgDictionaryVos.stream()
                .collect(Collectors.toMap(
                        CfgDictionaryVo::getCode,
                        vo -> vo.getDictionaryItems().stream()
                                .collect(Collectors.toMap(
                                        CfgDictionaryItemVo::getKeyCode,
                                        CfgDictionaryItemVo::getSort
                                ))
                ));
        list.sort((o1, o2) -> {
            for (Order order : orders) {
                String property = order.getProperty();
                Object o1Value = o1.get(property);
                Object o2Value = o2.get(property);
                if (!Objects.equals(o1Value, o2Value) && (ObjectUtil.isNotEmpty(o1Value) || ObjectUtil.isNotEmpty(o2Value))) {
                    if (ObjectUtil.isEmpty(o1Value) || ObjectUtil.isEmpty(o2Value)) {
                        return ObjectUtil.isEmpty(o1Value) ? 1 : -1;
                    }
                    if (order.getDesc() == null && StringUtils.isNotBlank(order.getRemoteDictType()) && dictMap.containsKey(order.getRemoteDictType())) {
                        Integer o1Sort = dictMap.get(order.getRemoteDictType()).getOrDefault(o1Value.toString(),999);
                        Integer o2Sort = dictMap.get(order.getRemoteDictType()).getOrDefault(o2Value.toString(), 999);
                        return o1Sort.compareTo(o2Sort);
                    }
                    if (order.getDesc() != null && order.getDesc()) {
                        return o2Value.toString().compareTo(o1Value.toString());
                    }
                    return o1Value.toString().compareTo(o2Value.toString());
                }
            }
            return 0;
        });
    }

    @Override
    public PagedResult<MSpaceAppData> pageWithoutPermission(String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        // 前置客制化处理
        customPageExtendStrategy.preHandler(spaceAppBid, pageQo);
        pageQo.getParam().setQueries(buildSpaceAppBid(spaceAppBid, pageQo.getParam().getQueries()));
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        if (apmSpaceApp == null) {
            throw new BusinessException("spaceAppBid不存在");
        }
        PagedResult pagedResult = appDataService.pageWithoutPermission(apmSpaceApp.getSpaceBid(),spaceAppBid, pageQo, filterRichText);
        List<MObject> list = pagedResult.getData();
        // 空直接退出
        if (CollectionUtils.isEmpty(list)) {
            return pagedResult;
        }
        // 类型转换
        pagedResult.setData(
                list.stream().map(mObject -> {
                    // 转换类型
                    MSpaceAppData mSpaceAppData = new MSpaceAppData();
                    mSpaceAppData.putAll(mObject);
                    if (filterRichText) {
                        filterBlobAttr.forEach(mSpaceAppData::remove);
                    }
                    return mSpaceAppData;
                }).collect(Collectors.toList())
        );
        return pagedResult;
    }
    private void specialRollBackFlowTemp(ApmSpaceApp app, MSpaceAppData mSpaceAppData, String bid,MObject object){
        //特殊流程回退
        if(StringUtils.isNotEmpty(rollBackTemplateBid) && CollectionUtils.isNotEmpty(mSpaceAppData)){
            try{
                String[] rollBackTemplateBids = rollBackTemplateBid.split(",");
                if(rollBackTemplateBids != null && rollBackTemplateBids.length > 4){
                    //当前需要回退的流程模板
                    String flowTemplateBid = rollBackTemplateBids[0];
                    //需要回退的节点dataBid
                    String rollBackNodeDataBid = rollBackTemplateBids[1];
                    //修改的字段
                    String propertie = rollBackTemplateBids[2];
                    //修改的值
                    String updateValue = rollBackTemplateBids[3];
                    //应用bid
                    String spaceAppBid = rollBackTemplateBids[4];
                    if(app.getBid().equals(spaceAppBid) && updateValue.equals(mSpaceAppData.get(propertie))){
                       //判断流程是否匹配
                        if(object != null && object.get(TranscendModelBaseFields.WORK_ITEM_TYPE) != null && flowTemplateBid.equals(object.get(TranscendModelBaseFields.WORK_ITEM_TYPE)+"")){
                           //判断该流程的是否已经全部完成
                           boolean isComplete = apmFlowInstanceNodeService.checkFlowIsComplete(flowTemplateBid, bid);
                           if(isComplete){
                               //回退流程
                               String instanceNodeBid = apmFlowInstanceNodeService.getInstanceNodeBid(flowTemplateBid, bid,rollBackNodeDataBid);
                               if(instanceNodeBid != null){
                                   updatePartialContentAndRollbackFlowNode(spaceAppBid, bid, instanceNodeBid, new MSpaceAppData(),false);
                               }
                           }
                        }
                    }
                }
            }catch (Exception e){
                log.error("特殊流程回退失败", e);
            }
        }
    }

    private StateDataDriveAO handleStateDrive(ApmSpaceApp app, MSpaceAppData mSpaceAppData, String bid,MObject object) {
        StateDataDriveAO stateDataDriveAO = new StateDataDriveAO();
        boolean isStartWorkFlow = true;
        stateDataDriveAO.setInstanceBid(bid);
        stateDataDriveAO.setModelCode(app.getModelCode());
        //生命周期变更需要判断是否去向上驱动
        if (mSpaceAppData.containsKey(ObjectEnum.LIFE_CYCLE_CODE.getCode())) {
            stateDataDriveAO.setLifeCycleCode(mSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode()).toString());
            if (object.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) == null || StringUtils.isEmpty(object.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) + "")) {
                //如果是空的，说明是非工作流程，不需要启动流程
                isStartWorkFlow = false;
                //判断是否需要去状态向上驱动
                ApmFlowTemplate apmFlowTemplate = null;
                List<ApmFlowTemplate> apmFlowTemplateList = apmFlowTemplateService.list(Wrappers.<ApmFlowTemplate>lambdaQuery().eq(ApmFlowTemplate::getSpaceAppBid, app.getBid()).eq(ApmFlowTemplate::getType, "state"));
                if(CollectionUtils.isNotEmpty(apmFlowTemplateList)){
                    apmFlowTemplate = apmFlowTemplateList.get(0);
                }
                if (apmFlowTemplate != null) {
                    //查询连线
                    //组装修改字段事件数据
                    ApmFlowNodeLine apmFlowNodeLine = apmFlowNodeLineService.getOne(Wrappers.<ApmFlowNodeLine>lambdaQuery().eq(ApmFlowNodeLine::getTemplateBid, apmFlowTemplate.getBid()).eq(ApmFlowNodeLine::getVersion, apmFlowTemplate.getVersion()).eq(ApmFlowNodeLine::getSourceNodeCode, object.getLifeCycleCode()).eq(ApmFlowNodeLine::getTargetNodeCode, mSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode()).toString()));
                    if (apmFlowNodeLine != null) {
                        //查询事件
                        List<ApmFlowLineEvent> apmFlowLineEvents = apmFlowLineEventService.list(Wrappers.<ApmFlowLineEvent>lambdaQuery().eq(ApmFlowLineEvent::getFlowTemplateBid, apmFlowTemplate.getBid()).eq(ApmFlowLineEvent::getVersion, apmFlowTemplate.getVersion()).eq(ApmFlowLineEvent::getLineBid, apmFlowNodeLine.getBid()));
                        if (CollectionUtils.isNotEmpty(apmFlowLineEvents)) {
                            for (ApmFlowLineEvent apmFlowLineEvent : apmFlowLineEvents) {
                                if (FlowEventTypeConstant.MODIFY_FIELD.equals(apmFlowLineEvent.getEventType())) {
                                    if (!mSpaceAppData.containsKey(apmFlowLineEvent.getFiledName())) {
                                        if(StringUtils.isNotEmpty(apmFlowLineEvent.getFiledValueType())){
                                            if("now".equals(apmFlowLineEvent.getFiledValueType())){
                                                //当前时间
                                                mSpaceAppData.put(apmFlowLineEvent.getFiledName(), new Date());
                                            }else if ("loginUser".equals(apmFlowLineEvent.getFiledValueType())){
                                                mSpaceAppData.put(apmFlowLineEvent.getFiledName(), SsoHelper.getJobNumber());
                                            }
                                        }else{
                                            mSpaceAppData.put(apmFlowLineEvent.getFiledName(), apmFlowLineEvent.getFiledValue());
                                        }
                                    }
                                } else if (FlowEventTypeConstant.DRIVE_RELATE.equals(apmFlowLineEvent.getEventType())) {
                                    stateDataDriveAO.setApmFlowLineEvent(apmFlowLineEvent);
                                }
                            }
                        }
                    }
                }
            }
        }
        stateDataDriveAO.setStartWorkFlow(isStartWorkFlow);
        return stateDataDriveAO;
    }

    @Override
    public List<StateDataDriveDto> updateDriveState(StateDataDriveAO stateDataDriveAO, ApmFlowDriveRelate apmFlowDriveRelate) {
        //查询关系表
        List<StateDataDriveDto> stateDataDriveDtos = new ArrayList<>();
        List<MObject> mObjects = objectModelCrudI.listSourceMObjects(apmFlowDriveRelate.getRelationModelCode(), apmFlowDriveRelate.getSourceModelCode(), Collections.singletonList(stateDataDriveAO.getInstanceBid()));
        if (CollectionUtils.isNotEmpty(mObjects)) {
            for (MObject mObject : mObjects) {
                //查询关系目标数据
                List<MObject> targetList = objectModelCrudI.listNoVersionRelationMObjects(apmFlowDriveRelate.getRelationModelCode(), stateDataDriveAO.getModelCode(), mObject, mObject.getBid());
                if (checkAllState(targetList, stateDataDriveAO.getLifeCycleCode())) {
                    //修改当前实例状态
                    StateDataDriveDto stateDataDriveDto = new StateDataDriveDto();
                    stateDataDriveDto.setSpaceAppBid(mObject.get(SpaceAppDataEnum.SPACE_APP_BID.getCode()).toString());
                    stateDataDriveDto.setBid(mObject.getBid());
                    MSpaceAppData mSpaceAppData = new MSpaceAppData();
                    mSpaceAppData.setLifeCycleCode(apmFlowDriveRelate.getSourceLifeCycleCode());
                    stateDataDriveDto.setMSpaceAppData(mSpaceAppData);
                    stateDataDriveDtos.add(stateDataDriveDto);
                }
            }
        }
        return stateDataDriveDtos;
    }

    private boolean checkAllState(List<MObject> targetList, String lifeCycleCode) {
        for (MObject mObject : targetList) {
            if (!lifeCycleCode.equals(mObject.getLifeCycleCode())) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePartialContent(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData) {
        IApmSpaceAppDataDrivenService proxy = (IApmSpaceAppDataDrivenService) AopContext.currentProxy();
        //事件方法修改的属性不需要记录日志
        boolean isRecordLog = mSpaceAppData.get("isRecordLog") == null || BooleanUtil.toBoolean(mSpaceAppData.get("isRecordLog").toString());
        mSpaceAppData.remove("isRecordLog");
        return proxy.updatePartialContent(spaceAppBid, bid, mSpaceAppData, isRecordLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePartialContent(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData, Boolean isRecordLog) {
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        MObject oldSpaceAppData = objectModelCrudI.getByBid(app.getModelCode(), bid);
        if(CollectionUtils.isEmpty(mSpaceAppData) || oldSpaceAppData == null){
            return true;
        }
        StateDataDriveAO stateDataDriveAO = handleStateDrive(app, mSpaceAppData, bid,oldSpaceAppData);
        //查找视图
        Object viewBid = mSpaceAppData.get(_VIEW_BID);
        mSpaceAppData.remove(_VIEW_BID);
        CfgViewVo cfgViewVo = Optional.ofNullable(viewBid)
                .filter(ObjectUtil::isNotEmpty)
                .map(v -> cfgViewFeignClient.getByBid(v.toString()).getCheckExceptionData())
                .orElseGet(() -> iAmSpaceAppConfigManageService.baseViewGet(spaceAppBid));
        List<CfgViewMetaDto> cfgViewMetaList = Optional.ofNullable(cfgViewVo)
                .map(CfgViewVo::getMetaList)
                .orElse(Collections.emptyList());
        String spaceBid = app.getSpaceBid();
        Map<String,String> cfgViewMetaTypeMap = cfgViewMetaList.stream().collect(Collectors.toMap(CfgViewMetaDto::getName,CfgViewMetaDto::getType,(k1,k2)->k1));

        //处理特殊字段(时间默认为系统当前时间)
        for(Map.Entry<String,Object> entry:mSpaceAppData.entrySet()){
            String filedType = cfgViewMetaTypeMap.get(entry.getKey());
            if(StringUtils.isNotEmpty(filedType) && filedType.equals(ViewComponentEnum.DATE.getType()) && "1970-01-01".equals(entry.getValue()+"")){
                mSpaceAppData.put(entry.getKey(), new Date());
            }
        }
        // 如果当前状态有修改则重置到达时间
        if (ObjectUtil.isNotEmpty(oldSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode()))
                && mSpaceAppData.containsKey(ObjectEnum.LIFE_CYCLE_CODE.getCode())
                && !ObjectUtil.equal(oldSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode()), mSpaceAppData.containsKey(ObjectEnum.LIFE_CYCLE_CODE.getCode()))){
            mSpaceAppData.put(ObjectEnum.REACH_TIME.getCode(), new Date());
        }
        mSpaceAppData.put(RelationEnum.BID.getCode(), bid);
        mSpaceAppData.setSpaceBid(spaceBid);
        //String keyName = (String) mSpaceAppData.keySet().toArray()[0];
        String createdBy = SsoHelper.getJobNumber();
        Map<String,Object> forMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        forMap.putAll(mSpaceAppData);
        forMap.remove(PermissionCheckEnum.CHECK_PERMISSION.getCode());
        for(Map.Entry<String,Object> entry:forMap.entrySet()){
            String keyName = entry.getKey();
            CfgViewMetaDto cfgViewMetaDto = cfgViewMetaList.stream().filter(cfgViewMeta -> cfgViewMeta.getName().equals(keyName)).findFirst().orElse(CfgViewMetaDto.builder().build());
            assert cfgViewVo != null;
            Map<String, Object> properties = apmSpaceAppConfigDrivenService.getProperties(cfgViewVo.getContent(), keyName);
            OperationLogAddParam operationLogAddParam = OperationLogAddParam.builder().modelCode(app.getModelCode()).cfgViewMetaDto(cfgViewMetaDto).properties(properties).instanceBid(bid)
                    .fieldName(keyName).fieldValue(mSpaceAppData.get(keyName)).isAppView(true).spaceAppBid(spaceAppBid).spaceBid(spaceBid).build();
            try {
                if (Boolean.TRUE.equals(isRecordLog)) {
                    operationLogAddParam.setCreatedBy(createdBy);
                    //日志异步提交
                    CompletableFuture.runAsync(() -> operationLogEsService.save(operationLogAddParam, oldSpaceAppData), SimpleThreadPool.getInstance());
                }
            } catch (Exception e) {
                log.error("更新数据Bid[" + bid + "],记录日志失败", e);
            }
            if (ViewComponentEnum.RELATION_CONSTANT.equals(cfgViewMetaDto.getType())) {
                List<CfgViewMetaDto> relationModuleList = queryRelationMetaList(spaceAppBid, keyName, null);
                // 扩展的方法
                extendRelationAddEventMethod(mSpaceAppData, app.getModelCode(), relationModuleList);
                checkAddOrUpdateRelationModule(app, relationModuleList, mSpaceAppData, Boolean.TRUE);
            }
        }
        // 处理特殊空间组件应用
        checkAddOrUpdateSpaceModule(app, mSpaceAppData);
        //特殊流程角色人员处理
        runtimeService.setSpecialApmRoleUsers(spaceAppBid, bid, mSpaceAppData);
        // 特性的流程 产品责任人映射流程节点产品经理角色
        runtimeService.productOwnerReflectionPdmRoleUser(app, bid, mSpaceAppData);
        mSpaceAppData.put(TranscendModelBaseFields.SPACE_APP_BID, spaceAppBid);
        mSpaceAppData.put(ObjectEnum.PERMISSION_BID.getCode(), oldSpaceAppData.getPermissionBid());
        Boolean updateFlag = appDataService.updateByBid(app.getModelCode(), bid, mSpaceAppData);
        Boolean updateProcessFlag = true;
        if (stateDataDriveAO.isStartWorkFlow()) {
            updateProcessFlag = runtimeService.startProcess(app.getSpaceBid(), spaceAppBid, mSpaceAppData, Boolean.TRUE);
        }
        if (stateDataDriveAO.getApmFlowLineEvent() != null) {
            //发起状态驱动
            NotifyEventBus.EVENT_BUS.post(stateDataDriveAO);
        }
        //责任人变更生成待办事件
        if(ObjectUtil.isEmpty(oldSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode())) &&
                (ObjectUtil.isNotEmpty(mSpaceAppData.get(ObjectEnum.HANDLER.getCode())) || ObjectUtil.isNotEmpty(mSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode())))){
            StateFlowTodoDriveAO stateFlowTodoDriveAO =  buildStateFlowTodoDriveAO(app, mSpaceAppData, oldSpaceAppData, bid);
            NotifyEventBus.EVENT_BUS.post(stateFlowTodoDriveAO);
        }
        //发送字段变更通知
        for(Map.Entry<String,Object> entry:mSpaceAppData.entrySet()){
            String keyName = entry.getKey();
            FieldUpdateEvent filedUpdateEvent = FieldUpdateEvent.builder().instanceBid(bid).spaceAppBid(spaceAppBid).spaceBid(spaceBid).fieldName(keyName).fieldValue(mSpaceAppData.get(keyName)).build();
            NotifyEventBus.EVENT_BUS.post(filedUpdateEvent);
        }
        //特殊流程回退校验
        specialRollBackFlowTemp(app, mSpaceAppData, bid,oldSpaceAppData);
        if((updateFlag && updateProcessFlag)){
            //发起更新异步事件
            NotifyEventBus.EVENT_BUS.post(NotifyObjectPartialContentDto.of().setModelCode(app.getModelCode()).setBid(bid).setMSpaceAppData(mSpaceAppData));
        }
        return updateFlag && updateProcessFlag;
    }

    private StateFlowTodoDriveAO buildStateFlowTodoDriveAO(ApmSpaceApp app, MSpaceAppData mSpaceAppData, MObject oldSpaceAppData, String bid) {
        StateFlowTodoDriveAO stateFlowTodoDriveAO = new StateFlowTodoDriveAO();
        stateFlowTodoDriveAO.setInstanceBid(bid);
        stateFlowTodoDriveAO.setSpaceAppBid(app.getBid());
        stateFlowTodoDriveAO.setSpaceBid(app.getSpaceBid());
        List<String> personResponsible = new ArrayList<>();
        Object personResponsibleObject = mSpaceAppData.get(ObjectEnum.HANDLER.getCode());
        if (ObjectUtil.isEmpty(personResponsibleObject)) {
            personResponsibleObject = oldSpaceAppData.get(ObjectEnum.HANDLER.getCode());
        }
        if(personResponsibleObject instanceof List){
            try{
                personResponsible = JSON.parseArray(JSON.toJSONString(personResponsibleObject), String.class);
            }catch (Exception e){
                log.error("checkPermission error",e);
            }
        }else if(personResponsibleObject instanceof String){
            personResponsible = Lists.newArrayList(personResponsibleObject.toString());
        }
        stateFlowTodoDriveAO.setPersonResponsible(new HashSet<>(personResponsible));

        Object lifeCycleCode = mSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode());
        if (ObjectUtil.isEmpty(lifeCycleCode)) {
            lifeCycleCode = oldSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode());
        }
        if (ObjectUtil.isNotEmpty(stateFlowTodoDriveAO)) {
            stateFlowTodoDriveAO.setLifeCycleCode(lifeCycleCode.toString());
        }
        List<ApmFlowNodeLine> apmFlowNodeLineList = apmFlowNodeLineService.listNodeLinesByTempBidAndVersion(oldSpaceAppData.getLcTemplBid(),oldSpaceAppData.getLcTemplVersion());
        Object finalLifeCycleCode = lifeCycleCode;
        stateFlowTodoDriveAO.setIsLastState(CollectionUtils.isEmpty(apmFlowNodeLineList) || apmFlowNodeLineList.stream().noneMatch(v->v.getSourceNodeCode().equals(finalLifeCycleCode)));
        return stateFlowTodoDriveAO;
    }

    /**
     * 扩展的方法， 目前只有关系组件需要扩展，并且规则只有 产品需求的 多对象（多种对象都能在第一层）的特殊初始化需求
     *
     * @param mSpaceAppData
     * @param modelCode
     * @param relationModuleList
     */
    private static void extendRelationAddEventMethod(MSpaceAppData mSpaceAppData, String modelCode, List<CfgViewMetaDto> relationModuleList) {
        boolean multiTreeHeadFlag = (DEMAND_SPECIFIC_MODEL_CODE.equals(modelCode) || DEMAND_STORY_MODEL_CODE.equals(modelCode)) && CollectionUtils.isNotEmpty(relationModuleList);
        if (multiTreeHeadFlag) {
            relationModuleList.forEach(relationModule -> {
                if (Objects.isNull(mSpaceAppData.get(relationModule.getName())) || StringUtils.isEmpty(mSpaceAppData.get(relationModule.getName()).toString())) {
                    mSpaceAppData.put(TranscendModelBaseFields.MULTI_TREE_HEAD, "1");
                } else {
                    mSpaceAppData.put(TranscendModelBaseFields.MULTI_TREE_HEAD, "0");
                }
            });
        }
    }

    @Override
    public GroupListResult<MSpaceAppData> groupList(String spaceAppBid, String groupProperty, QueryCondition queryCondition) {
        // 通过空间bid与空间应用bid，获取对象类型信息
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        Map<String, List<MObject>> groupList = objectModelCrudI.groupList(modelCode, groupProperty, queryCondition.setQueries(buildSpaceAppBid(spaceAppBid, queryCondition.getQueries())));
        if (groupList.isEmpty()) {
            return new GroupListResult<>();
        }
        // 转换类型
        Map<String, List<MSpaceAppData>> groupListCover = groupList.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            // 转换类型
            return entry.getValue().stream().map(mObject -> {
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.putAll(mObject);
                filterBlobAttr.forEach(mSpaceAppData::remove);
                return mSpaceAppData;
            }).collect(Collectors.toList());
        }));
        GroupListResult<MSpaceAppData> groupListResult = new GroupListResult<>();
        groupListResult.putAll(groupListCover);
        return groupListResult;
    }

    @Override
    public List<MSpaceAppData> sampleTree(String spaceAppBid, List<QueryWrapper> wrappers) {
        // 通过空间bid与空间应用bid，获取对象类型信息
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        buildSpaceAppBid(spaceAppBid, wrappers);
        List<MObjectTree> treeList = objectModelCrudI.tree(modelCode, wrappers);
        return treeList.stream().map(mObject -> {
            // 转换类型
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            mSpaceAppData.putAll(mObject);
            filterBlobAttr.forEach(mSpaceAppData::remove);
            return mSpaceAppData;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MObjectTree> signObjectAndMultiSpaceTreeList(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean filterRichText) {
        if (CollectionUtils.isEmpty(modelMixQo.getQueries())) {
            modelMixQo.setQueries(new ArrayList<>());
        }
        CopyOnWriteArrayList<ModelFilterQo> newQueryWrapper = new CopyOnWriteArrayList<>(modelMixQo.getQueries());
        for (ModelFilterQo modelFilterQo : newQueryWrapper) {
            if (RelationEnum.SPACE_BID.getColumn().equals(modelFilterQo.getProperty()) ||
                    SpaceAppDataEnum.SPACE_APP_BID.getCode().equals(modelFilterQo.getProperty())) {
                newQueryWrapper.remove(modelFilterQo);
            }
        }
        List<QueryWrapper> wrappers = QueryConveterTool.convert(newQueryWrapper, modelMixQo.getAnyMatch());
        wrappers.add(new QueryWrapper(Boolean.TRUE).setSqlRelation(" and "));
        QueryWrapper spaceBidQueryWrapper = new QueryWrapper();
        spaceBidQueryWrapper.eq(SPACE_BID.getCode(), spaceBid).or().eq(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode(), spaceBid);
        wrappers.addAll(QueryWrapper.buildSqlQo(spaceBidQueryWrapper));
        QueryCondition queryCondition = QueryCondition.of().setQueries(wrappers).setOrders(modelMixQo.getOrders());
        // 查询匹配数据的父级和子级
        List<MObject> list = appDataService.signObjectRecursionList(spaceBid, spaceAppBid, queryCondition);
        List<MObjectTree> treeList = list.stream().map(e -> {
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(e);
            if (filterRichText) {
                filterBlobAttr.forEach(mObjectTree::remove);
            }
            return mObjectTree;
        }).collect(Collectors.toList());
        return treeList;
    }

    @Override
    public List<MSpaceAppData> signObjectAndMultiSpaceTree(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean filterRichText) {
        if(CollectionUtils.isEmpty(modelMixQo.getQueries())){
            modelMixQo.setQueries(new ArrayList<>());
        }
        CopyOnWriteArrayList<ModelFilterQo> newQueryWrapper = new CopyOnWriteArrayList<>(modelMixQo.getQueries());
        for (ModelFilterQo modelFilterQo : newQueryWrapper) {
            if (RelationEnum.SPACE_BID.getColumn().equals(modelFilterQo.getProperty()) ||
                    SpaceAppDataEnum.SPACE_APP_BID.getCode().equals(modelFilterQo.getProperty())) {
                newQueryWrapper.remove(modelFilterQo);
            }
        }
        List<QueryWrapper> wrappers = QueryConveterTool.convert(newQueryWrapper, modelMixQo.getAnyMatch());
        wrappers.add(new QueryWrapper(Boolean.TRUE).setSqlRelation(" and "));
        QueryWrapper spaceBidQueryWrapper = new QueryWrapper();
        spaceBidQueryWrapper.eq(SPACE_BID.getCode(), spaceBid).or().eq(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode(), spaceBid);
        wrappers.addAll(QueryWrapper.buildSqlQo(spaceBidQueryWrapper));
        QueryCondition queryCondition = QueryCondition.of().setQueries(wrappers).setOrders(modelMixQo.getOrders());
        // 查询匹配数据的父级和子级
        List<MObject> list = appDataService.list(spaceBid, spaceAppBid, queryCondition);
        // 空直接退出
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        List<MSpaceAppData> metaList = list.stream().map(mObject -> {
            // 转换类型
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            mSpaceAppData.putAll(mObject);
            return mSpaceAppData;
        }).collect(Collectors.toList());

        getSpaceModuleList(spaceAppBid, metaList);
        // 没有排序需要初始化
        if (CollectionUtils.isEmpty(modelMixQo.getOrders())) {
            modelMixQo.setOrders(new ArrayList<>());
        }
        // 内存排序
        metaList = ObjectTreeTools.sortObjectList(modelMixQo.getOrders(), metaList);


        List<MObjectTree> treeList = metaList.stream().map(e -> {
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(e);
            if (filterRichText) {
                filterBlobAttr.forEach(mObjectTree::remove);
            }
            return mObjectTree;
        }).collect(Collectors.toList());

        treeList = baseObjectTreeService.convert2Tree(treeList);
        return treeList.stream().map(mObject -> {
            // 转换类型
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            mSpaceAppData.putAll(mObject);
            return mSpaceAppData;
        }).collect(Collectors.toList());

    }

    /**
     * @Description 构建空间应用bid，保证数据隔离
     * @Author jinpeng.bai
     * @Date 2023/10/9 18:08
     * @Param [spaceAppBid, wrappers]
     * @Return void
     * @Since version 1.0
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
    String almBaseSpaceBid = SpaceConstant.ALM_BASE_SPACE_BID;
    /**
     * 构建spaceBid查询条件
     *
     * @param spaceBid spaceBid
     * @param wrappers wrappers
     * @return
     * @version: 1.0
     * @date: 2023/10/10 18:03
     * @author: bin.yin
     */
    private List<QueryWrapper> buildSpaceBid(String spaceBid, List<QueryWrapper> wrappers, boolean isRelation) {
        if (CollectionUtils.isEmpty(wrappers)) {
            wrappers = Lists.newArrayList();
        }
        if (isRelation){
            wrappers.add(new QueryWrapper(Boolean.TRUE).setSqlRelation(" and "));
        }

        QueryWrapper spaceBidQueryWrapper = new QueryWrapper();
        if (almBaseSpaceBid.equals(spaceBid)) {
            //特殊逻辑，ALMv2 BASE空间查询不带空间条件
            spaceBidQueryWrapper.eq(BaseDataEnum.DELETE_FLAG.getColumn(), false);
        } else if (isRelation) {
            spaceBidQueryWrapper.eq(SPACE_BID.getCode(), spaceBid);
        } else {
            spaceBidQueryWrapper.eq(SPACE_BID.getCode(), spaceBid).or().eq(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode(), spaceBid);
        }
        wrappers.addAll(QueryWrapper.buildSqlQo(spaceBidQueryWrapper));
        return wrappers;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveTreeNode(String spaceAppBid, List<MoveTreeNodeParam> moveTreeNodeParams) {
        // 通过空间bid与空间应用bid，获取对象类型信息
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        List<MObjectTree> treeList = moveTreeNodeParams.stream().map(moveTreeNodeParam -> {
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(moveTreeNodeParam);
            return mObjectTree;
        }).collect(Collectors.toList());
        return objectModelCrudI.moveTreeNode(modelCode, treeList);
    }

    @Override
    public Boolean moveTreeNodeByModelCode(String spaceBid, String modelCode, List<MoveTreeNodeParam> moveTreeNodeParams) {
        // 通过空间bid与空间应用bid，获取对象类型信息
        List<MObjectTree> treeList = moveTreeNodeParams.stream().map(moveTreeNodeParam -> {
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(moveTreeNodeParam);
            return mObjectTree;
        }).collect(Collectors.toList());
        return objectModelCrudI.moveTreeNode(modelCode, treeList);
    }

    public List<String> getAllModelCodes(String spaceAppBid) {
        List<String> modelCodes = new ArrayList<>();
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        if (app == null) {
            return modelCodes;
        }
        List<CfgObjectVo> cfgObjectVos = cfgObjectFeignClient.listAllByModelCode(app.getModelCode()).getCheckExceptionData();
        if (CollectionUtils.isNotEmpty(cfgObjectVos)) {
            modelCodes = cfgObjectVos.stream().map(e -> e.getModelCode()).collect(Collectors.toList());
        }
        return modelCodes;
    }


    private void setMobjectLifeCycleState(MObject mObject,Map<String,ApmLifeCycleStateVO> lifeCycleStateMap){
        ApmLifeCycleStateVO apmLifeCycleStateVO = lifeCycleStateMap.get(mObject.get(TranscendModelBaseFields.SPACE_APP_BID)+"");
        if(apmLifeCycleStateVO != null){
            mObject.setLifeCycleCode(apmLifeCycleStateVO.getInitState());
            mObject.setLcTemplBid(apmLifeCycleStateVO.getLcTemplBid());
            mObject.setLcTemplVersion(apmLifeCycleStateVO.getLcTemplVersion());
        }else{
            apmLifeCycleStateVO = apmSpaceAppConfigManageService.getLifeCycleState(mObject.get(TranscendModelBaseFields.SPACE_APP_BID)+"");
            if(apmLifeCycleStateVO != null){
                lifeCycleStateMap.put(mObject.get(TranscendModelBaseFields.SPACE_APP_BID)+"",apmLifeCycleStateVO);
                mObject.setLifeCycleCode(apmLifeCycleStateVO.getInitState());
                mObject.setLcTemplBid(apmLifeCycleStateVO.getLcTemplBid());
                mObject.setLcTemplVersion(apmLifeCycleStateVO.getLcTemplVersion());
            }
        }
    }
    /**
     * 多对象复制
     * @param mObjectTree
     * @param copyInstanceBid
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyMultiTree(MObjectTree mObjectTree,String copyInstanceBid,String spaceBid,String spaceAppBid){
        //查源实例数据
        MObject mObject = objectModelCrudI.getByBid(mObjectTree.getModelCode(),mObjectTree.getBid());
        if(mObject == null){
            throw new BusinessException("源实例数据不存在");
        }
        Map<String,String> demandBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        String bid = SnowflakeIdWorker.nextIdStr();
        demandBidMap.put(mObject.getBid(),bid);
        mObject.setBid(bid);
        mObject.put(TranscendModelBaseFields.DATA_BID,bid);
        mObject.put("id",null);
        List<MObject> newDemandList = new ArrayList<>();
        List<MObject> newTaskList = new ArrayList<>();
        List<MObject> newDemandRelList = new ArrayList<>();
        List<MObject> newTaskRelList = new ArrayList<>();
        //开发任务modelCode
        String taskModelCode = TASK_MODEL_CODE;
        //产品需求
        String productDemandModelCode = DEMAND_MODEL_CODE;
        //产品需求和产品需求关系表
        String productDemandAndProductDemandModelCode = "A29";
        //产品需求和任务关系表
        String productDemandAndTaskModelCode = STORY_TASK_REL_MODEL_CODE;
        MObject relMObject = new MObject();
        relMObject.setBid(SnowflakeIdWorker.nextIdStr());
        relMObject.put(TranscendModelBaseFields.DATA_BID,relMObject.getBid());
        relMObject.put("draft",0);
        relMObject.put("sourceBid",copyInstanceBid);
        relMObject.put("targetBid",mObject.getBid());
        relMObject.put("sourceDataBid",copyInstanceBid);
        relMObject.put("targetDataBid",mObject.get(TranscendModelBaseFields.DATA_BID));
        relMObject.put(TranscendModelBaseFields.TENANT_ID,mObject.get(TranscendModelBaseFields.TENANT_ID));
        relMObject.put(TranscendModelBaseFields.SPACE_BID,spaceBid);
        relMObject.put(TranscendModelBaseFields.SPACE_APP_BID,spaceAppBid);
        relMObject.setDeleteFlag(false);
        Map<String,ApmLifeCycleStateVO> lifeCycleStateMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        setMobjectLifeCycleState(mObject,lifeCycleStateMap);
        List<MObjectTree> tableList = new ArrayList<>();
        String nullStr = "null";
        if(taskModelCode.equals(mObjectTree.getModelCode())){
            newTaskList.add(mObject);
            if(!StringUtils.isEmpty(copyInstanceBid) || !nullStr.equals(copyInstanceBid)){
                relMObject.setModelCode(productDemandAndTaskModelCode);
                newTaskRelList.add(relMObject);
            }
        }else{
            newDemandList.add(mObject);
            if(!StringUtils.isEmpty(copyInstanceBid) || !nullStr.equals(copyInstanceBid)){
                relMObject.setModelCode(productDemandAndProductDemandModelCode);
                newDemandRelList.add(relMObject);
            }
        }
        //将树转平级
        treeToTable(mObjectTree.getChildren(),tableList);
        if(CollectionUtils.isNotEmpty(tableList)){
           List<String> demandBids = tableList.stream().filter(e->e.getModelCode().startsWith(productDemandModelCode)).map(e->e.getBid()).collect(Collectors.toList());
           List<String> taskBids = tableList.stream().filter(e->e.getModelCode().equals(taskModelCode)).map(e->e.getBid()).collect(Collectors.toList());
           //查询产品需求和产品需求关系数据
           if(CollectionUtils.isNotEmpty(demandBids)){
               List<String> sourceBids = new ArrayList<>();
               sourceBids.add(mObjectTree.getBid());
               sourceBids.addAll(demandBids);
               //查询产品需求实例
               List<MObject> demandMObjects = objectModelCrudI.listByBids(demandBids,productDemandModelCode);
               if(CollectionUtils.isNotEmpty(demandMObjects)){
                   for(MObject mObject1: demandMObjects){
                       String bid1 = SnowflakeIdWorker.nextIdStr();
                       demandBidMap.put(mObject1.getBid(),bid1);
                       mObject1.setBid(bid1);
                       mObject1.put(TranscendModelBaseFields.DATA_BID,bid1);
                       mObject1.put(TranscendModelBaseFields.WORK_ITEM_TYPE,null);
                       mObject1.put("id",null);
                       setMobjectLifeCycleState(mObject1,lifeCycleStateMap);
                   }
                   newDemandList.addAll(demandMObjects);
                   RelationMObject relationMObjectDemand = RelationMObject.builder().relationModelCode(productDemandAndProductDemandModelCode).sourceBids(sourceBids).targetBids(demandBids).build();
                   List<MObject> copyRelations = objectModelCrudI.listOnlyRelationMObjects(relationMObjectDemand);
                   for(MObject mObject1: copyRelations){
                       String bid1 = SnowflakeIdWorker.nextIdStr();
                       mObject1.setBid(bid1);
                       mObject1.put(TranscendModelBaseFields.DATA_BID,bid1);
                       mObject1.put("id",null);
                       mObject1.put("sourceBid",demandBidMap.get(mObject1.get("sourceBid")+""));
                       mObject1.put("targetBid",demandBidMap.get(mObject1.get("targetBid")+""));
                       mObject1.put("sourceDataBid",mObject1.get("sourceBid"));
                       mObject1.put("targetDataBid",mObject1.get("targetBid"));
                   }
                   newDemandRelList.addAll(copyRelations);
               }
           }
           //查询产品需求和任务关系数据
           if(CollectionUtils.isNotEmpty(taskBids)){
               List<String> sourceBids = new ArrayList<>();
               sourceBids.add(mObjectTree.getBid());
               sourceBids.addAll(demandBids);
               RelationMObject relationMObjectTask = RelationMObject.builder().relationModelCode(productDemandAndTaskModelCode).sourceBids(sourceBids).targetBids(taskBids).build();
               List<MObject> copyRelations = objectModelCrudI.listOnlyRelationMObjects(relationMObjectTask);
               //查询任务实例
               List<MObject> taskMObjects = objectModelCrudI.listByBids(taskBids,taskModelCode);
               if(CollectionUtils.isNotEmpty(taskMObjects)){
                   Map<String,String> taskBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
                   for(MObject mObject1: taskMObjects){
                       String bid1 = SnowflakeIdWorker.nextIdStr();
                       taskBidMap.put(mObject1.getBid(),bid1);
                       mObject1.setBid(bid1);
                       mObject1.put(TranscendModelBaseFields.WORK_ITEM_TYPE,null);
                       mObject1.put(TranscendModelBaseFields.DATA_BID,bid1);
                       mObject1.put("id",null);
                       setMobjectLifeCycleState(mObject1,lifeCycleStateMap);
                   }
                   newTaskList.addAll(taskMObjects);
                   for (MObject mObject1: copyRelations){
                       String bid1 = SnowflakeIdWorker.nextIdStr();
                       mObject1.setBid(bid1);
                       mObject1.put(TranscendModelBaseFields.DATA_BID,bid1);
                       mObject1.put("id",null);
                       mObject1.put("sourceBid",demandBidMap.get(mObject1.get("sourceBid")+""));
                       mObject1.put("targetBid",taskBidMap.get(mObject1.get("targetBid")+""));
                       mObject1.put("sourceDataBid",mObject1.get("sourceBid"));
                       mObject1.put("targetDataBid",mObject1.get("targetBid"));
                   }
                   newTaskRelList.addAll(copyRelations);
               }
           }
        }
        //保存数据
        if(CollectionUtils.isNotEmpty(newDemandList)){
            appDataService.addBatch(productDemandModelCode,newDemandList);
        }
        if(CollectionUtils.isNotEmpty(newTaskList)){
            appDataService.addBatch(taskModelCode,newTaskList);
        }
        if(CollectionUtils.isNotEmpty(newDemandRelList)){
            appDataService.addBatch(productDemandAndProductDemandModelCode,newDemandRelList);
        }
        if(CollectionUtils.isNotEmpty(newTaskRelList)){
            appDataService.addBatch(productDemandAndTaskModelCode,newTaskRelList);
        }
        return true;
    }

    private void treeToTable(List<MObjectTree> treeList,List<MObjectTree> tableList){
       if(CollectionUtils.isNotEmpty(treeList)){
           List<MObjectTree> childList = JSON.parseArray(JSON.toJSONString(treeList),MObjectTree.class);
           for(MObjectTree tree:childList){
               tableList.add(tree);
               treeToTable(tree.getChildren(),tableList);
           }
       }
    }

    @Override
    public List<MObjectTree> multiTree(String spaceBid, String spaceAppBid, ApmMultiTreeModelMixQo modelMixQo,
                                       boolean filterRichText, Boolean checkPermission){
        return multiTreeQueryService.multiTree(spaceBid, spaceAppBid, modelMixQo, checkPermission);
    }


    @Override
    public GroupListResult<MSpaceAppData> multiTreeGroupBy(String spaceBid, String spaceAppBid, String groupProperty, List<QueryWrapper> wrappers, List<Order> orders) {
        MultiTreeConfigVo multiTreeConfigVo;
        // 能作为根节点的应用modelCode
        // 是否是多对象视图模式
        // 通过spaceAppBid 和 code = multiTree 获取多对象配置信息AppViewModelEnum.MULTI_TREE.getCode()
        LambdaQueryWrapper<ApmSpaceAppViewModelPo> viewModelQueryWrapper = Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery().eq(ApmSpaceAppViewModelPo::getSpaceAppBid, spaceAppBid)
                .eq(ApmSpaceAppViewModelPo::getCode, AppViewModelEnum.MULTI_TREE_VIEW.getCode());
        ApmSpaceAppViewModelPo apmSpaceAppViewModelPo = apmSpaceAppViewModelService.getOne(viewModelQueryWrapper);
        if (Objects.isNull(apmSpaceAppViewModelPo) || CollectionUtils.isEmpty(apmSpaceAppViewModelPo.getConfigContent()) || apmSpaceAppViewModelPo.getConfigContent().get("multiAppTreeConfig") == null) {
            throw new TranscendBizException("多对象树配置信息不存在");
        }
        // 通过空间bid 查询配置的对象实例
        multiTreeConfigVo = JSON.parseObject(JSON.toJSONString(apmSpaceAppViewModelPo.getConfigContent().get("multiAppTreeConfig")), MultiTreeConfigVo.class);

        TreeSet<String> modelCodes = Sets.newTreeSet();
        Set<String> rootModels = Sets.newHashSet();
        Set<String> relationModelCodes = Sets.newHashSet();
        // 递归配置
        Map<String, String> sourceWithTargetCodeMap = Maps.newHashMap();
        recursiveMultiTreeConfig(sourceWithTargetCodeMap, multiTreeConfigVo, modelCodes, relationModelCodes, null);
        //判断对象是否包含该属性
        checkObjectContainGroupAttr(modelCodes,groupProperty);
        // 通过modelCodes 找根节点对象编码(取前三位)
        for (String modelCode : modelCodes) {
            rootModels.add(ObjectCodeUtils.splitModelCode(modelCode).iterator().next());
        }
        // 所有实例数据
        List<MObject> insList = Lists.newCopyOnWriteArrayList();
        // 所有关系数据
        List<MObject> relationList = Lists.newCopyOnWriteArrayList();
        // 构建查询条件
        // 查询该空间下多对象所有关系数据
        List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();
        queryInsByModelCode(spaceBid, buildSpaceBid(spaceBid, QueryConveterTool.convert(Lists.newArrayList(), null), true), relationModelCodes, relationList, completableFutures, true);
        queryInsByModelCode(spaceBid, buildSpaceBid(spaceBid, wrappers, false), rootModels, insList, completableFutures, true);
        CompletableFuture.<Void>allOf(completableFutures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
            log.error("多对象树查询异常", ex);
            throw new TranscendBizException("多对象树查询异常");
        }).join();
        // 处理挂载空间数据
        getSpaceModuleList(spaceBid, insList);

        //特殊处理用户故事的计划开始时间和计划结束时间
        Map<String, List<String>> sourceBidAndTargetBidListMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for(MObject mObject : relationList){
            Object obj = mObject.get(RelationEnum.TARGET_BID.getCode());
            List<String> tempList = sourceBidAndTargetBidListMap.get(mObject.get(RelationEnum.SOURCE_BID.getCode())+"");
            if(tempList == null){
                tempList = new ArrayList<>();
            }
            if(obj instanceof String){
                tempList.add(obj+"");
            }else if (obj instanceof List){
                List<String> targetTempList = JSON.parseArray(obj.toString(), String.class);
                tempList.addAll(targetTempList);
            }
        }
        //列表查询特殊逻辑处理
        userDemandPlanTime(insList,sourceBidAndTargetBidListMap);

        //分组属性特殊处理 多选
        insList = attrMultiSelect(insList,groupProperty);

        Map<String, List<MObject>> groupList = insList.stream().collect(Collectors.groupingBy(mObject -> String.valueOf(mObject.get(groupProperty))));
        if (groupList.isEmpty()) {
            return new GroupListResult<>();
        }
        // 转换类型
        Map<String, List<MSpaceAppData>> groupListCover = groupList.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            // 转换类型
            return entry.getValue().stream().map(mObject -> {
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.putAll(mObject);
                filterBlobAttr.forEach(mSpaceAppData::remove);
                return mSpaceAppData;
            }).collect(Collectors.toList());
        }));
        GroupListResult<MSpaceAppData> groupListResult = new GroupListResult<>();
        groupListResult.putAll(groupListCover);
        return groupListResult;
    }

    private List<MObject> attrMultiSelect(List<MObject> insList, String groupProperty) {
        List<MObject> splitList = new ArrayList<>();
        insList.forEach(mObject -> {
            if(String.valueOf(mObject.get(groupProperty)).startsWith(CommonConstant.OPEN_BRACKET)){
                List<String> targetTempList = JSON.parseArray(String.valueOf(mObject.get(groupProperty)), String.class);
                targetTempList.forEach(targetTemp -> {
                    MObject mObjectTemp = new MObject();
                    mObjectTemp.putAll(mObject);
                    mObjectTemp.put(groupProperty, targetTemp);
                    splitList.add(mObjectTemp);
                });
            }else{
            splitList.add(mObject);
            }
        });
        return splitList;
    }

    @Value("${transcend.plm.apm.multiTree.planStartTime:planStartTime}")
    private String planStartTimeAttr;
    @Value("${transcend.plm.apm.multiTree.planEndTime:planedEndTime}")
    private String planEndTimeAttr;
    @Value("${transcend.plm.apm.task.startTime:startTime}")
    private String taskStartTimeAttr;
    @Value("${transcend.plm.apm.task.endTime:endTime}")
    private String taskEndTimeAttr;

    private void userDemandPlanTime(List<MObject> insList,Map<String,List<String>> sourceBidAndTargetBidList) {
        if(CollectionUtils.isEmpty(insList)){
            return;
        }
        Map<String,MObject> bidMap = insList.stream().collect(Collectors.toMap(MObject::getBid, Function.identity(),(k1,k2)->k1));
        insList.forEach(mObject -> {
            //用户需求特殊处理时间
          if(DEMAND_STORY_MODEL_CODE.equals(mObject.getModelCode()) && (mObject.get(planStartTimeAttr) == null
          || mObject.get(planEndTimeAttr) == null)){
              List<String> targetBidList = sourceBidAndTargetBidList.get(mObject.getBid());
              if(CollectionUtils.isNotEmpty(targetBidList)){
                 List<MObject> childList = targetBidList.stream().map(bidMap::get).collect(Collectors.toList());
                  List<MObject> minStartTimeList = childList.stream().filter(Objects::nonNull).filter(child -> child.get(taskStartTimeAttr) != null).collect(Collectors.toList());
                  List<MObject> minEndTimeList = childList.stream().filter(Objects::nonNull).filter(child -> child.get(taskEndTimeAttr) != null).collect(Collectors.toList());
                  if (mObject.get(planStartTimeAttr) == null && CollectionUtils.isNotEmpty(minStartTimeList)) {
                      Optional<MObject> minStartTime =minStartTimeList.stream().
                              min(Comparator.comparing(o -> ((LocalDateTime) o.get(taskStartTimeAttr))));
                      minStartTime.ifPresent(object -> mObject.put(planStartTimeAttr, object.get(taskStartTimeAttr)));
                  }
                  if (mObject.get(planEndTimeAttr) == null  && CollectionUtils.isNotEmpty(minEndTimeList)){
                      Optional<MObject> maxEndTime =minEndTimeList.stream().
                              max(Comparator.comparing(o -> ((LocalDateTime) o.get(taskEndTimeAttr))));
                      maxEndTime.ifPresent(object -> mObject.put(planEndTimeAttr, object.get(taskEndTimeAttr)));
                  }
              }
          }
        });
    }


    private void checkObjectContainGroupAttr(TreeSet<String> modelCodes,String groupProperty){
        if(CollectionUtils.isEmpty(modelCodes)){
            return;
        }
        modelCodes.forEach(e->{
            CfgObjectVo cfgObjectVo = cfgObjectFeignClient.getByModelCode(e).getCheckExceptionData();
            if (cfgObjectVo == null) {
                throw new TranscendBizException(String.format("模型[%s]不存在", e));
            }
            if (CollectionUtils.isEmpty(cfgObjectVo.getAttrList()) || cfgObjectVo.getAttrList().stream().noneMatch(attr -> groupProperty.equals(attr.getCode()))) {
                throw new TranscendBizException(String.format("模型[%s]中不存在[%s]分组属性", e, groupProperty));
            }
        });
    }



    /**
     * 查询对象实例数据

     * @param spaceBid
     * @param wrappers
     * @param modelCodes
     * @param relationList
     */
    private void queryInsByModelCode(String spaceBid, List<QueryWrapper> wrappers, Set<String> modelCodes, List<MObject> relationList, List<CompletableFuture<Void>> completableFutures, Boolean checkPermission) {
        List<MObject> vector = new Vector<>();
        String userNO = SsoHelper.getJobNumber();
        CountDownLatch countDownLatch = new CountDownLatch(modelCodes.size());
        for (String modelCode : modelCodes) {
            SimpleThreadPool.getInstance().execute(() -> {
                    try {
                        List<QueryWrapper> resultWrapper = Lists.newCopyOnWriteArrayList();
                        if (CollectionUtils.isNotEmpty(wrappers)) {
                            resultWrapper.addAll(wrappers);
                        }
                        ApmSpaceApp spaceApp = apmSpaceAppService.getBySpaceBidAndModelCode(spaceBid, modelCode);
                        QueryCondition queryCondition = new QueryCondition();
                        queryCondition.setQueries(resultWrapper);
                        if (spaceApp == null) {
                            List<MObject> list = objectModelCrudI.list(modelCode,queryCondition);
                            if (CollectionUtils.isNotEmpty(list)) {
                                vector.addAll(list);
                            }
                        }else{
                            if (checkPermission) {
                                List<MObject> list = appDataService.list(spaceBid, spaceApp.getBid(), userNO,queryCondition);
                                if (CollectionUtils.isNotEmpty(list)) {
                                    vector.addAll(list);
                                }
                            } else {
                                List<MObject> list = objectModelCrudI.list(spaceApp.getModelCode(),queryCondition);
                                if (CollectionUtils.isNotEmpty(list)) {
                                    vector.addAll(list);
                                }
                            }

                        }
                    } finally {
                        countDownLatch.countDown();
                    }
            });
        }
        //等待所有线程执行完毕
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        relationList.addAll(vector);
    }

    /**
     * 递归处理多对象树配置数据
     *
     * @param sourceWithTargetCodeMap 父子modelCode Map
     * @param multiTreeConfigVo       配置数据
     * @param relationModelCodes      关系对象modelCode
     * @param needModelCodes          需要查询的对象modelCodes
     */
    private void recursiveMultiTreeConfig(Map<String, String> sourceWithTargetCodeMap, MultiTreeConfigVo multiTreeConfigVo, Set<String> modelCodes,
                                          Set<String> relationModelCodes, Set<String> needModelCodes) {
        if (Objects.nonNull(multiTreeConfigVo)) {
            if (CollectionUtils.isEmpty(needModelCodes) || needModelCodes.contains(multiTreeConfigVo.getSourceModelCode())) {
                if (StringUtils.isNoneBlank(multiTreeConfigVo.getRelationModelCode())) {
                    relationModelCodes.add(multiTreeConfigVo.getRelationModelCode());
                }
                if (StringUtils.isNoneBlank(multiTreeConfigVo.getSourceModelCode())) {
                    modelCodes.add(multiTreeConfigVo.getSourceModelCode());
                    if (multiTreeConfigVo.getTargetModelCode() != null && StringUtils.isNotBlank(multiTreeConfigVo.getTargetModelCode().getSourceModelCode())) {
                        sourceWithTargetCodeMap.put(multiTreeConfigVo.getSourceModelCode(), multiTreeConfigVo.getTargetModelCode().getSourceModelCode());
                    }
                }
                recursiveMultiTreeConfig(sourceWithTargetCodeMap, multiTreeConfigVo.getTargetModelCode(), modelCodes, relationModelCodes, needModelCodes);
            }
        }
    }

    /**
     * 通过空间bid与空间应用bid，获取对象类型信息
     *
     * @param spaceAppBid 空间应用bid
     * @return
     */

    /**
     * 删除关系
     * @param dto
     * @return
     */
    @Override
    public Boolean deleteRel(String spaceAppBid,DeleteRelDto dto){
         return objectModelCrudI.deleteRel(dto.getModelCode(),dto.getSourceBid(),dto.getTargetBids());
    }

    /**
     * 批量逻辑删除
     *
     * @param spaceAppBid
     * @param bids
     * @return
     */
    @Override
    public Boolean batchLogicalDeleteByBids(String spaceAppBid, List<String> bids) {
        // 客制化处理
        customBatchDeleteExtendStrategy.preHandler(spaceAppBid, bids);
        return batchLogicalDeleteByBids(spaceAppBid, bids, true);
    }

    @Override
    public Boolean batchLogicalDeleteByBids(String spaceAppBid, List<String> bids, Boolean checkPermission){
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        //权限校验
        List<MObject> mObjects = objectModelCrudI.listByBids(bids, modelCode);
        ApmSpaceApp targetApp = apmSpaceAppService.getByBid(spaceAppBid);
        if (Boolean.TRUE.equals(checkPermission)) {
            boolean hasPermission = permissionCheckService.checkInstancesPermission(mObjects,targetApp.getSpaceBid(), OperatorEnum.DELETE.getCode());
            if(!hasPermission){
                throw new BusinessException("没有删除权限");
            }
        }
        //删除流程
        List<String> dataBids = bids.stream().map(bid -> runtimeService.getVersionInstanceBid(spaceAppBid, bid)).collect(Collectors.toList());
        boolean deleteProcessResult = runtimeService.deleteProcess(dataBids);
        //删除实例数据
        boolean deleteResult = objectModelCrudI.batchLogicalDeleteByBids(modelCode, bids);
        //逻辑删除该对象关联的关系数据
        Map<String, Set<String>> relationDeleteParams = getRelationDeleteParams(modelCode, Sets.newHashSet(bids));
        relationModelDomainService.batchLogicalDeleteBySourceBid(relationDeleteParams);
        //记录操作日志
        if (deleteResult) {
            try {
                List<GenericLogAddParam> genericLogAddParamList = bids.stream().map(e -> GenericLogAddParam.builder().logMsg("删除了[" + e + "]的实例数据")
                        .modelCode(modelCode)
                        .instanceBid(e).type(EsUtil.EsType.LOG.getType()).spaceBid(apmSpaceApplicationService.getSpaceBidBySpaceAppBid(spaceAppBid)).build()).collect(Collectors.toList());
                operationLogEsService.genericBulkSave(genericLogAddParamList);

                // 20250527保存记录空间应用的删除日志
                List<GenericLogAddParam> spaceAppGenericLogAddParamList = bids.stream().map(e -> GenericLogAddParam.builder().logMsg("删除了[" + e + "]的实例数据")
                        .modelCode(modelCode)
                        .instanceBid(spaceAppBid).type(EsUtil.EsType.LOG.getType()).spaceBid(apmSpaceApplicationService.getSpaceBidBySpaceAppBid(spaceAppBid)).build()).collect(Collectors.toList());
                operationLogEsService.genericBulkSave(spaceAppGenericLogAddParamList);
            } catch (Exception e) {
                log.error("删除了[" + JSON.toJSONString(bids) + "]的实例数据,记录日志失败", e);
            }
        }

        return deleteResult && deleteProcessResult;
    }

    @Override
    public Boolean batchLogicalDelete(String spaceAppBid, BatchLogicDelAndRemQo qo) {
        // 删除之前先查询删除的实例数据
        String modelCode = qo.getModelCode();
        ApmSpaceApp app = apmSpaceAppService.getByBid(spaceAppBid);
        if (StringUtils.isBlank(modelCode)) {
            modelCode = app.getModelCode();
        }
        List<MObject> list = objectModelCrudI.listByBids(qo.getInsBids(), modelCode);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException("数据不存在");
        }
        //由于存在跨空间数据，需要按应用分组
        Map<String, List<MObject>> spaceAppDataMap = list.stream().collect(Collectors.groupingBy(v -> v.getOrDefault(SpaceAppDataEnum.SPACE_APP_BID.getCode(), "").toString()));
        //校验删除权限
        for (Map.Entry<String, List<MObject>> entry : spaceAppDataMap.entrySet()) {
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(entry.getKey());
            if (apmSpaceApp == null) {
                throw new BusinessException("应用不存在");
            }
            boolean hasPermission = permissionCheckService.checkInstancesPermission(entry.getValue(),apmSpaceApp.getSpaceBid(), OperatorEnum.DELETE.getCode());
            if(!hasPermission){
                throw new BusinessException("没有删除权限");
            }
        }
        String insNameList = list.stream().map(MObject::getName).collect(Collectors.joining(","));
        List<GenericLogAddParam> genericLogAddParamList = Lists.newArrayList();
        try {
            // 记录实例删除日志
            MultiAppConfig multiAppConfig = apmSpaceAppConfigDrivenService.getMultiAppConfig(app.getSpaceBid(), spaceAppBid, qo.getTabId());
            MultiTreeConfigVo multiAppTreeConfig = multiAppConfig.getMultiAppTreeConfig();
            for (MObject mObject : list) {
                MultiTreeConfigVo parentConfig = findParentSourceModelCode(mObject.getModelCode(), multiAppTreeConfig);
                if (parentConfig != null) {
                    // 通过ModelCode和SpaceBid查询SpaceApp
                    ApmSpaceApp currentApp =
                            apmSpaceAppService.getApmSpaceAppBySpaceBidAndModelCode(String.valueOf(mObject.get(SPACE_BID.getCode())), mObject.getModelCode());
                    ViewQueryParam param = ViewQueryParam.of()
                            .setViewBelongBid(spaceAppBid);
                    List<CfgViewMetaDto> metaDtoList = cfgViewFeignClient.listMetaModels(param).getCheckExceptionData();
                    CfgViewMetaDto relationField = metaDtoList.stream()
                            .filter(dto -> "relation".equals(dto.getType()) && dto.getSourceModelCode().equals(parentConfig.getSourceModelCode()))
                            .findAny()
                            .orElseThrow(() -> new PlmBizException("未找到对应的视图属性字段！"));
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq(TARGET_BID.getCode(), mObject.getBid());
                    // 通过目标的bid查询关系实例
                    List<MObject> mObjectList = objectModelCrudI.listByQueryWrapper(parentConfig.getRelationModelCode(), QueryWrapper.buildSqlQo(queryWrapper));
                    mObjectList.stream()
                            .filter(obj -> {
                                // 数据库字段取不到就到ext里面取 （这里是为了兜底，严格上来讲这是操作失误）
                                String sourceBidInIns = "";
                                Object bid = mObject.get(relationField.getName());
                                if (bid != null) {
                                    sourceBidInIns = String.valueOf(bid);
                                } else {
                                    JSONObject ext = JSON.parseObject(JSON.toJSONString(mObject.get(TranscendModelBaseFields.EXT)));
                                    sourceBidInIns = ext.getString(relationField.getName());
                                }
                                return Objects.equals(obj.get(SOURCE_BID.getCode()), sourceBidInIns);
                            })
                            .findAny()
                            .ifPresent(obj -> {
                                // 如果存在关系实例，则记录日志
                                String sourceModelCode = parentConfig.getSourceModelCode();
                                GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                                        .logMsg("删除了[" + mObject.getName() + "]的实例数据")
                                        .modelCode(sourceModelCode)
                                        .instanceBid(String.valueOf(obj.get(SOURCE_BID.getCode())))
                                        .type(EsUtil.EsType.LOG.getType())
                                        .spaceBid(app.getSpaceBid())
                                        .build();
                                genericLogAddParamList.add(genericLogAddParam);

                                //20250527保存记录空间应用的删除日志
                                GenericLogAddParam spaceAppGenericLogAddParam = GenericLogAddParam.builder()
                                        .logMsg("删除了[" + mObject.getName() + "]的实例数据")
                                        .modelCode(sourceModelCode)
                                        .instanceBid(String.valueOf(mObject.get(SpaceAppDataEnum.SPACE_APP_BID.getCode())))
                                        .type(EsUtil.EsType.LOG.getType())
                                        .spaceBid(app.getSpaceBid())
                                        .build();
                                genericLogAddParamList.add(spaceAppGenericLogAddParam);

                            });
                }
            }
        } catch (Exception e) {
            log.error("记录删除[" + insNameList + "]的实例数据日志失败", e);
        }
        IApmSpaceAppDataDrivenService proxy = (IApmSpaceAppDataDrivenService) AopContext.currentProxy();
        for (Map.Entry<String, List<MObject>> entry : spaceAppDataMap.entrySet()) {
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(entry.getKey());
            List<String> bids = entry.getValue().stream().map(MObject::getBid).collect(Collectors.toList());
            proxy.batchLogicalDeleteByBids(apmSpaceApp.getBid(), bids, false);
        }
        try {
            // 记录关系删除日志 只有在tab中执行操作是才会记录关系移除的操作日志
            if (qo.isTabOperation()) {
                List<MObject> relationInsList = objectModelCrudI.listByBids(qo.getRelationBids(), qo.getRelationModelCode());
                Map<String, String> bid2NameMap = list.stream().collect(Collectors.toMap(MObject::getBid, MObject::getName, (k1, k2) -> k1));
                for (MObject relationIns : relationInsList) {
                    GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                            .logMsg("移除了与[" + bid2NameMap.get(String.valueOf(relationIns.get(TARGET_BID.getCode()))) + "]的关系")
                            .modelCode(qo.getRelationSourceModelCode())
                            .instanceBid(String.valueOf(relationIns.get(SOURCE_BID.getCode())))
                            .type(EsUtil.EsType.LOG.getType())
                            .spaceBid(app.getSpaceBid())
                            .build();
                    genericLogAddParamList.add(genericLogAddParam);
                }
            }
            if (CollectionUtils.isNotEmpty(genericLogAddParamList)) {
                operationLogEsService.genericBulkSave(genericLogAddParamList);
            }
        } catch (Exception e) {
            log.error("记录删除[" + insNameList + "]的实例数据日志失败", e);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addBatch(String spaceBid, String spaceAppBid, List<MSpaceAppData> targetMObjects,
                            ApmSpaceAppDataDrivenOperationFilterBo filterBo) {
        targetMObjects.forEach(targetMObject -> {
            apmSpaceAppDataDrivenService.add(spaceAppBid, targetMObject, filterBo);
            //检查流程 是否正常启动，对异常启动做补偿
            if(targetMObject.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) != null){
                CompletableFuture.runAsync(() -> runtimeService.runStartNode(targetMObject.getBid(),spaceBid,spaceAppBid), SimpleThreadPool.getInstance());
            }
        });
        return true;
    }

    /**
     * 分组移动以及更改分组值
     *
     * @param spaceAppBid         空间应用bid
     * @param groupProperty       分组属性
     * @param moveGroupNodeParams 移动分组数据
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveGroupNode(String spaceAppBid, String groupProperty, List<MoveGroupNodeParam> moveGroupNodeParams) {
        List<MObjectTree> treeList = moveGroupNodeParams.stream().map(moveTreeNodeParam -> {
            if(CommonConst.LIFE_CYCLE_CODE_STR.equals(groupProperty)){
                //生命周期需要校验状态是不是安顺序来的
                MSpaceAppData mSpaceAppData = get(spaceAppBid, moveTreeNodeParam.get(CommonConst.BID_STR)+"",false);
                if(mSpaceAppData == null){
                    throw new BusinessException("拖动实例不存在!");
                }
                ApmStateQo apmStateQo = new ApmStateQo();
                apmStateQo.setLcTemplBid(mSpaceAppData.getLcTemplBid());
                apmStateQo.setLcTemplVersion(mSpaceAppData.getLcTemplVersion());
                apmStateQo.setCurrentState(mSpaceAppData.getLifeCycleCode());
                apmStateQo.setInstanceBid(moveTreeNodeParam.get(CommonConst.BID_STR)+"");
                apmStateQo.setModelCode(mSpaceAppData.getModelCode());
                apmStateQo.setSpaceAppBid(mSpaceAppData.getSpaceAppBid());
                List<ApmStateVO>  apmStateVOList = apmSpaceAppConfigManageService.listNextStates(apmStateQo);
                if(CollectionUtils.isEmpty(apmStateVOList)){
                    throw new BusinessException("该状态无下个状态，无法拖动!");
                }
                List<String> nextStateList = apmStateVOList.stream().map(ApmStateVO::getLifeCycleCode).collect(Collectors.toList());
                if(!nextStateList.contains(moveTreeNodeParam.get(CommonConst.GROUP_PROPERTY_STR)+"")){
                    throw new BusinessException("该状态不是实例的下个状态，无法拖动!");
                }
            }
            // 复制数据
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(moveTreeNodeParam);
            // 设置更改分组值
            Object groupPropertyValue = moveTreeNodeParam.getGroupProperty();
            if (Objects.nonNull(groupPropertyValue)) {
                mObjectTree.put(groupProperty, groupPropertyValue);
            }
            return mObjectTree;
        }).collect(Collectors.toList());
        // 暂时可以使用批量移动树的方式
        return objectModelCrudI.moveTreeNode(
                apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid),
                treeList);
    }

    /**
     * 更新部分属性并完成流程节点
     *
     * @param spaceAppBid
     * @param bid
     * @param nodeBid
     * @param mSpaceAppData
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePartialContentAndCompleteFlowNode(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData) {
        // 复制提交的内容
        MSpaceAppData mSpaceAppDataCopy = new MSpaceAppData();
        mSpaceAppDataCopy.putAll(mSpaceAppData);
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        MObject mObject = objectModelCrudI.getByBid(app.getModelCode(), bid);
        MSpaceAppData mSpaceAppDataUpdate = new MSpaceAppData();
        mSpaceAppDataUpdate.putAll(mSpaceAppData);
        ApmFlowInstanceNode currentNode = apmFlowInstanceNodeService.getByBid(nodeBid);
        MObject checkMObject = new MObject();
        checkMObject.putAll(mObject);
        checkMObject.putAll(mSpaceAppData);
        runtimeService.completeNodeCheck(app, currentNode, checkMObject);
        // 更新属性
        IApmSpaceAppDataDrivenService proxy = (IApmSpaceAppDataDrivenService) AopContext.currentProxy();
        if (CollectionUtils.isNotEmpty(mSpaceAppData)) {
            // 更新部分属性
            proxy.updatePartialContent(spaceAppBid, bid, mSpaceAppData,false);
        }

        String versionInstanceBid = runtimeService.getVersionInstanceBid(spaceAppBid, bid);
        mSpaceAppData.setBid(versionInstanceBid);
        // 完成流程节点
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = runtimeService.completeNodeReturnNextNode(nodeBid, mSpaceAppData);

        // 记录历程
        if (CollectionUtils.isNotEmpty(apmFlowInstanceNodes)) {
            // 有操作动作记录动作
            apmFlowInstanceNodes.forEach(apmFlowInstanceNode -> {
                runtimeService.saveFlowProcess(nodeBid, runtimeService.getVersionInstanceBid(spaceAppBid, bid), "complete",apmFlowInstanceNode, mSpaceAppDataCopy);
            });
        } else {
            runtimeService.saveFlowProcess(nodeBid, runtimeService.getVersionInstanceBid(spaceAppBid, bid), "complete", mSpaceAppDataCopy);
        }

        try {
            // 日志记录
            // 通过nodeBid查询节点信息

            String webBid = currentNode.getWebBid();
            ViewQueryParam param = ViewQueryParam.of()
                    .setViewBelongBid(webBid + "#NODE_COMPLETE");
            CfgViewVo cfgViewVo = viewFeignClient.getView(param).getCheckExceptionData();
            if (cfgViewVo != null && CollectionUtils.isNotEmpty(cfgViewVo.getMetaList())) {
                Map<String, CfgViewMetaDto> name2MetaDtoMap = cfgViewVo.getMetaList().stream().collect(Collectors.toMap(CfgViewMetaDto::getName, Function.identity()));
                mSpaceAppDataUpdate.remove("specialCount");
                mSpaceAppDataUpdate.forEach((key, value) -> {
                    Map<String, Object> properties = apmSpaceAppConfigDrivenService.getProperties(cfgViewVo.getContent(), key);
                    OperationLogAddParam operationLogAddParam =
                            OperationLogAddParam.builder()
                                    .modelCode(app.getModelCode())
                                    .cfgViewMetaDto(name2MetaDtoMap.get(key))
                                    .properties(properties)
                                    .instanceBid(bid)
                                    .fieldName(key)
                                    .fieldValue(value)
                                    .isAppView(true)
                                    .spaceAppBid(spaceAppBid)
                                    .spaceBid(app.getSpaceBid()).build();
                    operationLogEsService.save(operationLogAddParam, null);
                });
            }
        } catch (Exception e) {
            log.error("流程节点完成日志记录失败", e);
        }
        // 完成流程节点
        return Boolean.TRUE;
    }

    /**
     * 更新部分属性并回滚流程节点
     *
     * @param spaceAppBid
     * @param bid
     * @param nodeBid
     * @param mSpaceAppData
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePartialContentAndRollbackFlowNode(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData,boolean runEvent) {
        // 复制提交的内容
        MSpaceAppData mSpaceAppDataCopy = new MSpaceAppData();
        mSpaceAppDataCopy.putAll(mSpaceAppData);
        // 更新属性
        IApmSpaceAppDataDrivenService proxy = (IApmSpaceAppDataDrivenService) AopContext.currentProxy();
        if (CollectionUtils.isNotEmpty(mSpaceAppData)) {
            proxy.updatePartialContent(spaceAppBid, bid, mSpaceAppData);
        }
        Boolean result = runtimeService.rollback(nodeBid,runEvent);
        // 记录历程
        runtimeService.saveFlowProcess(nodeBid, runtimeService.getVersionInstanceBid(spaceAppBid, bid), "rollback", mSpaceAppDataCopy);
        return result;
    }

    @Override
    public MObject executeAction(String spaceAppBid,
                                 String instanceBid,
                                 String actionBid,
                                 MSpaceAppData mSpaceAppData) {
        ApmAccess access = accessService.getOne(Wrappers.<ApmAccess>lambdaQuery().eq(ApmAccess::getBid, actionBid)
                .eq(ApmAccess::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                .eq(ApmAccess::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
        );
        if (Objects.isNull(access)) {
            throw new PlmBizException("操作不存在，请联系运维人员！");
        }
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        String spaceBid = apmSpaceApplicationService.getSpaceBidBySpaceAppBid(spaceAppBid);
        MObject instance = objectModelCrudI.getByBid(app.getModelCode(), instanceBid);
        if (Objects.isNull(instance)) {
            return instance;
        }
        List<CfgViewMetaDto> cfgViewMetaList = apmSpaceAppConfigDrivenService.baseViewGetMeteModels(spaceAppBid);
        List<String> valChangeRecord = Lists.newArrayList();
        // 通过action类型添加额外属性
        Map<String, String> resource = access.getOperationConfig();
        if (Objects.nonNull(resource) && !resource.isEmpty()) {
            mSpaceAppData.putAll(resource);
        }
        for (Object key : mSpaceAppData.keySet().toArray()) {
            String keyName = (String) key;
            CfgViewMetaDto cfgViewMetaDto = cfgViewMetaList.stream()
                    .filter(cfgViewMeta -> cfgViewMeta.getName().equals(keyName))
                    .findFirst()
                    .orElse(CfgViewMetaDto.builder().build());
            String fieldName = cfgViewMetaDto.getLabel();
            String oldVal = Optional.ofNullable(instance.get(keyName)).map(Object::toString).orElse(keyName);
            String newVal = Optional.ofNullable(mSpaceAppData.get(keyName)).map(Object::toString).orElse(keyName);
            if (!oldVal.equals(newVal) && fieldName != null) {
                valChangeRecord.add("「" + fieldName + "」:" + oldVal + "= >" + newVal);
            }

        }
        // 操作信息
        String actionName = access.getName();
        Boolean updateResult = appDataService.updateByBid(app.getModelCode(), instanceBid, mSpaceAppData);
        MObject instanceNew = objectModelCrudI.getByBid(app.getModelCode(), instanceBid);
        if (BooleanUtil.isTrue(updateResult)) {
            try {
                GenericLogAddParam logParam = GenericLogAddParam.builder().spaceBid(spaceBid)
                        .modelCode(mSpaceAppData.getModelCode())
                        .instanceBid(instanceBid)
                        .logMsg("执行了" + actionName + "操作，修改了" + String.join(",", valChangeRecord))
                        .build();
                operationLogEsService.genericSave(logParam);
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }
        return instanceNew;
    }

    /**
     * 检查空间与应用bid不为空
     *
     * @param spaceAppBid 空间应用bid
     */
    private static void checkSpaceAndAppBidNotNull(String spaceAppBid) {
        if (StringUtils.isEmpty(spaceAppBid)) {
            throw new PlmBizException("空间BID与应用bid必填！");
        }
    }

    private void checkAddOrUpdateSpaceModule(ApmSpaceApp app, MObject mObject) {
        boolean isVoid = Objects.isNull(app) || Objects.isNull(mObject)
                || Objects.isNull(mObject.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()));
        if (isVoid) {
            return;
        }
        //如果所选空间数据为空，那么修改当前数据挂载空间
        if (StringUtil.isBlank((String) mObject.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()))) {
            mObject.put(SPACE_BID.getCode(), app.getSpaceBid());
            mObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), app.getBid());
            return;
        }
        List<ApmSpaceAppVo> appList = apmSpaceApplicationService.listApp(String.valueOf(mObject.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode())));
        if (CollectionUtils.isEmpty(appList)) {
            throw new PlmBizException("所选空间不存在应用配置，请添加应用后再操作！");
        }
        ApmSpaceAppVo apmSpaceAppVo = appList.stream().filter(e -> e.getModelCode().equals(app.getModelCode())).findFirst().orElse(null);
        if (Objects.isNull(apmSpaceAppVo)) {
            throw new PlmBizException("所选空间不存在应用配置，请添加应用后再操作！");
        }
        /*//如果所选空间数据不为空，那么置换真实空间和空间数据信息
        mObject.put(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode(), app.getSpaceBid());
        mObject.put(SPACE_BID.getCode(), apmSpaceAppVo.getSpaceBid());
        mObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), apmSpaceAppVo.getBid());*/
    }

    /**
     * @Description 如果包含关系组件，需要保存关系组件数据
     * @Author jinpeng.bai
     * @Date 2023/10/10 11:43
     * @Param [spaceAppBid, mObject]
     * @Return void
     * @Since version 1.0
     */
    private void checkAddOrUpdateRelationModule(ApmSpaceApp app, List<CfgViewMetaDto> relationModuleList, MObject mObject, Boolean isEdit) {
        if (CollectionUtils.isNotEmpty(relationModuleList)) {
            // 保存关系组件数据,后面切换成线程池
            relationModuleList.forEach(relationModule -> {
                //先查询关系是否存在
                if (StringUtils.isEmpty(relationModule.getRelationModelCode()) || StringUtils.isEmpty(relationModule.getTargetModelCode()) || StringUtils.isEmpty(relationModule.getSourceModelCode())) {
                    return;
                }
                if (Boolean.TRUE.equals(isEdit) && StringUtils.isNoneBlank(relationModule.getRelationModelCode())) {
                    List<MObject> existRelationList = objectModelCrudI.list(
                            relationModule.getRelationModelCode(), buildQueryWrapperTargetBid(mObject.getBid()));
                    //如果存在，逻辑删除数据
                    if (CollectionUtils.isNotEmpty(existRelationList)) {
                        objectModelCrudI.batchLogicalDeleteByBids(relationModule.getRelationModelCode(), existRelationList.stream().map(MObject::getBid).collect(Collectors.toList()));
                    }
                }
                if (Objects.isNull(mObject.get(relationModule.getName())) || StringUtils.isEmpty(mObject.get(relationModule.getName()).toString())) {
                    return;
                }
                if (Boolean.TRUE.equals(relationModule.getMultiple())) {
                    List<String> relationBidList = new ArrayList<>();
                    Object object = mObject.get(relationModule.getName());
                    if(object instanceof List){
                        relationBidList = (List<String>) object;
                    }else if (object instanceof String){
                        relationBidList.add(object.toString());
                    }
                    relationBidList.forEach(e -> {
                        MObject relationObject = assembleRelationObject(e, mObject.getBid(), relationModule.getTargetModelCode(), app);
                        appDataService.add(relationModule.getRelationModelCode(), relationObject);
                    });
                    return;
                }
                MObject relationObject = assembleRelationObject(mObject.get(relationModule.getName()), mObject.getBid(),relationModule.getTargetModelCode(), app);
                appDataService.add(relationModule.getRelationModelCode(), relationObject);
                try {
                    String name = mObject.getName();
                    if (name == null) {
                        MObject obj = objectModelCrudI.getByBid(app.getModelCode(), mObject.getBid());
                        name = obj.getName();
                    }
                    GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                            .logMsg("关联了[" + name + "]的实例数据")
                            .modelCode(relationModule.getSourceModelCode())
                            .instanceBid(String.valueOf(relationObject.get("sourceBid")))
                            .type(EsUtil.EsType.LOG.getType())
                            .spaceBid(app.getSpaceBid())
                            .build();
                    CompletableFuture.runAsync(() -> operationLogEsService.genericSave(genericLogAddParam), SimpleThreadPool.getInstance());
                } catch (Exception e) {
                    log.error("记录关联了的实例数据日志失败", e);
                }
                //如果是增加需求和迭代的关系，则要维护需求和项目的关系
                if (REVISION_DEMAND_REL_MODEL_CODE.equals(relationModule.getRelationModelCode())) {
                    NotifyCrossRelationEventBusDto notifyCrossRelationEventBusDto = NotifyCrossRelationEventBusDto.builder()
                            .spaceBid(app.getSpaceBid())
                            .spaceAppBid(null)
                            .currentRelationModelCode(relationModule.getRelationModelCode())
                            .currentSourceModelCode(relationModule.getSourceModelCode())
                            .currentTargetModelCode(relationModule.getTargetModelCode())
                            .config(notifyCrossRelationConfig.getConfig()).currentSourceBid((String) mObject.get(relationModule.getName()))
                            .currentSourceDataBid((String) mObject.get(relationModule.getName()))
                            .currentTargetList(Lists.newArrayList(mObject)).jobNumber(SsoHelper.getJobNumber()).build();
                    NotifyEventBus.publishCrossEvent(notifyCrossRelationEventBusDto);
                }
            });
        }
    }

    /**
     * 组装关系数据
     * @param: @param sourceBid
     * @param sourceBid
     * @param targetBid
     * @param app
     * @return: com.transcend.plm.datadriven.api.model.MObject
     * @version: 1.0
     * @date: 2023/11/30/030
     * @author: yanbing.ao
     */
    private MObject assembleRelationObject(Object sourceBid, Object targetBid, String targetModeCode, ApmSpaceApp app) {
        MObject object = objectModelCrudI.getByBid(targetModeCode, targetBid.toString());
        MObject relationObject = new MObject();
        relationObject.put(RelationEnum.SOURCE_DATA_BID.getCode(), "-1");
        relationObject.put(RelationEnum.TARGET_DATA_BID.getCode(), object.get(RelationEnum.DATA_BID.getCode()));
        relationObject.put(RelationEnum.SOURCE_BID.getCode(), sourceBid);
        relationObject.put(RelationEnum.TARGET_BID.getCode(), targetBid);
        relationObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), "0");
        relationObject.put(SPACE_BID.getCode(), app.getSpaceBid());
        relationObject.put(RelationEnum.DRAFT.getCode(), Boolean.FALSE);
        return relationObject;
    }

    /**
     * 查询关系元数据组件
     *
     * @param spaceAppBid              空间应用bid
     * @param ignoreRelationModelCodes 忽略
     * @return
     */
    private List<CfgViewMetaDto> queryRelationMetaList(String spaceAppBid, String fieldName, Set<String> ignoreRelationModelCodes) {
        List<CfgViewMetaDto> relationModuleList = Lists.newArrayList();
        ignoreRelationModelCodes = Optional.ofNullable(ignoreRelationModelCodes).orElse(Sets.newHashSet());
        List<CfgViewMetaDto> cfgViewMetaList = apmSpaceAppConfigDrivenService.baseViewGetMeteModels(spaceAppBid);
        boolean fieldFlag = StringUtils.isBlank(fieldName);
        if (CollectionUtils.isNotEmpty(cfgViewMetaList)) {
            Set<String> finalIgnoreRelationModelCodes = ignoreRelationModelCodes;
            relationModuleList = cfgViewMetaList.stream().filter(
                    meta -> ViewComponentEnum.RELATION_CONSTANT.equals(meta.getType()) && (fieldFlag || StringUtils.equals(fieldName, meta.getName())) &&
                            !finalIgnoreRelationModelCodes.contains(meta.getRelationModelCode())
            ).collect(Collectors.toList());
        }
        return relationModuleList;
    }

    /**
     * @Description 查询单挑数据时处理特殊关系组件数据
     * @Author jinpeng.bai
     * @Date 2023/10/10 21:15
     * @Param [spaceAppBid, mObject]
     * @Return void
     * @Since version 1.0
     */
    private void getRelationModule(String spaceAppBid, MObject mObject) {
        List<CfgViewMetaDto> cfgViewMetaList = apmSpaceAppConfigDrivenService.baseViewGetMeteModels(spaceAppBid);
        if (CollectionUtils.isEmpty(cfgViewMetaList)) {
            return;
        }
        List<CfgViewMetaDto> relationModuleList = cfgViewMetaList.stream().filter(meta -> ViewComponentEnum.RELATION_CONSTANT.equals(meta.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(relationModuleList)) {
            relationModuleList.forEach(relationModule -> {
                if (StringUtils.isBlank(relationModule.getRelationModelCode())) {
                    return;
                }
                List<MObject> existRelationList = objectModelCrudI.list(
                        relationModule.getRelationModelCode(), buildQueryWrapperTargetBid(mObject.getBid()));
                //如果存在，返回关系数据
                if (CollectionUtils.isNotEmpty(existRelationList)) {
                    if (Boolean.TRUE.equals(relationModule.getMultiple())) {
                        List<Object> sourceBids = existRelationList.stream().map(e -> e.get(RelationEnum.SOURCE_BID.getCode())).collect(Collectors.toList());
                        mObject.put(relationModule.getName(), sourceBids);
                        return;
                    }
                    mObject.put(relationModule.getName(), existRelationList.get(0).get(RelationEnum.SOURCE_BID.getCode()));
                }
            });
        }
    }

    /**
     * @Description 查询单挑数据时处理特殊关系组件数据
     * @Author jinpeng.bai
     * @Date 2023/10/10 21:15
     * @Param [spaceAppBid, mObject]
     * @Return void
     * @Since version 1.0
     */
    private void getProcessRoleModule(String spaceAppBid, MObject mObject) {
        List<CfgViewMetaDto> cfgViewMetaList = apmSpaceAppConfigDrivenService.baseViewGetMeteModels(spaceAppBid);
        if (CollectionUtils.isEmpty(cfgViewMetaList)) {
            return;
        }
        List<CfgViewMetaDto> relationModuleList = cfgViewMetaList.stream().filter(meta -> ViewComponentEnum.ROLE_USER.getType().equals(meta.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(relationModuleList)) {
            List<ApmRoleUserAO> instanceRoleUserList = runtimeService.listInstanceRoleUser(mObject.get(VersionObjectEnum.DATA_BID.getCode()).toString());
            if (CollectionUtils.isEmpty(instanceRoleUserList)) {
                return;
            }
            Map<String, List<String>> roleUserMap = Maps.newHashMap();
            instanceRoleUserList.forEach(e -> {
                if (CollectionUtils.isNotEmpty(e.getUserList())) {
                    roleUserMap.put(e.getRoleBid(), e.getUserList().stream().map(ApmUser::getEmpNo).collect(Collectors.toList()));
                }
            });
            mObject.put(SpaceAppDataEnum.ROLE_USER.getCode(), roleUserMap);

        }
    }

    private void getSpaceModule(MObject mObject) {
        if (mObject == null || Objects.isNull(mObject.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode())) || StringUtil.isBlank((String) mObject.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()))) {
            return;
        }
        List<ApmSpaceAppVo> appList = apmSpaceApplicationService.listApp(String.valueOf(mObject.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode())));
        if (CollectionUtils.isEmpty(appList)) {
            throw new PlmBizException("所选空间不存在应用配置，请添加应用后再操作！");
        }
        ApmSpaceAppVo apmSpaceAppVo = appList.stream().filter(e -> e.getModelCode().equals(mObject.getModelCode())).findFirst().orElse(null);
        if (Objects.isNull(apmSpaceAppVo)) {
            throw new PlmBizException("所选空间不存在应用配置，请添加应用后再操作！");
        }
        //如果所选空间数据不为空，那么置换真实空间和空间数据信息
       /* mObject.put(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode(), mObject.get(SPACE_BID.getCode()));
        mObject.put(SPACE_BID.getCode(), apmSpaceAppVo.getSpaceBid());
        mObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), apmSpaceAppVo.getBid());*/
    }

    private void getSpaceModuleList(String spaceBid, List<? extends MObject> mObject) {
        Map<String, ApmSpaceAppVo> apmSpaceAppVoMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(mObject)) {
            return;
        }
        mObject.forEach(e -> {
            Object mountSpaceBid = e.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode());
            if (Objects.isNull(mountSpaceBid) || StringUtil.isBlank((String) mountSpaceBid) || spaceBid.equals(e.get(SPACE_BID.getCode()))) {
                e.put(SpaceAppDataEnum.IS_MOUNT.getCode(), false);
                return;
            }
            ApmSpaceAppVo apmSpaceAppVo = apmSpaceAppVoMap.get(e.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()) + e.getModelCode());
            if (apmSpaceAppVo == null) {
                List<ApmSpaceAppVo> appList = apmSpaceApplicationService.listApp(String.valueOf(e.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode())));
                if (CollectionUtils.isEmpty(appList)) {
                    throw new PlmBizException("所选空间不存在应用配置，请添加应用后再操作！");
                }
                apmSpaceAppVo = appList.stream().filter(appVo -> appVo.getModelCode().equals(e.getModelCode())).findFirst().orElse(null);
                if (Objects.isNull(apmSpaceAppVo)) {
                    throw new PlmBizException("所选空间不存在应用配置，请添加应用后再操作！");
                }
                apmSpaceAppVoMap.put(e.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()) + e.getModelCode(), apmSpaceAppVo);
            }
            //如果所选空间数据不为空，那么置换真实空间和空间数据信息
            /*e.put(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode(), e.get(SPACE_BID.getCode()));
            e.put(SPACE_BID.getCode(), apmSpaceAppVo.getSpaceBid());
            e.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), apmSpaceAppVo.getBid());*/
            e.put(SpaceAppDataEnum.IS_MOUNT.getCode(), true);
        });
    }

    /**
     * @Description 构建targetBid查询条件
     * @Author jinpeng.bai
     * @Date 2023/10/11 10:11
     * @Param [targetBid]
     * @Return com.transcend.plm.datadriven.api.model.QueryWrapper
     * @Since version 1.0
     */

    private List<QueryWrapper> buildQueryWrapperTargetBid(String targetBid) {
        List<QueryWrapper> queryWrappers = Lists.newArrayList();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(RelationEnum.TARGET_BID.getColumn());
        queryWrapper.setCondition("=");
        queryWrapper.setValue(targetBid);
        queryWrappers.add(queryWrapper);
        return queryWrappers;
    }

    @Override
    public List<ResourceVo> getSpaceResources(String spaceBid, ResourceQo resourceQo) {
        // 通过spaceBid查询空间下所有的任务
        QueryWrapper qo = new QueryWrapper(Boolean.FALSE);
        qo.eq(TranscendModelBaseFields.SPACE_BID, spaceBid);
        ModelMixQo modelMixQo = resourceQo.getModelMixQo();
        List<QueryWrapper> queryWrappers = new ArrayList<>();
        QueryCondition queryCondition = new QueryCondition();
        if(modelMixQo != null){
            queryCondition.setOrders(modelMixQo.getOrders());
            List<ModelFilterQo> queries = modelMixQo.getQueries();
            if(queries == null){
                queries = new ArrayList<>();
            }
            modelMixQo.setQueries(queries);
            queryWrappers = QueryConveterTool.convert(modelMixQo).getQueries();
        }
        if(CollectionUtils.isNotEmpty(queryWrappers)){
            queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo));
        }else{
            queryWrappers = QueryWrapper.buildSqlQo(qo);
        }
        queryCondition.setQueries(queryWrappers);
        // 查询空间下所有的任务
        List<MObject> taskList = appDataService.list(spaceBid,TASK_MODEL_CODE, queryCondition);
        // 查询空间下所有的故事
        QueryWrapper qo2 = new QueryWrapper();
        qo2.eq(TranscendModelBaseFields.MODEL_CODE, DEMAND_STORY_MODEL_CODE).and().eq(TranscendModelBaseFields.SPACE_BID, spaceBid);
        List<QueryWrapper> storyQueryWrappers = QueryWrapper.buildSqlQo(qo2);
        QueryCondition storyQueryCondition = new QueryCondition();
        storyQueryCondition.setQueries(storyQueryWrappers);
        List<MObject> storyList = appDataService.list(spaceBid,DEMAND_MODEL_CODE, storyQueryCondition);
        // 查询空间下所有的故事和任务的关系
        if(CollectionUtils.isNotEmpty(storyList)){
            //用户故事和任务modelCode
            String taskRcModelCode = STORY_TASK_REL_MODEL_CODE;
            List<String> storyBids = storyList.stream().map(MObject::getBid).collect(Collectors.toList());
            RelationMObject relationMObjectTask = RelationMObject.builder().relationModelCode(taskRcModelCode).sourceBids(storyBids).build();
            List<MObject> storyAndTaskRel = objectModelCrudI.listOnlyRelationMObjects(relationMObjectTask);
            //找所有任务
            Map<String, String> storyTaskMap = new HashMap<>(16);
            for (MObject mObject : storyAndTaskRel) {
                storyTaskMap.put(mObject.get(RelationEnum.SOURCE_BID.getCode()) + "", mObject.get(RelationEnum.TARGET_BID.getCode()) + "");
            }
            //过滤用户故事有任务的数据
            for (int i = storyList.size() - 1; i >= 0; i--) {
                MObject mObject = storyList.get(i);
                if (storyTaskMap.containsKey(mObject.getBid())) {
                    storyList.remove(i);
                }
            }
        }
        taskList.addAll(storyList);
        return mObject2ProjectResourceVo(taskList, resourceQo.getStartTime(), resourceQo.getEndTime());
    }


    /**
     * 去重属性列表
     *
     * @param spaceAppBid      空间应用bid
     * @param distinctProperty 去重属性
     * @param queryCondition   查询条件
     * @return List<Object>
     */
    @Override
    public List<Object> listPropertyDistinct(String spaceAppBid, String distinctProperty, QueryCondition queryCondition) {
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        // 设置应用过滤条件
        queryCondition.setQueries(buildSpaceAppBid(spaceAppBid, queryCondition.getQueries()));
        return objectModelCrudI.listPropertyDistinct(modelCode, distinctProperty, queryCondition);
    }

    @Override
    public MSpaceAppData revise(String spaceAppBid, String bid) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public MSpaceAppData promote(String spaceBid, String spaceAppBid, LifeCyclePromoteDto promoteDto) {
        promoteDto.setModelCode(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid));
        return appDataService.promote(spaceBid,spaceAppBid,promoteDto);
    }

    @Override
    public ApmLaneGroupData listAllLane(String spaceBid, String spaceAppBid, ApmLaneAllQo laneAllQo) {
        ApmLaneGroupData apmLaneGroupData = new ApmLaneGroupData();
        /*
         * 因为泳道图支持拖动改变生命周期状态
         * 所以为了避免将部分对象拖动到没有的生命周期
         * 需要把所有对象的生命周期都返回，给前端做拖动控制
         */
        MultiAppConfig multiAppConfig = apmSpaceAppConfigDrivenService.getMultiAppConfig(spaceBid, spaceAppBid, laneAllQo.getSpaceAppTabBid());
        List<MultiAppTree> multiAppTree = multiAppConfig.getMultiAppTree();
        // 1. 处理表头数据
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        if (Objects.isNull(apmSpaceApp)) {
            throw new PlmBizException("应用不存在");
        }
        HashMap<String, List<String>> headersMap = Maps.newHashMap();
        List<ApmSpaceApp> apmSpaceAppList = Lists.newArrayList();
        for (MultiAppTree appTree : multiAppTree) {
            // 查询生命周期状态
            ApmLifeCycleStateVO lifeCycleState = apmSpaceAppConfigManageService.getLifeCycleState(appTree.getBid());
            List<ApmStateVO> apmStateVOList = lifeCycleState.getApmStateVOList();
            if (!CollectionUtils.isEmpty(apmStateVOList)) {
                // 获取生命周期状态
                List<String> stateList = apmStateVOList.stream().map(ApmStateVO::getLifeCycleCode).collect(Collectors.toList());
                ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(appTree.getBid());
                apmSpaceAppList.add(app);
                headersMap.put(app.getModelCode(), stateList);
            }
        }
        // 如果没有生命周期状态，返回空数据
        if (CollectionUtils.isEmpty(headersMap)) {
            return apmLaneGroupData;
        }
        apmLaneGroupData.setHeadersMap(headersMap);
        // 2. 处理数据
        // 查询空间下所有的应用
        if (CollectionUtils.isEmpty(apmSpaceAppList)) {
            return apmLaneGroupData;
        }
        Map<String, List<MObject>> resultData = apmSpaceAppList.parallelStream()
                .map(app -> {
                    QueryCondition condition = QueryConveterTool.convert(laneAllQo.getMixQo());
                    condition.getQueries().add(new QueryWrapper(Boolean.TRUE).setSqlRelation(" and "));
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper
                            .eq(TranscendModelBaseFields.SPACE_BID, app.getSpaceBid())
                            .or()
                            .eq("mountSpaceBid", app.getSpaceBid());
                    condition.getQueries().addAll(QueryWrapper.buildSqlQo(queryWrapper));
                    return appDataService.list(app.getSpaceBid(),app.getBid(), condition);
                }).flatMap(Collection::stream)
                .collect(Collectors.groupingBy(obj -> String.valueOf(obj.get(TranscendModelBaseFields.LIFE_CYCLE_CODE)), Collectors.toList()));
        apmLaneGroupData.setLaneAllMObjects(resultData);
        return apmLaneGroupData;
    }

    @Override
    public Boolean batchRemove(String spaceAppBid, BatchLogicDelAndRemQo qo) {
        String modelCode = qo.getModelCode();
        ApmSpaceApp app = apmSpaceAppService.getByBid(spaceAppBid);
        if (StringUtils.isBlank(modelCode)) {
            modelCode = app.getModelCode();
        }
        String relationModelCode = qo.getRelationModelCode();
        //查询多对象配置
        if (Boolean.TRUE.equals(qo.isTabOperation()) && StringUtils.isNotBlank(qo.getTabId())){
            MultiAppConfig multiAppConfig = apmSpaceAppConfigDrivenService.getMultiAppConfig(app.getSpaceBid(), spaceAppBid, qo.getTabId());
            MultiTreeConfigVo multiAppTreeConfig = multiAppConfig.getMultiAppTreeConfig();
            MultiTreeConfigVo parentConfig = findParentSourceModelCode(modelCode, multiAppTreeConfig);
            if (parentConfig != null && StringUtils.isNotBlank(parentConfig.getRelationModelCode())){
                relationModelCode = parentConfig.getRelationModelCode();
            }
        }
        // 查实例
        List<MObject> list = objectModelCrudI.listByBids(qo.getInsBids(), modelCode);
        // 查关系实例
        List<MObject> relationInsList = objectModelCrudI.listByBids(qo.getRelationBids(), relationModelCode);
        // 1. 移除关系
        Boolean result = objectModelCrudI.batchLogicalDeleteByBids(relationModelCode, qo.getRelationBids());

        //由于存在跨空间数据，需要按应用分组
        Map<String, List<MObject>> spaceAppDataMap = list.stream().collect(Collectors.groupingBy(v -> v.getOrDefault(SpaceAppDataEnum.SPACE_APP_BID.getCode(), "").toString()));
        //校验删除权限
        for (Map.Entry<String, List<MObject>> entry : spaceAppDataMap.entrySet()) {
            String apmSpaceAppBid = entry.getKey();
            List<String> targetBids = entry.getValue().stream().map(MObject::getBid).collect(Collectors.toList());
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(apmSpaceAppBid);

            // 存在目标对象的属性值从源对象中获取，需要把源对象从目标对象属性中移除
            String finalRelationModelCode = relationModelCode;
            List<CfgViewMetaDto> cfgViewMetaDtos = iApmSpaceAppConfigDrivenService.queryRelationMetaList(apmSpaceAppBid, new ArrayList<>())
                    .stream().filter(dto -> finalRelationModelCode.equals(dto.getRelationModelCode())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(cfgViewMetaDtos)) {
                //查询要删除的目标对象实例
                List<MObject> targetMObjects = objectModelCrudI.listByBids(targetBids, modelCode);
                if (CollectionUtils.isEmpty(targetMObjects)) {
                    return Boolean.TRUE.equals(result);
                }
                int totalTasks = cfgViewMetaDtos.size() * targetMObjects.size();
                CountDownLatch countDownLatch = new CountDownLatch(totalTasks);
                for (CfgViewMetaDto dto : cfgViewMetaDtos) {
                    for (MObject obj : targetMObjects) {
                        String bid = obj.getBid();
                        MSpaceAppData mSpaceAppData = new MSpaceAppData();
                        // 多选属性
                        if (Boolean.TRUE.equals(dto.getMultiple())) {
                            List<Object> objectList = new ArrayList<>();
                            if(obj.get(dto.getName()) instanceof List){
                                objectList = new ArrayList<>((Collection<?>) obj.get(dto.getName()));
                            }else if(obj.get(dto.getName()) instanceof String){
                                objectList.add(obj.get(dto.getName()));
                            }
                            objectList.remove(qo.getSourceBid());
                            mSpaceAppData.put(dto.getName(), objectList);
                        } else {
                            mSpaceAppData.put(dto.getName(), null);
                        }
                        List<String> bids = new ArrayList<>();
                        bids.add(bid);

                        //多线程处理
                        String finalModelCode = modelCode;
                        SimpleThreadPool.getInstance().execute(() -> {
                            try {
                                //更新字段
                                objectModelCrudI.batchUpdatePartialContentByIds(finalModelCode, mSpaceAppData, bids);
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
        }
        // 2. 记录移除的日志
        String insNameList = list.stream().map(MObject::getName).collect(Collectors.joining(","));
        if (Boolean.TRUE.equals(result)) {
            try {
                List<GenericLogAddParam> genericLogAddParamList = Lists.newArrayList();
                Map<String, String> bid2NameMap = list.stream().collect(Collectors.toMap(MObject::getBid, MObject::getName, (k1, k2) -> k1));
                for (MObject relationIns : relationInsList) {
                    GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                            .logMsg("移除了与[" + bid2NameMap.get(String.valueOf(relationIns.get(TARGET_BID.getCode()))) + "]的关系")
                            .modelCode(qo.getRelationSourceModelCode())
                            .instanceBid(String.valueOf(relationIns.get(SOURCE_BID.getCode())))
                            .type(EsUtil.EsType.LOG.getType())
                            .spaceBid(app.getSpaceBid())
                            .build();
                    genericLogAddParamList.add(genericLogAddParam);
                }
                operationLogEsService.genericBulkSave(genericLogAddParamList);
            } catch (Exception e) {
                log.error("记录删除[" + insNameList + "]的实例数据日志失败", e);
            }
        }
        return result;
    }

    @Override
    public Boolean batchRemoveRelation(String spaceAppBid, BatchRemoveRelationQo qo) {

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in(RelationEnum.SOURCE_BID.getColumn(), qo.getSourceBids()).and().in(RelationEnum.TARGET_BID.getColumn(), qo.getTargetBids());
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(queryWrapper);
        List<MObject> list = objectModelCrudI.list(qo.getRelationModelCode(), queryWrappers);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> bids = list.stream().map(obj -> obj.getBid()).collect(Collectors.toList());
            return objectModelCrudI.batchLogicalDeleteByBids(qo.getRelationModelCode(), bids);
        }

        return Boolean.TRUE;
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
                        result.add(apmSpaceAppDataDrivenService.add(spaceAppBid,mSpaceAppData));
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
        String bid = mSpaceAppData.getBid();
        if (StringUtils.isNotBlank(bid)) {
            updatePartialContent(spaceAppBid,bid, mSpaceAppData);
        }else {
            //设置初始生命周期状态
            mSpaceAppData.setLifeCycleCode(CommonEnum.DRAFT.getCode());
            // 初始化过滤器对象
            ApmSpaceAppDataDrivenOperationFilterBo filterBo = new ApmSpaceAppDataDrivenOperationFilterBo() ;
            // 初始化校验
            checkSpaceAndAppBidNotNull(spaceAppBid);
            //获取空间应用数据
            ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
            // 初始化赋值
            mSpaceAppData.setSpaceBid(app.getSpaceBid());
            mSpaceAppData.setSpaceAppBid(spaceAppBid);
            // 查询关系组件
            List<CfgViewMetaDto> relationModuleList = queryRelationMetaList(spaceAppBid, null, filterBo.getIgnoreRelationModelCodes());
            // 如果是特性和用户故事,关系组件传值了,multiTreeHead需要变为1
            extendRelationAddEventMethod(mSpaceAppData, app.getModelCode(), relationModuleList);
            // 处理特殊空间组件应用
            checkAddOrUpdateSpaceModule(app, mSpaceAppData);
            // 处理定制化字段
            handleCustomFieldStrategy.execute(app, mSpaceAppData);
            // TODO 返回实体
            MObject mObject = appDataService.add(app.getModelCode(), mSpaceAppData);
            //记录操作日志
            try {
                GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder().logMsg("创建了[" + mObject.getName() + "]的实例数据")
                        .modelCode(app.getModelCode())
                        .instanceBid(mObject.getBid()).type(EsUtil.EsType.LOG.getType()).spaceBid(apmSpaceApplicationService.getSpaceBidBySpaceAppBid(spaceAppBid)).build();
                operationLogEsService.genericSave(genericLogAddParam);
            } catch (Exception e) {
                log.error("创建了[" + mObject.getName() + "]的实例数据,记录日志失败", e);
            }
            //判断当前视图是否包含关系组件，如果包含关系组件需要增加关系数据
            checkAddOrUpdateRelationModule(app, relationModuleList, mSpaceAppData,
                    Boolean.FALSE);
        }
        return mSpaceAppData;
    }


    @Override
    public ApmLaneGroupData listLane(String spaceBid, String spaceAppBid, ApmLaneQO apmLaneQO) {
        ApmLaneGroupData result = new ApmLaneGroupData();
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        if (Objects.isNull(apmSpaceApp)) {
            throw new PlmBizException("应用不存在");
        }
        ApmLifeCycleStateVO lifeCycleState = apmSpaceAppConfigManageService.getLifeCycleState(apmLaneQO.getTargetAppBid());
        List<ApmStateVO> apmStateVOList = lifeCycleState.getApmStateVOList();
        if (CollectionUtils.isEmpty(apmStateVOList)) {
            return result;
        }
        List<String> stateList = apmStateVOList.stream().map(ApmStateVO::getLifeCycleCode).collect(Collectors.toList());
        result.setHeaders(stateList);
        //查找顶层数据
        List<MSpaceAppData> sourceDatas;
        // 源对象不设查询条件，改为筛选目标对象，源对象数据按照创建时间降序排序
        ModelMixQo targetModelMixQo = apmLaneQO.getMixQo();
        ModelMixQo sourceModelMixQo = ModelMixQo.of().setAnyMatch(false).setQueries(Lists.newArrayList()).setOrders(Lists.newArrayList(Order.of().setDesc(true).setProperty(TranscendModelBaseFields.CREATED_TIME)));
        QueryCondition queryCondition = QueryConveterTool.convert(sourceModelMixQo);
        if (StringUtils.isAnyBlank(apmLaneQO.getFromInstanceBid(), apmLaneQO.getFromRelationModelCode())) {
            sourceDatas = list(apmLaneQO.getSourceAppBid(), queryCondition);
        } else {
            RelationMObject relationMObject = RelationMObject.builder()
                    .sourceBid(apmLaneQO.getFromInstanceBid())
                    .relationModelCode(apmLaneQO.getFromRelationModelCode())
                    .targetModelCode(apmLaneQO.getSourceModelCode())
                    .sourceModelCode(apmSpaceApp.getModelCode())
                    .modelMixQo(sourceModelMixQo)
                    .build();
            List<MObject> relationMObjects = objectModelCrudI.listRelationMObjects(relationMObject);
            if (CollectionUtils.isEmpty(relationMObjects)) {
                return result;
            }
            sourceDatas = relationMObjects.stream().map(mObject -> {
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.putAll(mObject);
                return mSpaceAppData;
            }).collect(Collectors.toList());
            //处理当前目标对象关联在数据里，泳道图展示时需要把源对象也展示出来，源对象并未关联在当前数据里的业务场景
            apmLaneQO.setMixQo(sourceModelMixQo);
            accordByTargetBuildLane(apmSpaceApp,sourceDatas,apmLaneQO);
        }
        if (CollectionUtils.isEmpty(sourceDatas)) {
            return result;
        }

        Set<String> sourceBidSet = sourceDatas.parallelStream().map(MSpaceAppData::getBid).collect(Collectors.toSet());
        //查询关系列表
        List<QueryWrapper> relationQueryWrappers = buildSpaceBid(spaceBid, QueryConveterTool.convert(Lists.newArrayList(), null), true);
        List<MObject> relationMObjects = objectModelCrudI.list(apmLaneQO.getRelationModelCode(), relationQueryWrappers);

        //查询目标对象
        QueryWrapper spaceBidQueryWrapper = new QueryWrapper();
        spaceBidQueryWrapper.eq(SPACE_BID.getCode(), spaceBid).or().eq(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode(), spaceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(spaceBidQueryWrapper);
        List<QueryWrapper> queries = QueryConveterTool.convert(targetModelMixQo).getQueries();
        if (CollectionUtils.isNotEmpty(queries)) {
            QueryWrapper relationWrapper = new QueryWrapper(Boolean.TRUE);
            relationWrapper.setSqlRelation("and");
            queryWrappers.add(relationWrapper);
            queryWrappers.addAll(queries);
        }
        List<MObject> targetMobject = objectModelCrudI.list(apmLaneQO.getTargetModelCode(), queryWrappers);
        if(DEMAND_STORY_MODEL_CODE.equals(apmLaneQO.getTargetModelCode())){
            userDemandPlanTimeLogic(spaceBid,targetMobject);
        }
        //组装分组数据
        Map<String, MObject> targetObjectMap = targetMobject.parallelStream().collect(Collectors.toMap(MObject::getBid, Function.identity()));
        Map<String, List<MObject>> relationObjectGroupBySource = relationMObjects.parallelStream()
                .filter(mObject -> sourceBidSet.contains((String) mObject.get(RelationEnum.SOURCE_BID.getCode())))
                .collect(Collectors.groupingBy(mObject -> (String) mObject.get(RelationEnum.SOURCE_BID.getCode())));
        Map<String, ListMultimap<String, MObject>> targetGroupData = Maps.newConcurrentMap();
        relationObjectGroupBySource.entrySet().parallelStream().forEach(entry -> {
            String sourceBid = entry.getKey();
            List<MObject> relationObjectList = entry.getValue();
            ArrayListMultimap<String, MObject> targetObjectMapList = ArrayListMultimap.create();
            relationObjectList.forEach(relationObject -> {
                String targetBid = (String) relationObject.get(RelationEnum.TARGET_BID.getCode());
                MObject targetObject = targetObjectMap.get(targetBid);
                if (Objects.nonNull(targetObject)) {
                    //过滤富文本属性
                    filterBlobAttr.forEach(targetMobject::remove);
                    filterBlobAttr.forEach(targetObject::remove);
                    targetObjectMapList.put(targetObject.getLifeCycleCode(), targetObject);
                }
            });
            targetGroupData.put(sourceBid, targetObjectMapList);
        });
        List<ApmLaneDateVO> apmLaneDateVOS = sourceDatas.parallelStream().map(sourceData -> {
            ApmLaneDateVO laneDateVO = new ApmLaneDateVO();
            laneDateVO.putAll(sourceData);
            ListMultimap<String, MObject> oneLaneData = targetGroupData.get(sourceData.getBid());
            if (null != oneLaneData) {
                laneDateVO.setChildData(oneLaneData.asMap());
            }
            return laneDateVO;
        }).collect(Collectors.toList());
        // 如果有筛选条件，过滤掉目标对象为空的数据
        if (CollectionUtils.isNotEmpty(targetModelMixQo.getQueries())) {
            apmLaneDateVOS = apmLaneDateVOS.stream().filter(e -> CollectionUtils.isNotEmpty(e.getChildData())).collect(Collectors.toList());
        }
        result.setLaneDatas(apmLaneDateVOS);
        return result;
    }

    private void userDemandPlanTimeLogic(String spaceBid, List<MObject> targetDataList){
        if(CollectionUtils.isEmpty(targetDataList)){
            return;
        }
        //查询关系列表
        List<QueryWrapper> relationQueryWrappers = buildSpaceBid(spaceBid, QueryConveterTool.convert(Lists.newArrayList(), null), true);
        List<MObject> relationMObjects = objectModelCrudI.list(STORY_TASK_REL_MODEL_CODE, relationQueryWrappers);

        //查询目标对象
        QueryWrapper spaceBidQueryWrapper = new QueryWrapper();
        spaceBidQueryWrapper.eq(SPACE_BID.getCode(), spaceBid).or().eq(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode(), spaceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(spaceBidQueryWrapper);

        List<MObject> targetMobject = objectModelCrudI.list(TASK_MODEL_CODE, queryWrappers);
        if (CollectionUtils.isEmpty(targetMobject)) {
            filterBlobAttr.forEach(targetMobject::remove);
        }
        Map<String,MObject> targetMobjectMap = targetDataList.stream().collect(Collectors.toMap(MObject::getBid, Function.identity()));
        Set<String> sourceBidSet = targetDataList.stream().map(MObject::getBid).collect(Collectors.toSet());
        //组装分组数据
        Map<String, MObject> targetObjectMap = targetMobject.parallelStream().collect(Collectors.toMap(MObject::getBid, Function.identity()));
        Map<String, List<MObject>> relationObjectGroupBySource = relationMObjects.parallelStream()
                .filter(mObject -> sourceBidSet.contains((String) mObject.get(RelationEnum.SOURCE_BID.getCode())))
                .collect(Collectors.groupingBy(mObject -> (String) mObject.get(RelationEnum.SOURCE_BID.getCode())));
        relationObjectGroupBySource.entrySet().parallelStream().forEach(entry -> {
          String sourceBid = entry.getKey();
            List<MObject> relationObjectList = entry.getValue();
            ArrayList<MObject> targetObjectList = Lists.newArrayList();
            relationObjectList.forEach(relationObject -> {
                String targetBid = (String) relationObject.get(RelationEnum.TARGET_BID.getCode());
                MObject targetObject = targetObjectMap.get(targetBid);
                if (Objects.nonNull(targetObject)) {
                    targetObjectList.add(targetObject);
                }
            });
           MObject userDemand =  targetMobjectMap.get(sourceBid);
            if(userDemand.get(planStartTimeAttr) == null
                    || userDemand.get(planEndTimeAttr) == null){
                if(CollectionUtils.isNotEmpty(targetObjectList)){
                    List<MObject> minStartTimeList = targetObjectList.stream().filter(Objects::nonNull).filter(child -> child.get(taskStartTimeAttr) != null).collect(Collectors.toList());
                    List<MObject> minEndTimeList = targetObjectList.stream().filter(Objects::nonNull).filter(child -> child.get(taskEndTimeAttr) != null).collect(Collectors.toList());
                    if (userDemand.get(planStartTimeAttr) == null && CollectionUtils.isNotEmpty(minStartTimeList)) {
                        Optional<MObject> minStartTime =minStartTimeList.stream().
                                min(Comparator.comparing(o -> ((LocalDateTime) o.get(taskStartTimeAttr))));
                        minStartTime.ifPresent(object -> userDemand.put(planStartTimeAttr, object.get(taskStartTimeAttr)));
                    }
                    if (userDemand.get(planEndTimeAttr) == null  && CollectionUtils.isNotEmpty(minEndTimeList)){
                        Optional<MObject> maxEndTime =minEndTimeList.stream().
                                max(Comparator.comparing(o -> ((LocalDateTime) o.get(taskEndTimeAttr))));
                        maxEndTime.ifPresent(object -> userDemand.put(planEndTimeAttr, object.get(taskEndTimeAttr)));
                    }
                }
            }
        });
    }

    private void accordByTargetBuildLane(ApmSpaceApp apmSpaceApp, List<MSpaceAppData> sourceDatas, ApmLaneQO apmLaneQO){
        RelationMObject relationMObject = RelationMObject.builder()
                .sourceBid(apmLaneQO.getFromInstanceBid())
                .relationModelCode(apmLaneQO.getFromRelationModelCode())
                .targetModelCode(apmLaneQO.getTargetModelCode())
                .sourceModelCode(apmSpaceApp.getModelCode())
                .modelMixQo(apmLaneQO.getMixQo())
                .build();
        List<MObject> relationMObjects = objectModelCrudI.listRelationMObjects(relationMObject);
        List<String> laneRelationTargetBids = relationMObjects.stream().map(MObject::getBid).collect(Collectors.toList());
        List<MObject> sourceDataList = objectModelCrudI.listSourceMObjects(apmLaneQO.getRelationModelCode(),apmLaneQO.getSourceModelCode(),laneRelationTargetBids);
        Set<String> sourceBidSet = sourceDatas.parallelStream().map(MSpaceAppData::getBid).collect(Collectors.toSet());
        List<MSpaceAppData> laneSourceDataList = sourceDataList.stream().filter(e-> !sourceBidSet.contains(e.getBid())).map(mObject -> {
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            mSpaceAppData.putAll(mObject);
            return mSpaceAppData;
        }).collect(Collectors.toList());
        sourceDatas.addAll(laneSourceDataList);
    }

    /**
     * 获取modelCode的关系，构建删除关系的参数
     *
     * @param modelCode
     * @param bids
     * @return
     */
    private Map<String, Set<String>> getRelationDeleteParams(String modelCode, Set<String> bids) {
        Map<String, Set<String>> deleteParams = Maps.newHashMap();
        // 查询是否是关系对象，如果不是关系对象，需要删除关系对象的关联关系，批量
        CfgObjectVo cfgObjectVo = cfgObjectFeignClient.getByModelCode(modelCode).getCheckExceptionData();
        if (!ObjectTypeEnum.RELATION.getCode().equals(cfgObjectVo.getType())) {
            // 查询当前对象的关系
            List<CfgObjectRelationVo> cfgObjectRelationVos = cfgObjectRelationFeignClient.listRelationTab(modelCode).getCheckExceptionData();
            if (CollectionUtils.isNotEmpty(cfgObjectRelationVos)) {
                for (CfgObjectRelationVo cfgObjectRelationVo : cfgObjectRelationVos) {
                    // 非内置关系 且是关系对象
                    if (!cfgObjectRelationVo.isInner()) {
                        deleteParams.put(cfgObjectRelationVo.getModelCode(), bids);
                    }
                }
            }
        }
        return deleteParams;
    }

    @Override
    public List<MSpaceAppData> importExcel(String spaceAppBid, MultipartFile file,String spaceBid) {
        return appExcelTemplateService.importExcel(spaceAppBid, file,spaceBid);
    }

    @Override
    public boolean importExcelAndSave(String spaceAppBid, MultipartFile file,String spaceBid){
        return appExcelTemplateService.importExcelAndSave(spaceAppBid, file,spaceBid);
    }

    @Override
    public MSpaceAppData checkOut(String spaceAppBid, String bid) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public Map<String, MSpaceAppData> batchCheckOut(String spaceAppBid, List<String> bids) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public MSpaceAppData cancelCheckOut(String spaceAppBid, String bid) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public Map<String, MSpaceAppData> batchCancelCheckOut(String spaceAppBid, List<String> bids) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public MSpaceAppData checkIn(String spaceAppBid, MVersionObject mObject) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public List<MSpaceAppData> batchCheckin(String spaceAppBid, List<MVersionObject> list) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public Boolean saveDraft(String spaceAppBid, MVersionObject mObject) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public Boolean batchSaveDraft(String spaceAppBid, List<MVersionObject> draftList) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public List<MSpaceAppData> listByHistory(String spaceAppBid, String dataBid, QueryCondition queryCondition) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public PagedResult<MSpaceAppData> pageListByHistory(String spaceAppBid, String dataBid, BaseRequest<QueryCondition> queryCondition, boolean filterRichText) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public MSpaceAppData addTonesData(String spaceAppBid, MSpaceAppData mSpaceAppData) {
        //流程没有传角色人员信息 需要组装对应的数据
        if(mSpaceAppData.containsKey(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode())){
            if(!mSpaceAppData.containsKey(SpaceAppDataEnum.ROLE_USER.getCode())){
                String workItemType = mSpaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode())+"";
                Map<String, List<String>> roleMap = new HashMap<>(16);
                List<ApmRoleAndIdentityVo> apmRoleAndIdentityVos = apmFlowApplicationService.listFlowTemplateRoles(workItemType);
                if(CollectionUtils.isNotEmpty(apmRoleAndIdentityVos)){
                    for(ApmRoleAndIdentityVo apmRoleAndIdentityVo : apmRoleAndIdentityVos){
                        List<ApmUser> apmUserList = apmRoleAndIdentityVo.getApmUserList();
                        List<String> userIds = new ArrayList<>();
                        if(CollectionUtils.isNotEmpty(apmUserList)){
                            userIds = apmUserList.stream().map(ApmUser::getEmpNo).collect(Collectors.toList());
                        }
                        roleMap.put(apmRoleAndIdentityVo.getApmRoleVO().getBid(), userIds);
                    }
                }
                mSpaceAppData.put(SpaceAppDataEnum.ROLE_USER.getCode(), roleMap);
            }
        }
        MSpaceAppData data = add(spaceAppBid, mSpaceAppData, null);
        runtimeService.runStartNode(data.getBid(),data.getSpaceBid(),spaceAppBid);
        return data;
    }

    @Override
    public String batchAddForCad(String spaceBid, String spaceAppBid, List<MSpaceAppData> mSpaceAppDataList) {
        Assert.notEmpty(mSpaceAppDataList, "数据不能为空");
        String requestId = SnowflakeIdWorker.nextIdStr();
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
                        res = apmSpaceAppDataDrivenService.add(spaceAppBid, mSpaceAppData);
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
        return cadBatchAddInstanceRecordService.getByBid(resultId).stream().map(e -> JSON.parseObject(e.getInstanceData(), MSpaceAppData.class)).collect(Collectors.toList());
    }

    @Override
    public String batchCheckinAsync(String spaceAppBid, List<MVersionObject> list) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    @Override
    public Map<String, Integer> batchCheckinAsyncProgress(String spaceAppBid, String resultId) {
        throw new PlmBizException("常规对象暂不支持该操作");
    }

    /**
     * 批量部分更新空间应用数据（异步处理有问题）
     *
     * @param spaceBid 空间Bid
     * @param spaceAppBid 应用Bid
     * @param bids 数据Bid列表（最大限制为100）
     * @param mSpaceAppData 待更新的数据内容
     * @return Boolean 是否全部操作成功
     * @throws PlmBizException 参数校验失败、无权限或异步任务执行异常
     */
    @Override
    public Boolean batchUpdatePartialContentByBids(String spaceBid, String spaceAppBid, List<String> bids, MSpaceAppData mSpaceAppData) {
        int limitSize = 100;

        // 参数校验
        if (CollectionUtils.isEmpty(bids)) {
            throw new PlmBizException("bids不能为空");
        }
        if (mSpaceAppData == null) {
            throw new PlmBizException("待更新数据不能为空");
        }
        if (bids.size() > limitSize) {
            throw new PlmBizException("批量修改限制数量为：" + limitSize + "，当前数量为：" + bids.size());
        }

        // 是否权限校验
        if (null == mSpaceAppData.get(PermissionCheckEnum.CHECK_PERMISSION.getCode())
                || mSpaceAppData.get(PermissionCheckEnum.CHECK_PERMISSION.getCode()).equals(Boolean.TRUE)) {
            // 批量校验权限
            batchOperationCheckPermission(spaceBid, spaceAppBid, bids, OperatorEnum.EDIT.getCode());
        }
        // 获取应用信息
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
//        bids.forEach(bid -> {
//            // 执行更新操作
//            // 上面已经权限校验了。批量编辑就不需要了
//            mSpaceAppData.put(PermissionCheckEnum.CHECK_PERMISSION.getCode(), Boolean.FALSE);
//            apmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid, bid, mSpaceAppData, Boolean.TRUE);
//        });

        CfgViewVo view = iAmSpaceAppConfigManageService.baseViewGet(spaceAppBid);
//        List<CfgViewMetaDto> cfgViewMetaList = apmSpaceAppConfigDrivenService.baseViewGetMeteModels(spaceAppBid);
//        Map<String,String> cfgViewMetaTypeMap = cfgViewMetaList.stream().collect(Collectors.toMap(CfgViewMetaDto::getName,CfgViewMetaDto::getType,(k1,k2)->k1));

//        //处理特殊字段(时间默认为系统当前时间)
//        for(Map.Entry<String,Object> entry:mSpaceAppData.entrySet()){
//            String filedType = cfgViewMetaTypeMap.get(entry.getKey());
//            if(StringUtils.isNotEmpty(filedType) && filedType.equals(ViewComponentEnum.DATE.getType()) && "1970-01-01".equals(entry.getValue()+"")){
//                mSpaceAppData.put(entry.getKey(), new Date());
//            }
//        }

        MSpaceAppData logSpaceAppData = new MSpaceAppData();
        logSpaceAppData.putAll(mSpaceAppData);
        // 更新之前需要获取旧数据，方便日志记录
        List<MObject> oldDataList = listByBids(app.getModelCode(), Sets.newHashSet(bids));

        // 执行更新
        objectModelCrudI.batchUpdatePartialContentByIds(app.getModelCode(), mSpaceAppData, bids);

        String createdBy = SsoHelper.getJobNumber();
//        // 异步并发执行更新操作
//        List<CompletableFuture<Void>> updateFutures = bids.stream()
//                .map(bid -> CompletableFuture.runAsync(() -> {
//                    // 上面已经权限校验了。批量编辑就不需要了
//                    mSpaceAppData.put(PermissionCheckEnum.CHECK_PERMISSION.getCode(), Boolean.FALSE);
//                    // 执行更新操作
//                    apmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid, bid, mSpaceAppData, Boolean.TRUE);
//                }))
//                .collect(Collectors.toList());
//
//        // 等待所有更新完成
//        try {
//            CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[0])).get();
//        } catch (InterruptedException | ExecutionException e) {
//            Thread.currentThread().interrupt();
//            throw new PlmBizException("部分数据更新失败", e);
//        }


        // 日志批处理
        // 异步 fire-and-forget 处理日志记录
        batchHandleUpdateLog(spaceBid, spaceAppBid, bids, logSpaceAppData, view, app, createdBy, oldDataList);
        return Boolean.TRUE;
    }

    private void batchHandleUpdateLog(String spaceBid, String spaceAppBid, List<String> bids, MSpaceAppData mSpaceAppData, CfgViewVo view, ApmSpaceApp app, String createdBy, List<MObject> oldDataList) {
        // 把旧数据 转换为map，key为bid，然后传递到 operationLogEsService.save(operationLogAddParam, null); 的null中
        // 1️⃣ 将 oldDataList 转换为 Map<bid, MObject>
        Map<String, MObject> oldDataMap = oldDataList.stream()
                .collect(Collectors.toMap(
                        MObject::getBid,  // 假设 getBid() 是唯一标识符
                        Function.identity(),
                        (existing, replacement) -> existing // 如果有重复 bid，保留第一个
                ));
        bids.forEach(bid -> {
            SimpleThreadPool.getInstance().execute(() -> {
                try {
                    Map<String, Object> forMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
                    forMap.putAll(mSpaceAppData);
                    forMap.put(RelationEnum.BID.getCode(), bid);
                    forMap.put(SPACE_BID.getCode(), spaceBid);

                    // 获取旧数据对象
                    MObject oldData = oldDataMap.get(bid);

                    // 日志异步处理
                    List<CfgViewMetaDto> cfgViewMetaList = Optional.ofNullable(view)
                            .map(CfgViewVo::getMetaList)
                            .orElse(Collections.emptyList());
                    for (Map.Entry<String, Object> entry : forMap.entrySet()) {
                        String keyName = entry.getKey();
                        CfgViewMetaDto cfgViewMetaDto = cfgViewMetaList.stream()
                                .filter(cfgViewMeta -> cfgViewMeta.getName().equals(keyName))
                                .findFirst()
                                .orElse(CfgViewMetaDto.builder().build());
                        Map<String, Object> properties = apmSpaceAppConfigDrivenService.getProperties(view.getContent(), keyName);
                        OperationLogAddParam operationLogAddParam = OperationLogAddParam.builder()
                                .modelCode(app.getModelCode())
                                .cfgViewMetaDto(cfgViewMetaDto)
                                .properties(properties)
                                .instanceBid(bid)
                                .fieldName(keyName)
                                .fieldValue(entry.getValue())
                                .isAppView(true)
                                .spaceAppBid(spaceAppBid)
                                .spaceBid(spaceBid)
                                .createdBy(createdBy) // createdBy 是外部传入的变量
                                .build();

                        // 日志异步提交（无返回值）
                        operationLogEsService.save(operationLogAddParam, oldData);

                    }
                } catch (Exception e) {
                    log.error("更新数据Bid[{}]，记录日志失败", bid, e);
                }
            });
        });
    }

    /**
     * 批量检查编辑权限
     *
     * @param spaceBid       空间Bid
     * @param spaceAppBid    应用Bid
     * @param bids           待更新的数据Bid列表
     * @param button         权限按钮
     * @return 无权限的bid集合
     */
    private Set<String> batchCheckEditPermission(String spaceBid, String spaceAppBid, List<String> bids, String button) {
        Set<String> noPermissionBidSet = new ConcurrentHashSet<>();

        if (ApmImplicitParameter.isSkipCheckPermission()) {
            return noPermissionBidSet; // 跳过权限检查
        }

        for (String bid : bids) {
            PermissionCheckDto permissionCheckDto = new PermissionCheckDto();
            permissionCheckDto.setSpaceBid(spaceBid)
                    .setSpaceAppBid(spaceAppBid)
                    .setInstanceBid(bid)
                    .setOperatorCode(button);

            if (!permissionCheckService.checkSpaceAppPermssion(permissionCheckDto)) {
                noPermissionBidSet.add(bid);
            }
        }

        return noPermissionBidSet;
    }

    private Set<String> batchCheckEditPermissionAsync(String spaceBid, String spaceAppBid, List<String> bids, String operatorCode, Executor executor) {
        Set<String> noPermissionBidSet = Collections.synchronizedSet(new HashSet<>());

        List<CompletableFuture<Void>> futures = bids.stream()
                .map(bid -> CompletableFuture.runAsync(() -> {
                    PermissionCheckDto permissionCheckDto = new PermissionCheckDto();
                    permissionCheckDto.setSpaceBid(spaceBid)
                            .setSpaceAppBid(spaceAppBid)
                            .setInstanceBid(bid)
                            .setOperatorCode(operatorCode);

                    if (!permissionCheckService.checkSpaceAppPermssion(permissionCheckDto)) {
                        noPermissionBidSet.add(bid);
                    }
                }, executor))
                .collect(Collectors.toList());

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new PlmBizException("异步权限校验失败", e);
        }

        return noPermissionBidSet;
    }

    /**
     * 查询无权限对象的详细信息（主要是名称）
     *
     * @param modelCode 模型编码
     * @param bids      无权限的bid集合
     * @return 对象列表
     */
    private List<MObject> listByBids(String modelCode, Collection<String> bids) {
        if (CollectionUtils.isEmpty(bids)) {
            return Collections.emptyList();
        }

        QueryWrapper qo = new QueryWrapper();
        qo.in(BaseDataEnum.BID.getCode(), bids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);

        return objectModelCrudI.list(modelCode, queryWrappers);
    }

    /**
     *  查询父对象信息
     *
     * @param modelCode 模型编码
     * @param parentBids 父对象bid集合
     * @return 对象列表
     */
    private List<MObject> listByParentBids(String modelCode, Collection<String> parentBids) {
        if (CollectionUtils.isEmpty(parentBids)) {
            return Collections.emptyList();
        }

        QueryWrapper qo = new QueryWrapper();
        qo.in(ObjectTreeEnum.PARENT_BID.getCode(), parentBids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);

        return objectModelCrudI.list(modelCode, queryWrappers);
    }


    @Override
    public Boolean multiObjectPartialContent(String spaceAppBid, List<MultiObjectUpdateDto> multiObjectUpdateDtoList) {
        CountDownLatch countDownLatch = new CountDownLatch(multiObjectUpdateDtoList.size());
        for (MultiObjectUpdateDto multiObjectUpdateDto : multiObjectUpdateDtoList) {
            SimpleThreadPool.getInstance().execute(() -> {
                try {
                    objectModelCrudI.batchUpdatePartialContentByIds(multiObjectUpdateDto.getModelCode(), multiObjectUpdateDto.getData(), multiObjectUpdateDto.getBids());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        //等待所有线程执行完毕
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
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
        // 获取应用信息
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);

        // 权限校验
        Set<String> noPermissionBidSet = batchCheckEditPermissionAsync(spaceBid, spaceAppBid, bids, operation, SimpleThreadPool.getInstance());

        // 如果没有权限，抛出异常并附带名称
        if (!noPermissionBidSet.isEmpty()) {
            List<MObject> oldDataList = listByBids(app.getModelCode(), noPermissionBidSet);
            Map<String, String> bidNameMap = oldDataList.stream()
                    .collect(Collectors.toMap(
                            MObject::getCoding,
                            MObject::getName,
                            (oldValue, newValue) -> oldValue // 保留第一个出现的值
                    ));

            String errorMsg = bidNameMap.entrySet().stream()
                    .map(entry -> "[编码为: " + entry.getKey() + ", 名称为: " + entry.getValue() + "]")
                    .collect(Collectors.joining("; "));

            throw new PlmBizException("以下数据无操作权限: " + errorMsg);
        }
        return Boolean.TRUE;
    }

    /**
     * 批量逻辑删除(根据视图模式)
     *
     * @param viewModel   视图模式
     * @param spaceAppBid spaceAppBid
     * @param bids        要删除的对象bid列表
     * @return 是否删除成功
     */
    @Override
    public Boolean batchViewModelLogicalDeleteByBids(String viewModel, String spaceAppBid, List<String> bids) {
        // 如果是树结构视图，需要递归查找所有子节点进行删除
        if (AppViewModelEnum.TREE_VIEW.getCode().equals(viewModel)) {
            Set<String> allBidsToDelete = new LinkedHashSet<>(bids); // 保留顺序 + 去重
            Set<String> currentParentBids = new HashSet<>(bids);
            int depth = 0;
            final int MAX_DEPTH = 10;

            // 获取应用信息
            ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
            String modelCode = app.getModelCode();

            while (!currentParentBids.isEmpty() && depth < MAX_DEPTH) {
                List<MObject> children = listByParentBids(modelCode, new ArrayList<>(currentParentBids));
                if (CollectionUtils.isEmpty(children)) {
                    break;
                }

                // 提取子节点 bid 并过滤掉已经包含的
                Set<String> childBids = children.stream()
                        .map(MObject::getBid)
                        .filter(bid -> !allBidsToDelete.contains(bid))
                        .collect(Collectors.toSet());

                allBidsToDelete.addAll(childBids);
                currentParentBids = childBids;
                depth++;
            }

            bids = new ArrayList<>(allBidsToDelete);
        }

        return batchLogicalDeleteByBids(spaceAppBid, bids);
    }


}
