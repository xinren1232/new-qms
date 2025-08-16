package com.transcend.plm.configcenter.draft.domain.service;

import com.transcend.plm.configcenter.draft.infrastructure.repository.CfgDraftRepository;
import com.transcend.plm.configcenter.draft.pojo.dto.CfgDraftDto;
import com.transcend.plm.configcenter.draft.pojo.vo.CfgDraftVo;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:38
 **/
@Service
public class CfgDraftDomainService {
    @Resource
    private CfgDraftRepository repository;

    public CfgDraftVo saveOrReplace(@Valid CfgDraftDto cfgDraftDto) {
        // 1.校验不为空的类目以及code TODO
        // 2.删除 类目+业务code
        repository.logicalDeleteByCategoryAndBizCode(cfgDraftDto.getBizCode(), cfgDraftDto.getCategory());
        // 3.添加 新内容
        return this.save(cfgDraftDto);
    }

    public Boolean logicalDeleteByCategoryAndBizCode(@NotNull String category, @NotNull String bizCode) {
        return repository.logicalDeleteByCategoryAndBizCode(category, bizCode);
    }

    public CfgDraftVo save(CfgDraftDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "attribute is null");
        return repository.save(cfgAttributeDto);
    }

    public Boolean logicalDelete(String bid) {
        return repository.logicalDeleteByBid(bid);
    }

    public CfgDraftVo getByCategoryAndBizCode(String category, String draftCode) {
        return repository.getByCategoryAndBizCode(category, draftCode);
    }
}
