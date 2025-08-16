package com.transcend.plm.configcenter.method.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.method.domain.service.CfgMethodDomainService;
import com.transcend.plm.configcenter.method.pojo.dto.CfgMethodDto;
import com.transcend.plm.configcenter.method.pojo.qo.CfgMethodQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgMethodVo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/manager/cfg/method")
@Api(value = "CfgMethod Controller", tags = "标准方法-控制器")
public class CfgMethodController {

    @Resource
    private CfgMethodDomainService cfgMethodDomainService;

    @PostMapping("/saveOrUpdate")
    @ApiOperation("新增或修改")
    public CfgMethodVo saveOrUpdate(@RequestBody @Validated CfgMethodDto cfgMethodDto) {
        return cfgMethodDomainService.saveOrUpdate(cfgMethodDto);
    }

    @GetMapping("/get/{bid}")
    @ApiOperation("获取详情")
    public CfgMethodVo getByBid(@PathVariable String bid) {
        return cfgMethodDomainService.getByBid(bid);
    }

    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgMethodVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgMethodQo> pageQo) {
        return cfgMethodDomainService.page(pageQo);
    }

    @PostMapping("/logicalDeleteByBid/{bid}")
    @ApiOperation("删除")
    public Boolean logicalDeleteByBid(@PathVariable String bid) {
        return cfgMethodDomainService.logicalDeleteByBid(bid);
    }
}
