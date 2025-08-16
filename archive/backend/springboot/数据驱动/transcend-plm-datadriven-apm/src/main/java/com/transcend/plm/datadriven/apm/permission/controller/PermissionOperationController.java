package com.transcend.plm.datadriven.apm.permission.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.PermissionPlmOperationMapAO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.PermissionPlmOperationMapVO;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionOperationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@RestController
@Api(value = "PermissionOperationController", tags = "操作权限配置管理")
public class PermissionOperationController {

    @Resource
    private IPermissionOperationService permissionOperationService;


    /**
     * 新增应用下的扩展操作权限
     *
     * @param permissionPlmOperationMapAO 映射实体
     * @return {@link TranscendApiResponse< Boolean>}
     * @date 2024/5/7 15:38
     * @author quan.cheng
     */
    @PostMapping("/permission/operation/saveOrUpdate/app/{spaceAppBid}")
    @ApiOperation("新增或者修改应用下的扩展操作权限")
    public TranscendApiResponse<Boolean> saveOrUpdate(@PathVariable("spaceAppBid") String spaceAppBid,@RequestBody PermissionPlmOperationMapAO permissionPlmOperationMapAO) {
        return TranscendApiResponse.success(permissionOperationService.saveOrUpdate(spaceAppBid,permissionPlmOperationMapAO));
    }

    @PostMapping("/permission/operation/delete")
    @ApiOperation("删除应用下的扩展操作权限")
    public TranscendApiResponse<Boolean> delete(@RequestBody List<PermissionPlmOperationMapAO> permissionPlmOperationMapAOs) {
        return TranscendApiResponse.success(permissionOperationService.delete(permissionPlmOperationMapAOs));
    }

    @PostMapping("/permission/operation/query/app/{spaceAppBid}")
    @ApiOperation("根据应用ID查询应用下自定义的权限按钮")
    public TranscendApiResponse<List<PermissionPlmOperationMapVO>> queryOperationByAppId(@PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(permissionOperationService.queryOperationByAppId(spaceAppBid));
    }

    @GetMapping("/permission/operation/query/appAllButton/{spaceAppBid}")
    @ApiOperation("根据应用ID查询应用下所有的权限按钮")
    public TranscendApiResponse<List<Map<String,String>>> queryAllOperationByAppId(@PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(permissionOperationService.queryAllOperationByAppId(spaceAppBid));
    }
}
