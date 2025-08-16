package com.transcend.plm.configcenter.draft.infrastructure.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.plm.configcenter.draft.infrastructure.repository.mapper.CfgDraftMapper;
import com.transcend.plm.configcenter.draft.infrastructure.repository.po.CfgDraftPo;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Service
public class CfgDraftServiceImpl extends ServiceImpl<CfgDraftMapper, CfgDraftPo>
        implements CfgDraftService {

    @Override
    @CheckTableDataExist(tableName = "cfg_role",fieldName = "bid",exist = true)
    public Boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.remove(Wrappers.<CfgDraftPo>lambdaQuery().eq(CfgDraftPo::getBid, bid));
    }

    @Override
    public Boolean logicalDeleteByCategoryAndBizCode(String category, String bizCode) {
        return this.remove(Wrappers.<CfgDraftPo>lambdaQuery()
                .eq(CfgDraftPo::getCategory, category)
                .eq(CfgDraftPo::getBizCode, bizCode)
        );
    }
}
