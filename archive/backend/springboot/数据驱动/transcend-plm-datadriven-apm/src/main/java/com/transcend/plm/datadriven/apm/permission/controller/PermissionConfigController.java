package com.transcend.plm.datadriven.apm.permission.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.AppPermissionDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.AppBasePermissionVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.AppConditionPermissionVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ConditionOperatorVo;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author unknown
 */
@RestController
@RequestMapping("/permission")
@Api(value = "PermissionConfigController", tags = "权限配置管理")
public class PermissionConfigController {

    @Resource
    private IPermissionConfigService permissionConfigService;

    @PostMapping("config/deletePermission")
    @ApiOperation("通用权限删除功能")
    public TranscendApiResponse<Boolean> deletePermission(@RequestBody AppPermissionDto appPermissionDto) {
        return TranscendApiResponse.success(permissionConfigService.deletePermission(appPermissionDto));
    }

    @PostMapping("config/saveOrUpdateBasePermission")
    @ApiOperation("应用权限新增或者修改操作")
    public TranscendApiResponse<Boolean> saveOrUpdateBasePermission(@RequestBody AppPermissionDto appPermissionDto) {
        return TranscendApiResponse.success(permissionConfigService.saveOrUpdateAppBasePermissionConfig(appPermissionDto));
    }

    @PostMapping("config/saveOrUpdateInstancePermissionConfig")
    @ApiOperation("实例权限新增或者修改操作")
    public TranscendApiResponse<Boolean> saveOrUpdateInstancePermissionConfig(@RequestBody AppPermissionDto appPermissionDto) {
        return TranscendApiResponse.success(permissionConfigService.saveOrUpdateInstancePermissionConfig(appPermissionDto));
    }

    @PostMapping("config/saveOrUpdateAppConditionPermission")
    @ApiOperation("应用条件权限新增")
    public TranscendApiResponse<Boolean> saveOrUpdateAppConditionPermission(@RequestBody AppPermissionDto appPermissionDto) {
        return TranscendApiResponse.success(permissionConfigService.saveAppConditionPermission(appPermissionDto));
    }

    @PostMapping("config/deleteAppConditionPermission/{spaceAppBid}/{permissionBid}")
    @ApiOperation("应用条件权限删除")
    public TranscendApiResponse<Boolean> deleteAppConditionPermission(@PathVariable String spaceAppBid, @PathVariable String permissionBid) {
        return TranscendApiResponse.success(permissionConfigService.deleteConditionPermission(spaceAppBid,permissionBid));
    }

    @PostMapping("config/deleteInstancePermission/{spaceAppBid}/{instanceBid}")
    @ApiOperation("实例权限删除")
    public TranscendApiResponse<Boolean> deleteInstancePermission(@PathVariable String spaceAppBid, @PathVariable String instanceBid) {
        return TranscendApiResponse.success(permissionConfigService.deleteInstancePermission(spaceAppBid, instanceBid));
    }

    @GetMapping("config/listAppBasePermissions/{spaceAppBid}")
    @ApiOperation("应用权限查询")
    public TranscendApiResponse<AppBasePermissionVo> listAppBasePermissions(@PathVariable String spaceAppBid) {
        return TranscendApiResponse.success(permissionConfigService.listAppBasePermissions(spaceAppBid,null));
    }
    @GetMapping("config/getConditionOperators")
    @ApiOperation("条件运算符查询")
    public TranscendApiResponse<List<ConditionOperatorVo>> getConditionOperators(){
        return TranscendApiResponse.success(permissionConfigService.getConditionOperators());
    }

    @GetMapping("config/listInstancePermissions/{spaceAppBid}/{instanceBid}")
    @ApiOperation("实例权限查询")
    public TranscendApiResponse<AppBasePermissionVo> listInstancePermissions(@PathVariable String spaceAppBid, @PathVariable String instanceBid) {
        return TranscendApiResponse.success(permissionConfigService.listAppBasePermissions(spaceAppBid,instanceBid));
    }

    @GetMapping("config/listAppConditionPermission/{spaceAppBid}")
    @ApiOperation("应用条件权限查询")
    public TranscendApiResponse<List<AppConditionPermissionVo>> listAppConditionPermission(@PathVariable String spaceAppBid) {
        return TranscendApiResponse.success(permissionConfigService.listAppConditionPermission(spaceAppBid));
    }
}
