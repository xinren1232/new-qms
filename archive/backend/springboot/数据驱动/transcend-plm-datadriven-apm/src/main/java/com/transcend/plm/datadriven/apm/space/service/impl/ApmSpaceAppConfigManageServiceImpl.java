package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.*;
import com.transcend.plm.configcenter.api.model.attribute.vo.CfgAttributeVo;
import com.transcend.plm.configcenter.api.model.enums.ViewEnums;
import com.transcend.plm.configcenter.api.model.lifecycle.dto.TemplateDto;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.CfgLifeCycleTemplateNodeVo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryFilterConditionEnum;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplateNode;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowApplicationService;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowTemplateNodeService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleDomainService;
import com.transcend.plm.datadriven.apm.space.converter.ApmSpaceAppTabConverter;
import com.transcend.plm.datadriven.apm.space.converter.ApmSpaceAppViewModelConverter;
import com.transcend.plm.datadriven.apm.space.converter.MultiViewMetaListConverter;
import com.transcend.plm.datadriven.apm.space.model.AppViewModelEnum;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.*;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceTabQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmStateQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.*;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceAppViewModelMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppTab;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppTabHeaderService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppTabService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppViewModelService;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.space.service.IAppExcelTemplateService;
import com.transcend.plm.datadriven.apm.space.utils.MultiTreeUtils;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.enums.DefaultParamEnum;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.expression.SimpleExpression;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.ObjectCodeUtils;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
public class ApmSpaceAppConfigManageServiceImpl implements IApmSpaceAppConfigManageService {
    @Resource
    @Lazy
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private CfgViewFeignClient viewFeignClient;
    @Resource
    private ApmSpaceAppViewModelMapper apmSpaceAppViewModelMapper;

    @Resource
    private CfgObjectFeignClient objectFeignClient;
    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationFeignClient;
    @Resource
    private ApmSpaceAppViewModelService apmSpaceAppViewModelService;
    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;
    @Resource
    private ApmSpaceAppTabService apmSpaceAppTabService;
    @Resource
    private ApmFlowApplicationService apmFlowApplicationService;
    @Resource
    private LifeCycleFeignClient lifeCycleFeignClient;

    @Resource
    private CfgObjectRelationFeignClient objectRelationFeignClient;

    @Resource
    private IAppExcelTemplateService appExcelTemplateService;

    @Resource
    private ApmAppTabHeaderService apmAppTabHeaderService;

    @Resource
    private ApmFlowTemplateNodeService apmFlowTemplateNodeService;

    @Resource
    private CfgAttributeFeignClient cfgAttributeFeignClient;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI<MObject> objectModelCrudI;

    @Resource
    IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;

    @Resource
    private ApmRoleDomainService apmRoleDomainService;
    @Resource
    private ApmSphereService apmSphereService;


    /**
     * 批量保存空间视图模型
     *
     * @param spaceAppBid
     * @param appViewModelDtos
     * @return
     */
    @Override
    public Boolean batchSaveViewModel(String spaceAppBid, List<AppViewModelDto> appViewModelDtos) {
        if (CollectionUtils.isEmpty(appViewModelDtos)) {
            return true;
        }
        List<ApmSpaceAppViewModelPo> apmSpaceAppViewModelPos = ApmSpaceAppViewModelConverter.INSTANCE.vos2pos(appViewModelDtos);
        return apmSpaceAppViewModelService.saveBatch(apmSpaceAppViewModelPos);
    }

    /**
     * 查询空间视图模型配置
     *
     * @param spaceAppBid 空间应用业务ID
     * @return
     */
    @Override
    public List<AppViewModelDto> listViewModel(String spaceAppBid) {
        ApmSpaceAppViewModelPo viewModel = new ApmSpaceAppViewModelPo();
        viewModel.setSpaceAppBid(spaceAppBid);
        List<ApmSpaceAppViewModelPo> resultViewModels = apmSpaceAppViewModelService.listByCondition(viewModel);
        // 如果没有配置，则初始化存储到应用的视图模式中，但是只能是未启用的
        if (CollectionUtils.isEmpty(resultViewModels)) {
            List<AppViewModelDto> defaultAppViewModelDtos = getDefaultAppViewModels(spaceAppBid, CommonConst.ENABLE_FLAG_DISABLE);
            batchSaveViewModel(spaceAppBid, defaultAppViewModelDtos);
            return defaultAppViewModelDtos;
        }
        return ApmSpaceAppViewModelConverter.INSTANCE.pos2vos(resultViewModels);
    }

    /**
     * 查询空间视图模型配置（按权限）
     *
     * @param spaceAppBid 空间应用业务ID
     * @return
     */
    @Override
    public List<AppViewModelDto> listViewModelByPermission(String spaceAppBid) {
        ApmSpaceAppViewModelPo viewModel = new ApmSpaceAppViewModelPo();
        viewModel.setSpaceAppBid(spaceAppBid);
        List<ApmSpaceAppViewModelPo> resultViewModels = apmSpaceAppViewModelService.listByCondition(viewModel);
        // 如果没有配置，则初始化存储到应用的视图模式中，但是只能是未启用的
        if (CollectionUtils.isEmpty(resultViewModels)) {
            List<AppViewModelDto> defaultAppViewModelDtos = getDefaultAppViewModels(spaceAppBid, CommonConst.ENABLE_FLAG_DISABLE);
            batchSaveViewModel(spaceAppBid, defaultAppViewModelDtos);
            return defaultAppViewModelDtos;
        }
        return ApmSpaceAppViewModelConverter.INSTANCE.pos2vos(resultViewModels);
    }

    @Override
    public CfgViewVo baseViewGet(String spaceAppBid) {
        return baseViewGet(spaceAppBid, null, null);
    }

    @Override
    public AppViewModelDto getViewModelDetail(String spaceAppBid, String viewModelCode) {
        ApmSpaceAppViewModelPo apmSpaceAppViewModelPo = apmSpaceAppViewModelMapper.getByCodeAndSpaceAppBid(spaceAppBid, viewModelCode);
        // 没有则立即创建
        if (apmSpaceAppViewModelPo == null) {
            AppViewModelEnum appViewModelEnum = null;
            AppViewModelEnum[] values = AppViewModelEnum.values();
            for (AppViewModelEnum value : values) {
                if (value.getCode().equals(viewModelCode)) {
                    appViewModelEnum = value;
                    break;
                }
            }
            if (appViewModelEnum == null) {
                return null;
            }
            Map<String, Object> defaultMap = Maps.newHashMap();
            AppViewModelDto appViewModelDto = AppViewModelDto.of()
                    .setBid(SnowflakeIdWorker.nextIdStr())
                    .setSpaceAppBid(spaceAppBid)
                    .setCode(appViewModelEnum.getCode())
                    .setName(appViewModelEnum.getZh())
                    .setConfigContent(defaultMap)
                    .setEnableFlag(CommonConst.ENABLE_FLAG_DISABLE).setSort(30);
            apmSpaceAppViewModelPo = ApmSpaceAppViewModelConverter.INSTANCE.dto2po(appViewModelDto);
            apmSpaceAppViewModelMapper.insert(apmSpaceAppViewModelPo);
            return appViewModelDto;
        } else {
            return ApmSpaceAppViewModelConverter.INSTANCE.po2vo(apmSpaceAppViewModelPo);
        }
    }

    @Override
    public CfgViewVo baseViewGet(String spaceAppBid, String viewScope, String viewType) {
        ViewQueryParam viewQueryParam = ViewQueryParam.of()
                .setViewBelongBid(spaceAppBid)
                .setViewScope(viewScope)
                .setViewType(viewType);
        return getView(viewQueryParam);
    }

    @Override
    public CfgViewVo baseViewGet(String spaceAppBid, String type) {
        String viewBid = spaceAppBid;
        // 不为空则拼接新的视图bid todo 临时方案，后续需要在配置中心处理类型，否则无法复制视图内容
        if (StringUtil.isNotBlank(type)) {
            viewBid = viewBid + "_" + type;
        }
        return viewFeignClient.getByBid(viewBid).getCheckExceptionData();
    }

