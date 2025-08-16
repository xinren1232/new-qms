package com.transcend.plm.configcenter.attribute.controller;

import com.transcend.plm.configcenter.attribute.application.service.ICfgAttributeApplicationService;
import com.transcend.plm.configcenter.attribute.domain.service.CfgAttributeDomainService;
import com.transcend.plm.configcenter.attribute.pojo.dto.CfgAttributeDto;
import com.transcend.plm.configcenter.attribute.pojo.qo.CfgAttributeQo;
import com.transcend.plm.configcenter.attribute.pojo.vo.CfgAttributeVo;
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
@Api(value = "CfgAttribute Controller", tags = "标准属性-控制器")
@RequestMapping(value ="/manager/cfg/attribute/standard")
public class CfgAttributeController {
    @Resource
    private ICfgAttributeApplicationService cfgAttributeApplicationService;
    @Resource
    private CfgAttributeDomainService cfgAttributeDomainService;

    @ApiOperation("新增属性基本信息")
    @PostMapping("/saveOrUpdate")
    public CfgAttributeVo saveOrUpdate(@RequestBody @Validated CfgAttributeDto cfgAttributeDto) {
        return cfgAttributeApplicationService.saveOrUpdate(cfgAttributeDto);
    }

    @GetMapping("/get/{bid}")
    @ApiOperation("查看详情")
    public CfgAttributeVo getByBid(@PathVariable("bid") String bid) {
        return cfgAttributeDomainService.getByBid(bid);
    }

    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgAttributeVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgAttributeQo> pageQo) {
        return cfgAttributeDomainService.page(pageQo);
    }

    @ApiOperation("批量新增")
    @PostMapping("/bulkAdd")
    public List<CfgAttributeVo> bulkAdd(@RequestBody List<CfgAttributeDto> cfgAttributeDtos) {
        return cfgAttributeDomainService.bulkAdd(cfgAttributeDtos);
    }

    @PostMapping("/logicalDelete/{bid}")
    @ApiOperation("删除")
    public Boolean logicalDeleteByBid(@PathVariable("bid") String bid) {
        return cfgAttributeDomainService.logicalDeleteByBid(bid);
    }
}
