package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.constants.SpaceConstant;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmRoleQO;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmSpaceRoleQO;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowApplicationService;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.log.model.dto.GenericLogAddParam;
import com.transcend.plm.datadriven.apm.mapstruct.ApmRoleConverter;
import com.transcend.plm.datadriven.apm.mapstruct.ApmSpaceAppConverter;
import com.transcend.plm.datadriven.apm.permission.configcenter.SysRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmSphereAO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmSphereCopyAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.*;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleAccessService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.*;
import com.transcend.plm.datadriven.apm.permission.service.impl.PlatformUserWrapper;
import com.transcend.plm.datadriven.apm.space.converter.ApmSpaceConverter;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.pojo.bo.ApmSpaceViewPermissionBo;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppAo;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppQueryDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppViewModelCopyDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppAccessVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppQueryVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpace;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmUserSpace;
import com.transcend.plm.datadriven.apm.space.repository.service.*;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigDrivenService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.apm.space.utils.RelationUtils;
import com.transcend.plm.datadriven.apm.springframework.config.GlobalRoleProperties;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import com.transsion.framework.uac.model.dto.DepartmentDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.*;
import static com.transcend.plm.datadriven.apm.enums.TypeEnum.OBJECT;
import static com.transcend.plm.datadriven.apm.enums.TypeEnum.SPACE;

/**
 * 空间应用service
 *
 * @author yuanhu.huang
 */
@Service
@Slf4j
public class ApmSpaceApplicationServiceImpl implements ApmSpaceApplicationService {

    private static final String SPACE_APP_CACHE_NAME = "apmSpaceApp";
    @Resource
    private OperationLogEsService operationLogEsService;
    @Resource
    private ApmSpaceService apmSpaceService;
    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private ApmUserSpaceService apmUserSpaceService;
    @Resource
    private ISphereDomainService sphereDomainService;
    @Resource
    private ApmRoleIdentityService apmRoleIdentityService;
    @Resource
    private ApmRoleService apmRoleService;
    @Resource
    private IApmRoleDomainService apmRoleDomainService;
    @Resource
    private ApmSphereService apmSphereService;
    @Resource
    private ApmRoleAccessService apmRoleAccessService;
    @Resource
    private PlatformUserWrapper platformUserWrapper;
    @Resource
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;
    @Resource
    private IApmSpaceAppConfigDrivenService apmSpaceAppConfigDrivenService;
    @Resource
    private ApmFlowApplicationService apmFlowApplicationService;
    @Resource
    private ApmSpaceAppViewModelService apmSpaceAppViewModelService;
    @Resource
    private ApmSpaceAppTabService apmSpaceAppTabService;
    @Resource
    private ApmAppEventService apmAppEventService;

    @Resource
    private GlobalRoleProperties globalRoleProperties;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IAppDataService appDataService;
    @Resource
    private IPermissionConfigService permissionConfigService;

    @Resource
    private IApmRoleIdentityDomainService apmRoleIdentityDomainService;

    @Autowired
    private SysRoleDomainService sysRoleDomainService;
    @Autowired
    private IPermissionOperationService iPermissionOperationService;


    @Value("#{'${transcend.plm.apm.batchImport.spaceAppBids:1111}'.split(',')}")
    private List<String> batchImportSpaceAppBids;

    @Value("#{'${transcend.plm.apm.batchImport.modelCodes:1111}'.split(',')}")
    private List<String> batchImportModelCodes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveApmSpace(ApmSpaceDto apmSpaceDto) {
        // 需要处理为 整个实例的复制，另外当前空间，当前唯一的实例都是和空间bid一致的复制 TODO @LUOJIE
        ApmSpace apmSpace = getApmSpace(apmSpaceDto);
        ApmSphereAO apmSphereAo = new ApmSphereAO();
        apmSphereAo.setType(SPACE.getCode());
        apmSphereAo.setBizBid(apmSpace.getBid());
        apmSphereAo.setName(apmSpaceDto.getName());
        // 保存域信息
        ApmSphere sphere = sphereDomainService.add(apmSphereAo);
        // 保存空间信息
        apmSpace.setSphereBid(sphere.getBid());
        // 复制默认空间配置信息
        copyDefaultSpaceAppInfo(apmSpace, sphere);
        // 是否初始化实例本身 ture:是 false:否 （因为需求管理创建空间项目时，会初始化应用等配置,但不需要初始化实例）
        if (!Boolean.TRUE.equals(apmSpaceDto.getInitSelfFlag())) {
            apmSpaceService.save(apmSpace);
        } else {
            // 遗漏的更新，会导致一系列错误（如 空间角色复制等）
            apmSpaceService.updateSphereBid(apmSpace.getBid(), apmSpace.getSphereBid());
        }

        // 创建空间完成后创建默认角色
        return apmRoleService.createDefaultRole(apmSpace.getName(), sphere.getBid());
    }

    @Override
    public Map<String, String> getSpaceBySpaceAppBids(List<String> spaceAppBids) {
        List<ApmSpaceAppVo> apmSpaceAppVos = apmSpaceAppService.listSpaceInfo(spaceAppBids);
        Map<String, String> spaceMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (CollectionUtils.isNotEmpty(apmSpaceAppVos)) {
            spaceMap = apmSpaceAppVos.stream().collect(Collectors.toMap(ApmSpaceAppVo::getBid, ApmSpaceAppVo::getSpaceBid));
        }
        return spaceMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copySpaceAppInfo(String copySpaceAppBid, String nowSpaceBid) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(copySpaceAppBid);
        if (apmSpaceApp == null) {
            throw new BusinessException("空间应用不存在");
        }
        //判断是否重复创建
        ApmSpaceApp oldSpaceApp = apmSpaceAppService.getBySpaceBidAndModelCode(nowSpaceBid, apmSpaceApp.getModelCode());
        if (oldSpaceApp != null) {
            throw new BusinessException("该空间已存在该应用");
        }
        List<ApmSpaceAppViewModelCopyDto> apmSpaceAppViewModelCopyDtos = new ArrayList<>();
        String oldSpaceBid = apmSpaceApp.getSpaceBid();
        Map<String, String> appBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        ApmSpaceAppViewModelCopyDto apmSpaceAppViewModelCopyDto = new ApmSpaceAppViewModelCopyDto();
        apmSpaceAppViewModelCopyDto.setOldSpaceAppBid(apmSpaceApp.getBid());
        apmSpaceAppViewModelCopyDto.setNewSpaceBid(nowSpaceBid);
        String bid = SnowflakeIdWorker.nextIdStr();
        apmSpaceAppViewModelCopyDto.setNewSpaceAppBid(bid);
        appBidMap.put(apmSpaceApp.getBid(), bid);
        // 概览视图
        appBidMap.put(apmSpaceApp.getBid() + "#" + "overview", bid + "#" + "overview");
        apmSpaceAppViewModelCopyDtos.add(apmSpaceAppViewModelCopyDto);
        apmSpaceApp.setSpaceBid(nowSpaceBid);
        apmSpaceApp.setBid(bid);
        apmSpaceApp.setId(null);
        apmSpaceApp.setCreatedTime(new Date());
        apmSpaceApp.setUpdatedTime(new Date());
        //拷贝域
        List<Map<String, String>> sphereBidMapList = sphereDomainService.copySpaceAppSphere(copySpaceAppBid, nowSpaceBid, bid);
        Map<String, String> sphereBidMap = sphereBidMapList.get(0);
        Map<String, String> sphereBidOldAndNewMap = sphereBidMapList.get(1);
        apmSpaceApp.setSphereBid(sphereBidMap.get(apmSpaceApp.getBid()));
        Map<String, String> oldNewAppIdMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        oldNewAppIdMap.put(copySpaceAppBid, bid);
        //拷贝应用
        apmSpaceAppService.save(apmSpaceApp);
        //拷贝视图
        apmSpaceAppConfigManageService.copyViews(appBidMap, null);
        //拷贝apm_space_app_view_model
        apmSpaceAppViewModelService.copyApmSpaceAppViewModel(appBidMap);
        //复制应用的角色
        Set<String> notInCodes = SpaceConstant.DEFAULT_ROLE_SET;
        Map<String, String> roleBidMap = apmRoleService.copyRoles(sphereBidOldAndNewMap, notInCodes);
        //复制应用的流程
        apmFlowApplicationService.copyFlow(appBidMap, roleBidMap);
        //复制apm_app_event
        Map<String, String> spaceBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        spaceBidMap.put(oldSpaceBid, nowSpaceBid);
        apmAppEventService.copyApmAppEvent(appBidMap, spaceBidMap);
        //拷贝操作
        apmSpaceAppConfigDrivenService.copyAccess(appBidMap, roleBidMap, sphereBidOldAndNewMap);
        //复制空间应用tab配置信息
        apmSpaceAppTabService.copyApmSpaceAbbTab(spaceBidMap, appBidMap, sphereBidOldAndNewMap, nowSpaceBid);
        //拷贝权限
        copyAppPermission(null, oldNewAppIdMap);
        return true;
    }

