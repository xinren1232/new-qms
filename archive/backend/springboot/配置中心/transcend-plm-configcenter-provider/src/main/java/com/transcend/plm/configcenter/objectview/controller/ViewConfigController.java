package com.transcend.plm.configcenter.objectview.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.objectview.domain.service.ViewConfigService;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.dto.CfgViewConfigDto;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.qo.CfgViewConfigQo;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.vo.CfgViewConfigVo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: yuanhu.huang Date: 2023/2/22 Time: 上午11:58 Describe: 生命周期控制器
 */
@Slf4j
@Api(value = "view Controller", tags = "对象视图-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/objectview/")
public class ViewConfigController {
    @Resource
    private ViewConfigService viewConfigService;

    @ApiOperation("新增或修改")
    @PostMapping("saveOrUpdate")
    public boolean saveOrUpdate(@RequestBody CfgViewConfigDto cfgViewConfigDto) {
        if (cfgViewConfigDto.getId() == null) {
            return viewConfigService.add(cfgViewConfigDto);
        }
        else {
            return viewConfigService.edit(cfgViewConfigDto);
        }
    }

    /*
     * @ApiOperation("编辑")
     * @PostMapping("edit") public boolean edit(@RequestBody CfgViewConfigDto cfgViewConfigDto) { return
     * viewConfigService.edit(cfgViewConfigDto); }
     */

    @ApiOperation("模板分页查询")
    @PostMapping("page")
    public PagedResult<CfgViewConfigVo> templatePage(@RequestBody BaseRequest<CfgViewConfigQo> pageQo) {
        return viewConfigService.page(pageQo);
    }

    @ApiOperation(value = "删除", httpMethod = "GET")
    @GetMapping("delete/{bid}")
    public boolean delete(@PathVariable(name = "bid") String bid) {
        return viewConfigService.delete(bid);
    }

    @ApiOperation(value = "获取单个视图信息", httpMethod = "GET")
    @GetMapping("getOne/{bid}")
    public CfgViewConfigVo getOne(@PathVariable(name = "bid") String bid) {
        return viewConfigService.getOne(bid);
    }

    @ApiOperation(value = "根据对象编码获取视图信息", httpMethod = "GET")
    @GetMapping("findViewByModelCode/{modelCode}")
    public List<CfgViewConfigVo> findViewByModelCode(@PathVariable(name = "modelCode") String modelCode){
        return viewConfigService.findViewByModelCode(modelCode);
    }

    @ApiOperation("设置状态")
    @PostMapping("setEnableFlag")
    public boolean setEnableFlag(@RequestBody CfgViewConfigDto cfgViewConfigDto) {
        return viewConfigService.setEnableFlag(cfgViewConfigDto);
    }
    @ApiOperation("对象测编辑视图信息")
    @PostMapping("editViewInfo")
    public boolean editViewInfo(@RequestBody CfgViewConfigDto cfgViewConfigDto){
       return viewConfigService.editViewInfo(cfgViewConfigDto);
    }

    @ApiOperation("复制")
    @PostMapping("copy")
    public boolean copy(@RequestBody CfgViewConfigDto cfgViewConfigDto) {
        return viewConfigService.copy(cfgViewConfigDto);
    }
}
