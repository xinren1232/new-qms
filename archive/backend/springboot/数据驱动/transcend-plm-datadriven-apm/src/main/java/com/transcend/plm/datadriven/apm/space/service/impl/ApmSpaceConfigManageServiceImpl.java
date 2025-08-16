package com.transcend.plm.datadriven.apm.space.service.impl;

import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgViewFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.apm.mapstruct.ApmSpaceAppConverter;
import com.transcend.plm.datadriven.apm.space.converter.ApmSpaceAppViewModelConverter;
import com.transcend.plm.datadriven.apm.space.converter.ApmSpaceViewTreeConverter;
import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceObjectVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceViewTreeVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpace;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppViewModelService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceConfigManageService;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
public class ApmSpaceConfigManageServiceImpl implements IApmSpaceConfigManageService {

    @Resource
    private ApmSpaceService apmSpaceService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ApmSpaceAppViewModelService apmSpaceAppViewModelService;

    @Resource
    private CfgObjectFeignClient cfgObjectFeignClient;

    @Resource
    private CfgViewFeignClient cfgViewFeignClient;

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

        }
        return null;
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
        // 如果没有配置，则返回默认配置全部的配置，但是只能是未启用的
        return ApmSpaceAppViewModelConverter.INSTANCE.pos2vos(resultViewModels);
    }

    @Override
    public List<AppViewModelDto> addApp(String spaceBid, ApmSpaceAppDto apmSpaceAppDto) {
        return null;
    }

    @Override
    public List<AppViewModelDto> appList(String spaceBid) {
        return null;
    }

    @Override
    public Boolean appChangeEnableFlag(String bid, Integer enableFlag) {
        return null;
    }

    @Override
    public List<ApmSpaceObjectVo> listChildrenByModelCode(String spaceBid, String modelCode) {
        List<ApmSpaceObjectVo> result = Lists.newArrayList();
        List<CfgObjectVo> cfgObjectVoList = cfgObjectFeignClient.listChildrenByModelCode(modelCode).getCheckExceptionData();
        if (CollectionUtils.isEmpty(cfgObjectVoList)) {
            return result;
        }
        List<String> modelCodes = cfgObjectVoList.stream().map(CfgObjectVo::getModelCode).collect(Collectors.toList());
        List<ApmSpaceAppVo> apmSpaceAppVos = apmSpaceAppService.listSpaceAppVoBySpaceBidAndModelCodes(spaceBid, modelCodes);
        if (CollectionUtils.isEmpty(apmSpaceAppVos)) {
            return result;
        }
        Map<String, String> modelCodeWithAppBidMap = apmSpaceAppVos.stream().collect(Collectors.toMap(ApmSpaceAppVo::getModelCode, ApmSpaceAppVo::getBid, (k1, k2) -> k1));
        for (CfgObjectVo cfgObjectVo : cfgObjectVoList) {
            ApmSpaceObjectVo apmSpaceObjectVo = ApmSpaceAppConverter.INSTANCE.cfgObjectVo2ApmSpaceObjectVo(cfgObjectVo);
            apmSpaceObjectVo.setSpaceAppBid(modelCodeWithAppBidMap.get(cfgObjectVo.getModelCode()));
            result.add(apmSpaceObjectVo);
        }
        return result;
    }

    @Override
    public List<ApmSpaceViewTreeVo> sameModelViewTree(String viewBid) {
        Assert.notNull(viewBid, "视图ID不能为空");
        //1、查询视图信息
        CfgViewVo view = cfgViewFeignClient.getByBid(viewBid).getCheckExceptionData();
        Assert.notNull(view, "视图信息无效");

        //2、查询视图所属的应用信息
        ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(view.getBelongBid());

        //3、查询模型所有的应用信息
        List<ApmSpaceApp> spaceAppList = apmSpaceAppService.list(apmSpaceAppService.lambdaQuery().getWrapper()
                .eq(ApmSpaceApp::getModelCode, spaceApp.getModelCode())
                .eq(ApmSpaceApp::getDeleteFlag, 0));

        //4、查询应用对应的视图信息
        List<String> spaceAppBidList = spaceAppList.stream().map(ApmSpaceApp::getBid).collect(Collectors.toList());
        List<CfgViewVo> viewList = cfgViewFeignClient.listView(spaceAppBidList).getCheckExceptionData();
        viewList = viewList.stream().filter(v -> !viewBid.equals(v.getBid())).collect(Collectors.toList());

        //5、组装对象
        List<String> spaceBidList = spaceAppList.stream().map(ApmSpaceApp::getSpaceBid).collect(Collectors.toList());
        List<ApmSpace> spaceList = apmSpaceService.list(apmSpaceService.lambdaQuery().getWrapper()
                .in(ApmSpace::getBid, spaceBidList).eq(ApmSpace::getDeleteFlag, 0));

        return ApmSpaceViewTreeConverter.INSTANCE.toVoList(spaceList, spaceAppList, viewList);
    }
}
