package com.transcend.plm.configcenter.method.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.method.domain.service.CfgEventDomainService;
import com.transcend.plm.configcenter.method.pojo.dto.CfgEventDto;
import com.transcend.plm.configcenter.method.pojo.qo.CfgEventQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgEventVo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:00
 **/
@RestController
@RequestMapping("/manager/cfg/event")
@Api(value = "CfgEvent Controller", tags = "标准事件-控制器")
public class CfgEventController {

    @Resource
    private CfgEventDomainService cfgEventDomainService;

    @PostMapping("/saveOrUpdate")
    @ApiOperation("新增或修改")
    public CfgEventVo saveOrUpdate(@RequestBody CfgEventDto cfgEventDto) {
        return cfgEventDomainService.saveOrUpdate(cfgEventDto);
    }

    @GetMapping("/get/{bid}")
    @ApiOperation("获取详情")
    public CfgEventVo getByBid(@PathVariable String bid) {
        return cfgEventDomainService.getByBid(bid);
    }

    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgEventVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgEventQo> pageQo) {
        return cfgEventDomainService.page(pageQo);
    }

    @PostMapping("/logicalDeleteByBid/{bid}")
    @ApiOperation("删除")
    public Boolean logicalDeleteByBid(@PathVariable String bid) {
        return cfgEventDomainService.logicalDeleteByBid(bid);
    }
}
