package com.transcend.plm.configcenter.table.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.table.infrastructure.repository.mapper.CfgTableMapper;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTablePo;
import com.transcend.plm.configcenter.table.pojo.CfgTableConverter;
import com.transcend.plm.configcenter.api.model.table.qo.CfgTableQo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Service
public class CfgTableServiceImpl extends ServiceImpl<CfgTableMapper, CfgTablePo>
implements CfgTableService {

    /**
     * 根据bid进行更新
     *
     * @param cfgDictionary
     * @return
     */
    @Override
    public CfgTablePo updateByBid(CfgTablePo cfgDictionary) {
        Assert.hasText(cfgDictionary.getBid(),"bid is blank");
        this.update(cfgDictionary,Wrappers.<CfgTablePo>lambdaUpdate()
                .eq(CfgTablePo::getBid, cfgDictionary.getBid())
        );
        return cfgDictionary;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgTablePo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgTablePo>lambdaQuery().eq(CfgTablePo::getBid, bid));
    }

    @Override
    public PagedResult<CfgTableVo> pageByQo(BaseRequest<CfgTableQo> pageQo) {
        final CfgTablePo cfgDictionary = CfgTableConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgTablePo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgTablePo> queryWrapper = Wrappers.<CfgTablePo>lambdaQuery()
                .eq(cfgDictionary.getEnableFlag() != null, CfgTablePo::getEnableFlag, cfgDictionary.getEnableFlag())
                .eq(StringUtil.isNotBlank(cfgDictionary.getCreatedBy()), CfgTablePo::getCreatedBy, cfgDictionary.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgDictionary.getUpdatedBy()), CfgTablePo::getUpdatedBy, cfgDictionary.getUpdatedBy())
                .eq(CfgTablePo::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED);
        IPage<CfgTablePo> iPage = this.page(page,queryWrapper);
        List<CfgTableVo> cfgTableVos = CfgTableConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgTableVos);
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_table",fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgTablePo>lambdaQuery().eq(CfgTablePo::getBid, bid));
    }

}
