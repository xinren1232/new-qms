package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.transcend.plm.configcenter.object.infrastructure.po.DomainObjectConfigPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.DomainObjectConfigMapper;
import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigFindParam;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * DomainObjectConfigRepository
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/30 11:37
 */
@Repository
public class DomainObjectConfigRepository {

    @Resource
    private DomainObjectConfigMapper domainObjectConfigMapper;

    public Boolean bulkInsert(List<DomainObjectConfigPo> configList) {
        return domainObjectConfigMapper.bulkInsert(configList) > 0;
    }

    public Boolean deleteByDomainBid(String domainBid) {
        return domainObjectConfigMapper.deleteByDomainBid(domainBid) > 0;
    }

    public Boolean deleteByBid(String bid) {
        return domainObjectConfigMapper.deleteByBid(bid) > 0;
    }

    public List<DomainObjectConfigPo> findByDomainBid(String domainBid) {
        return domainObjectConfigMapper.findByDomainBid(domainBid);
    }

    public List<DomainObjectConfigPo> find(DomainObjectConfigFindParam domainObjectConfigFindParam) {
        return domainObjectConfigMapper.find(domainObjectConfigFindParam);
    }

}
