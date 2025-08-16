package com.transcend.plm.configcenter.dictionary.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryItemPo;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgDictionaryItemService extends IService<CfgDictionaryItemPo> {

    /**
     * 根据dictionaryBid获取字典条目列表
     * @param dictionaryBid
     * @return
     */
    List<CfgDictionaryItemPo> listByDictionaryBid(String dictionaryBid);

    boolean logicalDeleteByBid(String bid);
}
