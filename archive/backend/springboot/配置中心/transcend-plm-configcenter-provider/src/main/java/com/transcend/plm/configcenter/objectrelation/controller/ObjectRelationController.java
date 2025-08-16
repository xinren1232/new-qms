package com.transcend.plm.configcenter.objectrelation.controller;

import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectRelationRuleQo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.objectrelation.domain.service.IObjectRelationService;
import com.transcend.plm.configcenter.api.model.objectrelation.dto.CfgObjectRelationDto;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.CfgObjectRelationQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Author: yuanhu.huang Date: 2023/2/22 Time: 上午11:58 Describe: 生命周期控制器
 *
 * @author yuanhu.huang
 */
@Slf4j
@Api(value = "OjectRelation Controller", tags = "关系管理-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/relation/")
public class ObjectRelationController {

    @Autowired
    private IObjectRelationService objectRelationService;

    @ApiOperation("新增或修改")
    @PostMapping("/saveOrUpdate")
//    @CacheEvict(value = CacheNameConstant.OBJECT_RELATION_TAB_LIST, key = "#cfgObjectRelationDto.modelCode")
    public boolean saveOrUpdate(@RequestBody CfgObjectRelationDto cfgObjectRelationDto) {
        if (StringUtil.isBlank(cfgObjectRelationDto.getBid())) {
            return objectRelationService.add(cfgObjectRelationDto);
        } else {
            return objectRelationService.edit(cfgObjectRelationDto);
        }
    }

    /*
     * @ApiOperation("编辑")
     * @PostMapping("/edit") public boolean edit(@RequestBody CfgObjectRelationDto cfgObjectRelationDto){ return
     * objectRelationService.edit(cfgObjectRelationDto); }
     */

    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgObjectRelationVo> page(@RequestBody BaseRequest<CfgObjectRelationQo> pageQo) {
        return objectRelationService.page(pageQo);
    }

    @ApiOperation(value = "删除", httpMethod = "POST")
    @PostMapping("/logicalDelete/{bid}")
    public boolean logicalDelete(@PathVariable(name = "bid") String bid) {
        return objectRelationService.delete(bid);
    }

    @ApiOperation(value = "设置启用状态", httpMethod = "POST")
    @PostMapping("/changeEnableFlag/{bid}/{enableFlag}")
    public boolean changeEnableFlag(@PathVariable(name = "bid") String bid,
                                    @PathVariable(name = "enableFlag") Integer enableFlag) {
        return objectRelationService.changeEnableFlag(bid, enableFlag);
    }

    @PostMapping("/getRelationRuleRes")
    public String getRelationRuleRes(@RequestBody ObjectRelationRuleQo objectRelationRuleQo) {
        return objectRelationService.getRelationRuleRes(objectRelationRuleQo);
    }

    @ApiOperation(value = "获取关系列表", httpMethod = "GET")
    @GetMapping("/listRelationTab/{modelCode}")
    public List<CfgObjectRelationVo> listRelationTab(@PathVariable(name = "modelCode") String modelCode) {
        return objectRelationService.listRelationTab(modelCode);
    }

}
