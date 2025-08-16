package com.transcend.plm.configcenter.object.controller;

import com.transcend.plm.configcenter.object.application.service.IDomainObjectConfigAppService;
import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigAddParam;
import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigFindParam;
import com.transcend.plm.configcenter.api.model.object.vo.DomainObjectConfigVO;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * DomainObjectConfigController
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/30 11:23
 */
@RestController
public class DomainObjectConfigController {

    @Resource
    private IDomainObjectConfigAppService domainObjectConfigAppService;


    public Boolean bulkInsertDomainObjectConfig(List<DomainObjectConfigAddParam> params) {
        return domainObjectConfigAppService.bulkInsert(params);
    }


    public Boolean deleteDomainObjectConfigByDomainBid(String domainBid) {
        return domainObjectConfigAppService.deleteByDomainBid(domainBid);
    }


    public Boolean deleteDomainObjectConfigByBid(String bid) {
        return domainObjectConfigAppService.deleteByBid(bid);
    }


    public List<DomainObjectConfigVO> findDomainObjectConfigByDomainBid(String domainBid) {
        return domainObjectConfigAppService.findByDomainBid(domainBid);
    }


    public List<DomainObjectConfigVO> find(DomainObjectConfigFindParam domainObjectConfigFindParam) {
        return domainObjectConfigAppService.find(domainObjectConfigFindParam);
    }

}