    /**
     * @param spaceBid
     * @param sourceModelCode
     * @param sourceModelCode
     * @return
     */
    @Override
    public List<ApmCrossRelationVO> listCrossRelationByModelCode(String spaceBid, String spaceAppBid, String sourceModelCode) {
        String targetModelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid);
        // 根据空间bid 获取空间下所有的模型应用
        List<ApmSpaceAppVo> apmSpaceAppVos = apmSpaceApplicationService.listApp(spaceBid);
        Map<String, ApmSpaceAppVo> modelCodeSpaceAppMap = apmSpaceAppVos.stream().collect(Collectors.toMap(ApmSpaceAppVo::getModelCode, Function.identity(), (k1, k2) -> k1));
        Map<String, List<CfgObjectRelationVo>> listGroupCrossRelationBySTModelCode = objectRelationFeignClient
                .listGroupCrossRelationBySTModelCode(sourceModelCode, targetModelCode, modelCodeSpaceAppMap.keySet()).getCheckExceptionData();
        // 收集结果
        List<ApmCrossRelationVO> result = new ArrayList<>();
        String jianTou = "->";
        // 寻找
        listGroupCrossRelationBySTModelCode.forEach(
                (key, values) -> {
                    // 名称收集
                    StringBuilder nameBuilder = new StringBuilder(
                            modelCodeSpaceAppMap.get(sourceModelCode).getName()
                    );
                    nameBuilder.append(jianTou);
                    values.forEach(value ->
                            nameBuilder.append(modelCodeSpaceAppMap.get(value.getSourceModelCode()).getName()).append(jianTou)
                    );
                    result.add(
                            ApmCrossRelationVO.of()
                                    .setCode(key)
                                    .setName(nameBuilder.toString().substring(0, nameBuilder.length() - 2))
                                    .setInfos(values)
                    );
                }

        );
        return result;
    }

    @Override
    public void downloadImportTemplate(String spaceAppBid, String type, HttpServletResponse response) {
        String fileName = null;
        try {
            fileName = URLEncoder.encode(com.transcend.framework.common.util.SnowflakeIdWorker.nextIdStr() + ".xlsx", StandardCharsets.UTF_8.name()).replace("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new TranscendBizException("生成文件名称错误", e);
        }
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        try (ByteArrayOutputStream outputStream = appExcelTemplateService.handleExcelTemplate(spaceAppBid, type)) {
            response.getOutputStream().write(outputStream.toByteArray());
        } catch (IOException e) {
            throw new TranscendBizException("生成应用Excel模板失败", e);
        }
    }

    @Override
    public CfgViewVo baseViewSaveOrUpdate(String spaceAppBid, String type, Map<String, Object> content) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        String viewBid = spaceAppBid;
        // 不为空则拼接新的视图bid todo 临时方案，后续需要在配置中心处理类型，否则无法复制视图内容
        if (StringUtil.isNotBlank(type)) {
            viewBid = viewBid + "_" + type;
        }
        // 初始化内容
        CfgViewDto cfgViewDto = CfgViewDto.builder()
                .belongBid(viewBid)
                .viewType("default")
                .viewScope("default")
                .content(content)
                .modelCode(apmSpaceApp.getModelCode())
                .enableFlag(CommonConst.ENABLE_FLAG_ENABLE)
                .name(apmSpaceApp.getName() + "-空间应用-" + viewBid)
                .type("OBJECT")
                .build();
        // 初始化内容
        return viewFeignClient.saveOrUpdateView(cfgViewDto).getCheckExceptionData();
    }

    @Override
    public CfgViewVo baseViewSaveOrUpdate(String spaceAppBid, Map<String, Object> content) {

        return baseViewSaveOrUpdate(spaceAppBid, null, content);
    }

    @Override
    public List<CfgObjectAttributeVo> baseViewListAttributes(String spaceAppBid) {
        return objectFeignClient.getByModelCode(
                apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid)
        ).getCheckExceptionData().getAttrList();
    }

    @Override
    public List<CfgObjectRelationVo> baseViewListTargetRelation(String spaceAppBid) {
        // 以目标对象驱动查询的关系列表
        return cfgObjectRelationFeignClient.listRelationByTargetModelCode(
                apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid)
        ).getCheckExceptionData();
    }

    @Override
    @CacheEvict(value = CacheNameConstant.SPACE_APP_VIEW_MODEL_LIST, key = "#spaceAppBid")
    public Boolean viewModelChangeEnableFlag(String spaceAppBid, String viewModelCode, Integer enableFlag) {
        // TODO
        return apmSpaceAppViewModelService.changeEnableFlag(spaceAppBid, viewModelCode, enableFlag);
    }

    @Override
    public List<CfgObjectRelationVo> relationListTab(String spaceAppBid) {
        // 以目标对象驱动查询的关系列表
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        return cfgObjectRelationFeignClient.listRelationTab(app.getModelCode()).getCheckExceptionData();
    }


    @Override
    public ApmLifeCycleStateVO getLifeCycleStateByModelCode(String spaceBid, String modelCode) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getBySpaceBidAndModelCode(spaceBid, modelCode);
        if (apmSpaceApp == null) {
            throw new PlmBizException("当前空间下未配置应用");
        }
        return getLifeCycleState(apmSpaceApp.getBid());
    }

    @Override
    @Cacheable(value = CacheNameConstant.APM_LIFE_CYCLE_STATE, key = "#spaceAppBid")
    public ApmLifeCycleStateVO getLifeCycleState(String spaceAppBid) {
        //先根据spaceAppBid查询状态流程数据
        ApmLifeCycleStateVO apmLifeCycleStateVO = apmFlowApplicationService.getLifeCycleState(spaceAppBid);
        if (apmLifeCycleStateVO == null || CollectionUtils.isEmpty(apmLifeCycleStateVO.getApmStateVOList())) {
            apmLifeCycleStateVO = new ApmLifeCycleStateVO();
            //取生命周期模板数据
            ObjectModelLifeCycleVO objectModelLifeCycleVO = objectFeignClient.findObjectLifecycleByModelCode(
                    apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid)
            ).getCheckExceptionData();
            apmLifeCycleStateVO.setLcTemplBid(objectModelLifeCycleVO.getLcTemplBid());
            apmLifeCycleStateVO.setLcTemplName(objectModelLifeCycleVO.getLcTemplName());
            apmLifeCycleStateVO.setInitState(objectModelLifeCycleVO.getInitState());
            apmLifeCycleStateVO.setInitStateName(objectModelLifeCycleVO.getInitStateName());
            apmLifeCycleStateVO.setModelCode(objectModelLifeCycleVO.getModelCode());
            apmLifeCycleStateVO.setLifeCycleStateType("lifyCycle");
            apmLifeCycleStateVO.setLcTemplVersion(objectModelLifeCycleVO.getLcTemplVersion());
            List<ApmStateVO> apmStateVOList = getLifeCycleState(objectModelLifeCycleVO.getLcTemplBid(), objectModelLifeCycleVO.getLcTemplVersion(), objectModelLifeCycleVO.getModelCode(), objectModelLifeCycleVO.getInitState(), false);
            apmLifeCycleStateVO.setApmStateVOList(apmStateVOList);
            // 设置生命周期阶段映射
            Map<String, ApmStateVO> phaseStateMap = getPhaseStateMap(objectModelLifeCycleVO.getLcTemplBid(), objectModelLifeCycleVO.getLcTemplVersion(), objectModelLifeCycleVO.getModelCode(), objectModelLifeCycleVO.getInitState());
            apmLifeCycleStateVO.setPhaseStateMap(phaseStateMap);
        } else {
            if (StringUtils.isEmpty(apmLifeCycleStateVO.getInitState())) {
                ObjectModelLifeCycleVO objectModelLifeCycleVO = objectFeignClient.findObjectLifecycleByModelCode(
                        apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid)
                ).getCheckExceptionData();
                apmLifeCycleStateVO.setInitState(objectModelLifeCycleVO.getInitState());
                apmLifeCycleStateVO.setInitStateName(objectModelLifeCycleVO.getInitStateName());
                apmLifeCycleStateVO.setModelCode(objectModelLifeCycleVO.getModelCode());
            } else if (StringUtils.isEmpty(apmLifeCycleStateVO.getModelCode())) {
                apmLifeCycleStateVO.setModelCode(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid));
            }
        }
        if (apmLifeCycleStateVO != null && CollectionUtils.isNotEmpty(apmLifeCycleStateVO.getApmStateVOList())) {
            setStateColor(apmLifeCycleStateVO.getApmStateVOList());
            if (CollectionUtils.isNotEmpty(apmLifeCycleStateVO.getPhaseStateMap())) {
                setStateColor(new ArrayList<>(apmLifeCycleStateVO.getPhaseStateMap().values()));
            }
        }
        return apmLifeCycleStateVO;
    }

    @Override
    public List<ApmStateVO> getMultiLifeCycleState(List<String> spaceAppBids) {
        Assert.notEmpty(spaceAppBids, "应用Bid不能为空");
        IApmSpaceAppConfigManageService proxy = (IApmSpaceAppConfigManageService) AopContext.currentProxy();
        return new ArrayList<>(spaceAppBids.parallelStream().map(proxy::getLifeCycleState)
                .flatMap(list -> list.getApmStateVOList().stream())
                .collect(Collectors.toMap(ApmStateVO::getLifeCycleCode, Function.identity(), (v1, v2) -> v1)).values());
    }


    private Map<String, ApmStateVO> getPhaseStateMap(String templateBid, String version, String modelCode, String lifeCycleCode) {
        Map<String, ApmStateVO> phaseStateMap = Maps.newConcurrentMap();
        TemplateDto templateDto = new TemplateDto();
        if (StringUtils.isEmpty(version)) {
            ObjectModelLifeCycleVO objectModelLifeCycleVO = objectFeignClient.findObjectLifecycleByModelCode(modelCode).getCheckExceptionData();
            templateDto.setTemplateBid(objectModelLifeCycleVO.getLcTemplBid());
            templateDto.setVersion(objectModelLifeCycleVO.getLcTemplVersion());
        } else {
            templateDto.setTemplateBid(templateBid);
            templateDto.setVersion(version);
        }
        templateDto.setModelCode(modelCode);
        templateDto.setCurrentLifeCycleCode(lifeCycleCode);
        // 查询所有模板节点
        List<CfgLifeCycleTemplateNodeVo> lifeCycleTemplateNodePos = lifeCycleFeignClient.getTemplateNodes(templateDto).getCheckExceptionData();
        if (CollectionUtils.isNotEmpty(lifeCycleTemplateNodePos)) {
            for (CfgLifeCycleTemplateNodeVo cfgLifeCycleTemplateNodeVo : lifeCycleTemplateNodePos) {
                String layout = cfgLifeCycleTemplateNodeVo.getLayout();
                if (StringUtils.isNotBlank(layout)) {
                    JSONObject jsonObject = JSON.parseObject(layout, JSONObject.class);
                    String shape = (String) jsonObject.get("shape");
                    String id = (String) jsonObject.get("id");
                    // 业务数据map
                    Map<String, Object> data = (Map<String, Object>) jsonObject.get("data");
                    if ("rect".endsWith(shape) && CollectionUtils.isNotEmpty(data) && data.get("phaseState") != null) {
                        Map<String, Object> phaseState = (Map<String, Object>) data.get("phaseState");
                        ApmStateVO apmStateVO = new ApmStateVO();
                        if (phaseState.get("code") != null) {
                            apmStateVO.setLifeCycleCode(phaseState.get("code") + "");
                        }
                        if (phaseState.get("name") != null) {
                            apmStateVO.setLifeCycleName(phaseState.get("name") + "");
                        }
                        apmStateVO.setNodeBid(id);
                        setApmStateVOWebAttr(apmStateVO);
                        phaseStateMap.put((String) data.get("code"), apmStateVO);
                    }
                }
            }
        }
        return phaseStateMap;
    }

    private void setStateColor(List<ApmStateVO> apmStateVOList) {
        if (CollectionUtils.isNotEmpty(apmStateVOList)) {
            List<String> lifeCycleCodes = apmStateVOList.stream().map(ApmStateVO::getLifeCycleCode).collect(Collectors.toList());
            List<LifeCycleStateVo> lifeCycleStateVos = lifeCycleFeignClient.queryByCodes(lifeCycleCodes).getCheckExceptionData();
            Map<String, LifeCycleStateVo> codeAndColorMap = lifeCycleStateVos.stream().collect(Collectors.toMap(LifeCycleStateVo::getCode, Function.identity()));
            Map<String, String> lifeCycleCodeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            for (int i = apmStateVOList.size() - 1; i >= 0; i--) {
                if (lifeCycleCodeMap.containsKey(apmStateVOList.get(i).getLifeCycleCode())) {
                    //过滤掉重复的状态
                    apmStateVOList.remove(i);
                } else {
                    lifeCycleCodeMap.put(apmStateVOList.get(i).getLifeCycleCode(), apmStateVOList.get(i).getLifeCycleCode());
                    if (codeAndColorMap.get(apmStateVOList.get(i).getLifeCycleCode()) != null) {
                        apmStateVOList.get(i).setColor(codeAndColorMap.get(apmStateVOList.get(i).getLifeCycleCode()).getColor());
                        if (StringUtils.isEmpty(apmStateVOList.get(i).getName())) {
                            apmStateVOList.get(i).setName(codeAndColorMap.get(apmStateVOList.get(i).getLifeCycleCode()).getName());
                            apmStateVOList.get(i).setLifeCycleName(codeAndColorMap.get(apmStateVOList.get(i).getLifeCycleCode()).getName());
                        }
                    }

                }
            }
        }
    }

    private List<ApmStateVO> getLifeCycleState(String templateBid, String version, String modelCode, String lifeCycleCode, boolean isNext) {
        TemplateDto templateDto = new TemplateDto();
        if (StringUtils.isEmpty(version)) {
            ObjectModelLifeCycleVO objectModelLifeCycleVO = objectFeignClient.findObjectLifecycleByModelCode(
                    modelCode
            ).getCheckExceptionData();
            templateDto.setTemplateBid(objectModelLifeCycleVO.getLcTemplBid());
            templateDto.setVersion(objectModelLifeCycleVO.getLcTemplVersion());
        } else {
            templateDto.setTemplateBid(templateBid);
            templateDto.setVersion(version);
        }
        templateDto.setModelCode(modelCode);
        templateDto.setCurrentLifeCycleCode(lifeCycleCode);
        List<CfgLifeCycleTemplateNodeVo> lifeCycleTemplateNodePos = new ArrayList<>();
        if (isNext) {
            lifeCycleTemplateNodePos = lifeCycleFeignClient.getNextTemplateNodes(templateDto).getCheckExceptionData();
        } else {
            lifeCycleTemplateNodePos = lifeCycleFeignClient.getTemplateNodes(templateDto).getCheckExceptionData();
        }
        List<ApmStateVO> apmStateVOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(lifeCycleTemplateNodePos)) {
            for (CfgLifeCycleTemplateNodeVo cfgLifeCycleTemplateNodeVo : lifeCycleTemplateNodePos) {
                ApmStateVO apmStateVO = new ApmStateVO();
                apmStateVO.setLifeCycleCode(cfgLifeCycleTemplateNodeVo.getLifeCycleCode());
                apmStateVO.setLifeCycleName(cfgLifeCycleTemplateNodeVo.getName());
                apmStateVO.setNodeBid(cfgLifeCycleTemplateNodeVo.getBid());
                setApmStateVOWebAttr(apmStateVO);
                apmStateVOList.add(apmStateVO);
            }
        }
        return apmStateVOList;
    }

    private void setApmStateVOWebAttr(ApmStateVO apmStateVO) {
        apmStateVO.setName(apmStateVO.getLifeCycleName());
        apmStateVO.setKeyCode(apmStateVO.getLifeCycleCode());
        apmStateVO.setLife_cycle_code(apmStateVO.getLifeCycleCode());
        apmStateVO.setBid(apmStateVO.getNodeBid());
        apmStateVO.setZh(apmStateVO.getName());
    }

    @Override
    public List<ApmStateVO> listNextStates(ApmStateQo apmStateQo) {
        apmStateQo.setLcTemplVersion(null);
        //先查流程的状态
        List<ApmStateVO> apmStateVOList = apmFlowApplicationService.listNextStates(apmStateQo);
        if (CollectionUtils.isEmpty(apmStateVOList)) {
            //查生命周期全部状态
            apmStateVOList = getLifeCycleState(apmStateQo.getLcTemplBid(), apmStateQo.getLcTemplVersion(), apmStateQo.getModelCode(), apmStateQo.getCurrentState(), true);
        }
        setStateColor(apmStateVOList);
        return apmStateVOList;
    }

    @Override
    public boolean copyViews(Map<String, String> viewBidMap, String type) {
        CfgViewDto cfgViewDto = new CfgViewDto();
        cfgViewDto.setViewBidMap(viewBidMap);
        cfgViewDto.setType(type);
        return viewFeignClient.copyViews(cfgViewDto).getCheckExceptionData();
    }

    @Override
    @CacheEvict(value = CacheNameConstant.SPACE_APP_VIEW_MODEL_LIST, key = "#spaceAppBid")
    public Boolean viewModelUpdatePartialContent(String spaceAppBid, String viewModelCode, Map<String, Object> configContent) {
        return viewModelUpdate(spaceAppBid, viewModelCode, AppViewModelDto.of().setConfigContent(configContent));
    }


    @Override
    @CacheEvict(value = CacheNameConstant.SPACE_APP_VIEW_MODEL_LIST, key = "#spaceAppBid")
    public Boolean viewModelUpdate(String spaceAppBid, String viewModelCode, AppViewModelDto appViewModelDto) {
        ApmSpaceAppViewModelPo spaceAppViewModel = apmSpaceAppViewModelMapper
                .selectOne(Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery()
                        .eq(ApmSpaceAppViewModelPo::getSpaceAppBid, spaceAppBid).
                        eq(ApmSpaceAppViewModelPo::getCode, viewModelCode));

        Map<String, Object> configContent = appViewModelDto.getConfigContent();
        // 处理json内容
        if (configContent != null && spaceAppViewModel.getConfigContent() != null) {
            Map<String, Object> jsonObject = Optional.ofNullable(spaceAppViewModel)
                    .map(ApmSpaceAppViewModelPo::getConfigContent).orElse(new HashMap<>(CommonConstant.START_MAP_SIZE));
            Map<String, Object> filterMap = Maps.newHashMap();
            filterMap.putAll(jsonObject);
            filterMap.putAll(configContent);
            spaceAppViewModel.setConfigContent(filterMap);
        }
        // 处理排序
        if (appViewModelDto.getSort() != null && spaceAppViewModel.getSort() != null) {
            spaceAppViewModel.setSort(appViewModelDto.getSort());
        }
        // 状态处理
        if (appViewModelDto.getEnableFlag() != null && spaceAppViewModel.getEnableFlag() != null) {
            spaceAppViewModel.setEnableFlag(appViewModelDto.getEnableFlag());
        }
        return apmSpaceAppViewModelMapper.updateById(spaceAppViewModel) > 0;
    }

    @Override
    public CfgViewVo flowNodeViewGet(String spaceAppBid, String action, String nodeBid) {
        ViewQueryParam param = ViewQueryParam.of()
                .setViewBelongBid(getNodeBidAndAction(action, nodeBid));
        return viewFeignClient.getView(param).getCheckExceptionData();
    }

    @Override
    public List<Object> getAllFlowView(String spaceAppBid, String flowBid, String version) {
        List<Object> metaList = Lists.newArrayList();
        //先查询基本视图
        CfgViewVo cfgViewVo = baseViewGet(spaceAppBid);
        if (cfgViewVo != null && cfgViewVo.getContent() != null) {
            Map<String, Object> content = cfgViewVo.getContent();
            Object obj = content.get("propertiesList");
            if (obj != null) {
                metaList.add(obj);
            }
        }
        //查询流程视图
        List<ApmFlowTemplateNode> apmFlowTemplateNodes = apmFlowTemplateNodeService.list(Wrappers.<ApmFlowTemplateNode>lambdaQuery()
                .eq(ApmFlowTemplateNode::getFlowTemplateBid, flowBid)
                .eq(ApmFlowTemplateNode::getVersion, version));
        //组装视图BID
        if (CollectionUtils.isNotEmpty(apmFlowTemplateNodes)) {
            Set<String> set = new HashSet<>();
            for (ApmFlowTemplateNode apmFlowTemplateNode : apmFlowTemplateNodes) {
                set.add(apmFlowTemplateNode.getWebBid() + "#NODE_COMPLETE");
                set.add(apmFlowTemplateNode.getWebBid() + "#NODE_ROLLBACK");
            }
            ViewQueryParam param = ViewQueryParam.of().setViewBelongBids(Lists.newArrayList(set));
            Map<String, CfgViewVo> viewVoMap = viewFeignClient.getViews(param).getCheckExceptionData();
            if (CollectionUtils.isNotEmpty(viewVoMap)) {
                List<CfgViewVo> viewList = Lists.newArrayList();
                viewList.addAll(viewVoMap.values());
                for (CfgViewVo cfgViewVo1 : viewList) {
                    Map<String, Object> content = cfgViewVo1.getContent();
                    if (CollectionUtils.isNotEmpty(content)) {
                        Object obj = content.get("propertiesList");
                        if (obj != null) {
                            metaList.add(obj);
                        }
                    }
                }
            }
        }
        return metaList;
    }

    /**
     * 获取节点的视图bid+action
     *
     * @param action  动作
     * @param nodeBid 节点bid
     * @return 视图bid+action
     */
    @NotNull
    private static String getNodeBidAndAction(String action, String nodeBid) {
        return nodeBid + "#" + action;
    }

    @Override
    public CfgViewVo flowNodeViewSaveOrUpdate(String spaceAppBid, String nodeBid, String action, Map<String, Object> content) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        String modelCode = Objects.isNull(apmSpaceApp) ? spaceAppBid : apmSpaceApp.getModelCode();
        String viewNamePrefix = Objects.isNull(apmSpaceApp) ? spaceAppBid : apmSpaceApp.getName();
        // 制作视图bid
        String viewBelongBid = getNodeBidAndAction(action, nodeBid);
        // 初始化内容
        CfgViewDto cfgViewDto = CfgViewDto.builder()
                .belongBid(viewBelongBid)
                .viewScope(ViewEnums.DEFAULT.getCode())
                .viewType(ViewEnums.DEFAULT.getCode())
                .content(content)
                .modelCode(modelCode)
                .enableFlag(CommonConst.ENABLE_FLAG_ENABLE)
                .name(viewNamePrefix + "-空间应用-流程节点-" + nodeBid + "-" + action)
                .type("OBJECT")
                .build();
        // 初始化内容
        return viewFeignClient.saveOrUpdateView(cfgViewDto).getCheckExceptionData();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveApmSpaceAppTab(String spaceBid, String spaceAppBid, ApmSpaceAppTabDto apmSpaceAppTabDto) {
        ApmSpaceAppTab apmSpaceAppTab = ApmSpaceAppTabConverter.INSTANCE.dto2entity(apmSpaceAppTabDto);
        apmSpaceAppTab.setBid(SnowflakeIdWorker.nextIdStr());
        if (StringUtils.isEmpty(apmSpaceAppTab.getCode())) {
            apmSpaceAppTab.setCode(apmSpaceAppTab.getViewModelCode());
        }
        apmSpaceAppTab.setEnableFlag(true);
        apmSpaceAppTab.setDeleteFlag(false);
        apmSpaceAppTab.setCreatedBy(SsoHelper.getJobNumber());
        apmSpaceAppTab.setSpaceAppBid(spaceAppBid);
        if (apmSpaceAppTabDto.isDeleteFlag()) {
            apmSpaceAppTab.setEnableFlag(false);
        }
        MultiAppConfig multiAppConfig = getMultiAppConfig(apmSpaceAppTabDto.getMultiAppConfigDtos());
        apmSpaceAppTab.setMultiAppTreeContent(multiAppConfig);
        apmSpaceAppTabService.deleteBySpaceAppBid(apmSpaceAppTab);
        apmSpaceAppTabService.save(apmSpaceAppTab);
        return true;
    }

    @Override
    public List<String> getAppModelCodes(String spaceAppBid, String targetSpaceAppBid) {
        ApmSpaceAppTab apmSpaceAppTab = apmSpaceAppTabService.getOne(Wrappers.<ApmSpaceAppTab>lambdaQuery().eq(ApmSpaceAppTab::getSpaceAppBid, spaceAppBid).eq(ApmSpaceAppTab::getTargetSpaceAppBid, targetSpaceAppBid).eq(ApmSpaceAppTab::getDeleteFlag, false).eq(ApmSpaceAppTab::getEnableFlag, true));
        if (apmSpaceAppTab == null) {
            return null;
        }
        MultiAppConfig multiAppConfig = apmSpaceAppTab.getMultiAppTreeContent();
        if (multiAppConfig == null || CollectionUtils.isEmpty(multiAppConfig.getMultiAppTree())) {
            return null;
        }
        List<MultiAppTree> multiAppTree = multiAppConfig.getMultiAppTree();
        List<String> modelCodes = multiAppTree.stream().map(MultiAppTree::getModelCode).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(modelCodes)) {
            return modelCodes;
        }
        List<String> appBids = multiAppTree.stream().map(MultiAppTree::getBid).collect(Collectors.toList());
        List<ApmSpaceApp> apmSpaceApps = apmSpaceAppService.list(Wrappers.<ApmSpaceApp>lambdaQuery().in(ApmSpaceApp::getBid, appBids).eq(ApmSpaceApp::getDeleteFlag, false).eq(ApmSpaceApp::getEnableFlag, true));
        return apmSpaceApps.stream().map(ApmSpaceApp::getModelCode).collect(Collectors.toList());
    }

    private MultiAppConfig getMultiAppConfig(List<MultiAppConfigDto> multiAppConfigDtos) {
        MultiAppConfig multiAppConfig = new MultiAppConfig();
        if (CollectionUtils.isNotEmpty(multiAppConfigDtos)) {
            List<MultiAppTree> multiAppTreeList = new ArrayList<>();
            Map<String, MultiAppTree> multiAppTreeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            List<String> multiAppTreeFirstLevel = new ArrayList<>();
            Map<String, MultiAppConfigDto> map = multiAppConfigDtos.stream().collect(Collectors.toMap(MultiAppConfigDto::getModelCode, e -> e));
            Map<String, String> parentCodeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            MultiTreeConfigVo multiAppTreeConfig = new MultiTreeConfigVo();
            List<String> levelModelCodes = new ArrayList<>();
            for (MultiAppConfigDto multiAppConfigDto : multiAppConfigDtos) {
                MultiAppTree multiAppTree = new MultiAppTree();
                multiAppTree.setBid(multiAppConfigDto.getBid());
                multiAppTree.setName(multiAppConfigDto.getName());
                multiAppTree.setModelCode(multiAppConfigDto.getModelCode());
                multiAppTreeMap.put(multiAppTree.getModelCode(), multiAppTree);
                if (multiAppConfigDto.isFirstLevel()) {
                    multiAppTreeFirstLevel.add(multiAppTree.getBid());
                }
                if (StringUtils.isEmpty(multiAppConfigDto.getParentModelCode())) {
                    multiAppTreeConfig.setSourceModelCode(multiAppConfigDto.getModelCode());
                } else {
                    parentCodeMap.put(multiAppConfigDto.getParentModelCode(), multiAppConfigDto.getModelCode());
                }
            }
            if (StringUtils.isEmpty(multiAppTreeConfig.getSourceModelCode())) {
                for (Map.Entry<String, String> entry : parentCodeMap.entrySet()) {
                    if (!map.containsKey(entry.getKey())) {
                        multiAppTreeConfig.setSourceModelCode(entry.getKey());
                        break;
                    }
                }
            }
            Map<String, String> allCodeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            if (StringUtils.isNotEmpty(multiAppTreeConfig.getSourceModelCode())) {
                levelModelCodes.add(multiAppTreeConfig.getSourceModelCode());
            }
            getMultiAppTreeConfig(multiAppTreeConfig, parentCodeMap, allCodeMap, levelModelCodes);
            //加工排序的应用列表
            for (String modelCode : levelModelCodes) {
                if (multiAppTreeMap.get(modelCode) != null) {
                    multiAppTreeList.add(multiAppTreeMap.get(modelCode));
                    multiAppTreeMap.remove(modelCode);
                }
            }
            if (CollectionUtils.isNotEmpty(multiAppTreeMap)) {
                multiAppTreeList.addAll(multiAppTreeMap.values());
            }
            multiAppConfig.setMultiAppTree(multiAppTreeList);
            multiAppConfig.setMultiAppTreeFirstLevel(multiAppTreeFirstLevel);
            multiAppConfig.setMultiAppTreeConfig(multiAppTreeConfig);
        }
        return multiAppConfig;
    }

    private void getMultiAppTreeConfig(MultiTreeConfigVo multiAppTreeConfig, Map<String, String> parentCodeMap, Map<String, String> allCodeMap, List<String> levelModelCodes) {
        if (parentCodeMap.containsKey(multiAppTreeConfig.getSourceModelCode())) {
            //根据source_model_code 和 target_model_code查关系model_code
            String relModelCode = relModelCode(multiAppTreeConfig.getSourceModelCode(), parentCodeMap.get(multiAppTreeConfig.getSourceModelCode()), allCodeMap);
            multiAppTreeConfig.setRelationModelCode(relModelCode);
            MultiTreeConfigVo multiAppTreeConfig1 = new MultiTreeConfigVo();
            multiAppTreeConfig1.setSourceModelCode(parentCodeMap.get(multiAppTreeConfig.getSourceModelCode()));
            levelModelCodes.add(multiAppTreeConfig1.getSourceModelCode());
            multiAppTreeConfig.setTargetModelCode(multiAppTreeConfig1);
            getMultiAppTreeConfig(multiAppTreeConfig1, parentCodeMap, allCodeMap, levelModelCodes);
        }
    }

    private String getKey(String sourceModelCode, String targetModelCode) {
        return String.format("%s:%s", sourceModelCode, targetModelCode);
    }

    private String relModelCode(String sourceModelCode, String targetModelCode, Map<String, String> allCodeMap) {
        String key = getKey(sourceModelCode, targetModelCode);
        if (allCodeMap.get(key) != null) {
            return allCodeMap.get(key);
        }
        LinkedHashSet<String> sourceModelCodes = ObjectCodeUtils.splitModelCodeDesc(sourceModelCode);
        LinkedHashSet<String> targetModelCodes = ObjectCodeUtils.splitModelCodeDesc(targetModelCode);
        List<String> sourceList = new ArrayList<>(sourceModelCodes);
        List<String> targetList = new ArrayList<>(targetModelCodes);
        List<String> allModelRelList = new ArrayList<>();
        for (String source : sourceList) {
            for (String target : targetList) {
                String st = getKey(source, target);
                allModelRelList.add(st);
                if (allCodeMap.get(st) != null) {
                    return allCodeMap.get(st);
                }
            }
        }
        Map<String, String> thisObjRelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        List<CfgObjectRelationVo> cfgObjectRelationVos = cfgObjectRelationFeignClient.listRelationBySTModelCode(
                sourceModelCode, targetModelCode).getCheckExceptionData();
        String modelCode = null;
        if (CollectionUtils.isNotEmpty(cfgObjectRelationVos)) {
            modelCode = cfgObjectRelationVos.get(0).getModelCode();
            for (CfgObjectRelationVo cfgObjectRelationVo : cfgObjectRelationVos) {
                thisObjRelMap.put(getKey(cfgObjectRelationVo.getSourceModelCode(), cfgObjectRelationVo.getTargetModelCode())
                        , cfgObjectRelationVo.getModelCode());
            }
            if (thisObjRelMap.get(getKey(sourceModelCode, targetModelCode)) != null) {
                modelCode = thisObjRelMap.get(getKey(sourceModelCode, targetModelCode));
            }
        }
        for (String modelRel : allModelRelList) {
            if (thisObjRelMap.get(modelRel) != null) {
                allCodeMap.put(modelRel, thisObjRelMap.get(modelRel));
            } else {
                allCodeMap.put(modelRel, modelCode);
            }
        }
        return modelCode;
    }


    @Override
    public List<ApmObjectRelationAppVo> relationAppListTab(ApmSpaceTabQo apmSpaceTabQo) {
        List<ApmObjectRelationAppVo> results = new ArrayList<>();
        // 以目标对象驱动查询的关系列表
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(apmSpaceTabQo.getSpaceAppBid());
        apmSpaceTabQo.setModelCode(app.getModelCode());
        List<CfgObjectRelationVo> relationVoList =
                cfgObjectRelationFeignClient.listRelationTab(app.getModelCode()).getCheckExceptionData();
        if (CollectionUtils.isEmpty(relationVoList)) {
            // 补充默认tab
            return results;
        }
        relationVoList.forEach(relationVo -> {
            List<ApmSpaceApp> appList =
                    apmSpaceAppService.listSpaceAppBySpaceBidAndModelCode(app.getSpaceBid(), relationVo.getTargetModelCode());
            if (CollectionUtils.isNotEmpty(appList)) {
                appList.forEach(appInfo -> {
                    ApmObjectRelationAppVo vo = new ApmObjectRelationAppVo();
                    BeanUtils.copyProperties(relationVo, vo);
                    vo.setSpaceBid(appInfo.getSpaceBid());
                    vo.setSpaceAppBid(appInfo.getBid());
                    vo.setTargetModelCode(appInfo.getModelCode());
                    vo.setRelationAttr(relationVo.getRelationAttr());
                    vo.setIsRefresh(false);
                    if (relationVo.getTargetModelCode().equals(app.getModelCode())) {
                        vo.setInverseQuery(!relationVo.getTargetModelCode().equals(relationVo.getSourceModelCode()));
                    }
                    results.add(vo);
                });
            }
        });

        // 补充默认
        List<ApmSpaceAppTab> apmSpaceAppTabs = apmSpaceAppTabService.listBySpaceAppBid(apmSpaceTabQo.getSpaceAppBid());
        Map<String, ApmSpaceAppTab> apmSpaceAppTabMap = new HashMap<>(16);
        List<ApmObjectRelationAppVo> otResults = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(apmSpaceAppTabs)) {
            for (ApmSpaceAppTab apmSpaceAppTab : apmSpaceAppTabs) {
                if (StringUtils.isNotEmpty(apmSpaceAppTab.getTargetSpaceAppBid())) {
                    apmSpaceAppTabMap.put(apmSpaceAppTab.getTargetSpaceAppBid() + apmSpaceAppTab.getRelationModelCode(), apmSpaceAppTab);
                } else {
                    ApmObjectRelationAppVo apmObjectRelationAppVo = new ApmObjectRelationAppVo();
                    if (apmSpaceAppTab.getEnableFlag()) {
                        apmObjectRelationAppVo.setChecked(true);
                    }

                    apmObjectRelationAppVo.setName(apmSpaceAppTab.getName());
                    apmObjectRelationAppVo.setCode(apmSpaceAppTab.getCode());
                    apmObjectRelationAppVo.setTabName(apmSpaceAppTab.getTabName());
                    apmObjectRelationAppVo.setShowViewModels(apmSpaceAppTab.getShowViewModels());
                    apmObjectRelationAppVo.setMultiTreeContent(apmSpaceAppTab.getMultiTreeContent());
                    apmObjectRelationAppVo.setConfigContent(apmSpaceAppTab.getConfigContent());
                    apmObjectRelationAppVo.setShowConditionContent(apmSpaceAppTab.getShowConditionContent());
                    apmObjectRelationAppVo.setMultiAppTreeContent(apmSpaceAppTab.getMultiAppTreeContent());
                    apmObjectRelationAppVo.setSort(apmSpaceAppTab.getSort());
                    apmObjectRelationAppVo.setSpaceAppTabBid(apmSpaceAppTab.getBid());
                    apmObjectRelationAppVo.setSource(apmSpaceAppTab.getSource());
                    apmObjectRelationAppVo.setIsRefresh(apmSpaceAppTab.getIsRefresh());
                    otResults.add(apmObjectRelationAppVo);
                }
            }
        }
        for (ApmObjectRelationAppVo apmObjectRelationAppVo : results) {
            String key = apmObjectRelationAppVo.getSpaceAppBid() + apmObjectRelationAppVo.getModelCode();
            if (apmSpaceAppTabMap.containsKey(key)) {
                ApmSpaceAppTab apmSpaceAppTab = apmSpaceAppTabMap.get(key);
                if (Boolean.TRUE.equals(apmSpaceAppTab.getEnableFlag())) {
                    apmObjectRelationAppVo.setChecked(true);
                }
                if (StringUtils.isNotEmpty(apmSpaceAppTab.getTabName())) {
                    apmObjectRelationAppVo.setTabName(apmSpaceAppTab.getTabName());
                }
                apmObjectRelationAppVo.setTabBid(apmSpaceAppTab.getBid());
                apmObjectRelationAppVo.setSort(apmSpaceAppTab.getSort());
                apmObjectRelationAppVo.setConfigContent(apmSpaceAppTab.getConfigContent());
                apmObjectRelationAppVo.setShowConditionContent(apmSpaceAppTab.getShowConditionContent());
                apmObjectRelationAppVo.setMultiTreeContent(apmSpaceAppTab.getMultiTreeContent());
                apmObjectRelationAppVo.setViewModelCode(apmSpaceAppTab.getViewModelCode());
                apmObjectRelationAppVo.setShowViewModels(apmSpaceAppTab.getShowViewModels());
                apmObjectRelationAppVo.setMultiAppTreeContent(apmSpaceAppTab.getMultiAppTreeContent());
                apmObjectRelationAppVo.setSpaceAppTabBid(apmSpaceAppTab.getBid());
                apmObjectRelationAppVo.setSource(apmSpaceAppTab.getSource());
                apmObjectRelationAppVo.setIsRefresh(apmSpaceAppTab.getIsRefresh());
                apmObjectRelationAppVo.setRelationActionPermissionList(apmSpaceAppTab.getRelationActionPermissionList());
                apmObjectRelationAppVo.setHideTab((byte) (isMatch(apmSpaceTabQo, apmSpaceAppTab.getShowConditionContent()) ? 0 : 1));
            }
        }
        if (StrUtil.isNotBlank(apmSpaceTabQo.getInstanceBid())) {
            ApmSphere sphere = apmSphereService.getByBid(app.getSphereBid());
            List<String> userRoleBids = getUserRoleBids(sphere);
            MObject instanceData = objectModelCrudI.getByBid(app.getModelCode(), apmSpaceTabQo.getInstanceBid());
            results.forEach(
                    vo -> checkPermission(vo, apmSpaceAppTabMap, userRoleBids, instanceData)
            );
        }
        List<ApmObjectRelationAppVo> collect = results.stream().filter(e -> e.getHideTab() == 0).collect(Collectors.toList());
        collect.addAll(otResults);
        for (ApmObjectRelationAppVo apmObjectRelationAppVo : collect) {
            if (apmObjectRelationAppVo.getSort() == null) {
                apmObjectRelationAppVo.setSort(999);
            }
            if (apmObjectRelationAppVo.getInverseQuery() == null) {
                apmObjectRelationAppVo.setInverseQuery(false);
            }
        }
        //根据sort排序
        List<ApmObjectRelationAppVo> resultSort = collect.stream().sorted(Comparator.comparing(ApmObjectRelationAppVo::getSort)).collect(Collectors.toList());
