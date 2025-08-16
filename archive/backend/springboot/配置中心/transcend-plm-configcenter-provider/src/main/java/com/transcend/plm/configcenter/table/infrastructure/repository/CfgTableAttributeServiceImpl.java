package com.transcend.plm.configcenter.table.infrastructure.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.plm.configcenter.table.infrastructure.repository.mapper.CfgTableAttributeMapper;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTableAttributePo;
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
public class CfgTableAttributeServiceImpl extends ServiceImpl<CfgTableAttributeMapper, CfgTableAttributePo> implements CfgTableAttributeService {


    @Override
    public List<CfgTableAttributePo> listByDictionaryBid(String dictionaryBid) {
        return null;
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_table_attribute", fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgTableAttributePo>lambdaQuery().eq(CfgTableAttributePo::getBid, bid));
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_table_attribute", fieldName = "table_bid",exist = true)
    public boolean logicalDeleteByTableBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgTableAttributePo>lambdaQuery().eq(CfgTableAttributePo::getBid, bid));
    }

}
