package com.transcend.plm.datadriven.apm.permission.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAccessAddAO;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleAccessDomainService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAccessVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yuanhu.huang
 * @version 1.0.0
 * @Description 角色操作管理控制器
 * @createTime 2023-09-20 14:58:00
 */
@RestController
@RequestMapping("/apm/roleAccess")
@Api(value = "ApmRoleAccessController", tags = "敏捷项目管理-操作管理")
public class ApmRoleAccessController {
    @Resource
    private IApmRoleAccessDomainService apmRoleAccessDomainService;

    /**
     * 修改
     * @return
     */
    @PostMapping("/saveOrUpdate")
    @ApiOperation("新增或者修改操作")
    public TranscendApiResponse<Boolean> saveOrUpdate(@RequestBody ApmRoleAccessAddAO apmRoleAccessAddAO) {
        return TranscendApiResponse.success(apmRoleAccessDomainService.saveOrUpdate(apmRoleAccessAddAO));
    }

    /**
     * 查询
     * @return
     */
    @GetMapping("/list/{roleBid}")
    @ApiOperation("根据角色bid查询")
    public TranscendApiResponse<List<ApmRoleAccessVO>> listByRoleId(@ApiParam("roleBid") @PathVariable("roleBid") String roleBid){
        return TranscendApiResponse.success(apmRoleAccessDomainService.listByRoleBid(roleBid));
    }


}
