package com.transcend.plm.configcenter.permission.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.permission.application.service.ICfgPermissionApplicationService;
import com.transcend.plm.configcenter.permission.application.service.IPermissionConfigService;
import com.transcend.plm.configcenter.permission.domain.service.CfgPermissionDomainService;
import com.transcend.plm.configcenter.permission.pojo.dto.AppPermissionDto;
import com.transcend.plm.configcenter.permission.pojo.dto.CfgObjectPermissionOperationDto;
import com.transcend.plm.configcenter.permission.pojo.qo.CfgObjectPermissionOperationQo;
import com.transcend.plm.configcenter.permission.pojo.vo.CfgAttributeVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.permission.pojo.vo.ObjPermissionVo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@RestController
@Api(value = "CfgPermission Controller", tags = "权限-控制器")
@RequestMapping(value ="/manager/cfg/permission")
public class CfgPermissionController {
    @Resource
    private ICfgPermissionApplicationService iCfgPermissionApplicationService;
    @Resource
    private CfgPermissionDomainService cfgPermissionDomainService;

    @Resource
    private IPermissionConfigService permissionConfigService;


//    @ApiOperation("获取")
//    @GetMapping("/saveOrUpdate")
//    public CfgAttributeVo saveOrUpdate(@RequestBody CfgObjectPermissionOperationDto cfgAttributeDto) {
//        return iCfgPermissionApplicationService.saveOrUpdate(cfgAttributeDto);
//    }



    @ApiOperation("新增或者修改对象权限")
    @PostMapping("/saveOrUpdateObjPermission")
    public Boolean saveOrUpdateObjPermission(@RequestBody AppPermissionDto appPermissionDto) {
        return permissionConfigService.saveOrUpdateBasePermissionConfig(appPermissionDto);
    }

    @ApiOperation("单个角色权限删除")
    @PostMapping("/deletePermission")
    public Boolean deletePermission(@RequestBody AppPermissionDto appPermissionDto) {
        return permissionConfigService.deletePermission(appPermissionDto);
    }

    @GetMapping("/listObjPermissions/{modelCode}")
    @ApiOperation("应用条件权限列表")
    public ObjPermissionVo listObjPermissions(@PathVariable String modelCode) {
        return permissionConfigService.listObjPermissions(modelCode);
    }

    @PostMapping("/deleteByPermissionBid/{modelCode}/{permissionBid}")
    @ApiOperation("应用条件权限删除")
    public Boolean deleteByPermissionBid(@PathVariable String modelCode, @PathVariable String permissionBid) {
        return permissionConfigService.deleteByPermissionBid(modelCode,permissionBid);
    }



    @ApiOperation("新增属性基本信息")
    @PostMapping("/saveOrUpdate")
    public CfgAttributeVo saveOrUpdate(@RequestBody CfgObjectPermissionOperationDto cfgAttributeDto) {
        return iCfgPermissionApplicationService.saveOrUpdate(cfgAttributeDto);
    }

    @GetMapping("/get/{bid}")
    @ApiOperation("查看详情")
    public CfgAttributeVo getByBid(@PathVariable("bid") String bid) {
        return cfgPermissionDomainService.getByBid(bid);
    }

    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgAttributeVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgObjectPermissionOperationQo> pageQo) {
        return cfgPermissionDomainService.page(pageQo);
    }

    @ApiOperation("批量新增")
    @PostMapping("/bulkAdd")
    public List<CfgAttributeVo> bulkAdd(@RequestBody List<CfgObjectPermissionOperationDto> cfgAttributeDtos) {
        return cfgPermissionDomainService.bulkAdd(cfgAttributeDtos);
    }

    @PostMapping("/logicalDelete/{bid}")
    @ApiOperation("删除")
    public Boolean logicalDeleteByBid(@PathVariable("bid") String bid) {
        return cfgPermissionDomainService.logicalDeleteByBid(bid);
    }


    @PostMapping("/distributePermission/{modelCode}")
    @ApiOperation("下发对象权限 下发目标包括（对象子类，对象空间，对象应用 排除实例的四有权限）")
    public Boolean distributePermission(@PathVariable String modelCode) {
        return permissionConfigService.distributePermissions(Collections.singletonList(modelCode));
    }

    @PostMapping("/distributeObjectPermission/{modelCode}")
    @ApiOperation("根据其子类下发对象权限到所有子类")
    public Boolean distributeObjectPermission(@PathVariable String modelCode) {
        return permissionConfigService.distributeObjectPermission(modelCode);
    }

    @GetMapping("/objAllButton/{modelCode}")
    @ApiOperation("根据应用ID查询对象下所有的权限按钮")
    public TranscendApiResponse<List<Map<String,String>>> queryAllOperationByAppId(@PathVariable("modelCode") String modelCode) {
        return TranscendApiResponse.success(permissionConfigService.queryAllOperationByModeCode(modelCode));
    }
}
