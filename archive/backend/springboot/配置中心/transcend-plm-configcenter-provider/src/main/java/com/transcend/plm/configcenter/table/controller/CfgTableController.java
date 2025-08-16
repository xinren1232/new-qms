package com.transcend.plm.configcenter.table.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.table.qo.CfgTableComplexQo;
import com.transcend.plm.configcenter.api.model.table.qo.CfgTableQo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAndCheckVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.object.domain.service.CfgObjectTableDomainService;
import com.transcend.plm.configcenter.table.application.service.ICfgTableApplicationService;
import com.transcend.plm.configcenter.table.domain.service.CfgTableDomainService;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableDto;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.tool.locate.AssertBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: jie.luo1
 * Date: 2023/2/5
 * Time: 上午11:58
 * Describe:  表控制器
 */
@Slf4j
@Api(value = "Table Controller", tags = "表管理-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/table")
public class CfgTableController {


    /**
     * 表编排服务-注入
     */
    @Resource
    private ICfgTableApplicationService iCfgTableApplicationService;

    @Resource
    private CfgTableDomainService cfgTableDomainService;

    @Resource
    private CfgObjectTableDomainService cfgObjectTableDomainService;

    @ApiOperation("新增表基本信息")
    @PostMapping("/add")
    public CfgTableVo add(@RequestBody CfgTableDto dto) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dto).code(ErrorMsgEnum.USER_NAME_NOT_NULL.getCode()).message(ErrorMsgEnum.USER_NAME_NOT_NULL.getDesc());
        return iCfgTableApplicationService.add(dto);
    }

    @ApiOperation("新增或修改表基本信息")
    @PostMapping("/saveOrUpdate")
    public CfgTableVo saveOrUpdate(@RequestBody CfgTableDto dto) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dto).notNull();
        return iCfgTableApplicationService.saveOrUpdate(dto);
    }


    @ApiOperation("更改表启用状态 0未启用，1启用，2禁用 bid必填，enableFlag必填")
    @PostMapping("/changeEnableFlag/{bid}/{enableFlag}")
    public Boolean changeEnableFlag(@PathVariable("bid") String bid,
                                    @PathVariable("enableFlag") Integer enableFlag) {
        return iCfgTableApplicationService.changeEnableFlag(bid, enableFlag);
    }

    @ApiOperation("删除表")
    @PostMapping("/logicalDelete/{bid}")
    public Boolean delete(@PathVariable("bid") String bid) {
        return iCfgTableApplicationService.logicalDelete(bid);
    }

    @ApiOperation("新增或者更新表基本信息+属性信息")
    @PostMapping("/saveOrUpdateTableAndAttribute")
    public CfgTableVo saveOrUpdateTableAndAttribute(@RequestBody CfgTableDto dto) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dto).notNull();
        return iCfgTableApplicationService.saveOrUpdateTableAndAttribute(dto);
    }

    @ApiOperation("校验并返回校验结果-批量新增或者更新表基本信息+属性信息")
    @PostMapping("/checkReturnBlukTableAndAttribute")
    public List<CfgTableAndCheckVo> checkReturnBlukTableAndAttribute(@RequestBody List<CfgTableDto> dtos) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dtos).notNull();
        return iCfgTableApplicationService.checkReturnBlukTableAndAttribute(dtos);
    }


    @ApiOperation("批量新增或者更新表基本信息+属性信息")
    @PostMapping("/blukSaveOrUpdateTableAndAttribute")
    public Boolean blukSaveOrUpdateTableAndAttribute(@RequestBody List<CfgTableDto> dtos) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dtos).notNull();
        return iCfgTableApplicationService.blukSaveOrUpdateTableAndAttribute(dtos);
    }

    @ApiOperation("根据bid查询表基本信息+属性")
    @GetMapping("/getTableAndAttributeByBid/{bid}")
    public CfgTableVo getTableAndAttributeByBid(@PathVariable String bid) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(bid).notNull();
        return iCfgTableApplicationService.getTableAndAttributeByBid(bid);
    }


    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgTableVo> page(@ApiParam("分页查询参数") @RequestBody BaseRequest<CfgTableQo> pageQo) {
        return iCfgTableApplicationService.page(pageQo);
    }

    @ApiOperation("根据bid查询")
    @GetMapping("/get/{bid}")
    public CfgTableVo getByBid(@PathVariable String bid) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(bid).notNull();
        return iCfgTableApplicationService.getByBid(bid);
    }


    @ApiOperation("根据表codes+enableFlag查询表基本信息+属性")
    @PostMapping("/listTableAndAttributeByTableNamesAndEnableFlags")
    public List<CfgTableVo> listTableAndAttributeByTableNamesAndEnableFlags(@RequestBody CfgTableComplexQo qo) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(qo).notNull();
        return iCfgTableApplicationService.listTableAndAttributeByTableNamesAndEnableFlags(qo.getCodes(), qo.getEnableFlags());
    }

    @ApiOperation("根据modelcode进行发布")
    @PostMapping("/publishByModelCode/{modelCode}")
    @CacheEvict(value = CacheNameConstant.OBJECT_TABLE, allEntries = true)
    public Boolean publishByModelCode(@PathVariable String modelCode, @RequestBody List<String> attrBids) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(modelCode).notNull();
        return cfgObjectTableDomainService.publishByModelCode(modelCode, attrBids);
    }


}
