package com.transcend.plm.configcenter.menuapp.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.menuapp.infrastructure.repository.mapper.CfgMenuAppMapper;
import com.transcend.plm.configcenter.menuapp.infrastructure.repository.po.CfgMenuAppPo;
import com.transcend.plm.configcenter.menuapp.pojo.CfgMenuAppConverter;
import com.transcend.plm.configcenter.menuapp.pojo.qo.CfgMenuAppQo;
import com.transcend.plm.configcenter.menuapp.pojo.vo.CfgMenuAppVo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Service
public class CfgMenuAppServiceImpl extends ServiceImpl<CfgMenuAppMapper, CfgMenuAppPo>
implements CfgMenuAppService {

    /**
     * 根据bid进行更新
     *
     * @param cfgMenuAppPo
     * @return
     */
    @Override
    public CfgMenuAppPo updateByBid(CfgMenuAppPo cfgMenuAppPo) {
        this.update(cfgMenuAppPo,Wrappers.<CfgMenuAppPo>lambdaUpdate()
                .eq(CfgMenuAppPo::getBid, cfgMenuAppPo.getBid())
        );
        return cfgMenuAppPo;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgMenuAppPo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgMenuAppPo>lambdaQuery().eq(CfgMenuAppPo::getBid, bid));
    }

    @Override
    public PagedResult<CfgMenuAppVo> pageByCfgAttributeQo(BaseRequest<CfgMenuAppQo> pageQo) {
        CfgMenuAppPo cfgMenuAppPo = CfgMenuAppConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgMenuAppPo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgMenuAppPo> queryWrapper = Wrappers.<CfgMenuAppPo>lambdaQuery()
                .eq(StringUtil.isNotBlank(cfgMenuAppPo.getParentBid()), CfgMenuAppPo::getParentBid, cfgMenuAppPo.getParentBid())
                .eq(StringUtil.isNotBlank(cfgMenuAppPo.getTypeCode()), CfgMenuAppPo::getTypeCode, cfgMenuAppPo.getTypeCode())
                .eq(StringUtil.isNotBlank(cfgMenuAppPo.getDescription()), CfgMenuAppPo::getDescription, cfgMenuAppPo.getDescription())
                .eq(StringUtil.isNotBlank(cfgMenuAppPo.getName()), CfgMenuAppPo::getName, cfgMenuAppPo.getName())
                .eq(StringUtil.isNotBlank(cfgMenuAppPo.getCreatedBy()), CfgMenuAppPo::getCreatedBy, cfgMenuAppPo.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgMenuAppPo.getUpdatedBy()), CfgMenuAppPo::getUpdatedBy, cfgMenuAppPo.getUpdatedBy());
        IPage<CfgMenuAppPo> iPage = this.page(page,queryWrapper);
        List<CfgMenuAppVo> cfgAttributeVos = CfgMenuAppConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgAttributeVos);
    }


    @Override
    @CheckTableDataExist(tableName = "cfg_menu_app",fieldName = "bid",exist = true)
    public Boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgMenuAppPo>lambdaQuery().eq(CfgMenuAppPo::getBid, bid));
    }
}
