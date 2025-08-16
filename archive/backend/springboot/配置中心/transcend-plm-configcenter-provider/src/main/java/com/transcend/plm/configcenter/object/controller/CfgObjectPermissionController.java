package com.transcend.plm.configcenter.object.controller;

import com.transcend.plm.configcenter.object.domain.service.CfgObjectPermissionDomainService;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectPermissionSaveParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectPermissionVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * CfgObjectPermissionController
 *
 * @author jie.Luo1
 * @version: 1.0
 * @date 2022/12/12 10:26
 */
@Slf4j
@Api(value = "CfgObjectPermission Controller", tags = "对象管理-权限-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/object/permission")
public class CfgObjectPermissionController {

    @Resource
    private CfgObjectPermissionDomainService cfgObjectPermissionDomainService;

    @ApiOperation("根据modelCode查询权限列表")
    @GetMapping("listByModelCode/{modelCode}")
    public List<CfgObjectPermissionVo> listByModelCode(@PathVariable String modelCode) {
        return cfgObjectPermissionDomainService.listByModelCode(modelCode);
    }

    @ApiOperation("根据modelCode查询对象继承权限列表")
    @GetMapping("listInheritByModelCode/{modelCode}")
    public List<CfgObjectPermissionVo> listInheritByModelCode(@PathVariable String modelCode) {
        return cfgObjectPermissionDomainService.listInheritByModelCode(modelCode);
    }

    @ApiOperation("根据modelCode查询对象操作权限")
    @GetMapping("getOperationsByModelCode/{modelCode}")
    public List<String> getOperationsByModelCode(@PathVariable String modelCode) {
        return cfgObjectPermissionDomainService.getOperationsByModelCode(modelCode);
    }

    @ApiOperation("保存并修改权限")
    @PostMapping("saveOrUpdate")
    public CfgObjectPermissionVo saveOrUpdate(@RequestBody CfgObjectPermissionSaveParam cfgObjectPermissionSaveParam) {
        return cfgObjectPermissionDomainService.saveOrUpdate(cfgObjectPermissionSaveParam);
    }


    @ApiOperation("批量保存-并修改权限")
    @PostMapping("batchSaveOrUpdate")
    public Boolean batchSaveOrUpdate(@RequestBody List<CfgObjectPermissionSaveParam> cfgObjectPermissionSaveParams) {
        return cfgObjectPermissionDomainService.batchSaveOrUpdate(cfgObjectPermissionSaveParams);
    }


    @ApiOperation("批量覆盖权限")
    @PostMapping("batchCoverByModelCode/{modelCode}")
    public Boolean batchCoverByModelCode(@PathVariable("modelCode") String modelCode,
                                     @RequestBody List<CfgObjectPermissionSaveParam> cfgObjectPermissionSaveParams) {
        return cfgObjectPermissionDomainService.batchCoverByModelCode(modelCode, cfgObjectPermissionSaveParams);
    }

    @ApiOperation("逻辑删除")
    @PostMapping("logicalDelete/{bid}")
    public Boolean deleteObjectPermission(@PathVariable("bid") String bid) {
        return cfgObjectPermissionDomainService.logicalDeleteByBid(bid);
    }


}
