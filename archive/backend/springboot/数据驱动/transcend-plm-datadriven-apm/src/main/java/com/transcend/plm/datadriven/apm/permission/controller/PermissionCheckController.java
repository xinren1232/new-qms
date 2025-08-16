package com.transcend.plm.datadriven.apm.permission.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionCheckDto;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionCheckService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author unknown
 */
@RestController
@RequestMapping("/permissionCheck")
@Api(value = "PermissionCheckController", tags = "权限检查管理")
public class PermissionCheckController {

    @Resource
    private IPermissionCheckService permissionCheckService;

    @PostMapping("/check")
    @ApiOperation("通用权限删除功能")
    public TranscendApiResponse<Boolean> checkPermission(@RequestBody PermissionCheckDto permissionCheckDto) {
        return TranscendApiResponse.success(permissionCheckService.checkPermission(permissionCheckDto));
    }

}
