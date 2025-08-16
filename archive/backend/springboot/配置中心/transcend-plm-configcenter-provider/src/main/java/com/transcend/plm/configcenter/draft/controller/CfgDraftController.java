package com.transcend.plm.configcenter.draft.controller;

import com.transcend.plm.configcenter.draft.application.service.ICfgDraftApplicationService;
import com.transcend.plm.configcenter.draft.domain.service.CfgDraftDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@RestController
@Api(value = "CfgDraft Controller", tags = "草稿-控制器")
@RequestMapping(value ="/manager/cfg/draft")
public class CfgDraftController {
    @Resource
    private ICfgDraftApplicationService iCfgDraftApplicationService;
    @Resource
    private CfgDraftDomainService cfgDraftDomainService;

    @ApiOperation("删除字典")
    @PostMapping("logicalDelete/{bid}")
    public Boolean delete(@PathVariable("bid") String bid) {
        return cfgDraftDomainService.logicalDelete(bid);
    }
}
