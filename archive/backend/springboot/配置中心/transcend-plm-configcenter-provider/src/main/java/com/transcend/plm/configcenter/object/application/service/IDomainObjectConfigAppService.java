package com.transcend.plm.configcenter.object.application.service;

import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigAddParam;
import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigFindParam;
import com.transcend.plm.configcenter.api.model.object.vo.DomainObjectConfigVO;

import java.util.List;

/**
 * IDomainObjectConfigAppService
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/30 11:24
 */
public interface IDomainObjectConfigAppService {

    Boolean bulkInsert(List<DomainObjectConfigAddParam> params);

    Boolean deleteByDomainBid(String domainBid);

    Boolean deleteByBid(String bid);

    List<DomainObjectConfigVO> findByDomainBid(String domainBid);

    List<DomainObjectConfigVO> find(DomainObjectConfigFindParam domainObjectConfigFindParam);

}
