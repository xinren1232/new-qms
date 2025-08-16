package com.transcend.plm.configcenter.dictionary.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.mapper.CfgDictionaryMapper;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryPo;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.configcenter.dictionary.converter.CfgDictionaryConverter;
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
public class CfgDictioanryServiceImpl extends ServiceImpl<CfgDictionaryMapper, CfgDictionaryPo>
implements CfgDictionaryService {

    /**
     * 根据bid进行更新
     *
     * @param cfgDictionary
     * @return
     */
    @Override
    public CfgDictionaryPo updateByBid(CfgDictionaryPo cfgDictionary) {
        Assert.hasText(cfgDictionary.getBid(),"bid is blank");
        this.update(cfgDictionary,Wrappers.<CfgDictionaryPo>lambdaUpdate()
                .eq(CfgDictionaryPo::getBid, cfgDictionary.getBid())
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
    public CfgDictionaryPo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgDictionaryPo>lambdaQuery().eq(CfgDictionaryPo::getBid, bid));
    }

    @Override
    public PagedResult<CfgDictionaryVo> pageByQo(BaseRequest<CfgDictionaryQo> pageQo) {
        final CfgDictionaryPo cfgDictionary = CfgDictionaryConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgDictionaryPo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgDictionaryPo> queryWrapper = Wrappers.<CfgDictionaryPo>lambdaQuery()
                .like(StringUtil.isNotBlank(cfgDictionary.getCode()), CfgDictionaryPo::getCode, cfgDictionary.getCode())
                .like(StringUtil.isNotBlank(cfgDictionary.getName()), CfgDictionaryPo::getName, cfgDictionary.getName())
                .like(StringUtil.isNotBlank(cfgDictionary.getDescription()), CfgDictionaryPo::getDescription, cfgDictionary.getDescription())
                .eq(cfgDictionary.getEnableFlag() != null, CfgDictionaryPo::getEnableFlag, cfgDictionary.getEnableFlag())
                .eq(StringUtil.isNotBlank(cfgDictionary.getCreatedBy()), CfgDictionaryPo::getCreatedBy, cfgDictionary.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgDictionary.getUpdatedBy()), CfgDictionaryPo::getUpdatedBy, cfgDictionary.getUpdatedBy())
                .eq(StringUtil.isNotBlank(cfgDictionary.getGroupName()), CfgDictionaryPo::getGroupName, cfgDictionary.getGroupName())
                .eq(StringUtil.isNotBlank(cfgDictionary.getPermissionScope()), CfgDictionaryPo::getPermissionScope, cfgDictionary.getPermissionScope())
                .eq(CfgDictionaryPo::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED);
        IPage<CfgDictionaryPo> iPage = this.page(page,queryWrapper);
        List<CfgDictionaryVo> cfgDictionaryVos = CfgDictionaryConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgDictionaryVos);
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_dictionary",fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgDictionaryPo>lambdaQuery().eq(CfgDictionaryPo::getBid, bid));
    }

}
