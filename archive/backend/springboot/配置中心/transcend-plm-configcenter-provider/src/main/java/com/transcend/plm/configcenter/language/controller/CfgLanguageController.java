package com.transcend.plm.configcenter.language.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.language.domain.service.CfgLanguageDomainService;
import com.transcend.plm.configcenter.language.pojo.dto.CfgLanguageDto;
import com.transcend.plm.configcenter.language.pojo.qo.CfgLanguageQo;
import com.transcend.plm.configcenter.language.pojo.vo.CfgLanguageVo;
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
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@RestController
@Api(value = "CfgLanguage Controller", tags = "多语言-控制器")
@RequestMapping(value ="/manager/cfg/language")
public class CfgLanguageController {
    @Resource
    private CfgLanguageDomainService cfgLanguageDomainService;

    @ApiOperation("新增或修改")
    @PostMapping("/saveOrUpdate")
    public CfgLanguageVo saveOrUpdate(@RequestBody CfgLanguageDto cfgLanguageDto) {
        return cfgLanguageDomainService.saveOrUpdate(cfgLanguageDto);
    }

    @GetMapping("/get/{bid}")
    @ApiOperation("查看详情")
    public CfgLanguageVo getByBid(@PathVariable("bid") String bid) {
        return cfgLanguageDomainService.getByBid(bid);
    }

    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgLanguageVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgLanguageQo> pageQo) {
        return cfgLanguageDomainService.page(pageQo);
    }

    @ApiOperation("批量新增")
    @PostMapping("/bulkAdd")
    public List<CfgLanguageVo> bulkAdd(@RequestBody List<CfgLanguageDto> cfgLanguageDtos) {
        return cfgLanguageDomainService.bulkAdd(cfgLanguageDtos);
    }

    @PostMapping("/logicalDelete/{bid}")
    @ApiOperation("删除")
    public Boolean logicalDeleteByBid(@PathVariable("bid") String bid) {
        return cfgLanguageDomainService.logicalDeleteByBid(bid);
    }

    @GetMapping("/version/get")
    @ApiOperation("查看版本")
    public Long getVersion() {
        return cfgLanguageDomainService.getVersion();
    }
}