    private void copyDefaultSpaceAppInfo(ApmSpace apmSpace, ApmSphere sphere) {
        String templateBid = apmSpace.getTemplateBid();
        if (StringUtil.isBlank(templateBid)) {
            //没有空间模板 不需要复制
            return;
        }
        //带出默认空间相关信息
        ApmSpace apmSpaceTemplate = apmSpaceService.getOne(
                Wrappers.<ApmSpace>lambdaQuery()
                        .eq(ApmSpace::getTemplateFlag, Boolean.TRUE)
                        .eq(ApmSpace::getBid, templateBid)
                , Boolean.FALSE
        );
        Map<String, String> spaceBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        spaceBidMap.put(apmSpaceTemplate.getBid(), apmSpace.getBid());
        //查空间应用信息
        List<ApmSpaceApp> apmSpaceApps = apmSpaceAppService.list(Wrappers.<ApmSpaceApp>lambdaQuery().eq(ApmSpaceApp::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED).eq(ApmSpaceApp::getSpaceBid, apmSpaceTemplate.getBid()));
        if (CollectionUtils.isEmpty(apmSpaceApps)) {
            return;
        }
        Map<String, String> appBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        //复制空间应用数据收集
        List<ApmSpaceAppViewModelCopyDto> apmSpaceAppViewModelCopyDtos = new ArrayList<>();
        for (ApmSpaceApp apmSpaceApp : apmSpaceApps) {
            ApmSpaceAppViewModelCopyDto apmSpaceAppViewModelCopyDto = new ApmSpaceAppViewModelCopyDto();
            apmSpaceAppViewModelCopyDto.setOldSpaceBid(apmSpace.getTemplateBid());
            apmSpaceAppViewModelCopyDto.setOldSpaceAppBid(apmSpaceApp.getBid());
            apmSpaceAppViewModelCopyDto.setNewSpaceBid(apmSpace.getBid());
            apmSpaceAppViewModelCopyDto.setModelCode(apmSpaceApp.getModelCode());
            apmSpaceAppViewModelCopyDto.setCopyInstanceModel(apmSpaceApp.getCopyInstanceModel());
            apmSpaceAppViewModelCopyDto.setOpenModel(apmSpaceApp.getOpenModel());

            String bid = SnowflakeIdWorker.nextIdStr();
            apmSpaceAppViewModelCopyDto.setNewSpaceAppBid(bid);
            appBidMap.put(apmSpaceApp.getBid(), bid);
            // 概览视图
            appBidMap.put(apmSpaceApp.getBid() + "#" + "overview", bid + "#" + "overview");
            apmSpaceAppViewModelCopyDtos.add(apmSpaceAppViewModelCopyDto);
            apmSpaceApp.setSpaceBid(apmSpace.getBid());
            apmSpaceApp.setBid(bid);
            apmSpaceApp.setId(null);
            apmSpaceApp.setCreatedTime(new Date());
            apmSpaceApp.setUpdatedTime(new Date());
            if (Objects.isNull(apmSpaceApp.getConfigContent())) {
                apmSpaceApp.setConfigContent(initSortConfig(new JSONObject(), apmSpaceApp.getModelCode()));
            }
        }
        // 处理拷贝应用的实例信息
        copySpaceAppInstance(apmSpaceAppViewModelCopyDtos);
        // 复制空间应用按钮配置信息
        copySpaceAppButton(apmSpaceAppViewModelCopyDtos);
        //拷贝域
        ApmSphereCopyAO apmSphereCopyAO = new ApmSphereCopyAO();
        apmSphereCopyAO.setPbid(sphere.getBid());
        apmSphereCopyAO.setAppBidMap(appBidMap);
        List<Map<String, String>> sphereBidMapList = sphereDomainService.copySphere(apmSphereCopyAO);
        Map<String, String> sphereBidMap = sphereBidMapList.get(0);
        Map<String, String> sphereBidOldAndNewMap = sphereBidMapList.get(1);
        sphereBidOldAndNewMap.put(apmSpaceTemplate.getSphereBid(), sphere.getBid());
        for (ApmSpaceApp apmSpaceApp : apmSpaceApps) {
            apmSpaceApp.setSphereBid(sphereBidMap.get(apmSpaceApp.getBid()));
        }
        //拷贝应用
        apmSpaceAppService.saveBatch(apmSpaceApps);
        //拷贝视图 拷贝apm_space_app_view_model
        apmSpaceAppConfigManageService.copyViews(appBidMap, null);
        apmSpaceAppViewModelService.copyApmSpaceAppViewModel(appBidMap);
        //复制空间以及各个应用的角色
        Set<String> notInCodes = SpaceConstant.DEFAULT_ROLE_SET;
        // 复制角色有问题？ TODO
        Map<String, String> roleBidMap = apmRoleService.copyRoles(sphereBidOldAndNewMap, notInCodes);
        // 复制对象权限和应用权限
        copyAppPermission(apmSpaceApps, appBidMap);


        // 1.1查询空间下对象的权限
        // 1.2查询空间下应用的权限
        // 将对象权限和应用权限复制到当前空间下

        //复制各个应用的流程
        apmFlowApplicationService.copyFlow(appBidMap, roleBidMap);

        //复制空间应用tab配置信息
        apmSpaceAppTabService.copyApmSpaceAbbTabs(spaceBidMap, appBidMap, sphereBidOldAndNewMap);
        //复制自定义视图 TODO
        //复制计划任务
//        copyPlanTask(apmSpace.getBid(),appBidMap,apmSpaceTemplate.getBid());
        //复制apm_app_event
        apmAppEventService.copyApmAppEvent(appBidMap, spaceBidMap);
        //拷贝操作
        apmSpaceAppConfigDrivenService.copyAccess(appBidMap, roleBidMap, sphereBidOldAndNewMap);
    }

    /**
     * 复制空间应用按钮信息
     *
     * @param apmSpaceAppViewModelCopyDtos 空间应用视图模型复制数据传输对象列表
     */
    private void copySpaceAppButton(List<ApmSpaceAppViewModelCopyDto> apmSpaceAppViewModelCopyDtos) {
        if (CollectionUtils.isEmpty(apmSpaceAppViewModelCopyDtos)) {
            return;
        }

        apmSpaceAppViewModelCopyDtos.
                forEach(dto -> {
                    try {
                        // 复制空间应用按钮信息
                        iPermissionOperationService.copyBySpaceAppBid(
                                dto.getOldSpaceAppBid(),
                                dto.getNewSpaceAppBid()
                        );
                    } catch (Exception e) {
                        log.error("Failed to copy space app from {} to {}: {}",
                                dto.getOldSpaceAppBid(), dto.getNewSpaceAppBid(), e.getMessage());
                    }
                });
    }

    /**
     * 复制空间应用实例信息
     *
     * @param apmSpaceAppViewModelCopyDtos 空间应用视图模型复制数据传输对象列表
     */
    private void copySpaceAppInstance(List<ApmSpaceAppViewModelCopyDto> apmSpaceAppViewModelCopyDtos) {
        // 不存在空间应用实例信息，则直接返回
        if (CollectionUtils.isEmpty(apmSpaceAppViewModelCopyDtos)) {
            return;
        }
        for (ApmSpaceAppViewModelCopyDto apmSpaceAppViewModelCopyDto : apmSpaceAppViewModelCopyDtos) {
            String copyInstanceModel = apmSpaceAppViewModelCopyDto.getCopyInstanceModel();
            // 如果不复制实例，则跳过
            if (StringUtil.isBlank(copyInstanceModel)) {
                continue;
            }
            // 处理实例复制方法
            doCopySpaceAppInstance(apmSpaceAppViewModelCopyDto);
        }
        // 埋点，由上层实现，此处通过接口调用，由上层实现 TODO 入参是 新空间spaceBid
    }

    /**
     * 复制空间应用实例从源空间到目标空间
     *
     * @param copyDto 包含复制所需信息的数据传输对象
     */
    private void doCopySpaceAppInstance(ApmSpaceAppViewModelCopyDto copyDto) {
        // 提取必要参数
        final String sourceSpaceBid = copyDto.getOldSpaceBid();
        final String targetSpaceBid = copyDto.getNewSpaceBid();
        final String targetSpaceAppBid = copyDto.getNewSpaceAppBid();
        final String modelCode = copyDto.getModelCode();
        final String copyInstanceModel = copyDto.getCopyInstanceModel();

        // 记录开始复制的日志
        log.info("开始复制空间应用实例, 从空间 {} 到空间 {}, 模型: {}",
                sourceSpaceBid, targetSpaceBid, modelCode);

        try {
            // 1. 查询源空间的实例信息
            List<MObject> sourceObjects = fetchSourceObjects(sourceSpaceBid, modelCode);

            if (CollectionUtils.isEmpty(sourceObjects)) {
                log.info("源空间 {} 没有找到模型 {} 的实例数据", sourceSpaceBid, modelCode);
                return;
            }

            // 2. 准备目标空间的实例数据
            //
            List<MObject> targetObjects = null;
            List<MObject> sourceRelations = null;
            // 如果是复制树结构(未来可以做策略模式，不同的复制类型，处理方式不一样)，需要处理parentBid
            String relationModelCode = "A0V";
            if (SpaceConstant.APP_COPY_MODEL_TREE_INSTANCE.equals(copyInstanceModel)) {
                targetObjects = prepareTreeTargetObjects(
                        sourceObjects, targetSpaceBid, targetSpaceAppBid, copyInstanceModel);
                // TODO 还需要复制关系被挂在那个实例上。 复制 WBS ：A0S的情况，关系 开发管理-WBS：A0V 后续考虑通用
                if (modelCode.equals("A0S")) {
                    sourceRelations = targetObjects.stream().map(
                            sourceObject ->
                                    RelationUtils.collectRelationPo(relationModelCode,
                                            targetSpaceBid, targetSpaceBid, sourceObject.getBid(), sourceObject.getBid(), targetSpaceBid, SsoHelper.getTenantId()
                                    )
                    ).collect(Collectors.toList());
                }

            } else {
                targetObjects = prepareTargetObjects(
                        sourceObjects, targetSpaceBid, targetSpaceAppBid, copyInstanceModel);
            }

            // 3. 批量保存到目标空间
            appDataService.addBatch(modelCode, targetObjects, true);
            appDataService.addBatch(relationModelCode, sourceRelations, true);

            log.info("成功复制 {} 个实例从空间 {} 到空间 {}",
                    targetObjects, sourceSpaceBid, targetSpaceBid);
        } catch (Exception e) {
            log.error("复制空间应用实例失败: {}", e.getMessage(), e);
            throw new TranscendBizException("复制空间应用实例失败", e);
        }
    }

