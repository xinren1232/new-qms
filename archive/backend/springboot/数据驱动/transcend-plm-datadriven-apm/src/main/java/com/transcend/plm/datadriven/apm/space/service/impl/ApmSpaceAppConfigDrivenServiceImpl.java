package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.open.entity.EmployeeInfo;
import com.transcend.framework.open.service.OpenUserService;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgViewFeignClient;
import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.configcenter.api.model.object.enums.CfgSysAppTabEnum;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.config.ObjectVo;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.relation.RelationTypeConstant;
import com.transcend.plm.datadriven.apm.common.TranscendForgePublicLogin;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.constants.RemoteDataConstant;
import com.transcend.plm.datadriven.apm.constants.ViewModelConstant;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowTemplateVO;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowApplicationService;
import com.transcend.plm.datadriven.apm.notice.PushCenterProperties;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmAccessOperationAo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.*;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmAccess;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleAccess;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmAccessService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleAccessService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionConfigService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmAccessDomainService;
import com.transcend.plm.datadriven.apm.space.controller.ApmSpaceAppDataDrivenController;
import com.transcend.plm.datadriven.apm.space.controller.ApmSpaceAppRelationDataDrivenController;
import com.transcend.plm.datadriven.apm.space.converter.ApmSpaceAppCustomViewConverter;
import com.transcend.plm.datadriven.apm.space.converter.ApmSpaceAppViewModelConverter;
import com.transcend.plm.datadriven.apm.space.enums.EntranceEnum;
import com.transcend.plm.datadriven.apm.space.enums.ViewFieldDataTypeEnum;
import com.transcend.plm.datadriven.apm.space.model.AppViewModelEnum;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.*;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmAccessQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeAccessQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceTabQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.*;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceAppMapper;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceAppViewModelMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.*;
import com.transcend.plm.datadriven.apm.space.repository.service.*;
import com.transcend.plm.datadriven.apm.space.service.*;
import com.transcend.plm.datadriven.apm.space.utils.CustomCellWriteHeightConfig;
import com.transcend.plm.datadriven.apm.space.utils.CustomCellWriteWidthConfig;
import com.transcend.plm.datadriven.apm.space.utils.EasyExcelUtils;
import com.transcend.plm.datadriven.apm.tools.CheckUtils;
import com.transcend.plm.datadriven.apm.tools.ParentBidHandler;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.support.external.object.CfgObjectService;
import com.transsion.framework.common.WebUtil;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.dto.BaseResponse;
import com.transsion.framework.exception.BusinessException;
import com.transsion.framework.http.util.HttpUtil;
import com.transsion.framework.uac.model.dto.TokenDTO;
import com.transsion.framework.uac.model.request.TokenRequest;
import com.transsion.framework.uac.service.IUacTokenService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.management.relation.RelationService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.constants.DictionaryConstant.BUTTON_CODE_GROUP;
import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.*;
import static com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum.OPERATOR_ORDER_LIST;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Service
public class ApmSpaceAppConfigDrivenServiceImpl implements IApmSpaceAppConfigDrivenService {
    private static final Logger log = LoggerFactory.getLogger(ApmSpaceAppConfigDrivenServiceImpl.class);
    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ApmSpaceAppTabService apmSpaceAppTabService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;

    @Resource
    private ApmSpaceAppViewModelService apmSpaceAppViewModelService;
    @Resource
    private IApmSpaceAppConfigManageService iAmSpaceAppConfigManageService;
    @Resource
    PushCenterProperties pushCenterProperties;
    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationFeignClient;

    @Resource
    private ApmAccessDomainService apmAccessDomainService;

    @Resource
    private ApmAccessService apmAccessService;

    @Resource
    private ApmRoleAccessService apmRoleAccessService;
    @Resource
    private ApmRoleService apmRoleService;
    @Resource
    private IApmRoleDomainService iApmRoleDomainService;
    @Resource
    private ApmSpaceAppCustomViewRoleService apmSpaceAppCustomViewRoleService;
    @Resource
    private ApmSpaceService apmSpaceService;
    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private CfgViewFeignClient viewFeignClient;
    @Resource
    private ApmSpaceAppMapper apmSpaceAppMapper;

    @Resource
    private ApmSpaceAppViewModelMapper apmSpaceAppViewModelMapper;
    @Resource
    private ApmSpaceAppCustomViewService apmSpaceAppCustomViewService;

    @Resource
    private CfgObjectService cfgObjectService;

    @Resource
    private ApmAppExportTemplateService apmAppExportTemplateService;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Resource
    private IBaseApmSpaceAppRelationDataDrivenService baseApmSpaceAppRelationDataDrivenService;

    @Resource
    private ApmAppTabHeaderService apmAppTabHeaderService;

    @Resource
    private DictionaryFeignClient dictionaryFeignClient;

    @Resource
    private DefaultAppExcelTemplateService defaultAppExcelTemplateService;

    @Resource
    private OpenUserService openUserService;

    @Resource
    private ApmSpaceAppDataDrivenController apmSpaceAppDataDrivenController;

    @Resource
    private ApmSpaceAppRelationDataDrivenController apmSpaceAppRelationDataDrivenController;

    @Resource
    private ApmFlowApplicationService apmFlowApplicationService;

    @Resource
    private IPermissionConfigService permissionConfigService;

    @Resource
    IUacTokenService uacTokenService;

    private static final String MODEL_CODE_LABEL = "A04";
    private static final String MODEL_CODE_SPACE = "100";



