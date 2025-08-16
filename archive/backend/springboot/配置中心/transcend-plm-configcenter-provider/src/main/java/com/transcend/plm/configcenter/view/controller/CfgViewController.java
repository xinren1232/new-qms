package com.transcend.plm.configcenter.view.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.common.constant.ViewConst;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.qo.CfgViewQo;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.configcenter.view.application.service.ICfgViewApplicationService;
import com.transcend.plm.configcenter.view.domain.service.CfgViewDomainService;
import com.transcend.plm.configcenter.view.pojo.qo.SyncViewContentQo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@RestController
@Api(value = "CfgView Controller", tags = "视图-控制器")
@RequestMapping(value = "/manager/cfg/view")
public class CfgViewController {
    @Resource
    private ICfgViewApplicationService iCfgViewApplicationService;
    @Resource
    private CfgViewDomainService cfgViewDomainService;

    @ApiOperation("新增属性基本信息")
    @PostMapping("/saveOrUpdate")
    public CfgViewVo saveOrUpdate(@RequestBody CfgViewDto cfgViewDto) {
        return iCfgViewApplicationService.saveOrUpdate(cfgViewDto);
    }

    @ApiOperation("更新视图局部内容")
    @PostMapping("/updatePartialContent/{bid}")
    public Boolean updatePartialContent(@PathVariable("bid") String bid, Map<String, Object> partialContent) {
        return iCfgViewApplicationService.updatePartialContent(bid, partialContent);
    }

    @GetMapping("/get/{bid}")
    @ApiOperation("查看详情")
    public CfgViewVo getByBid(@PathVariable("bid") String bid) {
        return cfgViewDomainService.getByBid(bid);
    }

    @ApiOperation("元类型的视图分页查询")
    @PostMapping("/meta/page")
    public PagedResult<CfgViewVo> metaPage(@ApiParam("分页查询参数") @RequestBody BaseRequest<CfgViewQo> pageQo) {
        pageQo.getParam().setType(ViewConst.TYPE_META);
        return cfgViewDomainService.mataPage(pageQo);
    }

    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgViewVo> page(@ApiParam("分页查询参数") @RequestBody BaseRequest<CfgViewQo> pageQo) {
        return cfgViewDomainService.page(pageQo);
    }

    @ApiOperation("批量新增")
    @PostMapping("/bulkAdd")
    public List<CfgViewVo> bulkAdd(@RequestBody List<CfgViewDto> cfgViewDtos) {
        return cfgViewDomainService.bulkAdd(cfgViewDtos);
    }

    @PostMapping("/logicalDelete/{bid}")
    @ApiOperation("删除")
    public Boolean logicalDeleteByBid(@PathVariable("bid") String bid) {
        return cfgViewDomainService.logicalDeleteByBid(bid);
    }

    @ApiOperation("更改表启用状态 0未启用，1启用，2禁用 bid必填，enableFlag必填")
    @PostMapping("/changeEnableFlag/{bid}/{enableFlag}")
    public Boolean changeEnableFlag(@PathVariable("bid") String bid,
                                    @PathVariable("enableFlag") Integer enableFlag) {
        return cfgViewDomainService.changeEnableFlag(bid, enableFlag);
    }

    @ApiOperation("根据参数查询视图")
    @PostMapping("/getView")
    public CfgViewVo getView(@RequestBody ViewQueryParam param) {
        return iCfgViewApplicationService.getView(param);
    }

    @ApiOperation("同步视图配置")
    @PostMapping("syncViewContent")
    public void syncViewContent(@RequestBody SyncViewContentQo qo) {
        iCfgViewApplicationService.syncViewContent(qo);
    }

    @ApiOperation("获取视图上一次同步的视图配置")
    @GetMapping("{viewBid}/lastSyncConfig")
    public List<String> lastSyncConfig(@PathVariable("viewBid") String viewBid) {
        return iCfgViewApplicationService.lastSyncConfig(viewBid);
    }

}
