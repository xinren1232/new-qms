package com.transcend.plm.datadriven.apm.permission.driven.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.permission.driven.standard.factory.strategy.PermissionContext;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectAddParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectAddResult;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectOperationResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Controller
@RequestMapping("/permission/driven")
public class PermissionDrivenController {

    /**
     * 新增权限收集
     * @return
     */
    @PostMapping("/collectAdd")
    public TranscendApiResponse<List<CollectAddResult>> addCollect(@RequestBody CollectAddParam param){
        return TranscendApiResponse.success(PermissionContext.getFactoryByTenant().collectAdd(param));
    }

        /**
     * 实例权限收集
     * @return
     */
    @PostMapping("/collectOperation")
    public TranscendApiResponse<Map<String, List<CollectOperationResult>>> collectOperation(@RequestBody CollectOperationParam param){
        return TranscendApiResponse.success(PermissionContext.getFactoryByTenant().collectOperation(param));
    }

//    /**
//     * 新增权限收集
//     * @return
//     */
//    @PostMapping("/{permissionModel}/collectAdd")
//    public TranscendApiResponse<List<CollectAddResult>> addCollectByModel(@PathVariable("permissionModel") String permissionModel,
//                                                                   @RequestBody CollectAddParam param){
//        return TranscendApiResponse.success(PermissionContext.getFactory(permissionModel).collectAdd(param));
//    }
//
//    /**
//     * 实例权限收集
//     * @return
//     */
//    @PostMapping("/{permissionModel}/collectOperation")
//    public TranscendApiResponse<Map<String, List<CollectOperationResult>>> collectOperation(@PathVariable("permissionModel") String permissionModel,
//                                                                                      @RequestBody CollectOperationParam param){
//        return TranscendApiResponse.success(PermissionContext.getFactory(permissionModel).collectOperation(param));
//    }
}