    /**
     * 获取源空间的对象实例
     */
    private List<MObject> fetchSourceObjects(String sourceSpaceBid, String modelCode) {
        QueryWrapper query = new QueryWrapper();
        query.eq(RelationEnum.SPACE_BID.getColumn(), sourceSpaceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(query);

        // 如果数据量大，这里可以考虑分页查询
        return objectModelCrudI.listByQueryWrapper(modelCode, queryWrappers);
    }

    /**
     * 准备目标空间的对象实例
     */
    private List<MObject> prepareTargetObjects(
            List<MObject> sourceObjects,
            String targetSpaceBid,
            String targetSpaceAppBid,
            String copyInstanceModel) {

        return sourceObjects.stream()
                .map(source -> {
                    MObject target = new MObject(); // 假设MObject有clone方法，否则需要深拷贝
                    target.putAll(source);

                    target.setId(null);

                    // 确定新的BID
                    String newBid;
                    if (SpaceConstant.APP_COPY_MODEL_SPACE_INSTANCE.equals(copyInstanceModel)) {
                        newBid = targetSpaceBid;
                    } else {
                        newBid = SnowflakeIdWorker.nextIdStr();
                    }

                    // 更新目标对象的关键字段
                    target.setBid(newBid);
                    target.put(TranscendModelBaseFields.DATA_BID, newBid);
                    target.put(TranscendModelBaseFields.SPACE_BID, targetSpaceBid);
                    target.put(TranscendModelBaseFields.SPACE_APP_BID, targetSpaceAppBid);

                    return target;
                })
                .collect(Collectors.toList());
    }

    /**
     * 准备目标空间的对象实例
     */
    private List<MObject> prepareTreeTargetObjects(
            List<MObject> sourceObjects,
            String targetSpaceBid,
            String targetSpaceAppBid,
            String copyInstanceModel) {

        // 创建一个映射，用于记录旧的BID与新的BID的对应关系（用于树结构父子关系处理）
        Map<Object, Object> bidMapping = new HashMap<>();

        return sourceObjects.stream()
                .map(source -> {
                    MObject target = new MObject(); // 假设MObject有clone方法，否则需要深拷贝
                    target.putAll(source);

                    target.setId(null);

                    // 确定新的BID
                    String newBid = SnowflakeIdWorker.nextIdStr();
                    bidMapping.put(source.getBid(), newBid); // 记录旧BID与新BID的映射

                    // 更新目标对象的关键字段
                    target.setBid(newBid);
                    target.put(TranscendModelBaseFields.DATA_BID, newBid);
                    target.put(TranscendModelBaseFields.SPACE_BID, targetSpaceBid);
                    target.put(TranscendModelBaseFields.SPACE_APP_BID, targetSpaceAppBid);

                    // 如果是树结构复制模式，需要处理parentBid
                    if (SpaceConstant.APP_COPY_MODEL_TREE_INSTANCE.equals(copyInstanceModel)) {
                        Object oldParentBid = source.get(TranscendModelBaseFields.PARENT_BID); // 假设MObject有getParentBid方法
                        if (oldParentBid != null) {
                            // 查找映射表中的新parentBid
                            Object newParentBid = bidMapping.get(oldParentBid);
                            target.put(TranscendModelBaseFields.PARENT_BID, newParentBid);
                        } else {
                            // 如果没有父节点，说明是根节点，parentBid可以设置为null或其他默认值
                            target.put(TranscendModelBaseFields.PARENT_BID, null);
                        }
                    }

                    return target;
                })
                .collect(Collectors.toList());
    }


    private void copyAppPermission(List<ApmSpaceApp> apmSpaceApps, Map<String, String> appBidMap) {
        // 根据应用bid去查询 权限规则明细表(新增)，权限规则明细表(列表)，权限规则表，权限规则操作明细表'
        // 需要重新生成permissionBid，应用ID,角色ID
        // 权限规则表
        List<String> sourceAppBids = new ArrayList<>(appBidMap.keySet());
        List<PermissionPlmRule> permissionPlmRules = permissionConfigService.getPermissionPlmRuleByAppBids(sourceAppBids);
        List<PermissionPlmAddRuleItem> permissionPlmAddRuleItems = permissionConfigService.getPermissionPlmAddRuleItemByAppBids(sourceAppBids);
        List<PermissionPlmListRuleItem> permissionPlmListRuleItems = permissionConfigService.getPermissionPlmListRuleItemByAppBids(sourceAppBids);
        List<PermissionPlmRuleCondition> permissionPlmRuleConditionList = permissionConfigService.getPermissionPlmRuleConditionByAppBids(sourceAppBids);
        List<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItems = permissionConfigService.getPermissionPlmOperationRuleItemByAppBids(sourceAppBids);
        String jobNumber = SsoHelper.getJobNumber();
        //保存
        List<PermissionPlmRule> permissionPlmRuleNews = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(permissionPlmRules)) {
            for (PermissionPlmRule permissionPlmRule : permissionPlmRules) {
                String permissionBid = permissionPlmRule.getBid();
                String oldAppBid = "";
                if (permissionBid.startsWith("APP:")) {
                    oldAppBid = permissionBid.replace("APP:", "");
                } else if (permissionBid.startsWith("LC:")) {
                    oldAppBid = permissionBid.split(":")[1];
                }
                PermissionPlmRule permissionPlmRule1 = new PermissionPlmRule();
                //将bid替换
                permissionPlmRule1.setBid(permissionPlmRule.getBid().replace(oldAppBid, appBidMap.get(oldAppBid)));
                permissionPlmRule1.setPath(permissionPlmRule.getPath().replace(oldAppBid, appBidMap.get(oldAppBid)));
                permissionPlmRule1.setRuleName(permissionPlmRule.getRuleName());
                permissionPlmRule1.setDescription(permissionPlmRule.getDescription());
                permissionPlmRule1.setEnableFlag(permissionPlmRule.getEnableFlag());
                permissionPlmRule1.setCreatedTime(new Date());
                permissionPlmRule1.setUpdatedTime(new Date());
                permissionPlmRule1.setCreatedBy(jobNumber);
                permissionPlmRule1.setUpdatedBy(jobNumber);
                permissionPlmRuleNews.add(permissionPlmRule1);
            }
        }

        List<PermissionPlmAddRuleItem> permissionPlmAddRuleItemNews = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(permissionPlmAddRuleItems)) {
            for (PermissionPlmAddRuleItem permissionPlmAddRuleItem : permissionPlmAddRuleItems) {
                PermissionPlmAddRuleItem permissionPlmAddRuleItem1 = new PermissionPlmAddRuleItem();
                String permissionBid = permissionPlmAddRuleItem.getPermissionBid();
                String oldAppBid = "";
                if (permissionBid.startsWith("APP:")) {
                    oldAppBid = permissionBid.replace("APP:", "");
                } else if (permissionBid.startsWith("LC:")) {
                    oldAppBid = permissionBid.split(":")[1];
                }
                String oldRoleCode = permissionPlmAddRuleItem.getRoleCode();
                String bid = SnowflakeIdWorker.nextIdStr();
                permissionPlmAddRuleItem1.setBid(bid);
                permissionPlmAddRuleItem1.setPermissionBid(permissionPlmAddRuleItem.getPermissionBid().replace(oldAppBid, appBidMap.get(oldAppBid)));
                permissionPlmAddRuleItem1.setPath(permissionPlmAddRuleItem.getPath().replace(oldAppBid, appBidMap.get(oldAppBid)));
                // 设置角色code 角色code不会变
                permissionPlmAddRuleItem1.setRoleCode(permissionPlmAddRuleItem.getRoleCode());
                permissionPlmAddRuleItem1.setEnableFlag(permissionPlmAddRuleItem.getEnableFlag());
                permissionPlmAddRuleItem1.setCreatedTime(new Date());
                permissionPlmAddRuleItem1.setUpdatedTime(new Date());
                permissionPlmAddRuleItem1.setCreatedBy(jobNumber);
                permissionPlmAddRuleItem1.setUpdatedBy(jobNumber);
                permissionPlmAddRuleItemNews.add(permissionPlmAddRuleItem1);

            }
        }
        List<PermissionPlmListRuleItem> permissionPlmListRuleItemNews = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(permissionPlmListRuleItems)) {
            for (PermissionPlmListRuleItem permissionPlmListRuleItem : permissionPlmListRuleItems) {
                PermissionPlmListRuleItem permissionPlmListRuleItem1 = new PermissionPlmListRuleItem();
                String permissionBid = permissionPlmListRuleItem.getPermissionBid();
                String oldAppBid = "";
                if (permissionBid.startsWith("APP:")) {
                    oldAppBid = permissionBid.replace("APP:", "");
                } else if (permissionBid.startsWith("LC:")) {
                    oldAppBid = permissionBid.split(":")[1];
                }
                String oldRoleCode = permissionPlmListRuleItem.getRoleCode();
                String bid = SnowflakeIdWorker.nextIdStr();
                permissionPlmListRuleItem1.setBid(bid);
                permissionPlmListRuleItem1.setPermissionBid(permissionPlmListRuleItem.getPermissionBid().replace(oldAppBid, appBidMap.get(oldAppBid)));
                permissionPlmListRuleItem1.setPath(permissionPlmListRuleItem.getPath().replace(oldAppBid, appBidMap.get(oldAppBid)));
                // 设置角色code 角色code不会变
                permissionPlmListRuleItem1.setRoleCode(permissionPlmListRuleItem.getRoleCode());
                permissionPlmListRuleItem1.setEnableFlag(permissionPlmListRuleItem.getEnableFlag());
                permissionPlmListRuleItem1.setCreatedTime(new Date());
                permissionPlmListRuleItem1.setUpdatedTime(new Date());
                permissionPlmListRuleItem1.setCreatedBy(jobNumber);
                permissionPlmListRuleItem1.setUpdatedBy(jobNumber);
                permissionPlmListRuleItemNews.add(permissionPlmListRuleItem1);
            }
        }
        List<PermissionPlmOperationRuleItem> PermissionPlmOperationRuleItemNews = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(permissionPlmOperationRuleItems)) {
            for (PermissionPlmOperationRuleItem permissionPlmOperationRuleItem : permissionPlmOperationRuleItems) {
                PermissionPlmOperationRuleItem permissionPlmOperationRuleItem1 = new PermissionPlmOperationRuleItem();
                String permissionBid = permissionPlmOperationRuleItem.getPermissionBid();
                String oldAppBid = "";
                if (permissionBid.startsWith("APP:")) {
                    oldAppBid = permissionBid.replace("APP:", "");
                } else if (permissionBid.startsWith("LC:")) {
                    oldAppBid = permissionBid.split(":")[1];
                }
                String oldRoleCode = permissionPlmOperationRuleItem.getRoleCode();
                String bid = SnowflakeIdWorker.nextIdStr();
                permissionPlmOperationRuleItem1.setBid(bid);
                permissionPlmOperationRuleItem1.setPermissionBid(permissionPlmOperationRuleItem.getPermissionBid().replace(oldAppBid, appBidMap.get(oldAppBid)));
                permissionPlmOperationRuleItem1.setPath(permissionPlmOperationRuleItem.getPath().replace(oldAppBid, appBidMap.get(oldAppBid)));
                // 设置角色code 角色code不会变
                permissionPlmOperationRuleItem1.setRoleCode(permissionPlmOperationRuleItem.getRoleCode());
                permissionPlmOperationRuleItem1.setEnableFlag(permissionPlmOperationRuleItem.getEnableFlag());
                permissionPlmOperationRuleItem1.setOperationDetail(permissionPlmOperationRuleItem.getOperationDetail());

                permissionPlmOperationRuleItem1.setOperationDelete(permissionPlmOperationRuleItem.getOperationDelete());
                permissionPlmOperationRuleItem1.setOperationRevise(permissionPlmOperationRuleItem.getOperationRevise());
                permissionPlmOperationRuleItem1.setOperationPromote(permissionPlmOperationRuleItem.getOperationPromote());
                permissionPlmOperationRuleItem1.setOperationMove(permissionPlmOperationRuleItem.getOperationMove());
                permissionPlmOperationRuleItem1.setOperationEdit(permissionPlmOperationRuleItem.getOperationEdit());
                permissionPlmOperationRuleItem1.setOperationDetail(permissionPlmOperationRuleItem.getOperationDetail());
                permissionPlmOperationRuleItem1.setOperationStr(permissionPlmOperationRuleItem.getOperationStr());

                permissionPlmOperationRuleItem1.setOperationImport(permissionPlmOperationRuleItem.getOperationImport());
                permissionPlmOperationRuleItem1.setOperationExport(permissionPlmOperationRuleItem.getOperationExport());
                permissionPlmOperationRuleItem1.setOperation3(permissionPlmOperationRuleItem.getOperation3());
                permissionPlmOperationRuleItem1.setOperation4(permissionPlmOperationRuleItem.getOperation4());
                permissionPlmOperationRuleItem1.setOperation5(permissionPlmOperationRuleItem.getOperation5());
                permissionPlmOperationRuleItem1.setOperation6(permissionPlmOperationRuleItem.getOperation6());
                permissionPlmOperationRuleItem1.setOperation7(permissionPlmOperationRuleItem.getOperation7());
                permissionPlmOperationRuleItem1.setOperation8(permissionPlmOperationRuleItem.getOperation8());
                permissionPlmOperationRuleItem1.setOperation9(permissionPlmOperationRuleItem.getOperation9());
                permissionPlmOperationRuleItem1.setOperation10(permissionPlmOperationRuleItem.getOperation10());
                permissionPlmOperationRuleItem1.setOperationDelete(permissionPlmOperationRuleItem.getOperationDelete());
                permissionPlmOperationRuleItem1.setModelViewAll(permissionPlmOperationRuleItem.getModelViewAll());
                permissionPlmOperationRuleItem1.setModelViewOverview(permissionPlmOperationRuleItem.getModelViewOverview());
                permissionPlmOperationRuleItem1.setModelViewKanbanView(permissionPlmOperationRuleItem.getModelViewKanbanView());
                permissionPlmOperationRuleItem1.setInstanceLock(permissionPlmOperationRuleItem.getInstanceLock());
                permissionPlmOperationRuleItem1.setInstanceUnlock(permissionPlmOperationRuleItem.getInstanceUnlock());
                permissionPlmOperationRuleItem1.setSync(permissionPlmOperationRuleItem.getSync());
                permissionPlmOperationRuleItem1.setModelViewGanttView(permissionPlmOperationRuleItem.getModelViewGanttView());
                permissionPlmOperationRuleItem1.setModelViewMeasureReport(permissionPlmOperationRuleItem.getModelViewMeasureReport());
                permissionPlmOperationRuleItem1.setModelViewMultiTreeView(permissionPlmOperationRuleItem.getModelViewMultiTreeView());
                permissionPlmOperationRuleItem1.setModelViewTableView(permissionPlmOperationRuleItem.getModelViewTableView());
                permissionPlmOperationRuleItem1.setModelViewTreeView(permissionPlmOperationRuleItem.getModelViewTreeView());


                permissionPlmOperationRuleItem1.setCreatedTime(new Date());
                permissionPlmOperationRuleItem1.setUpdatedTime(new Date());
                permissionPlmOperationRuleItem1.setCreatedBy(jobNumber);
                permissionPlmOperationRuleItem1.setUpdatedBy(jobNumber);
                PermissionPlmOperationRuleItemNews.add(permissionPlmOperationRuleItem1);
            }
        }

        List<PermissionPlmRuleCondition> permissionPlmRuleConditionListNew = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(permissionPlmRuleConditionList)) {
            for (PermissionPlmRuleCondition permissionPlmRuleCondition : permissionPlmRuleConditionList) {
                PermissionPlmRuleCondition permissionPlmRuleCondition1 = new PermissionPlmRuleCondition();
                String permissionBid = permissionPlmRuleCondition.getPermissionBid();
                String oldAppBid = "";
                if (permissionBid.startsWith("APP:")) {
                    oldAppBid = permissionBid.replace("APP:", "");
                } else if (permissionBid.startsWith("LC:")) {
                    oldAppBid = permissionBid.split(":")[1];
                }
                String bid = SnowflakeIdWorker.nextIdStr();
                permissionPlmRuleCondition1.setBid(bid);
                permissionPlmRuleCondition1.setPermissionBid(permissionPlmRuleCondition.getPermissionBid().replace(oldAppBid, appBidMap.get(oldAppBid)));
                permissionPlmRuleCondition1.setAppBid(appBidMap.get(oldAppBid));
                permissionPlmRuleCondition1.setType(permissionPlmRuleCondition.getType());
                permissionPlmRuleCondition1.setValueType(permissionPlmRuleCondition.getValueType());
                permissionPlmRuleCondition1.setAttrCode(permissionPlmRuleCondition.getAttrCode());
                permissionPlmRuleCondition1.setAttrCodeValue(permissionPlmRuleCondition.getAttrCodeValue());
                permissionPlmRuleCondition1.setOperator(permissionPlmRuleCondition.getOperator());
                permissionPlmRuleCondition1.setOperatorZh(permissionPlmRuleCondition.getOperatorZh());
                permissionPlmRuleCondition1.setRemoteDictType(permissionPlmRuleCondition.getRemoteDictType());
                permissionPlmRuleCondition1.setValueZh(permissionPlmRuleCondition.getValueZh());
                permissionPlmRuleCondition1.setPropertyZh(permissionPlmRuleCondition.getPropertyZh());

                permissionPlmRuleCondition1.setCreatedTime(new Date());
                permissionPlmRuleCondition1.setUpdatedTime(new Date());
                permissionPlmRuleCondition1.setCreatedBy(jobNumber);
                permissionPlmRuleCondition1.setUpdatedBy(jobNumber);
                permissionPlmRuleCondition1.setEnableFlag(permissionPlmRuleCondition.getEnableFlag());
                permissionPlmRuleConditionListNew.add(permissionPlmRuleCondition1);
            }
        }


        boolean b = permissionConfigService.saveBathPermissionData(permissionPlmRuleNews,
                permissionPlmAddRuleItemNews,
                permissionPlmListRuleItemNews,
                PermissionPlmOperationRuleItemNews,
                permissionPlmRuleConditionListNew);
    }

    private void copyPlanTask(String newSpaceBid, Map<String, String> appBidMap, String defaultSpaceBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SPACE_BID.getColumn(), defaultSpaceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        //计划任务modelCode
        String modelCode = PLAN_TASK_MODEL_CODE;
        List<MObject> mObjectList = objectModelCrudI.listByQueryWrapper(modelCode, queryWrappers);
        if (CollectionUtils.isNotEmpty(mObjectList)) {
            Map<String, String> bidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            for (MObject mObject : mObjectList) {
                mObject.setId(null);
                String newBid = SnowflakeIdWorker.nextIdStr();
                bidMap.put(mObject.getBid(), newBid);
                mObject.setBid(newBid);
                mObject.put(TranscendModelBaseFields.SPACE_BID, newSpaceBid);
                mObject.put(TranscendModelBaseFields.SPACE_APP_BID, appBidMap.get(mObject.get(TranscendModelBaseFields.SPACE_APP_BID) + ""));
            }
            for (MObject mObject : mObjectList) {
                mObject.put(TranscendModelBaseFields.PARENT_BID, bidMap.get(mObject.get(TranscendModelBaseFields.PARENT_BID) + ""));
                if (mObject.get(TranscendModelBaseFields.PARENT_BID) == null || StringUtils.isEmpty(mObject.get(TranscendModelBaseFields.PARENT_BID) + "")) {
                    mObject.put(TranscendModelBaseFields.PARENT_BID, "0");
                }
            }
            appDataService.addBatch(modelCode, mObjectList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheNameConstant.SPACE_APP, key = "#bid")
    public boolean logicDeleteApmSpace(String bid) {
        log.info("{}删除了bid为{}的空间", SsoHelper.getName(), bid);
        GenericLogAddParam logParam = GenericLogAddParam.builder().spaceBid(bid)
                .modelCode("")
                .instanceBid("")
                .logMsg(SsoHelper.getName() + "删除了bid为" + bid + "的空间")
                .build();
        operationLogEsService.genericSave(logParam);
        return apmSpaceService.remove(Wrappers.<ApmSpace>lambdaQuery().eq(ApmSpace::getBid, bid));
    }

    @Override
    public boolean updateApmSpace(ApmSpaceDto apmSpaceDto) {
        ApmSpace apmSpace = apmSpaceService.getOne(Wrappers.<ApmSpace>lambdaQuery().eq(ApmSpace::getBid, apmSpaceDto.getBid()));
        apmSpace.setSphereBid(apmSpaceDto.getSphereBid());
        apmSpace.setName(apmSpaceDto.getName());
        apmSpace.setDescription(apmSpaceDto.getDescription());
        apmSpace.setIconUrl(apmSpaceDto.getIconUrl());
        apmSpace.setTemplateFlag(apmSpaceDto.getTemplateFlag());
        apmSpace.setTemplateBid(apmSpaceDto.getTemplateBid());
        apmSpace.setUpdatedBy(SsoHelper.getJobNumber());
        apmSpace.setUpdatedTime(new Date());
        return apmSpaceService.updateById(apmSpace);
    }

    private ApmSpace getApmSpace(ApmSpaceDto apmSpaceDto) {
        ApmSpace apmSpace = new ApmSpace();
        String bid = apmSpaceDto.getBid();
        if (StringUtil.isBlank(apmSpaceDto.getBid())) {
            bid = SnowflakeIdWorker.nextIdStr();
        }
        apmSpace.setBid(bid);
        apmSpace.setName(apmSpaceDto.getName());
        apmSpace.setOriginBid(apmSpaceDto.getOriginBid());
        apmSpace.setOriginModelCode(apmSpaceDto.getOriginModelCode());
        apmSpace.setDescription(apmSpaceDto.getDescription());
        apmSpace.setIconUrl(apmSpaceDto.getIconUrl());
        apmSpace.setTemplateFlag(apmSpaceDto.getTemplateFlag());
        apmSpace.setTemplateBid(apmSpaceDto.getTemplateBid());
        apmSpace.setTemplateName(apmSpaceDto.getTemplateName());
        apmSpace.setSort(apmSpaceDto.getSort());
        apmSpace.setCreatedBy(SsoHelper.getJobNumber());
        apmSpace.setUpdatedBy(apmSpace.getCreatedBy());
        apmSpace.setCreatedTime(new Date());
        apmSpace.setUpdatedTime(new Date());
        apmSpace.setDeleteFlag(CommonConst.DELETE_FLAG_NOT_DELETED);
        apmSpace.setForeignBid(apmSpaceDto.getForeignBid());
        return apmSpace;
    }

    /**
     * 设置用户默认空间
     *
     * @param spaceBid 空间bid
     * @return boolean
     */
    @Override
    public boolean setDefaultSpace(String spaceBid) {
        String jobNum = SsoHelper.getJobNumber();
        ApmUserSpace apmUserSpace = apmUserSpaceService.getOne(Wrappers.<ApmUserSpace>lambdaQuery().eq(ApmUserSpace::getUserNo, jobNum));
        if (apmUserSpace == null) {
            //新增
            apmUserSpace = new ApmUserSpace();
            apmUserSpace.setBid(SnowflakeIdWorker.nextIdStr());
            apmUserSpace.setDefaultSpaceBid(spaceBid);
            apmUserSpace.setUserNo(jobNum);
            apmUserSpace.setCreatedBy(jobNum);
            apmUserSpace.setCreatedTime(new Date());
            apmUserSpace.setUpdatedBy(jobNum);
            apmUserSpace.setUpdatedTime(new Date());
            return apmUserSpaceService.save(apmUserSpace);
        } else {
            //修改
            apmUserSpace.setDefaultSpaceBid(spaceBid);
            apmUserSpace.setUpdatedBy(jobNum);
            apmUserSpace.setUpdatedTime(new Date());
            return apmUserSpaceService.updateById(apmUserSpace);
        }
    }

    @Override
    public PagedResult<ApmSpaceVo> listApmSpacePage(BaseRequest<ApmSpaceQo> pageRequest) {
        // 获取当前登录人工号
        String jobNum = SsoHelper.getJobNumber();
        // 获取当前登录人所属空间
        PagedResult<ApmSpaceVo> apmSpacesPage = listApmSpacePageByJobNum(jobNum, pageRequest);
        if (CollectionUtils.isNotEmpty(apmSpacesPage.getData())) {
            Set<String> bids = apmSpacesPage.getData().stream().map(ApmSpaceVo::getBid).collect(Collectors.toSet());
            Map<String, List<ApmSpaceAppVo>> apmSpaceVoMap = apmSpaceAppService.listSpaceAppVoBySpaceBids(bids);
            ApmUserSpace apmUserSpace = apmUserSpaceService.getOne(Wrappers.<ApmUserSpace>lambdaQuery().eq(ApmUserSpace::getUserNo, jobNum));
            String defaultBid = "";
            if (apmUserSpace != null) {
                defaultBid = apmUserSpace.getDefaultSpaceBid();
            }
            // 如果没有默认空间，第一个就是
            if (StringUtil.isBlank(defaultBid)) {
                defaultBid = apmSpacesPage.getData().get(0).getBid();
            }
            for (ApmSpaceVo apmSpaceVo : apmSpacesPage.getData()) {
                apmSpaceVo.setApmSpaceAppVoList(apmSpaceVoMap.get(apmSpaceVo.getBid()));
                if (apmSpaceVo.getBid().equals(defaultBid)) {
                    // 设置默认空间
                    apmSpaceVo.setDefaultSpace(true);
                }
            }
        }
        return apmSpacesPage;
    }

    public PagedResult<ApmSpaceVo> listApmSpacePageByJobNum(String jobNum, BaseRequest<ApmSpaceQo> pageRequest) {
        // 防止空指针
        ApmSpaceQo apmSpaceQo = pageRequest.getParam();
        if (apmSpaceQo == null) {
            apmSpaceQo = new ApmSpaceQo();
        }
        Page<ApmSpace> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        // 获取用户查看空间的权限信息
        ApmSpaceViewPermissionBo apmSpaceViewPermissionBo = getSpaceViewPermission(jobNum);
        // 忽略权限，直接返回没 带有权限的空间的过滤逻辑
        if (Boolean.TRUE.equals(apmSpaceViewPermissionBo.getIgnorePermission())) {
            LambdaQueryWrapper queryWrapper = Wrappers.<ApmSpace>lambdaQuery()
                    .like(StringUtil.isNotBlank(apmSpaceQo.getName()), ApmSpace::getName, apmSpaceQo.getName())
                    .eq(apmSpaceQo.getTemplateFlag() != null, ApmSpace::getTemplateFlag, apmSpaceQo.getTemplateFlag())
                    .eq(ApmSpace::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED);
            IPage<ApmSpace> iPage = apmSpaceService.page(page, queryWrapper);
            return PageResultTools.create(iPage, CollectionUtils.copyList(iPage.getRecords(), ApmSpaceVo.class));
        }
        // 非管理员需要根据权限过滤空间
        Set<String> spaceBids = apmSpaceViewPermissionBo.getPermissionSpaceBidSet();
        // 空的场景直接返回
        if (CollectionUtils.isEmpty(spaceBids)) {
            return PageResultTools.createEmpty();
        }

        // 如果不是超管+base空间管理员，base空间需要特殊移除
        Boolean spaceAdminOrGlobalAdmin = apmRoleDomainService.isSpaceAdminOrGlobalAdmin(SpaceConstant.ALM_BASE_SPACE_BID);
        if (!Boolean.TRUE.equals(spaceAdminOrGlobalAdmin)) {
            spaceBids.remove(SpaceConstant.ALM_BASE_SPACE_BID);
        }

        // 如果没有权限，直接返回
        if (CollectionUtils.isEmpty(spaceBids)) {
            return PageResultTools.createEmpty();
        }

        // 通过spaceBid查询空间信息
        LambdaQueryWrapper wrapper = Wrappers.<ApmSpace>lambdaQuery()
                .in(ApmSpace::getBid, spaceBids)
                .like(StringUtil.isNotBlank(apmSpaceQo.getName()), ApmSpace::getName, apmSpaceQo.getName())
                .eq(apmSpaceQo.getTemplateFlag() != null, ApmSpace::getTemplateFlag, apmSpaceQo.getTemplateFlag())
                .eq(ApmSpace::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED);
        IPage<ApmSpace> iPage = apmSpaceService.page(page, wrapper);
        return PageResultTools.create(iPage, CollectionUtils.copyList(iPage.getRecords(), ApmSpaceVo.class));
    }

    @Override
    public PagedResult<ApmSpaceVo> pageApmSpace(BaseRequest<ApmSpaceQo> pageRequest) {

        // 获取用户查看空间的权限信息
        ApmSpaceViewPermissionBo apmSpaceViewPermissionBo = getSpaceViewPermission(SsoHelper.getJobNumber());
        // 需要根据权限查询空间
        Set<String> permissionSpaceBidSet = apmSpaceViewPermissionBo.getPermissionSpaceBidSet();
        // 没有权限的空间，直接返回空
        if (CollectionUtils.isEmpty(permissionSpaceBidSet) && !Boolean.TRUE.equals(apmSpaceViewPermissionBo.getIgnorePermission())) {
            return PageResultTools.createEmpty();
        }
        ApmSpaceQo spaceQo = pageRequest.getParam();
        ApmSpace apmSpace = ApmSpaceConverter.INSTANCE.qo2po(spaceQo);
        Page<ApmSpace> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        LambdaQueryWrapper<ApmSpace> queryWrapper = Wrappers.<ApmSpace>lambdaQuery()
                // 没有忽略权限才需要过滤
                .in(!Boolean.TRUE.equals(apmSpaceViewPermissionBo.getIgnorePermission()), ApmSpace::getBid, permissionSpaceBidSet)
                .eq(null != apmSpace.getTemplateFlag(), ApmSpace::getTemplateFlag, apmSpace.getTemplateFlag())
                .like(com.transsion.framework.common.StringUtil.isNotBlank(apmSpace.getName()), ApmSpace::getName, spaceQo.getName());
        IPage<ApmSpace> iPage = apmSpaceService.page(page, queryWrapper);
        List<ApmSpaceVo> vos = ApmSpaceConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage, vos);
    }

    @Override
    public List<ApmSpaceVo> listApmSpace() {
        return listApmSpace(null);
    }


    @Override
    public List<ApmSpaceAppQueryVo> queryApmSpaceAppInstance(ApmSpaceAppQueryDto apmSpaceAppQueryDto) {
        List<ApmSpaceAppQueryVo> apmSpaceAppQueryVos = new ArrayList<>();
        //如果是项目集需要特殊处理
        if ("A3L".equals(apmSpaceAppQueryDto.getAppModelCode())) {
            QueryWrapper qo = new QueryWrapper();
            qo.eq(TranscendModelBaseFields.DELETE_FLAG, 0);
            List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(qo);
            List<MObject> list = objectModelCrudI.list(apmSpaceAppQueryDto.getAppModelCode(), wrappers);
            if (CollectionUtils.isNotEmpty(list)) {
                for (MObject mObject : list) {
                    if (!mObject.getBid().equals(apmSpaceAppQueryDto.getInstanceBid())) {
                        ApmSpaceAppQueryVo apmSpaceAppQueryVo = new ApmSpaceAppQueryVo();
                        apmSpaceAppQueryVo.setInstanceBid(mObject.getBid());
                        apmSpaceAppQueryVo.setInstanceName(mObject.get(TranscendModelBaseFields.NAME) + "");
                        apmSpaceAppQueryVos.add(apmSpaceAppQueryVo);
                    }
                }
            }
            return apmSpaceAppQueryVos;
        }
        //查询所属空间
        String jobNum = SsoHelper.getJobNumber();
        List<ApmSpace> apmSpaces = listApmSpaceByJobNum(jobNum, null);
        if (CollectionUtils.isEmpty(apmSpaces)) {
            return Collections.emptyList();
        }
        List<String> spaceBids = apmSpaces.stream().map(ApmSpace::getBid).collect(Collectors.toList());
        Map<String, String> nameMap = apmSpaces.stream().collect(Collectors.toMap(ApmSpace::getBid, ApmSpace::getName));
        QueryWrapper qo = new QueryWrapper();
        qo.in(TranscendModelBaseFields.SPACE_BID, spaceBids);
        List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(qo);
        //查询所属应用实例
        List<MObject> list = objectModelCrudI.list(apmSpaceAppQueryDto.getAppModelCode(), wrappers);
        if (CollectionUtils.isNotEmpty(list)) {
            for (MObject mObject : list) {
                if (!mObject.getBid().equals(apmSpaceAppQueryDto.getInstanceBid())) {
                    ApmSpaceAppQueryVo apmSpaceAppQueryVo = new ApmSpaceAppQueryVo();
                    apmSpaceAppQueryVo.setInstanceBid(mObject.getBid());
                    apmSpaceAppQueryVo.setSpaceAppBid(mObject.get(TranscendModelBaseFields.SPACE_APP_BID) + "");
                    apmSpaceAppQueryVo.setSpaceBid(mObject.get(TranscendModelBaseFields.SPACE_BID) + "");
                    apmSpaceAppQueryVo.setInstanceName(mObject.get(TranscendModelBaseFields.NAME) + "");
                    apmSpaceAppQueryVo.setSpaceName(nameMap.get(mObject.get(TranscendModelBaseFields.SPACE_BID) + ""));
                    apmSpaceAppQueryVos.add(apmSpaceAppQueryVo);
                }
            }
        }
        return apmSpaceAppQueryVos;
    }

    @Override
    public List<ApmSpaceAppVo> listAppByBids(List<String> bids) {
        List<ApmSpaceAppVo> apmSpaceAppVos = apmSpaceAppService.listSpaceAppVoByBids(bids);
        return apmSpaceAppVos;
    }

    @Override
    public ApmSpaceVo getDefaultApmSpace() {
        String jobNum = SsoHelper.getJobNumber();
        ApmUserSpace apmUserSpace = apmUserSpaceService.getOne(Wrappers.<ApmUserSpace>lambdaQuery().eq(ApmUserSpace::getUserNo, jobNum));
        String defaultBid = "";
        if (apmUserSpace != null) {
            defaultBid = apmUserSpace.getDefaultSpaceBid();
        }
        // 获取用户查看空间的权限信息
        ApmSpaceViewPermissionBo apmSpaceViewPermissionBo = getSpaceViewPermission(jobNum);
        // 忽略权限，直接返回没 带有权限的空间的过滤逻辑
        if (Boolean.TRUE.equals(apmSpaceViewPermissionBo.getIgnorePermission()) || (CollectionUtils.isNotEmpty(apmSpaceViewPermissionBo.getPermissionSpaceBidSet()) && apmSpaceViewPermissionBo.getPermissionSpaceBidSet().contains(defaultBid))) {
            ApmSpace apmSpace = apmSpaceService.getOne(Wrappers.<ApmSpace>lambdaQuery().eq(ApmSpace::getBid, defaultBid).eq(ApmSpace::getDeleteFlag, false));
            if (apmSpace != null) {
                ApmSpaceVo apmSpaceVo = ApmSpaceConverter.INSTANCE.po2vo(apmSpace);
                Map<String, List<ApmSpaceAppVo>> apmSpaceVoMap = apmSpaceAppService.listSpaceAppVoBySpaceBids(Sets.newHashSet(apmSpaceVo.getBid()));
                apmSpaceVo.setApmSpaceAppVoList(apmSpaceVoMap.get(apmSpaceVo.getBid()));
                return apmSpaceVo;
            }
        }
        return null;
    }

    @Override
    public List<ApmSpaceVo> listApmSpace(ApmSpaceQo qo) {
        // 获取当前登录人工号
        String jobNum = SsoHelper.getJobNumber();
        // 获取当前登录人所属空间
        List<ApmSpace> apmSpaces = listApmSpaceByJobNum(jobNum, qo);
        // 获取用户查看空间的权限信息,为空，直接返回空
        if (CollectionUtils.isEmpty(apmSpaces)) {
            return Collections.emptyList();
        }

        Set<String> bids = apmSpaces.stream().map(ApmSpace::getBid).collect(Collectors.toSet());

        Map<String, List<ApmSpaceAppVo>> apmSpaceVoMap = apmSpaceAppService.listSpaceAppVoBySpaceBids(bids);
        List<ApmSpaceVo> apmSpaceVos = CollectionUtils.copyList(apmSpaces, ApmSpaceVo.class);
        ApmUserSpace apmUserSpace = apmUserSpaceService.getOne(Wrappers.<ApmUserSpace>lambdaQuery().eq(ApmUserSpace::getUserNo, jobNum));
        String defaultBid = "";
        if (apmUserSpace != null) {
            defaultBid = apmUserSpace.getDefaultSpaceBid();
        }
        for (ApmSpaceVo apmSpaceVo : apmSpaceVos) {
            apmSpaceVo.setApmSpaceAppVoList(apmSpaceVoMap.get(apmSpaceVo.getBid()));
            if (apmSpaceVo.getBid().equals(defaultBid)) {
                // 设置默认空间
                apmSpaceVo.setDefaultSpace(true);
            }
        }

        return apmSpaceVos;
    }


    /**
     * 获取空间模板列表
     *
     * @return
     */
    @Override
    public List<ApmSpaceVo> listSpaceTemplate() {
        List<ApmSpace> poList = apmSpaceService.list(
                Wrappers.<ApmSpace>lambdaQuery()
                        .eq(ApmSpace::getTemplateFlag, Boolean.TRUE)
                        .eq(ApmSpace::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
        );
        // 空的场景直接返回
        if (CollectionUtils.isEmpty(poList)) {
            return Collections.emptyList();
        }
        return ApmSpaceConverter.INSTANCE.pos2vos(poList);
    }


    @Override
    public List<ApmRoleVO> listRoleAndPermission(ApmSpaceRoleQO apmSpaceRoleQo) {
        // 通过空间bid和域类型查询域信息
        ApmSphere sphere = apmSphereService.getOne(
                Wrappers.<ApmSphere>lambdaQuery()
                        .eq(ApmSphere::getBizBid, apmSpaceRoleQo.getBid())
                        .eq(ApmSphere::getType, apmSpaceRoleQo.getSphereType())
                        .eq(ApmSphere::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmSphere::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
        );
        if (Objects.isNull(sphere) || Objects.isNull(sphere.getBid())) {
            return Lists.newArrayList();
        }

        List<ApmRoleVO> list = apmRoleService.getRoleListBySphereBid(sphere.getBid());
        if (list == null) {
            list = Lists.newArrayList();
        }

        //只有超级管理员才展示内置角色
        if (apmRoleDomainService.isGlobalAdmin()) {
            //设置内置字段
            ApmRoleVO apmRoleVO = new ApmRoleVO();
            apmRoleVO.setInnerRole(true);
            apmRoleVO.setCode(InnerRoleEnum.CREATER.getCode());
            apmRoleVO.setName(InnerRoleEnum.CREATER.getDesc());
            apmRoleVO.setBid(InnerRoleEnum.CREATER.getCode());
            list.add(apmRoleVO);

            // 所有者
            ApmRoleVO apmRoleVORepon = new ApmRoleVO();
            apmRoleVORepon.setInnerRole(true);
            apmRoleVORepon.setCode(InnerRoleEnum.PERSON_RESPONSIBLE.getCode());
            apmRoleVORepon.setName(InnerRoleEnum.PERSON_RESPONSIBLE.getDesc());
            apmRoleVORepon.setBid(InnerRoleEnum.PERSON_RESPONSIBLE.getCode());
            list.add(apmRoleVORepon);

            // 关注人
            ApmRoleVO apmRoleVOFollow = new ApmRoleVO();
            apmRoleVOFollow.setInnerRole(true);
            apmRoleVOFollow.setCode(InnerRoleEnum.FOLLOW_MEMBER.getCode());
            apmRoleVOFollow.setName(InnerRoleEnum.FOLLOW_MEMBER.getDesc());
            apmRoleVOFollow.setBid(InnerRoleEnum.FOLLOW_MEMBER.getCode());
            list.add(apmRoleVOFollow);

            // 技术负责人
            ApmRoleVO apmRoleVOTechnical = new ApmRoleVO();
            apmRoleVOTechnical.setInnerRole(true);
            apmRoleVOTechnical.setCode(InnerRoleEnum.TECHNICAL_DIRECTOR.getCode());
            apmRoleVOTechnical.setName(InnerRoleEnum.TECHNICAL_DIRECTOR.getDesc());
            apmRoleVOTechnical.setBid(InnerRoleEnum.TECHNICAL_DIRECTOR.getCode());
            list.add(apmRoleVOTechnical);

            // UX代表
            ApmRoleVO apmRoleVoUx = new ApmRoleVO();
            apmRoleVoUx.setInnerRole(true);
            apmRoleVoUx.setCode(InnerRoleEnum.UX_AGENT.getCode());
            apmRoleVoUx.setName(InnerRoleEnum.UX_AGENT.getDesc());
            apmRoleVoUx.setBid(InnerRoleEnum.UX_AGENT.getCode());
            list.add(apmRoleVoUx);
        }

        //设置成员
        list.stream().parallel().forEach(apmRoleVO1 -> {
            ApmRoleQO apmRoleQo = new ApmRoleQO();
            apmRoleQo.setBid(apmRoleVO1.getBid());
            apmRoleQo.setBizType("employee");
            apmRoleQo.setCode(apmRoleVO1.getCode());
            apmRoleQo.setSphereBid(apmRoleVO1.getSphereBid());
            apmRoleVO1.setMembers(apmRoleIdentityDomainService.listByRole(apmRoleQo));
        });
        return list;
    }

    @Override
    public List<ApmRoleVO> listRoleAndPermissionTree(ApmSpaceRoleQO apmSpaceRoleQo) {
        // 通过空间bid和域类型查询域信息
        ApmSphere sphere = apmSphereService.getOne(
                Wrappers.<ApmSphere>lambdaQuery()
                        .eq(ApmSphere::getBizBid, apmSpaceRoleQo.getBid())
                        .eq(ApmSphere::getType, apmSpaceRoleQo.getSphereType())
                        .eq(ApmSphere::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmSphere::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
        );
        if (Objects.isNull(sphere) || Objects.isNull(sphere.getBid())) {
            return Lists.newArrayList();
        }
        //设置内置字段
        ApmRoleVO apmRoleVO = new ApmRoleVO();
        apmRoleVO.setInnerRole(true);
        apmRoleVO.setCode(InnerRoleEnum.CREATER.getCode());
        apmRoleVO.setName(InnerRoleEnum.CREATER.getDesc());
        apmRoleVO.setBid(InnerRoleEnum.CREATER.getCode());
        apmRoleVO.setParentBid(CommonConst.ROLE_TREE_DEFAULT_ROOT_BID);
        List<ApmRoleVO> list = apmRoleService.getRoleListBySphereBid(sphere.getBid());
        if (list == null) {
            list = Lists.newArrayList();
        }

        list.add(apmRoleVO);
        ApmRoleVO apmRoleVORepon = new ApmRoleVO();
        apmRoleVORepon.setInnerRole(true);
        apmRoleVORepon.setCode(InnerRoleEnum.PERSON_RESPONSIBLE.getCode());
        apmRoleVORepon.setName(InnerRoleEnum.PERSON_RESPONSIBLE.getDesc());
        apmRoleVORepon.setBid(InnerRoleEnum.PERSON_RESPONSIBLE.getCode());
        apmRoleVORepon.setParentBid(CommonConst.ROLE_TREE_DEFAULT_ROOT_BID);
        list.add(apmRoleVORepon);

        // 技术负责人
        ApmRoleVO apmRoleVOTechnical = new ApmRoleVO();
        apmRoleVOTechnical.setInnerRole(true);
        apmRoleVOTechnical.setCode(InnerRoleEnum.TECHNICAL_DIRECTOR.getCode());
        apmRoleVOTechnical.setName(InnerRoleEnum.TECHNICAL_DIRECTOR.getDesc());
        apmRoleVOTechnical.setBid(InnerRoleEnum.TECHNICAL_DIRECTOR.getCode());
        list.add(apmRoleVOTechnical);

        // UX代表
        ApmRoleVO apmRoleVoUx = new ApmRoleVO();
        apmRoleVoUx.setInnerRole(true);
        apmRoleVoUx.setCode(InnerRoleEnum.UX_AGENT.getCode());
        apmRoleVoUx.setName(InnerRoleEnum.UX_AGENT.getDesc());
        apmRoleVoUx.setBid(InnerRoleEnum.UX_AGENT.getCode());
        list.add(apmRoleVoUx);




        List<ApmRoleVO> allRoleList = list;

        //设置成员
        list.stream().parallel().forEach(apmRoleVO1 -> {
            ApmRoleQO apmRoleQo = new ApmRoleQO();
            apmRoleQo.setBid(apmRoleVO1.getBid());
            apmRoleQo.setBizType("employee");
            apmRoleQo.setCode(apmRoleVO1.getCode());
            apmRoleQo.setSphereBid(apmRoleVO1.getSphereBid());
            apmRoleVO1.setMembers(apmRoleIdentityDomainService.listByRole(apmRoleQo));
        });

        return list.stream()
                .filter(roleVO -> CommonConst.ROLE_TREE_DEFAULT_ROOT_BID.equals(roleVO.getParentBid()))
                .peek(role -> {
                    ApmRoleQO apmRoleQo = new ApmRoleQO();
                    apmRoleQo.setBid(role.getBid());
                    apmRoleQo.setBizType("employee");
                    apmRoleQo.setCode(role.getCode());
                    apmRoleQo.setSphereBid(role.getSphereBid());
                    role.setMembers(apmRoleIdentityDomainService.listByRole(apmRoleQo));
                    role.setChildRoles(this.getChildren(role, allRoleList));
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取子节点
     */
    private List<ApmRoleVO> getChildren(ApmRoleVO roleVO, List<ApmRoleVO> roleVOList) {
        return roleVOList.stream()
                .filter(vo -> Objects.equals(vo.getParentBid(), roleVO.getBid()))
                .peek(vo -> {
                    ApmRoleQO apmRoleQo = new ApmRoleQO();
                    apmRoleQo.setBid(vo.getBid());
                    apmRoleQo.setBizType("employee");
                    apmRoleQo.setCode(vo.getCode());
                    apmRoleQo.setSphereBid(vo.getSphereBid());
                    vo.setMembers(apmRoleIdentityDomainService.listByRole(apmRoleQo));
                    vo.setChildRoles(getChildren(vo, roleVOList));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ApmSpaceAppAccessVo> getPermissionBySphereBid(String sphereBid) {
        // 获取当前登陆人工号
        String jobNum = SsoHelper.getJobNumber();
        ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(jobNum);
        // 这里只能查到最底层的部门
        List<String> identityQueryList = apmUser.getDepts().stream().map(DepartmentDTO::getDeptNo).collect(Collectors.toList());
        // 需要根据最底层的部门向上反查所有的部门
        List<DepartmentDTO> parentDeptList = platformUserWrapper.getParentDepartmentByDepartmentId(apmUser.getDepartmentList().get(0));
        for (DepartmentDTO departmentDTO : parentDeptList) {
            identityQueryList.add(String.valueOf(departmentDTO.getDeptNo()));
        }
        // 把员工工号也放入到查询列表中
        identityQueryList.add(jobNum);
        List<String> roleBids = apmRoleService.getRoleListBySphereBidAndIdentityList(sphereBid, identityQueryList);
        // 通过交集结果和域Bid查询权限信息
        return apmRoleAccessService.getAccessByRoleBidList(roleBids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApmSpaceApp addApp(ApmSpaceAppAo apmSpaceAppAo) {
        // 构造空间应用对象保存信息
        ApmSpaceApp apmSpaceApp = ApmSpaceAppConverter.INSTANCE.ao2Entity(apmSpaceAppAo);
        apmSpaceApp.setBid(SnowflakeIdWorker.nextIdStr());
        apmSpaceApp.setCreatedBy(SsoHelper.getJobNumber());
        apmSpaceApp.setUpdatedBy(apmSpaceApp.getCreatedBy());
        apmSpaceApp.setIconUrl(apmSpaceApp.getIconUrl());
        apmSpaceApp.setConfigContent(initSortConfig(apmSpaceApp.getConfigContent(), apmSpaceApp.getModelCode()));
        apmSpaceApp.setCreatedTime(new Date());
        apmSpaceApp.setUpdatedTime(new Date());
        apmSpaceApp.setGroupName(apmSpaceAppAo.getGroupName());
        apmSpaceApp.setDeleteFlag(CommonConst.DELETE_FLAG_NOT_DELETED);
        apmSpaceApp.setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE);
        // 构造空间对象的领域信息
        ApmSphereAO apmSphereAo = new ApmSphereAO();
        apmSphereAo.setType(OBJECT.getCode());
        apmSphereAo.setBizBid(apmSpaceApp.getBid());
        apmSphereAo.setName(apmSpaceApp.getName());
        apmSphereAo.setPBizBid(apmSpaceApp.getSpaceBid());
        apmSphereAo.setPType(SPACE.getCode());
        // 保存对象领域信息
        ApmSphere sphere = sphereDomainService.add(apmSphereAo);
        apmSpaceApp.setSphereBid(sphere.getBid());
        // 保存空间对象信息
        apmSpaceAppService.save(apmSpaceApp);
        // 保存默认角色(全部成员)信息
        addDefaultRoleByAll(sphere.getBid());
        //新增应用初始化条目和规则
        boolean b = permissionConfigService.copyPermissionByModelCode(apmSpaceApp);
        return apmSpaceApp;
    }


    /**
     * 初始化应用实例排序配置'
     *
     * @param configContent
     * @param modelCode
     * @return
     */
    private JSONObject initSortConfig(JSONObject configContent, String modelCode) {
        List<Order> orders = Lists.newArrayList();
        if (DEMAND_MODEL_CODE.equals(modelCode)) {
            Order sortOrder = Order.of().setProperty(ObjectTreeEnum.SORT.getCode()).setDesc(false);
            orders.add(sortOrder);
        }
        if (PLAN_TASK_MODEL_CODE.equals(modelCode)) {
            Order sortOrder = Order.of().setProperty(ObjectTreeEnum.SORT.getCode()).setDesc(true);
            orders.add(sortOrder);
        }
        Order createdTimeOrder = Order.of().setProperty(BaseDataEnum.CREATED_TIME.getCode()).setDesc(!PLAN_TASK_MODEL_CODE.equals(modelCode));
        orders.add(createdTimeOrder);
        configContent.put("sortList", orders);
        return configContent;
    }

    @Override
    @CacheEvict(value = CacheNameConstant.SPACE_APP, key = "#apmSpaceAppAo.bid")
    public Boolean operationApp(ApmSpaceAppAo apmSpaceAppAo) {
        ApmSpaceApp apmSpaceApp = ApmSpaceAppConverter.INSTANCE.ao2Entity(apmSpaceAppAo);
        return apmSpaceAppService.update(Wrappers.<ApmSpaceApp>lambdaUpdate()
                .set(Objects.nonNull(apmSpaceApp.getVisibleFlag()), ApmSpaceApp::getVisibleFlag, apmSpaceApp.getVisibleFlag())
                .set(Objects.nonNull(apmSpaceApp.getDeleteFlag()), ApmSpaceApp::getDeleteFlag, apmSpaceApp.getDeleteFlag())
                .set(Objects.nonNull(apmSpaceApp.getEnableFlag()), ApmSpaceApp::getEnableFlag, apmSpaceApp.getEnableFlag())
                .set(Objects.nonNull(apmSpaceApp.getName()), ApmSpaceApp::getName, apmSpaceApp.getName())
                .set(Objects.nonNull(apmSpaceApp.getGroupName()), ApmSpaceApp::getGroupName, apmSpaceApp.getGroupName())
                .set(Objects.nonNull(apmSpaceApp.getIconUrl()), ApmSpaceApp::getIconUrl, apmSpaceApp.getIconUrl())
                .set(ApmSpaceApp::getUpdatedBy, SsoHelper.getJobNumber())
                .set(ApmSpaceApp::getUpdatedTime, new Date())
                .set(Objects.nonNull(apmSpaceApp.getConfigContent()), ApmSpaceApp::getConfigContent, apmSpaceApp.getConfigContent())
                .set(Objects.nonNull(apmSpaceApp.getIsVersionObject()), ApmSpaceApp::getIsVersionObject, apmSpaceApp.getIsVersionObject())
                .eq(ApmSpaceApp::getBid, apmSpaceApp.getBid()));
    }


    public List<ApmSpace> listApmSpaceByJobNum(String jobNum, ApmSpaceQo apmSpaceQo) {
        // 防止空指针
        if (apmSpaceQo == null) {
            apmSpaceQo = new ApmSpaceQo();
        }
        // 获取用户查看空间的权限信息
        ApmSpaceViewPermissionBo apmSpaceViewPermissionBo = getSpaceViewPermission(jobNum);
        // 忽略权限，直接返回没 带有权限的空间的过滤逻辑
        if (Boolean.TRUE.equals(apmSpaceViewPermissionBo.getIgnorePermission())) {
            return apmSpaceService.list(
                    Wrappers.<ApmSpace>lambdaQuery()
                            .like(StringUtil.isNotBlank(apmSpaceQo.getName()), ApmSpace::getName, apmSpaceQo.getName())
                            .eq(apmSpaceQo.getTemplateFlag() != null, ApmSpace::getTemplateFlag, apmSpaceQo.getTemplateFlag())
                            .eq(ApmSpace::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
            );
        }
        // 非管理员需要根据权限过滤空间
        Set<String> spaceBids = apmSpaceViewPermissionBo.getPermissionSpaceBidSet();
        // 空的场景直接返回
        if (CollectionUtils.isEmpty(spaceBids)) {
            return Collections.emptyList();
        }
        // 通过spaceBid查询空间信息
        return apmSpaceService.list(
                Wrappers.<ApmSpace>lambdaQuery()
                        .in(ApmSpace::getBid, spaceBids)
                        .like(StringUtil.isNotBlank(apmSpaceQo.getName()), ApmSpace::getName, apmSpaceQo.getName())
                        .eq(apmSpaceQo.getTemplateFlag() != null, ApmSpace::getTemplateFlag, apmSpaceQo.getTemplateFlag())
                        .eq(ApmSpace::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
        );
    }

    private ApmSpaceViewPermissionBo getSpaceViewPermission(String jobNum) {
        Set<String> permissionSpaceBids = null;
        Set<String> userSet = globalRoleProperties.getAdministrators();
        if (CollectionUtils.isNotEmpty(userSet) && userSet.contains(jobNum)) {
            return ApmSpaceViewPermissionBo.of().setIgnorePermission(Boolean.TRUE);
        }
        permissionSpaceBids = collectPermissionSpaceBids(jobNum);
        return ApmSpaceViewPermissionBo.of()
                .setPermissionSpaceBidSet(permissionSpaceBids);
    }

    /**
     * 收集有权限的空间Bid
     *
     * @param jobNum
     * @return
     */
    @NotNull
    private Set<String> collectPermissionSpaceBids(String jobNum) {
        Set<String> spaceBids = Sets.newHashSet();
        //特殊逻辑，如果是sr实例的UX代表，技术负责人，那么也有该空间权限
        Set<String> specialSpaceBid = getSpecialSpaceBid(jobNum);
        if (CollectionUtils.isNotEmpty(specialSpaceBid)) {
            spaceBids.addAll(specialSpaceBid);
        }
        // 查询当前登录人以及其部门的角色信息
        ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(jobNum);
        // 这里只能查到最底层的部门
        List<String> identityQueryList = apmUser.getDepts().stream().map(DepartmentDTO::getDeptNo).collect(Collectors.toList());
        // 需要根据最底层的部门向上反查所有的部门
        List<DepartmentDTO> parentDeptList = platformUserWrapper.getParentDepartmentByDepartmentId(apmUser.getDepartmentList().get(0));
        for (DepartmentDTO departmentDTO : parentDeptList) {
            identityQueryList.add(String.valueOf(departmentDTO.getDeptNo()));
        }
        // 把员工工号也放入到查询列表中
        identityQueryList.add(jobNum);
        List<ApmRoleIdentity> roleIdentities = apmRoleIdentityService.list(
                Wrappers.<ApmRoleIdentity>lambdaQuery()
                        .eq(ApmRoleIdentity::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmRoleIdentity::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
                        .in(ApmRoleIdentity::getIdentity, identityQueryList)
        );
        if (CollUtil.isEmpty(roleIdentities)) {
            return spaceBids;
        }
        // 过滤掉角色为空的数据
        Set<String> roleBids = roleIdentities.parallelStream()
                .map(ApmRoleIdentity::getRoleBid).filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        // 通过roleBid查询角色信息获取域信息
        List<ApmRole> apmRoles = apmRoleService.list(
                Wrappers.<ApmRole>lambdaQuery()
                        .eq(ApmRole::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmRole::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
                        .in(ApmRole::getBid, roleBids)
        );
        if (CollUtil.isEmpty(apmRoles)) {
            return spaceBids;
        }
        Set<String> sphereBids = apmRoles.parallelStream().map(ApmRole::getSphereBid).filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        // 这里的域bid包含了空间域、应用域和实例域，需要把应用域和实例域对应的空间域也查出来
        List<String> spaceSphereBidList = apmSphereService.querySpaceSphereBidBySphereBid(sphereBids);
        // 通过sphereBid查询域信息获取空间信息
        List<ApmSphere> apmSpheres = apmSphereService.list(
                Wrappers.<ApmSphere>lambdaQuery()
                        .in(ApmSphere::getBid, spaceSphereBidList)
                        .eq(ApmSphere::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmSphere::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
                        .eq(ApmSphere::getType, SPACE.getCode())
        );
        if (CollUtil.isEmpty(apmSpheres)) {
            return spaceBids;
        }
        spaceBids.addAll(apmSpheres.parallelStream().map(ApmSphere::getBizBid).collect(Collectors.toSet()));
        return spaceBids;
    }

    private Set<String> getSpecialSpaceBid(String jobNum) {
        //查询所有项目
        QueryWrapper qo = new QueryWrapper();
        qo.like("ux_score", jobNum).or().like("person_responsible", jobNum);
        List<QueryWrapper> wrappers =  QueryWrapper.buildSqlQo(qo);
        List<MObject> srDataList = objectModelCrudI.list("A02",wrappers);
        if (CollectionUtils.isNotEmpty(srDataList)) {
            return srDataList.stream().map(v->String.valueOf(v.get(SpaceAppDataEnum.SPACE_BID.getCode()))).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    /**
     * 通过空间bid与空间应用bid，获取对象类型信息
     *
     * @param spaceAppBid 空间应用bid
     * @return
     */
    @Override
    @Cacheable(value = CacheNameConstant.APM_SPACE_APP_MODELCODE, key = "#spaceAppBid")
    public String getModelCodeBySpaceAppBid(String spaceAppBid) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        Assert.notNull(apmSpaceApp, "空间应用不存在");
        return apmSpaceApp.getModelCode();
    }

    /**
     * 通过空间bid与空间bid，获取对象类型信息
     *
     * @param spaceAppBid 空间应用bid
     * @return
     */
    @Override
    @Cacheable(value = CacheNameConstant.APM_SPACE_APP_SPACEBID, key = "#spaceAppBid")
    public String getSpaceBidBySpaceAppBid(String spaceAppBid) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        return apmSpaceApp.getSpaceBid();
    }

    /**
     * 获取空间应用列表
     *
     * @param spaceBid 空间Bid
     * @return
     */
    @Override
    public List<ApmSpaceAppVo> listApp(String spaceBid) {
        List<ApmSpaceApp> apmSpaceApps = apmSpaceAppService.list(
                Wrappers.<ApmSpaceApp>lambdaQuery()
                        .eq(ApmSpaceApp::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmSpaceApp::getSpaceBid, spaceBid)
                        .orderByAsc(ApmSpaceApp::getSort)
        );
        List<ApmSpaceAppVo> apmSpaceAppVos = CollectionUtils.copyList(apmSpaceApps, ApmSpaceAppVo.class);
        for (ApmSpaceAppVo apmSpaceAppVo : apmSpaceAppVos) {
            if (batchImportSpaceAppBids.contains(apmSpaceAppVo.getBid()) || batchImportModelCodes.contains(apmSpaceAppVo.getModelCode())) {
                apmSpaceAppVo.setBatchImport(true);
            }
        }
        return apmSpaceAppVos;
    }

    @Override
    @Cacheable(value = CacheNameConstant.APM_SPACE_APP, key = "#spaceAppBid")
    public ApmSpaceApp getSpaceAppByBid(String spaceAppBid) {
        return apmSpaceAppService.getByBid(spaceAppBid);
    }

    @Override
    public List<ApmRoleVO> listRoleAndPermissionBySphereBid(String sphereBid) {
        List<ApmRoleVO> result = new ArrayList<>();
        //添加内置角色
        ApmRoleVO apmRoleVO = new ApmRoleVO();
        apmRoleVO.setBid(InnerRoleEnum.CREATER.getCode());
        apmRoleVO.setName(InnerRoleEnum.CREATER.getDesc());
        apmRoleVO.setCode(InnerRoleEnum.CREATER.getCode());
        result.add(apmRoleVO);
        if (StrUtil.isBlank(sphereBid)) {
            return result;
        }
        result.addAll(apmRoleService.getRoleListBySphereBid(sphereBid));
        return result;
    }

    /**
     * 更改是否为模板空间
     *
     * @param bid          空间bid
     * @param templateFlag 模板标识（1：是模板，0：不是模板）
     * @return Boolean
     */
    @Override
    public Boolean changeTemplateSpace(String bid, Boolean templateFlag) {
        ApmSpace apmSpace = apmSpaceService.getOne(Wrappers.<ApmSpace>lambdaQuery().eq(ApmSpace::getBid, bid));
        apmSpace.setTemplateFlag(templateFlag);
        apmSpace.setUpdatedBy(SsoHelper.getJobNumber());
        apmSpace.setUpdatedTime(new Date());
        return apmSpaceService.updateById(apmSpace);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ApmRoleVO> queryOrCreateTeam(String spaceBid, String spaceAppBid, String bid) {
        List<ApmRoleVO> result = Lists.newArrayList();
        // 查询实例 团队sphere是否创建，没创建先创建，有则直接查询团队
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(bid, TypeEnum.INSTANCE.getCode());
        if (Objects.nonNull(apmSphere)) {
            // 通过空间bid和域类型查询域信息
            return apmRoleService.getRoleListBySphereBid(apmSphere.getBid());
        } else {
            // 构造空间对象的领域信息
            ApmSphereAO apmSphereAo = new ApmSphereAO();
            apmSphereAo.setType(TypeEnum.INSTANCE.getCode());
            apmSphereAo.setName("团队");
            apmSphereAo.setBizBid(bid);
            apmSphereAo.setPBizBid(spaceAppBid);
            apmSphereAo.setPType(OBJECT.getCode());
            // 保存对象领域信息
            ApmSphere addApmSphere = sphereDomainService.add(apmSphereAo);
            // 保存默认角色 全部成员
            ApmRole apmRole = addDefaultRoleByAll(addApmSphere.getBid());
            result.add(ApmRoleConverter.INSTANCE.entity2VO(apmRole));
        }
        return result;
    }

    /**
     * 保存默认角色 全部成员
     *
     * @param sphereBid
     * @return
     */
    private ApmRole addDefaultRoleByAll(String sphereBid) {
        String roleBid = SnowflakeIdWorker.nextIdStr();
        ApmRole apmRole = new ApmRole()
                .setCode(RoleConstant.SPACE_ALL_EN).setName(RoleConstant.SPACE_ALL_CH).setDescription(RoleConstant.SPACE_ALL_CH)
                .setBid(roleBid).setPbid(RoleConstant.ROOT_BID)
                .setPath(RoleConstant.ROOT_BID + RoleConstant.SEMICOLON + roleBid).setSphereBid(sphereBid)
                .setCreatedBy(SsoHelper.getJobNumber()).setUpdatedBy(SsoHelper.getJobNumber()).setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE);
        apmRoleService.save(apmRole);
        return apmRole;
    }

    @Override
    public Boolean sortApp(List<ApmSpaceAppAo> apmSpaceAppAoList) {
        if (CollUtil.isEmpty(apmSpaceAppAoList)) {
            return Boolean.FALSE;
        }
        List<ApmSpaceApp> apmSpaceApps = ApmSpaceAppConverter.INSTANCE.aoList2EntityList(apmSpaceAppAoList);
        apmSpaceAppService.updateSort(apmSpaceApps);
        return Boolean.TRUE;
    }

    @Override
    public String getModelCodeByAppBid(String spaceAppBid) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        return null == apmSpaceApp ? "" : apmSpaceApp.getModelCode();
    }

    @Override
    @Cacheable(value = CacheNameConstant.SPACE, key = "#foreignBid")
    public ApmSpaceVo getApmSpaceByForeignBid(String foreignBid) {
        if (StringUtils.isBlank(foreignBid)) {
            return null;
        }
        ApmSpace apmSpace = apmSpaceService.getOne(Wrappers.<ApmSpace>lambdaQuery().eq(ApmSpace::getForeignBid, foreignBid));
        return ApmSpaceConverter.INSTANCE.po2vo(apmSpace);
    }

    @Override
    public List<ApmRoleVO> listRoleAndSystemRole(ApmSpaceRoleQO apmSpaceRoleQo) {
        ApmSphere sphere = apmSphereService.getByBizBidAndType(apmSpaceRoleQo.getBid(), apmSpaceRoleQo.getSphereType());
        if (Objects.isNull(sphere) || Objects.isNull(sphere.getBid())) {
            return Lists.newArrayList();
        }
        // 先查当前域下面的角色
        List<ApmRoleVO> roleList = apmRoleService.getRoleListBySphereBid(sphere.getBid());
        // 收集域角色的Code,用于过滤全局系统角色
        List<String> roleCodeList = roleList.stream().map(ApmRoleVO::getCode).collect(Collectors.toList());
        // 查系统角色
        List<CfgRoleVo> cfgRoleVos = sysRoleDomainService.listSysRole();
        List<ApmRoleVO> sysRoleList = cfgRoleVos.stream()
                .filter(cfgRoleVo -> !roleCodeList.contains(cfgRoleVo.getCode()))
                .map(cfgRoleVo -> {
                    ApmRoleVO apmRoleVO = new ApmRoleVO();
                    apmRoleVO.setCode(cfgRoleVo.getCode());
                    apmRoleVO.setName(cfgRoleVo.getName());
                    return apmRoleVO;
                }).collect(Collectors.toList());
        roleList.addAll(sysRoleList);
        return roleList;
    }

}
