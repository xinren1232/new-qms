package com.transcend.plm.configcenter.table.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTableAttributePo;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgTableAttributeService extends IService<CfgTableAttributePo> {

    /**
     * 根据dictionaryBid获取表属性列表
     * @param dictionaryBid
     * @return
     */
    List<CfgTableAttributePo> listByDictionaryBid(String dictionaryBid);

    boolean logicalDeleteByBid(String bid);

    public boolean logicalDeleteByTableBid(String tableBid);
}
