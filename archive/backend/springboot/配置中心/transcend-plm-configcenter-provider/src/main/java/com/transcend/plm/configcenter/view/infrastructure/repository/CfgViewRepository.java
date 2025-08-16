package com.transcend.plm.configcenter.view.infrastructure.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.tabmanage.enums.ViewComponentEnum;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.qo.CfgViewQo;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.configcenter.view.infrastructure.repository.mapper.CfgViewMapper;
import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewPo;
import com.transcend.plm.configcenter.view.pojo.CfgViewConverter;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:45
 **/
@Repository
public class CfgViewRepository {
    @Resource
    private CfgViewService cfgViewService;
    @Resource
    private CfgViewMapper cfgViewMapper;

    public CfgViewVo save(CfgViewDto cfgViewDto) {
        Assert.notNull(cfgViewDto, "attribute is null");
        CfgViewPo cfgViewPo = CfgViewConverter.INSTANCE.dto2po(cfgViewDto);
        cfgViewService.save(cfgViewPo);
        return CfgViewConverter.INSTANCE.po2vo(cfgViewPo);
    }

    public List<CfgViewPo> listByIds(Set<String> ids){
        if(CollectionUtils.isEmpty(ids)){
            return null;
        }
       return cfgViewService.list(Wrappers.<CfgViewPo>lambdaQuery().in(CfgViewPo::getBid, ids));
    }

    public boolean saveBatch(List<CfgViewPo> cfgViewPos){
        return cfgViewService.saveBatch(cfgViewPos);
    }

    public CfgViewVo update(CfgViewDto cfgViewDto) {
        CfgViewPo cfgViewPo = CfgViewConverter.INSTANCE.dto2po(cfgViewDto);
        cfgViewService.updateByBid(cfgViewPo);
        return CfgViewConverter.INSTANCE.po2vo(cfgViewPo);
    }

    public CfgViewVo getByBid(String bid) {
        CfgViewPo cfgViewPo = cfgViewMapper.getByBid(bid);
        return CfgViewConverter.INSTANCE.po2vo(cfgViewPo);
    }

    public PagedResult<CfgViewVo> page(BaseRequest<CfgViewQo> qo) {
        return cfgViewService.pageByQo(qo);
    }

    public List<CfgViewVo> bulkAdd(List<CfgViewDto> cfgViewDtos) {
        List<CfgViewPo> cfgViewPos = CfgViewConverter.INSTANCE.dtos2pos(cfgViewDtos);
        cfgViewService.saveBatch(cfgViewPos);
        return CfgViewConverter.INSTANCE.pos2vos(cfgViewPos);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgViewService.logicalDeleteByBid(bid);
    }

    public List<CfgViewVo> listByModelCode(String modelCode) {
        CfgViewQo qo = new CfgViewQo();
        qo.setModelCode(modelCode);
        List<CfgViewPo> viewPos = cfgViewService.listByQo(qo);
        return CfgViewConverter.INSTANCE.pos2vos(viewPos);
    }

    public List<CfgViewVo> listByQo(CfgViewQo qo) {
        List<CfgViewPo> viewPos = cfgViewService.listByQo(qo);
        return CfgViewConverter.INSTANCE.pos2vos(viewPos);
    }

    /**
     * 查询不要视图内容的，因为试图内容数据大，很多时候不需要视图内容
     * @param qo
     * @return List<CfgViewVo>
     */
    public List<CfgViewVo> listByQoNoContent(CfgViewQo qo) {
        List<CfgViewPo> viewPos = cfgViewService.listByQoNoContent(qo);
        return CfgViewConverter.INSTANCE.pos2vos(viewPos);
    }

    /**
     * 获取仅仅一个modelCode 匹配数据
     * model_code = #{modelCode}
     *         and enable_flag = 1
     *         and delete_flag = 0
     *         order by id desc
     *         limit 1
     * @param modelCode modelCode
     * @return CfgViewVo
     */
    public CfgViewVo getByModelCode(String modelCode) {
        CfgViewPo po = cfgViewMapper.getByModelCode(modelCode);
        return CfgViewConverter.INSTANCE.po2vo(po);
    }

    public CfgViewVo getByModelCodeAndTag(String modelCode, String tag) {
        CfgViewPo po = cfgViewMapper.getByModelCodeAndTag(modelCode, tag);
        return CfgViewConverter.INSTANCE.po2vo(po);
    }

    public List<CfgViewMetaDto> getMetaModelsByBid(String bid) {
        CfgViewPo viewPo = cfgViewMapper.getMetaModelsByBid(bid);
        if (viewPo == null) {
            return Lists.newArrayList();
        }
        return viewPo.getMetaList();
    }

    public int countByBid(String bid) {
        return cfgViewMapper.countByBid(bid);
    }

    public Map<String, List<CfgViewVo>> listByConditions(CfgViewQo qo) {
        // 查询视图时默认拼上default作用域的视图 避免查询不到视图的情况发生
        List<CfgViewPo> viewPoList = cfgViewService.listByConditions(
                qo.getBelongBid(),
                Lists.newArrayList(ViewComponentEnum.DEFAULT_CONSTANT, qo.getViewScope()),
                Lists.newArrayList(ViewComponentEnum.DEFAULT_CONSTANT, qo.getViewType())
        );
        List<CfgViewVo> viewVoList = CfgViewConverter.INSTANCE.pos2vos(viewPoList);
        return viewVoList.stream().collect(Collectors.groupingBy(CfgViewVo::getViewType));
    }

    public List<CfgViewPo> listByBelongBids(Collection<String> belongBids) {
        return cfgViewService.list(
                Wrappers.<CfgViewPo>lambdaQuery()
                .in(CfgViewPo::getBelongBid, belongBids)
        );
    }

    public List<CfgViewVo> listByBelongBid(String belongBid) {
        return CfgViewConverter.INSTANCE.pos2vos(
                cfgViewService.listByBelongBid(belongBid)
        );
    }
}
