package com.transcend.plm.configcenter.code.domain.service;

import com.transcend.plm.configcenter.code.infrastructure.repository.CfgCodeRuleItemRepository;
import com.transcend.plm.configcenter.code.pojo.dto.CfgCodeRuleItemPoDto;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRuleItemPoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRuleItemPoVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
@Service
public class CfgCodeRuleItemDomainService {
    @Resource
    private CfgCodeRuleItemRepository repository;

    public CfgCodeRuleItemPoVo getByBid(String bid) {
        return repository.getByBid(bid);
    }

    public PagedResult<CfgCodeRuleItemPoVo> page(BaseRequest<CfgCodeRuleItemPoQo> pageQo) {
        return repository.page(pageQo);
    }

    public List<CfgCodeRuleItemPoVo> bulkAdd(List<CfgCodeRuleItemPoDto> cfgCodeRuleItemDtos) {
        return repository.bulkAdd(cfgCodeRuleItemDtos);
    }

    public boolean deleteByBid(String bid) {
        return repository.deleteByBid(bid);
    }

    public CfgCodeRuleItemPoVo saveOrUpdate(CfgCodeRuleItemPoDto cfgCodeRuleItemDto) {
        return StringUtil.isBlank(cfgCodeRuleItemDto.getBid()) ? repository.save(cfgCodeRuleItemDto) : repository.update(cfgCodeRuleItemDto);
    }
}
