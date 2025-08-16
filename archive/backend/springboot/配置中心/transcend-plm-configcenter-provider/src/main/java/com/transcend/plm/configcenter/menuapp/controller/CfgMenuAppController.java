package com.transcend.plm.configcenter.menuapp.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.menuapp.application.service.ICfgMenuAppApplicationService;
import com.transcend.plm.configcenter.menuapp.domain.service.CfgMenuAppDomainService;
import com.transcend.plm.configcenter.menuapp.pojo.dto.CfgMenuAppDto;
import com.transcend.plm.configcenter.menuapp.pojo.qo.CfgMenuAppQo;
import com.transcend.plm.configcenter.menuapp.pojo.vo.CfgMenuAppVo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@RestController
@Api(value = "CfgMenuApp Controller", tags = "菜单应用管理-控制器")
@RequestMapping(value ="/manager/cfg/menu-app")
public class CfgMenuAppController {
    @Resource
    private ICfgMenuAppApplicationService iCfgMenuAppApplicationService;
    @Resource
    private CfgMenuAppDomainService cfgMenuAppDomainService;

    @ApiOperation("保存并修改基本信息")
    @PostMapping("/saveOrUpdate")
    public CfgMenuAppVo saveOrUpdate(@RequestBody CfgMenuAppDto cfgAttributeDto) {
        return iCfgMenuAppApplicationService.saveOrUpdate(cfgAttributeDto);
    }

    @ApiOperation("更新基本信息")
    @PostMapping("/update")
    public CfgMenuAppVo update(@RequestBody CfgMenuAppDto cfgAttributeDto) {
        return cfgMenuAppDomainService.update(cfgAttributeDto);
    }
    @ApiOperation("获取基本信息")
    @GetMapping("/get/{bid}")
    public CfgMenuAppVo getByBid(@PathVariable("bid") String bid) {
        return cfgMenuAppDomainService.getByBid(bid);
    }

    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgMenuAppVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgMenuAppQo> pageQo) {
        return cfgMenuAppDomainService.page(pageQo);
    }

    @ApiOperation("查树")
    @GetMapping("/tree")
    public List<CfgMenuAppVo> tree() {
        return cfgMenuAppDomainService.tree();
    }

    @ApiOperation("更改菜单应用启用状态 0未启用，1启用，2禁用 bid必填，enableFlag必填")
    @PostMapping("/changeEnableFlag/{bid}/{enableFlag}")
    public Boolean changeEnableFlag(@PathVariable("bid") String bid,
                                    @PathVariable("enableFlag") Integer enableFlag) {
        return cfgMenuAppDomainService.changeEnableFlag(bid, enableFlag);
    }

    @ApiOperation("删除")
    @PostMapping("logicalDelete/{bid}")
    public Boolean logicalDelete(@PathVariable("bid") String bid) {
        return cfgMenuAppDomainService.logicalDelete(bid);
    }
}
