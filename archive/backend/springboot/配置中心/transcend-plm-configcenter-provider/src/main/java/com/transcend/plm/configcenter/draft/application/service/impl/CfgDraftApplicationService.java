package com.transcend.plm.configcenter.draft.application.service.impl;

import com.transcend.plm.configcenter.draft.application.service.ICfgDraftApplicationService;
import com.transcend.plm.configcenter.draft.domain.service.CfgDraftDomainService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:36
 **/
@Service
public class CfgDraftApplicationService implements ICfgDraftApplicationService {
    @Resource
    private CfgDraftDomainService cfgDraftDomainService;

}
