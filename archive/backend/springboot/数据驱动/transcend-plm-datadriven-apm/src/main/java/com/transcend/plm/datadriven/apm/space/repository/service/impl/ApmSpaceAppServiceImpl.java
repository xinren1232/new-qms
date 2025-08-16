package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.mapstruct.ApmSpaceAppConverter;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceAppMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
public class ApmSpaceAppServiceImpl extends ServiceImpl<ApmSpaceAppMapper, ApmSpaceApp>
        implements ApmSpaceAppService {

    @Resource
    private ApmSpaceAppMapper apmSpaceAppMapper;
    @Resource
    private ApmSpaceService apmSpaceService;

    @Value("#{'${transcend.plm.apm.batchImport.spaceAppBids:1111}'.split(',')}")
    private List<String> batchImportSpaceAppBids;

    @Value("#{'${transcend.plm.apm.batchImport.modelCodes:1111}'.split(',')}")
    private List<String> batchImportModelCodes;

    @Override
    @Cacheable(value = CacheNameConstant.SPACE_APP, key = "#bid")
    public ApmSpaceApp getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<ApmSpaceApp>lambdaQuery().eq(ApmSpaceApp::getBid, bid));
    }

    @Override
    public List<ApmSpaceAppVo> listSpaceInfo(List<String> spaceAppBids) {
        return apmSpaceAppMapper.listSpaceInfo(spaceAppBids);
    }

    @Override
    public List<ApmSpaceApp> listSpaceAppBySpaceBidAndModelCode(String spaceBid, String modelCode) {
        return this.list(
                Wrappers.<ApmSpaceApp>lambdaQuery()
                        .eq(ApmSpaceApp::getSpaceBid, spaceBid)
                        .likeRight(ApmSpaceApp::getModelCode, modelCode)
                        .eq(ApmSpaceApp::getDeleteFlag, 0)
        );
    }

    @Override
    public ApmSpaceApp getApmSpaceAppBySpaceBidAndModelCode(String spaceBid, String modelCode) {
        return this.getOne(Wrappers.<ApmSpaceApp>lambdaQuery().eq(ApmSpaceApp::getSpaceBid, spaceBid)
                .eq(ApmSpaceApp::getModelCode, modelCode).eq(ApmSpaceApp::getDeleteFlag, 0));
    }

    @Override
    public List<ApmSpaceApp> listBySpaceBidsAndModelCode(List<String> spaceBids, String modelCode) {
        return this.list(Wrappers.<ApmSpaceApp>lambdaQuery().in(ApmSpaceApp::getSpaceBid, spaceBids)
                .eq(ApmSpaceApp::getModelCode, modelCode));
    }

    @Override
    public List<ApmSpaceApp> listSpaceAppWithOrder(String spaceBid, Boolean isAsc) {
        return this.list(Wrappers.<ApmSpaceApp>lambdaQuery()
                .eq(ApmSpaceApp::getSpaceBid, spaceBid)
                .eq(ApmSpaceApp::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED));
    }

    @Override
    public Map<String, List<ApmSpaceAppVo>> listSpaceAppVoBySpaceBids(Set<String> spaceBids) {
        List<ApmSpaceApp> apmSpaceApps = list(Wrappers.<ApmSpaceApp>lambdaQuery()
                .eq(ApmSpaceApp::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                .in(ApmSpaceApp::getSpaceBid, spaceBids)
                .orderByAsc(ApmSpaceApp::getSort)
        );
        List<ApmSpaceAppVo> apmSpaceAppVos = CollectionUtils.copyList(apmSpaceApps,ApmSpaceAppVo.class);
        for(ApmSpaceAppVo apmSpaceAppVo: apmSpaceAppVos){
            if(batchImportSpaceAppBids.contains(apmSpaceAppVo.getBid()) || batchImportModelCodes.contains(apmSpaceAppVo.getModelCode())){
                apmSpaceAppVo.setBatchImport(true);
            }
        }
        return apmSpaceAppVos.stream().collect(Collectors.groupingBy(ApmSpaceAppVo::getSpaceBid, Collectors.toList()));
    }

    @Override
    public List<ApmSpaceAppVo> listSpaceAppVoByBids(List<String> bids) {
        List<ApmSpaceApp> apmSpaceApps = list(Wrappers.<ApmSpaceApp>lambdaQuery()
                .eq(ApmSpaceApp::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                .in(ApmSpaceApp::getBid, bids)
                .orderByAsc(ApmSpaceApp::getSort)
        );
        List<ApmSpaceAppVo> apmSpaceAppVos = CollectionUtils.copyList(apmSpaceApps,ApmSpaceAppVo.class);
        return apmSpaceAppVos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSort(List<ApmSpaceApp> apmSpaceApps) {
        this.baseMapper.updateSort(apmSpaceApps);
    }

    @Override
    public ApmSpaceApp getBySpaceBidAndModelCode(String spaceBid, String modelCode) {
        return this.getOne(Wrappers.<ApmSpaceApp>lambdaQuery()
                .eq(ApmSpaceApp::getSpaceBid, spaceBid)
                .eq(ApmSpaceApp::getModelCode, modelCode)
                .eq(ApmSpaceApp::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED));
    }

    @Override
    public List<ApmSpaceAppVo> listSpaceAppVoBySpaceBidAndModelCodes(String spaceBid, List<String> modelCodes) {
        List<ApmSpaceApp> apmSpaceAppList = this.list(Wrappers.<ApmSpaceApp>lambdaQuery()
                .eq(ApmSpaceApp::getSpaceBid, spaceBid)
                .in(ApmSpaceApp::getModelCode, modelCodes));
        return ApmSpaceAppConverter.INSTANCE.entityList2VoList(apmSpaceAppList);
    }

    @Override
    public Set<String> getAllSpaceAppBids(String modelCode) {
        //查询有效的空间
        List<String> spaceBids = apmSpaceService.getSpaceBids();
        List<ApmSpaceApp> apmSpaceAppList = this.list(Wrappers.<ApmSpaceApp>lambdaQuery()
                .eq(ApmSpaceApp::getDeleteFlag, false).in(ApmSpaceApp::getSpaceBid, spaceBids).eq(ApmSpaceApp::getModelCode, modelCode));
        return apmSpaceAppList.stream().map(ApmSpaceApp::getBid).collect(Collectors.toSet());
    }

    @Override
    public List<String> getSpaceBidAndModelCodesBids(String spaceBid, List<String> modelCodeList) {
        return this.listObjs(this.lambdaQuery()
                        .select(ApmSpaceApp::getBid)
                        .getWrapper()
                        .eq(ApmSpaceApp::getSpaceBid, spaceBid)
                        .in(ApmSpaceApp::getModelCode, modelCodeList)
                        .eq(ApmSpaceApp::getDeleteFlag, 0),
                String::valueOf);
    }
}




