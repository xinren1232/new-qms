package com.transcend.plm.configcenter.lifecycle.infrastructure.repository;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.CfgLifeCycleTemplateVersionConverter;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateNodeQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateObjRelQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateNodeVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateTransitionLineVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVersionVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateNodePo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateObjRelPo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateTransitionLinePo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateVersionPo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service.CfgLifeCycleTemplateNodeService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service.CfgLifeCycleTemplateObjRelService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service.CfgLifeCycleTemplateService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service.CfgLifeCycleTemplateTransitionLineService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service.CfgLifeCycleTemplateVersionService;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.dto.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class CfgLifeCycleTemplateRepository {
    @Resource
    private CfgLifeCycleTemplateService cfgLifeCycleTemplateService;

    @Resource
    private CfgLifeCycleTemplateNodeService cfgLifeCycleTemplateNodeService;

    @Resource
    private CfgLifeCycleTemplateVersionService cfgLifeCycleTemplateVersionService;

    @Resource
    private CfgLifeCycleTemplateTransitionLineService cfgLifeCycleTemplateTransitionLineService;

    @Resource
    private CfgLifeCycleTemplateObjRelService cfgLifeCycleTemplateObjRelService;

    public PagedResult<CfgLifeCycleTemplateVo> pageByCfgLifeCycleTemplateQo(
            BaseRequest<CfgLifeCycleTemplateQo> pageQo) {
        return cfgLifeCycleTemplateService.pageByCfgLifeCycleTemplateQo(pageQo);
    }

    public CfgLifeCycleTemplateVo saveCfgLifeCycleTemplate(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        return cfgLifeCycleTemplateService.saveCfgLifeCycleTemplate(cfgLifeCycleTemplateDto);
    }

    public void saveCfgLifeCycleTemplateNodes(List<CfgLifeCycleTemplateNodePo> dtos) {
        if (!CollectionUtils.isEmpty(dtos)) {
            cfgLifeCycleTemplateNodeService.saveBatch(dtos);
        }
    }

    public CfgLifeCycleTemplateObjRelPo getCfgLifeCycleTemplateObjRel(CfgLifeCycleTemplateObjRelQo cfgLifeCycleTemplateObjRelQo){
        return cfgLifeCycleTemplateObjRelService.getCfgLifeCycleTemplateObjRel(cfgLifeCycleTemplateObjRelQo);
    }

    public boolean setEnable(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto){
        return cfgLifeCycleTemplateService.setEnable(cfgLifeCycleTemplateDto);
    }

    public CfgLifeCycleTemplateVo updateDescription(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        return cfgLifeCycleTemplateService.updateDescription(cfgLifeCycleTemplateDto);
    }

    public void saveCfgLifeCycleTemplateObjRel(List<CfgLifeCycleTemplateObjRelPo> cfgLifeCycleTemplateObjRels) {
        if (!CollectionUtils.isEmpty(cfgLifeCycleTemplateObjRels)) {
            cfgLifeCycleTemplateObjRelService.saveBatch(cfgLifeCycleTemplateObjRels);
        }
    }

    public void saveCfgLifeCycleTemplateTransitionLine(
            List<CfgLifeCycleTemplateTransitionLinePo> cfgLifeCycleTemplateTransitionLineDtos) {
        if (!CollectionUtils.isEmpty(cfgLifeCycleTemplateTransitionLineDtos)) {
            cfgLifeCycleTemplateTransitionLineService
                    .saveBatch(cfgLifeCycleTemplateTransitionLineDtos);
        }
    }

    public void saveCfgLifeCycleTemplateVersion(CfgLifeCycleTemplateVersionPo cfgLifeCycleTemplateVersion) {
        cfgLifeCycleTemplateVersionService.save(cfgLifeCycleTemplateVersion);
    }

    public long countByTemplateBid(String templateBid) {
        return cfgLifeCycleTemplateVersionService.countByTemplateBid(templateBid);
    }

    public boolean setVersion(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        return cfgLifeCycleTemplateService.setVersion(cfgLifeCycleTemplateDto);
    }

    public CfgLifeCycleTemplateVersionPo getCfgLifeCycleTemplateVersion(String templateBid, String version){
        return cfgLifeCycleTemplateVersionService.getCfgLifeCycleTemplateVersion(templateBid,version);
    }



    public List<CfgLifeCycleTemplateVersionVo> getVersions(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        Assert.notNull(cfgLifeCycleTemplateDto,"cfgLifeCycleTemplateDto is null");
        Assert.hasText(cfgLifeCycleTemplateDto.getBid(),"bid is null");
        Assert.hasText(cfgLifeCycleTemplateDto.getCurrentVersion(),"currentVersion is null");
        List<CfgLifeCycleTemplateVersionPo> list = cfgLifeCycleTemplateVersionService.getVersions(cfgLifeCycleTemplateDto);
        List<CfgLifeCycleTemplateVersionVo> resList = CfgLifeCycleTemplateVersionConverter.INSTANCE.pos2vos(list);
        return resList;
    }

    @Transactional
    public boolean deleteTemplate(String bid) {
        cfgLifeCycleTemplateTransitionLineService.deleteByTempBid(bid);
        cfgLifeCycleTemplateNodeService.deleteByTempBid(bid);
        cfgLifeCycleTemplateObjRelService.deleteByTempBid(bid);
        cfgLifeCycleTemplateVersionService.deleteByTempBid(bid);
        return cfgLifeCycleTemplateService.delete(bid);
    }

    public List<CfgLifeCycleTemplateNodeVo> getCfgLifeCycleTemplateNodeVos(TemplateDto templateDto) {
        return cfgLifeCycleTemplateNodeService.getCfgLifeCycleTemplateNodeVos(templateDto);
    }

    public List<CfgLifeCycleTemplateNodePo> getCfgLifeCycleTemplateNode(TemplateDto templateDto){
        return cfgLifeCycleTemplateNodeService.getCfgLifeCycleTemplateNode(templateDto);
    }

    public CfgLifeCycleTemplateNodePo getCfgLifeCycleTemplateNode(CfgLifeCycleTemplateNodeQo cfgLifeCycleTemplateNodeQo){
        return cfgLifeCycleTemplateNodeService.getCfgLifeCycleTemplateNode(cfgLifeCycleTemplateNodeQo);
    }

    public List<CfgLifeCycleTemplateNodePo> getCfgLifeCycleTemplateNodeByBids(List<String> bids,String templateBid,String version){
        return cfgLifeCycleTemplateNodeService.getCfgLifeCycleTemplateNodeByBids(bids,templateBid,version);
    }


    public List<CfgLifeCycleTemplateObjRelPo> getCfgLifeCycleTemplateObjRels(TemplateDto templateDto) {
        return cfgLifeCycleTemplateObjRelService.getCfgLifeCycleTemplateObjRels(templateDto);
    }

    public List<CfgLifeCycleTemplateTransitionLineVo> getCfgLifeCycleTemplateTransitionLineVos(
            TemplateDto templateDto) {
        return cfgLifeCycleTemplateTransitionLineService.getCfgLifeCycleTemplateTransitionLineVos(templateDto);
    }

    public List<CfgLifeCycleTemplateTransitionLinePo> getCfgLifeCycleTemplateTransitionLine(
            TemplateDto templateDto) {
        return cfgLifeCycleTemplateTransitionLineService.getCfgLifeCycleTemplateTransitionLine(templateDto);
    }

    public CfgLifeCycleTemplateVo getByBid(String bid) {
        return cfgLifeCycleTemplateService.getByBid(bid);
    }
}
