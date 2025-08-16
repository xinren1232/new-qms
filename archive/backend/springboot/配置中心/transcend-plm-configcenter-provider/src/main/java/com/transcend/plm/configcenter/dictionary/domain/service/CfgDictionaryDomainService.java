package com.transcend.plm.configcenter.dictionary.domain.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.CfgDictionaryRepository;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryDto;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryItemDto;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * 对象领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/2/20 10:39
 * @since 1.0
 */
@Service
public class CfgDictionaryDomainService {
    @Resource
    private CfgDictionaryRepository dictionaryRepository;

    public PagedResult<CfgDictionaryVo> page(BaseRequest<CfgDictionaryQo> pageQo) {
        return dictionaryRepository.page(pageQo);

    }

    public CfgDictionaryVo getByBid(String bid) {
        return dictionaryRepository.getByBid(bid);
    }

    public CfgDictionaryVo add(CfgDictionaryDto dto) {
        return dictionaryRepository.add(dto);
    }

    public List<CfgDictionaryVo> addBatch(List<CfgDictionaryDto> dtos){
        return dictionaryRepository.addBatch(dtos);
    }

    public CfgDictionaryVo update(CfgDictionaryDto dto) {
        return dictionaryRepository.update(dto);
    }

    public boolean blukAddDictionaryItem(List<CfgDictionaryItemDto> dictionaryItems) {
        return dictionaryRepository.blukAddDictionaryItem(dictionaryItems);
    }

    public int blukUpdateDictionaryItem(List<CfgDictionaryItemDto> dictionaryItems) {
        return dictionaryRepository.blukUpdateDictionaryItem(dictionaryItems);
    }

    public Boolean logicalDelete(String bid) {
        return dictionaryRepository.logicalDeleteByBid(bid);
    }

    public List<CfgDictionaryVo> listByByCodesAndEnableFlags(@NotNull List<String> codes, List<Integer> enableFlags) {
        return dictionaryRepository.listByByCodesAndEnableFlags(codes, enableFlags);
    }

    public List<CfgDictionaryItemVo> listDictionaryItemByDictionaryBids(Set<String> dictioanryBids) {
        return dictionaryRepository.listDictionaryItemByDictionaryBids(dictioanryBids);
    }
}
