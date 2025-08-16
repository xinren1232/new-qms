package com.transcend.plm.configcenter.code.controller;

import com.transcend.plm.configcenter.code.domain.service.CfgCodeRuleDomainService;
import com.transcend.plm.configcenter.code.domain.service.CfgCodeRuleItemDomainService;
import com.transcend.plm.configcenter.code.pojo.dto.CfgCodeRulePoDto;
import com.transcend.plm.configcenter.code.pojo.dto.CfgCodeRuleItemPoDto;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRuleItemPoQo;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRulePoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRuleItemPoVo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRulePoVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@RestController
@Api(value = "CfgCodeRule Controller", tags = "编码规则-控制器")
@RequestMapping(value ="/manager/cfg/code/rule")
public class CfgCodeRuleController {
    @Resource
    private CfgCodeRuleDomainService cfgCodeRuleDomainService;

    @Resource
    private CfgCodeRuleItemDomainService cfgCodeRuleItemDomainService;

    @ApiOperation("规则概述-新增修改")
    @PostMapping("/saveOrUpdate")
    public CfgCodeRulePoVo saveOrUpdate(@RequestBody @Validated CfgCodeRulePoDto cfgCodeRuleDto) {
        return cfgCodeRuleDomainService.saveOrUpdate(cfgCodeRuleDto);
    }

    @GetMapping("/get/{bid}")
    @ApiOperation("规则概述-查看详情")
    public CfgCodeRulePoVo getByBid(@PathVariable("bid") String bid) {
        return cfgCodeRuleDomainService.getByBid(bid);
    }

    @ApiOperation("规则概述-分页查询")
    @PostMapping("/page")
    public PagedResult<CfgCodeRulePoVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgCodeRulePoQo> pageQo) {
        return cfgCodeRuleDomainService.page(pageQo);
    }

    @ApiOperation("规则概述-批量新增")
    @PostMapping("/bulkAdd")
    public List<CfgCodeRulePoVo> bulkAdd(@RequestBody List<CfgCodeRulePoDto> cfgCodeRuleDtos) {
        return cfgCodeRuleDomainService.bulkAdd(cfgCodeRuleDtos);
    }

    @PostMapping("/logicalDelete/{bid}")
    @ApiOperation("规则概述-删除")
    public Boolean logicalDeleteByBid(@PathVariable("bid") String bid) {
        return cfgCodeRuleDomainService.logicalDeleteByBid(bid);
    }


    @ApiOperation("规则条目-新增修改")
    @PostMapping("/item/saveOrUpdate")
    public CfgCodeRuleItemPoVo saveOrUpdateItem(@RequestBody CfgCodeRuleItemPoDto cfgCodeRuleItemDto) {
        return cfgCodeRuleItemDomainService.saveOrUpdate(cfgCodeRuleItemDto);
    }

    @GetMapping("/item/get/{bid}")
    @ApiOperation("规则条目-查看详情")
    public CfgCodeRuleItemPoVo getItemByBid(@PathVariable("bid") String bid) {
        return cfgCodeRuleItemDomainService.getByBid(bid);
    }

    @ApiOperation("规则条目-分页查询")
    @PostMapping("/item/page")
    public PagedResult<CfgCodeRuleItemPoVo> pageItem(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgCodeRuleItemPoQo> pageQo) {
        return cfgCodeRuleItemDomainService.page(pageQo);
    }

    @ApiOperation("规则条目-批量新增")
    @PostMapping("/item/bulkAdd")
    public List<CfgCodeRuleItemPoVo> bulkAddItem(@RequestBody List<CfgCodeRuleItemPoDto> cfgCodeRuleItemDtos) {
        return cfgCodeRuleItemDomainService.bulkAdd(cfgCodeRuleItemDtos);
    }

    @PostMapping("/item/delete/{bid}")
    @ApiOperation("规则条目-删除")
    public Boolean deleteItemByBid(@PathVariable("bid") String bid) {
        return cfgCodeRuleItemDomainService.deleteByBid(bid);
    }
}