    /**
     * @param spaceAppBid
     * @return {@link List }<{@link AppViewModelDto }>
     */
    @Override
    @Cacheable(value = CacheNameConstant.SPACE_APP_VIEW_MODEL_LIST, key = "#spaceAppBid")
    public List<AppViewModelDto> listViewModel(String spaceAppBid) {
        ApmSpaceAppViewModelPo viewModel = new ApmSpaceAppViewModelPo();
        viewModel.setSpaceAppBid(spaceAppBid);
        viewModel.setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE);
        List<ApmSpaceAppViewModelPo> resultViewModels = apmSpaceAppViewModelService.listByCondition(viewModel);
        return ApmSpaceAppViewModelConverter.INSTANCE.pos2vos(resultViewModels);
    }

    /**
     * 根据权限查询viewModel
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param entrance    entrance
     * @param tabBid
     * @return List<AppViewModelDto>
     */
    @Override
    public List<AppViewModelDto> listViewModelByPermission(String spaceBid, String spaceAppBid, String entrance, String tabBid, Boolean inverseQuery) {
        // 查出所有viewModel (确保缓存生效)
        List<AppViewModelDto> appViewModelDtos = ((IApmSpaceAppConfigDrivenService) AopContext.currentProxy()).listViewModel(spaceAppBid);
        // 如果没有viewModel，直接返回空集合
        if (CollectionUtils.isEmpty(appViewModelDtos)) {
            return appViewModelDtos;
        }

        // 判断入口(目前关系入口 会强行控制显示隐藏) + 一定需要tabBid
        if (EntranceEnum.RELATION.getCode().equals(entrance) && StringUtils.isNotEmpty(tabBid)) {
            // 查询显示tab 视图模式的控制数据 如：showViewModels:["multiTree"，"tableView"]
            ApmSpaceAppTab apmSpaceAppTab = apmSpaceAppTabService.getByBid(tabBid);
            // 如果找不到，直接返回（相当于不处理）
            if (null == apmSpaceAppTab) {
                return appViewModelDtos;
            }
            List<String> showViewModels = apmSpaceAppTab.getShowViewModels();
            // 不为空的情况处理控制 viewModels
            if (CollectionUtils.isNotEmpty(showViewModels)) {
                Set<String> showViewModelSet = new HashSet<>(showViewModels);
                // 匹配 showViewModelSet才保留
                appViewModelDtos = appViewModelDtos.stream()
                        .filter(appViewModelDto -> showViewModelSet.contains(appViewModelDto.getCode()))
                        .collect(Collectors.toList());
            }
        }

        // 如果当前用户是超级管理员, 以及空间管理员，直接返回所有viewModel
        if (Boolean.TRUE.equals(iApmRoleDomainService.isGlobalAdmin()) || Boolean.TRUE.equals(iApmRoleDomainService.isSpaceAdmin(spaceBid))) {
            return inverseQueryFilter(inverseQuery, appViewModelDtos);
        }

        // 获取当前用户权限
        List<ApmActionConfigUserVo> apmActionConfigUserVos = currentUserPermissionAccess(spaceAppBid);
        // 如果没有权限，直接返回空集合
        if (CollectionUtils.isEmpty(apmActionConfigUserVos)) {
            return Lists.newArrayList();
        }

        // 检查用户是否有全部视图权限
        // 收集所有权限的action到一个Set中，便于快速查找
        Set<String> userPermissions = apmActionConfigUserVos.stream()
                .map(ApmActionConfigUserVo::getAction)
                .collect(Collectors.toSet());

        // 如果有全部视图权限，直接返回所有视图模型
        if (userPermissions.contains(ViewModelConstant.VIEW_MODEL_ALL)) {
            return inverseQueryFilter(inverseQuery, appViewModelDtos);
        }

        // 筛选出用户有权限的视图模型
        appViewModelDtos = appViewModelDtos.stream()
                .filter(viewModel ->
                        // viewModelPrefix，与viewModelCode拼接后，才去匹配用户权限
                        userPermissions.contains(ViewModelConstant.VIEW_MODEL_PREFIX + viewModel.getCode()))
                .collect(Collectors.toList());
        return inverseQueryFilter(inverseQuery, appViewModelDtos);
    }

    @Nullable
    private static List<AppViewModelDto> inverseQueryFilter(Boolean inverseQuery, List<AppViewModelDto> appViewModelDtos) {
        if (Boolean.TRUE.equals(inverseQuery)) {
            return appViewModelDtos.stream().filter(dto -> "tableView".equals(dto.getCode())).collect(Collectors.toList());
        }
        return appViewModelDtos;
    }

    /**
     * 查询当前登陆人操作权限 多树结构
     *
     * @param spaceBid    当前空间业务ID
     * @param spaceAppBid 当前空间应用业务ID
     * @param instanceBid 实例业务ID
     * @param query       查询条件
     * @return List<ApmActionConfigUserVo> 当前用户的操作权限列表
     */
    @Override
    public List<ApmActionConfigUserVo> currentUserAccessByMultiTree(String spaceBid, String spaceAppBid, String instanceBid, ApmMultiTreeAccessQo query) {

        // 获取实例数据和目标模型代码
        MBaseData instanceData = query.getInstanceData();
        if (ObjectUtils.isEmpty(instanceData) && StringUtils.isNotEmpty(spaceAppBid)) {
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
            if (ObjectUtils.isNotEmpty(apmSpaceApp)) {
                instanceData = objectModelCrudI.getByBid(apmSpaceApp.getModelCode(), instanceBid);
                if (ObjectUtils.isEmpty(instanceData)){
                    return Lists.newArrayList();
                }
            }
        }
        String targetModelCode = query.getTargetModelCode();
        String targetSpaceAppBid = query.getTargetSpaceAppBid();
        String entrance = query.getEntrance();

        // Step 1: 获取当前用户权限
        ApmAccessQo apmAccessQo = new ApmAccessQo();
        apmAccessQo.setInstance(instanceData);
        apmAccessQo.setSpaceBid(query.getSpaceBid());
        apmAccessQo.setSpaceAppBid(query.getSpaceAppBid());
        List<ApmActionConfigUserVo> apmActionConfigUserVos = currentUserPermissionAccess(spaceAppBid, instanceBid, apmAccessQo);

        // 移除当前权限中的 ADD 操作（注：因为tree 新增是新增子节点，不是当前节点，因此需要去掉当前节点的新增）
        removeAddPermissions(apmActionConfigUserVos);

        // 判断入口(目前关系入口才需要处理，子节点新增权限，其他情况直接返回结果即可)
        if (StringUtils.isEmpty(entrance) || !EntranceEnum.RELATION.getCode().equals(entrance)) {
            return apmActionConfigUserVos;
        }

        // Step 2: 如果目标空间应用业务ID为空，尝试根据 targetModelCode(不为空) 和 spaceBid 查询
        if (StringUtils.isEmpty(targetSpaceAppBid)) {

            // 如果目标模型代码为空，直接返回当前用户权限
            if (StringUtils.isEmpty(targetModelCode)) {
                return apmActionConfigUserVos;
            }
            ApmSpaceApp bySpaceBidAndModelCode = apmSpaceAppService.getBySpaceBidAndModelCode(spaceBid, targetModelCode);
            if (bySpaceBidAndModelCode == null) {
                // 如果查询不到目标应用，直接返回当前用户权限
                return apmActionConfigUserVos;
            }
            targetSpaceAppBid = bySpaceBidAndModelCode.getBid();
        }

        // Step 3: 查询目标应用的新增权限（仅保留 ADD 操作）
        List<ApmActionConfigUserVo> targetSpaceAppActionConfigUserVos = getAddPermissions(targetSpaceAppBid);

        // Step 4: 合并目标权限到当前用户权限
        if (CollectionUtils.isNotEmpty(targetSpaceAppActionConfigUserVos)) {
            //特殊逻辑，如果关系tab页没有新增目标实例权限，此处也不返回新增权限
            if (Boolean.TRUE.equals(checkRelationTabAddPermission(instanceData, targetModelCode))) {
                apmActionConfigUserVos.addAll(targetSpaceAppActionConfigUserVos);
            }
        }
        return apmActionConfigUserVos;
    }

    /**
     * 移除权限列表中的 ADD 操作
     *
     * @param permissions 权限列表
     */
    private void removeAddPermissions(List<ApmActionConfigUserVo> permissions) {
        if (CollectionUtils.isNotEmpty(permissions)) {
            permissions.removeIf(permission -> OperatorEnum.ADD.getCode().equals(permission.getAction()));
        }
    }

    /**
     * 获取指定目标空间应用的 ADD 权限
     *
     * @param targetSpaceAppBid 目标空间应用业务ID
     * @return List<ApmActionConfigUserVo> 过滤后的 ADD 权限列表
     */
    private List<ApmActionConfigUserVo> getAddPermissions(String targetSpaceAppBid) {
        // 查询目标空间应用的所有权限
        List<ApmActionConfigUserVo> targetPermissions = currentUserPermissionAccess(targetSpaceAppBid);

        // 筛选出 ADD 权限
        return targetPermissions.stream()
                .filter(vo -> OperatorEnum.ADD.getCode().equals(vo.getAction()))
                .collect(Collectors.toList());
    }

    /**
     * 校验关系table页新增目标对象权限
     *
     * @param instanceData instanceData
     * @return Boolean Boolean
     */
    private Boolean checkRelationTabAddPermission(MBaseData instanceData, String targetModelCode) {
        //查询关系对象
        List<CfgObjectRelationVo> cfgObjectRelationVos = cfgObjectRelationFeignClient.listRelationBySTModelCode(
                instanceData.get(ObjectEnum.MODEL_CODE.getCode()).toString(), targetModelCode).getCheckExceptionData();
        if (CollectionUtils.isEmpty(cfgObjectRelationVos)){
            return true;
        }
        CfgObjectRelationVo cfgObjectRelationVo = cfgObjectRelationVos.get(0);

        //如果关系对象类型为仅选取，则直接返回当前用户权限
        if (RelationTypeConstant.SELECT_ONLY.equals(cfgObjectRelationVo.getType())) {
            return false;
        }
        ApmSpaceAppTab relationTab = apmSpaceAppTabService.getBySpaceAppBidAndRelationModelCode(instanceData.get(ObjectEnum.SPACE_APP_BID.getCode()).toString(), cfgObjectRelationVo.getModelCode());
        if (relationTab == null) {
            return true;
        }
        List<String> roleCodes = permissionConfigService.listRoleCodeByBaseData(instanceData.get(ObjectEnum.SPACE_APP_BID.getCode()).toString(),instanceData);
        MObject mObject = new MObject();
        mObject.putAll(instanceData);
        return iAmSpaceAppConfigManageService.checkTabPermission(relationTab, roleCodes, mObject);
    }


    /**
     * 根据权限查询viewModel
     *
     * @param apmSpaceTabQo apmSpaceTabQo
     * @return List<AppViewModelDto>
     */
    @Override
    public List<ApmObjectRelationAppVo> listRelationTabByPermission(ApmSpaceTabQo apmSpaceTabQo) {

        List<ApmObjectRelationAppVo> cfgObjectRelationVos = iAmSpaceAppConfigManageService.relationAppListTab(apmSpaceTabQo);
        // 如果没有数据，直接返回空集合
        if (CollectionUtils.isEmpty(cfgObjectRelationVos)) {
            return Lists.newArrayList();
        }

        String spaceAppBid = apmSpaceTabQo.getSpaceAppBid();
        String spaceBid = apmSpaceTabQo.getSpaceBid();
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        // 目前 开发管理才会处理权限 tab。临时处理。后续用权限管理
        handleApmObjectRelationAppVos(app, spaceBid, cfgObjectRelationVos, spaceAppBid);
        return cfgObjectRelationVos;
    }

    /**
     * 处理权限 TODO 待优化
     *
     * @param app
     * @param spaceBid
     * @param cfgObjectRelationVos
     * @param spaceAppBid
     */
    @Nullable
    private void handleApmObjectRelationAppVos(ApmSpaceApp app, String spaceBid, List<ApmObjectRelationAppVo> cfgObjectRelationVos, String spaceAppBid) {
        if ("201".equals(app.getModelCode())) {
            // 如果当前用户是超级管理员, 以及空间管理员，直接返回所有viewModel
            if (Boolean.TRUE.equals(iApmRoleDomainService.isGlobalAdmin()) || Boolean.TRUE.equals(iApmRoleDomainService.isSpaceAdmin(spaceBid))) {
                return;
            }
            // 获取当前用户权限
            List<ApmActionConfigUserVo> apmActionConfigUserVos = currentUserPermissionAccess(spaceAppBid);
            // 如果没有权限，直接返回空集合
            boolean hasKanbanPermission = !CollectionUtils.isEmpty(apmActionConfigUserVos);
            // 收集所有权限的action到一个Set中，便于快速查找
            Set<String> userPermissions = apmActionConfigUserVos.stream()
                    .map(ApmActionConfigUserVo::getAction)
                    .collect(Collectors.toSet());
            // 如果有全部视图权限，直接返回所有视图模型
            if (userPermissions.contains("TAB_KANBAN")) {
                return;
            }
            // 不匹配，需要设置不显示 info (无权限 + 等于baseInfo)
            cfgObjectRelationVos.stream().filter(cfgObjectRelationVo ->
                            Boolean.FALSE.equals(hasKanbanPermission) && "baseInfo".equals(cfgObjectRelationVo.getCode())
                    )
                    .forEach(cfgObjectRelationVo ->
                            cfgObjectRelationVo.setChecked(Boolean.FALSE)
                    );
        }
    }


    /**
     * @param spaceAppBid
     * @return {@link List }<{@link CfgViewMetaDto }>
     */
    @Override
    public List<CfgViewMetaDto> baseViewGetMeteModels(String spaceAppBid) {
        ViewQueryParam param = ViewQueryParam.of()
                .setViewBelongBid(spaceAppBid);
        return viewFeignClient.listMetaModels(param).getCheckExceptionData();
    }

    /**
     * @param spaceAppBid
     * @param ignoreRelationModelCodes
     * @return {@link List }<{@link CfgViewMetaDto }>
     */
    @Override
    public List<CfgViewMetaDto> queryRelationMetaList(String spaceAppBid, List<String> ignoreRelationModelCodes) {
        List<CfgViewMetaDto> relationModuleList = Lists.newArrayList();
        ignoreRelationModelCodes = Optional.ofNullable(ignoreRelationModelCodes).orElse(Lists.newArrayList());
        List<CfgViewMetaDto> cfgViewMetaList = baseViewGetMeteModels(spaceAppBid);
        if (CollectionUtils.isNotEmpty(cfgViewMetaList)) {
            List<String> finalIgnoreRelationModelCodes = ignoreRelationModelCodes;
            relationModuleList = cfgViewMetaList.stream().filter(
                    meta -> ViewComponentEnum.RELATION_CONSTANT.equals(meta.getType()) &&
                            !finalIgnoreRelationModelCodes.contains(meta.getRelationModelCode())
            ).collect(Collectors.toList());
        }
        return relationModuleList;
    }

    /**
     * @param spaceAppBid
     * @return {@link CfgViewVo }
     */
    @Override
    public CfgViewVo tableView(String spaceAppBid) {
        return null;
    }

    /**
     * @param spaceAppBid
     * @param viewModelCode
     * @return {@link AppViewModelDto }
     */
    @Override
    public AppViewModelDto getViewModelDetail(String spaceAppBid, String viewModelCode) {
        return iAmSpaceAppConfigManageService.getViewModelDetail(spaceAppBid, viewModelCode);
    }

    /**
     * 复制视图模式（根据空间应用bid）
     *
     * @param oldSpaceAppBid 旧空间应用bid
     * @param newSpaceAppBid 新空间应用bid
     * @return boolean
     */
    @Override
    public Boolean copyViewModelBySpaceAppBid(String oldSpaceAppBid, String newSpaceAppBid) {
        return apmSpaceAppViewModelMapper.copyBySpaceAppBid(oldSpaceAppBid, newSpaceAppBid);
    }

    /**
     * 批量复制视图模式（根据空间应用bid）
     *
     * @param list 复制内容
     * @return boolean
     */
    @Override
    public Boolean batchCopyViewModelBySpaceAppBid(List<ApmSpaceAppViewModelCopyDto> list) {
        return apmSpaceAppViewModelMapper.batchCopyBySpaceAppBid(list);
    }

    /**
     * 复制空间应用（根据空间bid）
     *
     * @param oldSpaceBid    旧空间bid
     * @param newSpaceBid    新空间bid
     * @param newSpaceAppBid 新空间应用bid
     * @return boolean
     */
    @Override
    public Boolean copySpaceAppBySpaceBid(String oldSpaceBid, String newSpaceBid, String newSpaceAppBid) {
        return apmSpaceAppMapper.copyBySpaceBid(oldSpaceBid, newSpaceBid, newSpaceAppBid);
    }

    /**
     * 复制空间应用（根据空间bid）
     *
     * @param list 复制内容
     * @return boolean
     */
    @Override
    public Boolean batchCopySpaceAppBySpaceBid(List<ApmSpaceAppCopyDto> list) {
        return apmSpaceAppMapper.batchCopyBySpaceBid(list);
    }

    /**
     * @param spaceAppBid
     * @return {@link List }<{@link ApmObjectRelationAppVo }>
     */
    @Override
    public List<ApmObjectRelationAppVo> relationAppListTab(String spaceAppBid) {
        List<ApmObjectRelationAppVo> results = new ArrayList<>();
        // 以目标对象驱动查询的关系列表
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        List<CfgObjectRelationVo> list = cfgObjectRelationFeignClient.listRelationTab(app.getModelCode()).getCheckExceptionData();
        if (CollectionUtils.isEmpty(list)) {
            // 补充默认tab
            defaultTabAppend(results);
            return results;
        }
        String sourceModelCode = app.getModelCode();
        list.forEach(relationInfo -> {
            // 暂时因此源和目标对象一致的关系tab(由于敏捷项目 的产品需求下的需求关系链是 史诗->特性->用户故事，所以暂时这么处理) TODO, 后续通过前端配置Tab完成
            Map<String, Set<String>> relationChainMapTab = new HashMap<>(CommonConstant.START_MAP_SIZE);
            String sourceModelCode1 = relationInfo.getSourceModelCode();

            if (StringUtil.isNotEmpty(sourceModelCode1) && sourceModelCode1.equals(relationInfo.getTargetModelCode())) {
                String mainModelCode = DEMAND_MODEL_CODE;
                // 史诗->特性
                relationChainMapTab.put(DEMAND_EPIC_MODEL_CODE, Sets.newHashSet(DEMAND_SPECIFIC_MODEL_CODE));
                // 特性->用户故事
                relationChainMapTab.put(DEMAND_SPECIFIC_MODEL_CODE, Sets.newHashSet(DEMAND_STORY_MODEL_CODE));
                // 特性->null
                relationChainMapTab.put(DEMAND_STORY_MODEL_CODE, Sets.newHashSet(""));
                // 跳出循环
            }
            List<ApmSpaceApp> appList = apmSpaceAppService.listSpaceAppBySpaceBidAndModelCode(app.getSpaceBid(), relationInfo.getTargetModelCode());
            if (CollectionUtils.isNotEmpty(appList)) {
                relationAppTabAppend(relationInfo, appList, relationChainMapTab, sourceModelCode, results);
            } else {
                ApmObjectRelationAppVo vo = new ApmObjectRelationAppVo();
                BeanUtils.copyProperties(relationInfo, vo);
                vo.setRelationAttr(relationInfo.getRelationAttr());
            }
        });
        // 补充默认tab
        defaultTabAppend(results);
        return results;
    }

    /**
     * @param spaceAppBid
     * @return {@link List }<{@link ApmActionConfigVo }>
     */
    @Override
    public List<ApmActionConfigVo> queryAccessWithRoleBySphereBid(String spaceAppBid) {
        // 通过空间bid查询域信息
        ApmSphere sphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (Objects.isNull(sphere) || StrUtil.isBlank(sphere.getBid())) {
            return Lists.newArrayList();
        }
        return apmAccessDomainService.queryAccessWithRoleBySphereBid(sphere.getBid());
    }

    /**
     * @param spaceAppBidMap
     * @param roleBidMap
     * @param sphereBidMap
     * @return boolean
     */
    @Override
    public boolean copyAccess(Map<String, String> spaceAppBidMap, Map<String, String> roleBidMap, Map<String, String> sphereBidMap) {
        if (CollectionUtils.isEmpty(spaceAppBidMap) || CollectionUtils.isEmpty(roleBidMap) || CollectionUtils.isEmpty(sphereBidMap)) {
            return false;
        }
        List<ApmRoleAccess> apmRoleAccesses = apmRoleAccessService.listBySpaceAppBids(spaceAppBidMap.keySet());
        if (CollectionUtils.isNotEmpty(apmRoleAccesses)) {
            List<String> accessBids = apmRoleAccesses.stream().map(ApmRoleAccess::getAccessBid).collect(Collectors.toList());
            List<ApmAccess> apmAccesses = apmAccessService.listByBidConllection(accessBids);
            Map<String, String> accessMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            Map<String, String> viewBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            for (ApmAccess apmAccess : apmAccesses) {
                String bid = SnowflakeIdWorker.nextIdStr();
                accessMap.put(apmAccess.getBid(), bid);
                viewBidMap.put(apmAccess.getBid() + "#" + apmAccess.getCode(), bid + "#" + apmAccess.getCode());
                apmAccess.setId(null);
                apmAccess.setBid(bid);
            }
            for (int i = apmRoleAccesses.size() - 1; i >= 0; i--) {
                ApmRoleAccess apmRoleAccess = apmRoleAccesses.get(i);
                apmRoleAccess.setId(null);
                apmRoleAccess.setAccessBid(accessMap.get(apmRoleAccess.getAccessBid()));
                apmRoleAccess.setRoleBid(roleBidMap.get(apmRoleAccess.getRoleBid()));
                apmRoleAccess.setSpaceAppBid(spaceAppBidMap.get(apmRoleAccess.getSpaceAppBid()));
                apmRoleAccess.setSphereBid(sphereBidMap.get(apmRoleAccess.getSphereBid()));
                if (StringUtils.isEmpty(apmRoleAccess.getAccessBid()) || StringUtils.isEmpty(apmRoleAccess.getSphereBid()) || StringUtils.isEmpty(apmRoleAccess.getRoleBid())) {
                    apmRoleAccesses.remove(i);
                }
            }
            if (CollectionUtils.isNotEmpty(apmAccesses)) {
                apmAccessService.saveBatch(apmAccesses);
                CfgViewDto cfgViewDto = new CfgViewDto();
                cfgViewDto.setType("copyViewModel");
                cfgViewDto.setViewBidMap(viewBidMap);
                viewFeignClient.copyViews(cfgViewDto);
            }
            apmRoleAccessService.saveBatch(apmRoleAccesses);
        }
        return false;
    }

    /**
     * @param apmAccessOperationAo
     * @param spaceAppBid
     * @return {@link Boolean }
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAccess(ApmAccessOperationAo apmAccessOperationAo, String spaceAppBid) {
        // 非空检验
        if (Objects.isNull(apmAccessOperationAo)) {
            return Boolean.FALSE;
        }
        ApmSphere sphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (Objects.isNull(sphere) || StrUtil.isBlank(sphere.getBid())) {
            return Boolean.FALSE;
        }
        // 重复性校验
        List<ApmActionConfigVo> apmActionConfigVos = apmAccessService.queryAccessWithRoleBySphereBid(sphere.getBid());
        apmActionConfigVos.stream()
                .filter(apmActionConfigVo -> apmActionConfigVo.getActionName().equals(apmAccessOperationAo.getActionName()))
                .findFirst()
                .ifPresent(apmActionConfigVo -> {
                    throw new PlmBizException("该操作已存在， 请检查后再试");
                });
        // 封装ApmAccess实体用于持久化
        ApmAccess apmAccess = new ApmAccess();
        apmAccess.setBid(SnowflakeIdWorker.nextIdStr());
        apmAccess.setCode(apmAccessOperationAo.getAction());
        apmAccess.setName(apmAccessOperationAo.getActionName());
        apmAccess.setResource(apmAccessOperationAo.getResource());
        apmAccess.setDescription(apmAccessOperationAo.getDescription());
        apmAccess.setVisibleConfig(apmAccessOperationAo.getVisibleConfig());
        apmAccess.setOperationConfig(apmAccessOperationAo.getOperationConfig());
        apmAccess.setViewConfig(apmAccessOperationAo.getViewConfig());
        apmAccess.setMethodConfig(apmAccessOperationAo.getMethodConfig());
        apmAccess.setType(apmAccessOperationAo.getType());
        apmAccess.setCreatedBy(SsoHelper.getJobNumber());
        apmAccess.setCreatedTime(new Date());
        apmAccess.setTenantId(100);
        apmAccess.setEnableFlag(1);
        apmAccess.setDeleteFlag(0);
        apmAccess.setSort(apmAccessOperationAo.getSort());
        apmAccess.setIcon(apmAccessOperationAo.getIcon());
        apmAccess.setShowLocation(apmAccessOperationAo.getShowLocation());
        apmAccessService.save(apmAccess);
        // 封装ApmRoleAccess实体用于持久化
        List<ApmRoleAccess> apmRoleAccessList = apmAccessOperationAo.getRoleBids().stream().map(roleBid -> {
            ApmRoleAccess apmRoleAccess = new ApmRoleAccess();
            apmRoleAccess.setRoleBid(roleBid);
            apmRoleAccess.setAccessBid(apmAccess.getBid());
            apmRoleAccess.setSphereBid(sphere.getBid());
            apmRoleAccess.setSpaceAppBid(spaceAppBid);
            apmRoleAccess.setCreatedBy(SsoHelper.getJobNumber());
            apmRoleAccess.setCreatedTime(new Date());
            apmRoleAccess.setUpdatedBy(apmRoleAccess.getCreatedBy());
            apmRoleAccess.setUpdatedTime(apmRoleAccess.getCreatedTime());
            apmRoleAccess.setTenantId(100);
            apmRoleAccess.setEnableFlag(1);
            apmRoleAccess.setDeleteFlag(0);
            return apmRoleAccess;
        }).collect(Collectors.toList());
        return apmRoleAccessService.saveBatch(apmRoleAccessList);
    }

    /**
     * @param apmAccessOperationAo
     * @param spaceAppBid
     * @return {@link Boolean }
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAccess(ApmAccessOperationAo apmAccessOperationAo, String spaceAppBid) {
        // 非空检验
        if (Objects.isNull(apmAccessOperationAo) || StrUtil.isBlank(apmAccessOperationAo.getBid())) {
            return Boolean.FALSE;
        }
        // 通过spaceAppBid查询SphereBid
        ApmSphere sphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (StringUtils.isNotBlank(apmAccessOperationAo.getActionName())) {
            List<ApmActionConfigVo> apmActionConfigVos = apmAccessService.queryAccessWithRoleBySphereBid(sphere.getBid());
            apmActionConfigVos.stream()
                    .filter(apmActionConfigVo -> apmActionConfigVo.getActionName().equals(apmAccessOperationAo.getActionName()) && !apmActionConfigVo.getBid().equals(apmAccessOperationAo.getBid()))
                    .findFirst()
                    .ifPresent(apmActionConfigVo -> {
                        throw new PlmBizException("该操作已存在， 请检查后再试");
                    });
        }
        // 更新ApmAccess
        ApmAccess apmAccess = new ApmAccess();
        apmAccess.setBid(apmAccessOperationAo.getBid());
        apmAccess.setCode(apmAccessOperationAo.getAction());
        apmAccess.setName(apmAccessOperationAo.getActionName());
        apmAccess.setResource(apmAccessOperationAo.getResource());
        apmAccess.setDescription(apmAccessOperationAo.getDescription());
        apmAccess.setVisibleConfig(apmAccessOperationAo.getVisibleConfig());
        apmAccess.setOperationConfig(apmAccessOperationAo.getOperationConfig());
        apmAccess.setViewConfig(apmAccessOperationAo.getViewConfig());
        apmAccess.setMethodConfig(apmAccessOperationAo.getMethodConfig());
        apmAccess.setType(apmAccessOperationAo.getType());
        apmAccess.setSort(apmAccessOperationAo.getSort());
        apmAccess.setIcon(apmAccessOperationAo.getIcon());
        apmAccess.setDeleteFlag(apmAccessOperationAo.getDeleteFlag());
        apmAccess.setEnableFlag(apmAccessOperationAo.getEnableFlag());
        apmAccess.setShowLocation(apmAccessOperationAo.getShowLocation());
        apmAccessService.updateByBid(apmAccess);
        // 更新ApmRoleAccess
        List<String> roleBids = apmAccessOperationAo.getRoleBids();
        if (CollUtil.isEmpty(roleBids)) {
            return Boolean.TRUE;
        }

        // 通过sphereBid查询当前应用的配置的权限角色bid
        List<String> roleBidsInDb = apmRoleAccessService.list(Wrappers.lambdaQuery(ApmRoleAccess.class).eq(ApmRoleAccess::getAccessBid, apmAccessOperationAo.getBid()))
                .stream()
                .map(ApmRoleAccess::getRoleBid)
                .collect(Collectors.toList());
        List<String> roleBidsCopy = Lists.newArrayList(roleBids);
        // 获取需要新增的角色bid
        roleBids.removeAll(roleBidsInDb);
        // 获取需要删除的角色bid
        roleBidsInDb.removeAll(roleBidsCopy);
        if (CollUtil.isNotEmpty(roleBids)) {
            // 封装ApmRoleAccess实体用于持久化
            List<ApmRoleAccess> apmRoleAccessList = roleBids.stream().map(roleBid -> {
                ApmRoleAccess apmRoleAccess = new ApmRoleAccess();
                apmRoleAccess.setRoleBid(roleBid);
                apmRoleAccess.setAccessBid(apmAccess.getBid());
                apmRoleAccess.setSphereBid(sphere.getBid());
                apmRoleAccess.setSpaceAppBid(spaceAppBid);
                apmRoleAccess.setCreatedBy(SsoHelper.getJobNumber());
                apmRoleAccess.setCreatedTime(new Date());
                apmRoleAccess.setUpdatedBy(apmRoleAccess.getCreatedBy());
                apmRoleAccess.setUpdatedTime(apmRoleAccess.getCreatedTime());
                apmRoleAccess.setTenantId(100);
                apmRoleAccess.setEnableFlag(1);
                apmRoleAccess.setDeleteFlag(0);
                return apmRoleAccess;
            }).collect(Collectors.toList());
            apmRoleAccessService.saveBatch(apmRoleAccessList);
        }
        if (CollUtil.isNotEmpty(roleBidsInDb)) {
            apmRoleAccessService.remove(new QueryWrapper<ApmRoleAccess>().lambda().in(ApmRoleAccess::getRoleBid, roleBidsInDb).eq(ApmRoleAccess::getAccessBid, apmAccess.getBid()));
        }
        return Boolean.TRUE;
    }

    /**
     * @param spaceAppBid
     * @return {@link List }<{@link ApmActionConfigUserVo }>
     */
    @Override
    public List<ApmActionConfigUserVo> currentUserAccess(String spaceAppBid) {
        // 通过空间bid查询域信息
        ApmSphere sphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (Objects.isNull(sphere) || StrUtil.isBlank(sphere.getBid())) {
            return Lists.newArrayList();
        }
        List<ApmActionConfigUserVo> resultList = apmAccessDomainService.currentUserAccess(sphere);
        resultList.forEach(apmAccess -> {
            apmAccess.setActionType("expand");
        });
        return resultList;
    }

    /**
     * @param apmAccessOperationAo
     * @param spaceAppBid
     * @return {@link Boolean }
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAccess(ApmAccessOperationAo apmAccessOperationAo, String spaceAppBid) {
        // 非空检验
        if (Objects.isNull(apmAccessOperationAo) || StrUtil.isBlank(apmAccessOperationAo.getBid())) {
            return Boolean.FALSE;
        }
        // 删除ApmAccess
        apmAccessService.remove(new QueryWrapper<ApmAccess>().lambda().eq(ApmAccess::getBid, apmAccessOperationAo.getBid()));
        apmRoleAccessService.remove(new QueryWrapper<ApmRoleAccess>().lambda().eq(ApmRoleAccess::getAccessBid, apmAccessOperationAo.getBid()));
        return Boolean.TRUE;
    }

    /**
     * 自定义视图列表
     *
     * @param spaceAppBid
     * @return
     */
    @Override
    public List<ApmSpaceAppCustomViewVo> customViewPermissionList(String spaceAppBid) {
//        List<ApmSpaceAppCustomViewPo> apmSpaceAppCustomViewPoList = apmSpaceAppCustomViewService
//                .list(new QueryWrapper<ApmSpaceAppCustomViewPo>().lambda()
        // 根据本空间应用的角色以及应用权限给与部分权限的list TODO
        // 1.如果permissionType = 所有空间团队成员，都能查看
        // 2.如果permissionType = 指定角色，只有指定角色的人+创建人能看能查看
        // 根绝sphereBid和jobNum查询出登录人在当前域下面对应的角色
        // 通过空间bid和域类型查询域信息
        ApmSphere sphere = apmSphereService.getOne(
                Wrappers.<ApmSphere>lambdaQuery()
                        .eq(ApmSphere::getBizBid, spaceAppBid)
                        .eq(ApmSphere::getType, TypeEnum.OBJECT.getCode())
                        .eq(ApmSphere::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmSphere::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
        );
        // 收集有权限的bid集合
        final List<String> permissionCustomViewBids = Lists.newArrayList();
        // 不为空才需要处理
        if (!Objects.isNull(sphere) && !Objects.isNull(sphere.getBid())) {
            List<ApmRole> roleList = apmRoleService
                    .getRoleListByJobNumAndSphereBid(SsoHelper.getJobNumber(), sphere.getBid());
            List<String> roleBids = Optional.ofNullable(roleList)
                    .orElse(Lists.newArrayList()).stream().map(ApmRole::getBid)
                    .collect(Collectors.toList());
            // 收集有权限的bid集合
            permissionCustomViewBids.addAll(apmSpaceAppCustomViewRoleService.listCustomViewBidByRoleBids(roleBids));
        }
        // 权限类型（1-指定团队人员，2-所有空间团队成员(需要匹配有权限的自定义条件视图)）
        List<ApmSpaceAppCustomViewPo> list = apmSpaceAppCustomViewService.list(Wrappers.<ApmSpaceAppCustomViewPo>lambdaQuery()
                .eq(ApmSpaceAppCustomViewPo::getSpaceAppBid, spaceAppBid).and(
                        wrapper -> wrapper.eq(ApmSpaceAppCustomViewPo::getPermissionType, 1)
                                .or(
                                        CollectionUtils.isNotEmpty(permissionCustomViewBids)
                                        , orWrapper -> orWrapper.eq(ApmSpaceAppCustomViewPo::getPermissionType, 2)
                                                .in(ApmSpaceAppCustomViewPo::getBid, permissionCustomViewBids)
                                ).or().eq(ApmSpaceAppCustomViewPo::getCreatedBy, SsoHelper.getJobNumber())
                ).orderByDesc(Boolean.TRUE, ApmSpaceAppCustomViewPo::getSort));
        return ApmSpaceAppCustomViewConverter.INSTANCE.pos2vos(list);
    }

    /**
     * @param spaceBid
     * @param spaceAppBid
     * @return {@link Boolean }
     */
    @Override
    public Boolean distributeAccessByTemplateSpace(String spaceBid, String spaceAppBid) {
        ApmSpace apmSpace = apmSpaceService.getOne(Wrappers.<ApmSpace>lambdaQuery().eq(ApmSpace::getBid, spaceBid));
        if (Objects.isNull(apmSpace)) {
            throw new BusinessException("空间不存在");
        }
        //复制出来的空间列表
        List<ApmSpace> spaceList = apmSpaceService.list(Wrappers.<ApmSpace>lambdaQuery()
                .eq(ApmSpace::getTemplateBid, spaceBid)
                .eq(ApmSpace::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED));
        //空间列表为空 直接返回
        if (CollectionUtils.isEmpty(spaceList)) {
            return Boolean.TRUE;
        }
        spaceList.add(apmSpace);
        final ApmSpaceApp appInfo = apmSpaceAppService.getByBid(spaceAppBid);
        List<ApmSpaceApp> appBySpaceList = apmSpaceAppService.list(Wrappers.<ApmSpaceApp>lambdaQuery()
                .in(ApmSpaceApp::getSpaceBid, spaceList.stream().map(ApmSpace::getBid).collect(Collectors.toList()))
                .eq(ApmSpaceApp::getModelCode, appInfo.getModelCode())
                .eq(ApmSpaceApp::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
        );
        //空间应用列表为空 直接返回
        if (CollectionUtils.isEmpty(appBySpaceList)) {
            return Boolean.TRUE;
        }
        Map<String, String> appSphereBidAndAppSpaceBidMap = appBySpaceList.stream().collect(Collectors.toMap(ApmSpaceApp::getSphereBid, ApmSpaceApp::getSpaceBid));
        Set<String> spaceAppBids = appBySpaceList.stream().map(ApmSpaceApp::getBid).collect(Collectors.toSet());
        //查询空间(包含模板空间以及复制的空间)已经配置的操作信息
        List<ApmRoleAccess> appAccessList = apmRoleAccessService.listBySpaceAppBids(spaceAppBids);
        //当前需要下发的操作信息为空直接返回
        if (appAccessList.stream().noneMatch(roleAccess -> spaceAppBid.equals(roleAccess.getSpaceAppBid()))) {
            return Boolean.TRUE;
        }
        Map<String, String> accessBidAndSphereBidMap = appAccessList.stream().collect(Collectors.toMap(ApmRoleAccess::getAccessBid, ApmRoleAccess::getSphereBid, (k1, k2) -> k1));
        //通过操作bid列表查询操作信息
        List<ApmAccess> apmAccesses = apmAccessService.listByBidConllection(appAccessList.stream().map(ApmRoleAccess::getAccessBid).collect(Collectors.toList()));
        Map<String, List<ApmAccess>> accessMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        apmAccesses.forEach(apmAccess -> {
            String sphereBid = accessBidAndSphereBidMap.get(apmAccess.getBid());
            List<ApmAccess> list = accessMap.getOrDefault(sphereBid, new ArrayList<>());
            list.add(apmAccess);
            accessMap.put(sphereBid, list);
        });
        //查询空间角色信息
        final List<ApmRole> apmSphereRoles = apmRoleService.list(Wrappers.<ApmRole>lambdaQuery()
                .in(ApmRole::getSphereBid, spaceList.stream().map(ApmSpace::getSphereBid).collect(Collectors.toSet()))
                .eq(ApmRole::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED));
        Map<String, List<ApmRole>> roleMap = apmSphereRoles.stream().collect(Collectors.groupingBy(ApmRole::getSphereBid));
        Map<String, String> spaceOfSpaceBidAndSphereBidMap = spaceList.stream().collect(Collectors.toMap(ApmSpace::getBid, ApmSpace::getSphereBid));
        //把空间角色转换为应用域map 用于后面得数据循环
        Map<String, List<ApmRole>> convertToAppRoleMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        appSphereBidAndAppSpaceBidMap.forEach((appSphereBid, appSpaceBid) -> {
            convertToAppRoleMap.put(appSphereBid, roleMap.get(spaceOfSpaceBidAndSphereBidMap.get(appSpaceBid)));
        });
        //排除当前空间应用
        appBySpaceList = appBySpaceList.stream().filter(app -> !app.getBid().equals(spaceAppBid)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(spaceList)) {
            return Boolean.TRUE;
        }
        appAccessList = appAccessList.stream().filter(roleAccess -> spaceAppBid.equals(roleAccess.getSpaceAppBid())).collect(Collectors.toList());
        return batchCopyAccess(appAccessList, appBySpaceList, convertToAppRoleMap, accessMap, appInfo.getSphereBid());
    }

    private boolean batchCopyAccess(List<ApmRoleAccess> appAccessList, List<ApmSpaceApp> apmAppList, Map<String, List<ApmRole>> roleMap, Map<String, List<ApmAccess>> sphereAccessMap, String nowSphereBid) {

        if (CollectionUtils.isNotEmpty(appAccessList) && CollectionUtils.isNotEmpty(apmAppList) && CollectionUtils.isNotEmpty(roleMap) && CollectionUtils.isNotEmpty(sphereAccessMap)) {
            List<ApmRoleAccess> saveRoleAccessList = Lists.newArrayList();
            List<ApmAccess> saveAccessList = Lists.newArrayList();
            List<ApmAccess> apmAccesses = sphereAccessMap.get(nowSphereBid);
            Map<String, String> accessMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            Map<String, List<String>> viewBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            List<ApmRole> apmRoles = roleMap.get(nowSphereBid);
            Map<String, String> roleBidAndCodeMap = apmRoles.stream().collect(Collectors.toMap(ApmRole::getBid, ApmRole::getCode));
            apmAppList.stream().forEach(app -> {
                Map<String, ApmRole> sphereRoleMap = roleMap.get(app.getSphereBid()).stream().collect(Collectors.toMap(ApmRole::getCode, Function.identity()));
                List<String> exitAccessName = sphereAccessMap.getOrDefault(app.getSphereBid(), new ArrayList<>()).stream().map(ApmAccess::getName).collect(Collectors.toList());
                List<String> exitAccess = Lists.newArrayList();
                for (ApmAccess apmAccess : apmAccesses) {
                    ApmAccess access = BeanUtil.copyProperties(apmAccess, ApmAccess.class);
                    //已存在的操作(名称相等)不处理直接跳过
                    if (exitAccessName.contains(apmAccess.getName())) {
                        exitAccess.add(apmAccess.getBid());
                        continue;
                    }
                    String bid = SnowflakeIdWorker.nextIdStr();
                    accessMap.put(apmAccess.getBid(), bid);
                    if ("form".equals(apmAccess.getType())) {
                        List<String> newViewBids = viewBidMap.getOrDefault(apmAccess.getBid() + "#" + apmAccess.getName(), new ArrayList<>());
                        newViewBids.add(bid + "#" + apmAccess.getName());
                        viewBidMap.put(apmAccess.getBid() + "#" + apmAccess.getName(), newViewBids);
                    }
                    access.setId(null);
                    access.setBid(bid);
                    saveAccessList.add(access);
                }
                for (int i = appAccessList.size() - 1; i >= 0; i--) {
                    ApmRoleAccess apmRoleAccess = appAccessList.get(i);
                    ApmRoleAccess roleAccess = BeanUtil.copyProperties(apmRoleAccess, ApmRoleAccess.class);
                    //已经存在的操作的角色数据不处理直接跳过
                    if (exitAccess.contains(apmRoleAccess.getAccessBid())) {
                        continue;
                    }
                    ApmRole nowRole = sphereRoleMap.get(roleBidAndCodeMap.get(apmRoleAccess.getRoleBid()));
                    if (nowRole == null) {
                        continue;
                    }
                    roleAccess.setId(null);
                    roleAccess.setAccessBid(accessMap.get(apmRoleAccess.getAccessBid()));
                    roleAccess.setRoleBid(nowRole.getBid());
                    roleAccess.setSpaceAppBid(app.getBid());
                    roleAccess.setSphereBid(app.getSphereBid());
                    saveRoleAccessList.add(roleAccess);
                }
            });
            if (CollectionUtils.isNotEmpty(saveAccessList)) {
                apmAccessService.saveBatch(saveAccessList);
                CfgViewDto cfgViewDto = new CfgViewDto();
                cfgViewDto.setType("copyViewModel");
                cfgViewDto.setViewBatchCopyMap(viewBidMap);
                viewFeignClient.copyViews(cfgViewDto);
            }
            apmRoleAccessService.saveBatch(saveRoleAccessList);
        }
        return true;
    }

    /**
     * @param customViewBid
     * @return {@link ApmSpaceAppCustomViewVo }
     */
    @Override
    public ApmSpaceAppCustomViewVo customViewGet(String customViewBid) {
        ApmSpaceAppCustomViewVo apmSpaceAppCustomViewVo = ApmSpaceAppCustomViewConverter.INSTANCE.po2vo(apmSpaceAppCustomViewService.getByBid(customViewBid));
        // 要获取角色的名称
        String spaceAppBid = apmSpaceAppCustomViewVo.getSpaceAppBid();
        List<String> exeRoleBids = apmSpaceAppCustomViewRoleService.listRoleBidByCustomViewBid(customViewBid);
        // 通过应用bid找域bid
        // 通过空间bid和域类型查询域信息
        ApmSphere sphere = apmSphereService.getOne(
                Wrappers.<ApmSphere>lambdaQuery()
                        .eq(ApmSphere::getBizBid, spaceAppBid)
                        .eq(ApmSphere::getType, TypeEnum.OBJECT.getCode())
                        .eq(ApmSphere::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmSphere::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
        );

        if (!Objects.isNull(sphere) && !Objects.isNull(sphere.getBid()) && CollectionUtils.isNotEmpty(exeRoleBids)) {
            List<ApmRoleVO> roles =
                    apmRoleService.listByRoleBidsAndSpaceAppBid(exeRoleBids, sphere.getBid());
            apmSpaceAppCustomViewVo.setRoles(roles);
        }

        return apmSpaceAppCustomViewVo;
    }

    /**
     * @param spaceAppBid
     * @param apmSpaceAppCustomViewDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean customViewSaveOrUpdate(String spaceAppBid, ApmSpaceAppCustomViewDto apmSpaceAppCustomViewDto) {
        // 设置空间应用bid
        apmSpaceAppCustomViewDto.setSpaceAppBid(spaceAppBid);
        String bid = apmSpaceAppCustomViewDto.getBid();
        // 处理角色的存储
        List<String> roleBids = apmSpaceAppCustomViewDto.getRoleBids();
        List<String> deleteRoleBids = Lists.newArrayList();
        boolean isEdit = StringUtil.isNotBlank(bid);
        if (!isEdit) {
            bid = SnowflakeIdWorker.nextIdStr();
            apmSpaceAppCustomViewDto.setBid(bid);
        }
        // 修改场景
        if (isEdit) {
            // 查询绑定的角色
            List<String> exeRoleBids = apmSpaceAppCustomViewRoleService.listRoleBidByCustomViewBid(bid);
            // 收集需要删除的角色
            List<String> roleBidsCopy = Lists.newArrayList(roleBids);
            // 获取需要新增的角色bid
            roleBids.removeAll(exeRoleBids);
            // 获取需要删除的角色bid
            exeRoleBids.removeAll(roleBidsCopy);
            deleteRoleBids = exeRoleBids;
        }
        // 删除的角色绑定
        if (CollectionUtils.isNotEmpty(deleteRoleBids)) {
            apmSpaceAppCustomViewRoleService.remove(bid, deleteRoleBids);
        }
        // 新增的角色绑定
        if (CollectionUtils.isNotEmpty(roleBids)) {
            apmSpaceAppCustomViewRoleService.batchAdd(spaceAppBid, bid, roleBids);
        }
        // 删掉原来的角色，重新保存
        return isEdit ? apmSpaceAppCustomViewService.updateByBid(ApmSpaceAppCustomViewConverter.INSTANCE.dto2po(apmSpaceAppCustomViewDto)) :
                apmSpaceAppCustomViewService.save(ApmSpaceAppCustomViewConverter.INSTANCE.dto2po(apmSpaceAppCustomViewDto));
    }

    /**
     * @param spaceAppBid
     * @param instanceBid
     * @return {@link List }<{@link ApmActionConfigUserVo }>
     */
    @Override
    public List<ApmActionConfigUserVo> currentUserInstanceAccess(String spaceAppBid, String instanceBid) {
        // 通过SpaceAppBid查询应用信息
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        String modelCode = app.getModelCode();
        // 通过实例instanceBid查询实例信息
        MObject instance = objectModelCrudI.getByBid(modelCode, instanceBid);
        // 通过查询当前的应用操作权限
        List<ApmActionConfigUserVo> apmActionConfigUserVos = currentUserAccess(spaceAppBid);
        return apmActionConfigUserVos.stream().filter(obj -> {
            AtomicBoolean flag = new AtomicBoolean(false);
            List<FieldConditionParam> visibleConfigList = obj.getVisibleConfig();
            if (CollUtil.isEmpty(visibleConfigList)) {
                return flag.get();
            }
            visibleConfigList.forEach(visibleConfigDto -> {
                String fieldName = visibleConfigDto.getFieldName();
                String actual = (String) instance.get(fieldName);
                String condition = visibleConfigDto.getCondition();
                flag.set(CheckUtils.checkConditionBoolean(actual, condition, visibleConfigDto.getFieldVal()));
                /*if (StrUtil.isNotBlank(actual) && StrUtil.equals(actual, visibleConfigDto.getFieldVal())) {
                    flag.set(true);
                }*/
            });
            return flag.get();
        }).collect(Collectors.toList());
    }

    /**
     * @param spaceBid
     * @param spaceAppBid
     * @param tabBid
     * @return {@link List }<{@link ApmLaneTabVO }>
     */
    @Override
    public List<ApmLaneTabVO> laneTabList(String spaceBid, String spaceAppBid, String tabBid) {
        List<ApmLaneTabVO> apmLaneTabVOS = buildLaneTabListResult(getMultiAppConfig(spaceBid, spaceAppBid, tabBid));
        if (CollUtil.isNotEmpty(apmLaneTabVOS)) {
            ApmLaneTabVO apmLaneTabVO = new ApmLaneTabVO();
            apmLaneTabVO.setRelationTabName("全部");
            apmLaneTabVO.setTargetModelCode("allLane");
            apmLaneTabVOS.add(apmLaneTabVO);
        }
        return apmLaneTabVOS;
    }

    /**
     * @param spaceBid
     * @param spaceAppBid
     * @param tabBid
     * @return {@link MultiAppConfig }
     */
    @Override
    public MultiAppConfig getMultiAppConfig(String spaceBid, String spaceAppBid, String tabBid) {
        MultiAppConfig multiAppConfig = new MultiAppConfig();
        if (StringUtils.isNotBlank(tabBid)) {
            multiAppConfig = getApmLaneTabVOSFromRelation(spaceBid, tabBid);
        } else {
            multiAppConfig = getApmLaneTabVOSFromView(spaceBid, spaceAppBid);
        }
        return multiAppConfig;
    }

    /**
     * @param spaceAppBid
     * @return {@link ObjectVo }
     */
    @Override
    public ObjectVo objectBaseInfo(String spaceAppBid) {
        // 通过SpaceAppBid查询应用信息
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        String modelCode = app.getModelCode();
        return cfgObjectService.getByModelCode(modelCode);
    }

    /**
     * @param apmAppExportTemplateDto
     * @return {@link ApmAppExportTemplateVo }
     */
    @Override
    public ApmAppExportTemplateVo exportTemplateSaveOrUpdate(ApmAppExportTemplateDto apmAppExportTemplateDto) {
        Assert.notNull(apmAppExportTemplateDto, "attribute is null");
        apmAppExportTemplateDto.setUpdatedTime(new Date());
        return StringUtil.isBlank(apmAppExportTemplateDto.getBid()) ? apmAppExportTemplateService.save(apmAppExportTemplateDto) : apmAppExportTemplateService.update(apmAppExportTemplateDto);
    }

    /**
     * @param bid
     * @return {@link Boolean }
     */
    @Override
    public Boolean exportTemplateDelete(String bid) {
        return apmAppExportTemplateService.logicalDeleteByBid(bid);
    }

    /**
     * @param apmAppExportTemplateDto
     * @return {@link List }<{@link ApmAppExportTemplateVo }>
     */
    @Override
    public List<ApmAppExportTemplateVo> queryExportTemplate(ApmAppExportTemplateDto apmAppExportTemplateDto) {
        List<ApmAppExportTemplateVo> vos = apmAppExportTemplateService.queryByCondition(apmAppExportTemplateDto);
        // 如果查询结果为空，增加一个默认模板并返回
        if (CollectionUtils.isEmpty(vos)) {
            // 查询空间应用基础视图
            CfgViewVo cfgViewVo = iAmSpaceAppConfigManageService.baseViewGet(apmAppExportTemplateDto.getSpaceAppBid());
            if (ObjectUtils.isNotEmpty(cfgViewVo) && CollectionUtils.isNotEmpty(cfgViewVo.getMetaList())) {
                ApmAppExportTemplateDto dto = new ApmAppExportTemplateDto();
                dto.setTemplateName("默认模板");
                dto.setSpaceBid(apmAppExportTemplateDto.getSpaceBid());
                dto.setSpaceAppBid(apmAppExportTemplateDto.getSpaceAppBid());
                dto.setFields(cfgViewVo.getMetaList().stream().map(CfgViewMetaDto::getName).collect(Collectors.toList()));
                ApmAppExportTemplateVo apmAppExportTemplateVo = this.exportTemplateSaveOrUpdate(dto);
                return Lists.newArrayList(apmAppExportTemplateVo);
            }
        }
        return vos;
    }

    /**
     * @param apmAppExportDto
     * @param response
     * @throws IOException
     */
    @Override
    public void export(ApmAppExportDto apmAppExportDto, HttpServletResponse response) throws IOException {
        // 设置excel参数
        String spaceAppName = getFileName(apmAppExportDto.getSpaceAppBid());
        String fileName = URLEncoder.encode(spaceAppName, StandardCharsets.UTF_8.name()).replace("\\+", "%20");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=utf-8''" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        log.info("导出数据开始");
        AtomicReference<List<Map<String, Object>>> header = new AtomicReference<>(Lists.newArrayList());
        AtomicReference<List<List<Object>>> dataList = new AtomicReference<>(Lists.newArrayList());
        // 获取导出数据
        String interFaceName = apmAppExportDto.getInterFaceName();
        List<Map<String, Object>> mapList = Lists.newArrayList();
        String spaceBid = apmAppExportDto.getSpaceBid();
        String spaceAppBid = apmAppExportDto.getSpaceAppBid();
        if ("page".equals(interFaceName)) {
            // 获取导出表头
            header.set(getHeaderInfo(apmAppExportDto, "tableView"));
            PagedResult<MSpaceAppData> page = iBaseApmSpaceAppDataDrivenService.page(spaceAppBid, QueryConveterTool.convert(apmAppExportDto.getPageRequestParam()), false);
            if (page != null && ObjectUtils.isNotEmpty(page.getData())) {
                page.getData().forEach(data -> {
                    Map<String, Object> map = Maps.newHashMap();
                    map.putAll(data);
                    mapList.add(map);
                });
            }
        }
        if ("relationPage".equals(interFaceName)) {
            // 获取导出表头
            header.set(getHeaderInfo(apmAppExportDto, "tableView"));
            PagedResult<MObject> page = baseApmSpaceAppRelationDataDrivenService.listRelationPage(spaceBid, spaceAppBid, apmAppExportDto.getRelationPageRequestParam(), false);
            if (page != null && ObjectUtils.isNotEmpty(page.getData())) {
                page.getData().forEach(data -> {
                    Map<String, Object> map = Maps.newHashMap();
                    map.putAll(data);
                    mapList.add(map);
                });
            }
        }
        if ("tree".equals(interFaceName)) {
            header.set(getHeaderInfo(apmAppExportDto, "treeView"));
            List<MSpaceAppData> mSpaceAppData = ParentBidHandler.handleMSpaceAppData(iBaseApmSpaceAppDataDrivenService.signObjectAndMultiSpaceTree(spaceBid, spaceAppBid, apmAppExportDto.getTreeRequestParam(), false));
            if (CollectionUtils.isNotEmpty(mSpaceAppData)) {
                mSpaceAppData.forEach(data -> {
                    Map<String, Object> map = Maps.newHashMap();
                    map.putAll(data);
                    mapList.add(map);
                });
            }
        }
        if ("relationTree".equals(interFaceName)) {
            header.set(getHeaderInfo(apmAppExportDto, "treeView"));
            List<MObjectTree> mObjectTrees = baseApmSpaceAppRelationDataDrivenService.tree(spaceBid, spaceAppBid, apmAppExportDto.getRelationTreeRequestParam(), false);
            if (CollectionUtils.isNotEmpty(mObjectTrees)) {
                mObjectTrees.forEach(data -> {
                    Map<String, Object> map = Maps.newHashMap();
                    map.putAll(data);
                    mapList.add(map);
                });
            }
        }
        if ("multiTree".equals(interFaceName)) {
            header.set(getHeaderInfo(apmAppExportDto, "multiTreeView"));
            List<MObjectTree> multiTree = getMultiTreeData(spaceBid, spaceAppBid, apmAppExportDto.getTreeRequestParam());
            if (CollectionUtils.isNotEmpty(multiTree)) {
                // 平铺数据
                List<MObjectTree> result = Lists.newArrayList();
                tilingData(multiTree, result);
                result.forEach(data -> {
                    Map<String, Object> map = Maps.newHashMap();
                    map.putAll(data);
                    mapList.add(map);
                });
            }
        }
        if ("relationMultiTree".equals(interFaceName)) {
            header.set(getHeaderInfo(apmAppExportDto, "multiTreeView"));
            List<MObjectTree> multiTree = Lists.newArrayList();
            ApmMultiTreeDto apmMultiTreeDto = apmAppExportDto.getRelationMultiTreeRequestParam();
            if (StringUtils.isEmpty(apmMultiTreeDto.getSpaceBid())) {
                apmMultiTreeDto.setSourceBid(spaceBid);
            }
            if (StringUtils.isEmpty(apmMultiTreeDto.getSpaceAppBid())) {
                apmMultiTreeDto.setSpaceAppBid(spaceAppBid);
            }
            multiTree = ParentBidHandler.handleMObjectTree(baseApmSpaceAppRelationDataDrivenService.listMultiTree(apmMultiTreeDto, false));
            if (StringUtils.isNoneBlank(apmMultiTreeDto.getGroupProperty()) && StringUtils.isNoneBlank(apmMultiTreeDto.getGroupPropertyValue())) {
                apmMultiTreeDto.setTree(multiTree);
                multiTree = baseApmSpaceAppRelationDataDrivenService.listMultiTreeGroupBy(apmMultiTreeDto);
            }

            if (CollectionUtils.isNotEmpty(multiTree)) {
                // 平铺数据
                List<MObjectTree> result = Lists.newArrayList();
                tilingData(multiTree, result);
                result.forEach(data -> {
                    Map<String, Object> map = Maps.newHashMap();
                    map.putAll(data);
                    mapList.add(map);
                });
            }
        }
        dataList.set(exportDataCommonHandler(spaceBid, spaceAppBid, mapList, header.get()));
        List<List<String>> exportHeader = Lists.newArrayList();
        header.get().forEach(e -> {
            for (Map<String, Object> map : (List<Map<String, Object>>) e.get("fields")) {
                List<String> head = new ArrayList<>();
                head.add(e.get("name").toString());
                head.add(map.get("label").toString());
                exportHeader.add(head);
            }
        });
        log.info("导出表头：{}", exportHeader);
        EasyExcelFactory.write(response.getOutputStream()).excelType(ExcelTypeEnum.XLSX).autoCloseStream(Boolean.TRUE)
                //自适应列宽
                .registerWriteHandler(new CustomCellWriteWidthConfig())
                //自适应行高
                .registerWriteHandler(new CustomCellWriteHeightConfig())
                .registerWriteHandler(EasyExcelUtils.getStyleStrategy(header.get()))
                .sheet("对象数据导出").head(exportHeader).doWrite(dataList.get());
        log.info("导出数据成功");
    }

    private String getFileName(String spaceAppBid) {
        String fileName = "对象数据导出";
        if (StringUtils.isNotBlank(spaceAppBid)) {
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
            if (Objects.nonNull(apmSpaceApp)) {
                String spaceAppName = apmSpaceApp.getName();
                if ("RR".equals(spaceAppName)) {
                    spaceAppName = "原始需求RR";
                }
                fileName = spaceAppName + "导出";
            }
        }
        return fileName;
    }

    /**
     * @param spaceAppBid
     * @param instanceBid
     * @param apmAccessQo
     * @return {@link List }<{@link ApmActionConfigUserVo }>
     */
    @Override
    public List<ApmActionConfigUserVo> currentUserPermissionAccess(String spaceAppBid, String instanceBid, ApmAccessQo apmAccessQo) {
        MBaseData mSpaceAppData = apmAccessQo.getInstance();
        if (ObjectUtils.isEmpty(mSpaceAppData) && StringUtils.isNotEmpty(spaceAppBid)) {
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
            if (ObjectUtils.isNotEmpty(apmSpaceApp)) {
                mSpaceAppData = objectModelCrudI.getByBid(apmSpaceApp.getModelCode(), instanceBid);
                if (ObjectUtils.isEmpty(mSpaceAppData)){
                    return Lists.newArrayList();
                }
            }
        }
        //特殊逻辑，如果是从挂载空间查看，则只返回详情权限
        if (StringUtils.isNotBlank(apmAccessQo.getSpaceBid())
                && ObjectUtils.isNotEmpty(mSpaceAppData.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()))
                && apmAccessQo.getSpaceBid().equals(mSpaceAppData.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()))
                && !apmAccessQo.getSpaceBid().equals(mSpaceAppData.get(SpaceAppDataEnum.SPACE_BID.getCode()))) {
            return Lists.newArrayList(new ApmActionConfigUserVo().setAction(OperatorEnum.DETAIL.getCode()).setActionType("inner").setMore(false));

        }
        // 查询当前用户的角色的操作管理下的按钮
        List<ApmActionConfigUserVo> apmAccessList = currentUserInstanceAccess(spaceAppBid, instanceBid);
        Map<Integer, List<String>> accessOrderList = apmAccessList.stream().filter(acess -> acess.getSort() != null && acess.getSort() > 0)
                .collect(Collectors.groupingBy(ApmActionConfigUserVo::getSort, Collectors.mapping(ApmActionConfigUserVo::getAction, Collectors.toList())));
        // 按照sort字段值排序后的方法名
        accessOrderList = new TreeMap<>(accessOrderList);
        // 查询当前用户的角色的权限管理下的按钮
        List<ApmActionConfigUserVo> apmPermissionList = permissionConfigService.listAppPermissions(spaceAppBid, instanceBid, mSpaceAppData);
        apmAccessList.addAll(apmPermissionList);
        List<String> orderList = Lists.newArrayList(OPERATOR_ORDER_LIST);
        for (Map.Entry<Integer, List<String>> entry : accessOrderList.entrySet()) {
            Integer key = entry.getKey();
            List<String> value = entry.getValue();
            if (key > orderList.size()) {
                key = orderList.size();
            }
            for (String action : value) {
                int i = 0;
                orderList.add(key + i, action);
                i++;
            }

        }
        //按照自定义排序
        apmAccessList = customSort(orderList, apmAccessList, "action");
        for (int i = 0; i < apmAccessList.size(); i++) {
            ApmActionConfigUserVo vo = apmAccessList.get(i);
            if (i < 4) {
                vo.setMore(false);
            } else {
                vo.setMore(true);
            }
        }
        return apmAccessList;
    }

    @Override
    public List<ApmActionConfigUserVo> currentUserPermissionAccess(String spaceAppBid) {
        // 通过空间bid查询域信息
        ApmSphere sphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (Objects.isNull(sphere) || StrUtil.isBlank(sphere.getBid())) {
            return Lists.newArrayList();
        }
        List<ApmActionConfigUserVo> resultList = apmAccessDomainService.currentUserAccess(sphere);
        resultList.forEach(apmAccess -> {
            apmAccess.setActionType("expand");
        });
        //查询应用内权限配置操作权限
        String jobNum = SsoHelper.getJobNumber();
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        //查询用户当前角色
        List<String> roleCodeList = apmRoleService.getRoleCodeList(apmSpaceApp.getSpaceBid(), jobNum);
        //默认有所有实例角色
        roleCodeList.add(InnerRoleEnum.CREATER.getCode());
        roleCodeList.add(InnerRoleEnum.TECHNICAL_DIRECTOR.getCode());
        roleCodeList.add(InnerRoleEnum.UX_AGENT.getCode());
        roleCodeList.add(InnerRoleEnum.PERSON_RESPONSIBLE.getCode());
        roleCodeList.add(InnerRoleEnum.FOLLOW_MEMBER.getCode());

        //查询属性权限规则
        List<AppConditionPermissionVo> appConditionPermissionVos = permissionConfigService.listAppConditionPermission(spaceAppBid);
        if (CollectionUtils.isEmpty(appConditionPermissionVos)) {
            return resultList;
        }
        List<PermissionOperationItemVo> appPermissionOperationList = new ArrayList<>();
        for (AppConditionPermissionVo appConditionPermissionVo : appConditionPermissionVos) {
            for (PermissionOperationItemVo permissionOperationItemVo : appConditionPermissionVo.getAppPermissionOperationList()) {
                if (roleCodeList.contains(permissionOperationItemVo.getRoleCode())) {
                    appPermissionOperationList.add(permissionOperationItemVo);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(appPermissionOperationList)) {
            // 去重后的按钮CODE集合
            Set<String> operatorCodeList = appPermissionOperationList.stream().flatMap(operation -> operation.getOperatorList().stream()).collect(Collectors.toSet());
            // 查询按钮分组的字典
            Map<String, String> codeGroupMap = defaultAppExcelTemplateService.getDictCodeAndEnNameMap(BUTTON_CODE_GROUP);
            Set<String> extraOperations = new HashSet<>();
            for (String operation : operatorCodeList) {
                if (codeGroupMap.containsKey(operation)) {
                    Optional.ofNullable(codeGroupMap.get(operation)).ifPresent(obj -> {
                        extraOperations.addAll(JSONArray.parseArray(obj, String.class));
                    });
                } else {
                    extraOperations.add(operation);
                }
            }
            extraOperations.forEach(code -> {
                ApmActionConfigUserVo actionConfigUserVo = new ApmActionConfigUserVo();
                actionConfigUserVo.setAction(code);
                actionConfigUserVo.setActionType("inner");
                resultList.add(actionConfigUserVo);
            });
        }
        return resultList;
    }


    /**
     * 按照指定字段fieldName的值根据自定义的顺序排序
     *
     * @param orderList
     * @param sourceList
     * @param fieldName
     * @return
     */
    public <T> List<T> customSort(List<String> orderList, List<T> sourceList, String fieldName) {
        Map<String, Integer> orderMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (int i = 0; i < orderList.size(); i++) {
            orderMap.put(orderList.get(i), i);
        }
        Map<Integer, T> tree = new TreeMap<>();
        try {
            for (int i = 0; i < sourceList.size(); i++) {
                T source = sourceList.get(i);
                Field field = source.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                String fieldValue = (String) field.get(source);
                if (orderMap.containsKey(fieldValue)) {
                    tree.put(orderMap.get(fieldValue), source);
                } else {
                    tree.put(orderList.size() + i, source);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return sourceList;
        }
        return tree.values().stream().collect(Collectors.toList());
    }

    /**
     * @param spaceBid
     * @param spaceAppBid
     * @param modelMixQo
     * @return {@link List }<{@link MObjectTree }>
     */
    private List<MObjectTree> getMultiTreeData(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo) {
        List<MObjectTree> list = iBaseApmSpaceAppDataDrivenService.multiTree(spaceBid, spaceAppBid, ApmMultiTreeModelMixQo.of(modelMixQo), false, true);
        if (StringUtils.isNoneBlank(modelMixQo.getGroupProperty()) && StringUtils.isNoneBlank(modelMixQo.getGroupPropertyValue())) {
            ApmMultiTreeDto apmMultiTreeDto = new ApmMultiTreeDto();
            apmMultiTreeDto.setGroupProperty(modelMixQo.getGroupProperty());
            apmMultiTreeDto.setGroupPropertyValue(modelMixQo.getGroupPropertyValue());
            apmMultiTreeDto.setTree(list);
            return baseApmSpaceAppRelationDataDrivenService.listMultiTreeGroupBy(apmMultiTreeDto);
        }
        return list;
    }

    /**
     * 表头信息
     *
     * @param apmAppExportDto
     * @param headerType
     * @return {@link List }<{@link Map }<{@link String }, {@link Object }>>
     */
    private List<Map<String, Object>> getHeaderInfo(ApmAppExportDto apmAppExportDto, String headerType) {
        // 获取导出模板
        ApmAppExportTemplateVo exportTemplate = apmAppExportTemplateService.getByBid(apmAppExportDto.getTemplateBid());
        if (ObjectUtils.isEmpty(exportTemplate)) {
            throw new TranscendBizException("导出模板不存在");
        }
        // 视图信息
        CfgViewVo cfgViewVo = iAmSpaceAppConfigManageService.baseViewGet(apmAppExportDto.getSpaceAppBid());
        Object propertiesList = cfgViewVo.getContent().get("propertiesList");
        if (ObjectUtils.isEmpty(propertiesList)) {
            throw new TranscendBizException("对象视图配置信息不存在");
        }
        List<Map<String, Object>> viewInfoList = JSON.parseObject(JSON.toJSONString(propertiesList), new TypeReference<List<Map<String, Object>>>() {
        });
        Map<String, Map<String, Object>> fieldNameTypeMap = viewInfoList.stream().collect(Collectors.toMap(map -> map.get(TranscendModelBaseFields.NAME).toString(), Function.identity(), (k1, k2) -> k1));
        // 表头配置
        Map<String, Object> viewConfigContent = apmAppTabHeaderService.getApmAppTabHeaderVO(apmAppExportDto.getSpaceAppBid(), headerType).getViewConfigContent();
        List<Map<String, Object>> tableHeaders = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(viewConfigContent)) {
            List<Map<String, Object>> tableHeaderList = JSON.parseObject(JSON.toJSONString(viewConfigContent.get("tableHeaders")), new TypeReference<List<Map<String, Object>>>() {
            });
            tableHeaders = tableHeaderList.stream().sorted(Comparator.comparingInt(o -> Integer.parseInt(o.get("sort").toString()))).collect(Collectors.toList());
        }
        Map<String, Object> tableHeaderMap = tableHeaders.stream().collect(Collectors.toMap(e -> e.get("code").toString(), e -> e.get("sort"), (k1, k2) -> k1, HashMap::new));
        // 视图是最新的，保存的模板中字段可能不是最新的，以视图为准, 顺序以表头为准
        List<Map<String, Object>> header = getMaps(exportTemplate, fieldNameTypeMap, tableHeaderMap);
        return header;
    }

    public Map<String, Object> getProperties(Map<String, Object> content, String name) {
        Object propertiesList = content.get("propertiesList");
        if (ObjectUtils.isEmpty(propertiesList)) {
            return new HashMap<>();
        }
        List<Map<String, Object>> viewInfoList = JSON.parseObject(JSON.toJSONString(propertiesList), new TypeReference<List<Map<String, Object>>>() {
        });
        Map<String, Object> field = viewInfoList.stream().filter(v->name.equals(v.get(TranscendModelBaseFields.NAME))).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(field)) {
            return new HashMap<>();
        }
        getFieldInfo(field);
        return field;
    }

    /**
     * @param exportTemplate
     * @param fieldNameTypeMap
     * @param tableHeaderMap
     * @return {@link List }<{@link Map }<{@link String }, {@link Object }>>
     */
    private List<Map<String, Object>> getMaps(ApmAppExportTemplateVo exportTemplate, Map<String, Map<String, Object>> fieldNameTypeMap, Map<String, Object> tableHeaderMap) {
        List<Map<String, Object>> header = Lists.newArrayList();
        List<String> fields = exportTemplate.getFields();
        List<Object> groupData = exportTemplate.getGroupData();
        if (CollectionUtils.isNotEmpty(groupData)) {
            for (Object groupDatum : groupData) {
                Map<String, Object> group = JSON.parseObject(JSON.toJSONString(groupDatum), Map.class);
                Map<String, Object> groupHeader = new HashMap<>();
                groupHeader.put("name", group.get("groupName"));
                groupHeader.put("color", group.get("color"));
                List<Map<String, Object>> headerFields = Lists.newArrayList();
                List<String> groupFields = (List<String>) group.get("property");
                if (CollectionUtils.isNotEmpty(groupFields)) {
                    for (String e : groupFields) {
                        if (fieldNameTypeMap.containsKey(e)) {
                            Map<String, Object> fieldInfo = fieldNameTypeMap.get(e);
                            // 不支持附件导出
                            if (!"file-upload".equals(fieldInfo.get("type"))) {
                                // 提前塞一些用到的字段，避免每次解析json
                                getFieldInfo(fieldInfo);
                                Object sort = tableHeaderMap.get(e);
                                if (ObjectUtils.isNotEmpty(sort)) {
                                    fieldInfo.put("sort", sort);
                                }
                                headerFields.add(fieldInfo);
                            }
                        }
                    }
                }
                groupHeader.put("fields", headerFields);
                header.add(groupHeader);
            }
        } else {
            Map<String, Object> groupHeader = new HashMap<>();
            List<Map<String, Object>> headerFields = Lists.newArrayList();
            fields.forEach(e -> {
                if (fieldNameTypeMap.containsKey(e)) {
                    Map<String, Object> fieldInfo = fieldNameTypeMap.get(e);
                    // 不支持附件导出
                    if (!"file-upload".equals(fieldInfo.get("type"))) {
                        // 提前塞一些用到的字段，避免每次解析json
                        getFieldInfo(fieldInfo);
                        Object sort = tableHeaderMap.get(e);
                        if (ObjectUtils.isNotEmpty(sort)) {
                            fieldInfo.put("sort", sort);
                        }
                        headerFields.add(fieldInfo);
                    }
                }
            });
            groupHeader.put("name", "默认分组");
            groupHeader.put("color", "#CCCCCC");
            groupHeader.put("fields", headerFields);
            header.add(groupHeader);
        }
        return header;
    }

    public void getFieldInfo(Map<String, Object> fieldInfo) {
        Map<String, Object> field = JSON.parseObject(JSON.toJSONString(fieldInfo.get("field")), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> options = JSON.parseObject(JSON.toJSONString(field.get("options")), new TypeReference<Map<String, Object>>() {
        });
        if (options.get("relationInfo") != null) {
            Map<String, Object> relationInfo = JSON.parseObject(JSON.toJSONString(options.get("relationInfo")), new TypeReference<Map<String, Object>>() {
            });
            fieldInfo.put("targetModelCode", relationInfo.get("targetModelCode"));
            fieldInfo.put("sourceModelCode", relationInfo.get("sourceModelCode"));
        }
        // 实例选择器配置
        if (options.get("instanceSelectConfig") != null) {
            Map<String, Object> instanceInfo = JSON.parseObject(JSON.toJSONString(options.get("instanceSelectConfig")), new TypeReference<Map<String, Object>>() {
            });
            fieldInfo.put("viewMode", instanceInfo.get("viewMode"));
            fieldInfo.put(TranscendModelBaseFields.SPACE_BID, instanceInfo.get(TranscendModelBaseFields.SPACE_BID));
            fieldInfo.put("applicationBid", instanceInfo.get("applicationBid"));
        }
        // 远程接口数据
        if (ObjectUtils.isNotEmpty(options.get(RemoteDataConstant.API_URL))) {
            fieldInfo.put(RemoteDataConstant.API_METHOD, options.get(RemoteDataConstant.API_METHOD));
            fieldInfo.put(RemoteDataConstant.API_URL, options.get(RemoteDataConstant.API_URL));
            fieldInfo.put(RemoteDataConstant.API_LABEL, options.get(RemoteDataConstant.API_LABEL));
            fieldInfo.put(RemoteDataConstant.API_DATA_PATH, options.get(RemoteDataConstant.API_DATA_PATH));
            fieldInfo.put(RemoteDataConstant.API_VALUE, options.get(RemoteDataConstant.API_VALUE));
            fieldInfo.put(RemoteDataConstant.API_PARAMS, options.get(RemoteDataConstant.API_PARAMS));
            fieldInfo.put(RemoteDataConstant.API_CHILDREN, options.get(RemoteDataConstant.API_CHILDREN));
        }
        fieldInfo.put("label", options.get("label"));
        fieldInfo.put("remoteDictType", options.get("remoteDictType"));
    }

    /**
     * @param dataList
     * @param result
     */
    private void tilingData(List<MObjectTree> dataList, List<MObjectTree> result) {
        dataList.forEach(e -> {
            result.add(e);
            List<MObjectTree> children = e.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                tilingData(children, result);
            }
        });
    }

    /**
     * 导出数据处理
     *
     * @param spaceBid
     * @param spaceAppBid
     * @param dataList
     * @param header
     * @return {@link List }<{@link List }<{@link Object }>>
     */
    private List<List<Object>> exportDataCommonHandler(String spaceBid, String spaceAppBid, List<Map<String, Object>> dataList, List<Map<String, Object>> header) {
        if (CollectionUtils.isEmpty(header) || CollectionUtils.isEmpty(dataList)) {
            return Lists.newArrayList();
        }
        // 取出表头所有属性
        List<Map<String, Object>> headFields = new ArrayList<>();
        header.forEach(data -> {
            header.forEach(headerDatum -> {
                headFields.addAll((List<Map<String, Object>>) headerDatum.get("fields"));
            });
        });
        // 表头字段，按数据类型分类
        List<String> dateFields = Lists.newArrayList();
        List<String> selectFields = Lists.newArrayList();
        List<String> lifeCodeFields = Lists.newArrayList();
        List<String> workItemFlowFields = Lists.newArrayList();
        List<String> richFields = Lists.newArrayList();
        List<String> instanceFields = Lists.newArrayList();
        List<String> relationFields = Lists.newArrayList();
        List<String> userFields = Lists.newArrayList();
        List<String> dictCodes = Lists.newArrayList();
        List<String> remoteDataField = Lists.newArrayList();
        List<String> cascaderFields = Lists.newArrayList();
        List<String> spaceSelectFields = Lists.newArrayList();
        // 收集用户
        Map<String, String> userInfoMap = collectUserMap(dataList, headFields);
        // 获取多对象生命周期
        List<String> spaceAppBids = dataList.stream().map(e -> e.get(TranscendModelBaseFields.SPACE_APP_BID).toString()).distinct().collect(Collectors.toList());
        // 去重
        spaceAppBids = spaceAppBids.stream().distinct().collect(Collectors.toList());
        Map<String, String> lifeCycleMap = getMultiObjectLifeCycleMap(spaceAppBids);
        // 收集流程信息
        Map<String, String> workFlowMap = collectWorkItemFlowMap(spaceAppBids);
        // 获取多对象视图
        Map<String, Map<String, Map<String, Object>>> viewMetaMap = getMultiObjectBaseView(spaceAppBids);
        // 合并一下子对象的表头数据, 处理表头属性数据类型
        List<Map<String, Object>> multiObjectHeader = viewMetaMap.values().stream().flatMap(e -> e.values().stream()).collect(Collectors.toList());
        multiObjectHeader.addAll(headFields);
        handleHeaderDataType(multiObjectHeader, dateFields, selectFields, lifeCodeFields, workItemFlowFields, richFields, instanceFields, relationFields, userFields, dictCodes, remoteDataField, cascaderFields, spaceSelectFields);
        // 手机实例数据
        List<Map<String, Object>> instanceFieldInfo = multiObjectHeader.stream().filter(e -> instanceFields.contains(e.get(TranscendModelBaseFields.NAME).toString())).collect(Collectors.toList());
        Map<String, Map<String, String>> instanceMap = collectInstanceMap(spaceBid, instanceFieldInfo);
        //空间实例数据
        Map<String, String> spaceDataMap = collectSpaceDataMap(spaceSelectFields);
        // 收集关系数据
        List<Map<String, Object>> relationFieldInfo = multiObjectHeader.stream().filter(e -> relationFields.contains(e.get(TranscendModelBaseFields.NAME).toString())).collect(Collectors.toList());
        Map<String, Map<String, String>> relationMap = collectRelationMap(spaceBid, relationFieldInfo);
        // 查字典
        Map<String, Map<String, Object>> dictMap = getNameByDictCodes(dictCodes, "zh");
        //特性逻辑，关联特性远程接口有动态参数，需要处理一下
        for (Map<String, Object> headField : headFields) {
            if ("mountSF".equals(headField.get("name"))) {
                String url = headField.get(RemoteDataConstant.API_URL).toString().replace("{currentRow.spaceBid}", spaceBid)
                        .replace("{currentRow.spaceAppBid}", spaceAppBid);
                headField.put(RemoteDataConstant.API_URL, url);
            }
        }
        //查询远程接口数据
        Map<String, Map<String, Object>> remoteDataMap = getRemoteDataList(headFields, remoteDataField);
        //查询级联选择数据
        Map<String, Map<String, Object>> cascaderDataMap = getCascaderDataList(headFields, cascaderFields);
        // 处理层级关系
        List<String> bids = dataList.stream().map(v -> v.get(TranscendModelBaseFields.BID).toString()).collect(Collectors.toList());
        LinkedList<Map<String, Object>> topData = dataList.stream().filter(e -> "0".equals(e.get(TranscendModelBaseFields.PARENT_BID)) || (ObjectUtils.isNotEmpty(e.get(TranscendModelBaseFields.PARENT_BID)) && !bids.contains(e.get(TranscendModelBaseFields.PARENT_BID).toString()))).collect(Collectors.toCollection(LinkedList::new));
        dataList = levelDataHandler(dataList, topData, "0");
        // 处理数据
        List<List<Object>> exportData = Lists.newArrayList();
        // 处理一下，可能存在的脏数据
        dataList = dataList.stream().filter(e -> e.get("exportNum") != null).collect(Collectors.toList());
        dataList.sort((item1, item2) -> {
            char[] v1 = item1.get("exportNum").toString().toCharArray();
            char[] v2 = item2.get("exportNum").toString().toCharArray();
            int p1 = 0, p2 = 0;
            int d1 = 0, d2 = 0;
            while (p1 < v1.length && p2 < v2.length) {
                while (p1 < v1.length && v1[p1] != '.') {
                    p1++;
                }
                while (p2 < v2.length && v2[p2] != '.') {
                    p2++;
                }
                int i1 = Integer.parseInt(new String(v1, d1, p1 - d1));
                int i2 = Integer.parseInt(new String(v2, d2, p2 - d2));
                if (i1 != i2) {
                    return i1 - i2;
                }
                d1 = ++p1;
                d2 = ++p2;
            }
            return v1.length - v2.length;
        });
        // 添加序号列
        addNumColumn(header);
        List<String> formats = Arrays.asList("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
        dataList.forEach(data -> {
            List<Object> rowData = Lists.newArrayList();
            String appBid = data.get(TranscendModelBaseFields.SPACE_APP_BID).toString();
            Map<String, Map<String, Object>> fieldView = viewMetaMap.get(appBid);
            header.forEach(headerGroup -> {
                List<Map<String, Object>> fields = (List<Map<String, Object>>) headerGroup.get("fields");
                for (Map<String, Object> e : fields) {
                    // 值
                    String fieldName = e.get("name").toString();
                    Object value = data.get(fieldName);
                    if ("colNum".equals(fieldName)) {
                        rowData.add(data.get("exportNum"));
                        continue;
                    }
                    if (ObjectUtils.isEmpty(value)) {
                        rowData.add("");
                        continue;
                    }
                    if (richFields.contains(fieldName)) {
                        rowData.add(value.toString().replaceAll("<[^>]+>", ""));
                        continue;
                    }
                    if (dateFields.contains(fieldName)) {
                        addFormattedDate(value, formats, rowData);
                        continue;
                    }
                    if (lifeCodeFields.contains(fieldName) && ObjectUtils.isNotEmpty(lifeCycleMap)) {
                        rowData.add(StringUtils.isBlank(lifeCycleMap.get(value.toString())) ? value : lifeCycleMap.get(value.toString()));
                        continue;
                    }
                    if (userFields.contains(fieldName)) {
                        // 多选判断
                        try {
                            List<String> jobNumList = JSON.parseArray(JSON.toJSONString(value), String.class);
                            rowData.add(jobNumList.stream().map(item -> StringUtils.isBlank(userInfoMap.get(item)) ? item : userInfoMap.get(item)).collect(Collectors.joining(",")));
                        } catch (Exception exp) {
                            rowData.add(StringUtils.isBlank(userInfoMap.get(value.toString())) ? value : userInfoMap.get(value.toString()));
                        }
                        continue;
                    }
                    if (selectFields.contains(fieldName)) {
                        String remoteDictType = e.get("remoteDictType").toString();
                        Map<String, Object> dict = dictMap.get(remoteDictType);
                        if (dict != null) {
                            // 多选判断
                            try {
                                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                                rowData.add(valueList.stream().map(item -> ObjectUtils.isEmpty(dict.get(item)) ? item : dict.get(item).toString()).collect(Collectors.joining(",")));
                            } catch (Exception exp) {
                                rowData.add(ObjectUtils.isNotEmpty(dict) ? dict.get(value.toString()) : value);
                            }
                            continue;
                        }
                    }
                    if (remoteDataField.contains(fieldName)) {
                        Map<String, Object> remoteData = remoteDataMap.get(fieldName);
                        if (remoteData != null) {
                            // 多选判断
                            try {
                                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                                rowData.add(valueList.stream().map(item -> ObjectUtils.isEmpty(remoteData.get(item)) ? item : remoteData.get(item).toString()).collect(Collectors.joining(",")));
                            } catch (Exception exp) {
                                rowData.add(ObjectUtils.isNotEmpty(remoteData) && remoteData.containsKey(value.toString()) ? remoteData.get(value.toString()) : value);
                            }
                            continue;
                        }
                    }
                    if (spaceSelectFields.contains(fieldName)) {
                        // 多选判断
                        try {
                            List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                            rowData.add(valueList.stream().map(item -> ObjectUtils.isEmpty(spaceDataMap.get(item)) ? item : spaceDataMap.get(item).toString()).collect(Collectors.joining(",")));
                        } catch (Exception exp) {
                            rowData.add(ObjectUtils.isNotEmpty(spaceDataMap) && spaceDataMap.containsKey(value.toString()) ? spaceDataMap.get(value.toString()) : value);
                        }
                        continue;
                    }

                    if (cascaderFields.contains(fieldName)) {
                        Map<String, Object> remoteData = cascaderDataMap.get(fieldName);
                        if (remoteData != null) {
                            // 多选判断
                            try {
                                List<Object> valueList = JSON.parseArray(JSON.toJSONString(value), Object.class);
                                rowData.add(valueList.stream().map(item -> {
                                    if (item instanceof List){
                                        return ((List<?>) item).stream().map(k -> remoteData.containsKey(k.toString()) ? remoteData.get(k.toString()).toString() : k.toString()).collect(Collectors.joining(","));
                                    }else {
                                        return ObjectUtils.isEmpty(remoteData.get(item.toString())) ? item.toString() : remoteData.get(item.toString()).toString();
                                    }
                                }).collect(Collectors.joining(",")));
                            } catch (Exception exp) {
                                rowData.add(ObjectUtils.isNotEmpty(remoteData) && remoteData.containsKey(value.toString()) ? remoteData.get(value.toString()) : value);
                            }
                            continue;
                        }
                    }
                    if (relationFields.contains(fieldName) && ObjectUtils.isNotEmpty(fieldView) && ObjectUtils.isNotEmpty(fieldView.get(fieldName))) {
                        Map<String, String> map = relationMap.get(fieldView.get(fieldName).get("sourceModelCode"));
                        if (ObjectUtils.isNotEmpty(map)) {
                            try {
                                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                                rowData.add(valueList.stream().map(item -> StringUtils.isBlank(map.get(item)) ? item : map.get(item)).collect(Collectors.joining(",")));
                            } catch (Exception exp) {
                                rowData.add(StringUtils.isBlank(map.get(value.toString())) ? value : map.get(value.toString()));
                            }
                            continue;
                        }
                    }
                    if (workItemFlowFields.contains(fieldName)) {
                        try {
                            List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                            rowData.add(valueList.stream().map(item -> StringUtils.isBlank(workFlowMap.get(item)) ? item : workFlowMap.get(item)).collect(Collectors.joining(",")));
                        } catch (Exception exp) {
                            rowData.add(StringUtils.isBlank(workFlowMap.get(value.toString())) ? value : workFlowMap.get(value.toString()));
                        }
                        continue;
                    }
                    if (instanceFields.contains(fieldName)) {
                        Map<String, String> map = instanceMap.get(fieldName);
                        if (ObjectUtils.isNotEmpty(map)) {
                            try {
                                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                                rowData.add(valueList.stream().map(item -> StringUtils.isBlank(map.get(item)) ? item : map.get(item)).collect(Collectors.joining(",")));
                            } catch (Exception exp) {
                                rowData.add(StringUtils.isBlank(map.get(value.toString())) ? value : map.get(value.toString()));
                            }
                            continue;
                        }
                    }
                    rowData.add(value.toString());
                }
            });
            exportData.add(rowData);
        });
        return exportData;
    }

    private Map<String, String> collectSpaceDataMap(List<String> spaceSelectFields) {
        Map<String, String> objectObjectHashMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(spaceSelectFields)) {
            com.transcend.plm.datadriven.api.model.QueryWrapper qo = new com.transcend.plm.datadriven.api.model.QueryWrapper(Boolean.FALSE);
            qo.eq("deleteFlag", 0);
            List<MObject> mObjectList = objectModelCrudI.list(MODEL_CODE_SPACE, com.transcend.plm.datadriven.api.model.QueryWrapper.buildSqlQo(qo));
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(mObjectList)) {
                for (MObject mObject : mObjectList) {
                    objectObjectHashMap.put(mObject.getBid(), mObject.getName());
                }
            }
        }
        return objectObjectHashMap;
    }

    private void addNumColumn(List<Map<String, Object>> header) {
        Map<String, Object> colNumField = Maps.newHashMap();
        colNumField.put("name", "colNum");
        colNumField.put("label", "序号");
        colNumField.put("type", "input");

        Map<String, Object> groupHeader = new HashMap<>();
        groupHeader.put("name", "序号");
        groupHeader.put("color", "#CCCCCC");
        groupHeader.put("fields", Lists.newArrayList(colNumField));
        header.add(0, groupHeader);
    }

    private Map<String, Map<String, Object>> getRemoteDataList(List<Map<String, Object>> header, List<String> remoteDataField) {
        ConcurrentHashMap<String, Map<String, Object>> remoteDataMap = new ConcurrentHashMap<>(16);
        if (CollectionUtils.isEmpty(remoteDataField)) {
            return remoteDataMap;
        }
        Map<String, String> headerMap = getHeaderMap();
        List<CompletableFuture<Void>> futures = remoteDataField.stream()
                .filter(field -> header.stream().anyMatch(map -> map.get("name").equals(field)))
                .map(field -> CompletableFuture.runAsync(() -> {
                    try {
                        Map<String, Object> map = header.stream()
                                .filter(v -> v.get("name").equals(field))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Header not found for field: " + field));
                        Map<String, Object> data = getRemoteData(map, headerMap);
                        remoteDataMap.put(field, data);
                    } catch (Exception e) {
                        log.error("查询远程数据失败", e);
                    }
                }, SimpleThreadPool.getInstance()))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return remoteDataMap;
    }

    private Map<String, Map<String, Object>> getCascaderDataList(List<Map<String, Object>> header, List<String> cascaderFields) {
        ConcurrentHashMap<String, Map<String, Object>> remoteDataMap = new ConcurrentHashMap<>(16);
        if (CollectionUtils.isEmpty(cascaderFields)) {
            return remoteDataMap;
        }
        Map<String, String> headerMap = getHeaderMap();
        List<CompletableFuture<Void>> futures = cascaderFields.stream()
                .filter(field -> header.stream().anyMatch(map -> map.get("name").equals(field)))
                .map(field -> CompletableFuture.runAsync(() -> {
                    try {
                        Map<String, Object> map = header.stream()
                                .filter(v -> v.get("name").equals(field))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Header not found for field: " + field));
                        Map<String, Object> data = getRemoteData(map, headerMap);
                        remoteDataMap.put(field, data);
                    } catch (Exception e) {
                        log.error("查询远程数据失败", e);
                    }
                }, SimpleThreadPool.getInstance()))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return remoteDataMap;
    }

    private Map<String, Object> getRemoteData(Map<String, Object> map, Map<String, String> headerMap) {
        String method = map.get(RemoteDataConstant.API_METHOD).toString();
        String url = pushCenterProperties.getGateway() + map.get(RemoteDataConstant.API_URL).toString();
        Map<String, Object> param = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Object params = map.get(RemoteDataConstant.API_PARAMS);
        if (params instanceof JSONArray) {
            params = JSON.parseArray(params.toString());
            for (Object o : (JSONArray) params) {
                JSONObject jsonObject = (JSONObject) o;
                param.put(jsonObject.getString("key"), jsonObject.get("value"));
            }
        } else if (params instanceof String) {
            param = JSON.parseObject(params.toString());
        }
        String label = map.get(RemoteDataConstant.API_LABEL).toString();
        String value = map.get(RemoteDataConstant.API_VALUE).toString();
        String dataPath = map.get(RemoteDataConstant.API_DATA_PATH).toString();
        String childKey = map.getOrDefault(RemoteDataConstant.API_CHILDREN, "").toString();
        String result = null;
        if ("get".equalsIgnoreCase(method)) {
            result = HttpUtil.get(url, headerMap, JSON.toJSONString(param));
        } else if ("post".equalsIgnoreCase(method)) {
            result = HttpUtil.post(url, headerMap, JSON.toJSONString(param));
        }
        Map<String, Object> dataMap = new HashMap<>(16);
        if (StringUtils.isNotBlank(result)) {
            try {
                JSONObject object = JSON.parseObject(result);
                if (!"200".equals(object.getString("code"))) {
                    throw new PlmBizException("500", object.toJSONString());
                }
                JSONArray data = null;
                String[] pathList = dataPath.split("\\.");
                for (int i = 0; i < pathList.length; i++) {
                    if (i != pathList.length - 1) {
                        object = object.getJSONObject(pathList[i]);
                    } else {
                        data = object.getJSONArray(pathList[i]);
                    }
                }
                assert data != null;
                for (Object datum : data) {
                    if (datum instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) datum;
                        if (ObjectUtils.isNotEmpty(jsonObject.get(label)) && ObjectUtils.isNotEmpty(jsonObject.get(value))) {
                            dataMap.put(jsonObject.getString(value), jsonObject.getString(label));
                        }
                        if (StringUtils.isNotBlank(childKey) && jsonObject.containsKey(childKey)) {
                            getChildData(jsonObject, childKey, dataMap, value, label);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("解析远程数据失败", e);
            }
        }
        return dataMap;
    }

    private void getChildData(JSONObject jsonObject, String childKey, Map<String, Object> dataMap, String value, String label) {
        if (ObjectUtils.isNotEmpty(jsonObject) && jsonObject.containsKey(childKey)) {
            JSONArray jsonArray = jsonObject.getJSONArray(childKey);
            if (ObjectUtils.isNotEmpty(jsonArray)) {
                for (Object datum : jsonArray) {
                    if (datum instanceof JSONObject) {
                        JSONObject datum1 = (JSONObject) datum;
                        if (ObjectUtils.isNotEmpty(datum1.get(label)) && ObjectUtils.isNotEmpty(datum1.get(value))) {
                            dataMap.put(datum1.getString(value), datum1.getString(label));
                        }
                        if (ObjectUtils.isNotEmpty(datum1.get(childKey))) {
                            getChildData(datum1, childKey, dataMap, value, label);
                        }
                    }
                }
            }
        }

    }

    /**
     * @param date
     * @param formats
     * @param rowData
     */
    private void addFormattedDate(Object date, List<String> formats, List<Object> rowData) {
        for (String format : formats) {
            try {
                String formattedDate = formatDateTime(date, format);
                if (formattedDate != null) {
                    rowData.add(formattedDate);
                    return;
                }
            } catch (Exception exp) {
                log.info("日期格式化异常");
            }
        }
    }

    /**
     * @param date
     * @param format
     * @return {@link String }
     */
    private String formatDateTime(Object date, String format) {
        try {
            if (date instanceof LocalDateTime) {
                return LocalDateTimeUtil.format((LocalDateTime) date, format);
            } else if (date instanceof LocalDate) {
                return LocalDateTimeUtil.format((LocalDate) date, format);
            } else {
                return LocalDateTimeUtil.parse(date.toString(), format).toString();
            }
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * @param instanceFieldInfo
     * @return {@link Map }<{@link String }, {@link Map }<{@link String }, {@link String }>>
     */
    private Map<String, Map<String, String>> collectInstanceMap(String spaceBid, List<Map<String, Object>> instanceFieldInfo) {
        if (CollectionUtils.isEmpty(instanceFieldInfo)) {
            return new HashMap<>(CommonConstant.START_MAP_SIZE);
        }
        Map<String, Map<String, String>> instanceMap = Maps.newHashMap();
        instanceFieldInfo.forEach(e -> {
            // 判断类型
            String viewMode = ObjectUtils.isEmpty(e.get("viewMode")) ? "" : e.get("viewMode").toString();
            String currentSpaceBid = ObjectUtils.isEmpty(e.get(TranscendModelBaseFields.SPACE_BID)) ? "" : e.get(TranscendModelBaseFields.SPACE_BID).toString();
            String applicationBid = ObjectUtils.isEmpty(e.get("applicationBid")) ? "" : e.get("applicationBid").toString();
            if (ViewFieldDataTypeEnum.LABEL_SELECT.getCode().equals(e.get("type"))){
                Map<String, String> dataMap = getInstanceBidMap(spaceBid, MODEL_CODE_LABEL);
                instanceMap.put(e.get(TranscendModelBaseFields.NAME).toString(), dataMap);
            } else if (StringUtils.isNotBlank(viewMode) && StringUtils.isNotBlank(currentSpaceBid) && StringUtils.isNotBlank(applicationBid)) {
                Map<String, String> dataMap = Maps.newHashMap();
                if ("table".equals(viewMode)) {
                    BaseRequest<ModelMixQo> pageRequestParam = new BaseRequest<>();
                    pageRequestParam.setCurrent(1);
                    pageRequestParam.setSize(10000);
                    ModelMixQo modelMixQo = new ModelMixQo();
                    modelMixQo.setAnyMatch(false);
                    modelMixQo.setQueries(Lists.newArrayList());
                    modelMixQo.setOrders(Lists.newArrayList());
                    pageRequestParam.setParam(modelMixQo);
                    PagedResult<MSpaceAppData> page = iBaseApmSpaceAppDataDrivenService.pageWithoutPermission(applicationBid, QueryConveterTool.convert(pageRequestParam), true);
                    if (ObjectUtils.isNotEmpty(page.getData())) {
                        dataMap = page.getData().stream().filter(item -> ObjectUtils.isNotEmpty(item.getBid())).collect(Collectors.toMap(MSpaceAppData::getBid, item -> ObjectUtils.isEmpty(item.getName()) ? "" : item.getName(), (k1, k2) -> k1, HashMap::new));
                    }
                } else if ("tree".equals(viewMode)) {
                    List<MSpaceAppData> mSpaceAppData = ParentBidHandler.handleMSpaceAppData(iBaseApmSpaceAppDataDrivenService.signObjectAndMultiSpaceTree(currentSpaceBid, applicationBid, ModelMixQo.of(), true));
                    if (CollectionUtils.isNotEmpty(mSpaceAppData)) {
                        dataMap = mSpaceAppData.stream().filter(item -> ObjectUtils.isNotEmpty(item.getBid())).collect(Collectors.toMap(MSpaceAppData::getBid, item -> ObjectUtils.isEmpty(item.getName()) ? "" : item.getName(), (k1, k2) -> k1, HashMap::new));
                    }
                } else if ("multiTree".equals(viewMode)) {
                    List<MObjectTree> multiTree = getMultiTreeData(currentSpaceBid, applicationBid, null);
                    if (CollectionUtils.isNotEmpty(multiTree)) {
                        // 平铺数据
                        List<MObjectTree> result = Lists.newArrayList();
                        tilingData(multiTree, result);
                        dataMap = result.stream().filter(item -> ObjectUtils.isNotEmpty(item.getBid())).collect(Collectors.toMap(MObjectTree::getBid, item -> ObjectUtils.isEmpty(item.getName()) ? "" : item.getName(), (k1, k2) -> k1, HashMap::new));
                    }
                }
                if (CollectionUtils.isNotEmpty(dataMap)) {
                    instanceMap.put(e.get(TranscendModelBaseFields.NAME).toString(), dataMap);
                }
            }
        });
        return instanceMap;
    }

    /**
     * @param spaceAppBids
     * @return {@link Map }<{@link String }, {@link String }>
     */
    private Map<String, String> collectWorkItemFlowMap(List<String> spaceAppBids) {
        Map<String, String> workItemFlowMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (CollectionUtils.isNotEmpty(spaceAppBids)) {
            spaceAppBids.forEach(spaceAppBid -> {
                List<ApmFlowTemplateVO> apmFlowTemplateVOS = apmFlowApplicationService.listBySpaceAppBid(spaceAppBid);
                if (CollectionUtils.isNotEmpty(apmFlowTemplateVOS)) {
                    for (int i = apmFlowTemplateVOS.size() - 1; i >= 0; i--) {
                        if ("state".equals(apmFlowTemplateVOS.get(i).getType())) {
                            apmFlowTemplateVOS.remove(i);
                        }
                    }
                }
                workItemFlowMap.putAll(apmFlowTemplateVOS.stream().collect(Collectors.toMap(ApmFlowTemplateVO::getBid, ApmFlowTemplateVO::getName, (k1, k2) -> k1, HashMap::new)));
            });
        }
        return workItemFlowMap;
    }

    /**
     * @param spaceAppBids
     * @return {@link Map }<{@link String }, {@link Map }<{@link String }, {@link Map }<{@link String }, {@link Object }>>>
     */
    private Map<String, Map<String, Map<String, Object>>> getMultiObjectBaseView(List<String> spaceAppBids) {
        Map<String, Map<String, Map<String, Object>>> cfgViewMetaMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(spaceAppBids)) {
            spaceAppBids.forEach(e -> {
                CfgViewVo cfgViewVo = iAmSpaceAppConfigManageService.baseViewGet(e);
                Object propertiesList = cfgViewVo.getContent().get("propertiesList");
                if (ObjectUtils.isEmpty(propertiesList)) {
                    return;
                }
                List<Map<String, Object>> viewInfoList = JSON.parseObject(JSON.toJSONString(propertiesList), new TypeReference<List<Map<String, Object>>>() {
                });
                Map<String, Map<String, Object>> fieldNameTypeMap = viewInfoList.stream().collect(Collectors.toMap(map -> map.get(TranscendModelBaseFields.NAME).toString(), Function.identity(), (k1, k2) -> k1));
                fieldNameTypeMap.forEach((k, v) -> {
                    Map<String, Object> field = JSON.parseObject(JSON.toJSONString(v.get("field")), new TypeReference<Map<String, Object>>() {
                    });
                    Map<String, Object> options = JSON.parseObject(JSON.toJSONString(field.get("options")), new TypeReference<Map<String, Object>>() {
                    });
                    if (options.get("relationInfo") != null) {
                        Map<String, Object> relationInfo = JSON.parseObject(JSON.toJSONString(options.get("relationInfo")), new TypeReference<Map<String, Object>>() {
                        });
                        v.put("targetModelCode", relationInfo.get("targetModelCode"));
                        v.put("sourceModelCode", relationInfo.get("sourceModelCode"));
                    }
                    v.put("label", options.get("label"));
                    v.put("remoteDictType", options.get("remoteDictType"));
                });
                cfgViewMetaMap.put(e, fieldNameTypeMap);
            });
        }
        return cfgViewMetaMap;
    }

    /**
     * @param spaceAppBids
     * @return {@link Map }<{@link String }, {@link Map }<{@link String }, {@link String }>>
     */
    private Map<String, String> getMultiObjectLifeCycleMap(List<String> spaceAppBids) {
        Map<String, String> lifeCycleCodeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (CollectionUtils.isNotEmpty(spaceAppBids)) {
            spaceAppBids.forEach(e -> {
                ApmLifeCycleStateVO lifeCycleState = iAmSpaceAppConfigManageService.getLifeCycleState(e);
                if (lifeCycleState != null && lifeCycleState.getApmStateVOList() != null) {
                    Map<String, String> lifeCycleMap = lifeCycleState.getApmStateVOList().stream().collect(Collectors.toMap(ApmStateVO::getLifeCycleCode, ApmStateVO::getLifeCycleName, (k1, k2) -> k1, HashMap::new));
                    lifeCycleCodeMap.putAll(lifeCycleMap);
                }
            });
        }
        lifeCycleCodeMap.put("INIT", "已创建");
        return lifeCycleCodeMap;
    }

    /**
     * 设置数据的排序号，按照父子级生成层级序号
     * clone对象，防止子级有多个父级的情况，导致数据父子级序号错误
     *
     * @param dataList    平铺数据列表
     * @param loopData    当前需处理的数据
     * @param parentLevel 上层编号
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> levelDataHandler(List<Map<String, Object>> dataList, List<Map<String, Object>> loopData, String parentLevel) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        AtomicReference<Integer> num = new AtomicReference<>(1);
        String childrenKey = "children";
        String exportNumKey = "exportNum";
        String rootFlag = "0";
        if (rootFlag.equals(parentLevel) && CollectionUtils.isNotEmpty(loopData)) {
            loopData.forEach(data -> {
                String exportNum = num.getAndUpdate(n -> n + 1).toString();
                data.put(exportNumKey, exportNum);
                resultList.add(data);
                // 处理子级数据
                List<Map<String, Object>> children;
                if (data.containsKey(childrenKey)) {
                    children = (List<Map<String, Object>>) data.get(childrenKey);
                } else {
                    children = dataList.stream()
                            .filter(e -> e.get(TranscendModelBaseFields.PARENT_BID).equals(data.get(TranscendModelBaseFields.BID)))
                            .collect(Collectors.toCollection(LinkedList::new));
                }

                if (CollectionUtils.isNotEmpty(children)) {
                    List<Map<String, Object>> childrenList = levelDataHandler(dataList, children, exportNum);
                    resultList.addAll(childrenList);
                }
            });
        } else {
            loopData.forEach(data -> {
                // clone对象，防止子级有多个父级的情况，导致数据父子级序号错误
                Map<String, Object> cloneData = new HashMap<>(data);
                String exportNum = parentLevel + "." + num.getAndUpdate(n -> n + 1);
                cloneData.put(exportNumKey, exportNum);
                resultList.add(cloneData);
                List<Map<String, Object>> children;
                if (data.containsKey(childrenKey)) {
                    children = (List<Map<String, Object>>) data.get(childrenKey);
                } else {
                    children = dataList.stream().filter(o -> ObjectUtils.isNotEmpty(data.get("treeBid"))
                                    && o.get(TranscendModelBaseFields.PARENT_BID).toString().equals(data.get("treeBid").toString()))
                            .collect(Collectors.toCollection(LinkedList::new));
                }

                // 处理子级数据
                if (CollectionUtils.isNotEmpty(children)) {
                    List<Map<String, Object>> childrenList = levelDataHandler(dataList, children, exportNum);
                    resultList.addAll(childrenList);
                }
            });
        }
        return resultList;
    }

    /**
     * 表头数据类型处理
     *
     * @param header
     * @param dateFields
     * @param selectFields
     * @param lifeCodeFields
     * @param workItemFlowFields
     * @param richFields
     * @param instanceFields
     * @param relationFields
     * @param userFields
     * @param dictCodes
     */
    private void handleHeaderDataType(List<Map<String, Object>> header, List<String> dateFields, List<String> selectFields,
                                      List<String> lifeCodeFields, List<String> workItemFlowFields, List<String> richFields,
                                      List<String> instanceFields, List<String> relationFields, List<String> userFields,
                                      List<String> dictCodes, List<String> remoteDataField, List<String> cascader, List<String> spaceSelectFields) {
        header.forEach(e -> {
            String fieldName = e.get("name").toString();
            String fieldType = e.get("type").toString();
            if (ViewFieldDataTypeEnum.DATE.getCode().equals(fieldType)) {
                dateFields.add(fieldName);
            } else if (ViewFieldDataTypeEnum.RELATION.getCode().equals(fieldType)) {
                relationFields.add(fieldName);
            } else if (ViewFieldDataTypeEnum.RICH_EDITOR.getCode().equals(fieldType)) {
                richFields.add(fieldName);
            } else if (ViewFieldDataTypeEnum.USER.getCode().equals(fieldType) || ViewFieldDataTypeEnum.CHARACTER_ROLE.getCode().equals(fieldType)) {
                userFields.add(fieldName);
            } else if (ViewFieldDataTypeEnum.INSTANCE_SELECT.getCode().equals(fieldType) || ViewFieldDataTypeEnum.LABEL_SELECT.getCode().equals(fieldType)) {
                instanceFields.add(fieldName);
            } else if (ViewFieldDataTypeEnum.SELECT.getCode().equals(fieldType)) {
                Object dictCode = e.get("remoteDictType");
                if (ObjectUtils.isNotEmpty(dictCode)) {
                    if ("__lcTepState__".equals(dictCode.toString())) {
                        lifeCodeFields.add(fieldName);
                    } else if ("__workItemType__".equals(dictCode.toString())) {
                        workItemFlowFields.add(fieldName);
                    } else {
                        selectFields.add(fieldName);
                        dictCodes.add(dictCode.toString());
                    }
                } else if (ObjectUtils.isNotEmpty(e.get("apiUrl"))) {
                    remoteDataField.add(fieldName);
                }
            } else if (ViewFieldDataTypeEnum.CASCADE.getCode().equals(fieldType)) {
                cascader.add(fieldName);
            } else if (ViewFieldDataTypeEnum.SPACE_SELECT.getCode().equals(fieldType)) {
                spaceSelectFields.add(fieldName);
            }
        });
    }

    private Map<String, String> collectUserMap(List<Map<String, Object>> dataList, List<Map<String, Object>> header) {
        List<String> jobNums = Lists.newArrayList();
        dataList.forEach(data -> {
            header.forEach(e -> {
                if (ViewFieldDataTypeEnum.USER.getCode().equals(e.get("type")) || ViewFieldDataTypeEnum.CHARACTER_ROLE.getCode().equals(e.get("type"))) {
                    // 存在单选和多选的情况, 如果原来是单选，现在是多选，会存在脏数据
                    try {
                        List<String> jobNumList = JSON.parseArray(JSON.toJSONString(data.get(e.get("name"))), String.class);
                        if (CollectionUtils.isNotEmpty(jobNumList)) {
                            jobNums.addAll(jobNumList);
                        }
                    } catch (Exception exp) {
                        String jobNumber = data.get(e.get("name")).toString();
                        if (StringUtils.isNotBlank(jobNumber)) {
                            jobNums.add(jobNumber);
                        }
                    }
                }
            });
        });
        // 查询用户信息
        if (CollectionUtils.isEmpty(jobNums)) {
            return Maps.newHashMap();
        }
        Map<String, String> employeeInfos = openUserService.batchFindByEmoNo(jobNums.stream().distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EmployeeInfo::getJobNumber, v -> v.getName() + "(" + v.getJobNumber() + ")", (k1, k2) -> k1));
        log.info("收集到用户信息:{}", JSON.toJSONString(employeeInfos));
        return employeeInfos;
    }

    /**
     * @param spaceBid
     * @param relationFieldInfo
     * @return {@link Map }<{@link String }, {@link Map }<{@link String }, {@link String }>>
     */
    private Map<String, Map<String, String>> collectRelationMap(String spaceBid, List<Map<String, Object>> relationFieldInfo) {
        Map<String, Map<String, String>> relationMap = Maps.newHashMap();
        relationFieldInfo.forEach(e -> {
            Object sourceModelCode = e.get("sourceModelCode");
            if (sourceModelCode != null) {
                // 构建参数
                BaseRequest<ModelMixQo> pageQo = new BaseRequest<>();
                pageQo.setCurrent(1);
                pageQo.setSize(10000);
                ModelMixQo modelMixQo = new ModelMixQo();

                Order order = new Order();
                order.setDesc(true);
                order.setProperty(TranscendModelBaseFields.UPDATED_TIME);
                modelMixQo.setOrders(Lists.newArrayList(order));

                ModelFilterQo filterQo = new ModelFilterQo();
                filterQo.setCondition("EQ");
                filterQo.setProperty(TranscendModelBaseFields.SPACE_BID);
                filterQo.setValue(spaceBid);
                modelMixQo.setQueries(Lists.newArrayList(filterQo));

                pageQo.setParam(modelMixQo);
                List<MObject> data = (List<MObject>) objectModelCrudI.page(sourceModelCode.toString(), QueryConveterTool.convert(pageQo), true).getData();

                Map<String, String> dataMap = data.stream().filter(item -> ObjectUtils.isNotEmpty(item.getBid()))
                        .collect(Collectors.toMap(MObject::getBid, item -> ObjectUtils.isEmpty(item.getName()) ? "" : item.getName(), (k1, k2) -> k1, HashMap::new));
                relationMap.put(sourceModelCode.toString(), dataMap);
            }
        });
        return relationMap;
    }

    /**
     * @param codes
     * @param type
     * @return {@link Map }<{@link String }, {@link Map }<{@link String }, {@link Object }>>
     */
    public Map<String, Map<String, Object>> getNameByDictCodes(List<String> codes, String type) {
        if (CollectionUtils.isEmpty(codes)) {
            return Maps.newHashMap();
        }
        CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
        qo.setCodes(codes);
        List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
        if (CollectionUtils.isEmpty(cfgDictionaryVos)) {
            return Maps.newHashMap();
        }
        Map<String, Map<String, Object>> dicts = Maps.newHashMap();
        cfgDictionaryVos.forEach(e -> {
            Map<String, Object> map = Maps.newHashMap();
            List<CfgDictionaryItemVo> dictionaryItems = e.getDictionaryItems();
            dictionaryItems.forEach(item -> {
                map.put(item.getKeyCode(), item.get(type));
            });
            dicts.put(e.getCode(), map);
        });
        return dicts;
    }

    /**
     * @param spaceBid
     * @param tabBid
     * @return {@link MultiAppConfig }
     */
    private MultiAppConfig getApmLaneTabVOSFromRelation(String spaceBid, String tabBid) {
        ApmSpaceAppTab apmSpaceAppTab = apmSpaceAppTabService.getByBid(tabBid);
        if (Objects.isNull(apmSpaceAppTab)) {
            throw new TranscendBizException("关系对象不存在");
        }
        MultiAppConfig multiAppTreeConfig = apmSpaceAppTab.getMultiAppTreeContent();
        if (Objects.isNull(multiAppTreeConfig)) {
            throw new TranscendBizException("关系对象配置信息不存在");
        }
        return multiAppTreeConfig;
    }

    /**
     * @param spaceBid
     * @param spaceAppBid
     * @return {@link MultiAppConfig }
     */
    @NotNull
    private MultiAppConfig getApmLaneTabVOSFromView(String spaceBid, String spaceAppBid) {
        LambdaQueryWrapper<ApmSpaceAppViewModelPo> viewModelQueryWrapper = Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery().eq(ApmSpaceAppViewModelPo::getSpaceAppBid, spaceAppBid)
                .eq(ApmSpaceAppViewModelPo::getCode, AppViewModelEnum.MULTI_TREE_VIEW.getCode());
        ApmSpaceAppViewModelPo apmSpaceAppViewModelPo = apmSpaceAppViewModelService.getOne(viewModelQueryWrapper);
        if (Objects.isNull(apmSpaceAppViewModelPo) || CollectionUtils.isEmpty(apmSpaceAppViewModelPo.getConfigContent()) || apmSpaceAppViewModelPo.getConfigContent().get("multiAppTreeConfig") == null) {
            throw new TranscendBizException("多对象树配置信息不存在");
        }
        return JSON.parseObject(JSON.toJSONString(apmSpaceAppViewModelPo.getConfigContent()), MultiAppConfig.class);
    }

    /**
     * @param multiAppConfig
     * @return {@link List }<{@link ApmLaneTabVO }>
     */
    private List<ApmLaneTabVO> buildLaneTabListResult(MultiAppConfig multiAppConfig) {
        List<MultiAppTree> multiAppTree = multiAppConfig.getMultiAppTree();
        if (CollectionUtils.isEmpty(multiAppTree)) {
            throw new TranscendBizException("多对象树信息不存在");
        }
        List<ApmLaneTabVO> apmLaneTabVOS = buildLaneTabList2(multiAppConfig.getMultiAppTreeConfig(), null);
        int size = apmLaneTabVOS.size();
        for (int i = 0; i < size; i++) {
            ApmLaneTabVO apmLaneTabVO = apmLaneTabVOS.get(i);
            apmLaneTabVO.setSourceAppBid(multiAppTree.get(i).getBid());
            apmLaneTabVO.setSourceAppName(multiAppTree.get(i).getName());
            apmLaneTabVO.setTargetAppBid(multiAppTree.get(i + 1).getBid());
            apmLaneTabVO.setTargetAppName(multiAppTree.get(i + 1).getName());
        }
        return apmLaneTabVOS;
    }

    /**
     * @param multiAppTreeConfig
     * @param sourceLane
     * @return {@link List }<{@link ApmLaneTabVO }>
     */
    private List<ApmLaneTabVO> buildLaneTabList2(MultiTreeConfigVo multiAppTreeConfig, ApmLaneTabVO sourceLane) {
        if (multiAppTreeConfig == null) {
            throw new TranscendBizException("多对象树配置信息不存在");
        }
        List<ApmLaneTabVO> laneTabVOList = new ArrayList<>();
        if (null == sourceLane) {
            sourceLane = new ApmLaneTabVO();
            sourceLane.setSourceModelCode(multiAppTreeConfig.getSourceModelCode());
            sourceLane.setRelationModelCode(multiAppTreeConfig.getRelationModelCode());
            laneTabVOList.addAll(buildLaneTabList2(multiAppTreeConfig.getTargetModelCode(), sourceLane));
        } else {
            sourceLane.setTargetModelCode(multiAppTreeConfig.getSourceModelCode());
            laneTabVOList.add(sourceLane);
            if (multiAppTreeConfig.getTargetModelCode() == null) {
                return laneTabVOList;
            }
            ApmLaneTabVO nextSourceLane = new ApmLaneTabVO();
            nextSourceLane.setSourceModelCode(multiAppTreeConfig.getSourceModelCode());
            nextSourceLane.setRelationModelCode(multiAppTreeConfig.getRelationModelCode());
            laneTabVOList.addAll(buildLaneTabList2(multiAppTreeConfig.getTargetModelCode(), nextSourceLane));
        }
        return laneTabVOList;
    }

    /**
     * @param multiAppTreeConfig
     * @param sourceLane
     * @param spaceBid
     * @return {@link List }<{@link ApmLaneTabVO }>
     */
    private List<ApmLaneTabVO> buildLaneTabList(MultiTreeConfigVo multiAppTreeConfig, ApmLaneTabVO sourceLane, String spaceBid) {
        if (multiAppTreeConfig == null) {
            throw new TranscendBizException("多对象树配置信息不存在");
        }
        List<ApmLaneTabVO> laneTabVOList = new ArrayList<>();
        if (null == sourceLane) {
            sourceLane = new ApmLaneTabVO();
            sourceLane.setSourceModelCode(multiAppTreeConfig.getSourceModelCode());
            sourceLane.setRelationModelCode(multiAppTreeConfig.getRelationModelCode());
            //通过sourceModelCode查询应用 和 空间bid
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getBySpaceBidAndModelCode(spaceBid, multiAppTreeConfig.getSourceModelCode());
            sourceLane.setSourceAppName(apmSpaceApp.getName());
            sourceLane.setSourceAppBid(apmSpaceApp.getBid());
            laneTabVOList.addAll(buildLaneTabList(multiAppTreeConfig.getTargetModelCode(), sourceLane, spaceBid));
        } else {
            sourceLane.setTargetModelCode(multiAppTreeConfig.getSourceModelCode());
            //通过sourceModelCode查询应用 和 空间bid
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getBySpaceBidAndModelCode(spaceBid, multiAppTreeConfig.getSourceModelCode());
            sourceLane.setTargetAppName(apmSpaceApp.getName());
            sourceLane.setTargetAppBid(apmSpaceApp.getBid());
            laneTabVOList.add(sourceLane);
            if (multiAppTreeConfig.getTargetModelCode() == null) {
                return laneTabVOList;
            }
            ApmLaneTabVO nextSourceLane = new ApmLaneTabVO();
            nextSourceLane.setSourceModelCode(multiAppTreeConfig.getSourceModelCode());
            nextSourceLane.setRelationModelCode(multiAppTreeConfig.getRelationModelCode());
            nextSourceLane.setSourceAppName(apmSpaceApp.getName());
            nextSourceLane.setSourceAppBid(apmSpaceApp.getBid());
            laneTabVOList.addAll(buildLaneTabList(multiAppTreeConfig.getTargetModelCode(), nextSourceLane, spaceBid));
        }
        return laneTabVOList;
    }

    /**
     * @param relationInfo
     * @param appList
     * @param relationChainMapTab
     * @param sourceModelCode
     * @param results
     */
    private static void relationAppTabAppend(CfgObjectRelationVo relationInfo, List<ApmSpaceApp> appList, Map<String, Set<String>>
            relationChainMapTab, String sourceModelCode, List<ApmObjectRelationAppVo> results) {
        appList.forEach(appInfo -> {
            String targetAppModelCode = appInfo.getModelCode();
            // 不匹配链式源对象 全部添加
            // 匹配链式源对象，再看到是否匹配链式目标对象（不匹配，直接跳出循环）
            if (relationChainMapTab.containsKey(sourceModelCode) &&
                    !relationChainMapTab.get(sourceModelCode).contains(targetAppModelCode)) {
                return;
            }
            // 临时去掉规划需求的tab 多个需求存在的场景，不能存在需求关系 TODO
            if (appList.size() > 1 && targetAppModelCode.equals(DEMAND_MODEL_CODE)) {
                return;
            }
            ApmObjectRelationAppVo vo = new ApmObjectRelationAppVo();
            BeanUtils.copyProperties(relationInfo, vo);
            vo.setBid(appInfo.getBid());
            vo.setName(appInfo.getName());
            vo.setSpaceBid(appInfo.getSpaceBid());
            vo.setSpaceAppBid(appInfo.getBid());
            vo.setTargetModelCode(appInfo.getModelCode());
            if (!appInfo.getModelCode().equals(relationInfo.getTargetModelCode())) {
                vo.setShowType(AppViewModelEnum.TABLE_VIEW.getEn());
            }
            if (!Objects.equals(appInfo.getModelCode(), relationInfo.getTargetModelCode())) {
                vo.setTabName(appInfo.getName());
            }
            vo.setRelationAttr(relationInfo.getRelationAttr());
            results.add(vo);
        });
    }

    /**
     * 补充内置的应用（操作记录+评论）
     *
     * @param results 结果集
     */
    private static void defaultTabAppend(List<ApmObjectRelationAppVo> results) {
        // 补充操作记录
        ApmObjectRelationAppVo logTab = new ApmObjectRelationAppVo();
        logTab.setTabName(CfgSysAppTabEnum.OPERATION_RECORD.getDescription());
        logTab.setModelCode(CfgSysAppTabEnum.OPERATION_RECORD.getModelCode());
        logTab.setBid(CfgSysAppTabEnum.OPERATION_RECORD.getModelCode());
        logTab.setShowType("log");
        results.add(logTab);
        // 补充评论
        ApmObjectRelationAppVo commentTab = new ApmObjectRelationAppVo();
        commentTab.setTabName(CfgSysAppTabEnum.COMMENT.getDescription());
        commentTab.setModelCode(CfgSysAppTabEnum.COMMENT.getModelCode());
        commentTab.setBid(CfgSysAppTabEnum.COMMENT.getModelCode());
        commentTab.setShowType("comment");
        results.add(commentTab);
    }

    private Map<String, String> getHeaderMap() {
        HttpServletRequest servletRequest = WebUtil.getServletRequest();
        Map<String, String> headers = WebUtil.getHeaderMap(servletRequest);
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setAppId(TranscendForgePublicLogin.getHeaderValue(headers,"P-AppId"));
        tokenRequest.setRtoken(TranscendForgePublicLogin.getHeaderValue(headers,"P-Auth"));
        tokenRequest.setUtoken(TranscendForgePublicLogin.getHeaderValue(headers,"P-Rtoken"));
        BaseResponse<TokenDTO> req = uacTokenService.getRtoken(tokenRequest);
        if (!req.isSuccess()) {
            throw new PlmBizException(req.getCode(), req.getMessage());
        } else {
            TokenDTO dto = req.getData();
            Map<String, String> headerMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            headerMap.put("P-AppId", TranscendForgePublicLogin.getHeaderValue(headers,"P-AppId"));
            headerMap.put("P-Auth", dto.getRtoken());
            headerMap.put("P-Rtoken", dto.getUtoken());
            return headerMap;
        }
    }

    private Map<String, String> getInstanceBidMap(String spaceBid, String modelCode) {
        Map<String, String> bidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (StringUtils.isEmpty(spaceBid) || StringUtils.isEmpty(modelCode)) {
            return bidMap;
        }
        com.transcend.plm.datadriven.api.model.QueryWrapper qo = new com.transcend.plm.datadriven.api.model.QueryWrapper(Boolean.FALSE);
        qo.eq("spaceBid", spaceBid).and().eq("deleteFlag", 0);
        List<MObject> mObjectList = objectModelCrudI.list(modelCode, com.transcend.plm.datadriven.api.model.QueryWrapper.buildSqlQo(qo));
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(mObjectList)) {
            for (MObject mObject : mObjectList) {
                bidMap.put(mObject.getBid(), mObject.getName());
            }
        }
        return bidMap;
    }
}
