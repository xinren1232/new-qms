package com.transcend.plm.configcenter.dictionary.controller;

import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.dictionary.application.service.ICfgDictionaryApplicationService;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryDto;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryAndCheckVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.tool.locate.AssertBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: jie.luo1
 * Date: 2023/2/5
 * Time: 上午11:58
 * Describe:  字典控制器
 */
@Slf4j
@Api(value = "Dictionary Controller", tags = "字典管理-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/dictionary")
public class DictionaryController {


    /**
     * 字典编排服务-注入
     */
    @Resource
    private ICfgDictionaryApplicationService dictionaryApplicationService;

    @ApiOperation("新增字典基本信息")
    @PostMapping("/add")
    public CfgDictionaryVo add(@RequestBody CfgDictionaryDto dto) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dto).code(ErrorMsgEnum.USER_NAME_NOT_NULL.getCode()).message(ErrorMsgEnum.USER_NAME_NOT_NULL.getDesc());
        return dictionaryApplicationService.add(dto);
    }

    @ApiOperation("新增或修改字典基本信息")
    @PostMapping("/saveOrUpdate")
    public CfgDictionaryVo saveOrUpdate(@RequestBody CfgDictionaryDto dto) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dto).notNull();
        return dictionaryApplicationService.saveOrUpdate(dto);
    }


    @ApiOperation("更改字典启用状态 0未启用，1启用，2禁用 bid必填，enableFlag必填")
    @PostMapping("/changeEnableFlag/{bid}/{enableFlag}")
    public Boolean changeEnableFlag(@PathVariable("bid") String bid,
                                    @PathVariable("enableFlag") Integer enableFlag) {
        return dictionaryApplicationService.changeEnableFlag(bid, enableFlag);
    }

    @ApiOperation("删除字典")
    @PostMapping("/logicalDelete/{bid}")
    public Boolean delete(@PathVariable("bid") String bid) {
        return dictionaryApplicationService.logicalDelete(bid);
    }

    @ApiOperation("新增或者更新字典基本信息+条目信息")
    @PostMapping("/saveOrUpdateDictionaryAndItem")
    public CfgDictionaryVo saveOrUpdateDictionaryAndItem(@RequestBody CfgDictionaryDto dto) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dto).notNull();
        return dictionaryApplicationService.saveOrUpdateDictionaryAndItem(dto);
    }

    @ApiOperation("校验并返回校验结果-批量新增或者更新字典基本信息+条目信息")
    @PostMapping("/checkReturnBlukDictionaryAndItem")
    public List<CfgDictionaryAndCheckVo> checkReturnBlukDictionaryAndItem(@RequestBody List<CfgDictionaryDto> dtos) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dtos).notNull();
        return dictionaryApplicationService.checkReturnBlukDictionaryAndItem(dtos);
    }


    @ApiOperation("批量新增或者更新字典基本信息+条目信息")
    @PostMapping("/blukSaveOrUpdateDictionaryAndItem")
    public Boolean blukSaveOrUpdateDictionaryAndItem(@RequestBody List<CfgDictionaryDto> dtos) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(dtos).notNull();
        return dictionaryApplicationService.blukSaveOrUpdateDictionaryAndItem(dtos);
    }

    @ApiOperation("根据bid查询字典基本信息+条目")
    @GetMapping("/getDictionaryAndItemByBid/{bid}")
    public CfgDictionaryVo getDictionaryAndItemByBid(@PathVariable String bid) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(bid).notNull();
        return dictionaryApplicationService.getDictionaryAndItemByBid(bid);
    }


    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgDictionaryVo> page(@ApiParam("分页查询参数") @RequestBody BaseRequest<CfgDictionaryQo> pageQo) {
        return dictionaryApplicationService.page(pageQo);
    }

    @ApiOperation("根据bid查询")
    @GetMapping("/get/{bid}")
    public CfgDictionaryVo getByBid(@PathVariable String bid) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(bid).notNull();
        return dictionaryApplicationService.getByBid(bid);
    }


    @ApiOperation("根据字典codes+enableFlag查询字典基本信息+条目")
    @PostMapping("/listDictionaryAndItemByCodesAndEnableFlags")
    public List<CfgDictionaryVo> listDictionaryAndItemByCodesAndEnableFlags(@RequestBody CfgDictionaryComplexQo qo) {
        // 判断一个对象合法性使用断言
        AssertBuilder.ctor(qo).notNull();
        return dictionaryApplicationService.listDictionaryAndItemByCodesAndEnableFlags(qo.getCodes(), qo.getEnableFlags());
    }

}