//        filterRepeatTargetModelCode(resultSort, app.getModelCode());

        //反向查询数据，空间应用bid改为上层
        resultSort.parallelStream().forEach(vo -> {
            if (Boolean.TRUE.equals(vo.getInverseQuery())) {
                List<ApmSpaceApp> spaceAppList = apmSpaceAppService.listSpaceAppBySpaceBidAndModelCode(
                        app.getSpaceBid(), vo.getSourceModelCode());
                Optional.ofNullable(spaceAppList).filter(list -> !list.isEmpty()).map(list -> list.get(0))
                        .map(ApmSpaceApp::getBid)
                        .ifPresent(vo::setSpaceAppBid);
            }
        });
        return resultSort;
    }

    private List<String> getUserRoleBids(ApmSphere sphere) {
        List<ApmRole> userRoles = apmRoleDomainService.listRoleByJobNumAndSphereBid(SsoHelper.getJobNumber(), sphere.getPbid());
        return userRoles.stream().map(this::roleToRoleBids).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void checkPermission(ApmObjectRelationAppVo apmObjectRelationAppVo,
                                 Map<String, ApmSpaceAppTab> apmSpaceAppTabMap,
                                 List<String> userRoleBids, MObject instanceData) {
        if (apmSpaceAppTabMap.containsKey(apmObjectRelationAppVo.getSpaceAppBid() + apmObjectRelationAppVo.getModelCode())) {
            ApmSpaceAppTab apmSpaceAppTab = apmSpaceAppTabMap.get(apmObjectRelationAppVo.getSpaceAppBid() + apmObjectRelationAppVo.getModelCode());
            Boolean hasPermission = checkTabPermission(apmSpaceAppTab, userRoleBids, instanceData);
            if (!BooleanUtil.isTrue(hasPermission)) {
                apmObjectRelationAppVo.setType("");
            }
        }
    }

    private String roleToRoleBids(ApmRole apmRole) {
        if (Objects.equals(apmRole.getCode(), InnerRoleEnum.ALL.getCode())) {
            return InnerRoleEnum.ALL.getCode();
        }
        return apmRole.getBid();
    }

    public Boolean checkTabPermission(ApmSpaceAppTab apmSpaceAppTab, List<String> userRoleBids, MObject instanceData) {
        ArrayList<Boolean> checkResultList = Lists.<Boolean>newArrayList();
        Optional.ofNullable(apmSpaceAppTab.getRelationActionPermissionList())
                .ifPresent(permissionList -> {
                    if (permissionList.isEmpty()) {
                        return;
                    }
                    permissionList.forEach(permission -> {
                        AtomicBoolean hasPermission = new AtomicBoolean(true);
                        checkRoleBids(permission.getRoleBids(), userRoleBids, hasPermission);
                        if (hasPermission.get()) {
                            checkFieldConditionParams(permission.getFieldConditionParams(), instanceData, hasPermission);
                        }
                        checkResultList.add(hasPermission.get());
                    });
                });
        if (checkResultList.isEmpty()) {
            return true;
        }
        return checkResultList.stream().anyMatch(Boolean.TRUE::equals);
    }

    private void checkRoleBids(List<String> permissionRoleBids, List<String> userRoleBids, AtomicBoolean hasPermission) {
        if (CollUtil.isNotEmpty(permissionRoleBids)) {
            if (permissionRoleBids.contains(InnerRoleEnum.ALL.getCode())) {
                return;
            }
            retainUserRoles(permissionRoleBids, userRoleBids, hasPermission);
        }
    }

    private void retainUserRoles(List<String> permissionRoleBids, List<String> userRoleBids, AtomicBoolean hasPermission) {
        permissionRoleBids.retainAll(userRoleBids);
        if (permissionRoleBids.isEmpty()) {
            hasPermission.set(false);
        }
    }

    private void checkFieldConditionParams(List<FieldConditionParam> fieldConditionParams, MObject instanceData,
                                           AtomicBoolean hasPermission) {
        if (CollUtil.isNotEmpty(fieldConditionParams)) {
            List<SimpleExpression> expressionList = fieldConditionParams.stream()
                    .map(param -> transferToExpression(instanceData, param))
                    .collect(Collectors.toList());
            hasPermission.set(SimpleExpression.evaluateExpressions(expressionList, false));
        }
    }

    private SimpleExpression transferToExpression(MObject instanceData, FieldConditionParam param) {
        Object fieldVal = param.getFieldVal();
        fieldVal = beforeTransfer(fieldVal);
        return SimpleExpression.of(
                instanceData.get(param.getFieldName()),
                param.getCondition(),
                fieldVal
        );
    }

    @SuppressWarnings("unchecked")
    private Object beforeTransfer(Object fieldVal) {
        // 处理当前登录人
        if (fieldVal instanceof String && ((String) fieldVal).contains(DefaultParamEnum.LOGIN_USER.getCode())) {
            fieldVal = ((String) fieldVal).replace(DefaultParamEnum.LOGIN_USER.getCode(), SsoHelper.getJobNumber());
        } else if (
                fieldVal instanceof Collection<?> &&
                        ((Collection<?>) fieldVal).contains(DefaultParamEnum.LOGIN_USER.getCode())) {
            ((Collection<String>) fieldVal).remove(DefaultParamEnum.LOGIN_USER.getCode());
            ((Collection<String>) fieldVal).add(SsoHelper.getJobNumber());
        }
        return fieldVal;
    }

    private boolean isMatch(ApmSpaceTabQo apmSpaceTabQo, List<ModelFilterQo> modelMixQo) {
        if (apmSpaceTabQo.getInstanceBid() != null
                && !apmSpaceTabQo.getInstanceBid().isEmpty() && CollectionUtils.isNotEmpty(modelMixQo)) {
            List<ModelFilterQo> querys = JSONArray.parseArray(JSON.toJSONString(modelMixQo), ModelFilterQo.class);
            ModelFilterQo modelFilterQo = new ModelFilterQo();
            modelFilterQo.setProperty(RelationEnum.BID.getColumn());
            modelFilterQo.setCondition(QueryFilterConditionEnum.EQ.getFilter());
            modelFilterQo.setValue(apmSpaceTabQo.getInstanceBid());
            querys.add(modelFilterQo);
            List<MObject> list = objectModelCrudI.list(apmSpaceTabQo.getModelCode(), QueryConveterTool.convert(querys, false));
            return CollectionUtils.isNotEmpty(list);
        }
        return true;
    }

    /**
     * 过滤重复的targetModelCode数据
     *
     * @param resultSort
     */
    private void filterRepeatTargetModelCode(List<ApmObjectRelationAppVo> resultSort, String sourceModelCode) {
        if (CollectionUtils.isNotEmpty(resultSort)) {
            Map<String, Integer> targetCountMap = new HashMap<>(16);
            for (ApmObjectRelationAppVo apmObjectRelationAppVo : resultSort) {
                if (StringUtils.isNotEmpty(apmObjectRelationAppVo.getTargetModelCode())) {
                    Integer count = targetCountMap.get(apmObjectRelationAppVo.getTargetModelCode());
                    if (count == null) {
                        count = 0;
                    }
                    count++;
                    targetCountMap.put(apmObjectRelationAppVo.getTargetModelCode(), count);
                }
            }
            for (int i = resultSort.size() - 1; i >= 0; i--) {
                ApmObjectRelationAppVo apmObjectRelationAppVo = resultSort.get(i);
                if (StringUtils.isNotEmpty(apmObjectRelationAppVo.getTargetModelCode()) && !sourceModelCode.equals(apmObjectRelationAppVo.getSourceModelCode())) {
                    int count = targetCountMap.get(apmObjectRelationAppVo.getTargetModelCode());
                    if (count > 1) {
                        resultSort.remove(i);
                        count--;
                        targetCountMap.put(apmObjectRelationAppVo.getTargetModelCode(), count);
                    }
                }
            }
        }
    }

    private void relationAppTabAppend(CfgObjectRelationVo relationInfo, List<ApmSpaceApp> appList,
                                      Map<String, Set<String>> relationChainMapTab, String sourceModelCode,
                                      List<ApmObjectRelationAppVo> results
    ) {
        appList.forEach(appInfo -> {
            String targetAppModelCode = appInfo.getModelCode();
            // 不匹配链式源对象 全部添加
            // 匹配链式源对象，再看到是否匹配链式目标对象（不匹配，直接跳出循环）
            if (relationChainMapTab.containsKey(sourceModelCode) &&
                    !relationChainMapTab.get(sourceModelCode).contains(targetAppModelCode)) {
                return;
            }
            ApmObjectRelationAppVo vo = new ApmObjectRelationAppVo();
            BeanUtils.copyProperties(relationInfo, vo);
            vo.setBid(appInfo.getBid());
            vo.setName(appInfo.getName());
            vo.setSpaceBid(appInfo.getSpaceBid());
            vo.setSpaceAppBid(appInfo.getBid());
            vo.setTargetModelCode(appInfo.getModelCode());
            /*if(!appInfo.getModelCode().equals(relationInfo.getTargetModelCode())){
                vo.setShowType(AppViewModelEnum.TABLE_VIEW.getEn());
            }*/
            if (!Objects.equals(appInfo.getModelCode(), relationInfo.getTargetModelCode())) {
                vo.setTabName(appInfo.getName());
            }
            vo.setRelationAttr(relationInfo.getRelationAttr());
            vo.setIsRefresh(false);
            results.add(vo);
        });
    }

    @NotNull
    private static List<AppViewModelDto> getDefaultAppViewModels(String spaceAppBid, Integer enableFlag) {
        List<AppViewModelDto> list = Lists.newArrayList();
        // 解决json字段为空时，update更新不成功的问题
        Map<String, Object> defaultMap = Maps.newHashMap();
        list.add(
                AppViewModelDto.of()
                        .setBid(SnowflakeIdWorker.nextIdStr())
                        .setSpaceAppBid(spaceAppBid)
                        .setCode(AppViewModelEnum.TABLE_VIEW.getCode())
                        .setName(AppViewModelEnum.TABLE_VIEW.getZh())
                        .setConfigContent(defaultMap)
                        .setEnableFlag(enableFlag).setSort(600)
        );
        list.add(
                AppViewModelDto.of()
                        .setBid(SnowflakeIdWorker.nextIdStr())
                        .setSpaceAppBid(spaceAppBid)
                        .setCode(AppViewModelEnum.TREE_VIEW.getCode())
                        .setName(AppViewModelEnum.TREE_VIEW.getZh())
                        .setConfigContent(defaultMap)
                        .setEnableFlag(enableFlag).setSort(500)
        );
        list.add(
                AppViewModelDto.of()
                        .setBid(SnowflakeIdWorker.nextIdStr())
                        .setSpaceAppBid(spaceAppBid)
                        .setCode(AppViewModelEnum.KANBAN_VIEW.getCode())
                        .setName(AppViewModelEnum.KANBAN_VIEW.getZh())
                        .setConfigContent(defaultMap)
                        .setEnableFlag(enableFlag).setSort(400)
        );
        list.add(
                AppViewModelDto.of()
                        .setBid(SnowflakeIdWorker.nextIdStr())
                        .setSpaceAppBid(spaceAppBid)
                        .setCode(AppViewModelEnum.GANTT_VIEW.getCode())
                        .setName(AppViewModelEnum.GANTT_VIEW.getZh())
                        .setConfigContent(defaultMap)
                        .setEnableFlag(enableFlag).setSort(300)
        );
        list.add(
                AppViewModelDto.of()
                        .setBid(SnowflakeIdWorker.nextIdStr())
                        .setSpaceAppBid(spaceAppBid)
                        .setCode(AppViewModelEnum.MULTI_TREE_VIEW.getCode())
                        .setName(AppViewModelEnum.MULTI_TREE_VIEW.getZh())
                        .setConfigContent(defaultMap)
                        .setEnableFlag(enableFlag).setSort(200)
        );
        list.add(
                AppViewModelDto.of()
                        .setBid(SnowflakeIdWorker.nextIdStr())
                        .setSpaceAppBid(spaceAppBid)
                        .setCode(AppViewModelEnum.OVER_VIEW.getCode())
                        .setName(AppViewModelEnum.OVER_VIEW.getZh())
                        .setConfigContent(defaultMap)
                        .setEnableFlag(enableFlag).setSort(100)
        );
        list.add(
                AppViewModelDto.of()
                        .setBid(SnowflakeIdWorker.nextIdStr())
                        .setSpaceAppBid(spaceAppBid)
                        .setCode(AppViewModelEnum.SWIMLANE_DIAGRAM.getCode())
                        .setName(AppViewModelEnum.SWIMLANE_DIAGRAM.getZh())
                        .setConfigContent(defaultMap)
                        .setEnableFlag(enableFlag).setSort(50)
        );
        list.add(
                AppViewModelDto.of()
                        .setBid(SnowflakeIdWorker.nextIdStr())
                        .setSpaceAppBid(spaceAppBid)
                        .setCode(AppViewModelEnum.ADVANCED_FILTER.getCode())
                        .setName(AppViewModelEnum.ADVANCED_FILTER.getZh())
                        .setConfigContent(defaultMap)
                        .setEnableFlag(enableFlag).setSort(30)
        );
        return list;
    }

    @Override
    public CfgAttributeVo getAttributeConfigByCode(String code) {
        return cfgAttributeFeignClient.getByCode(code).getCheckExceptionData();
    }

    @Override
    public List<CfgObjectRelationVo> listRelationByModelCode(String spaceBid, String spaceAppBid) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        String modelCode = apmSpaceApp.getModelCode();
        return cfgObjectRelationFeignClient.listRelationBySTModelCode(modelCode, modelCode).getCheckExceptionData();
    }

    @Override
    public List<CfgLifeCycleTemplateKeyPathNodeVo> listPhaseLifeCycleState(String spaceAppBid, String instanceBid) {
        //获取当前应用的生命周期配置
        ObjectModelLifeCycleVO objectModelLifeCycleVO = objectFeignClient.findObjectLifecycleByModelCode(
                apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid)
        ).getCheckExceptionData();
        if (objectModelLifeCycleVO == null || StringUtil.isAnyBlank(objectModelLifeCycleVO.getLcPhaseTemplBid(), objectModelLifeCycleVO.getLcTemplVersion())) {
            return new ArrayList<>();
        }
        //查询阶段生命周期模板关键节点（应当优先取当前实例的阶段生命周期模板）
        TemplateDto templateDto = new TemplateDto();
        templateDto.setTemplateBid(objectModelLifeCycleVO.getLcPhaseTemplBid());
        templateDto.setVersion(objectModelLifeCycleVO.getLcPhaseTemplVersion());
        List<CfgLifeCycleTemplateNodeVo> checkExceptionData = lifeCycleFeignClient.getKeyPathNodes(templateDto).getCheckExceptionData();
        if (CollectionUtils.isEmpty(checkExceptionData)) {
            return new ArrayList<>();
        }
        List<CfgLifeCycleTemplateKeyPathNodeVo> cfgLifeCycleTemplateKeyPathNodeVos = getCfgLifeCycleTemplateKeyPathNodeVos(checkExceptionData);
        //查询实例的当前生命周期状态
        MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(spaceAppBid, instanceBid, false);
        if (mSpaceAppData == null || StringUtils.isEmpty(mSpaceAppData.getLifeCycleCode())) {
            return cfgLifeCycleTemplateKeyPathNodeVos;
        }
        String lifeCycleCode = mSpaceAppData.getLifeCycleCode();
        //获取生命周期状态与阶段生命周期状态的映射
        Map<String, ApmStateVO> phaseStateMap = new HashMap<>(16);
        ApmLifeCycleStateVO apmLifeCycleStateVO = apmFlowApplicationService.getLifeCycleState(spaceAppBid);
        if (apmLifeCycleStateVO == null || CollectionUtils.isEmpty(apmLifeCycleStateVO.getApmStateVOList())) {
            phaseStateMap = getPhaseStateMap(objectModelLifeCycleVO.getLcTemplBid(), objectModelLifeCycleVO.getLcTemplVersion(), objectModelLifeCycleVO.getModelCode(), lifeCycleCode);
        } else {
            phaseStateMap = apmLifeCycleStateVO.getPhaseStateMap();
        }
        // 查询当前生命周期状态对应的阶段状态
        ApmStateVO phaseStateVo = phaseStateMap.get(lifeCycleCode);
        if (phaseStateVo == null) {
            return cfgLifeCycleTemplateKeyPathNodeVos;
        }
        //查询阶段状态所属的关键节点
        templateDto.setCurrentLifeCycleCode(phaseStateVo.getLifeCycleCode());
        CfgLifeCycleTemplateNodeVo keyNodeVo = lifeCycleFeignClient.getKeyPathNode(templateDto).getCheckExceptionData();
        if (keyNodeVo == null || cfgLifeCycleTemplateKeyPathNodeVos.stream().noneMatch(v -> v.getLifeCycleCode().equals(keyNodeVo.getLifeCycleCode()))) {
            return cfgLifeCycleTemplateKeyPathNodeVos;
        }
        for (CfgLifeCycleTemplateKeyPathNodeVo cfgLifeCycleTemplateKeyPathNodeVo : cfgLifeCycleTemplateKeyPathNodeVos) {
            if (cfgLifeCycleTemplateKeyPathNodeVo.getLifeCycleCode().equals(keyNodeVo.getLifeCycleCode())) {
                cfgLifeCycleTemplateKeyPathNodeVo.setNodeState(1);
                if (!phaseStateVo.getLifeCycleCode().equals(cfgLifeCycleTemplateKeyPathNodeVo.getLifeCycleCode())) {
                    cfgLifeCycleTemplateKeyPathNodeVo.setActualNode(phaseStateVo);
                }
                break;
            } else {
                cfgLifeCycleTemplateKeyPathNodeVo.setNodeState(2);
            }
        }
        return cfgLifeCycleTemplateKeyPathNodeVos;
    }

    @Override
    public CfgViewVo getView(ViewQueryParam param) {
        return viewFeignClient.getView(param).getCheckExceptionData();
    }

    @Override
    public CfgViewVo saveOrUpdateView(CfgViewDto cfgViewDto) {
        return viewFeignClient.saveOrUpdateView(cfgViewDto).getCheckExceptionData();
    }

    @Override
    public List<CfgViewVo> listView(String belongBid) {
        return viewFeignClient.listView(belongBid).getCheckExceptionData();
    }

    @Override
    public MultiViewMetaListVo getViewMetaList(String spaceBid, String spaceAppBid, String currentSpaceBid, String viewModelType) {

        // 如果是多维表格视图，需要合并多个层级的MetaList
        if (AppViewModelEnum.MULTI_TREE_VIEW.getCode().equals(viewModelType)
                || AppViewModelEnum.IR_View.getCode().equals(viewModelType)) {
            List<CfgViewVo> hierarchyViews = getMultiTreeCfgViewList(spaceBid, spaceAppBid, currentSpaceBid);
            MultiViewMetaListVo viewMetaList = MultiViewMetaListConverter.INSTANCE.viewListToMetaVo(hierarchyViews,
                    viewModelType);
            if (viewMetaList != null) {
                return viewMetaList;
            }
        }

        // 获取视图信息
        ViewQueryParam viewQueryParam = ViewQueryParam.of().setViewBelongBid(spaceAppBid).setViewType(ViewEnums.FILTER.getCode());
        List<CfgViewVo> views = viewFeignClient.listTypeOrDefaultView(viewQueryParam).getCheckExceptionData();
        Assert.notEmpty(views, "筛选视图信息为空");
        return MultiViewMetaListConverter.INSTANCE.viewToMultiMetaVo(views.get(0), viewModelType);
    }

    @Override
    public List<ViewMetaListVo> listViewMetaList(String spaceBid, String spaceAppBid, String currentSpaceBid, String viewModelType) {
        // 如果是多维表格视图，需要合并多个层级的MetaList
        if (AppViewModelEnum.MULTI_TREE_VIEW.getCode().equals(viewModelType)
                || AppViewModelEnum.IR_View.getCode().equals(viewModelType)) {
            List<CfgViewVo> hierarchyViews = getMultiTreeCfgViewList(spaceBid, spaceAppBid, currentSpaceBid);
            List<ViewMetaListVo> viewMetaList = MultiViewMetaListConverter.INSTANCE.listViewToMetaVo(hierarchyViews,
                    viewModelType);
            if (viewMetaList != null) {
                return viewMetaList;
            }
        }

        // 获取视图信息
        ViewQueryParam viewQueryParam = ViewQueryParam.of().setViewBelongBid(spaceAppBid).setViewType(ViewEnums.FILTER.getCode());
        List<CfgViewVo> views = viewFeignClient.listTypeOrDefaultView(viewQueryParam).getCheckExceptionData();
        Assert.notEmpty(views, "筛选视图信息为空");
        ViewMetaListVo viewMetaListVo = MultiViewMetaListConverter.INSTANCE.viewToMetaVo(views.get(0), viewModelType);
        return Collections.singletonList(viewMetaListVo);
    }


    /**
     * 获取多对象树视图配置列表
     *
     * @param spaceBid        空间bid
     * @param spaceAppBid     应用bid
     * @param currentSpaceBid 当前空间bid
     * @return 视图配置列表
     */
    private List<CfgViewVo> getMultiTreeCfgViewList(String spaceBid, String spaceAppBid, String currentSpaceBid) {
        List<String> modelCodeList = getMultiTreeViewModelCodeList(spaceAppBid);
        List<String> spaceAppBidList = getSameSpaceMultiModelAppBid(spaceBid, spaceAppBid, currentSpaceBid, modelCodeList);
        //通过应用获取所有层的配置
        ViewQueryParam viewQueryParam = ViewQueryParam.of().setViewBelongBids(spaceAppBidList).setViewType(ViewEnums.FILTER.getCode());
        return viewFeignClient.listTypeOrDefaultView(viewQueryParam).getCheckExceptionData();
    }

    /**
     * 获取相同空间下多模型应用bid
     *
     * @param spaceBid        空间bid
     * @param spaceAppBid     应用bid
     * @param currentSpaceBid 当前空间bid
     * @param modelCodeList   模型编码列表
     * @return 应用bid列表
     */
    @NotNull
    private List<String> getSameSpaceMultiModelAppBid(String spaceBid, String spaceAppBid, String currentSpaceBid, List<String> modelCodeList) {
        //查询模型在对应空间的Bid
        List<String> spaceAppBidList = new ArrayList<>();
        spaceAppBidList.add(spaceAppBid);
        if (modelCodeList != null && !modelCodeList.isEmpty()) {
            String querySpaceBid = StringUtils.isNotBlank(currentSpaceBid) ? currentSpaceBid : spaceBid;
            spaceAppBidList.addAll(apmSpaceAppService.getSpaceBidAndModelCodesBids(querySpaceBid, modelCodeList));
        }
        return spaceAppBidList;
    }

    /**
     * 获取多维表格视图模式下所有模型编码列表
     *
     * @param spaceAppBid 空间应用bid
     * @return 模型编码列表
     */
    private List<String> getMultiTreeViewModelCodeList(String spaceAppBid) {
        //获取配置下的所有模型代码
        return apmSpaceAppViewModelService.getObj(apmSpaceAppViewModelService.lambdaQuery()
                        .select(ApmSpaceAppViewModelPo::getConfigContent)
                        .getWrapper()
                        .eq(ApmSpaceAppViewModelPo::getSpaceAppBid, spaceAppBid)
                        .eq(ApmSpaceAppViewModelPo::getCode, AppViewModelEnum.MULTI_TREE_VIEW.getCode())
                        .eq(ApmSpaceAppViewModelPo::getDeleteFlag, 0),
                obj -> Optional.ofNullable(obj).map(String::valueOf)
                        .map(str -> JSON.parseObject(str, MultiAppConfig.class))
                        .map(MultiAppConfig::getMultiAppTreeConfig).map(MultiTreeConfigVo::getTargetModelCode)
                        .map(MultiTreeUtils::getInstanceFlatModelCodes).orElse(null)
        );
    }


    private static List<CfgLifeCycleTemplateKeyPathNodeVo> getCfgLifeCycleTemplateKeyPathNodeVos(List<CfgLifeCycleTemplateNodeVo> checkExceptionData) {
        List<CfgLifeCycleTemplateKeyPathNodeVo> cfgLifeCycleTemplateKeyPathNodeVos = new ArrayList<>();
        for (CfgLifeCycleTemplateNodeVo checkExceptionDatum : checkExceptionData) {
            CfgLifeCycleTemplateKeyPathNodeVo cfgLifeCycleTemplateKeyPathNodeVo = new CfgLifeCycleTemplateKeyPathNodeVo();
            cfgLifeCycleTemplateKeyPathNodeVo.setId(checkExceptionDatum.getId());
            cfgLifeCycleTemplateKeyPathNodeVo.setBid(checkExceptionDatum.getBid());
            cfgLifeCycleTemplateKeyPathNodeVo.setLifeCycleCode(checkExceptionDatum.getLifeCycleCode());
            cfgLifeCycleTemplateKeyPathNodeVo.setName(checkExceptionDatum.getName());
            cfgLifeCycleTemplateKeyPathNodeVo.setNodeState(0);
            cfgLifeCycleTemplateKeyPathNodeVos.add(cfgLifeCycleTemplateKeyPathNodeVo);
        }
        return cfgLifeCycleTemplateKeyPathNodeVos;
    }
}
