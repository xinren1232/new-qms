package com.transcend.plm.configcenter.dictionary.infrastructure.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.mapper.CfgDictionaryItemMapper;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryItemPo;
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
public class CfgDictioanryItemServiceImpl extends ServiceImpl<CfgDictionaryItemMapper, CfgDictionaryItemPo> implements CfgDictionaryItemService {


    @Override
    public List<CfgDictionaryItemPo> listByDictionaryBid(String dictionaryBid) {
        return null;
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_dictionary_item", fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgDictionaryItemPo>lambdaQuery().eq(CfgDictionaryItemPo::getBid, bid));
    }

}
