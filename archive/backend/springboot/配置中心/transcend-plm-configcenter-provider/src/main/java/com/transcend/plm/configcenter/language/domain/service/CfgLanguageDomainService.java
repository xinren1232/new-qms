package com.transcend.plm.configcenter.language.domain.service;

import com.transcend.plm.configcenter.common.constant.ConfigNameConstant;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.language.infrastructure.repository.CfgLanguageRepository;
import com.transcend.plm.configcenter.language.infrastructure.repository.CfgVersionService;
import com.transcend.plm.configcenter.language.infrastructure.repository.po.CfgVersionPo;
import com.transcend.plm.configcenter.language.pojo.dto.CfgLanguageDto;
import com.transcend.plm.configcenter.language.pojo.qo.CfgLanguageQo;
import com.transcend.plm.configcenter.language.pojo.vo.CfgLanguageVo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:38
 **/
@Service
public class CfgLanguageDomainService {
    @Resource
    private CfgLanguageRepository repository;

    @Resource
    private CfgVersionService cfgVersionService;

    @Resource
    private TransactionTemplate transactionTemplate;

    public CfgLanguageVo saveOrUpdate(CfgLanguageDto cfgLanguageDto) {
        cfgLanguageDto.setUpdatedTime(LocalDateTime.now());
        return StringUtil.isBlank(cfgLanguageDto.getBid()) ? this.save(cfgLanguageDto) : this.update(cfgLanguageDto);
    }

    public CfgLanguageVo save(CfgLanguageDto cfgLanguageDto) {
        Assert.notNull(cfgLanguageDto, "Language is null");
        return transactionTemplate.execute(transactionStatus -> {
            cfgVersionService.save(CfgVersionPo.builder().name(ConfigNameConstant.LANGUAGE).version(ConfigNameConstant.INIT_VERSION).build());
            return repository.save(cfgLanguageDto);
        });

    }

    public CfgLanguageVo update(CfgLanguageDto cfgLanguageDto) {
        Assert.notNull(cfgLanguageDto, "Language is null");
        Assert.hasText(cfgLanguageDto.getBid(),"Language bid is blank");
        return transactionTemplate.execute(transactionStatus -> {
            cfgVersionService.increment(ConfigNameConstant.LANGUAGE);
            return repository.update(cfgLanguageDto);
        });
    }

    public CfgLanguageVo getByBid(String bid) {
        return repository.getByBid(bid);
    }

    public PagedResult<CfgLanguageVo> page(BaseRequest<CfgLanguageQo> pageQo) {
        return repository.page(pageQo);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CfgLanguageVo> bulkAdd(List<CfgLanguageDto> cfgLanguageDtos) {
        cfgVersionService.increment(ConfigNameConstant.LANGUAGE);
        return repository.bulkAdd(cfgLanguageDtos);
    }

    public boolean logicalDeleteByBid(String bid) {
        return repository.logicalDeleteByBid(bid);
    }

    public Long getVersion() {
        return cfgVersionService.getVersionByName(ConfigNameConstant.LANGUAGE);
    }
}
