package com.transcend.plm.configcenter.object.application.service.impl;

import com.transcend.plm.configcenter.object.application.service.IDomainObjectConfigAppService;
import com.transcend.plm.configcenter.object.domain.aggergate.DomainObjectConfigRoot;
import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigAddParam;
import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigFindParam;
import com.transcend.plm.configcenter.api.model.object.vo.DomainObjectConfigVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DomainObjectAppServiceImpl
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/19 11:12
 */
@Service
public class DomainObjectConfigAppServiceImpl implements IDomainObjectConfigAppService {

    @Override
    public Boolean bulkInsert(List<DomainObjectConfigAddParam> params) {
        return DomainObjectConfigRoot.build().bulkInsert(params);
    }

    @Override
    public Boolean deleteByDomainBid(String domainBid) {
        return DomainObjectConfigRoot.build().deleteByDomainBid(domainBid);
    }

    @Override
    public Boolean deleteByBid(String bid) {
        return DomainObjectConfigRoot.build().deleteByBid(bid);
    }

    @Override
    public List<DomainObjectConfigVO> findByDomainBid(String domainBid) {
        return DomainObjectConfigRoot.build().findByDomainBid(domainBid);
    }

    @Override
    public List<DomainObjectConfigVO> find(DomainObjectConfigFindParam domainObjectConfigFindParam) {
        return DomainObjectConfigRoot.build().find(domainObjectConfigFindParam);
    }

}
