package com.transcend.plm.configcenter.view.infrastructure.repository;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.view.infrastructure.repository.mapper.CfgViewMapper;
import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewPo;
import com.transcend.plm.configcenter.api.model.view.qo.CfgViewQo;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.view.pojo.CfgViewConverter;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Service
public class CfgViewServiceImpl extends ServiceImpl<CfgViewMapper, CfgViewPo>
implements CfgViewService {

    /**
     * 根据bid进行更新
     *
     * @param cfgViewPo
     * @return
     */
    @Override
    public CfgViewPo updateByBid(CfgViewPo cfgViewPo) {
        Assert.hasText(cfgViewPo.getBid(),"bid is blank");
        this.update(cfgViewPo,Wrappers.<CfgViewPo>lambdaUpdate()
                .eq(CfgViewPo::getBid, cfgViewPo.getBid())
        );
        return cfgViewPo;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgViewPo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgViewPo>lambdaQuery().eq(CfgViewPo::getBid, bid));
    }

    @Override
    public PagedResult<CfgViewVo> pageByQo(BaseRequest<CfgViewQo> pageQo) {
        CfgViewQo pageQoParam = pageQo.getParam();
        Page<CfgViewPo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgViewPo> queryWrapper = queryCondition(pageQoParam);

        IPage<CfgViewPo> iPage = this.page(page,queryWrapper);
        List<CfgViewVo> cfgViewVos = CfgViewConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage, cfgViewVos);
    }

    private LambdaQueryWrapper<CfgViewPo> queryCondition(CfgViewQo pageQoParam) {
        LambdaQueryWrapper<CfgViewPo> lambdaQueryWrapper = Wrappers.<CfgViewPo>lambdaQuery();
        return queryConditionLambdaQueryWrapper(lambdaQueryWrapper, pageQoParam);
    }

    private LambdaQueryWrapper<CfgViewPo> queryConditionNoContent(CfgViewQo pageQoParam) {
        LambdaQueryWrapper<CfgViewPo> lambdaQueryWrapper = Wrappers.<CfgViewPo>lambdaQuery()
                .select(CfgViewPo.class, tableFieldInfo -> !"content".equals(tableFieldInfo.getProperty()));
                return queryConditionLambdaQueryWrapper(lambdaQueryWrapper, pageQoParam);
    }
    private LambdaQueryWrapper<CfgViewPo> queryConditionLambdaQueryWrapper(LambdaQueryWrapper<CfgViewPo> lambdaQueryWrapper, CfgViewQo pageQoParam){
        return lambdaQueryWrapper.eq(StringUtil.isNotBlank(pageQoParam.getType()), CfgViewPo::getType, pageQoParam.getType())
                .like(StringUtil.isNotBlank(pageQoParam.getName()), CfgViewPo::getName, pageQoParam.getName())
                .eq(StringUtil.isNotBlank(pageQoParam.getModelCode()), CfgViewPo::getModelCode, pageQoParam.getModelCode())
                .eq(StringUtil.isNotBlank(pageQoParam.getBelongBid()), CfgViewPo::getBelongBid, pageQoParam.getBelongBid())
                .eq(null != pageQoParam.getEnableFlag(), CfgViewPo::getEnableFlag, pageQoParam.getEnableFlag())
                .eq(StringUtil.isNotBlank(pageQoParam.getCreatedBy()), CfgViewPo::getCreatedBy, pageQoParam.getCreatedBy())
                .eq(StringUtil.isNotBlank(pageQoParam.getUpdatedBy()), CfgViewPo::getUpdatedBy, pageQoParam.getUpdatedBy())
                .eq(StringUtil.isNotBlank(pageQoParam.getType()), CfgViewPo::getType, pageQoParam.getType())
                .in(CollectionUtil.isNotEmpty(pageQoParam.getTypes()), CfgViewPo::getType, pageQoParam.getTypes());
    }



    @Override
    @CheckTableDataExist(tableName = "cfg_view", fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgViewPo>lambdaQuery().eq(CfgViewPo::getBid, bid));
    }

    @Override
    public List<CfgViewPo> listByQo(CfgViewQo pageQo) {
        return this.list(queryCondition(pageQo));
    }

    @Override
    public List<CfgViewPo> listByQoNoContent(CfgViewQo qo) {
        return this.list(queryConditionNoContent(qo));
    }

    @Override
    public List<CfgViewPo> listByConditions(String belongBid, List<String> viewScopes, List<String> viewTypes) {
        return list(
                Wrappers.lambdaQuery(CfgViewPo.class)
                        .eq(CfgViewPo::getBelongBid, belongBid)
                        .in(CfgViewPo::getViewScope, viewScopes)
                        .in(CfgViewPo::getViewType, viewTypes)
        );
    }

    @Override
    public List<CfgViewPo> listByBelongBid(String belongBid) {
        return list(
                Wrappers.lambdaQuery(CfgViewPo.class)
                        .eq(CfgViewPo::getBelongBid, belongBid)
        );
    }
}
