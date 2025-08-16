package com.transcend.plm.configcenter.object.controller;

import com.transcend.plm.configcenter.object.application.service.ICfgObjectViewAppService;
import com.transcend.plm.configcenter.object.domain.service.CfgObjectViewRuleDomainService;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectViewRuleEditParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleAndViewInfoVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * ObjectViewController
 *
 * @author jie.Luo1
 * @version: 1.0
 * @date 2023/06/12 10:26
 */
@Slf4j
@Api(value = "CfgObjectView Controller", tags = "对象管理-视图规则-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/object/viewRule")
public class CfgObjectViewRuleController {

    @Resource
    private CfgObjectViewRuleDomainService cfgObjectViewRuleDomainService;

    @ApiOperation(value = "根据modelCode查询配置规则+视图合集")
    @GetMapping("/listAndViewInfoByModelCode/{modelCode}")
    public List<CfgObjectViewRuleAndViewInfoVo> listAndViewInfoByModelCode(@PathVariable String modelCode) {
        return cfgObjectViewRuleDomainService.listAndViewInfoByModelCode(modelCode);
    }


    @ApiOperation(value = "根据modelCode查询配置规则")
    @GetMapping("/listByModelCode/{modelCode}")
    public List<CfgObjectViewRuleVo> listByModelCode(@PathVariable String modelCode) {
        return cfgObjectViewRuleDomainService.listByModelCode(modelCode);
    }

    @ApiOperation(value = "根据保存或更新规则")
    @PostMapping("/saveOrUpdate")
    public CfgObjectViewRuleVo saveOrUpdate(@RequestBody CfgObjectViewRuleEditParam cfgObjectViewRuleEditParam) {
        return cfgObjectViewRuleDomainService.saveOrUpdate(cfgObjectViewRuleEditParam);
    }

    @ApiOperation(value = "根据视图bid查询规则")
    @GetMapping("/getByViewBid/{viewBid}")
    public CfgObjectViewRuleVo getByViewBid(@PathVariable String viewBid) {
        return cfgObjectViewRuleDomainService.getByViewBid(viewBid);
    }

    @PostMapping("/logicalDelete/{bid}")
    @ApiOperation("删除")
    public Boolean logicalDeleteByBid(@PathVariable("bid") String bid) {
        return cfgObjectViewRuleDomainService.logicalDeleteByBid(bid);
    }


}
